package usac.eps.controladores.mantenimientos;

import usac.eps.modelos.mantenimientos.UsuarioMantenimientoModel;
import usac.eps.repositorios.mantenimientos.UsuarioMantenimientoRepository;
import usac.eps.seguridad.RequiresRole;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/usuarios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class UsuarioController {

    @Inject
    private UsuarioMantenimientoRepository usuarioRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Context
    private HttpServletRequest request;

    // Método de debug simple
    @GET
    @Path("/test")
    public Response test() {
        return Response.ok("Usuario controller funcionando").build();
    }

    /**
     * 🔒 Solo ADMIN puede ver la lista completa de usuarios
     */
    @GET
    @RequiresRole({ "ADMIN" })
    public Response getAll() {
        try {
            List<UsuarioMantenimientoModel> usuarios = usuarioRepository.findAll();
            System.out.println("📋 Devolviendo " + usuarios.size() + " usuarios");
            return Response.ok(usuarios).build();
        } catch (Exception e) {
            System.out.println("❌ Error al obtener usuarios: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al obtener usuarios\"}")
                    .build();
        }
    }

    /**
     * 🔒 Roles operativos pueden ver usuarios activos (para asignaciones)
     */
    @GET
    @Path("/activos")
    @RequiresRole({ "ADMIN", "SUPERVISOR", "TECNICO", "TECNICO_EQUIPOS", "USER" })
    public List<UsuarioMantenimientoModel> getActivos() {
        return usuarioRepository.findByActivo(true);
    }

    @GET
    @Path("/{id}")
    @RequiresRole({ "ADMIN" })
    public UsuarioMantenimientoModel getById(@PathParam("id") Integer id) {
        return usuarioRepository.findById(id);
    }

    @GET
    @Path("/keycloak/{keycloakId}")
    public UsuarioMantenimientoModel getByKeycloakId(@PathParam("keycloakId") String keycloakId) {
        try {
            return usuarioRepository.findByKeycloakId(keycloakId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @POST
    @Path("/sync")
    public Response syncFromKeycloak(UsuarioMantenimientoModel usuario) {
        try {
            // Verificar si ya existe por keycloak_id
            UsuarioMantenimientoModel existente = usuarioRepository.findByKeycloakId(usuario.getKeycloakId());

            if (existente != null) {
                // Actualizar información existente
                existente.setNombreCompleto(usuario.getNombreCompleto());
                existente.setCorreo(usuario.getCorreo());
                existente.setActivo(usuario.getActivo());
                usuarioRepository.save(existente);
                return Response.ok(existente).build();
            } else {
                // Crear nuevo usuario
                usuarioRepository.save(usuario);
                return Response.status(Response.Status.CREATED).entity(usuario).build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al sincronizar usuario: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * 🔒 Solo ADMIN puede activar/desactivar usuarios
     */
    @PUT
    @Path("/{id}/estado")
    @RequiresRole({ "ADMIN" })
    public Response toggleEstado(@PathParam("id") Integer id) {
        try {
            UsuarioMantenimientoModel usuario = usuarioRepository.findById(id);
            if (usuario == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Usuario no encontrado\"}")
                        .build();
            }

            boolean nuevoEstado = !usuario.getActivo();
            usuario.setActivo(nuevoEstado);

            // 🔄 IMPORTANTE: Guardar en BD primero
            usuarioRepository.save(usuario);

            // 🚀 INVALIDAR CACHÉ: Forzar refresh del EntityManager
            try {
                entityManager.flush(); // Forzar escribir a BD
                entityManager.clear(); // Limpiar caché L1
                entityManager.getEntityManagerFactory().getCache().evictAll(); // Limpiar caché L2
                System.out.println("🧹 Cache invalidada después de cambio de estado");
            } catch (Exception cacheError) {
                System.out.println("⚠️ Error al invalidar cache (no crítico): " + cacheError.getMessage());
            }

            // Log de auditoría
            String accion = nuevoEstado ? "ACTIVADO" : "DESACTIVADO";
            System.out.println("👤 Usuario " + accion + ": " + usuario.getNombreCompleto() +
                    " (ID: " + usuario.getId() + ") - Cache invalidada ✅");

            return Response.ok()
                    .entity("{\"mensaje\": \"Usuario " + accion.toLowerCase() + " exitosamente\", \"usuario\": " +
                            "{\"id\":" + usuario.getId() + ",\"activo\":" + nuevoEstado + "}}")
                    .build();
        } catch (Exception e) {
            System.out.println("❌ Error al cambiar estado del usuario: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al cambiar estado: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * 🛡️ NOTA DE SEGURIDAD:
     * No se implementa eliminación física de usuarios para:
     * - Conservar integridad referencial con mantenimientos/tickets
     * - Mantener trazabilidad y auditoría
     * - Evitar referencias huérfanas en el sistema
     * 
     * Los usuarios se gestionan únicamente mediante activación/desactivación.
     * Para eliminar completamente un usuario, debe hacerse desde Keycloak.
     */

    /**
     * 🧹 ENDPOINT ADMIN: Invalidar cache manualmente
     * 🔒 Solo ADMIN puede invalidar cache
     */
    @POST
    @Path("/cache/invalidate")
    @RequiresRole({ "ADMIN" })
    public Response invalidateCache() {
        try {
            entityManager.flush();
            entityManager.clear();
            entityManager.getEntityManagerFactory().getCache().evictAll();

            System.out.println("🧹 Cache invalidada manualmente por administrador");

            return Response.ok()
                    .entity("{\"mensaje\": \"Cache invalidada exitosamente\", \"timestamp\": \"" +
                            java.time.LocalDateTime.now() + "\"}")
                    .build();
        } catch (Exception e) {
            System.out.println("❌ Error al invalidar cache: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al invalidar cache: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * 🔒 Solo ADMIN puede ver estadísticas de usuarios
     */
    @GET
    @Path("/stats")
    @RequiresRole({ "ADMIN" })
    public Response getStats() {
        try {
            List<UsuarioMantenimientoModel> todos = usuarioRepository.findAll();
            List<UsuarioMantenimientoModel> activos = usuarioRepository.findByActivo(true);

            return Response.ok()
                    .entity("{\"total\":" + todos.size() +
                            ",\"activos\":" + activos.size() +
                            ",\"inactivos\":" + (todos.size() - activos.size()) + "}")
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al obtener estadísticas\"}")
                    .build();
        }
    }

    // ===== MÉTODOS DE AUTO-SINCRONIZACIÓN =====

    /**
     * Endpoint para verificar o auto-sincronizar el usuario actual desde JWT
     */
    @POST
    @Path("/auto-sync")
    public Response autoSyncCurrentUser() {
        try {
            String keycloakId = (String) request.getAttribute("keycloakId");
            String username = (String) request.getAttribute("username");
            String email = (String) request.getAttribute("email");

            if (keycloakId == null || username == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Información de JWT incompleta\"}")
                        .build();
            }

            UsuarioMantenimientoModel usuario = getOrCreateUsuario(keycloakId, username, email);

            if (usuario != null) {
                return Response.ok(usuario).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("{\"error\": \"Error en auto-sincronización\"}")
                        .build();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al auto-sincronizar usuario\"}")
                    .build();
        }
    }

    /**
     * Obtiene información del usuario actual desde JWT (sin crear)
     */
    @GET
    @Path("/me")
    public Response getCurrentUser() {
        try {
            String keycloakId = (String) request.getAttribute("keycloakId");
            String username = (String) request.getAttribute("username");
            String email = (String) request.getAttribute("email");

            System.out.println("🔍 getCurrentUser - Keycloak ID: " + keycloakId);
            System.out.println("🔍 getCurrentUser - Username: " + username);

            if (keycloakId == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\": \"Token JWT inválido\"}")
                        .build();
            }

            // Buscar usuario existente (manejar NoResultException)
            UsuarioMantenimientoModel usuario = null;
            try {
                usuario = usuarioRepository.findByKeycloakId(keycloakId);
                System.out.println("✅ Usuario encontrado en BD: " + usuario.getNombreCompleto());
                System.out.println("📊 DETALLES COMPLETOS:");
                System.out.println("   - ID: " + usuario.getId());
                System.out.println("   - Nombre: " + usuario.getNombreCompleto());
                System.out.println("   - Email: " + usuario.getCorreo());
                System.out.println("   - Activo: " + usuario.getActivo() + " (tipo: "
                        + usuario.getActivo().getClass().getSimpleName() + ")");
                System.out.println("   - Keycloak ID: " + usuario.getKeycloakId());
            } catch (javax.persistence.NoResultException e) {
                System.out.println("🔄 Usuario no encontrado en BD, necesita sincronización: " + keycloakId);
                usuario = null; // Explícitamente establecer como null
            }

            if (usuario != null) {
                // 🛡️ VALIDACIÓN CRÍTICA: Verificar estado activo
                boolean estaActivo = usuario.getActivo();
                System.out.println("🔍 VALIDACIÓN DE ESTADO:");
                System.out.println("   - Valor activo leído: " + estaActivo);
                System.out.println("   - ¿Es true?: " + (estaActivo == true));
                System.out.println("   - ¿Es false?: " + (estaActivo == false));
                System.out.println("   - Negación (!activo): " + (!estaActivo));

                if (!estaActivo) {
                    System.out.println("🚫 ACCESO DENEGADO: Usuario desactivado - " + usuario.getNombreCompleto());
                    return Response.status(Response.Status.FORBIDDEN)
                            .entity("{\"error\": \"Usuario desactivado\", \"codigo\": \"USUARIO_DESACTIVADO\", \"usuario\": \""
                                    + usuario.getNombreCompleto() + "\"}")
                            .build();
                }

                System.out.println("✅ Usuario activo verificado: " + usuario.getNombreCompleto());
                return Response.ok(usuario).build();
            } else {
                // Usuario no sincronizado - devolver info básica
                System.out.println("📝 Devolviendo información básica para usuario no sincronizado");
                String json = String.format(
                        "{\"keycloakId\":\"%s\",\"nombreCompleto\":\"%s\",\"correo\":\"%s\",\"sincronizado\":false}",
                        keycloakId, username, email != null ? email : "");
                return Response.ok(json).build();
            }

        } catch (Exception e) {
            System.out.println("❌ Error en getCurrentUser: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al obtener usuario actual: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * Método privado para auto-sincronización (igual que en
     * MantenimientoController)
     */
    private UsuarioMantenimientoModel getOrCreateUsuario(String keycloakId, String username, String email) {
        try {
            // 1. Buscar usuario por Keycloak ID (manejar NoResultException)
            UsuarioMantenimientoModel usuario = null;
            try {
                usuario = usuarioRepository.findByKeycloakId(keycloakId);
            } catch (javax.persistence.NoResultException e) {
                // Usuario no encontrado - normal para nuevos usuarios
                usuario = null;
            }

            if (usuario != null) {
                // Usuario existe - actualizar información si es necesario
                boolean needsUpdate = false;

                if (!username.equals(usuario.getNombreCompleto())) {
                    usuario.setNombreCompleto(username);
                    needsUpdate = true;
                }

                if (email != null && !email.equals(usuario.getCorreo())) {
                    usuario.setCorreo(email);
                    needsUpdate = true;
                }

                if (needsUpdate) {
                    usuarioRepository.save(usuario);
                    System.out.println("✅ Usuario actualizado automáticamente: " + username);
                }

                return usuario;
            }

            // 2. Usuario no existe - AUTO-SINCRONIZACIÓN
            System.out.println("🔄 Auto-sincronizando nuevo usuario: " + username + " (" + keycloakId + ")");

            UsuarioMantenimientoModel nuevoUsuario = new UsuarioMantenimientoModel();
            nuevoUsuario.setKeycloakId(keycloakId);
            nuevoUsuario.setNombreCompleto(username);
            nuevoUsuario.setCorreo(email);
            nuevoUsuario.setActivo(true);

            usuarioRepository.save(nuevoUsuario);

            System.out.println(
                    "✅ Usuario auto-sincronizado exitosamente: " + username + " con ID: " + nuevoUsuario.getId());
            return nuevoUsuario;

        } catch (Exception e) {
            System.err.println("❌ Error en auto-sincronización: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
