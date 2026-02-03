# 🚀 Guía de Instalación en Servidor - Producción

## 📋 Prerequisitos del Servidor

### Hardware Mínimo Recomendado
- **CPU:** 4 cores
- **RAM:** 8 GB
- **Disco:** 50 GB SSD
- **Red:** Acceso VPN a red INACIF

### Software Requerido
- **SO:** Ubuntu 20.04+ / CentOS 8+ / Windows Server 2019+
- **Docker:** 20.10+
- **Docker Compose:** 2.x
- **Git:** 2.x
- **VPN:** Cliente OpenVPN o similar para acceso a red INACIF

---

## 🔧 Instalación Paso a Paso (Linux)

### 1. Instalar Docker y Docker Compose

```bash
# Actualizar sistema
sudo apt update && sudo apt upgrade -y

# Instalar Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Agregar usuario al grupo docker
sudo usermod -aG docker $USER

# Instalar Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Verificar instalación
docker --version
docker-compose --version
```

### 2. Instalar Git

```bash
sudo apt install git -y
git --version
```

### 3. Crear Estructura de Directorios

```bash
# Crear directorios principales
sudo mkdir -p /opt/inacif
sudo mkdir -p /opt/inacif-data/evidencias
sudo mkdir -p /opt/inacif-logs
sudo mkdir -p /opt/inacif/backups

# Dar permisos al usuario actual
sudo chown -R $USER:$USER /opt/inacif
sudo chown -R $USER:$USER /opt/inacif-data
sudo chown -R $USER:$USER /opt/inacif-logs
```

### 4. Clonar Repositorios

```bash
cd /opt/inacif

# Clonar backend
git clone <URL_REPO_BACKEND> MantenimientosBackend

# Clonar frontend
git clone <URL_REPO_FRONTEND> EPS-FRONTEND
```

### 5. Configurar Variables de Entorno

```bash
cd /opt/inacif/MantenimientosBackend

# Copiar archivo de ejemplo
cp .env.example .env

# Editar con valores reales
nano .env
```

**Completar con los valores proporcionados:**
```bash
# Base de datos
DB_HOST=172.16.1.xxx
DB_PORT=1433
DB_NAME=INACIF_Mantenimientos
DB_USER=usuario_db
DB_PASSWORD=password_db

# Keycloak
KEYCLOAK_URL=http://172.16.1.192:8080
KEYCLOAK_REALM=MantenimientosINACIF
KEYCLOAK_CLIENT_SECRET=secret-real-aqui

# Email
SMTP_HOST=mail.inacif.gob.gt
SMTP_PORT=587
SMTP_USER=mdapruebas
SMTP_PASSWORD=password-real-aqui

# Servidor
SERVER_URL=http://172.16.1.50
SERVER_DOMAIN=mantenimientos.inacif.gob.gt
HTTP_PORT=80

# Repositorios
GIT_BACKEND_REPO=https://github.com/DonyStreams/MantenimientosBackend.git
GIT_FRONTEND_REPO=https://github.com/DonyStreams/EPS-FRONTEND-USAC.git
GIT_BRANCH=main

# Rutas
STORAGE_PATH=/opt/inacif-data
LOGS_PATH=/opt/inacif-logs
```

### 6. Configurar Archivos de Propiedades

```bash
# Crear directorio config
mkdir -p config

# Copiar template de email
cp src/main/resources/email.properties.template config/email.properties

# Editar email.properties con credenciales SMTP reales
nano config/email.properties

# Copiar scheduler.properties
cp src/main/resources/scheduler.properties config/scheduler.properties
```

### 7. Configurar Frontend (environment.prod.ts)

```bash
cd /opt/inacif/EPS-FRONTEND

# Editar environment de producción
nano src/environments/environment.prod.ts
```

```typescript
export const environment = {
  production: true,
  apiUrl: 'http://172.16.1.50:8080/MantenimientosBackend/api',
  keycloak: {
    url: 'http://172.16.1.192:8080',
    realm: 'MantenimientosINACIF',
    clientId: 'inacif-frontend'
  }
};
```

### 8. Ejecutar Despliegue Inicial

```bash
cd /opt/inacif/MantenimientosBackend

# Dar permisos de ejecución al script
chmod +x deploy.sh

# Ejecutar despliegue
./deploy.sh
```

El script automáticamente:
- ✅ Verificará prerrequisitos
- ✅ Creará backups
- ✅ Construirá imágenes Docker
- ✅ Levantará los contenedores
- ✅ Verificará que todo esté funcionando

### 9. Verificar Instalación

```bash
# Ver logs en tiempo real
docker logs -f inacif-backend
docker logs -f inacif-frontend

# Ver estado de contenedores
docker ps

# Verificar health
curl http://localhost:8080/MantenimientosBackend/api/health

# Acceder a la aplicación
# Frontend: http://172.16.1.50
# Backend:  http://172.16.1.50:8080/MantenimientosBackend
```

