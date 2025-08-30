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

# Contexto Técnico - Sistema de Mantenimientos INACIF

## 🎯 Objetivo
Construir un sistema de gestión de mantenimientos preventivos, correctivos y calibraciones de equipos en el **INACIF**, con trazabilidad, alertas automáticas y reportes, bajo un stack moderno.

---

## ⚙️ Arquitectura
- **Backend:** Java Jakarta EE (TomEE, EclipseLink, Deltaspike, Maven).
- **Frontend:** Angular (SPA) con integración a Keycloak.
- **Autenticación y Autorización:** Keycloak (OAuth2, OpenID Connect, JWT).
- **Base de datos:** SQL Server / PostgreSQL.
- **Infraestructura:** Docker para ambientes dev/qa/prod.
- **IDE:** Visual Studio Code / Visual Studio.
- **CI/CD:** Pipeline en Cloud Sonet para despliegue automatizado.

---

## 📂 Módulos Principales
1. **Usuarios y Roles**
   - Gestión de usuarios desde Keycloak (`keycloak_id` en BD).
   - Roles: Administrador, Técnico, Proveedor, Usuario Consulta.

2. **Gestión de Equipos**
   - Catálogo de equipos con atributos técnicos.
   - Historial de equipos y condiciones operativas.

3. **Mantenimientos**
   - Programaciones automáticas con alertas.
   - Ejecuciones de mantenimientos (preventivo/correctivo/calibración).
   - Estados de mantenimiento (Planificado, En Proceso, Completado, Cancelado).

4. **Tickets de Falla**
   - Flujo: Abierto → Asignado → En Proceso → Resuelto → Cerrado.
   - Comentarios con tipos (técnico, seguimiento, alerta, resolución).
   - Evidencias asociadas (archivos, fotos, documentos).

5. **Contratos y Proveedores**
   - Registro de contratos, fechas, vigencia, costos.
   - Asociación de contratos con proveedores y equipos.

6. **Alertas y Notificaciones**
   - Vencimiento de contratos.
   - Mantenimientos atrasados o próximos a vencer.
   - Configuración de alertas personalizables.

7. **Reportes**
   - Exportación en PDF/XLS.
   - Dashboard con KPIs (equipos, contratos, tickets, mantenimientos).
   - Vistas especializadas (`VW_AlertasMantenimiento`, `vw_DashboardMantenimientos`).

---

## 🗄️ Base de Datos
Basada en `Sistema-Completo-INACIF.sql`.  
Tablas principales:
- **Usuarios** (integración con Keycloak).
- **Áreas**.
- **Equipos** + **Historial_Equipo**.
- **Tipos_Mantenimiento**.
- **Proveedores**.
- **Contratos** + relaciones con Equipos y Tipos de Mantenimiento.
- **Ejecuciones_Mantenimiento**.
- **Tickets**, **Comentarios_Ticket**, **Evidencias**.
- **Estados_Mantenimiento**.
- **Notificaciones**, **Configuracion_Alertas**, **Programaciones_Mantenimiento**.
Incluye **índices, vistas, procedimientos y funciones** ya definidos.

---

## 🔒 Seguridad
- Integración con Keycloak:
  - Autenticación con OAuth2.0 (Authorization Code Flow).
  - Autorización por roles con JWT.
- Rutas protegidas en Angular con **guards**.
- Conexiones cifradas (**TLS 1.2+**).
- Acceso mediante **VPN institucional**.
- Backups automáticos de BD y evidencias.

---

## 📦 Despliegue
- Backend desplegado en **Apache TomEE**.
- Frontend Angular servido desde contenedor Docker o Nginx.
- Keycloak configurado como IdP.
- Base de datos SQL en servidor institucional.
- Pipeline en **Cloud Sonet**:
  - Build → Test → Deploy (dev/qa/prod).
  - Ejecución automática de migraciones de BD.
  - Generación de reportes en cada build (tests, cobertura, seguridad).

---

## ✅ Instrucciones para Copilot Agent
Cuando reciba instrucciones, debe:
1. Respetar este **modelo de datos** ya definido.
2. Generar **endpoints REST** que correspondan a los módulos (equipos, mantenimientos, tickets, contratos, reportes).
3. Asegurar que el **frontend en Angular** se conecte a esos endpoints y valide roles con Keycloak.
4. Configurar contenedores **Docker** para backend, frontend, base de datos y Keycloak.
5. Preparar scripts de **CI/CD en Cloud Sonet** para despliegue automático.
6. Generar documentación técnica en **Markdown/PDF** con ejemplos de uso de APIs.
7. Mantener pruebas unitarias e integración en cada módulo.
