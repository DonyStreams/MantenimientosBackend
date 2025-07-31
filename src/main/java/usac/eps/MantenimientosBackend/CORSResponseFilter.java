package usac.eps.MantenimientosBackend;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.jose4j.jwk.HttpsJwks;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.keys.resolvers.HttpsJwksVerificationKeyResolver;
import org.jose4j.lang.JoseException;

/**
 * CORS filter configurado para el Sistema de Mantenimientos INACIF.
 * Maneja CORS para el frontend Angular y simula autenticación con Keycloak
 * para propósitos de testing y desarrollo.
 * 
 * @author tuxtor
 * @modified para INACIF - Sistema de Mantenimientos
 */
@WebFilter(urlPatterns = { "/*" })
public class CORSResponseFilter implements Filter {

        private JwtConsumer jwtConsumer;

        public CORSResponseFilter() {
        }

        public void destroy() {
        }

        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
                        throws IOException, ServletException {

                HttpServletRequest request = (HttpServletRequest) servletRequest;
                HttpServletResponse response = (HttpServletResponse) servletResponse;

                // Configurar headers CORS para el frontend Angular
                response.addHeader("Access-Control-Allow-Headers",
                                "X-Count-Total, Content-Type, Accept, Origin, Authorization, X-Filename, X-Requested-With, Cache-Control");
                response.addHeader("Access-Control-Expose-Headers",
                                "X-Count-Total, Content-Type, Accept, Origin, Authorization, X-Filename, Content-Disposition");
                response.addHeader("Access-Control-Allow-Origin", "http://localhost:4200");
                response.addHeader("Access-Control-Allow-Methods",
                                "GET, OPTIONS, HEAD, PUT, POST, DELETE");
                response.addHeader("Access-Control-Allow-Credentials", "true");
                response.addHeader("Access-Control-Max-Age", "1209600");

                // Manejar pre-flight requests
                if (request.getMethod().equals("OPTIONS")) {
                        response.addHeader("Content-Type", "application/json");
                        response.setStatus(HttpServletResponse.SC_OK);
                        return;
                }

                // --- AUTENTICACIÓN MOCK PARA TESTING ---
                // Simular usuario autenticado en todas las peticiones
                // Esto permite que el frontend funcione sin un token JWT real
                request.setAttribute("username", "admin");
                request.setAttribute("email", "admin@inacif.gob.gt");
                request.setAttribute("fullName", "Administrador Sistema");
                request.setAttribute("userId", "admin-001");
                request.setAttribute("roles", java.util.Arrays.asList("ADMIN", "SUPERVISOR", "USER"));

                // Simular claims de Keycloak
                request.setAttribute("preferred_username", "admin");
                request.setAttribute("realm_access_roles", java.util.Arrays.asList("ADMIN"));
                request.setAttribute("resource_access_roles", java.util.Arrays.asList("ADMIN", "SUPERVISOR"));
                request.setAttribute("authenticated", true);

                System.out.println("[CORS Filter] Usuario mock autenticado: admin con roles: " +
                                java.util.Arrays.asList("ADMIN", "SUPERVISOR", "USER"));

                // --- BLOQUE JWT COMENTADO PARA TESTING ---
                /*
                 * String authHeader = request.getHeader("Authorization");
                 * if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                 * response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                 * "Token JWT requerido");
                 * return;
                 * }
                 * 
                 * String token = authHeader.substring(7);
                 * try {
                 * JwtClaims claims = jwtConsumer.processToClaims(token);
                 * // Opcional: aquí puedes leer claims, como el usuario, roles, etc.
                 * } catch (org.jose4j.jwt.consumer.InvalidJwtException e) {
                 * response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                 * "Token JWT inválido o expirado");
                 * return;
                 * }
                 */

                chain.doFilter(request, servletResponse);
        }

        @Override
        public void init(FilterConfig fConfig) throws ServletException {
                String jwksUrl = "http://localhost:8080/realms/demo/protocol/openid-connect/certs";

                HttpsJwks httpsJwks = new HttpsJwks(jwksUrl);
                HttpsJwksVerificationKeyResolver keyResolver = new HttpsJwksVerificationKeyResolver(httpsJwks);

                jwtConsumer = new JwtConsumerBuilder()
                                .setRequireExpirationTime() // requiere tiempo de expiración
                                .setAllowedClockSkewInSeconds(30) // tolerancia de reloj
                                .setRequireSubject() // requiere claim "sub"
                                .setExpectedAudience("account")
                                .setVerificationKeyResolver(keyResolver) // usa la clave pública de Keycloak para
                                                                         // verificar firma
                                .build();
        }

}
