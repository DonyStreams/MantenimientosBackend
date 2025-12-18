package usac.eps.controladores.mantenimientos;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.servlet.http.HttpServletRequest;

import usac.eps.controladores.mantenimientos.dto.EjecucionMantenimientoDTO;
import usac.eps.controladores.mantenimientos.mapper.EjecucionMantenimientoMapper;
import usac.eps.modelos.mantenimientos.ContratoModel;
import usac.eps.modelos.mantenimientos.EjecucionMantenimientoModel;
import usac.eps.modelos.mantenimientos.EquipoModel;
import usac.eps.modelos.mantenimientos.ProgramacionMantenimientoModel;
import usac.eps.modelos.mantenimientos.TipoMantenimientoModel;
import usac.eps.modelos.mantenimientos.UsuarioMantenimientoModel;
import usac.eps.repositorios.mantenimientos.ProgramacionMantenimientoRepository;
import usac.eps.repositorios.mantenimientos.EquipoRepository;
import usac.eps.repositorios.mantenimientos.TipoMantenimientoRepository;
import usac.eps.repositorios.mantenimientos.EjecucionMantenimientoRepository;
import usac.eps.repositorios.mantenimientos.ContratoRepository;
import usac.eps.repositorios.mantenimientos.UsuarioMantenimientoRepository;

/**
 * Controlador REST para gestión de programaciones de mantenimiento
 */
