#!/bin/bash
# ============================================
# Script de Despliegue Automatizado
# Sistema de Mantenimientos INACIF
# ============================================

set -e  # Salir si hay error

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Variables
BACKEND_DIR="/opt/inacif/MantenimientosBackend"
FRONTEND_DIR="/opt/inacif/EPS-FRONTEND"
BACKUP_DIR="/opt/inacif/backups"
LOG_FILE="/var/log/inacif-deploy.log"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# Función para logging
log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1" | tee -a "$LOG_FILE"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1" | tee -a "$LOG_FILE"
    exit 1
}

warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1" | tee -a "$LOG_FILE"
}

# ============================================
# 1. Verificar prerrequisitos
# ============================================
log "Verificando prerrequisitos..."

if ! command -v docker &> /dev/null; then
    error "Docker no está instalado"
fi

if ! command -v docker-compose &> /dev/null; then
    error "Docker Compose no está instalado"
fi

if ! command -v git &> /dev/null; then
    error "Git no está instalado"
fi

# Verificar conectividad VPN (ping a base de datos)
if ! timeout 3 bash -c "cat < /dev/null > /dev/tcp/${DB_HOST:-172.16.1.100}/${DB_PORT:-1433}"; then
    warning "No se puede conectar a la base de datos. ¿VPN conectada?"
fi

# ============================================
# 2. Crear backup de la versión actual
# ============================================
log "Creando backup..."

mkdir -p "$BACKUP_DIR"

if [ -d "$BACKEND_DIR" ]; then
    tar -czf "$BACKUP_DIR/backend_$TIMESTAMP.tar.gz" -C "$BACKEND_DIR" . 2>/dev/null || true
    log "Backup backend creado: backend_$TIMESTAMP.tar.gz"
fi

if [ -d "$FRONTEND_DIR" ]; then
    tar -czf "$BACKUP_DIR/frontend_$TIMESTAMP.tar.gz" -C "$FRONTEND_DIR" . 2>/dev/null || true
    log "Backup frontend creado: frontend_$TIMESTAMP.tar.gz"
fi

# Limpiar backups antiguos (mantener últimos 5)
ls -t "$BACKUP_DIR"/*.tar.gz | tail -n +6 | xargs -r rm 2>/dev/null || true

# ============================================
# 3. Actualizar código desde Git
# ============================================
log "Actualizando código desde repositorios..."

# Backend
if [ -d "$BACKEND_DIR/.git" ]; then
    log "Actualizando backend..."
    cd "$BACKEND_DIR"
    git fetch origin
    git reset --hard "origin/${GIT_BRANCH:-main}"
    git pull origin "${GIT_BRANCH:-main}"
else
    log "Clonando backend por primera vez..."
    mkdir -p "$(dirname "$BACKEND_DIR")"
    git clone -b "${GIT_BRANCH:-main}" "${GIT_BACKEND_REPO}" "$BACKEND_DIR"
fi

# Frontend
if [ -d "$FRONTEND_DIR/.git" ]; then
    log "Actualizando frontend..."
    cd "$FRONTEND_DIR"
    git fetch origin
    git reset --hard "origin/${GIT_BRANCH:-main}"
    git pull origin "${GIT_BRANCH:-main}"
else
    log "Clonando frontend por primera vez..."
    mkdir -p "$(dirname "$FRONTEND_DIR")"
    git clone -b "${GIT_BRANCH:-main}" "${GIT_FRONTEND_REPO}" "$FRONTEND_DIR"
fi

# ============================================
# 4. Verificar configuración
# ============================================
log "Verificando configuración..."

cd "$BACKEND_DIR"

if [ ! -f ".env" ]; then
    error "Archivo .env no existe. Copiar desde .env.example y configurar"
fi

# Cargar variables de entorno
set -o allexport
source .env
set +o allexport

# Cargar variables de entorno
source .env

# Verificar configuración de email.properties
if [ ! -f "config/email.properties" ]; then
    warning "config/email.properties no existe. Creando desde template..."
    mkdir -p config
    cp src/main/resources/email.properties.template config/email.properties
    error "Configurar config/email.properties con credenciales reales antes de continuar"
fi

# Generar resources.xml con credenciales desde .env
log "Generando resources.xml desde variables de entorno..."
cat > src/main/resources/META-INF/resources.xml <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<resources>
    <Resource id="inacifDataSource" type="javax.sql.DataSource">
        jdbcDriver = com.microsoft.sqlserver.jdbc.SQLServerDriver
        jdbcUrl = jdbc:sqlserver://${DB_HOST}:${DB_PORT};databaseName=${DB_NAME};encrypt=true;trustServerCertificate=true;
        jtaManaged = true
        password = ${DB_PASSWORD}
        userName = ${DB_USER}
    </Resource>
</resources>
EOF

# ============================================
# 5. Detener contenedores actuales
# ============================================
log "Deteniendo contenedores actuales..."

cd "$BACKEND_DIR"
docker-compose -f docker-compose.production.yml down 2>/dev/null || true

# ============================================
# 6. Construir nuevas imágenes
# ============================================
log "Construyendo imágenes Docker..."

cd "$BACKEND_DIR"
docker-compose -f docker-compose.production.yml build --no-cache

# ============================================
# 7. Iniciar contenedores
# ============================================
log "Iniciando contenedores..."

docker-compose -f docker-compose.production.yml up -d

# ============================================
# 8. Esperar y verificar health
# ============================================
log "Esperando que los servicios estén listos..."

sleep 10

# Verificar backend
log "Verificando backend..."
for i in {1..30}; do
    if curl -f -s "http://localhost:${TOMEE_PORT:-8080}/MantenimientosBackend/api/health" > /dev/null; then
        log "✓ Backend está listo"
        break
    fi
    if [ $i -eq 30 ]; then
        error "Backend no responde después de 30 intentos"
    fi
    sleep 2
done

# Verificar frontend
log "Verificando frontend..."
for i in {1..15}; do
    if curl -f -s "http://localhost:${HTTP_PORT:-80}/" > /dev/null; then
        log "✓ Frontend está listo"
        break
    fi
    if [ $i -eq 15 ]; then
        error "Frontend no responde después de 15 intentos"
    fi
    sleep 2
done

# ============================================
# 9. Mostrar estado de contenedores
# ============================================
log "Estado de contenedores:"
docker-compose -f docker-compose.production.yml ps

# ============================================
# 10. Mostrar logs recientes
# ============================================
log "Últimas líneas de logs del backend:"
docker logs --tail 20 inacif-backend

echo ""
log "${GREEN}============================================${NC}"
log "${GREEN}Despliegue completado exitosamente!${NC}"
log "${GREEN}============================================${NC}"
log "Frontend: http://${SERVER_DOMAIN:-localhost}:${HTTP_PORT:-80}"
log "Backend:  http://${SERVER_DOMAIN:-localhost}:${TOMEE_PORT:-8080}/MantenimientosBackend"
log ""
log "Para ver logs en tiempo real:"
log "  Backend:  docker logs -f inacif-backend"
log "  Frontend: docker logs -f inacif-frontend"
log ""
log "Para detener: cd $BACKEND_DIR && docker-compose -f docker-compose.production.yml down"
log "${GREEN}============================================${NC}"
