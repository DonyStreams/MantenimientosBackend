# 🚀 Inicio Rápido - Sistema de Notificaciones por Correo

## ⚡ Configuración en 5 Pasos

### Paso 1: Copiar archivo de configuración
```bash
cd src/main/resources/
cp email.properties.template email.properties
```

### Paso 2: Editar credenciales
Abra `src/main/resources/email.properties` y actualice:

```properties
# Servidor SMTP (ejemplo con Gmail)
mail.smtp.host=smtp.gmail.com
mail.smtp.port=587

# Sus credenciales
mail.smtp.user=tu-correo@inacif.gob.gt
mail.smtp.password=tu-password-aqui

# Destinatarios
mail.admin.address=admin@inacif.gob.gt
mail.jefatura.address=jefatura@inacif.gob.gt
```

### Paso 3: Recompilar proyecto
```bash
mvn clean package
```

### Paso 4: Desplegar en TomEE
```bash
# Opción 1: Con Docker
docker cp target/MantenimientosBackend.war tomee-server:/usr/local/tomee/webapps/
docker restart tomee-server

# Opción 2: Manualmente
# Copiar MantenimientosBackend.war a la carpeta webapps de TomEE
```

### Paso 5: Probar funcionalidad
1. Acceda al sistema de mantenimientos
2. Edite un ticket y cambie prioridad a **"Crítica"**
3. Verifique que llegue el correo

---

## 🧪 Pruebas Rápidas

### Probar Ticket Crítico
```bash
# Desde Postman o curl
curl -X PUT http://localhost:8081/MantenimientosBackend/api/tickets/1 \
  -H "Content-Type: application/json" \
  -d '{"prioridad": "Crítica"}'
```

### Probar Equipo Crítico
```bash
# Desde Postman o curl
curl -X PUT http://localhost:8081/MantenimientosBackend/api/equipos/1 \
  -H "Content-Type: application/json" \
  -d '{"estado": "Critico"}'
```

### Verificar Logs
```bash
docker logs tomee-server --tail 50 | grep -i "notificación\|correo"
```

---

## 🔍 Verificar Configuración

### Test 1: Archivo de configuración existe
```bash
ls -la src/main/resources/email.properties
```
✅ Debe existir el archivo (no el .template)

### Test 2: Dependencias correctas
```bash
mvn dependency:tree | grep javax.mail
```
✅ Debe mostrar: `com.sun.mail:javax.mail:jar:1.6.2`

### Test 3: Servicio compilado
```bash
ls -la target/MantenimientosBackend/WEB-INF/classes/usac/eps/servicios/mantenimientos/EmailService.class
```
✅ Debe existir el archivo .class

---

## 🐛 Solución de Problemas Comunes

### Problema 1: No se envían correos
**Síntoma:** La aplicación funciona pero no llegan correos

**Solución:**
```bash
# 1. Verificar archivo de configuración
cat src/main/resources/email.properties

# 2. Activar debug
# En email.properties:
mail.debug=true

# 3. Revisar logs
docker logs tomee-server --tail 100
```

### Problema 2: Authentication failed
**Síntoma:** Error "535 Authentication failed"

**Solución:**
- Para Gmail: Use contraseña de aplicación (https://myaccount.google.com/apppasswords)
- Para Office 365: Verifique que la cuenta tenga SMTP habilitado
- Verifique usuario y password correctos

### Problema 3: Connection refused
**Síntoma:** Error "Connection refused" o "Connection timed out"

**Solución:**
```properties
# Verificar host y puerto
mail.smtp.host=smtp.gmail.com  # Correcto
mail.smtp.port=587              # Correcto para STARTTLS
# O use 465 para SSL directo
```

### Problema 4: Correos van a spam
**Síntoma:** Los correos llegan pero a carpeta de spam

**Solución:**
1. Use un servidor SMTP institucional verificado
2. Configure SPF y DKIM en su dominio
3. Use dirección corporativa como remitente
4. Agregue la dirección a la lista blanca

---

## 📋 Checklist de Verificación

Antes de marcar como completo, verifique:

- [ ] Archivo `email.properties` creado y configurado
- [ ] Credenciales SMTP correctas
- [ ] Destinatarios actualizados
- [ ] Proyecto compilado sin errores
- [ ] Backend desplegado en TomEE
- [ ] Prueba de ticket crítico exitosa
- [ ] Prueba de equipo crítico exitosa
- [ ] Correos recibidos correctamente
- [ ] Logs muestran envíos exitosos

---

## 📞 Soporte Rápido

### Ver logs en tiempo real
```bash
docker logs -f tomee-server
```

### Reiniciar servidor
```bash
docker restart tomee-server
```

### Verificar que el servicio esté activo
```bash
curl http://localhost:8081/MantenimientosBackend/api/tickets
```

---

## 💡 Tips

1. **Use contraseñas de aplicación** específicas, no su contraseña personal
2. **Pruebe primero con Gmail** (es más fácil de configurar)
3. **Active mail.debug=true** durante las pruebas
4. **Monitoree los logs** para detectar problemas temprano
5. **Configure alertas** para cuando fallen los envíos
6. **Documente sus credenciales** en lugar seguro (no en Git)

---

## ✅ Listo para Producción

Una vez que las pruebas sean exitosas:

1. Desactive debug: `mail.debug=false`
2. Use servidor SMTP institucional
3. Configure backups del archivo de configuración
4. Documente procedimientos de emergencia
5. Capacite al equipo de soporte

---

**¡Todo listo! Su sistema de notificaciones está configurado y funcionando.** 🎉
