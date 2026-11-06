# Backend â€” Sesion 13

Requiere Java 21 o superior, Oracle con el esquema del proyecto y Docker para Redis.

Desde esta carpeta, iniciar Redis y ejecutar el backend en desarrollo:

```powershell
docker compose up -d redis
.\mvnw.cmd spring-boot:run
```

En desarrollo debes definir DB_PASSWORD y JWT_SECRET como variables de entorno. DB_USERNAME tiene Prarmas como valor por defecto. En produccion las credenciales y la clave JWT deben llegar mediante variables de entorno:

```powershell
$env:SPRING_PROFILES_ACTIVE="prod"
$env:DB_URL="jdbc:oracle:thin:@//localhost:1522/FREEPDB1"
$env:DB_USERNAME="PHARMADB"
$env:DB_PASSWORD="<clave de Oracle>"
$env:JWT_SECRET="<clave Base64 de al menos 32 bytes>"
$env:FRONTEND_ORIGINS="http://localhost:8081"
$env:REDIS_HOST="localhost"
$env:REDIS_PORT="6379"
.\mvnw.cmd clean package
java -jar target/PharmaBackend-0.0.1-SNAPSHOT.jar
```

Redis tiene un TTL de 10 minutos y tiempos de conexion/comando de 500 ms. Los DTO son serializables. Categorias y productos se invalidan despues de escrituras exitosas; renombrar una categoria invalida tambien productos. Registrar ventas invalida productos por el descuento de stock. El proyecto base no tiene una operacion de anulacion de ventas.

Si Redis falla, el manejador registra WARN y permite consultar Oracle. La salud incluye Redis, de modo que puede responder DOWN/503 mientras el API sigue atendiendo. Si una invalidacion falla durante una caida, una entrada previa puede conservarse hasta su TTL al recuperar Redis.

Cada respuesta incluye `X-Request-Id`, expuesto por CORS. Los logs de acceso contienen metodo, ruta sin query string, estado y duracion. En prod se escriben en `logs/pharma-backend.log`, con rotacion de 10 MB y 14 dias.

## Comprobacion manual

1. Obtener token mediante `POST /api/v1/auth/login` y usar `Authorization: Bearer <token>`.
2. Ejecutar dos `GET /api/v1/categorias`: el segundo debe evitar SQL. Consultar `docker compose exec redis redis-cli GET categorias::todas` y `docker compose exec redis redis-cli TTL categorias::todas`.
3. Crear/actualizar/eliminar categorias o productos y volver a consultar. Al renombrar categorias se actualiza `categoriaNombre`; tras registrar una venta se actualiza stock.
4. Consultar `GET /actuator/health` sin token: estado sin detalles. Con ADMIN: detalles de db, redis y diskSpace.
5. Consultar `/actuator/metrics` y `/actuator/metrics/http.server.requests` con ADMIN. VENDEDOR debe recibir 403 y un anonimo 401.
6. Consultar `/actuator/metrics/cache.gets?tag=name:categorias&tag=result:hit` con ADMIN despues de las dos lecturas. Las estadisticas son locales a la instancia del backend.
7. Detener Redis con `docker compose stop redis`, volver a consultar categorias y verificar respuesta desde Oracle, WARN y salud DOWN. Recuperar con `docker compose start redis`.
8. Comparar `X-Request-Id` con los logs. Verificar ausencia de tokens y contrasenas en el archivo de produccion.

Para medir antes/despues sin cambiar codigo, ejecutar una instancia con `--spring.cache.type=none`, registrar consultas SQL y tiempos de dos GET consecutivos y repetir con Redis. No se incluyen mediciones inventadas.

Las pruebas unitarias y de proxy de cache no requieren Oracle ni Redis:

```powershell
.\mvnw.cmd test "-Dtest=ProductoServiceImplTest,VentaServiceImplTest,CatalogoCacheTest,RequestIdFilterTest"
```

La prueba `PharmaBackendApplicationTests` carga toda la aplicacion y requiere Oracle disponible. El frontend, Nginx, las capturas y los cambios en repositorios remotos quedan fuera de esta implementacion de backend.

En esta copia para GitHub, configurar DB_PASSWORD y JWT_SECRET tambien para el perfil dev antes de ejecutar Maven.
