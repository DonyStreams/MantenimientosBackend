package usac.eps.controladores.auth;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthController {

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
            List<String> roles = (List<String>) request.getAttribute("roles");

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("username", username != null ? username : "admin");
            userInfo.put("email", email != null ? email : "admin@inacif.gob.gt");
            userInfo.put("roles", roles != null ? roles : java.util.Arrays.asList("ADMIN"));
            userInfo.put("authenticated", true);

            return Response.ok(userInfo).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al obtener información del usuario: " + e.getMessage())
                    .build();
        }
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
