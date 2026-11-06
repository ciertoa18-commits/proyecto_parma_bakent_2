package pe.edu.upeu.PharmaBackend.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

/*
 * Envoltorio estable para respuestas paginadas. Se usa en lugar de
 * serializar Page directamente para que el contrato JSON no dependa de
 * la estructura interna de Spring Data.
 */
@Getter
@AllArgsConstructor
public class PaginaResponseDTO<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<T> contenido;
    private int pagina;
    private int tamanio;
    private long totalElementos;
    private int totalPaginas;
    private boolean ultima;

    public static <T> PaginaResponseDTO<T> de(Page<T> page) {
        return new PaginaResponseDTO<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
