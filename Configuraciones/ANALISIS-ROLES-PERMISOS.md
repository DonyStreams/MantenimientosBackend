# 📊 Análisis Completo de Roles y Permisos - Sistema de Mantenimientos INACIF

## 📋 Resumen Ejecutivo

Este documento analiza el sistema de roles y la implementación de permisos del Sistema de Mantenimientos INACIF.

**Estado: ✅ IMPLEMENTADO** (Enero 2026)

---

## 🔐 1. Roles Definidos en Keycloak

| Rol | Descripción | Usuario de Prueba |
|-----|-------------|-------------------|
| **ADMIN** | Administrador del sistema - Control total | `admin` / `admin123` |
| **SUPERVISOR** | Supervisor de laboratorio - Gestión completa sin eliminar | `supervisor` / `supervisor123` |
| **TECNICO** | Técnico de mantenimiento - Ejecución de mantenimientos | `tecnico` / `tecnico123` |
| **TECNICO_EQUIPOS** | Técnico de equipos - Solo gestión de equipos | `tecnico.equipos` / `equipos123` |
| **USER** | Usuario de solo lectura - Consultas únicamente | `usuario.lectura` / `lectura123` |

---

## ✅ 2. Implementación Completada

### 2.1 Frontend Angular

| Componente | Estado | Archivo |
|------------|--------|---------|
| Filtrado de menú por rol | ✅ | `app.menu.component.ts` |
| Protección de rutas | ✅ | `pages-routing.module.ts` |
| AuthGuard con roles | ✅ | `auth.guard.ts` |
| Directiva `*appHasRole` | ✅ | `has-role.directive.ts` |
| Directiva `*tienePermiso` | ✅ | `tiene-permiso.directive.ts` |
| Métodos de permisos | ✅ | `keycloak.service.ts` |

### 2.2 Backend Java

| Componente | Estado | Archivo |
|------------|--------|---------|
| Anotación `@RequiresRole` | ✅ | `RequiresRole.java` |
| Filtro de autorización | ✅ | `RoleAuthorizationFilter.java` |
| Protección de Usuarios | ✅ | `UsuarioController.java` |
| Protección de Equipos | ✅ | `EquipoController.java` |
| Protección de Reportes | ✅ | `ReportesController.java` |

---

## 📐 3. Matriz de Permisos Propuesta

### 3.1 Módulo: Equipos

| Acción | ADMIN | SUPERVISOR | TECNICO | TECNICO_EQUIPOS | USER |
|--------|:-----:|:----------:|:-------:|:---------------:|:----:|
| Ver listado | ✅ | ✅ | ✅ | ✅ | ✅ |
| Ver detalle | ✅ | ✅ | ✅ | ✅ | ✅ |
| Crear | ✅ | ✅ | ❌ | ✅ | ❌ |
| Editar | ✅ | ✅ | ❌ | ✅ | ❌ |
| Eliminar | ✅ | ❌ | ❌ | ❌ | ❌ |
| Ver historial | ✅ | ✅ | ✅ | ✅ | ✅ |

### 3.2 Módulo: Categorías de Equipos

| Acción | ADMIN | SUPERVISOR | TECNICO | TECNICO_EQUIPOS | USER |
|--------|:-----:|:----------:|:-------:|:---------------:|:----:|
| Ver | ✅ | ✅ | ✅ | ✅ | ✅ |
| Crear | ✅ | ✅ | ❌ | ❌ | ❌ |
| Editar | ✅ | ✅ | ❌ | ❌ | ❌ |
| Eliminar | ✅ | ❌ | ❌ | ❌ | ❌ |

### 3.3 Módulo: Programaciones de Mantenimiento

| Acción | ADMIN | SUPERVISOR | TECNICO | TECNICO_EQUIPOS | USER |
|--------|:-----:|:----------:|:-------:|:---------------:|:----:|
| Ver calendario | ✅ | ✅ | ✅ | ❌ | ✅ |
| Ver listado | ✅ | ✅ | ✅ | ❌ | ✅ |
| Crear programación | ✅ | ✅ | ❌ | ❌ | ❌ |
| Editar programación | ✅ | ✅ | ❌ | ❌ | ❌ |
| Eliminar programación | ✅ | ❌ | ❌ | ❌ | ❌ |
| Ver historial | ✅ | ✅ | ✅ | ❌ | ✅ |

### 3.4 Módulo: Ejecuciones de Mantenimiento

| Acción | ADMIN | SUPERVISOR | TECNICO | TECNICO_EQUIPOS | USER |
|--------|:-----:|:----------:|:-------:|:---------------:|:----:|
| Ver ejecuciones | ✅ | ✅ | ✅ | ❌ | ✅ |
| Ejecutar mantenimiento | ✅ | ✅ | ✅ | ❌ | ❌ |
| Agregar comentarios | ✅ | ✅ | ✅ | ❌ | ❌ |
| Adjuntar evidencias | ✅ | ✅ | ✅ | ❌ | ❌ |
| Aprobar/Cerrar | ✅ | ✅ | ❌ | ❌ | ❌ |

