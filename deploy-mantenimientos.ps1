# Detener contenedores existentes
docker stop tomee-server keycloak-server 2>$null
docker rm tomee-server keycloak-server 2>$null

Write-Host "Iniciando Keycloak..."
# Iniciar Keycloak primero
cd Configuraciones
docker-compose -f docker-compose-keycloak.yml up -d
cd ..

Write-Host "Esperando a que Keycloak se inicie completamente..."
Start-Sleep -Seconds 30

# Compilar el proyecto
mvn clean package -q

# Construir y ejecutar el contenedor de la aplicación en puerto 8081
$env:TOMEE_PORT = "8081"
docker-compose up --build -d

Write-Host "Despliegue completado!"
Write-Host "Keycloak Admin: http://localhost:8080/admin (admin/admin)"
Write-Host "Aplicación: http://localhost:8081/MantenimientosBackend/"
Write-Host "Frontend: http://localhost:4200 (ejecutar 'ng serve' en EPS-FRONTEND-USAC)"
Write-Host ""
Write-Host "Usuarios de prueba:"
Write-Host "- admin / admin123"
Write-Host "- supervisor / supervisor123"
Write-Host "- tecnico / tecnico123"
Write-Host ""
Write-Host "Esperando a que el servidor se inicie completamente..."
Start-Sleep -Seconds 10
Write-Host "Verificando logs de despliegue..."
docker logs tomee-server --tail 20