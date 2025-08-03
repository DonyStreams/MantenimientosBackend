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

import usac.eps.modelos.mantenimientos.ProgramacionMantenimientoModel;
import usac.eps.modelos.mantenimientos.EquipoModel;
import usac.eps.modelos.mantenimientos.TipoMantenimientoModel;
import usac.eps.repositorios.mantenimientos.ProgramacionMantenimientoRepository;
import usac.eps.repositorios.mantenimientos.EquipoRepository;
import usac.eps.repositorios.mantenimientos.TipoMantenimientoRepository;

/**
 * Controlador REST para gestión de programaciones de mantenimiento
 */
@Path("/programaciones")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProgramacionMantenimientoController {

    @Inject
    private ProgramacionMantenimientoRepository programacionRepository;

    @Inject
    private EquipoRepository equipoRepository;

    @Inject
    private TipoMantenimientoRepository tipoMantenimientoRepository;

    @Context
    private SecurityContext securityContext;

    /**
     * Obtiene todas las programaciones activas
     */
    @GET
    public Response getAllProgramaciones() {
        try {
            List<ProgramacionMantenimientoModel> programaciones = programacionRepository
                    .findByActivaOrderByFechaProximoMantenimiento(true);
            return Response.ok(programaciones).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al obtener programaciones: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Obtiene una programación por ID
     */
    @GET
    @Path("/{id}")
    public Response getProgramacion(@PathParam("id") Integer id) {
        try {
            ProgramacionMantenimientoModel programacion = programacionRepository.findBy(id);
            if (programacion == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Programación no encontrada")
                        .build();
            }
            return Response.ok(programacion).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al obtener programación: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Crea una nueva programación
     */
    @POST
    public Response createProgramacion(ProgramacionMantenimientoModel programacion) {
        try {
            // Validaciones básicas
            if (programacion.getEquipo() == null || programacion.getTipoMantenimiento() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Equipo y tipo de mantenimiento son requeridos")
                        .build();
            }

            if (programacion.getFrecuenciaDias() == null || programacion.getFrecuenciaDias() <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Frecuencia en días debe ser mayor a 0")
                        .build();
            }

            // Verificar que el equipo existe
            EquipoModel equipo = equipoRepository.findBy(programacion.getEquipo().getIdEquipo());
            if (equipo == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Equipo no encontrado")
                        .build();
            }

            // Verificar que el tipo de mantenimiento existe
            TipoMantenimientoModel tipo = tipoMantenimientoRepository
                    .findBy(programacion.getTipoMantenimiento().getIdTipo());
            if (tipo == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Tipo de mantenimiento no encontrado")
                        .build();
            }

            // Verificar que no existe una programación activa para el mismo equipo y tipo
            ProgramacionMantenimientoModel existente = programacionRepository
                    .findByEquipoAndTipoMantenimientoAndActiva(equipo, tipo, true);
            if (existente != null) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("Ya existe una programación activa para este equipo y tipo de mantenimiento")
                        .build();
            }

            // Establecer referencias completas
            programacion.setEquipo(equipo);
            programacion.setTipoMantenimiento(tipo);

            // Calcular próximo mantenimiento si no se especificó fecha del último
            if (programacion.getFechaUltimoMantenimiento() == null) {
                programacion.setFechaUltimoMantenimiento(new Date());
            }
            programacion.calcularProximoMantenimiento();

            // Valores por defecto
            if (programacion.getDiasAlertaPrevia() == null) {
                programacion.setDiasAlertaPrevia(7);
            }
            if (programacion.getActiva() == null) {
                programacion.setActiva(true);
            }

            ProgramacionMantenimientoModel saved = programacionRepository.save(programacion);
            return Response.status(Response.Status.CREATED).entity(saved).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al crear programación: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Actualiza una programación existente
     */
    @PUT
    @Path("/{id}")
    public Response updateProgramacion(@PathParam("id") Integer id, ProgramacionMantenimientoModel programacion) {
        try {
            ProgramacionMantenimientoModel existing = programacionRepository.findBy(id);
            if (existing == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Programación no encontrada")
                        .build();
            }

            // Actualizar campos
            if (programacion.getFrecuenciaDias() != null) {
                existing.setFrecuenciaDias(programacion.getFrecuenciaDias());
            }
            if (programacion.getFechaUltimoMantenimiento() != null) {
                existing.setFechaUltimoMantenimiento(programacion.getFechaUltimoMantenimiento());
            }
            if (programacion.getDiasAlertaPrevia() != null) {
                existing.setDiasAlertaPrevia(programacion.getDiasAlertaPrevia());
            }
            if (programacion.getActiva() != null) {
                existing.setActiva(programacion.getActiva());
            }
            if (programacion.getObservaciones() != null) {
                existing.setObservaciones(programacion.getObservaciones());
            }

            // Recalcular próximo mantenimiento
            existing.calcularProximoMantenimiento();
            existing.setFechaModificacion(new Date());

            ProgramacionMantenimientoModel updated = programacionRepository.save(existing);
            return Response.ok(updated).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al actualizar programación: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Desactiva una programación
     */
    @DELETE
    @Path("/{id}")
    public Response deleteProgramacion(@PathParam("id") Integer id) {
        try {
            ProgramacionMantenimientoModel programacion = programacionRepository.findBy(id);
            if (programacion == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Programación no encontrada")
                        .build();
            }

            // No eliminar físicamente, solo desactivar
            programacion.setActiva(false);
            programacion.setFechaModificacion(new Date());
            programacionRepository.save(programacion);

            return Response.ok().entity("Programación desactivada exitosamente").build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al desactivar programación: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Obtiene programaciones por equipo
     */
    @GET
    @Path("/equipo/{equipoId}")
    public Response getProgramacionesByEquipo(@PathParam("equipoId") Integer equipoId) {
        try {
            EquipoModel equipo = equipoRepository.findBy(equipoId);
            if (equipo == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Equipo no encontrado")
                        .build();
            }

            List<ProgramacionMantenimientoModel> programaciones = programacionRepository
                    .findByEquipoAndActivaOrderByFechaProximoMantenimiento(equipo, true);
            return Response.ok(programaciones).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al obtener programaciones del equipo: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Obtiene programaciones que requieren alerta
     */
    @GET
    @Path("/alertas")
    public Response getProgramacionesConAlerta(@QueryParam("dias") @DefaultValue("7") Integer dias) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, dias);
            Date fechaLimite = cal.getTime();

            List<ProgramacionMantenimientoModel> programaciones = programacionRepository
                    .findProgramacionesParaAlerta(fechaLimite);
            return Response.ok(programaciones).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al obtener alertas: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Obtiene programaciones vencidas
     */
    @GET
    @Path("/vencidas")
    public Response getProgramacionesVencidas() {
        try {
            Date fechaActual = new Date();
            List<ProgramacionMantenimientoModel> programaciones = programacionRepository
                    .findProgramacionesVencidas(fechaActual);
            return Response.ok(programaciones).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al obtener programaciones vencidas: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Actualiza la fecha del último mantenimiento después de una ejecución
     */
    @POST
    @Path("/{id}/actualizar-ultimo-mantenimiento")
    public Response actualizarUltimoMantenimiento(@PathParam("id") Integer id) {
        try {
            ProgramacionMantenimientoModel programacion = programacionRepository.findBy(id);
            if (programacion == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Programación no encontrada")
                        .build();
            }

            Date ahora = new Date();
            programacion.setFechaUltimoMantenimiento(ahora);
            programacion.calcularProximoMantenimiento();
            programacion.setFechaModificacion(ahora);

            ProgramacionMantenimientoModel updated = programacionRepository.save(programacion);
            return Response.ok(updated).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al actualizar último mantenimiento: " + e.getMessage())
                    .build();
        }
    }
}
