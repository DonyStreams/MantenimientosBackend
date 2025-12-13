# Informe Técnico del Proyecto (Formato Word)

**Institución:** Instituto Nacional de Ciencias Forenses de Guatemala (INACIF)  
**Proyecto:** Desarrollo de una herramienta de software para la programación y registro de mantenimientos preventivos y correctivos  
**Versión:** 1.0  
**Fecha:** ____________________  
**Elaborado por:** ____________________  

> Recomendación: para Word, abre este archivo o usa la versión RTF y aplica estilos “Título 1 / Título 2” para generar tabla de contenido automáticamente.

## 1. Identificación del proyecto

**Nombre del proyecto:** Desarrollo de una herramienta de software para la programación y registro de mantenimientos preventivos y correctivos de equipos en INACIF.

**Institución:** Instituto Nacional de Ciencias Forenses de Guatemala (INACIF).

**Tipo de solución:** Sistema web (SPA + API REST) para gestión de activos, mantenimientos, programación, alertas, tickets, contratos y reportes.

**Ámbito de operación:** Red interna institucional (sin soporte offline). Navegadores compatibles: Firefox 60+, Chrome 78+ (según requerimientos no funcionales definidos).

**Normativa/Contexto de calidad:** Prioriza equipos críticos de laboratorio vinculados a procesos bajo ISO/IEC 17025 (trazabilidad, historial, evidencia y control).

**Licenciamiento (según consideraciones generales):** Creative Commons Reconocimiento – No Comercial – Compartir Igual 3.0 Guatemala.

**Confidencialidad:** la información y documentación institucional se maneja con confidencialidad y uso interno.

---

## 2. Resumen ejecutivo (referencia base)

El proyecto moderniza y centraliza la gestión de activos y mantenimientos del INACIF. Reemplaza controles semi‑automatizados (hojas de cálculo y registros manuales) por un sistema web con trazabilidad histórica, programación de mantenimientos, registro documental de ejecuciones, alertas y reportes.

El sistema está orientado a mejorar eficiencia operativa, transparencia y productividad institucional, alineado con lineamientos de Gobierno Electrónico.

**Visión:** ampliar la vida útil y garantizar la correcta operación de los equipos administrados por el INACIF mediante una herramienta que permita programar, registrar y dar seguimiento a tareas preventivas y correctivas.

**Entregables esperados (marco de referencia):**
- Módulo de seguridad con roles y permisos.
- Catálogo de equipos, historial y control.
- Programación de mantenimientos preventivos/correctivos.
- Registro de ejecución de mantenimientos.
- Alertas y notificaciones (revisión automática y ejecución manual).
- Reportes en formatos Excel/PDF (en implementación actual se entrega como TXT/CSV “simplificado”).

---

## 3. Alcance funcional del sistema

Esta sección describe la funcionalidad desde el punto de vista de negocio, agrupada por módulos, y resaltando reglas clave.

### 3.1 Gestión de usuarios (integración con Keycloak + BD local)
- **Autenticación y autorización** centralizadas en **Keycloak** (OpenID Connect / OAuth2).
- Los usuarios **no se “administran” completamente** en la BD de la aplicación: la identidad y roles provienen de Keycloak.
- La aplicación mantiene una tabla local de usuarios para:
  - Control de estado **activo/inactivo** (bloqueo de acceso sin borrar físicamente).
  - Auditoría/trazabilidad (usuario creación/modificación en contratos, equipos, etc.).
  - Integridad referencial con mantenimientos/tickets.

**Regla clave:** no se elimina físicamente un usuario por trazabilidad; se gestiona por activación/desactivación.

**Implicación operativa:** un usuario puede autenticarse en Keycloak, pero el sistema puede denegar acceso si el usuario está desactivado en la base local (control interno sin borrar identidad institucional).

