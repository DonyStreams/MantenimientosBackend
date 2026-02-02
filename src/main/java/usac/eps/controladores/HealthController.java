package usac.eps.controladores;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Properties;
import java.io.InputStream;

/**
 * Controlador para verificar el estado del sistema (Health Check)
 * Usado por Docker healthcheck y monitoreo
 */
@Path("/health")
public class HealthController {

    @PersistenceContext(unitName = "MantenimientosPU")
    private EntityManager em;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkHealth() {
        JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
        boolean isHealthy = true;

        try {
            // Verificar conexión a base de datos
            boolean dbConnected = checkDatabase();
            responseBuilder.add("database", dbConnected ? "UP" : "DOWN");
            if (!dbConnected)
                isHealthy = false;

            // Verificar configuración de email
            boolean emailConfigured = checkEmailConfig();
            responseBuilder.add("email", emailConfigured ? "CONFIGURED" : "NOT_CONFIGURED");

            // Verificar configuración de Keycloak
            boolean keycloakConfigured = checkKeycloakConfig();
            responseBuilder.add("keycloak", keycloakConfigured ? "CONFIGURED" : "NOT_CONFIGURED");

            // Estado general
            responseBuilder.add("status", isHealthy ? "UP" : "DOWN");
            responseBuilder.add("timestamp", System.currentTimeMillis());

            // Información del sistema
            responseBuilder.add("java_version", System.getProperty("java.version"));
            responseBuilder.add("os", System.getProperty("os.name"));

            if (isHealthy) {
                return Response.ok(responseBuilder.build()).build();
            } else {
                return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                        .entity(responseBuilder.build())
                        .build();
            }

        } catch (Exception e) {
            responseBuilder.add("status", "DOWN");
            responseBuilder.add("error", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(responseBuilder.build())
                    .build();
        }
    }

    private boolean checkDatabase() {
        try {
            em.createNativeQuery("SELECT 1").getSingleResult();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkEmailConfig() {
        try {
            Properties props = new Properties();
            InputStream is = getClass().getClassLoader().getResourceAsStream("email.properties");
            if (is != null) {
                props.load(is);
                return props.containsKey("mail.smtp.host");
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkKeycloakConfig() {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("keycloak.json");
            return is != null;
        } catch (Exception e) {
            return false;
        }
    }
}
