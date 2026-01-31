# 🧪 Prueba del Sistema de Notificaciones - Configurado

## ✅ Configuración Completada

El sistema está configurado con las credenciales de INACIF:

- **Servidor SMTP:** mail.inacif.gob.gt
- **Puerto:** 587 (STARTTLS)
- **Usuario:** mdapruebas
- **Correo remitente:** mdapruebas@inacif.gob.gt
- **Destinatarios:** mdapruebas@inacif.gob.gt

---

## 🚀 Estado del Despliegue

✅ Backend compilado exitosamente  
✅ Desplegado en TomEE  
✅ Configuración SMTP aplicada  
✅ Servicio EmailService disponible  

---

## 🧪 Pruebas Recomendadas

### Prueba 1: Ticket con Prioridad Crítica

#### Desde el Frontend:
1. Acceder a http://localhost:4200 (o tu URL del frontend)
2. Ir al módulo de **Tickets**
3. Crear un nuevo ticket o editar uno existente
4. Cambiar la **prioridad** a **"Crítica"**
5. Guardar cambios
6. **Verificar:** Revisar el correo mdapruebas@inacif.gob.gt

#### Desde Postman/curl:
```bash
curl -X PUT http://localhost:8081/MantenimientosBackend/api/tickets/1 \
  -H "Content-Type: application/json" \
  -d '{
    "prioridad": "Crítica"
  }'
```

**Resultado esperado:**
- ✅ Ticket actualizado correctamente
- ✅ Correo enviado a mdapruebas@inacif.gob.gt
- ✅ Asunto: "🚨 TICKET CRÍTICO #X - [Nombre del Equipo]"
- ✅ Contenido HTML con detalles del ticket

---

### Prueba 2: Equipo en Estado Crítico

#### Desde el Frontend:
1. Acceder a http://localhost:4200
2. Ir al módulo de **Equipos**
3. Editar un equipo existente
4. Cambiar el **estado** a **"Critico"**
5. Guardar cambios
6. **Verificar:** Revisar el correo mdapruebas@inacif.gob.gt

#### Desde Postman/curl:
```bash
curl -X PUT http://localhost:8081/MantenimientosBackend/api/equipos/1 \
  -H "Content-Type: application/json" \
  -d '{
    "estado": "Critico",
    "nombre": "Equipo de Prueba",
    "codigoInacif": "TEST-001",
    "ubicacion": "Laboratorio Central"
  }'
```

**Resultado esperado:**
- ✅ Equipo actualizado correctamente
- ✅ Correo enviado a mdapruebas@inacif.gob.gt
- ✅ Asunto: "⚠️ EQUIPO EN ESTADO CRÍTICO - [Nombre] ([Código])"
- ✅ Contenido HTML con detalles del equipo

---

## 📧 Verificar Correos

### Revisar Buzón
1. Acceder a mdapruebas@inacif.gob.gt
2. Buscar correos del remitente: **mdapruebas@inacif.gob.gt**
3. Verificar que lleguen los correos con formato HTML

### Posibles Ubicaciones
- ✅ **Bandeja de entrada**
- ⚠️ **Spam/Correo no deseado** (primera vez puede ir aquí)
- ⚠️ **Promociones** (si usa filtros inteligentes)

**Tip:** Si va a spam, marcarlo como "No es spam" para futuros correos.

---

## 🔍 Verificar Logs del Servidor

### Ver logs en tiempo real:
```bash
docker logs -f tomee-server
```

### Buscar mensajes de correo:
```bash
docker logs tomee-server | grep -i "notificación\|correo\|email"
```

### Mensajes esperados:

#### ✅ Éxito:
```
✅ Configuración de correo cargada exitosamente
📧 Correo enviado exitosamente a: mdapruebas@inacif.gob.gt
✅ Notificación de ticket crítico enviada - Ticket #123
✅ Notificación de equipo crítico enviada - Equipo #45
```

#### ❌ Error:
```
⚠️ Error al enviar notificación de ticket crítico: Connection refused
❌ Error al enviar correo a mdapruebas@inacif.gob.gt
```

---

## 🛠️ Solución de Problemas

### Problema 1: No llegan correos

**Pasos a seguir:**

