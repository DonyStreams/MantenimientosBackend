package usac.eps.seguridad;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Filtro de autorización que verifica los roles del usuario
 * basándose en la anotación @RequiresRole.
 * 
 * Se ejecuta DESPUÉS del filtro de autenticación JWT.
 * 
 * @author Sistema de Mantenimientos INACIF
 */
@Provider
@Priority(Priorities.AUTHORIZATION) // Se ejecuta después de AUTHENTICATION
public class RoleAuthorizationFilter implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    @Context
    private HttpServletRequest httpRequest;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Obtener el método y la clase del recurso
        Method method = resourceInfo.getResourceMethod();
        Class<?> resourceClass = resourceInfo.getResourceClass();

        // Buscar anotación @RequiresRole en el método primero, luego en la clase
        RequiresRole methodAnnotation = method.getAnnotation(RequiresRole.class);
        RequiresRole classAnnotation = resourceClass.getAnnotation(RequiresRole.class);

        RequiresRole roleAnnotation = methodAnnotation != null ? methodAnnotation : classAnnotation;

        // Si no hay anotación, permitir acceso (ya pasó la autenticación)
        if (roleAnnotation == null) {
            return;
        }

        // Obtener roles del usuario desde el request (puestos por
        // JWTAuthenticationFilter)
        @SuppressWarnings("unchecked")
        List<String> userRoles = (List<String>) httpRequest.getAttribute("roles");

        if (userRoles == null || userRoles.isEmpty()) {
            abortWithForbidden(requestContext, roleAnnotation.message());
            return;
        }

        String[] requiredRoles = roleAnnotation.value();
        boolean requireAll = roleAnnotation.requireAll();

        boolean hasAccess;

        if (requireAll) {
            // Debe tener TODOS los roles
            hasAccess = Arrays.stream(requiredRoles)
                    .allMatch(userRoles::contains);
        } else {
            // Debe tener AL MENOS UNO de los roles
            hasAccess = Arrays.stream(requiredRoles)
                    .anyMatch(userRoles::contains);
        }

        if (!hasAccess) {
            abortWithForbidden(requestContext, roleAnnotation.message());
            return;
        }
    }

    private void abortWithForbidden(ContainerRequestContext requestContext, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Acceso denegado");
        errorResponse.put("mensaje", message);
        errorResponse.put("codigo", "FORBIDDEN");

        requestContext.abortWith(
                Response.status(Response.Status.FORBIDDEN)
                        .entity(errorResponse)
                        .type("application/json")
                        .build());
    }
}
