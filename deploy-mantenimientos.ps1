# Detener todos los procesos java.exe (TomEE/Tomcat)
Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force

# Esperar a que los procesos terminen
Start-Sleep -Seconds 3

# Limpiar carpeta y WAR anterior en TomEE
$warPath = ".\Configuraciones\apache-tomee-8.0.9-plume\apache-tomee-plume-8.0.9\webapps\MantenimientosBackend.war"
$dirPath = ".\Configuraciones\apache-tomee-8.0.9-plume\apache-tomee-plume-8.0.9\webapps\MantenimientosBackend"
if (Test-Path $warPath) { Remove-Item -Force $warPath }
if (Test-Path $dirPath) { Remove-Item -Recurse -Force $dirPath }

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