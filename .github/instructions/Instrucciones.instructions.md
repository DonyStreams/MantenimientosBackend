---
applyTo: '**'
---
Requerimientos Técnicos del Sistema (Con Keycloak)
🔐 1. Seguridad y Gestión de Accesos (Keycloak)
Objetivo: Centralizar la autenticación y autorización mediante el uso de Keycloak como Identity Provider (IdP).

a. Autenticación
Integración del frontend Angular con Keycloak mediante el adaptador oficial keycloak-js.

Protección de rutas por roles (guards) en Angular.

Inicio de sesión y cierre de sesión federado con redirección al portal principal de INACIF.

Sesiones basadas en tokens JWT (con renovación automática).

b. Autorización
Asignación de roles y permisos desde Keycloak (por ejemplo: administrador, jefatura-laboratorio, almacen, proveedor, lectura).

Validación de roles en backend Java mediante adaptador keycloak-spring-boot o keycloak-servlet-filter.

Uso de Authorization Services para reglas específicas si se requieren recursos protegidos dinámicamente.

c. Configuración recomendada en Keycloak
Realm: inacif

Clients:

inacif-frontend (confidencial, acceso desde Angular)

inacif-backend (bearer-only, sin login interactivo)

Protocol Mapper: incluir preferred_username, email, realm_access.roles en el token JWT.

d. Integraciones adicionales
Importación de usuarios desde directorios existentes (LDAP si aplica).

Posibilidad de Single Sign-On (SSO) con otros portales internos.

🧱 2. Arquitectura
Backend Java 11 bajo Jakarta EE 8 + MicroProfile 2.0.1.

Frontend Angular modular.

Arquitectura modular monolítica, con posibilidad de migrar a microservicios.

📦 3. Módulos funcionales
Mantenimiento de equipos (ya desarrollado):

Registro, detalle, historial.

Usuarios y permisos (actual):

Conectado a Keycloak (no gestionados directamente en BD).

Módulo de fallas y tickets:

Creación de reportes por laboratorios.

Asignación automática según tipo de equipo o laboratorio.

Estatus: creado, en revisión, diagnóstico, compra, finalizado.

Alerta de mantenimiento:

Programación por cron (quartz, o desde backend) que revise si hay mantenimientos próximos.

Inventario de equipos:

Consulta por laboratorio, tipo, estado operativo.

Descarga de reportes en PDF/XLS.

Firma digital de mantenimiento (futuro):

Firma del proveedor y responsable.

PDF firmado electrónicamente (PKCS#7 o similar).

🧪 4. Pruebas
Unitarias: JUnit 4

Integración: Arquillian, REST-assured

Base de datos: Flyway para migraciones y Testcontainers para pruebas en entorno aislado.

💾 5. Persistencia
JPA con EclipseLink 2.7.7

Apache Deltaspike para transacciones

Conexión a base de datos PostgreSQL o MySQL (según disponibilidad en Centro de Datos)

📋 6. Requisitos No Funcionales
Tiempo de respuesta esperado < 1s por operación CRUD.

Soporte offline no requerido, todo sobre red local interna.

Navegadores compatibles: Firefox 60+, Chrome 78+

Seguridad:

Validación de input en frontend y backend.

Uso de HTTPS obligatorio (TLS 1.2 mínimo).

Tokens expirables y protegidos.

