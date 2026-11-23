# Mi Tienda Cerca – Proyecto final de Programación Web (USCO)

Juan Camilo Mayor Escobar · código 20251231315 · Docente: Diego Andrés Carvajal

Aplicación web para que la tienda de barrio reciba, gestione y despache pedidos a domicilio, con fiado digital, seguimiento del repartidor en un mapa y un asistente que arma el carrito a partir de un texto, una nota de voz o la foto de una lista. Esta instalación es para Agropita, la primera empresa que adquirió el aplicativo.

## Carpetas

| Carpeta | Qué hay |
|---|---|
| `01-documentacion` | Anteproyecto y memoria del proyecto |
| `02-backend-spring-boot` | API REST en Java con Spring Boot (proyecto Maven) |
| `03-frontend-angular` | Aplicación en Angular + Bootstrap y página de inicio en jQuery |
| `04-base-de-datos` | Scripts SQL de PostgreSQL y MySQL (tablas, triggers y catálogo) |
| `05-herramientas` | Instalador de Lombok para Eclipse |

## Cómo se ejecuta (modo local)

1. Encender PostgreSQL (puerto 5432) y MySQL (puerto 3307) y crear la base `tienda_cerca` en PostgreSQL con `04-base-de-datos/script.sql`.
2. En `02-backend-spring-boot/src/main/resources/application.properties` poner el usuario y la clave de PostgreSQL y MySQL.
3. Back-end: importar `02-backend-spring-boot` en Eclipse (Import → Existing Maven Projects, con Lombok instalado) y ejecutar `TiendaCercaApplication`. Queda en http://localhost:8080 (Swagger en http://localhost:8080/swagger-ui/index.html).
4. Front-end: en `03-frontend-angular` ejecutar `npm install` (solo la primera vez) y luego `npm start`. Queda en http://localhost:4200.
5. Opcional: variable de entorno `GEMINI_API_KEY` para leer fotos de listas con IA.

Cuentas de prueba (clave `123456`): `maria@correo.com` (cliente), `tendero@tienda.com`, `repartidor@tienda.com`, `asesor@tienda.com`.