### 3.2 Gestión de equipos
Funcionalidades principales:
- Alta, consulta, actualización y consulta por ID.
- Validación de unicidad del **código INACIF**.
- Asociación a **áreas** y **categorías**.
- Manejo de **imágenes** asociadas al equipo (subida y visualización).
- Registro de auditoría/historial simplificado (acciones como CREACION/ACTUALIZACION).

**Datos típicos gestionados:** identificación del equipo, datos técnicos (marca/modelo/serie), ubicación, área/categoría, condiciones de operación y documentación asociada (manuales/firmware cuando aplica).

### 3.3 Gestión de mantenimientos (ejecución y trazabilidad)
- Registro de ejecuciones de mantenimiento (preventivo/correctivo/calibración, según catálogos).
- Relación con:
  - Equipo
  - Tipo de mantenimiento
  - Contrato (cuando aplica)
  - Evidencias (archivos)
  - Estado/flujo operativo

**Objetivo de negocio:** garantizar que cada mantenimiento ejecutado quede documentado con trazabilidad mínima (equipo, fecha, tipo, contrato/proveedor si aplica y evidencias).

### 3.4 Programación de mantenimientos (planificación)
- CRUD de programaciones.
- Cálculo y control de:
  - Frecuencia en días.
  - Fecha de último mantenimiento.
  - Fecha de próximo mantenimiento.
  - Días de alerta previa.
  - Activación/desactivación de programación.

  **Resultados esperados:** convertir la gestión reactiva en proactiva, habilitando alertas por proximidad o vencimiento.

### 3.5 Alertas de mantenimiento
- Dashboard de alertas:
  - Total de programaciones activas.
  - Total de alertas próximas (por defecto 7 días).
  - Total de vencidas.
- Consultas:
  - Próximas por cantidad de días.
  - Vencidas.
  - Por rango de fechas.
- Acciones manuales:
  - Ejecutar revisión de alertas.
  - Ejecutar revisión de vencidos.

**Regla de clasificación:**
- *Próximas*: fecha próxima dentro del umbral definido (por defecto 7 días).
- *Vencidas*: fecha próxima anterior a la fecha actual.

### 3.6 Tickets de falla
- Registro de tickets vinculados a un equipo.
- Datos relevantes:
  - Descripción, prioridad, estado, fechas.
  - Usuario creador y usuario asignado.
- Soporte para evidencias asociadas (por archivos permitidos).

**Objetivo de negocio:** centralizar la atención de incidentes/fallas y vincularlos al activo afectado, evitando pérdida de información y permitiendo seguimiento.

### 3.7 Contratos y proveedores
- CRUD y consulta de estadísticas:
  - Total, vigentes, por vencer (30 días), vencidos, inactivos.
- Relación contrato–proveedor y contrato–equipo.
- Gestión documental de contratos:
  - Subida de archivos (PDF/DOC/DOCX) y registro en BD.
  - Descarga/listado/eliminación.

**Indicadores de gestión (ejemplos):** número de contratos vigentes, contratos por vencer (ej. a 30 días), contratos vencidos y contratos inactivos.

### 3.8 Reportes
- Reportes de equipos:
  - “PDF” (actualmente entregado como texto con header de PDF/descarga).
  - Excel (actualmente CSV).
- Reportes de mantenimientos:
  - TXT con filtros por fecha.

**Nota importante:** algunos reportes etiquetados como “PDF/Excel” están implementados actualmente como texto/CSV (descarga) y pueden evolucionar a formatos finales (PDF real y XLSX) sin cambiar la lógica de negocio.

---

## 4. Roles, permisos y control de acceso

### 4.1 Roles definidos (Keycloak)
En la configuración de Keycloak se definen roles de cliente para `inacif-frontend`:
- `ADMIN`: administración completa.
- `SUPERVISOR`: gestión de equipos y mantenimientos.
- `TECNICO`: ejecución/operación de mantenimientos.
- `TECNICO_EQUIPOS`: gestión enfocada en equipos.
- `USER`: solo lectura.

