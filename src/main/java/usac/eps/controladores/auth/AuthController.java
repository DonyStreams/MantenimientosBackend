package usac.eps.controladores.auth;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthController {

    private static final Logger LOGGER = Logger.getLogger(AuthController.class.getName());

    @Context
    private HttpServletRequest request;

    @GET
    @Path("/me")
    public Response getCurrentUser() {
        try {
            // Obtener información del usuario desde las propiedades del request
            String username = (String) request.getAttribute("username");
            String email = (String) request.getAttribute("email");
            @SuppressWarnings("unchecked")
            Map<String, Object> jwtClaims = (Map<String, Object>) request.getAttribute("jwt_claims");

            // Extraer roles desde los claims JWT
            List<String> roles = extractRolesFromClaims(jwtClaims);

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("username", username != null ? username : "anonymous");
            userInfo.put("email", email != null ? email : "");
            userInfo.put("roles", roles);
            userInfo.put("authenticated", username != null);

            return Response.ok(userInfo).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener información del usuario", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al obtener información del usuario: " + e.getMessage())
                    .build();
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> extractRolesFromClaims(Map<String, Object> claims) {
        if (claims == null)
            return java.util.Arrays.asList("USER");

        try {
            // Intentar extraer roles del cliente inacif-frontend
            Map<String, Object> resourceAccess = (Map<String, Object>) claims.get("resource_access");
            if (resourceAccess != null) {
                Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get("inacif-frontend");
                if (clientAccess != null) {
                    List<String> clientRoles = (List<String>) clientAccess.get("roles");
                    if (clientRoles != null && !clientRoles.isEmpty()) {
                        return clientRoles;
                    }
                }
            }

            // Fallback: intentar extraer roles del realm
            Map<String, Object> realmAccess = (Map<String, Object>) claims.get("realm_access");
            if (realmAccess != null) {
                List<String> realmRoles = (List<String>) realmAccess.get("roles");
                if (realmRoles != null && !realmRoles.isEmpty()) {
                    return realmRoles;
                }
            }

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error extrayendo roles desde claims", e);
        }

        // Fallback por defecto
        return java.util.Arrays.asList("USER");
    }

    @GET
    @Path("/health")
    public Response healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "OK");
        health.put("timestamp", new java.util.Date());
        health.put("service", "MantenimientosBackend Auth Service");
        return Response.ok(health).build();
    }

    @OPTIONS
    @Path("/{path:.*}")
    public Response handleOptions() {
        return Response.ok().build();
    }
}
