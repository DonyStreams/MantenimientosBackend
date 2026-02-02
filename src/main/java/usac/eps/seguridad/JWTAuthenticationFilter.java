package usac.eps.seguridad;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwk.HttpsJwks;
import org.jose4j.keys.resolvers.JwksVerificationKeyResolver;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

// Autenticación JWT activada para producción
@Provider
@Priority(Priorities.AUTHENTICATION)
public class JWTAuthenticationFilter implements ContainerRequestFilter {
    private static final Logger LOGGER = Logger.getLogger(JWTAuthenticationFilter.class.getName());

    @Context
    private HttpServletRequest httpRequest;

    private static final String KEYCLOAK_URL = "http://172.16.1.192:8080/auth";
    private static final String REALM = "MantenimientosINACIF";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();

        // Rutas públicas que NO requieren autenticación JWT
        if (isPublicPath(path)) {
            return; // Permitir acceso sin token
        }

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
            String keycloakId = (String) claims.get("sub"); // ID único del usuario en Keycloak
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
            // Usar tanto setProperty (para JAX-RS) como setAttribute (para Servlets)
            requestContext.setProperty("keycloakId", keycloakId);
            requestContext.setProperty("username", username);
            requestContext.setProperty("email", email);
            requestContext.setProperty("roles", roles);
            requestContext.setProperty("jwt_claims", claims);

            // También almacenar en el HttpServletRequest para compatibilidad
            if (httpRequest != null) {
                httpRequest.setAttribute("keycloakId", keycloakId);
                httpRequest.setAttribute("username", username);
                httpRequest.setAttribute("email", email);
                httpRequest.setAttribute("roles", roles);
                httpRequest.setAttribute("jwt_claims", claims);
            }

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Token inválido", e);
            // Token inválido
            requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .entity("Token inválido: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Verifica si la ruta es pública y no requiere autenticación
     */
    private boolean isPublicPath(String path) {
        // Lista de rutas públicas
        String[] publicPaths = {
                "auth/health", // Health check público
                "health", // Health check general si existe
                "status", // Status general si existe
                "imagenes/view", // Ver imágenes sin autenticación
                "imagenes/test", // Test del sistema de imágenes
                "imagenes/upload" // Subir imágenes
        };

        for (String publicPath : publicPaths) {
            if (path.equals(publicPath) || path.startsWith(publicPath + "/")) {
                return true;
            }
        }
        return false;
    }

    private Map<String, Object> validateToken(String token) throws Exception {
        try {
            // URL del JWKS de Keycloak
            String jwksUrl = KEYCLOAK_URL + "/realms/" + REALM + "/protocol/openid-connect/certs";

            // Crear HttpsJwks para obtener las claves públicas de Keycloak
            HttpsJwks httpsJkws = new HttpsJwks(jwksUrl);
            JwksVerificationKeyResolver jwksResolver = new JwksVerificationKeyResolver(httpsJkws.getJsonWebKeys());

            // Configurar el consumer JWT
            JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                    .setRequireExpirationTime() // Token debe tener exp
                    .setAllowedClockSkewInSeconds(30) // Permitir 30 segundos de diferencia de reloj
                    .setRequireSubject() // Token debe tener subject
                    .setExpectedIssuer(KEYCLOAK_URL + "/realms/" + REALM) // Verificar emisor
                    .setExpectedAudience("inacif-frontend") // Validar que el token es para nuestro cliente
                    .setVerificationKeyResolver(jwksResolver) // Usar claves públicas de Keycloak
                    .build();

            // Validar y procesar el token
            JwtClaims jwtClaims = jwtConsumer.processToClaims(token);

            // Convertir claims a Map para compatibilidad
            Map<String, Object> claimsMap = jwtClaims.getClaimsMap();

            return claimsMap;

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al validar token JWT", e);
            throw new Exception("Token JWT inválido: " + e.getMessage());
        }
    }
}