### 3.5 Módulo: Tickets de Falla

| Acción | ADMIN | SUPERVISOR | TECNICO | TECNICO_EQUIPOS | USER |
|--------|:-----:|:----------:|:-------:|:---------------:|:----:|
| Ver tickets | ✅ | ✅ | ✅ | ✅ | ✅ |
| Crear ticket | ✅ | ✅ | ✅ | ✅ | ✅ |
| Asignar ticket | ✅ | ✅ | ❌ | ❌ | ❌ |
| Resolver ticket | ✅ | ✅ | ✅ | ❌ | ❌ |
| Cerrar ticket | ✅ | ✅ | ❌ | ❌ | ❌ |
| Reabrir ticket | ✅ | ✅ | ❌ | ❌ | ❌ |

### 3.6 Módulo: Contratos

| Acción | ADMIN | SUPERVISOR | TECNICO | TECNICO_EQUIPOS | USER |
|--------|:-----:|:----------:|:-------:|:---------------:|:----:|
| Ver contratos | ✅ | ✅ | ✅ | ❌ | ✅ |
| Crear contrato | ✅ | ✅ | ❌ | ❌ | ❌ |
| Editar contrato | ✅ | ✅ | ❌ | ❌ | ❌ |
| Eliminar contrato | ✅ | ❌ | ❌ | ❌ | ❌ |
| Subir documentos | ✅ | ✅ | ❌ | ❌ | ❌ |

### 3.7 Módulo: Proveedores

| Acción | ADMIN | SUPERVISOR | TECNICO | TECNICO_EQUIPOS | USER |
|--------|:-----:|:----------:|:-------:|:---------------:|:----:|
| Ver proveedores | ✅ | ✅ | ✅ | ❌ | ✅ |
| Crear proveedor | ✅ | ✅ | ❌ | ❌ | ❌ |
| Editar proveedor | ✅ | ✅ | ❌ | ❌ | ❌ |
| Eliminar proveedor | ✅ | ❌ | ❌ | ❌ | ❌ |

### 3.8 Módulo: Áreas

| Acción | ADMIN | SUPERVISOR | TECNICO | TECNICO_EQUIPOS | USER |
|--------|:-----:|:----------:|:-------:|:---------------:|:----:|
| Ver áreas | ✅ | ✅ | ✅ | ✅ | ✅ |
| Crear área | ✅ | ✅ | ❌ | ❌ | ❌ |
| Editar área | ✅ | ✅ | ❌ | ❌ | ❌ |
| Eliminar área | ✅ | ❌ | ❌ | ❌ | ❌ |

### 3.9 Módulo: Usuarios

| Acción | ADMIN | SUPERVISOR | TECNICO | TECNICO_EQUIPOS | USER |
|--------|:-----:|:----------:|:-------:|:---------------:|:----:|
| Ver usuarios | ✅ | ❌ | ❌ | ❌ | ❌ |
| Activar/Desactivar | ✅ | ❌ | ❌ | ❌ | ❌ |

### 3.10 Módulo: Reportes

| Acción | ADMIN | SUPERVISOR | TECNICO | TECNICO_EQUIPOS | USER |
|--------|:-----:|:----------:|:-------:|:---------------:|:----:|
| Ver dashboard | ✅ | ✅ | ✅ | ✅ | ✅ |
| Ver reportes | ✅ | ✅ | ❌ | ❌ | ❌ |
| Exportar Excel | ✅ | ✅ | ❌ | ❌ | ❌ |
| Exportar PDF | ✅ | ✅ | ❌ | ❌ | ❌ |

### 3.11 Módulo: Notificaciones

| Acción | ADMIN | SUPERVISOR | TECNICO | TECNICO_EQUIPOS | USER |
|--------|:-----:|:----------:|:-------:|:---------------:|:----:|
| Ver notificaciones | ✅ | ✅ | ✅ | ✅ | ✅ |
| Configurar alertas | ✅ | ✅ | ❌ | ❌ | ❌ |

---

## 🎯 4. Resumen por Rol

### 👑 ADMIN (Administrador)
**Acceso completo al sistema**
- ✅ CRUD completo en todos los módulos
- ✅ Eliminar registros (único rol con este permiso)
- ✅ Gestión de usuarios (activar/desactivar)
- ✅ Configuración del sistema
- ✅ Exportación de reportes
- ✅ Acceso a todas las secciones del menú

### 📋 SUPERVISOR (Supervisor de Laboratorio)
**Gestión operativa sin destrucción de datos**
- ✅ Ver y gestionar equipos, mantenimientos, tickets, contratos
- ✅ Crear y editar registros
- ❌ No puede eliminar registros permanentemente
- ❌ No gestiona usuarios
- ✅ Acceso a reportes y exportación
- ✅ Aprobar ejecuciones de mantenimiento