### 4.2 Protección de rutas y UI (Frontend Angular)
- Guard de autenticación:
  - Verifica sesión en Keycloak.
  - Verifica roles requeridos declarados en ruta.
  - Valida **estado activo** consultando `/api/usuarios/me`.
- Directiva de roles:
  - Permite/oculta elementos del UI según rol.

**Validación adicional de negocio:** el frontend valida estado activo del usuario consultando el endpoint del backend; ante un usuario desactivado, el UI redirige a acceso denegado.

### 4.3 Protección de APIs (Backend)
- La API REST opera bajo la ruta base `/api`.
- Se valida el token JWT en backend:
  - Se inspecciona el header `Authorization: Bearer <token>`.
  - Se descarga el JWKS de Keycloak (certificados) y se valida firma/issuer/audience.
  - Se extraen `preferred_username`, `email`, `sub` (Keycloak ID) y roles.
  - Se colocan estos valores como atributos del request para auditoría y lógica posterior.

**Rutas públicas definidas (sin JWT):** health/status y endpoints de testing (FTP/imagenes) según configuración de filtros.

**Observación técnica:** existen dos mecanismos visibles de validación/soporte de seguridad y CORS: un filtro JAX-RS (`JWTAuthenticationFilter`) y un filtro Servlet (`CORSResponseFilter`). Ambos participan en la validación y en la propagación de datos del usuario a la petición.

---

## 5. Arquitectura del sistema

### 5.1 Visión general
Arquitectura web de dos capas principales:
- **Frontend:** SPA en Angular.
- **Backend:** API REST desplegada como WAR en Apache TomEE.

Complementos:
- **Keycloak** como IdP (SSO) para autenticación/roles.
- Persistencia en **SQL Server** (driver configurado) y modelo JPA.
- Manejo de archivos mediante almacenamiento local y/o volúmenes Docker.

**Patrón principal:** aplicación web con separación de responsabilidades:
- **Presentación:** Angular (SPA)
- **Servicios:** API REST (JAX-RS)
- **Datos:** JPA/SQL Server
- **Identidad:** Keycloak

### 5.2 Estilo arquitectónico
- **Monolito modular**: módulos funcionales agrupados en controladores, servicios y repositorios, con separación por dominios (mantenimientos, seguridad, utilidades).
- Enfoque orientado a servicios y repositorios:
  - Controladores JAX-RS: exponen endpoints.
  - Servicios: lógica de negocio (ej. alertas/bitácora).
  - Repositorios: consultas y persistencia (DeltaSpike Data/JPA + queries nativas).

**Motivación del diseño:** facilitar mantenimiento evolutivo, aislando reglas de negocio en servicios y accediendo a datos mediante repositorios, conservando el despliegue como un solo artefacto backend (WAR).

### 5.3 Componentes backend (Java)
**Capa API (JAX-RS / Apache CXF):**
- Application config: registro explícito de controladores y filtros.
- Controladores por dominio: equipos, contratos, tickets, programaciones, alertas, reportes, etc.

**Capa de seguridad:**
- Filtro JAX-RS de autenticación JWT.
- Filtro Servlet para CORS (habilita Angular) y refuerza validación JWT.

**Persistencia:**
- JPA con EclipseLink.
- Repositorios DeltaSpike (y consultas JPQL/nativas en controladores).

**Archivos:**
- Imágenes de equipos: directorio local `inacif-imagenes/equipos`.
- Documentos de contratos: directorio local `contratos-archivos` y tabla `Documentos_Contrato`.
- Evidencias de tickets: directorio local `inacif-evidencias/tickets`.

**Configuración JAX-RS:** la API se publica bajo `/api` y registra explícitamente controladores, filtros de seguridad y provider de multipart.

### 5.4 Componentes frontend (Angular)
- SPA modular con:
  - Guards de autenticación/roles.
  - Interceptor HTTP que adjunta JWT a cada request.
  - Directivas para control visual por roles.