1. **Verificar archivo de configuración:**
   ```bash
   docker exec tomee-server cat /usr/local/tomee/webapps/MantenimientosBackend/WEB-INF/classes/email.properties
   ```

2. **Activar modo debug:**
   - Editar `email.properties`
   - Cambiar: `mail.debug=true`
   - Recompilar y redesplegar

3. **Ver logs detallados:**
   ```bash
   docker logs tomee-server --tail 100
   ```

4. **Probar conexión SMTP:**
   ```bash
   telnet mail.inacif.gob.gt 587
   ```

### Problema 2: Authentication failed

**Posibles causas:**
- Password incorrecto
- Usuario bloqueado temporalmente
- Servidor SMTP requiere configuración adicional

**Verificar:**
```properties
mail.smtp.user=mdapruebas
mail.smtp.password=$Mdapruebas#2701
```

### Problema 3: Correos van a spam

**Soluciones:**
1. Marcar como "No es spam" en el cliente de correo
2. Agregar mdapruebas@inacif.gob.gt a contactos
3. Crear regla de filtro para correos del sistema

---

## 📊 Lista de Verificación

### Antes de Pruebas
- [x] Backend compilado sin errores
- [x] Desplegado en TomEE
- [x] Archivo email.properties configurado
- [x] Credenciales SMTP correctas
- [ ] Acceso al buzón mdapruebas@inacif.gob.gt

### Durante Pruebas
- [ ] Cambiar ticket a prioridad "Crítica"
- [ ] Cambiar equipo a estado "Critico"
- [ ] Verificar logs del servidor
- [ ] Revisar buzón de correo
- [ ] Verificar formato HTML de correos

### Después de Pruebas
- [ ] Correos recibidos correctamente
- [ ] Formato profesional y legible
- [ ] Información completa en correos
- [ ] Sin errores en logs del servidor
- [ ] Documentar cualquier problema encontrado

---

## 🎯 Criterios de Éxito

La prueba es **exitosa** si:

✅ Los correos se envían automáticamente  
✅ Llegan al buzón de mdapruebas@inacif.gob.gt  
✅ Tienen formato HTML profesional  
✅ Contienen toda la información requerida  
✅ Los logs no muestran errores  
✅ El sistema continúa funcionando normal  

---

## 📝 Registro de Pruebas

### Prueba de Ticket Crítico
- **Fecha:** __________
- **Ticket ID:** __________
- **Correo enviado:** ☐ Sí  ☐ No
- **Correo recibido:** ☐ Sí  ☐ No
- **Ubicación:** ☐ Entrada  ☐ Spam  ☐ Otros
- **Observaciones:** _______________________

### Prueba de Equipo Crítico
- **Fecha:** __________
- **Equipo ID:** __________
- **Correo enviado:** ☐ Sí  ☐ No
- **Correo recibido:** ☐ Sí  ☐ No
- **Ubicación:** ☐ Entrada  ☐ Spam  ☐ Otros
- **Observaciones:** _______________________

---

## 🔄 Próximos Pasos

### Si las pruebas son exitosas:
1. ✅ Actualizar destinatarios reales en `email.properties`
2. ✅ Desactivar modo debug: `mail.debug=false`
3. ✅ Documentar procedimientos
4. ✅ Capacitar usuarios finales
5. ✅ Monitorear en producción

### Si hay problemas:
1. ⚠️ Revisar logs detalladamente
2. ⚠️ Verificar conectividad de red
3. ⚠️ Contactar al administrador de correo
4. ⚠️ Revisar firewall y puertos
5. ⚠️ Consultar documentación técnica

---

## 📞 Soporte

Para problemas durante las pruebas:
1. Revisar [README-Notificaciones-Email.md](README-Notificaciones-Email.md)
2. Consultar logs del servidor
3. Verificar configuración SMTP
4. Contactar al equipo de desarrollo

---

**Estado:** ✅ **LISTO PARA PRUEBAS**

El sistema está completamente configurado y desplegado. 
Solo falta realizar las pruebas y verificar la recepción de correos.

**Fecha de configuración:** 30 de enero de 2026  
**Servidor:** mail.inacif.gob.gt  
**Cuenta de prueba:** mdapruebas@inacif.gob.gt