### 🔧 TECNICO (Técnico de Mantenimiento)
**Ejecución de mantenimientos y tickets**
- ✅ Ver información general del sistema
- ✅ Ejecutar mantenimientos programados
- ✅ Resolver tickets asignados
- ✅ Agregar comentarios y evidencias
- ❌ No puede crear programaciones
- ❌ No puede editar configuraciones
- ❌ No puede aprobar ejecuciones

### 🖥️ TECNICO_EQUIPOS (Técnico de Equipos)
**Gestión enfocada en inventario de equipos**
- ✅ CRUD de equipos (sin eliminar)
- ✅ Ver tickets relacionados con equipos
- ✅ Crear tickets de falla
- ❌ No accede a módulo de mantenimientos
- ❌ No accede a contratos ni proveedores
- ❌ No accede a reportes avanzados

### 👁️ USER (Usuario de Solo Lectura)
**Consulta de información únicamente**
- ✅ Ver dashboard y estadísticas generales
- ✅ Ver listados de equipos, mantenimientos, tickets
- ✅ Crear tickets de falla (reportar problemas)
- ❌ No puede modificar ningún registro
- ❌ No puede exportar información
- ❌ Acceso limitado a menú

---

## 📝 5. Plan de Implementación

### Fase 1: Protección de Rutas (Frontend)
Agregar `data: { roles: [] }` a cada ruta en los módulos de routing.

```typescript
// Ejemplo: usuarios-routing.module.ts
{ 
  path: '', 
  component: UsuariosComponent,
  canActivate: [AuthGuard],
  data: { roles: ['ADMIN'] }
}
```

### Fase 2: Filtrado del Menú
Modificar `app.menu.component.ts` para filtrar items según rol.

```typescript
// Agregar propiedad roles a cada item
{ 
  label: 'Usuarios', 
  icon: 'pi pi-fw pi-users', 
  routerLink: ['/administracion/usuarios'],
  roles: ['ADMIN']  // Solo ADMIN ve este item
}
```

### Fase 3: Validación en Backend
Crear anotación `@RequiresRole` para endpoints protegidos.

```java
@GET
@Path("/usuarios")
@RequiresRole({"ADMIN"})
public Response getUsuarios() { ... }
```

### Fase 4: Permisos Granulares en UI
Ocultar botones de acción según permisos específicos.

```html
<!-- Solo ADMIN puede eliminar -->
<button *appHasRole="'ADMIN'" (click)="eliminar()">Eliminar</button>

<!-- ADMIN y SUPERVISOR pueden editar -->
<button *appHasRole="['ADMIN', 'SUPERVISOR']" (click)="editar()">Editar</button>
```

---

## 📊 6. Métricas de Acceso por Módulo

| Módulo | ADMIN | SUPERVISOR | TECNICO | TECNICO_EQUIPOS | USER |
|--------|:-----:|:----------:|:-------:|:---------------:|:----:|
| Dashboard | ✅ | ✅ | ✅ | ✅ | ✅ |
| Equipos | ✅ | ✅ | ✅ | ✅ | ✅ |
| Categorías | ✅ | ✅ | ✅ | ✅ | ✅ |
| Mantenimientos | ✅ | ✅ | ✅ | ❌ | ✅ |
| Programaciones | ✅ | ✅ | ✅ | ❌ | ✅ |
| Ejecuciones | ✅ | ✅ | ✅ | ❌ | ✅ |
| Tickets | ✅ | ✅ | ✅ | ✅ | ✅ |
| Contratos | ✅ | ✅ | ✅ | ❌ | ✅ |
| Proveedores | ✅ | ✅ | ✅ | ❌ | ✅ |
| Áreas | ✅ | ✅ | ✅ | ✅ | ✅ |
| Usuarios | ✅ | ❌ | ❌ | ❌ | ❌ |
| Reportes | ✅ | ✅ | ❌ | ❌ | ❌ |
| Notificaciones | ✅ | ✅ | ✅ | ✅ | ✅ |

---

## 🔄 7. Próximos Pasos Recomendados

1. **Inmediato:**
   - [ ] Implementar filtrado de menú por roles
   - [ ] Agregar protección de rutas en routing modules

2. **Corto plazo:**
   - [ ] Implementar validación de roles en backend (endpoints críticos)
   - [ ] Completar directiva `tiene-permiso.directive.ts`

3. **Mediano plazo:**
   - [ ] Auditoría de acciones por rol
   - [ ] Logging de intentos de acceso no autorizado

---

## 📌 Notas Técnicas

- Los roles se obtienen del token JWT: `resource_access.inacif-frontend.roles`
- El `AuthGuard` ya soporta validación de roles vía `data: { roles: [] }`
- La directiva `*appHasRole` ya está funcional y documentada
- El backend valida autenticación pero aún no autorización por rol

---

*Documento generado para el Sistema de Mantenimientos INACIF*  
*Última actualización: Enero 2026*