- Librerías UI y productividad:
  - PrimeNG/PrimeFlex/PrimeIcons.
  - FullCalendar (agenda/calendarios).
  - Chart.js (gráficas).
  - ExcelJS + file-saver (exportación).

**Integración API:** el frontend consume el backend por medio de `environment.apiUrl` y añade el JWT vía interceptor HTTP.

### 5.5 Integración con Keycloak (SSO)
- Realm: `MantenimientosINACIF`.
- Clientes:
  - `inacif-frontend` (OIDC para el SPA).
  - `inacif-backend` (bearer-only para API).
- Claims clave utilizados en la aplicación:
  - `preferred_username`, `email`, `sub`.
  - `resource_access.inacif-frontend.roles`.

---

## 6. Lógica de negocio (reglas y flujos)

### 6.1 Flujo de autenticación y autorización
1. Usuario ingresa al frontend.
2. El frontend redirige a Keycloak para login (OIDC).
3. Angular obtiene token JWT y lo adjunta como `Bearer` en cada request.
4. Backend valida el token contra JWKS de Keycloak.
5. Backend extrae identidad/roles y los expone como atributos del request.
6. Frontend consulta `/api/usuarios/me` para validar si el usuario está activo:
   - Si está desactivado en BD: bloquea el acceso y redirige a “acceso denegado”.

**Resultado del flujo:** la sesión queda gobernada por dos controles: (a) autenticación/roles en Keycloak y (b) habilitación operativa en la BD local.

### 6.2 Reglas para gestión de usuarios (BD local)
- Si el usuario no existe en BD, se habilita auto‑sincronización (creación “on-demand”).
- Si existe, debe estar **activo** para poder operar.
- Cambio de estado (activar/desactivar) invalida cachés del EntityManager.

### 6.3 Reglas para equipos
- El `codigoInacif` debe ser único (evita duplicados).
- Creación/actualización registra historial/auditoría con el usuario autenticado (cuando está sincronizado en BD).
- Imágenes solo aceptan extensiones permitidas (jpg/jpeg/png/gif/webp/bmp).

### 6.4 Reglas para contratos
- Un contrato puede estar vigente, por vencer o vencido según `fechaFin`.
- Manejo de documentos: solo PDF/DOC/DOCX.
- Se registra metadata del archivo (tamaño, mime, fecha) en la tabla de documentos.

### 6.5 Programación de mantenimientos y alertas
- Programación define frecuencia (días) y días de alerta previa.
- Se calcula la fecha próxima de mantenimiento.
- Se clasifican programaciones en:
  - Próximas: dentro del umbral (ej. 7 días).
  - Vencidas: fecha próxima anterior a la fecha actual.
- Dashboard consolida métricas para supervisión.

### 6.6 Tickets
- Ticket requiere:
  - Descripción.
  - Equipo válido.
  - Usuario creador válido.
- Se registra en bitácora la creación del ticket.
- Soporta evidencias con restricciones de extensión.

---

## 7. Tecnologías, herramientas y estándares

### 7.1 Backend
- Java (aplicación empaquetada como WAR).
- Jakarta/Java EE (JAX-RS, CDI, JPA).
- Apache TomEE (runtime de despliegue).
- Apache CXF (JAX-RS runtime y multipart provider).
- Maven (build y dependencias).
- EclipseLink (JPA provider).
- DeltaSpike (repositorios y módulos CDI/JPA).
- SQL Server JDBC driver.
- JOSE4J (validación de JWT y JWKS).

### 7.2 Frontend
- Angular (SPA).
- RxJS.
- Keycloak JS.
- PrimeNG / PrimeFlex / PrimeIcons.
- FullCalendar.
- Chart.js.
- ExcelJS + file-saver.

### 7.3 DevOps / despliegue
- Docker + docker-compose.
- Contenedor de TomEE para desplegar el WAR.
- Volúmenes para persistencia de archivos (imágenes y contratos).

