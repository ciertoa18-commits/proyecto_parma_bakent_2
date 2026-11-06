package pe.edu.upeu.PharmaBackend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {
    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    @Override
    public CacheErrorHandler errorHandler() {
        return new SimpleCacheErrorHandler() {
            private void advertir(Cache cache, String operacion, RuntimeException ex) {
                // No registrar valores ni mensajes que puedan contener credenciales de conexion.
                log.warn("Cache {} no disponible al {} | {}", cache.getName(),
                        operacion, ex.getClass().getSimpleName());
            }

            @Override
            public void handleCacheGetError(RuntimeException ex, Cache cache, Object key) {
                advertir(cache, "leer", ex);
            }

            @Override
            public void handleCachePutError(RuntimeException ex, Cache cache, Object key, Object value) {
                advertir(cache, "guardar", ex);
            }

            @Override
            public void handleCacheEvictError(RuntimeException ex, Cache cache, Object key) {
                advertir(cache, "invalidar", ex);
            }

            @Override
            public void handleCacheClearError(RuntimeException ex, Cache cache) {
                advertir(cache, "limpiar", ex);
            }
        };
    }
}
