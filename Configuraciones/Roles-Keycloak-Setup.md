# Configuración de Roles en Keycloak - Sistema de Mantenimientos INACIF

## 🔐 Datos de Acceso

### Keycloak Admin Console
- **URL**: http://172.16.1.192:8080/auth (producción) | http://localhost:8080/auth (desarrollo)
- **Usuario**: admin
- **Password**: admin
- **Realm**: MantenimientosINACIF
- **Cliente**: inacif-frontend

---

## 👥 Usuarios de Prueba Configurados

| Usuario | Password | Rol | Email |
|---------|----------|-----|-------|
| `admin` | `admin123` | ADMIN | admin@inacif.gob.gt |
| `supervisor` | `supervisor123` | SUPERVISOR | supervisor@inacif.gob.gt |
| `tecnico` | `tecnico123` | TECNICO | tecnico@inacif.gob.gt |
| `tecnico.equipos` | `equipos123` | TECNICO_EQUIPOS | tecnico.equipos@inacif.gob.gt |
| `usuario.lectura` | `lectura123` | USER | usuario.lectura@inacif.gob.gt |

> ⚠️ **Nota**: Las contraseñas están marcadas como temporales. Al primer login se solicitará cambiarlas.

---

## 🎭 Descripción de Roles

### 👑 ADMIN - Administrador del Sistema
**Acceso completo y sin restricciones**
- Control total sobre todos los módulos
- Único rol que puede ELIMINAR registros
- Gestión de usuarios (activar/desactivar)
- Configuración del sistema
- Exportación de reportes

### 📋 SUPERVISOR - Supervisor de Laboratorio
**Gestión operativa sin destrucción de datos**
- Crear y editar registros en todos los módulos
- Aprobar ejecuciones de mantenimiento
- Acceso a reportes y exportación
- **NO puede** eliminar registros
- **NO puede** gestionar usuarios

### 🔧 TECNICO - Técnico de Mantenimiento
**Ejecución operativa de mantenimientos**
- Ejecutar mantenimientos programados
- Resolver tickets asignados
- Agregar comentarios y evidencias
- **NO puede** crear programaciones
- **NO puede** aprobar ejecuciones

### 🖥️ TECNICO_EQUIPOS - Técnico de Equipos
**Gestión especializada de inventario**
- Crear y editar equipos
- Ver y crear tickets de falla
- **NO accede** a módulo de mantenimientos
- **NO accede** a contratos/proveedores

### 👁️ USER - Usuario de Solo Lectura
**Consulta de información**
- Ver dashboard y estadísticas
- Ver listados (equipos, mantenimientos, tickets)
- Crear tickets de falla (reportar problemas)
- **NO puede** modificar registros

---

## 📊 Matriz de Acceso por Módulo

| Módulo | ADMIN | SUPERVISOR | TECNICO | TECNICO_EQUIPOS | USER |
|--------|:-----:|:----------:|:-------:|:---------------:|:----:|
| Dashboard | ✅ | ✅ | ✅ | ✅ | ✅ |
| Equipos | ✅ CRUD | ✅ CRU | ✅ R | ✅ CRU | ✅ R |
| Categorías | ✅ CRUD | ✅ CRU | ✅ R | ✅ R | ✅ R |
| Mantenimientos | ✅ CRUD | ✅ CRU | ✅ R/Ejecutar | ❌ | ✅ R |
| Programaciones | ✅ CRUD | ✅ CRU | ✅ R | ❌ | ✅ R |
| Ejecuciones | ✅ Full | ✅ Aprobar | ✅ Ejecutar | ❌ | ✅ R |
| Tickets | ✅ CRUD | ✅ CRU/Cerrar | ✅ CRU/Resolver | ✅ CR | ✅ CR |
| Contratos | ✅ CRUD | ✅ CRU | ✅ R | ❌ | ✅ R |
| Proveedores | ✅ CRUD | ✅ CRU | ✅ R | ❌ | ✅ R |
| Áreas | ✅ CRUD | ✅ CRU | ✅ R | ✅ R | ✅ R |
| Usuarios | ✅ Full | ❌ | ❌ | ❌ | ❌ |
| Reportes | ✅ Full | ✅ Full | ❌ | ❌ | ❌ |
| Notificaciones | ✅ Config | ✅ Config | ✅ Ver | ✅ Ver | ✅ Ver |

> **Leyenda**: C=Crear, R=Leer, U=Actualizar, D=Eliminar

---

## 🔍 Verificar Token JWT

Después de hacer login, verificar el token en https://jwt.io

El token debe contener:
```json
{
  "resource_access": {
    "inacif-frontend": {
      "roles": ["ADMIN"]
    }
  },
  "preferred_username": "admin",
  "email": "admin@inacif.gob.gt"
}
```

---

## ⚙️ Pasos para Configurar Roles Manualmente

### 1. Acceder a Keycloak Admin Console
- Ir a http://172.16.1.192:8080/auth/admin
- Iniciar sesión con admin/admin
- Seleccionar realm "MantenimientosINACIF"

### 2. Crear Roles del Cliente
1. Ir a **Clients** > **inacif-frontend** > **Roles**
2. Click en **Create Role**
3. Crear cada rol: ADMIN, SUPERVISOR, TECNICO, TECNICO_EQUIPOS, USER

### 3. Asignar Roles a Usuarios
1. Ir a **Users** > seleccionar usuario
2. Tab **Role Mappings**
3. En "Client Roles" seleccionar **inacif-frontend**
4. Mover el rol deseado a "Assigned Roles"

### 4. Importar Configuración Completa
Usar el archivo `keycloak-simple.json` para importar realm completo:
```bash
# Desde Keycloak Admin > Realm Settings > Partial Import
# Subir archivo keycloak-simple.json
```

---

## 📝 Notas Adicionales

- Los roles están configurados a nivel de **cliente** (`inacif-frontend`), no a nivel de realm
- El frontend extrae roles desde `resource_access.inacif-frontend.roles`
- El backend valida el token contra el JWKS de Keycloak
- Existe validación adicional de "usuario activo" en la BD local

---

*Última actualización: Enero 2026*
