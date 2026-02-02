# ============================================
# Script de Despliegue Automatizado (PowerShell)
# Sistema de Mantenimientos INACIF
# ============================================

$ErrorActionPreference = "Stop"

# Variables
$BACKEND_DIR = "C:\inacif\MantenimientosBackend"
$FRONTEND_DIR = "C:\inacif\EPS-FRONTEND"
$BACKUP_DIR = "C:\inacif\backups"
$LOG_FILE = "C:\inacif\logs\deploy.log"
$TIMESTAMP = Get-Date -Format "yyyyMMdd_HHmmss"

# Función para logging
function Write-Log {
    param([string]$Message, [string]$Level = "INFO")
    
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $logMessage = "[$timestamp] [$Level] $Message"
    
    switch ($Level) {
        "ERROR" { Write-Host $logMessage -ForegroundColor Red }
        "WARNING" { Write-Host $logMessage -ForegroundColor Yellow }
        "SUCCESS" { Write-Host $logMessage -ForegroundColor Green }
        default { Write-Host $logMessage -ForegroundColor Cyan }
    }
    
    Add-Content -Path $LOG_FILE -Value $logMessage
}

# ============================================
# 1. Verificar prerrequisitos
# ============================================
Write-Log "Verificando prerrequisitos..."

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Log "Docker no está instalado" "ERROR"
    exit 1
}

if (-not (Get-Command docker-compose -ErrorAction SilentlyContinue)) {
    Write-Log "Docker Compose no está instalado" "ERROR"
    exit 1
}

if (-not (Get-Command git -ErrorAction SilentlyContinue)) {
    Write-Log "Git no está instalado" "ERROR"
    exit 1
}

# ============================================
# 2. Crear backup de la versión actual
# ============================================
Write-Log "Creando backup..."

New-Item -ItemType Directory -Force -Path $BACKUP_DIR | Out-Null

if (Test-Path $BACKEND_DIR) {
    Compress-Archive -Path "$BACKEND_DIR\*" -DestinationPath "$BACKUP_DIR\backend_$TIMESTAMP.zip" -Force
    Write-Log "Backup backend creado: backend_$TIMESTAMP.zip" "SUCCESS"
}

if (Test-Path $FRONTEND_DIR) {
    Compress-Archive -Path "$FRONTEND_DIR\*" -DestinationPath "$BACKUP_DIR\frontend_$TIMESTAMP.zip" -Force
    Write-Log "Backup frontend creado: frontend_$TIMESTAMP.zip" "SUCCESS"
}

# Limpiar backups antiguos (mantener últimos 5)
Get-ChildItem -Path $BACKUP_DIR -Filter "*.zip" | 
    Sort-Object LastWriteTime -Descending | 
    Select-Object -Skip 5 | 
    Remove-Item -Force

# ============================================
# 3. Actualizar código desde Git
# ============================================
Write-Log "Actualizando código desde repositorios..."

# Cargar variables de entorno
if (Test-Path "$BACKEND_DIR\.env") {
    Get-Content "$BACKEND_DIR\.env" | ForEach-Object {
        if ($_ -match '^([^=]+)=(.*)$') {
            [Environment]::SetEnvironmentVariable($matches[1], $matches[2], "Process")
        }
    }
}

$GIT_BRANCH = $env:GIT_BRANCH
if (-not $GIT_BRANCH) { $GIT_BRANCH = "main" }

# Backend
if (Test-Path "$BACKEND_DIR\.git") {
    Write-Log "Actualizando backend..."
    Set-Location $BACKEND_DIR
    git fetch origin
    git reset --hard "origin/$GIT_BRANCH"
    git pull origin $GIT_BRANCH
} else {
    Write-Log "Clonando backend por primera vez..."
    New-Item -ItemType Directory -Force -Path (Split-Path $BACKEND_DIR) | Out-Null
    git clone -b $GIT_BRANCH $env:GIT_BACKEND_REPO $BACKEND_DIR
}

