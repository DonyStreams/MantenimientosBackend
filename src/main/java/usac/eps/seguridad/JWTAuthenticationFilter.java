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
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.keys.resolvers.JwksVerificationKeyResolver;
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
                    .setVerificationKeyResolver(jwksResolver) // Usar claves públicas de Keycloak
                    .build();

            // Validar y procesar el token
            JwtClaims jwtClaims = jwtConsumer.processToClaims(token);
            
            // Convertir claims a Map para compatibilidad
            Map<String, Object> claimsMap = jwtClaims.getClaimsMap();
            
            return claimsMap;

        } catch (Exception e) {
            System.err.println("[JWT Validation] Error validating token: " + e.getMessage());
            throw new Exception("Token JWT inválido: " + e.getMessage());
        }
}
