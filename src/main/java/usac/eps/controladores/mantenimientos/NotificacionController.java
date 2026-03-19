package usac.eps.controladores.mantenimientos;

import usac.eps.modelos.mantenimientos.NotificacionModel;
import usac.eps.modelos.mantenimientos.ConfiguracionAlertaModel;
import usac.eps.modelos.mantenimientos.UsuarioMantenimientoModel;
import usac.eps.servicios.mantenimientos.NotificacionService;
import usac.eps.servicios.mantenimientos.NotificacionScheduler;
import usac.eps.repositorios.mantenimientos.NotificacionRepository;
import usac.eps.repositorios.mantenimientos.ConfiguracionAlertaRepository;
import usac.eps.repositorios.mantenimientos.UsuarioMantenimientoRepository;
import usac.eps.seguridad.RequiresRole;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador REST para gestión de notificaciones del sistema
 * Incluye endpoints para CRUD, verificación manual y configuración del
 * scheduler
 */
@Path("/notificaciones")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NotificacionController {

    private static final Logger LOGGER = Logger.getLogger(NotificacionController.class.getName());

    @Inject
    private NotificacionService notificacionService;

    @Inject
    private NotificacionRepository notificacionRepository;

    @Inject
    private ConfiguracionAlertaRepository configuracionRepository;

    @Inject
    private NotificacionScheduler scheduler;

    @Inject
    private UsuarioMantenimientoRepository usuarioRepository;

    @Context
    private HttpServletRequest httpRequest;

    // ==================== NOTIFICACIONES ====================

    /**
     * Obtiene todas las notificaciones
     */
    @GET
    public Response listarNotificaciones() {
        try {
            List<NotificacionModel> notificaciones = notificacionService.obtenerTodas();
            return Response.ok(notificaciones).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al listar notificaciones", e);
            return Response.serverError()
                    .entity(Map.of("error", "Error al obtener notificaciones: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Obtiene solo notificaciones no leídas
     */
    @GET
    @Path("/no-leidas")
    public Response listarNoLeidas() {
        try {
            List<NotificacionModel> notificaciones = notificacionService.obtenerNoLeidas();
            return Response.ok(notificaciones).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al listar notificaciones no leídas", e);
            return Response.serverError()
                    .entity(Map.of("error", "Error al obtener notificaciones: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Obtiene contadores de notificaciones para badges
     */
    @GET
    @Path("/contadores")
    public Response obtenerContadores() {
        try {
            Map<String, Long> contadores = notificacionService.obtenerContadores();
            return Response.ok(contadores).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener contadores", e);
            return Response.serverError()
                    .entity(Map.of("error", "Error al obtener contadores: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Obtiene notificaciones por tipo
     */
    @GET
    @Path("/tipo/{tipo}")
    public Response listarPorTipo(@PathParam("tipo") String tipo) {
        try {
            List<NotificacionModel> notificaciones = notificacionRepository.findByTipo(tipo);
            return Response.ok(notificaciones).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al listar notificaciones por tipo", e);
            return Response.serverError()
                    .entity(Map.of("error", "Error al obtener notificaciones: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Obtiene una notificación por ID
     */
    @GET
    @Path("/{id}")
    public Response obtenerPorId(@PathParam("id") Integer id) {
        try {
            NotificacionModel notificacion = notificacionRepository.findById(id);
            if (notificacion == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Notificación no encontrada"))
                        .build();
            }
            return Response.ok(notificacion).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener notificación", e);
            return Response.serverError()
                    .entity(Map.of("error", "Error al obtener notificación: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Marca una notificación como leída
     */
    @PUT
    @Path("/{id}/leer")
    public Response marcarComoLeida(@PathParam("id") Integer id) {
        try {
            NotificacionModel notificacion = notificacionService.marcarComoLeida(id);
            if (notificacion == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Notificación no encontrada"))
                        .build();
            }
            return Response.ok(notificacion).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al marcar notificación como leída", e);
            return Response.serverError()
                    .entity(Map.of("error", "Error al actualizar notificación: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Marca todas las notificaciones como leídas
     */
    @PUT
    @Path("/leer-todas")
    public Response marcarTodasComoLeidas() {
        try {
            int actualizadas = notificacionService.marcarTodasComoLeidas();
            return Response.ok(Map.of(
                    "mensaje", "Notificaciones actualizadas",
                    "cantidad", actualizadas)).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al marcar todas como leídas", e);
            return Response.serverError()
                    .entity(Map.of("error", "Error al actualizar notificaciones: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Elimina una notificación
     */
    @DELETE
    @Path("/{id}")
    @RequiresRole({ "ADMIN" })
    public Response eliminar(@PathParam("id") Integer id) {
        try {
            notificacionRepository.delete(id);
            return Response.ok(Map.of("mensaje", "Notificación eliminada")).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar notificación", e);
            return Response.serverError()
                    .entity(Map.of("error", "Error al eliminar notificación: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Elimina TODAS las notificaciones
     */
    @DELETE
    @Path("/todas")
    @RequiresRole({ "ADMIN" })
    public Response eliminarTodas() {
        try {
            int eliminadas = notificacionRepository.eliminarTodas();
            LOGGER.info("🗑️ Eliminadas " + eliminadas + " notificaciones");
            return Response.ok(Map.of(
                    "mensaje", "Todas las notificaciones eliminadas",
                    "cantidad", eliminadas)).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar todas las notificaciones", e);
            return Response.serverError()
                    .entity(Map.of("error", "Error al eliminar notificaciones: " + e.getMessage()))
                    .build();
        }
    }

    // ==================== SCHEDULER ====================

    /**
     * Obtiene estado actual del scheduler
     */
    @GET
    @Path("/scheduler/estado")
    public Response obtenerEstadoScheduler() {
        try {
            Map<String, Object> estado = scheduler.getEstado();
            return Response.ok(estado).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener estado del scheduler", e);
            return Response.serverError()
                    .entity(Map.of("error", "Error al obtener estado: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Ejecuta verificación manual de alertas
     */
    @POST
    @Path("/scheduler/ejecutar")
    @RequiresRole({ "ADMIN", "SUPERVISOR" })
    public Response ejecutarVerificacionManual() {
        try {
            LOGGER.info("🔄 Ejecutando verificación manual solicitada via API...");
            Map<String, Object> resultado = scheduler.ejecutarVerificacionManual();
            return Response.ok(resultado).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al ejecutar verificación manual", e);
            return Response.serverError()
                    .entity(Map.of("error", "Error al ejecutar verificación: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Habilita o deshabilita el scheduler
     */
    @PUT
    @Path("/scheduler/habilitar/{estado}")
    @RequiresRole({ "ADMIN", "SUPERVISOR" })
    public Response habilitarScheduler(@PathParam("estado") boolean estado) {
        try {
            scheduler.setSchedulerHabilitado(estado);
            return Response.ok(Map.of(
                    "mensaje", estado ? "Scheduler habilitado" : "Scheduler deshabilitado",
                    "habilitado", estado)).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al cambiar estado del scheduler", e);
            return Response.serverError()
                    .entity(Map.of("error", "Error al cambiar estado: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Actualiza hora de ejecución del scheduler
     */
    @PUT
    @Path("/scheduler/horario")
    @RequiresRole({ "ADMIN", "SUPERVISOR" })
    public Response actualizarHorario(Map<String, Integer> horario) {
        try {
            if (horario.containsKey("hora")) {
                scheduler.setHoraEjecucion(horario.get("hora"));
            }
            if (horario.containsKey("minuto")) {
                scheduler.setMinutoEjecucion(horario.get("minuto"));
            }
            return Response.ok(Map.of(
                    "mensaje", "Horario actualizado",
                    "hora", scheduler.getHoraEjecucion(),
                    "minuto", scheduler.getMinutoEjecucion())).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar horario", e);
            return Response.serverError()
                    .entity(Map.of("error", "Error al actualizar horario: " + e.getMessage()))
                    .build();
        }
    }

    // ==================== CONFIGURACIÓN DE ALERTAS ====================

    /**
     * Obtiene todas las configuraciones de alertas
     */
    @GET
    @Path("/configuracion")
    public Response listarConfiguraciones() {
        try {
            List<ConfiguracionAlertaModel> configuraciones = configuracionRepository.findAll();
            return Response.ok(configuraciones).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al listar configuraciones", e);
            return Response.serverError()
                    .entity(Map.of("error", "Error al obtener configuraciones: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Obtiene una configuración por ID
     */
    @GET
    @Path("/configuracion/{id}")
    public Response obtenerConfiguracion(@PathParam("id") Integer id) {
        try {
            ConfiguracionAlertaModel config = configuracionRepository.findById(id);
            if (config == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Configuración no encontrada"))
                        .build();
            }
            return Response.ok(config).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener configuración", e);
            return Response.serverError()
                    .entity(Map.of("error", "Error al obtener configuración: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Actualiza una configuración de alerta
     */
    @PUT
    @Path("/configuracion/{id}")
    @RequiresRole({ "ADMIN", "SUPERVISOR" })
    public Response actualizarConfiguracion(@PathParam("id") Integer id, ConfiguracionAlertaModel config) {
        try {
            ConfiguracionAlertaModel existente = configuracionRepository.findById(id);
            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Configuración no encontrada"))
                        .build();
            }

            existente.setNombre(config.getNombre());
            existente.setDescripcion(config.getDescripcion());
            existente.setDiasAnticipacion(config.getDiasAnticipacion());
            existente.setActiva(config.getActiva());
            existente.setUsuariosNotificar(config.getUsuariosNotificar());
            // Actualizar tipoAlerta solo si no es un tipo de sistema protegido
            if (config.getTipoAlerta() != null
                    && !config.getTipoAlerta().contains("vencido")
                    && !config.getTipoAlerta().contains("critico")
                    && !"scheduler_config".equals(config.getTipoAlerta())) {
                existente.setTipoAlerta(config.getTipoAlerta());
            }

            ConfiguracionAlertaModel actualizada = configuracionRepository.save(existente);
            return Response.ok(actualizada).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar configuración", e);
            return Response.serverError()
                    .entity(Map.of("error", "Error al actualizar configuración: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Elimina una configuración de alerta
     */
    @DELETE
    @Path("/configuracion/{id}")
    @RequiresRole({ "ADMIN", "SUPERVISOR" })
    public Response eliminarConfiguracion(@PathParam("id") Integer id) {
        try {
            ConfiguracionAlertaModel existente = configuracionRepository.findById(id);
            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Configuración no encontrada"))
                        .build();
            }
            if ("scheduler_config".equals(existente.getTipoAlerta()) || existente.getTipoAlerta().contains("vencido") || existente.getTipoAlerta().contains("critico")) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(Map.of("error", "No se puede eliminar esta configuración del sistema"))
                        .build();
            }
            configuracionRepository.delete(id);
            return Response.ok(Map.of("mensaje", "Configuración eliminada")).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar configuración", e);
            return Response.serverError()
                    .entity(Map.of("error", "Error al eliminar configuración: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Crea una nueva configuración de alerta
     */
    @POST
    @Path("/configuracion")
    @RequiresRole({ "ADMIN", "SUPERVISOR" })
    public Response crearConfiguracion(ConfiguracionAlertaModel config) {
        try {
            String keycloakId = (String) httpRequest.getAttribute("keycloakId");
            if (keycloakId != null) {
                UsuarioMantenimientoModel usuario = usuarioRepository.findByKeycloakId(keycloakId);
                if (usuario != null) {
                    config.setUsuarioCreacion(usuario.getId());
                }
            }
            ConfiguracionAlertaModel nueva = configuracionRepository.save(config);
            return Response.status(Response.Status.CREATED).entity(nueva).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al crear configuración", e);
            return Response.serverError()
                    .entity(Map.of("error", "Error al crear configuración: " + e.getMessage()))
                    .build();
        }
    }

    // ==================== UTILIDADES ====================

    /**
     * Limpia notificaciones antiguas leídas
     */
    @DELETE
    @Path("/limpiar/{dias}")
    public Response limpiarAntiguedad(@PathParam("dias") int dias) {
        try {
            int eliminadas = notificacionService.limpiarNotificacionesAntiguas(dias);
            return Response.ok(Map.of(
                    "mensaje", "Limpieza completada",
                    "notificacionesEliminadas", eliminadas)).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al limpiar notificaciones", e);
            return Response.serverError()
                    .entity(Map.of("error", "Error al limpiar: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Obtiene resumen completo para el dashboard
     */
    @GET
    @Path("/dashboard")
    public Response obtenerDashboard() {
        try {
            Map<String, Object> dashboard = new HashMap<>();

            // Contadores
            dashboard.put("contadores", notificacionService.obtenerContadores());

            // Estado del scheduler
            dashboard.put("scheduler", scheduler.getEstado());

            // Últimas 10 notificaciones
            List<NotificacionModel> recientes = notificacionRepository.findRecientes(7);
            if (recientes.size() > 10) {
                recientes = recientes.subList(0, 10);
            }
            dashboard.put("recientes", recientes);

            // Configuraciones activas
            dashboard.put("configuraciones", configuracionRepository.findActivas());

            return Response.ok(dashboard).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener dashboard", e);
            return Response.serverError()
                    .entity(Map.of("error", "Error al obtener dashboard: " + e.getMessage()))
                    .build();
        }
    }
}