---

## 🔧 Instalación en Windows Server

### 1. Instalar Docker Desktop

Descargar e instalar desde: https://www.docker.com/products/docker-desktop

### 2. Instalar Git

Descargar desde: https://git-scm.com/download/win

### 3. Crear Directorios

```powershell
# PowerShell como Administrador
New-Item -ItemType Directory -Path "C:\inacif" -Force
New-Item -ItemType Directory -Path "C:\inacif-data\evidencias" -Force
New-Item -ItemType Directory -Path "C:\inacif-logs" -Force
New-Item -ItemType Directory -Path "C:\inacif\backups" -Force
```

### 4. Clonar Repositorios

```powershell
cd C:\inacif
git clone <URL_REPO_BACKEND> MantenimientosBackend
git clone <URL_REPO_FRONTEND> EPS-FRONTEND
```

### 5. Configurar .env

```powershell
cd C:\inacif\MantenimientosBackend
Copy-Item .env.example .env
notepad .env
```

### 6. Ejecutar Despliegue

```powershell
cd C:\inacif\MantenimientosBackend
.\deploy.ps1
```

---

## 🔄 Actualizaciones Posteriores

Para desplegar nuevos cambios del repositorio:

```bash
# Linux
cd /opt/inacif/MantenimientosBackend
./deploy.sh

# Windows
cd C:\inacif\MantenimientosBackend
.\deploy.ps1
```

El script automáticamente:
1. Crea backup de la versión actual
2. Descarga últimos cambios de Git (git pull)
3. Reconstruye imágenes Docker
4. Reinicia contenedores
5. Verifica que todo funcione

---

## 📊 Monitoreo y Mantenimiento

### Ver Logs

```bash
# Logs en tiempo real
docker logs -f inacif-backend
docker logs -f inacif-frontend

# Últimas 100 líneas
docker logs --tail 100 inacif-backend

# Logs desde hace 1 hora
docker logs --since 1h inacif-backend
```

### Reiniciar Servicios

```bash
cd /opt/inacif/MantenimientosBackend

# Reiniciar todo
docker-compose -f docker-compose.production.yml restart

# Reiniciar solo backend
docker restart inacif-backend

# Reiniciar solo frontend
docker restart inacif-frontend
```

### Detener Servicios

```bash
# Detener sin eliminar datos
docker-compose -f docker-compose.production.yml stop

# Detener y eliminar contenedores (no elimina volúmenes)
docker-compose -f docker-compose.production.yml down

# Detener todo incluyendo volúmenes (CUIDADO: elimina datos)
docker-compose -f docker-compose.production.yml down -v
```

### Backup Manual

```bash
# Backup de datos
sudo tar -czf /opt/inacif/backups/data_$(date +%Y%m%d).tar.gz -C /opt/inacif-data .

# Backup de base de datos (desde SQL Server)
# Ejecutar desde SQL Server Management Studio o comando sqlcmd
```

---

## 🆘 Solución de Problemas

### Contenedores no inician

```bash
# Ver logs detallados
docker-compose -f docker-compose.production.yml logs

# Verificar recursos
docker stats

# Verificar red
docker network ls
docker network inspect inacif_inacif-network
```

### Error de conexión a BD

```bash
# Verificar conectividad VPN
ping 172.16.1.xxx

# Probar conexión SQL
telnet 172.16.1.xxx 1433

# Ver variables de entorno del contenedor
docker exec inacif-backend env | grep DB_
```

### Error de permisos

```bash
# Dar permisos a directorios
sudo chown -R 1000:1000 /opt/inacif-data
sudo chmod -R 755 /opt/inacif-data
```

### Limpiar Docker

```bash
# Limpiar imágenes sin usar
docker image prune -a

# Limpiar contenedores detenidos
docker container prune

# Limpiar todo (CUIDADO)
docker system prune -a --volumes
```

---

## 🔐 Seguridad

### Firewall

```bash
# Ubuntu UFW
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable

# CentOS Firewalld
sudo firewall-cmd --permanent --add-port=80/tcp
sudo firewall-cmd --permanent --add-port=443/tcp
sudo firewall-cmd --reload
```

### SSL/HTTPS (Opcional)

Para configurar HTTPS con Let's Encrypt:

```bash
# Instalar Certbot
sudo apt install certbot python3-certbot-nginx

# Obtener certificado
sudo certbot --nginx -d mantenimientos.inacif.gob.gt

# Renovación automática
sudo certbot renew --dry-run
```

---

## 📞 Soporte

Para problemas durante la instalación:
- Email: soporte.sistemas@inacif.gob.gt
- Verificar logs en: `/opt/inacif-logs/`
- Revisar backups en: `/opt/inacif/backups/`

---

**Última actualización:** Febrero 2026