@Path("/programaciones")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProgramacionMantenimientoController {

    @PersistenceContext(unitName = "usac.eps_ControlSuministros")
    private EntityManager em;

    @Inject
    private ProgramacionMantenimientoRepository programacionRepository;

    @Inject
    private EquipoRepository equipoRepository;

    @Inject
    private TipoMantenimientoRepository tipoMantenimientoRepository;

    @Inject
    private EjecucionMantenimientoRepository ejecucionRepository;

    @Inject
    private ContratoRepository contratoRepository;

    @Inject
    private UsuarioMantenimientoRepository usuarioRepository;

    @Context
    private SecurityContext securityContext;

    @Context
    private HttpServletRequest request;

    /**
     * Obtiene todas las programaciones con datos relacionados
     */
    @GET
    public Response getAllProgramaciones() {
        try {
            // Obtener TODAS las programaciones para la gestión
            List<ProgramacionMantenimientoModel> programaciones = programacionRepository.findAll();

            // Construir respuesta JSON manualmente para incluir datos relacionados
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("[");

            for (int i = 0; i < programaciones.size(); i++) {
                ProgramacionMantenimientoModel p = programaciones.get(i);
                if (i > 0)
                    jsonBuilder.append(",");

                jsonBuilder.append("{");
                jsonBuilder.append("\"idProgramacion\":").append(p.getIdProgramacion()).append(",");
                jsonBuilder.append("\"frecuenciaDias\":").append(p.getFrecuenciaDias()).append(",");
                jsonBuilder.append("\"diasAlertaPrevia\":")
                        .append(p.getDiasAlertaPrevia() != null ? p.getDiasAlertaPrevia() : 7).append(",");
                jsonBuilder.append("\"activa\":").append(p.getActiva()).append(",");
                jsonBuilder.append("\"observaciones\":\"").append(escapeJson(p.getObservaciones())).append("\",");

                // Fechas
                jsonBuilder.append("\"fechaUltimoMantenimiento\":")
                        .append(p.getFechaUltimoMantenimiento() != null
                                ? "\"" + formatDate(p.getFechaUltimoMantenimiento()) + "\""
                                : "null")
                        .append(",");
                jsonBuilder.append("\"fechaProximoMantenimiento\":")
                        .append(p.getFechaProximoMantenimiento() != null
                                ? "\"" + formatDate(p.getFechaProximoMantenimiento()) + "\""
                                : "null")
                        .append(",");

                // Equipo
                jsonBuilder.append("\"equipo\":");
                if (p.getEquipo() != null) {
                    jsonBuilder.append("{");
                    jsonBuilder.append("\"idEquipo\":").append(p.getEquipo().getIdEquipo()).append(",");
                    jsonBuilder.append("\"nombre\":\"").append(escapeJson(p.getEquipo().getNombre())).append("\",");
                    jsonBuilder.append("\"codigoInacif\":\"").append(escapeJson(p.getEquipo().getCodigoInacif()))
                            .append("\",");
                    jsonBuilder.append("\"ubicacion\":\"").append(escapeJson(p.getEquipo().getUbicacion()))
                            .append("\"");
                    jsonBuilder.append("}");
                } else {
                    jsonBuilder.append("null");
                }
                jsonBuilder.append(",");

                // Tipo de Mantenimiento
                jsonBuilder.append("\"tipoMantenimiento\":");
                if (p.getTipoMantenimiento() != null) {
                    jsonBuilder.append("{");
                    jsonBuilder.append("\"idTipo\":").append(p.getTipoMantenimiento().getIdTipo()).append(",");
                    jsonBuilder.append("\"nombre\":\"").append(escapeJson(p.getTipoMantenimiento().getNombre()))
                            .append("\"");
                    jsonBuilder.append("}");
                } else {
                    jsonBuilder.append("null");
                }
                jsonBuilder.append(",");

                // Contrato
                jsonBuilder.append("\"contrato\":");
                if (p.getContrato() != null) {
                    jsonBuilder.append("{");
                    jsonBuilder.append("\"idContrato\":").append(p.getContrato().getIdContrato()).append(",");
                    jsonBuilder.append("\"descripcion\":\"").append(escapeJson(p.getContrato().getDescripcion()))
                            .append("\",");
                    // Proveedor del contrato
                    jsonBuilder.append("\"proveedor\":");
                    if (p.getContrato().getProveedor() != null) {
                        jsonBuilder.append("{");
                        jsonBuilder.append("\"idProveedor\":").append(p.getContrato().getProveedor().getIdProveedor())
                                .append(",");
                        jsonBuilder.append("\"nombre\":\"")
                                .append(escapeJson(p.getContrato().getProveedor().getNombre())).append("\"");
                        jsonBuilder.append("}");
                    } else {
                        jsonBuilder.append("null");
                    }
                    jsonBuilder.append(",");
                    jsonBuilder.append("\"fechaInicio\":")
                            .append(p.getContrato().getFechaInicio() != null
                                    ? "\"" + formatDate(p.getContrato().getFechaInicio()) + "\""
                                    : "null")
                            .append(",");
                    jsonBuilder.append("\"fechaFin\":")
                            .append(p.getContrato().getFechaFin() != null
                                    ? "\"" + formatDate(p.getContrato().getFechaFin()) + "\""
                                    : "null");
                    jsonBuilder.append("}");
                } else {
                    jsonBuilder.append("null");
                }

                jsonBuilder.append("}");
            }

            jsonBuilder.append("]");

            return Response.ok(jsonBuilder.toString()).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Error al obtener programaciones: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    // Helpers para formateo
    private String escapeJson(String text) {
        if (text == null)
            return "";
        return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    private String formatDate(java.util.Date date) {
        if (date == null)
            return null;
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    /**
     * Obtiene estadísticas de programaciones
     */
    @GET
    @Path("/estadisticas")
    public Response getEstadisticas() {
        try {
            List<ProgramacionMantenimientoModel> todas = programacionRepository.findAll();

            int total = todas.size();
            int activas = 0;
            int proximas = 0;
            int vencidas = 0;

            Date hoy = new Date();
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, 7);
            Date enSieteDias = cal.getTime();

            for (ProgramacionMantenimientoModel p : todas) {
                if (p.getActiva()) {
                    activas++;

                    if (p.getFechaProximoMantenimiento() != null) {
                        if (p.getFechaProximoMantenimiento().before(hoy)) {
                            vencidas++;
                        } else if (p.getFechaProximoMantenimiento().before(enSieteDias)) {
                            proximas++;
                        }
                    }
                }
            }

            Map<String, Integer> stats = new HashMap<>();
            stats.put("total", total);
            stats.put("activas", activas);
            stats.put("proximas", proximas);
            stats.put("vencidas", vencidas);

            return Response.ok(stats).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al obtener estadísticas: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Obtiene una programación por ID con datos relacionados
     */
    @GET
    @Path("/{id}")
    public Response getProgramacion(@PathParam("id") Integer id) {
        try {
            ProgramacionMantenimientoModel p = programacionRepository.findBy(id);
            if (p == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Programación no encontrada\"}")
                        .build();
            }

            // Construir respuesta JSON manualmente para incluir datos relacionados
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{");
            jsonBuilder.append("\"idProgramacion\":").append(p.getIdProgramacion()).append(",");
            jsonBuilder.append("\"frecuenciaDias\":").append(p.getFrecuenciaDias()).append(",");
            jsonBuilder.append("\"diasAlertaPrevia\":")
                    .append(p.getDiasAlertaPrevia() != null ? p.getDiasAlertaPrevia() : 7).append(",");
            jsonBuilder.append("\"activa\":").append(p.getActiva()).append(",");
            jsonBuilder.append("\"observaciones\":\"").append(escapeJson(p.getObservaciones())).append("\",");

            // Fechas
            jsonBuilder.append("\"fechaUltimoMantenimiento\":")
                    .append(p.getFechaUltimoMantenimiento() != null
                            ? "\"" + formatDate(p.getFechaUltimoMantenimiento()) + "\""
                            : "null")
                    .append(",");
            jsonBuilder.append("\"fechaProximoMantenimiento\":")
                    .append(p.getFechaProximoMantenimiento() != null
                            ? "\"" + formatDate(p.getFechaProximoMantenimiento()) + "\""
                            : "null")
                    .append(",");

            // Equipo
            jsonBuilder.append("\"equipo\":");
            if (p.getEquipo() != null) {
                jsonBuilder.append("{");
                jsonBuilder.append("\"idEquipo\":").append(p.getEquipo().getIdEquipo()).append(",");
                jsonBuilder.append("\"nombre\":\"").append(escapeJson(p.getEquipo().getNombre())).append("\",");
                jsonBuilder.append("\"codigoInacif\":\"").append(escapeJson(p.getEquipo().getCodigoInacif()))
                        .append("\",");
                jsonBuilder.append("\"ubicacion\":\"").append(escapeJson(p.getEquipo().getUbicacion())).append("\"");
                jsonBuilder.append("}");
            } else {
                jsonBuilder.append("null");
            }
            jsonBuilder.append(",");

            // Tipo de Mantenimiento
            jsonBuilder.append("\"tipoMantenimiento\":");
            if (p.getTipoMantenimiento() != null) {
                jsonBuilder.append("{");
                jsonBuilder.append("\"idTipo\":").append(p.getTipoMantenimiento().getIdTipo()).append(",");
                jsonBuilder.append("\"nombre\":\"").append(escapeJson(p.getTipoMantenimiento().getNombre()))
                        .append("\"");
                jsonBuilder.append("}");
            } else {
                jsonBuilder.append("null");
            }
            jsonBuilder.append(",");

            // Contrato
            jsonBuilder.append("\"contrato\":");
            if (p.getContrato() != null) {
                jsonBuilder.append("{");
                jsonBuilder.append("\"idContrato\":").append(p.getContrato().getIdContrato()).append(",");
                jsonBuilder.append("\"descripcion\":\"").append(escapeJson(p.getContrato().getDescripcion()))
                        .append("\",");
                // Proveedor del contrato
                jsonBuilder.append("\"proveedor\":");
                if (p.getContrato().getProveedor() != null) {
                    jsonBuilder.append("{");
                    jsonBuilder.append("\"idProveedor\":").append(p.getContrato().getProveedor().getIdProveedor())
                            .append(",");
                    jsonBuilder.append("\"nombre\":\"").append(escapeJson(p.getContrato().getProveedor().getNombre()))
                            .append("\"");
                    jsonBuilder.append("}");
                } else {
                    jsonBuilder.append("null");
                }
                jsonBuilder.append(",");
                jsonBuilder.append("\"fechaInicio\":")
                        .append(p.getContrato().getFechaInicio() != null
                                ? "\"" + formatDate(p.getContrato().getFechaInicio()) + "\""
                                : "null")
                        .append(",");
                jsonBuilder.append("\"fechaFin\":")
                        .append(p.getContrato().getFechaFin() != null
                                ? "\"" + formatDate(p.getContrato().getFechaFin()) + "\""
                                : "null");
                jsonBuilder.append("}");
            } else {
                jsonBuilder.append("null");
            }

            jsonBuilder.append("}");

            return Response.ok(jsonBuilder.toString()).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Error al obtener programación: " + e.getMessage() + "\"}")
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
            System.out.println("🔍 Buscando equipo con ID: " + programacion.getEquipo().getIdEquipo());
            EquipoModel equipo = null;
            try {
                equipo = equipoRepository.findBy(programacion.getEquipo().getIdEquipo());
            } catch (Exception e) {
                System.out.println("❌ Error buscando equipo: " + e.getMessage());
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Equipo con ID " + programacion.getEquipo().getIdEquipo() + " no encontrado")
                        .build();
            }
            if (equipo == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Equipo no encontrado")
                        .build();
            }
            System.out.println("✅ Equipo encontrado: " + equipo.getNombre());

            // Verificar que el tipo de mantenimiento existe
            System.out.println(
                    "🔍 Buscando tipo de mantenimiento con ID: " + programacion.getTipoMantenimiento().getIdTipo());
            TipoMantenimientoModel tipo = null;
            try {
                tipo = tipoMantenimientoRepository.findBy(programacion.getTipoMantenimiento().getIdTipo());
            } catch (Exception e) {
                System.out.println("❌ Error buscando tipo de mantenimiento: " + e.getMessage());
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Tipo de mantenimiento con ID " + programacion.getTipoMantenimiento().getIdTipo()
                                + " no encontrado")
                        .build();
            }
            if (tipo == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Tipo de mantenimiento no encontrado")
                        .build();
            }
            System.out.println("✅ Tipo de mantenimiento encontrado: " + tipo.getNombre());

            // Verificar que el contrato existe
            if (programacion.getContrato() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Contrato es requerido")
                        .build();
            }
            System.out.println("🔍 Buscando contrato con ID: " + programacion.getContrato().getIdContrato());
            ContratoModel contrato = null;
            try {
                contrato = contratoRepository.findBy(programacion.getContrato().getIdContrato());
            } catch (Exception e) {
                System.out.println("❌ Error buscando contrato: " + e.getMessage());
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Contrato con ID " + programacion.getContrato().getIdContrato() + " no encontrado")
                        .build();
            }
            if (contrato == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Contrato no encontrado")
                        .build();
            }
            System.out.println("✅ Contrato encontrado: " + contrato.getDescripcion());

            // Verificar que no existe una programación activa para el mismo equipo y tipo
            System.out.println("🔍 Verificando si ya existe programación activa para este equipo y tipo...");
            ProgramacionMantenimientoModel existente = null;
            try {
                existente = programacionRepository
                        .findByEquipoAndTipoMantenimientoAndActiva(equipo, tipo, true);
            } catch (Exception e) {
                // No existe programación previa, esto es normal
                System.out.println("✅ No existe programación previa (esto es correcto)");
            }
            if (existente != null) {
                System.out.println("⚠️ Ya existe una programación activa");
                return Response.status(Response.Status.CONFLICT)
                        .entity("Ya existe una programación activa para este equipo y tipo de mantenimiento")
                        .build();
            }

            // Establecer referencias completas
            programacion.setEquipo(equipo);
            programacion.setTipoMantenimiento(tipo);
            programacion.setContrato(contrato);

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

            // Auditoría
            programacion.setFechaCreacion(new Date());
            asignarUsuarioAuditoria(programacion, true);

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

            // Actualizar contrato si viene en la petición
            if (programacion.getContrato() != null) {
                ContratoModel contrato = contratoRepository.findBy(programacion.getContrato().getIdContrato());
                if (contrato != null) {
                    existing.setContrato(contrato);
                }
            }

            // Recalcular próximo mantenimiento
            existing.calcularProximoMantenimiento();

            // Auditoría
            existing.setFechaModificacion(new Date());
            asignarUsuarioAuditoria(existing, false);

            ProgramacionMantenimientoModel updated = programacionRepository.save(existing);
            return Response.ok(updated).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al actualizar programación: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Alternar estado activa/inactiva
     */
    @PATCH
    @Path("/{id}/toggle-activa")
    public Response toggleActiva(@PathParam("id") Integer id, Map<String, Boolean> body) {
        try {
            ProgramacionMantenimientoModel programacion = programacionRepository.findBy(id);
            if (programacion == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Programación no encontrada")
                        .build();
            }

            Boolean activa = body.get("activa");
            if (activa != null) {
                programacion.setActiva(activa);
                programacion.setFechaModificacion(new Date());
                asignarUsuarioAuditoria(programacion, false);
                programacionRepository.save(programacion);
            }

            return Response.ok().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al cambiar estado: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Crea un mantenimiento desde una programación
     */
    @POST
    @Path("/{id}/crear-mantenimiento")
    public Response crearMantenimiento(@PathParam("id") Integer id) {
        try {
            ProgramacionMantenimientoModel programacion = programacionRepository.findByIdProgramacion(id);
            if (programacion == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Programación no encontrada")
                        .build();
            }

            // Crear nueva ejecución
            EjecucionMantenimientoModel ejecucion = new EjecucionMantenimientoModel();
            ejecucion.setEquipo(programacion.getEquipo());
            ejecucion.setProgramacion(programacion);

            Date fechaReferencia = programacion.getFechaProximoMantenimiento() != null
                    ? programacion.getFechaProximoMantenimiento()
                    : new Date();
            ejecucion.setFechaEjecucion(fechaReferencia);
            ejecucion.setEstado("PROGRAMADO");
            ejecucion.setBitacora("Mantenimiento generado automáticamente desde programación ID: " + id);

            if (programacion.getContrato() != null) {
                ejecucion.setContrato(programacion.getContrato());
            }

            // Intentar asignar contrato si es posible (lógica simplificada)
            // En un escenario real, deberíamos buscar el contrato vigente para este equipo
            // y tipo
            // Por ahora, dejamos contrato nulo o buscamos uno genérico si es necesario

            ejecucion.setFechaCreacion(new Date());
            ejecucion.setFechaModificacion(new Date());

            // Asignar usuario responsable (el que ejecuta la acción)
            try {
                String keycloakId = (String) request.getAttribute("keycloakId");
                if (keycloakId != null) {
                    UsuarioMantenimientoModel usuario = usuarioRepository.findByKeycloakId(keycloakId);
                    if (usuario != null) {
                        ejecucion.setUsuarioResponsable(usuario);
                        ejecucion.setUsuarioCreacion(usuario);
                    }
                }
            } catch (Exception e) {
                // Ignorar error de usuario
            }

            if (ejecucion.getUsuarioResponsable() == null && ejecucion.getUsuarioCreacion() != null) {
                ejecucion.setUsuarioResponsable(ejecucion.getUsuarioCreacion());
            }

            if (ejecucion.getUsuarioResponsable() == null) {
                ejecucion.setUsuarioResponsable(programacion.getUsuarioModificacion() != null
                        ? programacion.getUsuarioModificacion()
                        : programacion.getUsuarioCreacion());
            }

            if (ejecucion.getUsuarioCreacion() == null) {
                ejecucion.setUsuarioCreacion(programacion.getUsuarioModificacion() != null
                        ? programacion.getUsuarioModificacion()
                        : programacion.getUsuarioCreacion());
            }

            ejecucionRepository.save(ejecucion);

            // Actualizar programación: solo registrar modificación, no mover fechas hasta
            // completar
            programacion.setFechaModificacion(new Date());
            programacionRepository.save(programacion);

            EjecucionMantenimientoDTO dto = EjecucionMantenimientoMapper.toDTO(ejecucion);
            return Response.ok(dto).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al crear mantenimiento: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Desactiva una programación
     */
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteProgramacion(@PathParam("id") Integer id) {
        try {
            ProgramacionMantenimientoModel programacion = programacionRepository.findBy(id);
            if (programacion == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Programación no encontrada\"}")
                        .build();
            }

            // Eliminar ejecuciones asociadas primero
            String sqlDeleteEjecuciones = "DELETE FROM Ejecuciones_Mantenimiento WHERE id_programacion = ?";
            int ejecucionesEliminadas = em.createNativeQuery(sqlDeleteEjecuciones)
                    .setParameter(1, id)
                    .executeUpdate();

            System.out.println("🗑️ Eliminadas " + ejecucionesEliminadas + " ejecuciones de la programación " + id);

            // Ahora eliminar la programación
            ProgramacionMantenimientoModel managedProgramacion = em.merge(programacion);
            em.remove(managedProgramacion);
            em.flush();

            System.out.println("✅ Programación " + id + " eliminada correctamente");
            return Response.noContent().build();

        } catch (Exception e) {
            System.err.println("❌ Error al eliminar programación " + id + ": " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * Pausar/Despausar programación (cambiar estado activa)
     */
    @PATCH
    @Path("/{id}/toggle")
    @Transactional
    public Response toggleProgramacion(@PathParam("id") Integer id) {
        try {
            ProgramacionMantenimientoModel programacion = programacionRepository.findBy(id);
            if (programacion == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Programación no encontrada\"}")
                        .build();
            }

            // Cambiar estado activa
            programacion.setActiva(!programacion.getActiva());
            programacion.setFechaModificacion(new Date());
            programacionRepository.save(programacion);

            String mensaje = programacion.getActiva() ? "activada" : "pausada";
            System.out.println("🔄 Programación " + id + " " + mensaje);

            return Response.ok()
                    .entity("{\"message\": \"Programación " + mensaje + " exitosamente\", \"activa\": "
                            + programacion.getActiva() + "}")
                    .build();

        } catch (Exception e) {
            System.err.println("❌ Error al cambiar estado de programación " + id + ": " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
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

    /**
     * Descarta/Salta una programación vencida y avanza a la siguiente fecha.
     * Similar al comportamiento de "Descartar" en Outlook para eventos recurrentes.
     * 
     * @param id   ID de la programación
     * @param body Mapa con "motivo" opcional
     * @return Nueva fecha próxima calculada
     */
    @POST
    @Path("/{id}/descartar")
    @Transactional
    public Response descartarProgramacion(@PathParam("id") Integer id, Map<String, String> body) {
        try {
            ProgramacionMantenimientoModel programacion = programacionRepository.findByIdProgramacion(id);
            if (programacion == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Programación no encontrada")
                        .build();
            }

            Date fechaOriginal = programacion.getFechaProximoMantenimiento();
            if (fechaOriginal == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("La programación no tiene fecha próxima definida")
                        .build();
            }

            Integer frecuenciaDias = programacion.getFrecuenciaDias();
            if (frecuenciaDias == null || frecuenciaDias <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("La programación no tiene frecuencia válida")
                        .build();
            }

            // Calcular nueva fecha (avanzar según frecuencia)
            Calendar cal = Calendar.getInstance();
            cal.setTime(fechaOriginal);
            cal.add(Calendar.DAY_OF_MONTH, frecuenciaDias);
            Date nuevaFecha = cal.getTime();

            String motivo = body != null ? body.get("motivo") : null;
            if (motivo == null || motivo.trim().isEmpty()) {
                motivo = "Descartado por usuario";
            }

            // Obtener usuario actual para auditoría
            Integer usuarioId = null;
            try {
                String keycloakId = (String) request.getAttribute("keycloakId");
                if (keycloakId != null) {
                    UsuarioMantenimientoModel usuario = usuarioRepository.findByKeycloakId(keycloakId);
                    if (usuario != null) {
                        usuarioId = usuario.getId();
                    }
                }
            } catch (Exception e) {
                // Ignorar
            }

            // Insertar en historial (usando query nativa porque no tenemos la entidad aún)
            try {
                String insertHistorial = "INSERT INTO Historial_Programacion " +
                        "(id_programacion, tipo_evento, fecha_original, fecha_nueva, motivo, usuario_id, fecha_registro) "
                        +
                        "VALUES (?1, 'SALTADO', ?2, ?3, ?4, ?5, ?6)";

                em.createNativeQuery(insertHistorial)
                        .setParameter(1, id)
                        .setParameter(2, fechaOriginal)
                        .setParameter(3, nuevaFecha)
                        .setParameter(4, motivo)
                        .setParameter(5, usuarioId)
                        .setParameter(6, new Date())
                        .executeUpdate();
            } catch (Exception e) {
                // Si la tabla no existe aún, solo logear y continuar
                System.out.println("Nota: No se pudo insertar en Historial_Programacion: " + e.getMessage());
            }

            // Actualizar programación
            programacion.setFechaProximoMantenimiento(nuevaFecha);
            programacion.setFechaModificacion(new Date());
            asignarUsuarioAuditoria(programacion, false);
            programacionRepository.save(programacion);

            // Respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Programación descartada exitosamente");
            response.put("fechaOriginal", formatDate(fechaOriginal));
            response.put("nuevaFechaProximo", formatDate(nuevaFecha));
            response.put("motivo", motivo);
            response.put("idProgramacion", id);

            return Response.ok(response).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al descartar programación: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Obtiene el historial de eventos de una programación (ejecutados, saltados,
     * reprogramados)
     */
    @GET
    @Path("/{id}/historial")
    public Response getHistorialProgramacion(@PathParam("id") Integer id) {
        try {
            String query = "SELECT id_historial, tipo_evento, fecha_original, fecha_nueva, motivo, " +
                    "usuario_id, fecha_registro, id_ejecucion " +
                    "FROM Historial_Programacion WHERE id_programacion = ?1 " +
                    "ORDER BY fecha_registro DESC";

            @SuppressWarnings("unchecked")
            List<Object[]> resultados = em.createNativeQuery(query)
                    .setParameter(1, id)
                    .getResultList();

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < resultados.size(); i++) {
                Object[] row = resultados.get(i);
                if (i > 0)
                    json.append(",");
                json.append("{");
                json.append("\"idHistorial\":").append(row[0]).append(",");
                json.append("\"tipoEvento\":\"").append(row[1]).append("\",");
                json.append("\"fechaOriginal\":\"").append(row[2]).append("\",");
                json.append("\"fechaNueva\":").append(row[3] != null ? "\"" + row[3] + "\"" : "null").append(",");
                json.append("\"motivo\":\"").append(escapeJson(row[4] != null ? row[4].toString() : "")).append("\",");
                json.append("\"usuarioId\":").append(row[5] != null ? row[5] : "null").append(",");
                json.append("\"fechaRegistro\":\"").append(row[6]).append("\",");
                json.append("\"idEjecucion\":").append(row[7] != null ? row[7] : "null");
                json.append("}");
            }
            json.append("]");

            return Response.ok(json.toString()).build();

        } catch (Exception e) {
            // Si la tabla no existe, devolver array vacío
            return Response.ok("[]").build();
        }
    }

    /**
     * Obtiene métricas de cumplimiento de programaciones
     */
    @GET
    @Path("/metricas")
    public Response getMetricasCumplimiento() {
        try {
            String query = "SELECT " +
                    "COUNT(*) as total, " +
                    "SUM(CASE WHEN tipo_evento = 'EJECUTADO' THEN 1 ELSE 0 END) as ejecutados, " +
                    "SUM(CASE WHEN tipo_evento = 'SALTADO' THEN 1 ELSE 0 END) as saltados, " +
                    "SUM(CASE WHEN tipo_evento = 'REPROGRAMADO' THEN 1 ELSE 0 END) as reprogramados " +
                    "FROM Historial_Programacion " +
                    "WHERE YEAR(fecha_registro) = YEAR(GETDATE()) AND MONTH(fecha_registro) = MONTH(GETDATE())";

            Object[] resultado = (Object[]) em.createNativeQuery(query).getSingleResult();

            int total = resultado[0] != null ? ((Number) resultado[0]).intValue() : 0;
            int ejecutados = resultado[1] != null ? ((Number) resultado[1]).intValue() : 0;
            int saltados = resultado[2] != null ? ((Number) resultado[2]).intValue() : 0;
            int reprogramados = resultado[3] != null ? ((Number) resultado[3]).intValue() : 0;
            double cumplimiento = total > 0 ? (ejecutados * 100.0 / total) : 100.0;

            Map<String, Object> metricas = new HashMap<>();
            metricas.put("total", total);
            metricas.put("ejecutados", ejecutados);
            metricas.put("saltados", saltados);
            metricas.put("reprogramados", reprogramados);
            metricas.put("porcentajeCumplimiento", Math.round(cumplimiento * 100.0) / 100.0);

            return Response.ok(metricas).build();

        } catch (Exception e) {
            // Si la tabla no existe, devolver métricas vacías
            Map<String, Object> metricas = new HashMap<>();
            metricas.put("total", 0);
            metricas.put("ejecutados", 0);
            metricas.put("saltados", 0);
            metricas.put("reprogramados", 0);
            metricas.put("porcentajeCumplimiento", 100.0);
            return Response.ok(metricas).build();
        }
    }

    /**
     * Obtiene TODO el historial de programaciones (para la vista de bitácora)
     */
    @GET
    @Path("/bitacora/todos")
    public Response getHistorialCompleto() {
        try {
            System.out.println("=== Consultando historial completo ===");

            String query = "SELECT hp.id_historial, hp.id_programacion, hp.tipo_evento, " +
                    "hp.fecha_original, hp.fecha_nueva, hp.motivo, hp.usuario_id, " +
                    "hp.fecha_registro, hp.id_ejecucion, " +
                    "ISNULL(e.nombre, 'Sin equipo') as equipo_nombre, " +
                    "ISNULL(e.numero_serie, '') as equipo_serie, " +
                    "ISNULL(tm.nombre, 'N/A') as tipo_mantenimiento, " +
                    "ISNULL(u.nombre_completo, 'Sistema') as usuario_nombre " +
                    "FROM Historial_Programacion hp " +
                    "LEFT JOIN Programaciones_Mantenimiento pm ON hp.id_programacion = pm.id_programacion " +
                    "LEFT JOIN Equipos e ON pm.id_equipo = e.id_equipo " +
                    "LEFT JOIN Tipos_Mantenimiento tm ON pm.id_tipo_mantenimiento = tm.id_tipo " +
                    "LEFT JOIN Usuarios u ON hp.usuario_id = u.id " +
                    "ORDER BY hp.fecha_registro DESC";

            @SuppressWarnings("unchecked")
            List<Object[]> resultados = em.createNativeQuery(query).getResultList();

            System.out.println("Registros encontrados: " + resultados.size());

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < resultados.size(); i++) {
                Object[] row = resultados.get(i);
                if (i > 0)
                    json.append(",");
                json.append("{");
                json.append("\"idHistorial\":").append(row[0]).append(",");
                json.append("\"idProgramacion\":").append(row[1]).append(",");
                json.append("\"tipoEvento\":\"").append(row[2]).append("\",");
                json.append("\"fechaOriginal\":\"").append(row[3]).append("\",");
                json.append("\"fechaNueva\":").append(row[4] != null ? "\"" + row[4] + "\"" : "null").append(",");
                json.append("\"motivo\":\"").append(escapeJson(row[5] != null ? row[5].toString() : "")).append("\",");
                json.append("\"usuarioId\":").append(row[6] != null ? row[6] : "null").append(",");
                json.append("\"fechaRegistro\":\"").append(row[7]).append("\",");
                json.append("\"idEjecucion\":").append(row[8] != null ? row[8] : "null").append(",");
                json.append("\"equipoNombre\":\"").append(escapeJson(row[9] != null ? row[9].toString() : ""))
                        .append("\",");
                json.append("\"equipoSerie\":\"").append(escapeJson(row[10] != null ? row[10].toString() : ""))
                        .append("\",");
                json.append("\"tipoMantenimiento\":\"").append(escapeJson(row[11] != null ? row[11].toString() : ""))
                        .append("\",");
                json.append("\"usuarioNombre\":\"").append(escapeJson(row[12] != null ? row[12].toString() : ""))
                        .append("\"");
                json.append("}");
            }
            json.append("]");

            return Response.ok(json.toString()).build();

        } catch (Exception e) {
            System.out.println("Error en historial: " + e.getMessage());
            e.printStackTrace();
            return Response.ok("[]").build();
        }
    }

    // Método auxiliar para auditoría
    private void asignarUsuarioAuditoria(ProgramacionMantenimientoModel programacion, boolean esCreacion) {
        try {
            String keycloakId = (String) request.getAttribute("keycloakId");
            if (keycloakId != null) {
                UsuarioMantenimientoModel usuario = usuarioRepository.findByKeycloakId(keycloakId);
                if (usuario != null) {
                    if (esCreacion) {
                        programacion.setUsuarioCreacion(usuario);
                    }
                    programacion.setUsuarioModificacion(usuario);
                }
            }
        } catch (Exception e) {
            // Ignorar errores de auditoría para no bloquear la operación principal
            System.out.println("Error asignando auditoría: " + e.getMessage());
        }
    }
}
