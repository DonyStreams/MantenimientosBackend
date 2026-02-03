package usac.eps.controladores;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Controlador para verificar el estado del sistema (Health Check)
 * Usado por Docker healthcheck y monitoreo
 */
@Path("/health")
public class HealthController {

    @PersistenceContext(unitName = "usac.eps_ControlSuministros")
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
            // Verificar configuración SMTP desde variables de entorno
            String smtpHost = System.getenv("SMTP_HOST");
            if (smtpHost != null && !smtpHost.isEmpty()) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkKeycloakConfig() {
        try {
            // Verificar conectividad con Keycloak JWKS endpoint
            String keycloakUrl = System.getenv("KEYCLOAK_URL");
            if (keycloakUrl == null || keycloakUrl.isEmpty()) {
                keycloakUrl = "http://172.16.1.192:8080/auth"; // URL por defecto
            }
            String realm = System.getenv("KEYCLOAK_REALM");
            if (realm == null || realm.isEmpty()) {
                realm = "inacif";
            }

            String jwksUrl = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/certs";
            URL url = new URL(jwksUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            int responseCode = conn.getResponseCode();
            conn.disconnect();

            return responseCode == 200;
        } catch (Exception e) {
            return false;
        }
    }
}
