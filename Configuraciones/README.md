# 📁 Configuraciones del Sistema

Esta carpeta contiene archivos de configuración y documentación de Keycloak.

## 📄 Archivos

### keycloak-simple.json
Configuración de Keycloak para importación rápida del realm `inacif`.

**Uso:**
1. Acceder a Keycloak Admin Console
2. Ir a **Realm Settings** → **Partial Import**
3. Subir este archivo
4. Importar usuarios, roles y configuración del cliente

### Roles-Keycloak-Setup.md
Guía completa de configuración de roles en Keycloak:
- Usuarios de prueba con credenciales
- Descripción detallada de cada rol
- Instrucciones paso a paso para configurar manualmente
- Verificación de tokens JWT

### ANALISIS-ROLES-PERMISOS.md
Análisis exhaustivo del sistema de permisos:
- Matriz completa de permisos por módulo y rol
- Implementación en frontend y backend
- Plan de implementación por fases
- Métricas de acceso

## 🔐 Roles Definidos

| Rol | Descripción |
|-----|-------------|
| **ADMIN** | Administrador del sistema - Control total |
| **SUPERVISOR** | Supervisor de laboratorio - Gestión sin eliminar |
| **TECNICO** | Técnico de mantenimiento - Ejecutar mantenimientos |
| **TECNICO_EQUIPOS** | Técnico de equipos - Gestión de inventario |
| **USER** | Usuario de solo lectura - Consultas |

## 🚀 Inicio Rápido

Para configurar Keycloak en un nuevo ambiente:

```bash
# 1. Levantar Keycloak (si no está corriendo)
docker run -p 8180:8080 \
  -e KEYCLOAK_USER=admin \
  -e KEYCLOAK_PASSWORD=admin \
  quay.io/keycloak/keycloak:23.0.0

# 2. Acceder a Admin Console
# http://localhost:8180

# 3. Importar keycloak-simple.json
# Realm Settings → Partial Import

# 4. Verificar usuarios de prueba
# Users → Ver lista de usuarios importados
```

## 📚 Referencias

Ver [README.md](../README.md) principal para:
- Instalación completa del sistema
- Configuración de email y scheduler
- Despliegue con Docker
- APIs y endpoints
