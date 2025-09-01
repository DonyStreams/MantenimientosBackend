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

                // DEBUG: Logging detallado de todas las peticiones
                String method = request.getMethod();
                String uri = request.getRequestURI();
                String authHeader = request.getHeader("Authorization");

                System.out.println("=== CORS FILTER DEBUG ===");
                System.out.println("Method: " + method);
                System.out.println("URI: " + uri);
                System.out.println("Authorization Header: " + (authHeader != null
                                ? authHeader.substring(0, Math.min(50, authHeader.length())) + "..."
                                : "NULL"));
                System.out.println("========================");

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
                        System.out.println("[CORS Filter] OPTIONS request - permitiendo pre-flight");
                        response.addHeader("Content-Type", "application/json");
                        response.setStatus(HttpServletResponse.SC_OK);
                        return;
                }

                // 🆕 VERIFICAR SI ES RUTA PÚBLICA ANTES DE REQUERIR JWT
                if (isPublicPath(uri)) {
                        System.out.println("[CORS Filter] ✅ Ruta pública permitida sin JWT: " + uri);
                        // Continuar sin verificar JWT
                        chain.doFilter(request, response);
                        return;
                }

                // --- AUTENTICACIÓN JWT ACTIVADA PARA PRODUCCIÓN ---
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                        System.err.println("[CORS Filter] BLOQUEANDO ACCESO - Sin token JWT válido");
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token JWT requerido");
                        return;
                }

                String token = authHeader.substring(7);
                try {
                        JwtClaims claims = jwtConsumer.processToClaims(token);

                        // Extraer información del usuario desde el token
                        String username = claims.getStringClaimValue("preferred_username");
                        String email = claims.getStringClaimValue("email");

                        // Almacenar en atributos del request para uso posterior
                        request.setAttribute("username", username);
                        request.setAttribute("email", email);
                        request.setAttribute("jwt_claims", claims.getClaimsMap());
                        request.setAttribute("authenticated", true);

                        System.out.println("[CORS Filter] ✅ Usuario JWT autenticado: " + username);

                } catch (Exception e) {
                        System.err.println("[CORS Filter] ❌ Error validando JWT: " + e.getMessage());
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token JWT inválido o expirado");
                        return;
                }

                chain.doFilter(request, servletResponse);
        }

        @Override
        public void init(FilterConfig fConfig) throws ServletException {
                String jwksUrl = "http://172.16.1.192:8080/auth/realms/MantenimientosINACIF/protocol/openid-connect/certs";

                HttpsJwks httpsJwks = new HttpsJwks(jwksUrl);
                HttpsJwksVerificationKeyResolver keyResolver = new HttpsJwksVerificationKeyResolver(httpsJwks);

                jwtConsumer = new JwtConsumerBuilder()
                                .setRequireExpirationTime() // requiere tiempo de expiración
                                .setAllowedClockSkewInSeconds(30) // tolerancia de reloj
                                .setRequireSubject() // requiere claim "sub"
                                .setExpectedIssuer("http://172.16.1.192:8080/auth/realms/MantenimientosINACIF") // verificar
                                // emisor
                                .setSkipDefaultAudienceValidation() // temporalmente sin audience
                                .setVerificationKeyResolver(keyResolver) // usa la clave pública de Keycloak para
                                                                         // verificar firma
                                .build();
        }

        /**
         * 🆕 Verifica si la ruta es pública y no requiere autenticación JWT
         */
        private boolean isPublicPath(String uri) {
                // Rutas públicas que NO requieren JWT
                String[] publicPaths = {
                                "/MantenimientosBackend/api/ftp/test",
                                "/MantenimientosBackend/api/ftp/upload",
                                "/MantenimientosBackend/api/ftp",
                                "/MantenimientosBackend/api/auth/health",
                                "/MantenimientosBackend/api/health",
                                "/MantenimientosBackend/api/status",
                                "/MantenimientosBackend/api/imagenes/view",
                                "/MantenimientosBackend/api/imagenes/test"
                };

                for (String publicPath : publicPaths) {
                        if (uri.equals(publicPath) || uri.startsWith(publicPath + "/")) {
                                return true;
                        }
                }
                return false;
        }

}
