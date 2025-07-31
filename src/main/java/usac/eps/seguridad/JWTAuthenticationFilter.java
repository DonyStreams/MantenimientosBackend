package usac.eps.seguridad;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

// Temporalmente deshabilitado para testing
// @Provider
@Priority(Priorities.AUTHENTICATION)
public class JWTAuthenticationFilter implements ContainerRequestFilter {

    @Context
    private HttpServletRequest httpRequest;

    private static final String KEYCLOAK_URL = "http://localhost:8080";
    private static final String REALM = "MantenimientosINACIF";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Obtener el token del header Authorization
        String authorizationHeader = requestContext.getHeaderString("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            // Sin token, denegar acceso
            requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .entity("Token de acceso requerido")
                            .build());
            return;
        }

        String token = authorizationHeader.substring("Bearer ".length()).trim();

        try {
            // Validar el token JWT
            Map<String, Object> claims = validateToken(token);

            // Extraer información del usuario
            String username = (String) claims.get("preferred_username");
            String email = (String) claims.get("email");

            // Extraer roles del cliente
            @SuppressWarnings("unchecked")
            Map<String, Object> resourceAccess = (Map<String, Object>) claims.get("resource_access");
            List<String> roles = null;

            if (resourceAccess != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get("inacif-frontend");
                if (clientAccess != null) {
                    @SuppressWarnings("unchecked")
                    List<String> clientRoles = (List<String>) clientAccess.get("roles");
                    roles = clientRoles;
                }
            }

            // Almacenar información del usuario en el contexto de la petición
            requestContext.setProperty("username", username);
            requestContext.setProperty("email", email);
            requestContext.setProperty("roles", roles);
            requestContext.setProperty("jwt_claims", claims);

        } catch (Exception e) {
            // Token inválido
            requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .entity("Token inválido: " + e.getMessage())
                            .build());
        }
    }

    private Map<String, Object> validateToken(String token) throws Exception {
        try {
            // Por ahora validamos solo la estructura básica del JWT
            // En producción deberías validar la firma con la clave pública de Keycloak
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new Exception("Token JWT mal formado");
            }

            // Decodificar el payload (segunda parte)
            java.util.Base64.Decoder decoder = java.util.Base64.getUrlDecoder();
            String payload = new String(decoder.decode(parts[1]));

            // Convertir JSON a Map (simplificado)
            // En producción deberías usar una librería JSON como Jackson
            java.util.Map<String, Object> claims = new java.util.HashMap<>();

            // Parseo básico del JSON (para demo)
            if (payload.contains("\"preferred_username\"")) {
                // Extraer username
                String username = extractJsonValue(payload, "preferred_username");
                claims.put("preferred_username", username);
            }

            if (payload.contains("\"email\"")) {
                // Extraer email
                String email = extractJsonValue(payload, "email");
                claims.put("email", email);
            }

            // Simular roles para testing
            claims.put("resource_access", createMockResourceAccess());

            return claims;

        } catch (Exception e) {
            throw new Exception("Token JWT inválido: " + e.getMessage());
        }
    }

    private String extractJsonValue(String json, String key) {
        try {
            String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]+)\"";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                return m.group(1);
            }
        } catch (Exception e) {
            // Ignorar errores de parsing
        }
        return null;
    }

    private Map<String, Object> createMockResourceAccess() {
        Map<String, Object> resourceAccess = new java.util.HashMap<>();
        Map<String, Object> clientAccess = new java.util.HashMap<>();
        clientAccess.put("roles", Arrays.asList("ADMIN", "SUPERVISOR")); // Roles de prueba
        resourceAccess.put("inacif-frontend", clientAccess);
        return resourceAccess;
    }
}
