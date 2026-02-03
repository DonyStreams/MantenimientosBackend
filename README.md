# 🔧 Sistema de Gestión de Mantenimientos - INACIF

## 📋 Descripción General

Sistema integral de gestión de mantenimientos preventivos, correctivos y calibraciones de equipos para el Instituto Nacional de Ciencias Forenses (INACIF). Desarrollado con Java Jakarta EE en el backend y Angular en el frontend, con autenticación centralizada mediante Keycloak.

---

## 📚 Documentación

### 🚀 Despliegue en Producción
- **[INSTALACION-SERVIDOR.md](INSTALACION-SERVIDOR.md)** - Guía completa de instalación en servidor
- **[CHECKLIST-DESPLIEGUE.md](CHECKLIST-DESPLIEGUE.md)** - Lista de verificación pre-despliegue
- **[.env.example](.env.example)** - Plantilla de variables de entorno
- **Scripts de despliegue:**
  - `deploy.sh` - Script automatizado para Linux
  - `deploy.ps1` - Script automatizado para Windows

### 📖 Configuración
- **[Configuraciones/](Configuraciones/)** - Configuración de Keycloak y roles
- **[MantenimientosBackend.postman_collection.json](MantenimientosBackend.postman_collection.json)** - Colección de APIs

---

## 🏗️ Arquitectura del Sistema

### Stack Tecnológico

**Backend:**
- Java 11
- Jakarta EE 8 (JAX-RS, JPA, CDI)
- Apache TomEE 9.0.0-M7
- EclipseLink 2.7.7 (JPA)
- Apache Deltaspike 1.9.5
- Maven 3.8+

**Frontend:**
- Angular 17+
- PrimeNG 17.x
- TypeScript 5.x
- SCSS/CSS3
- Keycloak JS Adapter

**Infraestructura:**
- Keycloak 23+ (OAuth2/OpenID Connect)
- SQL Server / PostgreSQL
- Docker & Docker Compose
- Apache Nginx (producción)

### Arquitectura de Capas

```
┌─────────────────────────────────────┐
│         Angular Frontend            │
│    (PrimeNG + Keycloak Auth)        │
└──────────────┬──────────────────────┘
               │ HTTP/REST (JWT)
┌──────────────▼──────────────────────┐
│       JAX-RS Controllers            │
│   (Autorización por Roles)          │
├─────────────────────────────────────┤
│       Business Services             │
│   (Lógica de Negocio + CDI)         │
├─────────────────────────────────────┤
│      JPA Repositories               │
│   (EclipseLink + Deltaspike)        │
└──────────────┬──────────────────────┘
               │ JDBC
┌──────────────▼──────────────────────┐
│        Base de Datos SQL            │
│  (Tablas, Vistas, Procedimientos)   │
└─────────────────────────────────────┘
```

---

## 🔐 Seguridad y Autenticación

### Integración con Keycloak

El sistema utiliza Keycloak como Identity Provider (IdP) centralizado:

**Configuración del Realm:**
- **Realm:** `inacif`
- **Clients:**
  - `inacif-frontend` (Confidencial, flujo Authorization Code)
  - `inacif-backend` (Bearer-only, validación de tokens)

**Roles definidos:**
- `ADMIN` - Administrador del sistema (control total)
- `SUPERVISOR` - Supervisor de laboratorio (crear/editar sin eliminar)
- `TECNICO` - Técnico de mantenimiento (ejecutar mantenimientos y resolver tickets)
- `TECNICO_EQUIPOS` - Técnico de equipos (gestión de inventario)
- `USER` - Usuario solo lectura (consulta e informes)

**Flujo de autenticación:**
1. Usuario accede al frontend Angular
2. Redirección automática a Keycloak
3. Login exitoso → Keycloak genera JWT
4. Frontend almacena token y lo envía en cada petición
5. Backend valida token y extrae roles
6. Autorización por endpoint según roles requeridos

**Configuración backend (persistence.xml):**
```xml
<property name="keycloak.realm" value="inacif"/>
<property name="keycloak.auth-server-url" value="http://localhost:8180/auth"/>
<property name="keycloak.resource" value="inacif-backend"/>
```

