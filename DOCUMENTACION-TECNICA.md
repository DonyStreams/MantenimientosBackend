# 📚 Documentación Técnica - Sistema de Gestión de Mantenimientos INACIF

**Versión:** 2.0.0  
**Fecha:** Febrero 2026  
**Clasificación:** Documento Técnico Interno

---

## 📋 Tabla de Contenidos

1. [Visión General](#visión-general)
2. [Arquitectura del Sistema](#arquitectura-del-sistema)
3. [Stack Tecnológico](#stack-tecnológico)
4. [Estructura de Proyecto](#estructura-de-proyecto)
5. [Base de Datos](#base-de-datos)
6. [API REST](#api-rest)
7. [Autenticación y Autorización](#autenticación-y-autorización)
8. [Integración Keycloak](#integración-keycloak)
9. [Componentes y Servicios](#componentes-y-servicios)
10. [Patrones de Diseño](#patrones-de-diseño)
11. [Seguridad](#seguridad)
12. [Deploy y DevOps](#deploy-y-devops)
13. [Monitoring y Logging](#monitoring-y-logging)
14. [Guía de Desarrollo](#guía-de-desarrollo)

---

## 1. Visión General

El **Sistema de Gestión de Mantenimientos INACIF** es una aplicación empresarial que centraliza la administración de:

- **Equipos** - Catálogo e inventario de activos
- **Mantenimientos** - Preventivo, correctivo, calibración
- **Tickets** - Reportes de falla y seguimiento
- **Contratos** - Vigencia y proveedores
- **Notificaciones** - Alertas automáticas y por email

**Objetivos técnicos:**
- Arquitectura modular y escalable
- Seguridad centralizada con Keycloak
- Trazabilidad completa de operaciones
- API RESTful con autenticación JWT
- Frontend reactivo con Angular/PrimeNG

---

## 2. Arquitectura del Sistema

### 2.1 Diagrama de Capas

```
┌──────────────────────────────────────────────────┐
│         PRESENTACIÓN (Angular + PrimeNG)        │
│    - UI Reactiva (Components)                    │
│    - Guards y Interceptores                      │
│    - State Management                            │
└────────────────────┬─────────────────────────────┘
                     │ HTTP/REST (JWT)
┌────────────────────▼─────────────────────────────┐
│    CAPA REST (JAX-RS Controllers)               │
│    - @Path, @GET, @POST, @PUT, @DELETE         │
│    - Validación de entrada                       │
│    - Serialización JSON                          │
│    - Autorización por Roles (@RolesAllowed)    │
└────────────────────┬─────────────────────────────┘
                     │ CDI Injection
┌────────────────────▼─────────────────────────────┐
│    CAPA DE NEGOCIO (Services + CDI)             │
│    - Lógica de reglas                            │
│    - Transacciones (@Transactional)             │
│    - Cálculos y decisiones                       │
│    - Integración con terceros                    │
└────────────────────┬─────────────────────────────┘
                     │ Entity Manager
┌────────────────────▼─────────────────────────────┐
│    CAPA DE DATOS (JPA Repositories)             │
│    - EclipseLink ORM                             │
│    - Deltaspike Data Framework                   │
│    - Query Methods                               │
└────────────────────┬─────────────────────────────┘
                     │ JDBC
┌────────────────────▼─────────────────────────────┐
│         BASE DE DATOS (SQL Server/PostgreSQL)    │
│    - Tablas normalizadas                         │
│    - Índices y constraints                       │
│    - Vistas especializadas                       │
│    - Procedimientos almacenados                  │
└──────────────────────────────────────────────────┘
```

### 2.2 Flujo de una Petición HTTP

```
1. Cliente Angular
   ↓
2. HttpClient + Keycloak Interceptor
   - Adjunta token JWT en header Authorization
   ↓
3. JAX-RS Controller
   - Recibe petición HTTP
   - Extrae datos y parámetros
   - Valida formato con Bean Validation
   ↓
4. Authorization Filter
   - Verifica token JWT con Keycloak JWKS
   - Extrae roles del token
   - Verifica @RolesAllowed en endpoint
   ↓
5. Service (Negocio)
   - Ejecuta lógica de reglas
   - Interactúa con BD vía repositorios
   - Maneja transacciones
   ↓
6. Repository (Datos)
   - Construye queries JPA
   - Persiste/recupera entidades
   ↓
7. Base de Datos
   - Ejecuta SQL
   - Retorna datos
   ↓
8. Service
   - Transforma resultado
   - Retorna al controller
   ↓
9. Controller
   - Serializa a JSON
   - Retorna HTTP 200/201/400/401/403
   ↓
10. Cliente Angular
    - Recibe respuesta
    - Actualiza UI
```

---

## 3. Stack Tecnológico

### 3.1 Backend

| Componente | Versión | Función |
|-----------|---------|---------|
| **Java** | 11 LTS | Lenguaje base |
| **Jakarta EE** | 8 | Especificación enterprise |
| **Apache TomEE** | 9.0.0-M7 | Servidor aplicaciones |
| **JAX-RS** | 2.1 | Endpoints REST |
| **JPA** | 2.2 | Persistencia ORM |
| **EclipseLink** | 2.7.7 | Implementación JPA |
| **CDI** | 2.0 | Inyección dependencias |
| **Deltaspike** | 1.9.5 | Extensiones CDI |
| **Bean Validation** | 2.0 | Validación datos |
| **Maven** | 3.8+ | Build & Dependency |

### 3.2 Frontend

| Componente | Versión | Función |
|-----------|---------|---------|
| **Angular** | 17+ | Framework SPA |
| **TypeScript** | 5.x | Lenguaje tipado |
| **RxJS** | 7.x | Programación reactiva |
| **PrimeNG** | 17.x | Componentes UI |
| **Keycloak JS** | 18+ | Cliente OAuth2 |
| **SCSS** | 3.x | Estilos moderno |
| **npm** | 9+ | Gestor paquetes |

### 3.3 Infraestructura

| Componente | Versión | Función |
|-----------|---------|---------|
| **Docker** | 20.10+ | Contenedores |
| **Docker Compose** | 2.x | Orquestación local |
| **Keycloak** | 23+ | Identity Provider |
| **SQL Server / PostgreSQL** | 2019+ / 13+ | Base de datos |
| **Nginx** | Latest | Reverse proxy (prod) |

---

## 4. Estructura de Proyecto

### 4.1 Backend

```
MantenimientosBackend/
├── src/main/java/usac/eps/
│   ├── controladores/          # JAX-RS Controllers
│   │   ├── EquiposController.java
│   │   ├── MantenimientosController.java
│   │   ├── TicketsController.java
│   │   ├── ContratosController.java
│   │   └── NotificacionesController.java
│   │
│   ├── servicios/              # Business Logic Services
│   │   ├── EquiposService.java
│   │   ├── MantenimientosService.java
│   │   ├── TicketsService.java
│   │   ├── EmailService.java
│   │   ├── NotificacionesService.java
│   │   └── SchedulerService.java
│   │
│   ├── repositorios/           # JPA Data Access
│   │   ├── EquiposRepository.java
│   │   ├── MantenimientosRepository.java
│   │   ├── TicketsRepository.java
│   │   └── ...Repository.java
│   │
│   ├── entidades/              # JPA Entities
│   │   ├── Equipo.java
│   │   ├── Mantenimiento.java
│   │   ├── Ticket.java
│   │   ├── Contrato.java
│   │   ├── Usuario.java
│   │   ├── Notificacion.java
│   │   └── ...
│   │
│   ├── dto/                    # Data Transfer Objects
│   │   ├── EquipoDTO.java
│   │   ├── MantenimientoDTO.java
│   │   └── ...
│   │
│   ├── excepciones/            # Custom Exceptions
│   │   ├── ResourceNotFoundException.java
│   │   ├── UnauthorizedException.java
│   │   └── ...
│   │
│   ├── seguridad/              # Security & Auth
│   │   ├── KeycloakSecurityContext.java
│   │   ├── JWTFilter.java
│   │   └── RoleValidator.java
│   │
│   ├── util/                   # Utilidades
│   │   ├── DateUtil.java
│   │   ├── FileUtil.java
│   │   ├── ExcelExporter.java
│   │   ├── PDFExporter.java
│   │   └── ...
│   │
│   ├── scheduler/              # Tareas Programadas
│   │   ├── MantenimientosScheduler.java
│   │   ├── AlertasScheduler.java
│   │   └── NotificacionesScheduler.java
│   │
│   └── config/                 # Configuración
│       ├── ApplicationConfig.java
│       ├── CORSFilter.java
│       └── JacksonConfiguration.java
│
├── src/main/resources/
│   ├── META-INF/
│   │   ├── persistence.xml     # JPA Configuration
│   │   ├── beans.xml           # CDI beans
│   │   └── resources.xml       # Data sources
│   ├── email.properties        # SMTP Configuration
│   ├── scheduler.properties    # Cron jobs
│   └── application.properties
│
├── src/test/java/              # Pruebas unitarias e integración
│
├── pom.xml                     # Maven POM
├── Dockerfile                  # Imagen Docker
├── docker-compose.yml          # Composición local
└── README.md
```

### 4.2 Frontend

```
EPS-FRONTEND/
├── src/
│   ├── app/
│   │   ├── components/
│   │   │   ├── pages/
│   │   │   │   ├── equipos/
│   │   │   │   ├── mantenimientos/
│   │   │   │   ├── tickets/
│   │   │   │   ├── contratos/
│   │   │   │   └── dashboard/
│   │   │   │
│   │   │   ├── layout/         # Layout principal
│   │   │   ├── auth/           # Auth components
│   │   │   └── shared/         # Componentes reutilizables
│   │   │
│   │   ├── service/            # Angular Services
│   │   │   ├── equipos.service.ts
│   │   │   ├── mantenimientos.service.ts
│   │   │   ├── tickets.service.ts
│   │   │   ├── keycloak.service.ts
│   │   │   └── notificaciones.service.ts
│   │   │
│   │   ├── guards/             # Route Guards
│   │   │   ├── auth.guard.ts
│   │   │   └── permisos.guard.ts
│   │   │
│   │   ├── interceptors/       # HTTP Interceptors
│   │   │   ├── keycloak.interceptor.ts
│   │   │   └── error.interceptor.ts
│   │   │
│   │   ├── directives/         # Custom Directives
│   │   │   ├── has-role.directive.ts
│   │   │   └── tiene-permiso.directive.ts
│   │   │
│   │   ├── api/                # API Models
│   │   │   └── equipos.ts
│   │   │
│   │   ├── app-routing.module.ts
│   │   ├── app.module.ts
│   │   └── app.component.ts
│   │
│   ├── assets/
│   │   ├── images/
│   │   ├── plantillas/
│   │   ├── data/
│   │   └── silent-check-sso.html
│   │
│   ├── environments/           # Configuración por ambiente
│   │   ├── environment.ts      # Desarrollo
│   │   └── environment.prod.ts # Producción
│   │
│   ├── styles.scss
│   ├── main.ts
│   └── index.html
│
├── angular.json
├── tsconfig.json
├── package.json
└── README.md
```

---

## 5. Base de Datos

### 5.1 Diagrama Entidad-Relación (Simplificado)

```
┌─────────────────┐         ┌──────────────────┐
│   USUARIOS      │         │    KEYCLOAK      │
├─────────────────┤         │  (OAuth2/OIDC)  │
│ id (PK)         │◄────────│  keycloak_id    │
│ keycloak_id (FK)│         │  (sincronizado) │
│ nombre_completo │         └──────────────────┘
│ correo          │
│ estado (activo) │
│ fecha_creacion  │
└────────┬────────┘
         │
         │ 1:N (crea/asigna)
         ▼
┌─────────────────┐         ┌──────────────────┐
│   EQUIPOS       │◄────────│    ÁREAS         │
├─────────────────┤         ├──────────────────┤
│ id_equipo (PK)  │─┐       │ id_area (PK)     │
│ area_id (FK)    │ │       │ nombre_area      │
│ nombre_equipo   │ │       │ ubicacion        │
│ marca           │ │       └──────────────────┘
│ modelo          │ │
│ serie           │ │       ┌──────────────────┐
│ estado          │ │       │ CATEGORÍAS       │
│ ubicacion       │ └──────►├──────────────────┤
│ fecha_creacion  │         │ id_categoria (PK)│
└────────┬────────┘         │ nombre           │
         │                  └──────────────────┘
         │ 1:N
         │
    ┌────┴──────────────────────────────┐
    │                                   │
    ▼                                   ▼
┌─────────────────────┐      ┌─────────────────────┐
│ MANTENIMIENTOS      │      │ TICKETS             │
├─────────────────────┤      ├─────────────────────┤
│ id_mantenimiento (PK)       │ id_ticket (PK)      │
│ equipo_id (FK)      │      │ equipo_id (FK)      │
│ tipo (P/C/Cal)      │      │ usuario_creador (FK)│
│ fecha_programada    │      │ usuario_asignado(FK)│
│ estado              │      │ descripcion         │
│ observaciones       │      │ prioridad           │
└──────────┬──────────┘      │ estado              │
           │                 │ fecha_creacion      │
    ┌──────┴──────────┐     └────────────┬────────┘
    │                 │                   │
    ▼                 ▼                   ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────────┐
│ EJECUCIONES  │  │ CONTRATOS    │  │ COMENTARIOS_TKT  │
├──────────────┤  ├──────────────┤  ├──────────────────┤
│ id_ejecucion │  │ id_contrato  │  │ id_comentario    │
│ mantnto_id FK    │ proveedor_id │  │ ticket_id (FK)   │
│ fecha_inicio │  │ fecha_inicio │  │ usuario (FK)     │
│ fecha_fin    │  │ fecha_fin    │  │ tipo_comentario  │
│ estado       │  │ estado       │  │ texto            │
└──────────────┘  │ valor        │  │ fecha_creacion   │
                  └──────────────┘  └──────────────────┘
                        │
                        ▼
                   ┌──────────────┐
                   │ PROVEEDORES  │
                   ├──────────────┤
                   │ id_proveedor │
                   │ nombre       │
                   │ contacto     │
                   └──────────────┘
```

### 5.2 Tablas Principales

**USUARIOS**
```sql
CREATE TABLE Usuarios (
    id INT PRIMARY KEY AUTO_INCREMENT,
    keycloak_id VARCHAR(255) UNIQUE NOT NULL,
    nombre_completo VARCHAR(255) NOT NULL,
    correo VARCHAR(255) UNIQUE,
    area_id INT,
    estado INT DEFAULT 1, -- 1=Activo, 0=Inactivo
    fecha_creacion DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (area_id) REFERENCES Areas(id_area)
);
```

**EQUIPOS**
```sql
CREATE TABLE Equipos (
    id_equipo INT PRIMARY KEY AUTO_INCREMENT,
    nombre_equipo VARCHAR(255) NOT NULL,
    marca VARCHAR(100),
    modelo VARCHAR(100),
    serie VARCHAR(100) UNIQUE,
    codigo_inacif VARCHAR(50) UNIQUE,
    area_id INT NOT NULL,
    categoria_id INT,
    estado VARCHAR(50), -- Operativo, Reparación, Baja, Calibración
    ubicacion VARCHAR(255),
    fecha_creacion DATETIME DEFAULT GETDATE(),
    usuario_creacion INT,
    FOREIGN KEY (area_id) REFERENCES Areas(id_area),
    FOREIGN KEY (categoria_id) REFERENCES Categorias_Equipo(id_categoria),
    FOREIGN KEY (usuario_creacion) REFERENCES Usuarios(id)
);

CREATE INDEX IX_Equipos_Area ON Equipos(area_id);
CREATE INDEX IX_Equipos_Estado ON Equipos(estado);
```

**MANTENIMIENTOS**
```sql
CREATE TABLE Mantenimientos (
    id_mantenimiento INT PRIMARY KEY AUTO_INCREMENT,
    equipo_id INT NOT NULL,
    tipo_mantenimiento VARCHAR(50), -- Preventivo, Correctivo, Calibración
    frecuencia_dias INT,
    estado VARCHAR(50), -- Programado, En Proceso, Completado, Cancelado
    fecha_programada DATETIME,
    observaciones TEXT,
    contrato_id INT,
    usuario_responsable INT,
    fecha_creacion DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (equipo_id) REFERENCES Equipos(id_equipo),
    FOREIGN KEY (usuario_responsable) REFERENCES Usuarios(id),
    FOREIGN KEY (contrato_id) REFERENCES Contratos(id_contrato)
);

CREATE INDEX IX_Mantenimientos_Equipo ON Mantenimientos(equipo_id);
CREATE INDEX IX_Mantenimientos_Estado ON Mantenimientos(estado);
CREATE INDEX IX_Mantenimientos_Fecha ON Mantenimientos(fecha_programada);
```

**TICKETS**
```sql
CREATE TABLE Tickets (
    id_ticket INT PRIMARY KEY AUTO_INCREMENT,
    equipo_id INT NOT NULL,
    usuario_creador INT NOT NULL,
    usuario_asignado INT,
    descripcion TEXT NOT NULL,
    prioridad VARCHAR(20), -- Baja, Media, Alta, Crítica
    estado VARCHAR(50), -- Abierto, Asignado, En Proceso, Resuelto, Cerrado
    fecha_creacion DATETIME DEFAULT GETDATE(),
    fecha_cierre DATETIME,
    FOREIGN KEY (equipo_id) REFERENCES Equipos(id_equipo),
    FOREIGN KEY (usuario_creador) REFERENCES Usuarios(id),
    FOREIGN KEY (usuario_asignado) REFERENCES Usuarios(id)
);

CREATE INDEX IX_Tickets_Estado ON Tickets(estado);
CREATE INDEX IX_Tickets_Prioridad ON Tickets(prioridad);
CREATE INDEX IX_Tickets_Asignado ON Tickets(usuario_asignado);
```

**NOTIFICACIONES**
```sql
CREATE TABLE Notificaciones (
    id_notificacion INT PRIMARY KEY AUTO_INCREMENT,
    usuario_id INT NOT NULL,
    tipo VARCHAR(50), -- mantenimiento_proximo, ticket_critico, contrato_vencido
    titulo VARCHAR(255),
    descripcion TEXT,
    prioridad VARCHAR(20),
    leida INT DEFAULT 0,
    fecha_creacion DATETIME DEFAULT GETDATE(),
    fecha_lectura DATETIME,
    FOREIGN KEY (usuario_id) REFERENCES Usuarios(id)
);

CREATE INDEX IX_Notificaciones_Usuario ON Notificaciones(usuario_id);
CREATE INDEX IX_Notificaciones_Leida ON Notificaciones(leida);
```

### 5.3 Vistas Especializadas

**VW_AlertasMantenimiento** - Mantenimientos próximos a vencer
```sql
CREATE VIEW VW_AlertasMantenimiento AS
SELECT 
    e.id_equipo,
    e.nombre_equipo,
    m.fecha_programada,
    DATEDIFF(day, GETDATE(), m.fecha_programada) as dias_restantes,
    tm.nombre_tipo as tipo_mantenimiento,
    u.nombre_completo as responsable,
    CASE 
        WHEN DATEDIFF(day, GETDATE(), m.fecha_programada) <= 7 THEN 'Crítica'
        WHEN DATEDIFF(day, GETDATE(), m.fecha_programada) <= 15 THEN 'Alta'
        WHEN DATEDIFF(day, GETDATE(), m.fecha_programada) <= 30 THEN 'Media'
        ELSE 'Baja'
    END as prioridad_alerta
FROM Mantenimientos m
INNER JOIN Equipos e ON m.equipo_id = e.id_equipo
INNER JOIN Tipos_Mantenimiento tm ON m.tipo_mantenimiento_id = tm.id_tipo
INNER JOIN Usuarios u ON m.usuario_responsable = u.id
WHERE m.estado IN ('Programado', 'Pendiente')
AND DATEDIFF(day, GETDATE(), m.fecha_programada) BETWEEN 0 AND 30;
```

---

## 6. API REST

### 6.1 Convenciones

**Base URL:** `http://localhost:8080/MantenimientosBackend/api`

**Autenticación:** Todas las peticiones requieren header:
```
Authorization: Bearer <jwt_token>
```

**Formato respuesta:**
```json
{
  "success": true,
  "data": { /* payload */ },
  "message": "Operación exitosa",
  "timestamp": "2026-02-03T15:30:00Z"
}
```

**Códigos HTTP:**
- `200` - OK
- `201` - Created
- `204` - No Content
- `400` - Bad Request
- `401` - Unauthorized
- `403` - Forbidden
- `404` - Not Found
- `500` - Internal Server Error

### 6.2 Endpoints por Módulo

#### **EQUIPOS**

```
GET    /api/equipos              [ADMIN, SUPERVISOR, TECNICO, USER]
POST   /api/equipos              [ADMIN, SUPERVISOR, TECNICO_EQUIPOS]
GET    /api/equipos/{id}         [ADMIN, SUPERVISOR, TECNICO, USER]
PUT    /api/equipos/{id}         [ADMIN, SUPERVISOR, TECNICO_EQUIPOS]
DELETE /api/equipos/{id}         [ADMIN]
GET    /api/equipos/area/{areaId}  [ADMIN, SUPERVISOR, TECNICO, USER]
GET    /api/equipos/search?q=...   [ADMIN, SUPERVISOR, TECNICO, USER]
```

#### **MANTENIMIENTOS**

```
GET    /api/mantenimientos                    [ADMIN, SUPERVISOR, TECNICO, USER]
POST   /api/mantenimientos                    [ADMIN, SUPERVISOR]
PUT    /api/mantenimientos/{id}               [ADMIN, SUPERVISOR]
DELETE /api/mantenimientos/{id}               [ADMIN]
GET    /api/mantenimientos/programaciones    [ADMIN, SUPERVISOR, TECNICO]
POST   /api/mantenimientos/{id}/ejecutar     [ADMIN, SUPERVISOR, TECNICO]
GET    /api/mantenimientos/{id}/historial    [ADMIN, SUPERVISOR, TECNICO, USER]
GET    /api/mantenimientos/alertas/proximas  [ADMIN, SUPERVISOR]
```

#### **TICKETS**

```
GET    /api/tickets                            [ADMIN, SUPERVISOR, TECNICO, USER]
POST   /api/tickets                            [ADMIN, SUPERVISOR, TECNICO, USER]
GET    /api/tickets/{id}                       [ADMIN, SUPERVISOR, TECNICO, USER]
PUT    /api/tickets/{id}                       [ADMIN, SUPERVISOR, TECNICO]
DELETE /api/tickets/{id}                       [ADMIN]
PUT    /api/tickets/{id}/asignar              [ADMIN, SUPERVISOR]
PUT    /api/tickets/{id}/estado               [ADMIN, SUPERVISOR, TECNICO]
POST   /api/tickets/{id}/comentarios          [ADMIN, SUPERVISOR, TECNICO, USER]
GET    /api/tickets/{id}/comentarios          [ADMIN, SUPERVISOR, TECNICO, USER]
POST   /api/tickets/{id}/evidencias           [ADMIN, SUPERVISOR, TECNICO]
GET    /api/tickets/{id}/evidencias           [ADMIN, SUPERVISOR, TECNICO, USER]
```

#### **CONTRATOS**

```
GET    /api/contratos                         [ADMIN, SUPERVISOR, TECNICO, USER]
POST   /api/contratos                         [ADMIN, SUPERVISOR]
GET    /api/contratos/{id}                    [ADMIN, SUPERVISOR, TECNICO, USER]
PUT    /api/contratos/{id}                    [ADMIN, SUPERVISOR]
DELETE /api/contratos/{id}                    [ADMIN]
GET    /api/contratos/alertas/vencimiento    [ADMIN, SUPERVISOR]
```

#### **NOTIFICACIONES**

```
GET    /api/notificaciones                    [ADMIN, SUPERVISOR, TECNICO, USER]
GET    /api/notificaciones/conteo             [ADMIN, SUPERVISOR, TECNICO, USER]
PUT    /api/notificaciones/{id}/leer          [ADMIN, SUPERVISOR, TECNICO, USER]
DELETE /api/notificaciones/{id}               [ADMIN, SUPERVISOR, TECNICO, USER]
PUT    /api/notificaciones/marcar-todas-leidas [ADMIN, SUPERVISOR, TECNICO, USER]
```

### 6.3 Ejemplo de Petición Completa

**POST - Crear Ticket**

```bash
curl -X POST http://localhost:8080/MantenimientosBackend/api/tickets \
  -H "Authorization: Bearer eyJhbGciOiJSUzI1NiIs..." \
  -H "Content-Type: application/json" \
  -d '{
    "equipoId": 5,
    "descripcion": "El microscopio no enciende",
    "prioridad": "Alta",
    "usuarioAsignadoId": 3
  }'
```

**Response - 201 Created**

```json
{
  "success": true,
  "data": {
    "id": 42,
    "equipoId": 5,
    "descripcion": "El microscopio no enciende",
    "prioridad": "Alta",
    "estado": "Abierto",
    "usuarioCreadorId": 1,
    "usuarioAsignadoId": 3,
    "fechaCreacion": "2026-02-03T15:35:00Z"
  },
  "message": "Ticket creado exitosamente",
  "timestamp": "2026-02-03T15:35:01Z"
}
```

---

## 7. Autenticación y Autorización

### 7.1 Flujo OAuth2 (Authorization Code)

```
┌──────────────┐                              ┌────────────────┐
│   Angular    │                              │   Keycloak     │
│   Frontend   │                              │   IdP           │
└──────┬───────┘                              └────────┬────────┘
       │                                              │
       │ 1. Usuario accede a /dashboard             │
       │─────────────────────────────────────────────►│
       │                                              │
       │ 2. No autenticado → Redirige a login      │
       │◄─────────────────────────────────────────────│
       │                                              │
       │ 3. Muestra formulario de login               │
       │                                              │
       │ 4. Usuario ingresa credenciales              │
       │────────────────────────────────────────────►│
       │                                              │
       │ 5. Valida credenciales (BD usuarios)        │
       │                                              │
       │ 6. Genera JWT con roles y scopes             │
       │◄─────────────────────────────────────────────│
       │                                              │
       │ 7. Almacena token en localStorage           │
       │                                              │
       │ 8. Redirige a /dashboard                    │
       │                                              │
       │ 9. GET /api/equipos                         │
       │    Authorization: Bearer <token>            │
       │────────────────────────────────────────────►│
       │                                              │
       │ 10. Backend valida token contra Keycloak JWKS
       │                                              │
       │ 11. Extrae roles del JWT                    │
       │                                              │
       │ 12. Verifica @RolesAllowed({"ADMIN"})      │
       │                                              │
       │ 13. Retorna datos (200 OK)                  │
       │◄─────────────────────────────────────────────│
       │                                              │
```

### 7.2 Estructura del JWT Token

```json
Header:
{
  "alg": "RS256",
  "typ": "JWT",
  "kid": "..."
}

Payload:
{
  "jti": "...",
  "exp": 1643901600,
  "nbf": 0,
  "iat": 1643901300,
  "iss": "http://localhost:8180/auth/realms/inacif",
  "sub": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "typ": "Bearer",
  "azp": "inacif-frontend",
  "preferred_username": "admin",
  "email": "admin@inacif.gob.gt",
  "resource_access": {
    "inacif-frontend": {
      "roles": ["ADMIN", "manage-account"]
    }
  },
  "realm_access": {
    "roles": ["offline_access", "uma_authorization"]
  }
}
```

### 7.3 Validación Backend

**KeycloakSecurityContext.java**
```java
@Provider
public class KeycloakSecurityContext implements SecurityContext {
    
    @Inject
    private KeycloakPrincipal<KeycloakSecurityContext> principal;
    
    public boolean isUserInRole(String role) {
        return principal.getKeycloakSecurityContext()
                       .getToken()
                       .getRealmAccess()
                       .isUserInRole(role);
    }
    
    public Set<String> getUserRoles() {
        return principal.getKeycloakSecurityContext()
                       .getToken()
                       .getResourceAccess("inacif-frontend")
                       .getRoles();
    }
}
```

**Anotación en Controllers**
```java
@Path("/api/equipos")
public class EquiposController {
    
    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")  // Solo ADMIN puede eliminar
    public Response eliminarEquipo(@PathParam("id") int id) {
        // lógica de eliminación
    }
}
```

---

## 8. Integración Keycloak

### 8.1 Configuración Backend (persistence.xml)

```xml
<provider>
    <name>keycloak-cfg</name>
    <property-value>
        {
            "realm": "inacif",
            "bearer-only": true,
            "auth-server-url": "http://localhost:8180",
            "ssl-required": "external",
            "resource": "inacif-backend",
            "credentials": {
                "secret": "tu-secret-aqui"
            }
        }
    </property-value>
</provider>
```

### 8.2 Configuración Frontend (environment.ts)

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/MantenimientosBackend/api',
  keycloak: {
    url: 'http://localhost:8180',
    realm: 'inacif',
    clientId: 'inacif-frontend',
    redirectUri: 'http://localhost:4200'
  }
};
```

### 8.3 Inicialización Keycloak (main.ts)

```typescript
import { KeycloakService } from './app/service/keycloak.service';

const keycloakService = new KeycloakService();

keycloakService.init().then(() => {
  platformBrowserDynamic()
    .bootstrapModule(AppModule)
    .catch(err => console.error(err));
}).catch(error => {
  console.error('Falló inicialización de Keycloak', error);
});
```

### 8.4 Sincronización Usuarios (Local)

```java
@Stateless
public class UsuarioSyncService {
    
    @Inject
    private UsuariosRepository usuariosRepository;
    
    /**
     * Sincroniza o crea usuario basado en Keycloak
     */
    public Usuario sincronizarDesdeKeycloak(String keycloakId, String email, String nombreCompleto) {
        Usuario usuario = usuariosRepository.findByKeycloakId(keycloakId);
        
        if (usuario == null) {
            usuario = new Usuario();
            usuario.setKeycloakId(keycloakId);
            usuario.setEstado(1); // Activo
            usuariosRepository.save(usuario);
        }
        
        usuario.setCorreo(email);
        usuario.setNombreCompleto(nombreCompleto);
        usuariosRepository.update(usuario);
        
        return usuario;
    }
    
    /**
     * Valida que usuario esté activo en BD local
     * (Regla crítica: acceso denegado si está inactivo)
     */
    public boolean isUsuarioActivo(String keycloakId) {
        Usuario usuario = usuariosRepository.findByKeycloakId(keycloakId);
        return usuario != null && usuario.getEstado() == 1;
    }
}
```

---

## 9. Componentes y Servicios

### 9.1 Arquitectura de Servicios

```
┌─────────────────────────────────────────┐
│     Controllers (JAX-RS)                │
│  - Reciben peticiones HTTP              │
│  - Validan entrada                      │
│  - Llaman a servicios                   │
│  - Retornan respuestas                  │
└────────────┬────────────────────────────┘
             │
    ┌────────┴────────┐
    │                 │
    ▼                 ▼
┌──────────────┐  ┌──────────────────┐
│   Services   │  │ Data Services    │
│ (Negocio)    │  │ (Repositorios)   │
└──────────────┘  └──────────────────┘
    │                 │
    └────────┬────────┘
             │
    ┌────────▼────────┐
    │                 │
    ▼                 ▼
┌──────────────┐  ┌──────────────────┐
│ EmailService │  │ FileService      │
│ (Notif.)     │  │ (Evidencias)     │
└──────────────┘  └──────────────────┘
```

### 9.2 Servicios Principales

**EquiposService.java**
- `getAllEquipos()` - Listar con paginación
- `getEquipoById(id)` - Obtener detalle
- `createEquipo(dto)` - Crear con validaciones
- `updateEquipo(id, dto)` - Actualizar estado
- `deleteEquipo(id)` - Eliminar (solo ADMIN)
- `getEquiposByArea(areaId)` - Filtrar por área
- `getHistorialEquipo(id)` - Cambios históricos

**MantenimientosService.java**
- `getProgramaciones()` - Próximas a vencer
- `crearEjecucion(dto)` - Registrar mantenimiento
- `generarAlertasProximas(diasAnticipacion)` - Notificaciones automáticas
- `exportarHistorial(equipoId)` - Reportes

**TicketsService.java**
- `crearTicket(dto)` - Crear con asignación automática
- `cambiarEstado(id, nuevoEstado)` - Validar transiciones
- `asignarTicket(id, usuarioId)` - Asignar técnico
- `agregarComentario(ticketId, comentarioDto)` - Seguimiento
- `subirEvidencia(ticketId, archivo)` - Almacenar archivos

**NotificacionesService.java**
- `crearNotificacion(usuarioId, tipo, titulo)` - Crear alerta
- `enviarEmailNotificacion(usuarioId, asunto)` - SMTP
- `marcarComoLeida(notificacionId)` - Actualizar estado
- `obtenerConteo(usuarioId)` - KPIs en navbar

**EmailService.java**
- `enviarEmail(destinatario, asunto, contenido)` - SMTP
- `enviarEmailProgramado(evento)` - Tareas scheduler
- `construirTemplate(tipo, datos)` - HTML templates

### 9.3 Inyección de Dependencias (CDI)

```java
@Stateless
public class EquiposService {
    
    @Inject
    private EquiposRepository equiposRepository;
    
    @Inject
    private NotificacionesService notificacionesService;
    
    @Inject
    private Logger logger;
    
    @Transactional(Transactional.TxType.REQUIRED)
    public Equipo crearEquipo(EquipoDTO dto) {
        Equipo equipo = new Equipo();
        // ... mapear de DTO a entidad
        equiposRepository.save(equipo);
        
        logger.info("Equipo creado: " + equipo.getId());
        return equipo;
    }
}
```

---

## 10. Patrones de Diseño

### 10.1 Repository Pattern

```java
public interface BaseRepository<T, ID> {
    T findById(ID id);
    List<T> findAll();
    T save(T entity);
    T update(T entity);
    void delete(T entity);
}

@Repository
public class EquiposRepository implements BaseRepository<Equipo, Integer> {
    
    @PersistenceContext
    private EntityManager em;
    
    @Override
    public Equipo findById(Integer id) {
        return em.find(Equipo.class, id);
    }
    
    public List<Equipo> findByArea(Integer areaId) {
        return em.createQuery(
            "SELECT e FROM Equipo e WHERE e.area.id = ?1",
            Equipo.class
        ).setParameter(1, areaId).getResultList();
    }
}
```

### 10.2 DTO Pattern (Data Transfer Objects)

```java
// Entidad JPA (pesada, con relaciones)
@Entity
public class Equipo {
    @Id private Integer id;
    @ManyToOne private Area area;
    @OneToMany private List<Mantenimiento> mantenimientos;
    // ... relaciones
}

// DTO para respuesta API (ligero, sin relaciones innecesarias)
public class EquipoDTO {
    private Integer id;
    private String nombre;
    private String codigo;
    private Integer areaId;
    private String estado;
    // ... solo campos necesarios
}

// Mapeo en servicio
public EquipoDTO getEquipoDTO(Integer id) {
    Equipo equipo = equiposRepository.findById(id);
    return new EquipoDTO(
        equipo.getId(),
        equipo.getNombre(),
        equipo.getCodigo(),
        equipo.getArea().getId(),
        equipo.getEstado()
    );
}
```

### 10.3 Service Locator (CDI)

```java
@Stateless
@Transactional
public class MantenimientosService {
    
    @Inject
    private MantenimientosRepository repository;
    
    @Inject
    private EquiposRepository equiposRepository;
    
    @Inject
    private NotificacionesService notificacionesService;
    
    @Inject
    private EmailService emailService;
    
    public void ejecutarMantenimiento(MantenimientoDTO dto) {
        // Lógica que orquesta múltiples servicios
    }
}
```

### 10.4 Observer Pattern (Notificaciones)

```java
@Stateless
public class MantenimientosService {
    
    @Inject
    private NotificacionesService notificacionesService;
    
    @Transactional
    public Mantenimiento crearMantenimiento(MantenimientoDTO dto) {
        Mantenimiento m = new Mantenimiento();
        // ... crear
        
        // Observer: notificar cambio
        notificacionesService.crearNotificacion(
            usuarioResponsable.getId(),
            "mantenimiento_proximo",
            "Nuevo mantenimiento programado"
        );
        
        return m;
    }
}
```

---

## 11. Seguridad

### 11.1 Validación de Entrada

**Backend**
```java
@Path("/api/equipos")
public class EquiposController {
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crearEquipo(
        @Valid EquipoDTO equipo
    ) {
        // EquipoDTO tiene @NotBlank, @Min, @Max, etc.
        // Bean Validation falla automáticamente si inválido
    }
}

@Data
public class EquipoDTO {
    @NotBlank(message = "Nombre es obligatorio")
    private String nombre;
    
    @Min(value = 1, message = "El área es requerida")
    private Integer areaId;
    
    @Pattern(regexp = "^(Operativo|Reparacion|Baja|Calibracion)$")
    private String estado;
}
```

**Frontend**
```typescript
// Reactive Forms con validación
this.form = this.formBuilder.group({
  nombre: ['', [Validators.required, Validators.minLength(3)]],
  areaId: ['', Validators.required],
  estado: ['', Validators.required]
});

// Mostrar errores
<input [formControl]="form.get('nombre')">
<small *ngIf="form.get('nombre').invalid">
  El nombre es obligatorio
</small>
```

### 11.2 Prevención CSRF

```xml
<!-- Keycloak maneja CSRF automáticamente -->
<!-- En frontend, los tokens CSRF se incluyen automáticamente -->
```

### 11.3 Seguridad de Headers

```java
@Provider
public class SecurityHeadersFilter implements ContainerResponseFilter {
    
    @Override
    public void filter(ContainerRequestContext req, 
                      ContainerResponseContext res) {
        res.getHeaders().add("X-Content-Type-Options", "nosniff");
        res.getHeaders().add("X-Frame-Options", "DENY");
        res.getHeaders().add("X-XSS-Protection", "1; mode=block");
        res.getHeaders().add("Strict-Transport-Security", 
            "max-age=31536000; includeSubDomains");
    }
}
```

### 11.4 Sanitización de SQL

```java
// ✅ CORRECTO: JPA prepara sentencia
List<Equipo> equipos = em.createQuery(
    "SELECT e FROM Equipo e WHERE e.nombre = ?1",
    Equipo.class
).setParameter(1, nombreUsuario).getResultList();

// ❌ INCORRECTO: Concatenación (SQL Injection)
String query = "SELECT e FROM Equipo e WHERE e.nombre = '" + nombre + "'";
```

---

## 12. Deploy y DevOps

### 12.1 Dockerización Backend

**Dockerfile**
```dockerfile
FROM tomee:9.0.0-M7-plus

# Copiar WAR compilado
COPY target/MantenimientosBackend.war \
  /usr/local/tomee/webapps/

# Exponer puerto
EXPOSE 8080

# Variables de entorno
ENV CATALINA_OPTS="\
  -Dkeycloak.realm=inacif \
  -Dkeycloak.auth-server-url=http://keycloak:8080 \
  -Dkeycloak.resource=inacif-backend"

CMD ["catalina.sh", "run"]
```

### 12.2 Docker Compose (Desarrollo)

```yaml
version: '3.8'

services:
  # Backend
  tomee-server:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: tomee-server
    ports:
      - "8080:8080"
    environment:
      - CATALINA_OPTS=-Xmx1024M
    volumes:
      - ./logs:/usr/local/tomee/logs
    networks:
      - inacif-network
    depends_on:
      - db-server
      - keycloak-server
    healthcheck:
      test: ["CMD", "curl", "-f", 
             "http://localhost:8080/MantenimientosBackend/api/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  # Base de Datos
  db-server:
    image: mcr.microsoft.com/mssql/server:2019-latest
    container_name: db-server
    environment:
      - SA_PASSWORD=YourPassword123!
      - ACCEPT_EULA=Y
    ports:
      - "1433:1433"
    volumes:
      - mssql_data:/var/opt/mssql
    networks:
      - inacif-network

  # Keycloak
  keycloak-server:
    image: quay.io/keycloak/keycloak:23.0.0
    container_name: keycloak-server
    environment:
      - KEYCLOAK_ADMIN=admin
      - KEYCLOAK_ADMIN_PASSWORD=admin
    ports:
      - "8180:8080"
    networks:
      - inacif-network

volumes:
  mssql_data:

networks:
  inacif-network:
    driver: bridge
```

### 12.3 CI/CD Pipeline (Cloud Sonet)

```yaml
pipeline:
  stages:
    - build
    - test
    - deploy

build:
  stage: build
  script:
    - mvn clean package -DskipTests
  artifacts:
    paths:
      - target/MantenimientosBackend.war

test:
  stage: test
  script:
    - mvn test
    - mvn verify
  coverage: '/Coverage: (\d+\.\d+)%/'
  reports:
    junit: target/surefire-reports/**/*.xml

deploy_dev:
  stage: deploy
  script:
    - docker build -t inacif-backend:latest .
    - docker push registry.inacif.gob.gt/inacif-backend:latest
    - kubectl set image deployment/mantenimientos-backend 
        backend=registry.inacif.gob.gt/inacif-backend:latest
  environment:
    name: development
```

---

## 13. Monitoring y Logging

### 13.1 Configuración de Logs

**logging.properties**
```properties
handlers = java.util.logging.ConsoleHandler, \
           java.util.logging.FileHandler

.level = INFO

# Logs de aplicación
usac.eps.level = DEBUG

# Logs de Keycloak
org.keycloak.level = INFO

# Logs de JPA
org.eclipse.persistence.level = WARNING

# Logs de Deltaspike
org.apache.deltaspike.level = INFO

# Handler archivo
java.util.logging.FileHandler.level = DEBUG
java.util.logging.FileHandler.pattern = \
  /usr/local/tomee/logs/MantenimientosBackend.log
java.util.logging.FileHandler.limit = 50000000
java.util.logging.FileHandler.count = 5
java.util.logging.FileHandler.formatter = \
  java.util.logging.SimpleFormatter

java.util.logging.SimpleFormatter.format = \
  %1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$s %2$s %5$s%6$s%n
```

### 13.2 Health Check Endpoint

```java
@Path("/api/health")
public class HealthCheckController {
    
    @Inject
    private DataSource dataSource;
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        
        // Verificar BD
        try {
            dataSource.getConnection().close();
            health.put("database", "UP");
        } catch (Exception e) {
            health.put("database", "DOWN");
        }
        
        // Verificar Keycloak
        try {
            // Validar token
            health.put("keycloak", "UP");
        } catch (Exception e) {
            health.put("keycloak", "DOWN");
        }
        
        return Response.ok(health).build();
    }
}
```

### 13.3 Métricas y Auditoría

```java
@Stateless
@Interceptors({AuditInterceptor.class})
public class EquiposService {
    
    @Inject
    private Logger logger;
    
    @Inject
    private AuditRepository auditRepository;
    
    public Equipo crearEquipo(EquipoDTO dto) {
        // ... crear
        
        // Registrar auditoría
        auditRepository.registrar(
            usuarioActual.getId(),
            "CREAR_EQUIPO",
            "Equipo creado: " + equipo.getId(),
            new Date()
        );
        
        logger.info("Equipo creado por " + usuarioActual.getNombre() + 
                   " - ID: " + equipo.getId());
        
        return equipo;
    }
}
```

---

## 14. Guía de Desarrollo

### 14.1 Setup Inicial

```bash
# 1. Clonar repositorio
git clone https://github.com/DonyStreams/MantenimientosBackend.git
cd MantenimientosBackend

# 2. Verificar Java 11
java -version

# 3. Instalar Maven
mvn -version

# 4. Instalar Docker
docker --version
docker-compose --version

# 5. Compilar proyecto
mvn clean install

# 6. Iniciar contenedores
docker-compose up -d

# 7. Esperar ~30 segundos y verificar
docker logs tomee-server
```

### 14.2 Estructura de una Nueva Feature

**Crear un nuevo módulo (Ejemplo: "Calibraciones")**

1. **Entidad JPA**
   ```java
   @Entity
   @Table(name = "Calibraciones")
   public class Calibracion {
       @Id
       @GeneratedValue(strategy = GenerationType.IDENTITY)
       private Integer id;
       
       @ManyToOne
       @JoinColumn(name = "equipo_id")
       private Equipo equipo;
       // ...
   }
   ```

2. **DTO**
   ```java
   public class CalibrationDTO {
       private Integer equipoId;
       private LocalDateTime fecha;
       // ...
   }
   ```

3. **Repository**
   ```java
   @Repository
   public class CalibracionesRepository 
       implements BaseRepository<Calibracion, Integer> {
       // ...
   }
   ```

4. **Service**
   ```java
   @Stateless
   @Transactional
   public class CalibracionesService {
       @Inject
       private CalibracionesRepository repository;
       // ...
   }
   ```

5. **Controller**
   ```java
   @Path("/api/calibraciones")
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public class CalibracionesController {
       @Inject
       private CalibracionesService service;
       
       @GET
       @RolesAllowed({"ADMIN", "SUPERVISOR"})
       public Response listar() {
           // ...
       }
   }
   ```

6. **Frontend Component**
   ```typescript
   @Component({
     selector: 'app-calibraciones',
     templateUrl: './calibraciones.component.html',
     styleUrls: ['./calibraciones.component.scss']
   })
   export class CalibracionesComponent implements OnInit {
     @Inject private calibracionesService: CalibracionesService;
   }
   ```

### 14.3 Testing

**Test Unitario**
```java
@RunWith(ArquillianRunner.class)
public class EquiposServiceTest {
    
    @Inject
    private EquiposService service;
    
    @Test
    public void testCrearEquipo() {
        EquipoDTO dto = new EquipoDTO();
        dto.setNombre("Microscopio Test");
        
        Equipo resultado = service.crearEquipo(dto);
        
        assertNotNull(resultado.getId());
        assertEquals("Microscopio Test", resultado.getNombre());
    }
}
```

**Test de API**
```java
@RunWith(ArquillianRunner.class)
public class EquiposAPITest {
    
    @Test
    public void testGetEquipos() {
        Response response = given()
            .header("Authorization", "Bearer " + token)
            .when()
            .get("/api/equipos")
            .then()
            .statusCode(200)
            .extract()
            .response();
        
        List<EquipoDTO> equipos = 
            response.jsonPath().getList("data", EquipoDTO.class);
        
        assertNotNull(equipos);
    }
}
```

### 14.4 Best Practices

✅ **HACER:**
- Usar Deltaspike Data para operaciones CRUD
- Validar entrada con Bean Validation
- Loguear operaciones críticas
- Usar transacciones explícitas (@Transactional)
- Documentar APIs con comentarios
- Escribir pruebas unitarias

❌ **NO HACER:**
- Concatenar SQL (usar PreparedStatements)
- Confiar solo en autenticación frontend
- Almacenar contraseñas en código
- Hacer queries N+1
- Retornar entidades JPA directamente (usar DTOs)
- Ignorar excepciones sin loguear

---

## Conclusión

Esta documentación proporciona una base sólida para entender la arquitectura, componentes y flujos del sistema. Para mayor detalle, consultar:

- **README.md** - Instalación y uso
- **Configuraciones/** - Setup de Keycloak
- **Código fuente** - Ejemplos prácticos
- **postman_collection.json** - APIs disponibles

---

**Documento técnico completado.**  
*Última actualización: Febrero 2026*
