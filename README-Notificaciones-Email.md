# 📧 Configuración de Notificaciones por Correo Electrónico

## 🎯 Descripción
Sistema de notificaciones automáticas por correo electrónico para alertar sobre eventos críticos en el sistema de mantenimientos INACIF.

## 🚨 Eventos que Generan Notificaciones

### 1. Ticket con Prioridad Crítica
Cuando un ticket cambia a prioridad **"Crítica"**, se envía automáticamente un correo a:
- Administrador del sistema
- Jefatura de laboratorio

**Contenido del correo:**
- ID del ticket
- Equipo afectado y código INACIF
- Ubicación del equipo
- Usuario asignado
- Descripción del problema

### 2. Equipo en Estado Crítico
Cuando un equipo cambia a estado **"Critico"**, se envía automáticamente un correo a:
- Administrador del sistema
- Jefatura de laboratorio

**Contenido del correo:**
- ID del equipo
- Nombre y código INACIF
- Ubicación
- Estado anterior
- Motivo del cambio

## ⚙️ Configuración del Servidor SMTP

### Paso 1: Editar archivo de configuración
Edite el archivo `src/main/resources/email.properties` con los datos de su servidor de correo:

```properties
# Servidor SMTP
mail.smtp.host=smtp.ejemplo.com
mail.smtp.port=587
mail.smtp.auth=true
mail.smtp.starttls.enable=true
mail.smtp.starttls.required=true

# Credenciales de autenticación
mail.smtp.user=notificaciones@inacif.gob.gt
mail.smtp.password=CAMBIAR_PASSWORD_AQUI

# Configuración de correos
mail.from.address=notificaciones@inacif.gob.gt
mail.from.name=Sistema de Mantenimientos INACIF

# Destinatarios para notificaciones críticas
mail.admin.address=admin@inacif.gob.gt
mail.jefatura.address=jefatura@inacif.gob.gt
```

### Paso 2: Valores Comunes de Configuración

#### Gmail
```properties
mail.smtp.host=smtp.gmail.com
mail.smtp.port=587
mail.smtp.starttls.enable=true
```

#### Office 365
```properties
mail.smtp.host=smtp.office365.com
mail.smtp.port=587
mail.smtp.starttls.enable=true
```

#### Outlook.com
```properties
mail.smtp.host=smtp-mail.outlook.com
mail.smtp.port=587
mail.smtp.starttls.enable=true
```

#### Servidor SMTP Personalizado
Consulte con su proveedor de correo institucional los valores de configuración correctos.

### Paso 3: Actualizar Destinatarios
Modifique las direcciones de correo de los destinatarios según su organización:

```properties
mail.admin.address=tu.administrador@inacif.gob.gt
mail.jefatura.address=tu.jefatura@inacif.gob.gt
```

### Paso 4: Recompilar y Desplegar
Después de editar `email.properties`, recompile y despliegue el backend:

```bash
mvn clean package
```

Luego reinicie el servidor TomEE.

## 🔒 Seguridad

### Credenciales Sensibles
**⚠️ IMPORTANTE:** No suba el archivo `email.properties` con credenciales reales a repositorios públicos.

Considere usar:
- Variables de entorno
- Archivos de configuración externos no versionados
- Servicios de gestión de secretos

### Ejemplo con Variables de Entorno
Puede modificar el código para leer desde variables de entorno:

```java
String smtpHost = System.getenv("SMTP_HOST");
String smtpPassword = System.getenv("SMTP_PASSWORD");
```

## 🧪 Pruebas

### Probar Notificación de Ticket Crítico
1. Crear o editar un ticket
2. Cambiar su prioridad a **"Crítica"**
3. Verificar que se envíe el correo a los destinatarios configurados

### Probar Notificación de Equipo Crítico
1. Editar un equipo existente
2. Cambiar su estado a **"Critico"**
3. Verificar que se envíe el correo a los destinatarios configurados

## 📝 Logs
Los eventos de envío de correos se registran en los logs del servidor:

```
✅ Notificación de ticket crítico enviada - Ticket #123
📧 Correo enviado exitosamente a: admin@inacif.gob.gt
```

En caso de error:
```
⚠️ Error al enviar notificación de ticket crítico: Connection refused
❌ Error al enviar correo a admin@inacif.gob.gt
```

## 🛠️ Solución de Problemas

### Error: Connection refused
- Verifique que el host SMTP y el puerto sean correctos
- Verifique la conectividad de red

### Error: Authentication failed
- Verifique las credenciales de usuario y contraseña
- Algunos proveedores requieren "contraseñas de aplicación" específicas

### No se envían correos
1. Revise los logs del servidor TomEE
2. Verifique el archivo `email.properties` esté en `src/main/resources/`
3. Verifique que el servicio esté correctamente inyectado con CDI
4. Active el modo debug: `mail.debug=true` en `email.properties`

### Correos van a spam
- Configure registros SPF, DKIM y DMARC en su dominio
- Use una dirección de correo corporativa verificada
- Considere usar un servidor SMTP dedicado institucional

## 📋 Archivo de Configuración Completo

Plantilla completa de `email.properties`:

```properties
# ============================================
# Configuración del servidor SMTP para INACIF
# ============================================

# Servidor SMTP
mail.smtp.host=smtp.ejemplo.com
mail.smtp.port=587
mail.smtp.auth=true
mail.smtp.starttls.enable=true
mail.smtp.starttls.required=true

# Credenciales de autenticación
mail.smtp.user=notificaciones@inacif.gob.gt
mail.smtp.password=CAMBIAR_PASSWORD_AQUI

# Configuración de correos
mail.from.address=notificaciones@inacif.gob.gt
mail.from.name=Sistema de Mantenimientos INACIF

# Destinatarios para notificaciones críticas
mail.admin.address=admin@inacif.gob.gt
mail.jefatura.address=jefatura@inacif.gob.gt

# Configuración de timeout (en milisegundos)
mail.smtp.timeout=5000
mail.smtp.connectiontimeout=5000

# Habilitar debug de JavaMail (solo para desarrollo)
mail.debug=false
```

## 🚀 Despliegue en Producción

### Recomendaciones
1. **Use un servidor SMTP dedicado** para envío de correos transaccionales
2. **Configure límites de envío** si su proveedor los tiene
3. **Monitoree los logs** para detectar problemas de entrega
4. **Mantenga actualizadas** las direcciones de los destinatarios
5. **Pruebe regularmente** el sistema de notificaciones

### Integración con Servicios de Correo Profesionales
Considere usar servicios como:
- **SendGrid**
- **Amazon SES**
- **Mailgun**
- **Servidor SMTP institucional**

Estos servicios suelen tener mejor deliverabilidad y métricas de seguimiento.

## 📞 Soporte
Para problemas relacionados con la configuración de correos, contacte al equipo de desarrollo o al administrador del sistema.