**Configuración frontend (environment.ts):**
```typescript
keycloak: {
  url: 'http://localhost:8180',
  realm: 'inacif',
  clientId: 'inacif-frontend'
}
```

---

## 📦 Módulos Funcionales

### 1. Gestión de Usuarios
- Sincronización con Keycloak (campo `keycloak_id`)
- Asignación de roles y permisos
- Vinculación con áreas de trabajo
- Historial de accesos

### 2. Gestión de Equipos
- Catálogo completo de equipos
- Información técnica (marca, modelo, serie, fabricante)
- Ubicación por área/laboratorio
- Estados operativos (Operativo, Reparación, Baja, Calibración)
- Historial de cambios y movimientos
- Categorización por tipo de equipo

### 3. Mantenimientos
**Tipos soportados:**
- Preventivo
- Correctivo
- Calibración
- Verificación

**Programación automática:**
- Frecuencia configurable (días)
- Alertas automáticas antes de vencimiento (30, 15, 7 días)
- Generación automática de ejecuciones pendientes
- Scheduler con Quartz/Timer CDI

**Ejecución de mantenimientos:**
- Registro de fecha y hora
- Técnico responsable
- Observaciones y comentarios
- Estados: Planificado, En Proceso, Completado, Cancelado
- Adjuntar evidencias (PDF, imágenes, documentos)

### 4. Sistema de Tickets
**Ciclo de vida:**
```
Abierto → Asignado → En Proceso → Resuelto → Cerrado
```

**Características:**
- Prioridades: Baja, Media, Alta, Crítica
- Asignación automática por tipo de equipo
- Comentarios con tipos (técnico, seguimiento, alerta, resolución)
- Evidencias asociadas
- Notificaciones por email en tickets críticos
- Trazabilidad completa

### 5. Contratos y Proveedores
- Registro de contratos de mantenimiento
- Fechas de vigencia y renovación
- Montos y condiciones
- Asociación con equipos específicos
- Alertas de vencimiento (30, 15, 7 días antes)
- Vinculación con proveedores y tipos de mantenimiento

### 6. Sistema de Notificaciones

**Canales:**
- Notificaciones in-app (campana en navbar)
- Correos electrónicos automáticos

**Eventos notificables:**
- Ticket crítico creado
- Equipo en estado crítico
- Mantenimiento próximo a vencer (30, 15, 7 días)
- Mantenimiento vencido
- Contrato próximo a vencer (30, 15, 7 días)
- Contrato vencido

**Configuración de destinatarios:**
- Panel de administración para configurar correos por tipo de alerta
- Soporte para múltiples destinatarios (separados por `;`, `,` o saltos de línea)
- Validación de formato de email en tiempo real
- Fallback a email.properties si no hay configuración

**Propiedades de email (email.properties):**
```properties
mail.smtp.host=smtp.gmail.com
mail.smtp.port=587
mail.smtp.auth=true
mail.smtp.starttls.enable=true
mail.smtp.from=sistema@inacif.gob.gt
mail.smtp.username=usuario@gmail.com
mail.smtp.password=app-password
mail.admin.address=admin@inacif.gob.gt
mail.jefatura.address=jefatura@inacif.gob.gt
```

### 7. Reportes y Dashboard

**KPIs disponibles:**
- Total de equipos por estado
- Mantenimientos realizados vs pendientes
- Tickets abiertos por prioridad
- Contratos próximos a vencer
- Equipos críticos que requieren atención

**Reportes exportables:**
- PDF con plantillas personalizadas
- Excel (XLS/XLSX) con datos tabulares
- Filtros por fecha, área, tipo, estado

**Vistas especializadas:**
- `VW_AlertasMantenimiento` - Mantenimientos próximos a vencer
- `vw_DashboardMantenimientos` - Resumen ejecutivo
- `VW_EquiposCriticos` - Equipos que requieren atención inmediata

---

## 🗄️ Base de Datos

### Entidades Principales

**Usuarios**
- Integración con Keycloak (`keycloak_id`)
- Información de contacto y área

**Áreas**
- Organización jerárquica
- Ubicación física

**Equipos**
- Información técnica completa
- Historial de cambios
- Relación con contratos

**Mantenimientos**
- Tipos y frecuencias
- Programaciones automáticas
- Ejecuciones con evidencias

