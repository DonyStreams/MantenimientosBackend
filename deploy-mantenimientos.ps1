# Detener todos los procesos java.exe (TomEE/Tomcat)
Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force

# Compilar el proyecto
mvn clean package

# Copiar el .war al webapps de TomEE
Copy-Item -Force .\target\MantenimientosBackend.war .\Configuraciones\apache-tomee-8.0.9-plume\apache-tomee-plume-8.0.9\webapps\

# Definir CATALINA_HOME para TomEE
$TomEEHome = Resolve-Path ".\Configuraciones\apache-tomee-8.0.9-plume\apache-tomee-plume-8.0.9"
$env:CATALINA_HOME = $TomEEHome

# Iniciar TomEE
$startup = Join-Path $TomEEHome "bin\startup.bat"
& $startup

Write-Host "Despliegue y reinicio completados. Accede a http://localhost:8080/MantenimientosBackend/"