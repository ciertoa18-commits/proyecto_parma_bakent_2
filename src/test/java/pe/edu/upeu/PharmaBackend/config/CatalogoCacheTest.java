package pe.edu.upeu.PharmaBackend.config;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import pe.edu.upeu.PharmaBackend.dto.CategoriaRequestDTO;
import pe.edu.upeu.PharmaBackend.entity.Categoria;
import pe.edu.upeu.PharmaBackend.mapper.CategoriaMapper;
import pe.edu.upeu.PharmaBackend.repository.CategoriaRepository;
import pe.edu.upeu.PharmaBackend.repository.ProductoRepository;
import pe.edu.upeu.PharmaBackend.service.impl.CategoriaServiceImpl;
import pe.edu.upeu.PharmaBackend.service.service.CategoriaService;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CatalogoCacheTest {
    private AnnotationConfigApplicationContext contexto(CacheManager manager, CategoriaRepository repo) {
        var ctx = new AnnotationConfigApplicationContext();
        ctx.registerBean(CacheManager.class, () -> manager);
        ctx.registerBean(CategoriaRepository.class, () -> repo);
        ctx.registerBean(ProductoRepository.class, () -> mock(ProductoRepository.class));
        ctx.register(CacheConfig.class, CategoriaMapper.class, CategoriaServiceImpl.class);
        ctx.refresh();
        return ctx;
    }

    @Test
    void evitaSegundaConsultaEInvalidaAmbosCatalogosAlRenombrar() {
        var repo = mock(CategoriaRepository.class);
        var categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Original");
        when(repo.findAll()).thenReturn(List.of(categoria));
        when(repo.findById(1L)).thenReturn(Optional.of(categoria));
        when(repo.saveAndFlush(categoria)).thenReturn(categoria);
        var manager = new ConcurrentMapCacheManager("categorias", "productos");
        try (var ctx = contexto(manager, repo)) {
            var service = ctx.getBean(CategoriaService.class);
            service.listar();
            service.listar();
            verify(repo, times(1)).findAll();
            manager.getCache("productos").put("id-9", "categoria original");
            service.actualizar(1L, new CategoriaRequestDTO("Actualizada", null, true));
            assertThat(manager.getCache("productos").get("id-9")).isNull();
            assertThat(service.listar().getFirst().getNombre()).isEqualTo("Actualizada");
            verify(repo, times(2)).findAll();
        }
    }

    @Test
    void respondeDesdeRepositorioAunqueLaCacheNoPermitaLeerNiGuardar() {
        var repo = mock(CategoriaRepository.class);
        when(repo.findAll()).thenReturn(List.of());
        var cache = mock(Cache.class);
        when(cache.getName()).thenReturn("categorias");
        when(cache.get(any())).thenThrow(new IllegalStateException("Redis caido"));
        doThrow(new IllegalStateException("Redis caido")).when(cache).put(any(), any());
        var manager = mock(CacheManager.class);
        when(manager.getCache("categorias")).thenReturn(cache);
        try (var ctx = contexto(manager, repo)) {
            assertThat(ctx.getBean(CategoriaService.class).listar()).isEmpty();
            verify(repo).findAll();
        }
    }
}
