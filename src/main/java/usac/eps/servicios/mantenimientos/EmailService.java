package usac.eps.servicios.mantenimientos;

import javax.enterprise.context.ApplicationScoped;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servicio para envío de notificaciones por correo electrónico
 * Gestiona alertas críticas de equipos y tickets
 */
@ApplicationScoped
public class EmailService {
    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());

    private Properties emailProperties;

    public EmailService() {
        loadEmailProperties();
    }

    /**
     * Carga configuración del servidor SMTP desde email.properties
     */
    private void loadEmailProperties() {
        emailProperties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("email.properties")) {
            if (input != null) {
                emailProperties.load(input);
                LOGGER.info("✅ Configuración de correo cargada exitosamente");
            } else {
                LOGGER.warning("⚠️ No se encontró archivo email.properties, usando valores por defecto");
                setDefaultProperties();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "❌ Error al cargar email.properties", e);
            setDefaultProperties();
        }
    }

    /**
     * Establece valores por defecto de configuración
     */
    private void setDefaultProperties() {
        emailProperties.setProperty("mail.smtp.host", "smtp.ejemplo.com");
        emailProperties.setProperty("mail.smtp.port", "587");
        emailProperties.setProperty("mail.smtp.auth", "true");
        emailProperties.setProperty("mail.smtp.starttls.enable", "true");
        emailProperties.setProperty("mail.from.address", "notificaciones@inacif.gob.gt");
        emailProperties.setProperty("mail.from.name", "Sistema de Mantenimientos INACIF");
    }

    /**
     * Envía notificación cuando un ticket cambia a prioridad crítica
     */
    public void notificarTicketCritico(Integer ticketId, String descripcion,
            String equipoNombre, String codigoInacif,
            String usuarioAsignado, String ubicacion) {
        try {
            String asunto = "🚨 TICKET CRÍTICO #" + ticketId + " - " + equipoNombre;

            String contenido = generarHtmlTicketCritico(ticketId, descripcion, equipoNombre,
                    codigoInacif, usuarioAsignado, ubicacion);

            // Enviar a administrador y jefatura
            String adminEmail = emailProperties.getProperty("mail.admin.address", "admin@inacif.gob.gt");
            String jefaturaEmail = emailProperties.getProperty("mail.jefatura.address", "jefatura@inacif.gob.gt");

            // Enviar al administrador
            enviarCorreo(adminEmail, asunto, contenido);

            // Solo enviar a jefatura si es diferente al admin (evitar duplicados)
            if (!adminEmail.equalsIgnoreCase(jefaturaEmail)) {
                enviarCorreo(jefaturaEmail, asunto, contenido);
                LOGGER.info("📧 Correo enviado a 2 destinatarios (admin y jefatura)");
            } else {
                LOGGER.info("📧 Correo enviado a 1 destinatario (admin = jefatura, evitando duplicado)");
            }

            LOGGER.info("✅ Notificación de ticket crítico enviada - Ticket #" + ticketId);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ Error al enviar notificación de ticket crítico #" + ticketId, e);
        }
    }

    /**
     * Envía notificación cuando un equipo cambia a estado crítico
     */
    public void notificarEquipoCritico(Integer equipoId, String equipoNombre,
            String codigoInacif, String ubicacion,
            String estadoAnterior, String motivoCambio) {
        try {
            String asunto = "⚠️ EQUIPO EN ESTADO CRÍTICO - " + equipoNombre + " (" + codigoInacif + ")";

            String contenido = generarHtmlEquipoCritico(equipoId, equipoNombre, codigoInacif,
                    ubicacion, estadoAnterior, motivoCambio);

            // Enviar a administrador y jefatura
            String adminEmail = emailProperties.getProperty("mail.admin.address", "admin@inacif.gob.gt");
            String jefaturaEmail = emailProperties.getProperty("mail.jefatura.address", "jefatura@inacif.gob.gt");

            // Enviar al administrador
            enviarCorreo(adminEmail, asunto, contenido);

            // Solo enviar a jefatura si es diferente al admin (evitar duplicados)
            if (!adminEmail.equalsIgnoreCase(jefaturaEmail)) {
                enviarCorreo(jefaturaEmail, asunto, contenido);
                LOGGER.info("📧 Correo enviado a 2 destinatarios (admin y jefatura)");
            } else {
                LOGGER.info("📧 Correo enviado a 1 destinatario (admin = jefatura, evitando duplicado)");
            }

            LOGGER.info("✅ Notificación de equipo crítico enviada - Equipo #" + equipoId);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ Error al enviar notificación de equipo crítico #" + equipoId, e);
        }
    }

    /**
     * Método genérico para enviar correo electrónico
     */
    private void enviarCorreo(String destinatario, String asunto, String contenidoHtml) {
        try {
            // Configurar propiedades del servidor SMTP
            Properties props = new Properties();
            props.put("mail.smtp.auth", emailProperties.getProperty("mail.smtp.auth", "true"));
            props.put("mail.smtp.starttls.enable", emailProperties.getProperty("mail.smtp.starttls.enable", "true"));
            props.put("mail.smtp.host", emailProperties.getProperty("mail.smtp.host"));
            props.put("mail.smtp.port", emailProperties.getProperty("mail.smtp.port", "587"));
            props.put("mail.smtp.timeout", emailProperties.getProperty("mail.smtp.timeout", "5000"));
            props.put("mail.smtp.connectiontimeout",
                    emailProperties.getProperty("mail.smtp.connectiontimeout", "5000"));
            props.put("mail.debug", emailProperties.getProperty("mail.debug", "false"));

            final String username = emailProperties.getProperty("mail.smtp.user");
            final String password = emailProperties.getProperty("mail.smtp.password");

            // Crear sesión con autenticación
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            // Crear mensaje
            Message message = new MimeMessage(session);

            String fromAddress = emailProperties.getProperty("mail.from.address", "notificaciones@inacif.gob.gt");
            String fromName = emailProperties.getProperty("mail.from.name", "Sistema de Mantenimientos INACIF");

            message.setFrom(new InternetAddress(fromAddress, fromName));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            message.setSubject(asunto);
            message.setContent(contenidoHtml, "text/html; charset=utf-8");

            // Enviar mensaje
            Transport.send(message);

            LOGGER.info("📧 Correo enviado exitosamente a: " + destinatario);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ Error al enviar correo a " + destinatario, e);
            throw new RuntimeException("Error al enviar correo", e);
        }
    }

    /**
     * Genera contenido HTML para notificación de ticket crítico
     */
    private String generarHtmlTicketCritico(Integer ticketId, String descripcion,
            String equipoNombre, String codigoInacif,
            String usuarioAsignado, String ubicacion) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                "        .header { background: #dc3545; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }"
                +
                "        .content { background: #f9f9f9; padding: 20px; border: 1px solid #ddd; }" +
                "        .info-row { padding: 10px 0; border-bottom: 1px solid #ddd; }" +
                "        .label { font-weight: bold; color: #555; }" +
                "        .value { color: #333; }" +
                "        .footer { background: #f1f1f1; padding: 15px; text-align: center; font-size: 12px; color: #666; }"
                +
                "        .critical { color: #dc3545; font-weight: bold; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <div class='header'>" +
                "            <h2>🚨 ALERTA DE TICKET CRÍTICO</h2>" +
                "        </div>" +
                "        <div class='content'>" +
                "            <p>Se ha registrado un ticket con <span class='critical'>PRIORIDAD CRÍTICA</span> que requiere atención inmediata:</p>"
                +
                "            <div class='info-row'>" +
                "                <span class='label'>Ticket ID:</span> <span class='value'>#" + ticketId + "</span>" +
                "            </div>" +
                "            <div class='info-row'>" +
                "                <span class='label'>Equipo:</span> <span class='value'>" + equipoNombre + "</span>" +
                "            </div>" +
                "            <div class='info-row'>" +
                "                <span class='label'>Código INACIF:</span> <span class='value'>" + codigoInacif
                + "</span>" +
                "            </div>" +
                "            <div class='info-row'>" +
                "                <span class='label'>Ubicación:</span> <span class='value'>" + ubicacion + "</span>" +
                "            </div>" +
                "            <div class='info-row'>" +
                "                <span class='label'>Usuario Asignado:</span> <span class='value'>" + usuarioAsignado
                + "</span>" +
                "            </div>" +
                "            <div class='info-row'>" +
                "                <span class='label'>Descripción:</span><br>" +
                "                <span class='value'>" + descripcion + "</span>" +
                "            </div>" +
                "        </div>" +
                "        <div class='footer'>" +
                "            <p>Este es un mensaje automático del Sistema de Mantenimientos INACIF<br>" +
                "            Por favor, no responda a este correo.</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }

    /**
     * Genera contenido HTML para notificación de equipo crítico
     */
    private String generarHtmlEquipoCritico(Integer equipoId, String equipoNombre,
            String codigoInacif, String ubicacion,
            String estadoAnterior, String motivoCambio) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                "        .header { background: #ff9800; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }"
                +
                "        .content { background: #f9f9f9; padding: 20px; border: 1px solid #ddd; }" +
                "        .info-row { padding: 10px 0; border-bottom: 1px solid #ddd; }" +
                "        .label { font-weight: bold; color: #555; }" +
                "        .value { color: #333; }" +
                "        .footer { background: #f1f1f1; padding: 15px; text-align: center; font-size: 12px; color: #666; }"
                +
                "        .critical { color: #ff9800; font-weight: bold; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <div class='header'>" +
                "            <h2>⚠️ EQUIPO EN ESTADO CRÍTICO</h2>" +
                "        </div>" +
                "        <div class='content'>" +
                "            <p>Un equipo ha cambiado a <span class='critical'>ESTADO CRÍTICO</span> y requiere revisión urgente:</p>"
                +
                "            <div class='info-row'>" +
                "                <span class='label'>Equipo ID:</span> <span class='value'>#" + equipoId + "</span>" +
                "            </div>" +
                "            <div class='info-row'>" +
                "                <span class='label'>Nombre:</span> <span class='value'>" + equipoNombre + "</span>" +
                "            </div>" +
                "            <div class='info-row'>" +
                "                <span class='label'>Código INACIF:</span> <span class='value'>" + codigoInacif
                + "</span>" +
                "            </div>" +
                "            <div class='info-row'>" +
                "                <span class='label'>Ubicación:</span> <span class='value'>" + ubicacion + "</span>" +
                "            </div>" +
                "            <div class='info-row'>" +
                "                <span class='label'>Estado Anterior:</span> <span class='value'>" + estadoAnterior
                + "</span>" +
                "            </div>" +
                "            <div class='info-row'>" +
                "                <span class='label'>Estado Nuevo:</span> <span class='critical'>CRÍTICO</span>" +
                "            </div>" +
                (motivoCambio != null && !motivoCambio.isEmpty() ? "            <div class='info-row'>" +
                        "                <span class='label'>Motivo del cambio:</span><br>" +
                        "                <span class='value'>" + motivoCambio + "</span>" +
                        "            </div>" : "")
                +
                "        </div>" +
                "        <div class='footer'>" +
                "            <p>Este es un mensaje automático del Sistema de Mantenimientos INACIF<br>" +
                "            Por favor, no responda a este correo.</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }

    /**
     * Envía notificación cuando un mantenimiento está próximo a vencer
     */
    public void notificarMantenimientoProximo(Integer programacionId, String equipoNombre,
            String codigoInacif, String tipoMantenimiento,
            java.util.Date fechaProxima, int diasRestantes) {
        try {
            String urgencia = diasRestantes == 0 ? "⚠️ HOY"
                    : diasRestantes == 1 ? "⚠️ MAÑANA" : "en " + diasRestantes + " días";

            String asunto = "🔧 MANTENIMIENTO PRÓXIMO: " + equipoNombre + " - " + urgencia;

            String contenido = generarHtmlMantenimientoProximo(programacionId, equipoNombre,
                    codigoInacif, tipoMantenimiento, fechaProxima, diasRestantes);

            // Enviar a administrador y jefatura
            String adminEmail = emailProperties.getProperty("mail.admin.address", "admin@inacif.gob.gt");
            String jefaturaEmail = emailProperties.getProperty("mail.jefatura.address", "jefatura@inacif.gob.gt");

            enviarCorreo(adminEmail, asunto, contenido);

            if (!adminEmail.equalsIgnoreCase(jefaturaEmail)) {
                enviarCorreo(jefaturaEmail, asunto, contenido);
            }

            LOGGER.info("✅ Notificación de mantenimiento próximo enviada - Programación #" + programacionId);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ Error al enviar notificación de mantenimiento próximo", e);
        }
    }

    /**
     * Genera HTML para notificación de mantenimiento próximo
     */
    private String generarHtmlMantenimientoProximo(Integer programacionId, String equipoNombre,
            String codigoInacif, String tipoMantenimiento,
            java.util.Date fechaProxima, int diasRestantes) {

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        String fechaFormateada = sdf.format(fechaProxima);

        String colorUrgencia = diasRestantes <= 2 ? "#dc3545" : (diasRestantes <= 5 ? "#fd7e14" : "#28a745");
        String textoUrgencia = diasRestantes == 0 ? "⚠️ VENCE HOY"
                : diasRestantes == 1 ? "⚠️ VENCE MAÑANA" : "Vence en " + diasRestantes + " días";

        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }"
                +
                "        .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }"
                +
                "        .header { background: linear-gradient(135deg, #17a2b8 0%, #138496 100%); color: white; padding: 20px; text-align: center; }"
                +
                "        .content { padding: 30px; }" +
                "        .urgencia { background: " + colorUrgencia
                + "; color: white; padding: 10px 20px; border-radius: 5px; display: inline-block; font-weight: bold; margin-bottom: 20px; }"
                +
                "        .info-row { padding: 10px 0; border-bottom: 1px solid #eee; }" +
                "        .label { font-weight: bold; color: #555; width: 150px; display: inline-block; }" +
                "        .value { color: #333; }" +
                "        .footer { background: #f8f9fa; padding: 15px; text-align: center; font-size: 12px; color: #666; }"
                +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <div class='header'>" +
                "            <h2>🔧 MANTENIMIENTO PRÓXIMO</h2>" +
                "        </div>" +
                "        <div class='content'>" +
                "            <div class='urgencia'>" + textoUrgencia + "</div>" +
                "            <p>El siguiente equipo tiene un mantenimiento programado que requiere atención:</p>" +
                "            <div class='info-row'>" +
                "                <span class='label'>Programación ID:</span> <span class='value'>#" + programacionId
                + "</span>" +
                "            </div>" +
                "            <div class='info-row'>" +
                "                <span class='label'>Equipo:</span> <span class='value'>" + equipoNombre + "</span>" +
                "            </div>" +
                "            <div class='info-row'>" +
                "                <span class='label'>Código INACIF:</span> <span class='value'>" + codigoInacif
                + "</span>" +
                "            </div>" +
                "            <div class='info-row'>" +
                "                <span class='label'>Tipo:</span> <span class='value'>" + tipoMantenimiento + "</span>"
                +
                "            </div>" +
                "            <div class='info-row'>" +
                "                <span class='label'>Fecha programada:</span> <span class='value' style='color: "
                + colorUrgencia + "; font-weight: bold;'>" + fechaFormateada + "</span>" +
                "            </div>" +
                "            <p style='margin-top: 20px; padding: 15px; background: #e7f3ff; border-radius: 5px;'>" +
                "                <strong>Acción requerida:</strong> Por favor coordine la ejecución del mantenimiento antes de la fecha programada."
                +
                "            </p>" +
                "        </div>" +
                "        <div class='footer'>" +
                "            <p>Este es un mensaje automático del Sistema de Mantenimientos INACIF<br>" +
                "            Por favor, no responda a este correo.</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }

    /**
     * Envía notificación cuando un contrato está próximo a vencer
     */
    public void notificarContratoProximo(Integer contratoId, String numeroContrato,
            String descripcion, String proveedorNombre,
            java.util.Date fechaFin, int diasRestantes) {
        try {
            String urgencia = diasRestantes <= 7 ? "⚠️ URGENTE" : "📄 PRÓXIMO A VENCER";

            String asunto = urgencia + " - Contrato " + numeroContrato + " vence en " + diasRestantes + " días";

            String contenido = generarHtmlContratoProximo(contratoId, numeroContrato,
                    descripcion, proveedorNombre, fechaFin, diasRestantes);

            // Enviar a administrador y jefatura
            String adminEmail = emailProperties.getProperty("mail.admin.address", "admin@inacif.gob.gt");
            String jefaturaEmail = emailProperties.getProperty("mail.jefatura.address", "jefatura@inacif.gob.gt");

            enviarCorreo(adminEmail, asunto, contenido);

            if (!adminEmail.equalsIgnoreCase(jefaturaEmail)) {
                enviarCorreo(jefaturaEmail, asunto, contenido);
            }

            LOGGER.info("✅ Notificación de contrato próximo enviada - Contrato #" + contratoId);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ Error al enviar notificación de contrato próximo", e);
        }
    }

    /**
     * Genera HTML para notificación de contrato próximo a vencer
     */
    private String generarHtmlContratoProximo(Integer contratoId, String numeroContrato,
            String descripcion, String proveedorNombre,
            java.util.Date fechaFin, int diasRestantes) {

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        String fechaFormateada = sdf.format(fechaFin);

        String colorUrgencia = diasRestantes <= 7 ? "#dc3545" : (diasRestantes <= 15 ? "#fd7e14" : "#ffc107");
        String textoUrgencia = diasRestantes == 0 ? "⚠️ VENCE HOY"
                : diasRestantes <= 7 ? "⚠️ URGENTE - " + diasRestantes + " días"
                        : "Vence en " + diasRestantes + " días";

        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }"
                +
                "        .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }"
                +
                "        .header { background: linear-gradient(135deg, #6f42c1 0%, #5a32a3 100%); color: white; padding: 20px; text-align: center; }"
                +
                "        .content { padding: 30px; }" +
                "        .urgencia { background: " + colorUrgencia
                + "; color: white; padding: 10px 20px; border-radius: 5px; display: inline-block; font-weight: bold; margin-bottom: 20px; }"
                +
                "        .info-row { padding: 10px 0; border-bottom: 1px solid #eee; }" +
                "        .label { font-weight: bold; color: #555; width: 150px; display: inline-block; }" +
                "        .value { color: #333; }" +
                "        .footer { background: #f8f9fa; padding: 15px; text-align: center; font-size: 12px; color: #666; }"
                +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <div class='header'>" +
                "            <h2>📄 CONTRATO PRÓXIMO A VENCER</h2>" +
                "        </div>" +
                "        <div class='content'>" +
                "            <div class='urgencia'>" + textoUrgencia + "</div>" +
                "            <p>El siguiente contrato está próximo a vencer y requiere atención:</p>" +
                "            <div class='info-row'>" +
                "                <span class='label'>Contrato ID:</span> <span class='value'>#" + contratoId + "</span>"
                +
                "            </div>" +
                "            <div class='info-row'>" +
                "                <span class='label'>Número:</span> <span class='value'>" + numeroContrato + "</span>" +
                "            </div>" +
                "            <div class='info-row'>" +
                "                <span class='label'>Proveedor:</span> <span class='value'>" + proveedorNombre
                + "</span>" +
                "            </div>" +
                "            <div class='info-row'>" +
                "                <span class='label'>Descripción:</span> <span class='value'>"
                + (descripcion != null ? descripcion : "Sin descripción") + "</span>" +
                "            </div>" +
                "            <div class='info-row'>" +
                "                <span class='label'>Fecha vencimiento:</span> <span class='value' style='color: "
                + colorUrgencia + "; font-weight: bold;'>" + fechaFormateada + "</span>" +
                "            </div>" +
                "            <p style='margin-top: 20px; padding: 15px; background: #fff3cd; border-radius: 5px;'>" +
                "                <strong>Acción requerida:</strong> Por favor gestione la renovación del contrato o inicie el proceso de nueva contratación antes del vencimiento."
                +
                "            </p>" +
                "        </div>" +
                "        <div class='footer'>" +
                "            <p>Este es un mensaje automático del Sistema de Mantenimientos INACIF<br>" +
                "            Por favor, no responda a este correo.</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }
}