# Frontend
if (Test-Path "$FRONTEND_DIR\.git") {
    Write-Log "Actualizando frontend..."
    Set-Location $FRONTEND_DIR
    git fetch origin
    git reset --hard "origin/$GIT_BRANCH"
    git pull origin $GIT_BRANCH
} else {
    Write-Log "Clonando frontend por primera vez..."
    New-Item -ItemType Directory -Force -Path (Split-Path $FRONTEND_DIR) | Out-Null
    git clone -b $GIT_BRANCH $env:GIT_FRONTEND_REPO $FRONTEND_DIR
}

# ============================================
# 4. Verificar configuración
# ============================================
Write-Log "Verificando configuración..."

Set-Location $BACKEND_DIR

if (-not (Test-Path ".env")) {
    Write-Log "Archivo .env no existe. Copiar desde .env.example y configurar" "ERROR"
    exit 1
}

if (-not (Test-Path "config\email.properties")) {
    Write-Log "config\email.properties no existe. Creando desde template..." "WARNING"
    New-Item -ItemType Directory -Force -Path "config" | Out-Null
    Copy-Item "src\main\resources\email.properties.template" "config\email.properties"
    Write-Log "Configurar config\email.properties con credenciales reales antes de continuar" "ERROR"
    exit 1
}

# ============================================
# 5. Detener contenedores actuales
# ============================================
Write-Log "Deteniendo contenedores actuales..."

Set-Location $BACKEND_DIR
docker-compose -f docker-compose.production.yml down 2>$null

# ============================================
# 6. Construir nuevas imágenes
# ============================================
Write-Log "Construyendo imágenes Docker..."

docker-compose -f docker-compose.production.yml build --no-cache

# ============================================
# 7. Iniciar contenedores
# ============================================
Write-Log "Iniciando contenedores..."

docker-compose -f docker-compose.production.yml up -d

# ============================================
# 8. Esperar y verificar health
# ============================================
Write-Log "Esperando que los servicios estén listos..."

Start-Sleep -Seconds 10

$TOMEE_PORT = $env:TOMEE_PORT
if (-not $TOMEE_PORT) { $TOMEE_PORT = "8080" }

$HTTP_PORT = $env:HTTP_PORT
if (-not $HTTP_PORT) { $HTTP_PORT = "80" }

# Verificar backend
Write-Log "Verificando backend..."
$backendReady = $false
for ($i = 1; $i -le 30; $i++) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:$TOMEE_PORT/MantenimientosBackend/api/health" -UseBasicParsing -TimeoutSec 2
        if ($response.StatusCode -eq 200) {
            Write-Log "✓ Backend está listo" "SUCCESS"
            $backendReady = $true
            break
        }
    } catch {
        Start-Sleep -Seconds 2
    }
}

if (-not $backendReady) {
    Write-Log "Backend no responde después de 30 intentos" "ERROR"
    exit 1
}

# Verificar frontend
Write-Log "Verificando frontend..."
$frontendReady = $false
for ($i = 1; $i -le 15; $i++) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:$HTTP_PORT/" -UseBasicParsing -TimeoutSec 2
        if ($response.StatusCode -eq 200) {
            Write-Log "✓ Frontend está listo" "SUCCESS"
            $frontendReady = $true
            break
        }
    } catch {
        Start-Sleep -Seconds 2
    }
}

if (-not $frontendReady) {
    Write-Log "Frontend no responde después de 15 intentos" "ERROR"
    exit 1
}

# ============================================
# 9. Mostrar estado de contenedores
# ============================================
Write-Log "Estado de contenedores:"
docker-compose -f docker-compose.production.yml ps

# ============================================
# 10. Mostrar logs recientes
# ============================================
Write-Log "Últimas líneas de logs del backend:"
docker logs --tail 20 inacif-backend

Write-Host "`n============================================" -ForegroundColor Green
Write-Host "Despliegue completado exitosamente!" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Green
Write-Host "Frontend: http://$($env:SERVER_DOMAIN):$HTTP_PORT"
Write-Host "Backend:  http://$($env:SERVER_DOMAIN):$TOMEE_PORT/MantenimientosBackend"
Write-Host "`nPara ver logs en tiempo real:"
Write-Host "  Backend:  docker logs -f inacif-backend"
Write-Host "  Frontend: docker logs -f inacif-frontend"
Write-Host "`nPara detener: docker-compose -f docker-compose.production.yml down"
Write-Host "============================================`n" -ForegroundColor Green