**Tickets**
- Flujo de estados
- Comentarios y evidencias
- Asignación de técnicos

**Contratos**
- Relación con proveedores
- Vigencias y montos
- Equipos cubiertos

**Notificaciones**
- Registro de alertas generadas
- Estado (leída/no leída)
- Prioridad y tipo

### Índices y Optimización

El sistema incluye índices optimizados para:
- Búsquedas por equipo y área
- Consultas de mantenimientos pendientes
- Filtrado de tickets por estado y prioridad
- Alertas no leídas por usuario

### Vistas Especializadas

```sql
-- Ejemplo de vista para alertas de mantenimiento
CREATE VIEW VW_AlertasMantenimiento AS
SELECT 
    e.id_equipo,
    e.nombre_equipo,
    pm.fecha_programada,
    DATEDIFF(day, GETDATE(), pm.fecha_programada) as dias_restantes,
    tm.nombre_tipo as tipo_mantenimiento
FROM Programaciones_Mantenimiento pm
INNER JOIN Equipos e ON pm.equipo_id = e.id_equipo
INNER JOIN Tipos_Mantenimiento tm ON pm.tipo_mantenimiento_id = tm.id_tipo
WHERE pm.estado = 'Pendiente'
AND DATEDIFF(day, GETDATE(), pm.fecha_programada) BETWEEN 0 AND 30;
```

---

## 🚀 Instalación y Despliegue

### Requisitos Previos

**Software necesario:**
- JDK 11+
- Maven 3.8+
- Docker 20.10+ y Docker Compose 2.x
- Node.js 18+ y npm 9+ (para frontend)
- Git

**Puertos requeridos:**
- `8080` - TomEE (Backend)
- `8180` - Keycloak
- `1433` - SQL Server (o 5432 para PostgreSQL)
- `4200` - Angular Dev Server
- `80/443` - Nginx (producción)

### Instalación Rápida - Backend (Local)

**1. Clonar repositorio:**
```bash
git clone https://github.com/DonyStreams/MantenimientosBackend.git
cd MantenimientosBackend
```

**2. Configurar base de datos:**
Editar `src/main/resources/META-INF/persistence.xml`:
```xml
<property name="javax.persistence.jdbc.url" value="jdbc:sqlserver://localhost:1433;databaseName=INACIF_Mantenimientos"/>
<property name="javax.persistence.jdbc.user" value="sa"/>
<property name="javax.persistence.jdbc.password" value="tu_password"/>
```

**3. Configurar Keycloak:**
Editar `src/main/resources/keycloak.json`:
```json
{
  "realm": "inacif",
  "auth-server-url": "http://localhost:8180",
  "resource": "inacif-backend",
  "credentials": {
    "secret": "tu-secret-aqui"
  }
}
```

**4. Configurar email:**
```bash
# Crear email.properties desde el template
cd src/main/resources
cp email.properties.template email.properties
# Editar email.properties con tus credenciales SMTP reales
```

⚠️ **IMPORTANTE:** `email.properties` contiene credenciales sensibles y está en `.gitignore`. Nunca lo subas al repositorio.

**5. Compilar y desplegar:**
```bash
# Compilar
mvn clean package -DskipTests

# Iniciar con Docker Compose (incluye TomEE)
docker-compose up -d

# Esperar inicio (~10 segundos)
# Ver logs
docker logs -f tomee-server

# Acceder a:
# http://localhost:8080/MantenimientosBackend/
```

### Instalación Frontend

**1. Navegar al proyecto frontend:**
```bash
cd EPS-FRONTEND
```

**2. Instalar dependencias:**
```bash
npm install
```

