package usac.eps.seguridad;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Arrays;

@Provider
public class CORSFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        
        // Configurar headers CORS para el frontend Angular
        responseContext.getHeaders().add("Access-Control-Allow-Origin", "http://localhost:4200");
        responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        responseContext.getHeaders().add("Access-Control-Allow-Headers", 
            "Origin, Content-Type, Accept, Authorization, X-Requested-With, Cache-Control");
        responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
        responseContext.getHeaders().add("Access-Control-Max-Age", "1209600");
        responseContext.getHeaders().add("Access-Control-Expose-Headers", "Content-Disposition");

        // Para testing con Keycloak Mock - simular usuario autenticado en todas las peticiones
        // Esto permite que el frontend funcione sin un token JWT real
        if (requestContext.getProperty("username") == null) {
            // Simular un usuario administrador autenticado
            requestContext.setProperty("username", "admin");
            requestContext.setProperty("email", "admin@inacif.gob.gt");
            requestContext.setProperty("fullName", "Administrador Sistema");
            requestContext.setProperty("userId", "admin-001");
            requestContext.setProperty("roles", Arrays.asList("ADMIN", "SUPERVISOR", "USER"));
            
            // Simular claims de Keycloak
            requestContext.setProperty("preferred_username", "admin");
            requestContext.setProperty("realm_access_roles", Arrays.asList("ADMIN"));
            requestContext.setProperty("resource_access_roles", Arrays.asList("ADMIN", "SUPERVISOR"));
            
            // Marcar como autenticado
            requestContext.setProperty("authenticated", true);
            
            System.out.println("[CORS Filter] Usuario mock autenticado: admin con roles: " + 
                Arrays.asList("ADMIN", "SUPERVISOR", "USER"));
        }
    }
}
