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
import javax.persistence.Query;
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
import usac.eps.modelos.mantenimientos.ComentarioEjecucionModel;
import usac.eps.repositorios.mantenimientos.ProgramacionMantenimientoRepository;
import usac.eps.repositorios.mantenimientos.EquipoRepository;
import usac.eps.repositorios.mantenimientos.TipoMantenimientoRepository;
import usac.eps.repositorios.mantenimientos.EjecucionMantenimientoRepository;
import usac.eps.repositorios.mantenimientos.ContratoRepository;
import usac.eps.repositorios.mantenimientos.UsuarioMantenimientoRepository;
import usac.eps.repositorios.mantenimientos.ComentarioEjecucionRepository;

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

    @Inject
    private ComentarioEjecucionRepository comentarioEjecucionRepository;

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
    @Transactional
    public Response createProgramacion(ProgramacionMantenimientoModel programacion) {
        try {
            // Validaciones básicas
            if (programacion.getEquipo() == null || programacion.getTipoMantenimiento() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Equipo y tipo de mantenimiento son requeridos")
                        .build();
            }

            if (programacion.getFrecuenciaDias() == null || programacion.getFrecuenciaDias() < 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Frecuencia en días debe ser mayor o igual a 0 (0 = único)")
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

            System.out.println("📦 Guardando programación con EntityManager directamente...");

            // Usar EntityManager directamente para garantizar que el ID se genere
            em.persist(programacion);
            em.flush();

            // Ahora el ID debe estar disponible
            Integer idGenerado = programacion.getIdProgramacion();
            System.out.println("✅ Programación guardada con ID: " + idGenerado);

            // Registrar en historial como CREADO (fecha_original = fecha_nueva para
            // creación)
            if (idGenerado != null) {
                try {
                    Date fechaProximo = programacion.getFechaProximoMantenimiento();
                    registrarHistorial(idGenerado, "CREADO", fechaProximo, fechaProximo,
                            "Programación creada");
                    System.out.println("✅ Historial CREADO registrado correctamente");
                } catch (Exception historialError) {
                    System.out.println("⚠️ Error registrando historial: " + historialError.getMessage());
                    historialError.printStackTrace();
                }
            } else {
                System.out.println("⚠️ No se pudo obtener ID de programación para registrar historial");
            }

            // Refrescar para obtener la entidad completa con relaciones
            em.refresh(programacion);

            return Response.status(Response.Status.CREATED).entity(programacion).build();

        } catch (Exception e) {
            System.out.println("❌ Error en createProgramacion: " + e.getMessage());
            e.printStackTrace();
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
    @Transactional
    public Response updateProgramacion(@PathParam("id") Integer id, ProgramacionMantenimientoModel programacion) {
        try {
            ProgramacionMantenimientoModel existing = programacionRepository.findBy(id);
            if (existing == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Programación no encontrada")
                        .build();
            }

            // Guardar fecha original antes de cualquier cambio
            Date fechaOriginal = existing.getFechaProximoMantenimiento();

            // Actualizar equipo si viene en la petición
            if (programacion.getEquipo() != null && programacion.getEquipo().getIdEquipo() != null) {
                EquipoModel equipo = equipoRepository.findBy(programacion.getEquipo().getIdEquipo());
                if (equipo != null) {
                    existing.setEquipo(equipo);
                }
            }

            // Actualizar tipo de mantenimiento si viene en la petición
            if (programacion.getTipoMantenimiento() != null
                    && programacion.getTipoMantenimiento().getIdTipo() != null) {
                TipoMantenimientoModel tipo = tipoMantenimientoRepository
                        .findBy(programacion.getTipoMantenimiento().getIdTipo());
                if (tipo != null) {
                    existing.setTipoMantenimiento(tipo);
                }
            }

            // Actualizar contrato si viene en la petición
            if (programacion.getContrato() != null && programacion.getContrato().getIdContrato() != null) {
                ContratoModel contrato = contratoRepository.findBy(programacion.getContrato().getIdContrato());
                if (contrato != null) {
                    existing.setContrato(contrato);
                }
            }

            // Actualizar frecuencia
            if (programacion.getFrecuenciaDias() != null) {
                existing.setFrecuenciaDias(programacion.getFrecuenciaDias());
            }

            // Actualizar fecha próximo mantenimiento directamente si viene
            if (programacion.getFechaProximoMantenimiento() != null) {
                existing.setFechaProximoMantenimiento(programacion.getFechaProximoMantenimiento());
            }

            // Actualizar fecha último mantenimiento si viene
            if (programacion.getFechaUltimoMantenimiento() != null) {
                existing.setFechaUltimoMantenimiento(programacion.getFechaUltimoMantenimiento());
            }

            // Actualizar días alerta
            if (programacion.getDiasAlertaPrevia() != null) {
                existing.setDiasAlertaPrevia(programacion.getDiasAlertaPrevia());
            }

            // Actualizar estado activa
            if (programacion.getActiva() != null) {
                existing.setActiva(programacion.getActiva());
            }

            // Actualizar observaciones
            if (programacion.getObservaciones() != null) {
                existing.setObservaciones(programacion.getObservaciones());
            }

            // Auditoría
            existing.setFechaModificacion(new Date());
            asignarUsuarioAuditoria(existing, false);

            ProgramacionMantenimientoModel updated = programacionRepository.save(existing);

            // Registrar en historial
            Date fechaNueva = updated.getFechaProximoMantenimiento();
            registrarHistorial(id, "EDITADO", fechaOriginal, fechaNueva,
                    "Programación editada por usuario");

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
    @Transactional
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
                Boolean estadoAnterior = programacion.getActiva();
                programacion.setActiva(activa);
                programacion.setFechaModificacion(new Date());
                asignarUsuarioAuditoria(programacion, false);
                programacionRepository.save(programacion);

                // Registrar en historial
                String tipoEvento = activa ? "ACTIVADO" : "PAUSADO";
                String motivo = activa ? "Programación activada por usuario" : "Programación pausada por usuario";
                registrarHistorial(id, tipoEvento, programacion.getFechaProximoMantenimiento(),
                        programacion.getFechaProximoMantenimiento(), motivo);
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
    @Transactional
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

            // Usar persist del EntityManager para que el objeto quede manejado
            em.persist(ejecucion);
            em.flush(); // Forzar flush para que se genere el ID
            // Ahora ejecucion.getIdEjecucion() debería tener el ID generado

            System.out.println("✅ Ejecución persistida con ID: " + ejecucion.getIdEjecucion());

            // Crear comentario inicial en la conversación
            try {
                ComentarioEjecucionModel comentarioInicial = new ComentarioEjecucionModel();
                comentarioInicial.setEjecucion(ejecucion);
                comentarioInicial.setUsuario(ejecucion.getUsuarioCreacion());
                comentarioInicial.setTipoComentario("SEGUIMIENTO");

                String tipoMant = programacion.getTipoMantenimiento() != null
                        ? programacion.getTipoMantenimiento().getNombre()
                        : "N/A";
                String equipoNombre = programacion.getEquipo() != null
                        ? programacion.getEquipo().getNombre()
                        : "N/A";

                comentarioInicial.setComentario(String.format(
                        "📋 Ejecución creada a partir de programación #%d.\n" +
                                "• Equipo: %s\n" +
                                "• Tipo: %s\n" +
                                "• Estado inicial: PROGRAMADO",
                        id, equipoNombre, tipoMant));
                comentarioInicial.setEstadoNuevo("PROGRAMADO");
                comentarioInicial.setFechaCreacion(new Date());
                comentarioEjecucionRepository.save(comentarioInicial);
                System.out.println("✅ Comentario inicial creado");
            } catch (Exception e) {
                System.err.println("⚠️ Error al crear comentario inicial: " + e.getMessage());
                // Continuar sin comentario
            }

            // Actualizar programación: solo registrar modificación, no mover fechas hasta
            // completar
            try {
                programacion.setFechaModificacion(new Date());
                programacionRepository.save(programacion);
                System.out.println("✅ Programación actualizada");
            } catch (Exception e) {
                System.err.println("⚠️ Error al actualizar programación: " + e.getMessage());
            }

            // Registrar en historial solo si la ejecución se completa o cancela
            // Si está en estado PROGRAMADO, no registrar aún porque no se ha ejecutado
            try {
                if ("COMPLETADO".equals(ejecucion.getEstado())) {
                    registrarHistorial(id, "EJECUTADO",
                            programacion.getFechaProximoMantenimiento(),
                            programacion.getFechaProximoMantenimiento(),
                            "Mantenimiento ejecutado y completado");
                    System.out.println("✅ Historial registrado: EJECUTADO");
                } else if ("CANCELADO".equals(ejecucion.getEstado())) {
                    registrarHistorial(id, "SALTADO",
                            programacion.getFechaProximoMantenimiento(),
                            programacion.getFechaProximoMantenimiento(),
                            "Mantenimiento cancelado");
                    System.out.println("✅ Historial registrado: SALTADO");
                } else {
                    // Estado PROGRAMADO o EN_PROCESO: no registrar en historial aún
                    // Se registrará cuando se complete o cancele la ejecución
                    System.out.println("ℹ️ Ejecución creada en estado " + ejecucion.getEstado() +
                            " - Historial se registrará al completar");
                }
            } catch (Exception e) {
                System.err.println("⚠️ Error al registrar historial: " + e.getMessage());
                e.printStackTrace();
            }

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

            // Eliminar historial de programación primero (FK_HistorialProg_Programacion)
            String sqlDeleteHistorial = "DELETE FROM Historial_Programacion WHERE id_programacion = ?";
            int historialEliminado = em.createNativeQuery(sqlDeleteHistorial)
                    .setParameter(1, id)
                    .executeUpdate();

            System.out.println(
                    "🗑️ Eliminados " + historialEliminado + " registros de historial de la programación " + id);

            // Eliminar ejecuciones asociadas
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
            Boolean nuevoEstado = !programacion.getActiva();
            programacion.setActiva(nuevoEstado);
            programacion.setFechaModificacion(new Date());
            programacionRepository.save(programacion);

            String mensaje = nuevoEstado ? "activada" : "pausada";
            System.out.println("🔄 Programación " + id + " " + mensaje);

            // Registrar en historial
            String tipoEvento = nuevoEstado ? "ACTIVADO" : "PAUSADO";
            String motivo = nuevoEstado ? "Programación activada por usuario" : "Programación pausada por usuario";
            Date fechaProximo = programacion.getFechaProximoMantenimiento();
            registrarHistorial(id, tipoEvento, fechaProximo, fechaProximo, motivo);

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
                        .entity("No se puede descartar una programación única (sin frecuencia). Use eliminar o editar.")
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

            // Registrar en historial usando método auxiliar
            registrarHistorial(id, "SALTADO", fechaOriginal, nuevaFecha, motivo);

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
     * Reprograma un mantenimiento a una fecha específica elegida por el usuario.
     * A diferencia de descartar (que avanza automáticamente), aquí el usuario elige
     * la fecha.
     */
    @POST
    @Path("/{id}/reprogramar")
    @Transactional
    public Response reprogramarProgramacion(@PathParam("id") Integer id, Map<String, Object> body) {
        try {
            ProgramacionMantenimientoModel programacion = programacionRepository.findByIdProgramacion(id);
            if (programacion == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Programación no encontrada")
                        .build();
            }

            // Obtener nueva fecha del body
            String nuevaFechaStr = (String) body.get("nuevaFecha");
            if (nuevaFechaStr == null || nuevaFechaStr.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("La nueva fecha es requerida")
                        .build();
            }

            // Parsear la fecha (formato yyyy-MM-dd) usando zona horaria local para evitar
            // desfases
            Date nuevaFecha;
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                sdf.setTimeZone(java.util.TimeZone.getTimeZone("America/Guatemala"));
                nuevaFecha = sdf.parse(nuevaFechaStr.substring(0, 10));
                System.out.println("📅 Reprogramar: recibido=" + nuevaFechaStr + " parseado=" + nuevaFecha);
            } catch (Exception e) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Formato de fecha inválido. Use yyyy-MM-dd")
                        .build();
            }

            Date fechaOriginal = programacion.getFechaProximoMantenimiento();
            String motivo = body.get("motivo") != null ? body.get("motivo").toString() : "Reprogramado por usuario";

            // Registrar en historial usando método auxiliar
            registrarHistorial(id, "REPROGRAMADO", fechaOriginal, nuevaFecha, motivo);

            // Actualizar programación
            programacion.setFechaProximoMantenimiento(nuevaFecha);
            programacion.setFechaModificacion(new Date());
            asignarUsuarioAuditoria(programacion, false);

            // Usar merge para asegurar que el EntityManager gestione la entidad
            ProgramacionMantenimientoModel merged = em.merge(programacion);
            em.flush(); // Forzar persistencia inmediata

            System.out.println("✅ Programación " + id + " reprogramada: nueva fecha = " + nuevaFecha);

            // Respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Programación reprogramada exitosamente");
            response.put("fechaOriginal", formatDate(fechaOriginal));
            response.put("nuevaFecha", formatDate(nuevaFecha));
            response.put("motivo", motivo);
            response.put("idProgramacion", id);

            return Response.ok(response).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al reprogramar: " + e.getMessage())
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

    /**
     * Elimina múltiples registros del historial de programaciones
     */
    @DELETE
    @Path("/historial/batch")
    @Transactional
    public Response deleteHistorialMultiple(Map<String, List<Integer>> payload) {
        try {
            List<Integer> ids = payload.get("ids");

            if (ids == null || ids.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"mensaje\":\"No se proporcionaron IDs para eliminar\"}")
                        .build();
            }

            System.out.println("🗑️ Eliminando " + ids.size() + " registros del historial de programaciones");

            // Construir query con parámetros posicionales
            StringBuilder queryBuilder = new StringBuilder(
                    "DELETE FROM Historial_Programacion WHERE id_historial IN (");
            for (int i = 0; i < ids.size(); i++) {
                if (i > 0)
                    queryBuilder.append(",");
                queryBuilder.append("?").append(i + 1);
            }
            queryBuilder.append(")");

            Query query = em.createNativeQuery(queryBuilder.toString());
            for (int i = 0; i < ids.size(); i++) {
                query.setParameter(i + 1, ids.get(i));
            }

            int deletedCount = query.executeUpdate();

            System.out.println("✅ Se eliminaron " + deletedCount + " registros del historial");

            return Response.ok()
                    .entity("{\"mensaje\":\"" + deletedCount + " registro(s) eliminado(s) correctamente\"}")
                    .build();

        } catch (Exception e) {
            System.err.println("❌ Error eliminando registros del historial: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"mensaje\":\"Error al eliminar registros: " + e.getMessage() + "\"}")
                    .build();
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

    /**
     * Registra un evento en el historial de la programación
     * Tipos de evento: CREADO, EDITADO, PAUSADO, ACTIVADO, REPROGRAMADO, SALTADO,
     * EJECUTADO
     */
    private void registrarHistorial(Integer idProgramacion, String tipoEvento, Date fechaOriginal, Date fechaNueva,
            String motivo) {
        try {
            // Obtener usuario actual
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
                System.out.println("⚠️ No se pudo obtener usuario: " + e.getMessage());
            }

            System.out.println(
                    "📝 Intentando registrar historial: " + tipoEvento + " para programación " + idProgramacion);

            String insertHistorial = "INSERT INTO Historial_Programacion " +
                    "(id_programacion, tipo_evento, fecha_original, fecha_nueva, motivo, usuario_id, fecha_registro) " +
                    "VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7)";

            int rows = em.createNativeQuery(insertHistorial)
                    .setParameter(1, idProgramacion)
                    .setParameter(2, tipoEvento)
                    .setParameter(3, fechaOriginal)
                    .setParameter(4, fechaNueva)
                    .setParameter(5, motivo)
                    .setParameter(6, usuarioId)
                    .setParameter(7, new Date())
                    .executeUpdate();

            System.out.println("✅ Historial registrado: " + tipoEvento + " para programación " + idProgramacion
                    + " (filas afectadas: " + rows + ")");
        } catch (Exception e) {
            System.out.println("❌ Error al insertar en Historial_Programacion: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
