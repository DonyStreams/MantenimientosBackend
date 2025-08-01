# 🚀 Scripts de Despliegue - MantenimientosBackend

## 📋 Estrategia de Desarrollo

Este proyecto está configurado para separar **Keycloak** (que se mantiene corriendo) del **Backend TomEE** (que se reinicia frecuentemente durante desarrollo).

## 🔧 Scripts Disponibles

### 1. **Primera vez / Despliegue completo**
```powershell
.\deploy-mantenimientos.ps1
```
- Inicia Keycloak + TomEE desde cero
- Usar cuando configures el proyecto por primera vez

### 2. **Iniciar solo Keycloak (una vez)**
```powershell
.\start-keycloak.ps1
```
- Inicia solo Keycloak con datos persistentes
- Se mantiene corriendo hasta que lo detengas manualmente
- ✅ **Usar esto una vez y olvidarte**

### 3. **Desarrollo del Backend (uso diario)**
```powershell
.\deploy-backend.ps1
```
- Compila y reinicia solo TomEE
- Keycloak sigue corriendo sin interrupciones
- ⚡ **Desarrollo súper rápido**

## 🌐 URLs de Acceso

- **Keycloak Admin**: http://localhost:8080/admin
  - Usuario: `admin` | Password: `admin`
- **Backend API**: http://localhost:8081/MantenimientosBackend/
- **Frontend**: http://localhost:4200 (cuando ejecutes `ng serve`)

## 👤 Usuarios de Prueba

| Usuario     | Password      | Rol         |
|-------------|---------------|-------------|
| admin       | admin123      | Administrador |
| supervisor  | supervisor123 | Supervisor  |
| tecnico     | tecnico123    | Técnico     |

## 🔄 Flujo de Trabajo Recomendado

### Setup inicial (una sola vez):
```powershell
.\start-keycloak.ps1
```

### Desarrollo diario:
```powershell
# Hacer cambios en Java...
.\deploy-backend.ps1
# Probar cambios...
.\deploy-backend.ps1
# Repetir...
```

### Detener todo:
```powershell
# Detener TomEE
docker stop tomee-server

# Detener Keycloak (opcional)
docker-compose -f docker-compose-keycloak.yml down
```

## 💾 Persistencia de Datos

- **Keycloak**: Los datos se guardan en el volumen `keycloak_data`
- **Configuraciones**: Se mantienen entre reinicios
- **Usuarios**: No se pierden al reiniciar Keycloak

## 🐛 Solución de Problemas

### Keycloak no responde:
```powershell
docker logs keycloak-server
```

### TomEE no se conecta a Keycloak:
```powershell
docker logs tomee-server
```

### Limpiar todo y empezar de cero:
```powershell
docker-compose down
docker-compose -f docker-compose-keycloak.yml down
docker volume rm mantenimientosbackend_keycloak_data
.\deploy-mantenimientos.ps1
```

## Resultados de Pruebas

### ✅ Workflow Validado Exitosamente

**Tiempo de Setup Inicial (con Keycloak):**
- Keycloak: ~30 segundos (primera vez)
- Total: ~30 segundos

**Tiempo de Desarrollo (solo TomEE):**
- Compilación: ~10 segundos
- Docker build: ~10 segundos  
- Startup: ~5 segundos
- **Total: ~25 segundos** (4x más rápido vs reinicio completo)

**Estado Final:**
- ✅ Keycloak corriendo en puerto 8080
- ✅ TomEE corriendo en puerto 8081
- ✅ Backend respondiendo correctamente
- ✅ Comunicación entre servicios funcionando
- ✅ Scripts PowerShell sin errores de sintaxis

### Ventajas del Nuevo Workflow:
1. **Desarrollo más rápido**: 25s vs 2+ minutos
2. **Keycloak persistente**: No se pierden configuraciones
3. **Menos recursos**: Solo reinicia TomEE cuando es necesario
4. **Debugging mejorado**: Logs separados por servicio
