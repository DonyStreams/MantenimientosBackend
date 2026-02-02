#!/bin/bash
# ============================================
# Script de Instalación de Docker y Docker Compose
# Para Ubuntu/Debian en Servidor INACIF
# ============================================

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${GREEN}============================================${NC}"
echo -e "${GREEN}Instalando Docker y Docker Compose${NC}"
echo -e "${GREEN}Servidor: 172.16.33.11${NC}"
echo -e "${GREEN}============================================${NC}\n"

# Verificar si se ejecuta como root
if [ "$EUID" -ne 0 ]; then 
    echo -e "${RED}Este script debe ejecutarse como root o con sudo${NC}"
    exit 1
fi

# Actualizar sistema
echo -e "${YELLOW}[1/6] Actualizando sistema...${NC}"
apt update && apt upgrade -y

# Instalar dependencias
echo -e "${YELLOW}[2/6] Instalando dependencias...${NC}"
apt install -y \
    ca-certificates \
    curl \
    gnupg \
    lsb-release \
    git \
    vim

# Agregar clave GPG de Docker
echo -e "${YELLOW}[3/6] Agregando repositorio de Docker...${NC}"
mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg

# Agregar repositorio Docker
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null

# Instalar Docker
echo -e "${YELLOW}[4/6] Instalando Docker...${NC}"
apt update
apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# Iniciar Docker
systemctl start docker
systemctl enable docker

# Agregar usuario actual al grupo docker
echo -e "${YELLOW}[5/6] Configurando permisos de Docker...${NC}"
if [ -n "$SUDO_USER" ]; then
    usermod -aG docker $SUDO_USER
    echo -e "${GREEN}Usuario $SUDO_USER agregado al grupo docker${NC}"
else
    echo -e "${YELLOW}Ejecutar manualmente: sudo usermod -aG docker \$USER${NC}"
fi

# Instalar Docker Compose (standalone)
echo -e "${YELLOW}[6/6] Instalando Docker Compose...${NC}"
DOCKER_COMPOSE_VERSION="v2.24.5"
curl -L "https://github.com/docker/compose/releases/download/${DOCKER_COMPOSE_VERSION}/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose
ln -sf /usr/local/bin/docker-compose /usr/bin/docker-compose

# Verificar instalación
echo -e "\n${GREEN}============================================${NC}"
echo -e "${GREEN}Verificando instalación...${NC}"
echo -e "${GREEN}============================================${NC}\n"

docker --version
docker-compose --version

# Probar Docker
echo -e "\n${YELLOW}Probando Docker...${NC}"
docker run --rm hello-world

echo -e "\n${GREEN}============================================${NC}"
echo -e "${GREEN}✓ Instalación completada exitosamente!${NC}"
echo -e "${GREEN}============================================${NC}\n"

echo -e "${YELLOW}IMPORTANTE:${NC}"
echo -e "1. Cerrar sesión y volver a iniciar sesión para que los permisos de Docker surtan efecto"
echo -e "2. Luego ejecutar: ${GREEN}docker ps${NC} para verificar que funciona sin sudo\n"

echo -e "${GREEN}Siguiente paso:${NC}"
echo -e "1. Cerrar sesión: ${YELLOW}exit${NC}"
echo -e "2. Volver a conectar por SSH"
echo -e "3. Clonar repositorios y ejecutar deploy.sh\n"