**3. Configurar environment:**
Editar `src/environments/environment.ts`:
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/MantenimientosBackend/api',
  keycloak: {
    url: 'http://localhost:8180',
    realm: 'inacif',
    clientId: 'inacif-frontend'
  }
};
```

**4. Ejecutar en desarrollo:**
```bash
ng serve
# Acceder a http://localhost:4200
```

### Tareas de VS Code

El proyecto incluye tareas preconfiguradas en `.vscode/tasks.json`:

**Desplegar y arrancar MantenimientosBackend:**
```bash
Ctrl+Shift+B (tarea por defecto)
```
Compila, copia el WAR al contenedor TomEE y reinicia el servidor.

**Iniciar Backend (primera vez):**
Compila el proyecto y levanta docker-compose desde cero.

**Ver Logs TomEE:**
Muestra logs en tiempo real del contenedor.

**Reiniciar Solo TomEE:**
Reinicia el contenedor sin recompilar.

**Parar MantenimientosBackend:**
Detiene todos los contenedores.

---

## 🐳 Docker y Contenedores

### Estructura de Contenedores

**docker-compose.yml (Desarrollo Local):**
```yaml
version: '3.8'
services:
  tomee-server:
    build: .
    ports:
      - "8080:8080"
    environment:
      - TOMEE_ADMIN_PASSWORD=admin
    volumes:
      - ./logs:/usr/local/tomee/logs
    networks:
      - inacif-network

  # Puedes agregar aquí otros servicios como PostgreSQL, Keycloak, etc.

networks:
  inacif-network:
    driver: bridge
```

**Dockerfile (TomEE):**
```dockerfile
FROM tomee:9.0.0-M7-plus
COPY target/MantenimientosBackend.war /usr/local/tomee/webapps/
EXPOSE 8080
CMD ["catalina.sh", "run"]
```

### Comandos Docker Útiles

```bash
# Construir y levantar servicios
docker-compose up --build -d

# Ver logs en tiempo real
docker logs -f tomee-server

# Reiniciar TomEE
docker restart tomee-server

# Detener todos los servicios
docker-compose down

# Limpiar volúmenes y redes
docker-compose down -v --remove-orphans

# Acceder al contenedor
docker exec -it tomee-server bash

# Ver recursos utilizados
docker stats tomee-server
```

---

## 🔧 Configuración de Email (SMTP)

Editar `src/main/resources/email.properties`. Ejemplos de configuración:

```properties
# Gmail
mail.smtp.host=smtp.gmail.com
mail.smtp.port=587
mail.smtp.auth=true
mail.smtp.starttls.enable=true

# Outlook/Office 365
mail.smtp.host=smtp.office365.com

# Servidor SMTP Institucional
mail.smtp.host=mail.inacif.gob.gt
mail.smtp.port=25
mail.smtp.auth=false

