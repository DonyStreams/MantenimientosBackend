# 🚀 Guía de Inicio Rápido - Servidor 172.16.33.11

## ✅ Información Configurada

**Base de Datos:** 172.16.0.15:6060 (inventarios)  
**Keycloak:** http://172.16.1.192:8080/auth  
**Servidor:** 172.16.33.11:80 (Linux)  
**Repos:** GitHub público (DonyStreams)

---

## 📋 Paso a Paso para Desplegar

### 1️⃣ Conectar al Servidor

```bash
ssh usuario@172.16.33.11
```

### 2️⃣ Instalar Docker (Primera Vez)

```bash
# Descargar script de instalación
wget https://raw.githubusercontent.com/DonyStreams/MantenimientosBackend/main/install-docker.sh

# Dar permisos
chmod +x install-docker.sh

# Ejecutar como root
sudo ./install-docker.sh

# Cerrar sesión y volver a conectar
exit
ssh usuario@172.16.33.11
```

### 3️⃣ Crear Estructura de Directorios

```bash
sudo mkdir -p /opt/inacif
sudo chown -R $USER:$USER /opt/inacif
cd /opt/inacif
```

### 4️⃣ Clonar Repositorios

```bash
git clone https://github.com/DonyStreams/MantenimientosBackend.git
git clone https://github.com/DonyStreams/EPS-FRONTEND-USAC.git
```

### 5️⃣ Configurar Backend

```bash
cd /opt/inacif/MantenimientosBackend

# Copiar archivo de variables de entorno
cp .env.example .env

# Editar si es necesario (ya tiene valores correctos)
nano .env
```

**Verificar estas variables en .env:**
- `DB_HOST=172.16.0.15`
- `DB_PORT=6060`
- `DB_NAME=inventarios`
- `DB_USER=bsoto`
- `DB_PASSWORD=wPqbTMsN`
- `KEYCLOAK_URL=http://172.16.1.192:8080`
- `SERVER_URL=http://172.16.33.11`

### 6️⃣ Configurar Email

```bash
# Crear directorio de configuración
mkdir -p config

# Copiar template de email
cp src/main/resources/email.properties.template config/email.properties

# Editar con password SMTP real
nano config/email.properties
```

**Configurar en email.properties:**
```properties
mail.smtp.host=mail.inacif.gob.gt
mail.smtp.port=587
mail.smtp.user=mdapruebas
mail.smtp.password=TU_PASSWORD_SMTP_AQUI
mail.smtp.from.address=mdapruebas@inacif.gob.gt
```

### 7️⃣ Desplegar Sistema

```bash
# Dar permisos al script
chmod +x deploy.sh

# Ejecutar despliegue
./deploy.sh
```

El script automáticamente:
- ✅ Hace git pull de ambos repositorios
- ✅ Crea backup de versión actual
- ✅ Construye imágenes Docker
- ✅ Levanta contenedores
- ✅ Verifica que todo funcione

### 8️⃣ Verificar Funcionamiento

```bash
# Ver estado de contenedores
docker ps

# Ver logs del backend
docker logs -f inacif-backend

# Ver logs del frontend
docker logs -f inacif-frontend

# Probar health check
curl http://172.16.33.11:8080/MantenimientosBackend/api/health
```

---

## 🌐 URLs de Acceso

Una vez desplegado, acceder desde cualquier computadora en la red INACIF:

- **Frontend:** http://172.16.33.11
- **Backend API:** http://172.16.33.11:8080/MantenimientosBackend
- **Health Check:** http://172.16.33.11:8080/MantenimientosBackend/api/health

---

## 🔄 Actualizaciones Posteriores

Cuando hagas cambios en GitHub, simplemente ejecutar:

```bash
cd /opt/inacif/MantenimientosBackend
./deploy.sh
```

El script automáticamente descarga cambios y actualiza el sistema.

---

## 🛠️ Comandos Útiles

```bash
# Ver logs en tiempo real
docker logs -f inacif-backend
docker logs -f inacif-frontend

# Reiniciar servicios
docker restart inacif-backend
docker restart inacif-frontend

# Detener todo
cd /opt/inacif/MantenimientosBackend
docker-compose -f docker-compose.production.yml down

# Ver uso de recursos
docker stats

# Limpiar Docker (espacio)
docker system prune -a
```

---

## 🆘 Solución de Problemas

### Backend no inicia

```bash
# Ver logs completos
docker logs inacif-backend

# Verificar conectividad BD
ping 172.16.0.15
telnet 172.16.0.15 6060
```

### Frontend no carga

```bash
# Ver logs
docker logs inacif-frontend

# Verificar puerto
curl http://172.16.33.11/
```

### No puede conectar a Keycloak

```bash
# Verificar conectividad
ping 172.16.1.192
curl http://172.16.1.192:8080
```

---

## 📞 Soporte

- **Logs del sistema:** `/opt/inacif-logs/`
- **Backups:** `/opt/inacif/backups/`
- **Documentación completa:** Ver `README.md` en el repositorio

---

**Fecha de creación:** Febrero 2026  
**Servidor:** 172.16.33.11  
**SO:** Linux
