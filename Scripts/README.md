# Scripts de Base de Datos - Sistema Mantenimientos INACIF

## 📁 Archivos Disponibles

### 🧹 `Limpiar-Base-Datos.sql`
**Propósito:** Limpieza completa de la base de datos
- Elimina todas las tablas, vistas, procedimientos y funciones del sistema
- Limpia automáticamente todas las restricciones FK
- Prepara la base de datos para una instalación nueva
- **Usar cuando:** Quieras empezar desde cero

### 🚀 `Sistema-Completo-INACIF.sql`
**Propósito:** Instalación completa del sistema desde cero
- Crea toda la estructura de base de datos
- Incluye tablas principales + mejoras + programaciones
- Inserta datos iniciales de ejemplo
- Crea vistas, procedimientos, funciones y triggers
- Incluye validación automática al final
- **Usar cuando:** Quieras instalar todo el sistema completo

## 🔧 Instrucciones de Uso

### Para Instalación Nueva (Recomendado)
```bash
# 1. Limpiar base de datos existente
sqlcmd -S localhost -d MantenimientosDB -i "Scripts\Limpiar-Base-Datos.sql"

# 2. Instalar sistema completo
sqlcmd -S localhost -d MantenimientosDB -i "Scripts\Sistema-Completo-INACIF.sql"
```

### Para Instalación Solo del Sistema Completo
```bash
# Si la base de datos está vacía, puedes ejecutar directamente:
sqlcmd -S localhost -d MantenimientosDB -i "Scripts\Sistema-Completo-INACIF.sql"
```

## ✅ Validación Automática

El script `Sistema-Completo-INACIF.sql` incluye validación automática que reporta:
- Número de elementos instalados correctamente
- Porcentaje de éxito de la instalación
- Cantidad de datos insertados
- Estado final del sistema

## 🎯 Resultado Esperado

Después de ejecutar ambos scripts tendrás:
- **11 tablas principales** del sistema
- **6 tablas de mejoras** (alertas, programaciones, etc.)
- **2 vistas especializadas** para consultas
- **3 procedimientos almacenados** para automatización
- **2 funciones** para cálculos de negocio
- **Datos de ejemplo** para pruebas
- **Sistema completamente funcional**

## 🏗️ Arquitectura Resultante

### Tablas Principales
- `Usuarios` - Integración con Keycloak
- `Areas` - Catálogo de áreas/laboratorios
- `Equipos` - Catálogo de equipos
- `Tipos_Mantenimiento` - Tipos de mantenimiento
- `Proveedores` - Proveedores de servicio
- `Contratos` - Contratos de mantenimiento
- `Ejecuciones_Mantenimiento` - Registro de mantenimientos realizados
- `Tickets` - Sistema de incidencias

### Tablas de Mejoras
- `Estados_Mantenimiento` - Estados del ciclo de vida
- `Programaciones_Mantenimiento` - **⭐ NUEVA: Programaciones automáticas**
- `Notificaciones` - Sistema de alertas
- `Configuracion_Alertas` - Configuración de alertas
- `Documentos_Contrato` - Gestión de documentos
- `Seguimiento_Estado_Mantenimiento` - Trazabilidad de cambios

### APIs REST Disponibles (Backend Java)
- `/api/programaciones` - CRUD de programaciones
- `/api/alertas` - Dashboard y alertas
- `/api/equipos` - Gestión de equipos
- `/api/contratos` - Gestión de contratos
- `/api/mantenimientos` - Ejecuciones de mantenimiento

## 🔐 Seguridad y Integración

- **Keycloak:** Autenticación y autorización centralizada
- **Roles:** Gestión de permisos por roles de usuario
- **Auditoría:** Campos de auditoría en todas las tablas
- **Validación:** Constraints y validaciones de integridad

---
**Sistema de Mantenimientos INACIF - Versión 2.0 Completa**
*Fecha: Agosto 2025*
