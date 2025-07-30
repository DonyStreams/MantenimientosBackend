# Configuración de Roles en Keycloak

## Pasos para configurar los roles:

### 1. Acceder a Keycloak Admin Console
- Ir a http://localhost:8080
- Acceder con admin/admin
- Seleccionar el realm "demo"

### 2. Crear Roles del Cliente
Ir a Clients > my-angular-client > Roles > Create Role

Crear estos roles:
- **ADMIN**: Acceso completo al sistema
- **SUPERVISOR**: Gestión de equipos y mantenimientos  
- **TECNICO**: Ejecución de mantenimientos
- **TECNICO_EQUIPOS**: Solo gestión de equipos
- **USER**: Solo lectura

### 3. Asignar Roles a Usuarios
Ir a Users > [seleccionar usuario] > Role Mappings > Client Roles > my-angular-client

Seleccionar los roles apropiados para cada usuario.

### 4. Verificar Token
En el navegador, después de hacer login, puedes verificar el token JWT en:
- Network tab > cualquier request > Headers > Authorization
- Copiar el token y verificarlo en https://jwt.io

El token debe contener:
```json
{
  "resource_access": {
    "my-angular-client": {
      "roles": ["ADMIN", "SUPERVISOR", etc...]
    }
  }
}
```

## Roles y Permisos por Módulo:

### Equipos:
- **Ver**: ADMIN, SUPERVISOR, TECNICO_EQUIPOS, USER
- **Crear**: ADMIN, SUPERVISOR  
- **Editar**: ADMIN, SUPERVISOR
- **Eliminar**: ADMIN

### Mantenimientos:
- **Ver**: ADMIN, SUPERVISOR, TECNICO
- **Crear**: ADMIN, SUPERVISOR
- **Ejecutar**: ADMIN, SUPERVISOR, TECNICO

### Administración:
- **Gestionar Usuarios**: ADMIN
- **Ver Reportes**: ADMIN, SUPERVISOR
- **Exportar Reportes**: ADMIN