# Configuración común
mail.smtp.from=sistema.mantenimientos@inacif.gob.gt
mail.smtp.username=usuario@correo.com
mail.smtp.password=contraseña
mail.admin.address=admin@inacif.gob.gt
mail.jefatura.address=jefatura@inacif.gob.gt
```

**⚠️ Importante:**
- Para Gmail: generar "Contraseña de Aplicación" desde la configuración de cuenta
- Asegurar que el servidor SMTP permita relay desde la IP del servidor
- Configurar SPF/DKIM para evitar que correos caigan en spam
- `email.properties` está en `.gitignore` - nunca subirlo al repositorio

### Configuración de Scheduler

El sistema incluye tareas programadas para:
- Generar ejecuciones pendientes de mantenimiento
- Enviar alertas de vencimiento
- Verificar contratos próximos a expirar

**Frecuencias configurables en tabla `Configuracion_Scheduler`:**
```sql
INSERT INTO Configuracion_Scheduler (clave, valor, descripcion) VALUES
('scheduler.mantenimientos.cron', '0 0 2 * * ?', 'Ejecutar a las 2:00 AM diario'),
('scheduler.alertas.dias_previos', '30,15,7', 'Días previos para alertar'),
('scheduler.enabled', 'true', 'Activar/desactivar scheduler');
```

---

## 📡 API REST - Endpoints Principales

### Autenticación
Todos los endpoints requieren token JWT en header:
```
Authorization: Bearer <token>
```

### Equipos

**GET** `/api/equipos` - Listar equipos
```json
Query params: ?page=0&size=20&area=1&estado=Operativo
Response: {
  "content": [...],
  "totalElements": 100,
  "totalPages": 5
}
```

**GET** `/api/equipos/{id}` - Detalle de equipo

**POST** `/api/equipos` - Crear equipo (Rol: ADMIN, SUPERVISOR, TECNICO_EQUIPOS)
```json
{
  "nombreEquipo": "Microscopio Óptico",
  "marca": "Olympus",
  "modelo": "CX43",
  "serie": "ABC123",
  "areaId": 1,
  "estado": "Operativo"
}
```

**PUT** `/api/equipos/{id}` - Actualizar equipo

**DELETE** `/api/equipos/{id}` - Eliminar equipo (Rol: ADMIN)

### Mantenimientos

**GET** `/api/mantenimientos/programaciones` - Programaciones pendientes

**POST** `/api/mantenimientos/ejecutar` - Registrar ejecución
```json
{
  "equipoId": 1,
  "tipoMantenimientoId": 2,
  "tecnicoId": 5,
  "observaciones": "Mantenimiento preventivo completado",
  "archivos": ["base64..."]
}
```

**GET** `/api/mantenimientos/historial/{equipoId}` - Historial por equipo

### Tickets

**GET** `/api/tickets` - Listar tickets
```json
Query params: ?estado=Abierto&prioridad=Crítica
```

**POST** `/api/tickets` - Crear ticket
```json
{
  "equipoId": 1,
  "descripcion": "El equipo no enciende",
  "prioridad": "Alta",
  "solicitanteId": 10
}
```

**PUT** `/api/tickets/{id}/asignar` - Asignar técnico

**POST** `/api/tickets/{id}/comentarios` - Agregar comentario

**PUT** `/api/tickets/{id}/estado` - Cambiar estado

### Notificaciones

**GET** `/api/notificaciones` - Mis notificaciones

**GET** `/api/notificaciones/conteo` - Contadores
```json
Response: {
  "total": 15,
  "alta": 3,
  "media": 8,
  "baja": 4
}
```

**PUT** `/api/notificaciones/{id}/marcar-leida` - Marcar como leída

**DELETE** `/api/notificaciones/{id}` - Eliminar notificación

### Configuración de Correos (Admin)

**GET** `/api/configuracion-correos` - Obtener configuración
```json
Response: [
  {
    "tipo": "ticket_critico",
    "descripcion": "Ticket con prioridad crítica",
    "usuariosNotificar": "admin@inacif.gob.gt; jefe@inacif.gob.gt"
  },
  ...
]
```

**PUT** `/api/configuracion-correos/{tipo}` - Actualizar destinatarios
```json
{
  "usuariosNotificar": "nuevo@inacif.gob.gt; otro@inacif.gob.gt"
}
```

### Reportes

**GET** `/api/reportes/dashboard` - KPIs del dashboard

**GET** `/api/reportes/equipos/pdf` - Exportar equipos a PDF

**GET** `/api/reportes/mantenimientos/excel` - Exportar mantenimientos a Excel

---

## 🧪 Pruebas

### Pruebas Unitarias (Backend)

```bash
# Ejecutar todas las pruebas
mvn test

# Ejecutar pruebas de un módulo específico
mvn test -Dtest=EquipoServiceTest

# Generar reporte de cobertura
mvn jacoco:report
```

**Frameworks utilizados:**
- JUnit 4
- Mockito para mocks
- REST-assured para pruebas de API

### Pruebas de Integración

```bash
# Ejecutar pruebas de integración
mvn verify -P integration-tests

# Usar Testcontainers para BD en memoria
mvn verify -P testcontainers
```

### Pruebas Frontend

```bash
cd EPS-FRONTEND

# Ejecutar pruebas unitarias
ng test

# Pruebas con cobertura
ng test --code-coverage

# Pruebas E2E (Cypress)
npm run e2e
```

### Collection de Postman

El repositorio incluye `MantenimientosBackend.postman_collection.json` con ejemplos de todas las APIs.

**Importar en Postman:**
1. Abrir Postman
2. Import → File → Seleccionar el JSON
3. Configurar variables:
   - `base_url`: http://localhost:8080/MantenimientosBackend
   - `token`: (obtener de Keycloak)

---

## 📊 Monitoreo y Logs

### Logs del Sistema

**Ubicación de logs:**
- TomEE: `/usr/local/tomee/logs/catalina.out`
- Aplicación: `/usr/local/tomee/logs/MantenimientosBackend.log`
- Docker: `docker logs tomee-server`

**Niveles de log configurables en `logging.properties`:**
```properties
# Nivel general
.level=INFO