### 7.4 Calidad y pruebas (objetivo)
- En el marco técnico se recomienda:
  - Unitarias: JUnit 4.
  - Integración: Arquillian / REST-assured.
  - Migraciones: Flyway.
  - Entorno aislado: Testcontainers.

**Nota:** en el estado actual del repositorio no se observan suites de pruebas en `src/test`.

**Recomendación práctica:** incorporar pruebas unitarias en servicios de negocio (alertas, bitácora, validaciones) y pruebas de integración mínimas para endpoints críticos (usuarios/me, equipos, programaciones).

---

## 8. Endpoints principales (visión resumida)

> Ruta base: `/MantenimientosBackend/api`

- Autenticación / salud
  - `GET /auth/health`

- Usuarios
  - `GET /usuarios`
  - `GET /usuarios/me`
  - `POST /usuarios/auto-sync`
  - `PUT /usuarios/{id}/estado`

- Equipos
  - `GET /equipos`
  - `GET /equipos/{id}`
  - `POST /equipos`
  - `PUT /equipos/{id}`

- Programaciones
  - `GET /programaciones`
  - `GET /programaciones/estadisticas`
  - `GET /programaciones/{id}`

- Alertas
  - `GET /alertas-mantenimiento/dashboard`
  - `GET /alertas-mantenimiento/proximas?dias=7`
  - `GET /alertas-mantenimiento/vencidas`
  - `POST /alertas-mantenimiento/revisar-alertas`

- Tickets
  - `GET /tickets`
  - `GET /tickets/{id}`
  - `POST /tickets`

- Contratos
  - `GET /contratos`
  - `GET /contratos/stats`
  - `POST /contratos`

- Archivos (contratos)
  - `POST /archivos/upload/{idContrato}`
  - `GET /archivos/download/{fileName}`
  - `GET /archivos/contrato/{idContrato}/list`

- Imágenes
  - `POST /imagenes/upload`
  - `GET /imagenes/view/{fileName}`

- Reportes
  - `GET /reportes/equipos/pdf`
  - `GET /reportes/equipos/excel`
  - `GET /reportes/mantenimientos/pdf`

---

## 9. Despliegue y configuración

### 9.1 Entornos
- Desarrollo local con Angular (puerto típico 4200) y backend en TomEE (puerto típico 8081).
- Keycloak puede correr local o en infraestructura (IP institucional), según configuración.

### 9.2 Docker (backend)
- `docker-compose.yml` construye contenedor TomEE.
- Se exponen puertos mediante variable `TOMEE_PORT`.
- Se montan volúmenes para persistencia de archivos.

**Persistencia de archivos:** se utilizan volúmenes para conservar imágenes y documentos aunque el contenedor se reinicie.

### 9.3 Configuración de Keycloak
- Realm: `MantenimientosINACIF`.
- Clientes: `inacif-frontend` y `inacif-backend`.
- Roles y usuarios de prueba predefinidos en archivo de configuración.

---

## 10. Consideraciones no funcionales y de seguridad

- Uso recomendado de HTTPS/TLS 1.2+ en entornos productivos.
- Validación de inputs en frontend y backend.
- Tokens JWT con expiración (y renovación en frontend).
- Principio de mínimo privilegio: UI y rutas protegidas por roles.
- Auditoría: registro de usuario en operaciones clave (cuando usuario está sincronizado).

---

## 11. Conclusión

El sistema implementa una base sólida para la gestión integral de mantenimientos en INACIF, con autenticación centralizada (Keycloak), API REST, trazabilidad de equipos y ejecución de mantenimientos, programación automática y paneles de alertas. El diseño modular facilita evolución futura (notificaciones, reportes PDF “reales”, firma digital, etc.) manteniendo la integridad operativa y la auditoría requerida en el contexto institucional.

**Siguiente paso sugerido (si se requiere informe final):** anexar capturas del sistema, diccionario de datos y un apartado de pruebas/validación con evidencias de ejecución (logs, resultados de endpoints, casos de prueba).
