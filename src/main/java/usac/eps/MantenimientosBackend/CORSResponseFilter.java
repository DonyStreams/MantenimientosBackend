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
 * Generic CORS filter to use services from different port/host. You
 * could/should personalize Allow Origin option to match your domain. It also
 * allows pre-flight (OPTIONS) query for application/json data type.
 *
 * @author tuxtor
 *
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

                // Authorize (allow) all domains to consume the content
                ((HttpServletResponse) servletResponse).addHeader("Access-Control-Allow-Headers",
                                "X-Count-Total, Content-Type, Accept, Origin, Authorization, X-Filename");
                ((HttpServletResponse) servletResponse).addHeader("Access-Control-Expose-Headers",
                                "X-Count-Total, Content-Type, Accept, Origin, Authorization, X-Filename");
                ((HttpServletResponse) servletResponse).addHeader("Access-Control-Allow-Origin", "*");
                ((HttpServletResponse) servletResponse).addHeader("Access-Control-Allow-Methods",
                                "GET, OPTIONS, HEAD, PUT, POST, DELETE");

                HttpServletResponse resp = (HttpServletResponse) servletResponse;

                if (request.getMethod().equals("OPTIONS")) {
                        ((HttpServletResponse) servletResponse).addHeader("Content-Type", "application/json");
                        resp.setStatus(HttpServletResponse.SC_ACCEPTED);
                        return;
                }

                // --- INICIO BLOQUE JWT ---
                /*
                 * String authHeader = request.getHeader("Authorization");
                 * if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                 * ((HttpServletResponse)
                 * servletResponse).sendError(HttpServletResponse.SC_UNAUTHORIZED,
                 * "Token JWT requerido");
                 * return;
                 * }
                 * 
                 * String token = authHeader.substring(7);
                 * try {
                 * JwtClaims claims = jwtConsumer.processToClaims(token);
                 * // Opcional: aquí puedes leer claims, como el usuario, roles, etc.
                 * } catch (org.jose4j.jwt.consumer.InvalidJwtException e) {
                 * ((HttpServletResponse)
                 * servletResponse).sendError(HttpServletResponse.SC_UNAUTHORIZED,
                 * "Token JWT inválido o expirado");
                 * return;
                 * }
                 */
                // --- FIN BLOQUE JWT ---

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