# Nivel para paquetes específicos
usac.eps.level=DEBUG
org.apache.deltaspike.level=INFO
org.eclipse.persistence.level=WARNING
```

### Monitoreo de Health

**Endpoint de salud:**
```bash
curl http://localhost:8080/MantenimientosBackend/api/health
```

**Response:**
```json
{
  "status": "UP",
  "database": "UP",
  "keycloak": "UP",
  "smtp": "UP"
}
```

---

## 🔒 Seguridad y Mejores Prácticas

### Validación de Entrada
- Validación en frontend (Angular forms)
- Validación en backend (Bean Validation)
- Sanitización de SQL (JPA PreparedStatements)
- Escapado de HTML en templates

### Protección CSRF
- Tokens CSRF en formularios
- SameSite cookies
- Validación de origen

### Headers de Seguridad
```
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
Strict-Transport-Security: max-age=31536000
Content-Security-Policy: default-src 'self'
```

### Auditoría
- Registro de acciones críticas
- Trazabilidad de cambios (campo `modificado_por`)
- Logs de acceso y errores

---

## 🚨 Solución de Problemas

### Backend no inicia

**Verificar logs:**
```bash
docker logs tomee-server
```

**Errores comunes:**
- Puerto 8080 en uso → Cambiar puerto en `docker-compose.yml`
- Conexión BD fallida → Verificar `persistence.xml`
- Keycloak no disponible → Verificar URL en configuración

### Frontend no conecta con backend

**Verificar:**
1. Backend en ejecución: `curl http://localhost:8080/MantenimientosBackend/api/health`
2. CORS habilitado en backend
3. URL correcta en `environment.ts`
4. Token válido en localStorage

**Error CORS:**
Agregar en `ApplicationConfig.java`:
```java
@Override
public Set<Object> getSingletons() {
    Set<Object> singletons = new HashSet<>();
    singletons.add(new CorsFilter());
    return singletons;
}
```

### Emails no se envían

**Verificar:**
1. Configuración `email.properties` correcta
2. Credenciales SMTP válidas
3. Firewall no bloquea puerto 587/25
4. Logs de EmailService para errores

**Probar conexión SMTP:**
```bash
telnet smtp.gmail.com 587
```

### Keycloak: Token inválido

**Soluciones:**
1. Sincronizar reloj del servidor
2. Verificar `realm` y `clientId` correctos
3. Regenerar secret en Keycloak Admin
4. Limpiar cache del navegador

---

## 📚 Recursos Adicionales

### Documentación Oficial
- [Jakarta EE 8 Specs](https://jakarta.ee/specifications/platform/8/)
- [Apache TomEE Documentation](https://tomee.apache.org/documentation.html)
- [Angular Official Docs](https://angular.io/docs)
- [PrimeNG Components](https://primeng.org/)
- [Keycloak Documentation](https://www.keycloak.org/documentation)

### Guías de Referencia
- **Configuración de Keycloak:** `Configuraciones/Roles-Keycloak-Setup.md`
- **Análisis de Roles:** `Configuraciones/ANALISIS-ROLES-PERMISOS.md`

### Postman Collection
- `MantenimientosBackend.postman_collection.json` - Ejemplos de todas las APIs

---

## 👥 Equipo de Desarrollo

**Desarrollado para:**
Instituto Nacional de Ciencias Forenses de Guatemala (INACIF)

**Contacto técnico:**
- Email: soporte.sistemas@inacif.gob.gt

---

## 📄 Licencia

Este sistema es propiedad del INACIF y su uso está restringido para fines institucionales.

---

## 🔄 Versionamiento

**Versión actual:** 2.0.0

**Historial de versiones:**
- **2.0.0** (Feb 2026) - Sistema completo con notificaciones email, configuración de alertas, dashboard mejorado
- **1.5.0** (Ene 2026) - Integración Keycloak, módulo de tickets
- **1.0.0** (Dic 2025) - MVP con gestión básica de equipos y mantenimientos

---

## 🗺️ Roadmap

### Próximas funcionalidades:
- [ ] Firma digital de mantenimientos (PKCS#7)
- [ ] App móvil para técnicos (React Native)
- [ ] Integración con sistema de inventarios
- [ ] Dashboard predictivo con Machine Learning
- [ ] Migración a microservicios
- [ ] Soporte offline con sincronización

---

**Última actualización:** Febrero 2026
