# ✅ Checklist Pre-Despliegue en Servidor

Usar esta lista antes de ejecutar el despliegue en producción.

## 📋 Información Requerida

### Base de Datos
- [ ] IP/Host del servidor SQL Server: `_________________`
- [ ] Puerto: `_________________` (típicamente 1433)
- [ ] Nombre de la base de datos: `_________________`
- [ ] Usuario de BD: `_________________`
- [ ] Contraseña de BD: `_________________`
- [ ] Script SQL ejecutado y base de datos creada

### Keycloak
- [ ] URL de Keycloak: `_________________` (ej: http://172.16.1.192:8080)
- [ ] Realm configurado: `MantenimientosINACIF`
- [ ] Client `inacif-backend` creado
- [ ] Client `inacif-frontend` creado
- [ ] Client Secret de `inacif-backend`: `_________________`
- [ ] Roles creados (ADMIN, SUPERVISOR, TECNICO, TECNICO_EQUIPOS, USER)
- [ ] Usuarios de prueba configurados

### Servidor de Despliegue
- [ ] IP del servidor: `_________________`
- [ ] Dominio (opcional): `_________________`
- [ ] Puerto HTTP: `_________________` (típicamente 80)
- [ ] Puerto HTTPS (si aplica): `_________________` (típicamente 443)
- [ ] VPN configurada y conectada
- [ ] Acceso SSH al servidor (Linux) o RDP (Windows)

### Email SMTP
- [ ] Servidor SMTP: `_________________`
- [ ] Puerto SMTP: `_________________` (típicamente 587 o 25)
- [ ] Usuario SMTP: `_________________`
- [ ] Contraseña SMTP: `_________________`
- [ ] Email remitente: `_________________`
- [ ] Email administrador: `_________________`
- [ ] Email jefatura: `_________________`

### Repositorios Git
- [ ] URL del repositorio Backend: `_________________`
- [ ] URL del repositorio Frontend: `_________________`
- [ ] Branch a usar: `_________________` (main/master)
- [ ] Acceso a los repositorios configurado (SSH key o HTTPS)

---

## 🔧 Configuración del Servidor

### Software Instalado
- [ ] Docker instalado (versión 20.10+)
- [ ] Docker Compose instalado (versión 2.x)
- [ ] Git instalado
- [ ] Cliente VPN instalado y configurado

### Directorios Creados
- [ ] `/opt/inacif` (Linux) o `C:\inacif` (Windows)
- [ ] `/opt/inacif-data` para almacenar evidencias
- [ ] `/opt/inacif-logs` para logs
- [ ] `/opt/inacif/backups` para backups

### Permisos y Firewall
- [ ] Usuario tiene permisos para ejecutar Docker
- [ ] Puertos 80/443 abiertos en firewall
- [ ] Permisos de escritura en directorios de datos

---

## 📝 Archivos de Configuración

### Archivo .env
- [ ] Copiado desde `.env.example`
- [ ] Variable `DB_HOST` configurada
- [ ] Variable `DB_PORT` configurada
- [ ] Variable `DB_NAME` configurada
- [ ] Variable `DB_USER` configurada
- [ ] Variable `DB_PASSWORD` configurada
- [ ] Variable `KEYCLOAK_URL` configurada
- [ ] Variable `KEYCLOAK_CLIENT_SECRET` configurada
- [ ] Variables SMTP configuradas
- [ ] Variables `GIT_BACKEND_REPO` y `GIT_FRONTEND_REPO` configuradas
- [ ] Variable `SERVER_URL` configurada

### Archivo config/email.properties
- [ ] Creado desde `email.properties.template`
- [ ] Credenciales SMTP reales configuradas
- [ ] Emails de administrador y jefatura configurados

### Archivo src/environments/environment.prod.ts (Frontend)
- [ ] Variable `apiUrl` apunta al backend correcto
- [ ] Configuración de Keycloak correcta (`url`, `realm`, `clientId`)

---

## 🧪 Pruebas Pre-Despliegue

### Conectividad
- [ ] Ping exitoso a servidor de base de datos
- [ ] Telnet exitoso al puerto de base de datos
- [ ] Ping exitoso a servidor Keycloak
- [ ] Acceso HTTP a Keycloak Admin Console

### Credenciales
- [ ] Login exitoso a SQL Server con las credenciales proporcionadas
- [ ] Login exitoso a Keycloak Admin Console
- [ ] Cliente de email puede autenticarse con SMTP

---

## 🚀 Despliegue

### Ejecución del Script
- [ ] Repositorios clonados en el servidor
- [ ] Archivo `.env` copiado y configurado
- [ ] Script `deploy.sh` (Linux) o `deploy.ps1` (Windows) tiene permisos de ejecución
- [ ] Script ejecutado sin errores
- [ ] Contenedores `inacif-backend` e `inacif-frontend` en estado `Up`

### Verificación Post-Despliegue
- [ ] Health check responde: `curl http://localhost:8080/MantenimientosBackend/api/health`
- [ ] Frontend accesible en navegador: `http://SERVIDOR_IP`
- [ ] Login en frontend exitoso con usuario de Keycloak
- [ ] Backend conectado a base de datos (health check muestra "database": "UP")
- [ ] Logs de backend sin errores críticos: `docker logs inacif-backend`
- [ ] Logs de frontend sin errores: `docker logs inacif-frontend`

---

## 📊 Monitoreo Inicial

### Primera Semana
- [ ] Revisar logs diarios
- [ ] Verificar espacio en disco
- [ ] Confirmar que scheduler de notificaciones funciona
- [ ] Verificar envío de emails de prueba
- [ ] Backup manual de base de datos

### Documentación
- [ ] Credenciales guardadas en lugar seguro
- [ ] IPs y URLs documentadas
- [ ] Usuarios de Keycloak documentados
- [ ] Procedimiento de backup documentado

---

## 🆘 Contactos de Soporte

| Área | Contacto | Email/Teléfono |
|------|----------|----------------|
| Base de Datos | _______________ | _______________ |
| Keycloak | _______________ | _______________ |
| Infraestructura | _______________ | _______________ |
| Desarrollo | _______________ | _______________ |

---

## 📌 Notas Adicionales

Usar este espacio para notas específicas del despliegue:

```
_____________________________________________________________________

_____________________________________________________________________

_____________________________________________________________________

_____________________________________________________________________
```

---

**Fecha de despliegue:** _______________  
**Responsable:** _______________  
**Firma:** _______________
