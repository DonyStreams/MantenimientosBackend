package usac.eps.controladores.mantenimientos;

import usac.eps.controladores.mantenimientos.dto.EjecucionMantenimientoDTO;
import usac.eps.controladores.mantenimientos.dto.EjecucionMantenimientoRequest;
import usac.eps.controladores.mantenimientos.mapper.EjecucionMantenimientoMapper;
import usac.eps.modelos.mantenimientos.ContratoModel;
import usac.eps.modelos.mantenimientos.EjecucionMantenimientoModel;
import usac.eps.modelos.mantenimientos.EquipoModel;
import usac.eps.modelos.mantenimientos.ProgramacionMantenimientoModel;
import usac.eps.modelos.mantenimientos.UsuarioMantenimientoModel;
import usac.eps.repositorios.mantenimientos.ContratoRepository;
import usac.eps.repositorios.mantenimientos.EjecucionMantenimientoRepository;
import usac.eps.repositorios.mantenimientos.EquipoRepository;
import usac.eps.repositorios.mantenimientos.ProgramacionMantenimientoRepository;
import usac.eps.repositorios.mantenimientos.UsuarioMantenimientoRepository;
import usac.eps.seguridad.RequiresRole;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/ejecuciones-mantenimiento")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class EjecucionMantenimientoController {
    private static final Set<String> ESTADOS_VALIDOS = Set.of("PROGRAMADO", "EN_PROCESO", "COMPLETADO", "CANCELADO");
    private static final String EVIDENCIAS_DIR = System.getProperty("user.home") + File.separator + "inacif-evidencias"
            + File.separator + "ejecuciones";
    private static final String[] ALLOWED_EXTENSIONS = { ".jpg", ".jpeg", ".png", ".gif", ".webp", ".pdf", ".doc",
            ".docx", ".xls", ".xlsx" };

    @PersistenceContext
    private EntityManager em;

    @Inject
    private EjecucionMantenimientoRepository ejecucionMantenimientoRepository;

    @Inject
    private ContratoRepository contratoRepository;

    @Inject
    private EquipoRepository equipoRepository;

    @Inject
    private UsuarioMantenimientoRepository usuarioRepository;

    @Inject
    private ProgramacionMantenimientoRepository programacionRepository;

    @Context
    private HttpServletRequest request;

    @GET
    public List<EjecucionMantenimientoDTO> getAll(@QueryParam("estado") String estado) {
        List<EjecucionMantenimientoModel> ejecuciones;

        if (estado != null && !estado.trim().isEmpty()) {
            String estadoFiltrado = estado.trim().toUpperCase();
            ejecuciones = ejecucionMantenimientoRepository.findByEstadoOrderByFechaEjecucionDesc(estadoFiltrado);
        } else {
            ejecuciones = ejecucionMantenimientoRepository.findAll();
        }

        return ejecuciones.stream()
                .map(EjecucionMantenimientoMapper::toDTO)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Integer id) {
        EjecucionMantenimientoModel ejecucion = ejecucionMantenimientoRepository.findByIdEjecucion(id);
        if (ejecucion == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Ejecución no encontrada")
                    .build();
        }
        return Response.ok(EjecucionMantenimientoMapper.toDTO(ejecucion)).build();
    }

    @POST
    @RequiresRole({ "ADMIN", "SUPERVISOR", "TECNICO" })
    public Response create(EjecucionMantenimientoRequest request) {
        try {
            EjecucionMantenimientoModel ejecucion = new EjecucionMantenimientoModel();
            actualizarEntidadConRequest(ejecucion, request, true);

            ejecucionMantenimientoRepository.save(ejecucion);
            return Response.status(Response.Status.CREATED).entity(EjecucionMantenimientoMapper.toDTO(ejecucion))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al crear ejecución: " + e.getMessage())
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    @RequiresRole({ "ADMIN", "SUPERVISOR", "TECNICO" })
    public Response update(@PathParam("id") Integer id, EjecucionMantenimientoRequest request) {
        try {
            EjecucionMantenimientoModel existente = ejecucionMantenimientoRepository.findByIdEjecucion(id);
            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Ejecución no encontrada")
                        .build();
            }

            actualizarEntidadConRequest(existente, request, false);

            ejecucionMantenimientoRepository.save(existente);
            return Response.ok(EjecucionMantenimientoMapper.toDTO(existente)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al actualizar ejecución: " + e.getMessage())
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @RequiresRole({ "ADMIN" })
    public Response delete(@PathParam("id") Integer id) {
        try {
            EjecucionMantenimientoModel ejecucion = ejecucionMantenimientoRepository.findByIdEjecucion(id);
            if (ejecucion == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Ejecución no encontrada\"}")
                        .build();
            }

            // Verificar SOLO si tiene historial de programación asociado (esto sí bloquea
            // la eliminación)
            String queryHistorial = "SELECT COUNT(*) FROM Historial_Programacion WHERE id_ejecucion = ?";
            Integer countHistorial = ((Number) em.createNativeQuery(queryHistorial)
                    .setParameter(1, id)
                    .getSingleResult()).intValue();

            if (countHistorial > 0) {
                String mensaje = "No se puede eliminar la ejecución porque tiene " + countHistorial +
                        " registro(s) de historial de programación asociado(s). " +
                        "Estos registros son parte del historial del equipo y no pueden eliminarse.";

                System.out.println("⚠️ No se puede eliminar ejecución " + id + ": tiene historial de programación");
                return Response.status(Response.Status.CONFLICT)
                        .entity("{\"error\": \"" + mensaje + "\"}")
                        .build();
            }

            // Si no tiene historial, eliminar en cascada: primero comentarios, luego
            // evidencias, finalmente la ejecución

            // Eliminar comentarios asociados
            String sqlDeleteComentarios = "DELETE FROM Comentarios_Ejecucion WHERE id_ejecucion = ?";
            int comentariosEliminados = em.createNativeQuery(sqlDeleteComentarios)
                    .setParameter(1, id)
                    .executeUpdate();
            System.out.println("🗑️ Eliminados " + comentariosEliminados + " comentarios de la ejecución " + id);

            // Eliminar evidencias asociadas
            String sqlDeleteEvidencias = "DELETE FROM Evidencias WHERE entidad_relacionada = 'ejecucion_mantenimiento' AND entidad_id = ?";
            int evidenciasEliminadas = em.createNativeQuery(sqlDeleteEvidencias)
                    .setParameter(1, id)
                    .executeUpdate();
            System.out.println("🗑️ Eliminadas " + evidenciasEliminadas + " evidencias de la ejecución " + id);

            // Eliminar la ejecución
            EjecucionMantenimientoModel managedEjecucion = em.merge(ejecucion);
            em.remove(managedEjecucion);
            em.flush();

            System.out.println("✅ Ejecución " + id + " eliminada correctamente (con " + comentariosEliminados +
                    " comentarios y " + evidenciasEliminadas + " evidencias)");
            return Response.noContent().build();

        } catch (Exception e) {
            System.err.println("❌ Error al eliminar ejecución " + id + ": " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    @PATCH
    @Path("/{id}/estado")
    public Response actualizarEstado(@PathParam("id") Integer id, EstadoRequest estadoRequest) {
        if (estadoRequest == null || estadoRequest.getEstado() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Debe especificar el estado")
                    .build();
        }

        EjecucionMantenimientoModel ejecucion = ejecucionMantenimientoRepository.findByIdEjecucion(id);
        if (ejecucion == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Ejecución no encontrada")
                    .build();
        }

        try {
            String estadoAnterior = ejecucion.getEstado();

            EjecucionMantenimientoRequest request = new EjecucionMantenimientoRequest();
            request.setEstado(estadoRequest.getEstado());
            request.setBitacora(estadoRequest.getBitacora());

            if ("EN_PROCESO".equalsIgnoreCase(estadoRequest.getEstado())) {
                request.setFechaInicioTrabajo(estadoRequest.getFechaReferencia());
            }
            if ("COMPLETADO".equalsIgnoreCase(estadoRequest.getEstado())) {
                request.setFechaCierre(estadoRequest.getFechaReferencia());
                if (estadoRequest.getFechaInicio() != null) {
                    request.setFechaInicioTrabajo(estadoRequest.getFechaInicio());
                }
            }

            actualizarEntidadConRequest(ejecucion, request, false);
            ejecucionMantenimientoRepository.save(ejecucion);

            // 🔥 NUEVO: Actualizar programación cuando se completa la ejecución
            if ("COMPLETADO".equalsIgnoreCase(estadoRequest.getEstado()) &&
                    !"COMPLETADO".equalsIgnoreCase(estadoAnterior) &&
                    ejecucion.getProgramacion() != null) {

                System.out.println("✅ Ejecución completada - Actualizando programación...");
                actualizarProgramacionDespuesDeEjecucion(ejecucion);
            }

            return Response.ok(EjecucionMantenimientoMapper.toDTO(ejecucion)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al actualizar estado: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/contrato/{idContrato}")
    public List<EjecucionMantenimientoDTO> getByContrato(@PathParam("idContrato") Integer idContrato) {
        return ejecucionMantenimientoRepository.findByContratoIdContrato(idContrato).stream()
                .map(EjecucionMantenimientoMapper::toDTO)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/equipo/{idEquipo}")
    public List<EjecucionMantenimientoDTO> getByEquipo(@PathParam("idEquipo") Integer idEquipo) {
        return ejecucionMantenimientoRepository.findByEquipoIdEquipo(idEquipo).stream()
                .map(EjecucionMantenimientoMapper::toDTO)
                .collect(Collectors.toList());
    }

    private void actualizarEntidadConRequest(EjecucionMantenimientoModel ejecucion,
            EjecucionMantenimientoRequest request,
            boolean esNuevo) {

        UsuarioMantenimientoModel usuarioContexto = obtenerUsuarioContexto();

        if (esNuevo) {
            if (request.getIdContrato() == null) {
                throw new IllegalArgumentException("El contrato es obligatorio");
            }
            ContratoModel contrato = contratoRepository.findByIdContrato(request.getIdContrato());
            if (contrato == null) {
                throw new IllegalArgumentException("Contrato no encontrado");
            }
            ejecucion.setContrato(contrato);

            if (request.getIdEquipo() == null) {
                throw new IllegalArgumentException("El equipo es obligatorio");
            }
            EquipoModel equipo = equipoRepository.findByIdEquipo(request.getIdEquipo());
            if (equipo == null) {
                throw new IllegalArgumentException("Equipo no encontrado");
            }
            ejecucion.setEquipo(equipo);
        } else {
            if (request.getIdContrato() != null) {
                ContratoModel contrato = contratoRepository.findByIdContrato(request.getIdContrato());
                if (contrato == null) {
                    throw new IllegalArgumentException("Contrato no encontrado");
                }
                ejecucion.setContrato(contrato);
            }
            if (request.getIdEquipo() != null) {
                EquipoModel equipo = equipoRepository.findByIdEquipo(request.getIdEquipo());
                if (equipo == null) {
                    throw new IllegalArgumentException("Equipo no encontrado");
                }
                ejecucion.setEquipo(equipo);
            }
        }

        if (request.getUsuarioResponsableId() != null) {
            UsuarioMantenimientoModel responsable = usuarioRepository.findById(request.getUsuarioResponsableId());
            if (responsable == null) {
                throw new IllegalArgumentException("Usuario responsable no encontrado");
            }
            ejecucion.setUsuarioResponsable(responsable);
            if (esNuevo && ejecucion.getUsuarioCreacion() == null) {
                ejecucion.setUsuarioCreacion(responsable);
            }
        }

        if (request.getIdProgramacion() != null) {
            ProgramacionMantenimientoModel programacion = programacionRepository
                    .findByIdProgramacion(request.getIdProgramacion());
            if (programacion == null) {
                throw new IllegalArgumentException("Programación no encontrada");
            }
            ejecucion.setProgramacion(programacion);
        } else if (esNuevo) {
            ejecucion.setProgramacion(null);
        }

        if (request.getBitacora() != null) {
            ejecucion.setBitacora(request.getBitacora());
        }

        if (request.getFechaEjecucion() != null) {
            ejecucion.setFechaEjecucion(request.getFechaEjecucion());
        } else if (esNuevo && ejecucion.getFechaEjecucion() == null) {
            ejecucion.setFechaEjecucion(new Date());
        }

        if (request.getFechaInicioTrabajo() != null) {
            ejecucion.setFechaInicioTrabajo(request.getFechaInicioTrabajo());
        }

        if (request.getFechaCierre() != null) {
            ejecucion.setFechaCierre(request.getFechaCierre());
        }

        if (request.getEstado() != null && !request.getEstado().trim().isEmpty()) {
            String nuevoEstado = request.getEstado().trim().toUpperCase();
            if (!ESTADOS_VALIDOS.contains(nuevoEstado)) {
                throw new IllegalArgumentException("Estado no válido: " + nuevoEstado);
            }
            ejecucion.setEstado(nuevoEstado);
            manejarTransicionesEstado(ejecucion, nuevoEstado);
        } else if (esNuevo && ejecucion.getEstado() == null) {
            ejecucion.setEstado("PROGRAMADO");
        }

        if (esNuevo) {
            ejecucion.setFechaCreacion(new Date());
            ejecucion.setFechaModificacion(new Date());
        } else {
            ejecucion.setFechaModificacion(new Date());
        }

        if (usuarioContexto != null) {
            if (esNuevo) {
                if (ejecucion.getUsuarioCreacion() == null) {
                    ejecucion.setUsuarioCreacion(usuarioContexto);
                }
                if (ejecucion.getUsuarioResponsable() == null && request.getUsuarioResponsableId() == null) {
                    ejecucion.setUsuarioResponsable(usuarioContexto);
                }
            } else {
                ejecucion.setUsuarioModificacion(usuarioContexto);
            }
        }
    }

    private void manejarTransicionesEstado(EjecucionMantenimientoModel ejecucion, String nuevoEstado) {
        if (Objects.equals(nuevoEstado, "EN_PROCESO") && ejecucion.getFechaInicioTrabajo() == null) {
            ejecucion.setFechaInicioTrabajo(new Date());
        }

        if (Objects.equals(nuevoEstado, "COMPLETADO")) {
            if (ejecucion.getFechaInicioTrabajo() == null) {
                ejecucion.setFechaInicioTrabajo(new Date());
            }
            if (ejecucion.getFechaCierre() == null) {
                ejecucion.setFechaCierre(new Date());
            }

            // Actualizar programación asociada si existe
            if (ejecucion.getProgramacion() != null) {
                ProgramacionMantenimientoModel programacion = ejecucion.getProgramacion();
                programacion.setFechaUltimoMantenimiento(ejecucion.getFechaCierre());
                programacion.setFechaModificacion(new Date());
                programacionRepository.save(programacion);
            }
        } else if (Objects.equals(nuevoEstado, "CANCELADO")) {
            if (ejecucion.getFechaCierre() == null) {
                ejecucion.setFechaCierre(new Date());
            }
        }
    }

    private UsuarioMantenimientoModel obtenerUsuarioContexto() {
        if (request == null) {
            return null;
        }
        try {
            String keycloakId = (String) request.getAttribute("keycloakId");
            if (keycloakId != null) {
                return usuarioRepository.findByKeycloakId(keycloakId);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static class EstadoRequest {
        private String estado;
        private String bitacora;
        private Date fechaReferencia;
        private Date fechaInicio;

        public String getEstado() {
            return estado;
        }

        public void setEstado(String estado) {
            this.estado = estado;
        }

        public String getBitacora() {
            return bitacora;
        }

        public void setBitacora(String bitacora) {
            this.bitacora = bitacora;
        }

        public Date getFechaReferencia() {
            return fechaReferencia;
        }

        public void setFechaReferencia(Date fechaReferencia) {
            this.fechaReferencia = fechaReferencia;
        }

        public Date getFechaInicio() {
            return fechaInicio;
        }

        public void setFechaInicio(Date fechaInicio) {
            this.fechaInicio = fechaInicio;
        }
    }

    // ==================== EVIDENCIAS ====================

    @GET
    @Path("/{id}/evidencias")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEvidencias(@PathParam("id") Integer ejecucionId) {
        try {
            EjecucionMantenimientoModel ejecucion = ejecucionMantenimientoRepository.findByIdEjecucion(ejecucionId);
            if (ejecucion == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Ejecución no encontrada\"}")
                        .build();
            }

            String sql = "SELECT id, nombre_archivo, nombre_original, tipo_archivo, tamanio, " +
                    "descripcion, archivo_url, fecha_creacion " +
                    "FROM Evidencias WHERE entidad_relacionada = 'ejecucion_mantenimiento' AND entidad_id = ? " +
                    "ORDER BY fecha_creacion DESC";

            @SuppressWarnings("unchecked")
            List<Object[]> rows = em.createNativeQuery(sql)
                    .setParameter(1, ejecucionId)
                    .getResultList();

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < rows.size(); i++) {
                Object[] row = rows.get(i);
                if (i > 0)
                    json.append(",");
                json.append("{")
                        .append("\"id\": ").append(row[0]).append(",")
                        .append("\"nombreArchivo\": \"").append(row[1] != null ? row[1] : "").append("\",")
                        .append("\"nombreOriginal\": \"").append(row[2] != null ? row[2] : "").append("\",")
                        .append("\"tipoArchivo\": \"").append(row[3] != null ? row[3] : "").append("\",")
                        .append("\"tamanio\": ").append(row[4] != null ? row[4] : 0).append(",")
                        .append("\"descripcion\": \"").append(row[5] != null ? escapeJson(row[5].toString()) : "")
                        .append("\",")
                        .append("\"archivoUrl\": \"").append(row[6] != null ? row[6] : "").append("\",")
                        .append("\"fechaCreacion\": \"").append(row[7] != null ? row[7] : "").append("\"")
                        .append("}");
            }
            json.append("]");

            return Response.ok(json.toString()).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al obtener evidencias: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @POST
    @Path("/{id}/evidencias/upload")
    @Consumes({ MediaType.APPLICATION_OCTET_STREAM, MediaType.MULTIPART_FORM_DATA, "image/*", "application/*" })
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response uploadEvidencia(
            @PathParam("id") Integer ejecucionId,
            InputStream inputStream,
            @HeaderParam("X-Filename") String fileName,
            @HeaderParam("X-Descripcion") String descripcion) {

        try {
            System.out.println("📎 Subiendo evidencia para ejecución: " + ejecucionId);

            EjecucionMantenimientoModel ejecucion = ejecucionMantenimientoRepository.findByIdEjecucion(ejecucionId);
            if (ejecucion == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Ejecución no encontrada\"}")
                        .build();
            }

            if (fileName == null || fileName.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Debe enviar el nombre del archivo en el header X-Filename\"}")
                        .build();
            }

            if (!isValidEvidenciaFile(fileName)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Tipo de archivo no permitido\"}")
                        .build();
            }

            // Crear directorio
            java.nio.file.Path ejecucionDir = Paths.get(EVIDENCIAS_DIR, String.valueOf(ejecucionId));
            if (!Files.exists(ejecucionDir)) {
                Files.createDirectories(ejecucionDir);
            }

            // Generar nombre único
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
            String extension = getFileExtension(fileName);
            String baseName = getFileNameWithoutExtension(fileName);
            String uniqueFileName = baseName + "_" + timestamp + extension;

            java.nio.file.Path filePath = ejecucionDir.resolve(uniqueFileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            long fileSize = Files.size(filePath);

            String tipoMime = detectMimeType(fileName);
            String archivoUrl = "/MantenimientosBackend/api/ejecuciones-mantenimiento/" + ejecucionId
                    + "/evidencias/download/" + uniqueFileName;
            String desc = descripcion != null ? java.net.URLDecoder.decode(descripcion, "UTF-8") : "";

            // Guardar en BD
            String sql = "INSERT INTO Evidencias (entidad_relacionada, entidad_id, nombre_archivo, nombre_original, " +
                    "tipo_archivo, tamanio, descripcion, archivo_url, fecha_creacion) " +
                    "VALUES ('ejecucion_mantenimiento', ?, ?, ?, ?, ?, ?, ?, GETDATE())";

            em.createNativeQuery(sql)
                    .setParameter(1, ejecucionId)
                    .setParameter(2, uniqueFileName)
                    .setParameter(3, fileName)
                    .setParameter(4, tipoMime)
                    .setParameter(5, fileSize)
                    .setParameter(6, desc)
                    .setParameter(7, archivoUrl)
                    .executeUpdate();

            System.out.println("✅ Evidencia guardada: " + uniqueFileName);

            String jsonResponse = String.format(
                    "{\"id\": 0, \"nombreArchivo\": \"%s\", \"nombreOriginal\": \"%s\", " +
                            "\"tipoArchivo\": \"%s\", \"tamanio\": %d, \"descripcion\": \"%s\", " +
                            "\"archivoUrl\": \"%s\"}",
                    uniqueFileName, fileName, tipoMime, fileSize, escapeJson(desc), archivoUrl);

            return Response.status(Response.Status.CREATED).entity(jsonResponse).build();

        } catch (Exception e) {
            System.out.println("❌ Error al subir evidencia: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al guardar archivo: " + e.getMessage() + "\"}")
                    .build();
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException ignored) {
            }
        }
    }

    @GET
    @Path("/{id}/evidencias/download/{fileName}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadEvidencia(
            @PathParam("id") Integer ejecucionId,
            @PathParam("fileName") String fileName) {

        try {
            java.nio.file.Path filePath = Paths.get(EVIDENCIAS_DIR, String.valueOf(ejecucionId), fileName);

            if (!Files.exists(filePath)) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Archivo no encontrado")
                        .build();
            }

            byte[] fileData = Files.readAllBytes(filePath);
            String mimeType = detectMimeType(fileName);

            return Response.ok(fileData)
                    .type(mimeType)
                    .header("Content-Disposition", "inline; filename=\"" + fileName + "\"")
                    .build();

        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al descargar archivo")
                    .build();
        }
    }

    @DELETE
    @Path("/{id}/evidencias/{evidenciaId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @RequiresRole({ "ADMIN" })
    public Response deleteEvidencia(
            @PathParam("id") Integer ejecucionId,
            @PathParam("evidenciaId") Integer evidenciaId) {

        try {
            // Obtener nombre del archivo
            String sqlSelect = "SELECT nombre_archivo FROM Evidencias WHERE id = ? AND entidad_id = ? AND entidad_relacionada = 'ejecucion_mantenimiento'";
            @SuppressWarnings("unchecked")
            List<Object> results = em.createNativeQuery(sqlSelect)
                    .setParameter(1, evidenciaId)
                    .setParameter(2, ejecucionId)
                    .getResultList();

            if (results.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Evidencia no encontrada\"}")
                        .build();
            }

            String nombreArchivo = (String) results.get(0);

            // Eliminar archivo físico
            try {
                java.nio.file.Path filePath = Paths.get(EVIDENCIAS_DIR, String.valueOf(ejecucionId), nombreArchivo);
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                System.out.println("⚠️ No se pudo eliminar archivo físico: " + e.getMessage());
            }

            // Eliminar de BD
            String sqlDelete = "DELETE FROM Evidencias WHERE id = ?";
            em.createNativeQuery(sqlDelete).setParameter(1, evidenciaId).executeUpdate();

            return Response.ok("{\"message\": \"Evidencia eliminada\"}").build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al eliminar: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    // Métodos auxiliares para evidencias
    private boolean isValidEvidenciaFile(String fileName) {
        String lower = fileName.toLowerCase();
        for (String ext : ALLOWED_EXTENSIONS) {
            if (lower.endsWith(ext))
                return true;
        }
        return false;
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot) : "";
    }

    private String getFileNameWithoutExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(0, lastDot) : fileName;
    }

    private String detectMimeType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg"))
            return "image/jpeg";
        if (lower.endsWith(".png"))
            return "image/png";
        if (lower.endsWith(".gif"))
            return "image/gif";
        if (lower.endsWith(".webp"))
            return "image/webp";
        if (lower.endsWith(".pdf"))
            return "application/pdf";
        if (lower.endsWith(".doc"))
            return "application/msword";
        if (lower.endsWith(".docx"))
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (lower.endsWith(".xls"))
            return "application/vnd.ms-excel";
        if (lower.endsWith(".xlsx"))
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        return "application/octet-stream";
    }

    private String escapeJson(String text) {
        if (text == null)
            return "";
        return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    /**
     * Actualiza la programación después de completar una ejecución
     * - Actualiza fechaUltimoMantenimiento con la fecha de la ejecución
     * - Recalcula fechaProximoMantenimiento sumando la frecuencia
     * - Registra en historial como EJECUTADO
     */
    private void actualizarProgramacionDespuesDeEjecucion(EjecucionMantenimientoModel ejecucion) {
        try {
            ProgramacionMantenimientoModel programacion = ejecucion.getProgramacion();
            if (programacion == null) {
                System.out.println("⚠️ La ejecución no tiene programación asociada");
                return;
            }

            // Usar la fecha de cierre si existe, si no la fecha de ejecución
            Date fechaRealizada = ejecucion.getFechaCierre() != null
                    ? ejecucion.getFechaCierre()
                    : ejecucion.getFechaEjecucion();

            if (fechaRealizada == null) {
                System.out.println("⚠️ No hay fecha de ejecución para actualizar");
                return;
            }

            // Guardar fecha original antes de actualizar
            Date fechaOriginalProgramada = programacion.getFechaProximoMantenimiento();

            System.out.println("📅 Actualizando programación ID: " + programacion.getIdProgramacion());
            System.out.println("📅 Fecha último mantenimiento anterior: " + programacion.getFechaUltimoMantenimiento());
            System.out.println("📅 Nueva fecha último mantenimiento: " + fechaRealizada);

            // Actualizar fecha del último mantenimiento
            programacion.setFechaUltimoMantenimiento(fechaRealizada);

            // Verificar si es programación única (frecuencia = 0)
            boolean esProgramacionUnica = programacion.getFrecuenciaDias() != null
                    && programacion.getFrecuenciaDias() == 0;

            if (esProgramacionUnica) {
                // Para programaciones únicas, desactivar automáticamente después de ejecutar
                System.out.println("🔒 Programación única (frecuencia=0) - Desactivando automáticamente...");
                programacion.setActiva(false);
                // Mantener la fecha próxima como null ya que no habrá siguiente
                programacion.setFechaProximoMantenimiento(null);
            } else {
                // Recalcular próximo mantenimiento solo para programaciones recurrentes
                programacion.calcularProximoMantenimiento();
            }

            System.out.println("📅 Nueva fecha próximo mantenimiento: " + programacion.getFechaProximoMantenimiento());
            System.out.println("📅 Programación activa: " + programacion.getActiva());

            // Actualizar auditoría
            programacion.setFechaModificacion(new Date());

            // Persistir cambios de programación
            em.merge(programacion);

            // 🆕 Registrar en Historial_Programacion
            // El tipo de evento depende del estado inicial de la ejecución creada
            try {
                Integer usuarioId = null;
                if (ejecucion.getUsuarioResponsable() != null) {
                    usuarioId = ejecucion.getUsuarioResponsable().getId();
                } else if (ejecucion.getUsuarioModificacion() != null) {
                    usuarioId = ejecucion.getUsuarioModificacion().getId();
                }

                // Si la ejecución se crea en estado PROGRAMADO, registrar como
                // EJECUTADO_PROGRAMADO
                // Si se completa directamente, registrar como EJECUTADO
                String tipoEvento = "EJECUTADO_PROGRAMADO";
                String estadoActual = ejecucion.getEstado();

                if ("COMPLETADO".equals(estadoActual)) {
                    tipoEvento = "EJECUTADO";
                } else if ("CANCELADO".equals(estadoActual)) {
                    tipoEvento = "SALTADO";
                } else if ("EN_PROCESO".equals(estadoActual)) {
                    tipoEvento = "EJECUTADO_PROGRAMADO";
                } else {
                    // PROGRAMADO u otro estado
                    tipoEvento = "EJECUTADO_PROGRAMADO";
                }

                String insertHistorial = "INSERT INTO Historial_Programacion " +
                        "(id_programacion, tipo_evento, fecha_original, id_ejecucion, usuario_id, fecha_registro) " +
                        "VALUES (?1, ?2, ?3, ?4, ?5, GETDATE())";

                em.createNativeQuery(insertHistorial)
                        .setParameter(1, programacion.getIdProgramacion())
                        .setParameter(2, tipoEvento)
                        .setParameter(3, fechaOriginalProgramada)
                        .setParameter(4, ejecucion.getIdEjecucion())
                        .setParameter(5, usuarioId)
                        .executeUpdate();

                System.out.println("📝 Historial de ejecución registrado");
            } catch (Exception historialEx) {
                // Si la tabla no existe aún, solo logear
                System.out.println("Nota: No se pudo registrar historial: " + historialEx.getMessage());
            }

            em.flush();

            System.out.println("✅ Programación actualizada exitosamente");

        } catch (Exception e) {
            System.err.println("❌ Error actualizando programación: " + e.getMessage());
            e.printStackTrace();
            // No lanzamos excepción para no afectar el flujo principal
        }
    }
}
