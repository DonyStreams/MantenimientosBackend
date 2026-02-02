package usac.eps.controladores.mantenimientos;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.logging.Level;
import java.util.logging.Logger;

import usac.eps.modelos.mantenimientos.ProgramacionMantenimientoModel;
import usac.eps.repositorios.mantenimientos.ProgramacionMantenimientoRepository;
import usac.eps.servicios.mantenimientos.AlertaMantenimientoService;

/**
 * Controlador REST para gestión de alertas y reportes de mantenimiento
 */
@Path("/alertas-mantenimiento")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AlertaMantenimientoController {
    private static final Logger LOGGER = Logger.getLogger(AlertaMantenimientoController.class.getName());

    @Inject
    private ProgramacionMantenimientoRepository programacionRepository;

    @Inject
    private AlertaMantenimientoService alertaService;

    @Context
    private SecurityContext securityContext;

    /**
     * Obtiene el dashboard con resumen de alertas
     */
    @GET
    @Path("/dashboard")
    public Response getDashboard() {
        try {
            // Programaciones activas
            List<ProgramacionMantenimientoModel> activas = programacionRepository
                    .findByActivaOrderByFechaProximoMantenimiento(true);

            // Próximas alertas (7 días)
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 7);
            List<ProgramacionMantenimientoModel> proximas = programacionRepository
                    .findProgramacionesParaAlerta(cal.getTime());

            // Vencidas
            List<ProgramacionMantenimientoModel> vencidas = programacionRepository
                    .findProgramacionesVencidas(new Date());

            // Crear respuesta
            DashboardResponse dashboard = new DashboardResponse();
            dashboard.total_programaciones_activas = activas.size();
            dashboard.total_alertas = proximas.size();
            dashboard.total_vencidas = vencidas.size();
            dashboard.alertas = proximas;
            dashboard.vencidas = vencidas;
            return Response.ok(dashboard).build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en getDashboard", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al obtener dashboard: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Obtiene alertas próximas por días especificados
     */
    @GET
    @Path("/proximas")
    public Response getAlertasProximas(@QueryParam("dias") @DefaultValue("7") Integer dias) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, dias);
            Date fechaLimite = cal.getTime();

            List<ProgramacionMantenimientoModel> programaciones = programacionRepository
                    .findProgramacionesParaAlerta(fechaLimite);

            return Response.ok(programaciones).build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener alertas próximas", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al obtener alertas próximas: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Obtiene mantenimientos vencidos
     */
    @GET
    @Path("/vencidas")
    public Response getMantenimientosVencidos() {
        try {
            Date fechaActual = new Date();
            List<ProgramacionMantenimientoModel> programaciones = programacionRepository
                    .findProgramacionesVencidas(fechaActual);

            return Response.ok(programaciones).build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener mantenimientos vencidos", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al obtener mantenimientos vencidos: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Obtiene alertas por rango de fechas
     */
    @GET
    @Path("/rango")
    public Response getAlertasPorRango(
            @QueryParam("fechaInicio") String fechaInicioStr,
            @QueryParam("fechaFin") String fechaFinStr) {
        try {
            // Validar parámetros requeridos
            if (fechaInicioStr == null || fechaFinStr == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("fechaInicio y fechaFin son requeridos")
                        .build();
            }

            // Parsear fechas (formato: yyyy-MM-dd)
            java.sql.Date fechaInicio = java.sql.Date.valueOf(fechaInicioStr);
            java.sql.Date fechaFin = java.sql.Date.valueOf(fechaFinStr);

            List<ProgramacionMantenimientoModel> programaciones = programacionRepository
                    .findProgramacionesEntreFechas(fechaInicio, fechaFin);

            return Response.ok(programaciones).build();

        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Formato de fecha inválido en rango", e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Formato de fecha inválido. Use yyyy-MM-dd")
                    .build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener alertas por rango", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al obtener alertas por rango: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Obtiene reporte de estado completo
     */
    @GET
    @Path("/reporte")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getReporteEstado() {
        try {
            String reporte = alertaService.generarReporteEstado();
            return Response.ok(reporte).build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al generar reporte de alertas", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al generar reporte: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Ejecuta manualmente la revisión de alertas
     */
    @POST
    @Path("/revisar-alertas")
    public Response revisarAlertasManual() {
        try {
            alertaService.revisarAlertasMantenimiento();
            return Response.ok().entity("Revisión de alertas ejecutada exitosamente").build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al revisar alertas", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al revisar alertas: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Ejecuta manualmente la revisión de vencidos
     */
    @POST
    @Path("/revisar-vencidos")
    public Response revisarVencidosManual() {
        try {
            alertaService.revisarMantenimientosVencidos();
            return Response.ok().entity("Revisión de vencidos ejecutada exitosamente").build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al revisar vencidos", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al revisar vencidos: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Clase interna para respuesta del dashboard
     */
    public static class DashboardResponse {
        public int total_programaciones_activas;
        public int total_alertas;
        public int total_vencidas;
        public List<ProgramacionMantenimientoModel> alertas;
        public List<ProgramacionMantenimientoModel> vencidas;

        public DashboardResponse() {
        }
    }
}
