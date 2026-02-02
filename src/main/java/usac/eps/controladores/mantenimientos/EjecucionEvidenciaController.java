package usac.eps.controladores.mantenimientos;

import usac.eps.modelos.mantenimientos.EvidenciaModel;
import usac.eps.modelos.mantenimientos.EjecucionMantenimientoModel;
import usac.eps.modelos.mantenimientos.UsuarioMantenimientoModel;
import usac.eps.repositorios.mantenimientos.EvidenciaRepository;
import usac.eps.repositorios.mantenimientos.EjecucionMantenimientoRepository;
import usac.eps.repositorios.mantenimientos.UsuarioMantenimientoRepository;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador para gestionar evidencias de ejecuciones de mantenimiento.
 * Permite subir, listar, descargar y eliminar archivos asociados a ejecuciones.
 */
@Path("/ejecuciones-mantenimiento/{ejecucionId}/evidencias")
@RequestScoped
public class EjecucionEvidenciaController {
    private static final Logger LOGGER = Logger.getLogger(EjecucionEvidenciaController.class.getName());

    private static final String BASE_DIR = System.getProperty("user.home") + File.separator + "inacif-evidencias"
            + File.separator + "ejecuciones";

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final String[] ALLOWED_EXTENSIONS = { ".jpg", ".jpeg", ".png", ".gif", ".webp", ".pdf", ".doc",
            ".docx", ".xls", ".xlsx" };

    @Inject
    private EvidenciaRepository evidenciaRepository;

    @Inject
    private EjecucionMantenimientoRepository ejecucionRepository;

    @Inject
    private UsuarioMantenimientoRepository usuarioRepository;

    @javax.persistence.PersistenceContext
    private javax.persistence.EntityManager em;

    @Context
    private HttpServletRequest request;

    /**
     * Lista todas las evidencias de una ejecución específica
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEvidencias(@PathParam("ejecucionId") Integer ejecucionId) {
        try {
            // Verificar que la ejecución existe
            EjecucionMantenimientoModel ejecucion = ejecucionRepository.findByIdEjecucion(ejecucionId);
            if (ejecucion == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Ejecución no encontrada\"}")
                        .build();
            }

            List<EvidenciaModel> evidencias = evidenciaRepository
                    .findByEntidadRelacionadaAndEntidadId("ejecucion_mantenimiento", ejecucionId);

            List<EvidenciaDTO> dtos = evidencias.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());

            return Response.ok(dtos).build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener evidencias", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al obtener evidencias: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * Sube una nueva evidencia a una ejecución
     */
    @POST
    @Path("/upload")
    @Consumes({ MediaType.APPLICATION_OCTET_STREAM, MediaType.MULTIPART_FORM_DATA, "image/*", "application/*" })
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response uploadEvidencia(
            @PathParam("ejecucionId") Integer ejecucionId,
            InputStream inputStream,
            @HeaderParam("X-Filename") String fileName,
            @HeaderParam("X-Descripcion") String descripcion) {

        try {
            // Verificar que la ejecución existe
            EjecucionMantenimientoModel ejecucion = ejecucionRepository.findByIdEjecucion(ejecucionId);
            if (ejecucion == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Ejecución no encontrada\"}")
                        .build();
            }

            // Validar nombre de archivo
            if (fileName == null || fileName.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Debe enviar el nombre del archivo en el header X-Filename\"}")
                        .build();
            }

            // Decodificar el nombre del archivo (viene URL-encoded del frontend)
            String decodedFileName = java.net.URLDecoder.decode(fileName, "UTF-8");
            String decodedDescripcion = descripcion != null ? java.net.URLDecoder.decode(descripcion, "UTF-8") : "";

            // Validar extensión
            if (!isValidFile(decodedFileName)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Tipo de archivo no permitido. Use: "
                                + String.join(", ", ALLOWED_EXTENSIONS) + "\"}")
                        .build();
            }

            // Crear directorio específico para esta ejecución
            java.nio.file.Path ejecucionDir = Paths.get(BASE_DIR, String.valueOf(ejecucionId));
            if (!Files.exists(ejecucionDir)) {
                Files.createDirectories(ejecucionDir);
            }

            // Generar nombre único - reemplazar espacios y caracteres especiales
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
            String extension = getFileExtension(decodedFileName);
            String baseName = getFileNameWithoutExtension(decodedFileName);
            // Limpiar el nombre base de caracteres problemáticos
            String cleanBaseName = baseName.replaceAll("[^a-zA-Z0-9_-]", "_");
            String uniqueFileName = cleanBaseName + "_" + timestamp + extension;

            java.nio.file.Path filePath = ejecucionDir.resolve(uniqueFileName);

            // Guardar archivo
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            long fileSize = Files.size(filePath);

            // Validar tamaño
            if (fileSize > MAX_FILE_SIZE) {
                Files.delete(filePath);
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"El archivo excede el tamaño máximo de 10MB\"}")
                        .build();
            }

            // Crear registro en BD
            EvidenciaModel evidencia = new EvidenciaModel();
            evidencia.setEntidadRelacionada("ejecucion_mantenimiento");
            evidencia.setEntidadId(ejecucionId);
            evidencia.setNombreArchivo(uniqueFileName); // Nombre limpio para el sistema de archivos
            evidencia.setNombreOriginal(decodedFileName); // Nombre original decodificado
            evidencia.setTipoArchivo(detectMimeType(decodedFileName));
            evidencia.setTamanio(fileSize);
            evidencia.setArchivoUrl("/MantenimientosBackend/api/ejecuciones-mantenimiento/" + ejecucionId
                    + "/evidencias/download/" + uniqueFileName);
            evidencia.setDescripcion(decodedDescripcion);
            evidencia.setFechaCreacion(new Date());

            // Asignar usuario si está autenticado
            try {
                String keycloakId = (String) request.getAttribute("keycloakId");
                if (keycloakId != null) {
                    UsuarioMantenimientoModel usuario = usuarioRepository.findByKeycloakId(keycloakId);
                    evidencia.setUsuarioCreacion(usuario);
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error al asignar usuario de creación", e);
            }

            evidenciaRepository.save(evidencia);

            return Response.status(Response.Status.CREATED)
                    .entity(toDTO(evidencia))
                    .build();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al subir evidencia", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al guardar archivo: " + e.getMessage() + "\"}")
                    .build();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Error al cerrar InputStream", e);
            }
        }
    }

    /**
     * Descarga una evidencia específica
     */
    @GET
    @Path("/download/{fileName}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadEvidencia(
            @PathParam("ejecucionId") Integer ejecucionId,
            @PathParam("fileName") String fileName) {

        try {
            java.nio.file.Path filePath = Paths.get(BASE_DIR, String.valueOf(ejecucionId), fileName);

            if (!Files.exists(filePath)) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Archivo no encontrado: " + fileName)
                        .build();
            }

            byte[] fileData = Files.readAllBytes(filePath);
            String mimeType = detectMimeType(fileName);

            return Response.ok(fileData)
                    .type(mimeType)
                    .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                    .header("Cache-Control", "public, max-age=3600")
                    .build();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al descargar evidencia", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al descargar archivo: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Elimina una evidencia
     */
    @DELETE
    @Path("/{evidenciaId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response deleteEvidencia(
            @PathParam("ejecucionId") Integer ejecucionId,
            @PathParam("evidenciaId") Integer evidenciaId) {

        try {
            // Buscar evidencia con SQL nativo
            String sqlSelect = "SELECT id, nombre_archivo, entidad_id, entidad_relacionada FROM Evidencias WHERE id = ?";
            @SuppressWarnings("unchecked")
            java.util.List<Object[]> results = em.createNativeQuery(sqlSelect)
                    .setParameter(1, evidenciaId)
                    .getResultList();

            if (results.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Evidencia no encontrada\"}")
                        .build();
            }

            Object[] row = results.get(0);
            String nombreArchivo = row[1] != null ? row[1].toString() : null;
            Integer entidadId = row[2] != null ? ((Number) row[2]).intValue() : null;
            String entidadRelacionada = row[3] != null ? row[3].toString() : null;

            // Verificar que pertenece a la ejecución
            if (!ejecucionId.equals(entidadId) || !"ejecucion_mantenimiento".equals(entidadRelacionada)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"La evidencia no pertenece a esta ejecución\"}")
                        .build();
            }

            // Eliminar archivo físico
            try {
                if (nombreArchivo != null) {
                    java.nio.file.Path filePath = Paths.get(BASE_DIR, String.valueOf(ejecucionId), nombreArchivo);
                    if (Files.exists(filePath)) {
                        Files.delete(filePath);
                    }
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "No se pudo eliminar archivo físico", e);
            }

            // Eliminar registro de BD con SQL nativo
            String sqlDelete = "DELETE FROM Evidencias WHERE id = ?";
            int deleted = em.createNativeQuery(sqlDelete)
                    .setParameter(1, evidenciaId)
                    .executeUpdate();

            return Response.ok("{\"message\": \"Evidencia eliminada correctamente\"}").build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar evidencia", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al eliminar evidencia: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * Actualiza la descripción de una evidencia
     */
    @PUT
    @Path("/{evidenciaId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateEvidencia(
            @PathParam("ejecucionId") Integer ejecucionId,
            @PathParam("evidenciaId") Integer evidenciaId,
            EvidenciaUpdateRequest updateRequest) {

        try {
            EvidenciaModel evidencia = evidenciaRepository.findById(evidenciaId);

            if (evidencia == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Evidencia no encontrada\"}")
                        .build();
            }

            if (!evidencia.getEntidadId().equals(ejecucionId)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"La evidencia no pertenece a esta ejecución\"}")
                        .build();
            }

            if (updateRequest.getDescripcion() != null) {
                evidencia.setDescripcion(updateRequest.getDescripcion());
            }

            evidenciaRepository.save(evidencia);

            return Response.ok(toDTO(evidencia)).build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar evidencia", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al actualizar evidencia: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    // --- Métodos auxiliares ---

    private boolean isValidFile(String fileName) {
        String lower = fileName.toLowerCase();
        for (String ext : ALLOWED_EXTENSIONS) {
            if (lower.endsWith(ext)) {
                return true;
            }
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

    private EvidenciaDTO toDTO(EvidenciaModel model) {
        EvidenciaDTO dto = new EvidenciaDTO();
        dto.setId(model.getId());
        dto.setNombreArchivo(model.getNombreArchivo());
        dto.setNombreOriginal(model.getNombreOriginal());
        dto.setTipoArchivo(model.getTipoArchivo());
        dto.setTamanio(model.getTamanio());
        dto.setDescripcion(model.getDescripcion());
        dto.setArchivoUrl(model.getArchivoUrl());
        dto.setFechaCreacion(model.getFechaCreacion());
        if (model.getUsuarioCreacion() != null) {
            dto.setUsuarioCreacionNombre(model.getUsuarioCreacion().getNombreCompleto());
        }
        return dto;
    }

    // --- DTOs internos ---

    public static class EvidenciaDTO {
        private Integer id;
        private String nombreArchivo;
        private String nombreOriginal;
        private String tipoArchivo;
        private Long tamanio;
        private String descripcion;
        private String archivoUrl;
        private Date fechaCreacion;
        private String usuarioCreacionNombre;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getNombreArchivo() {
            return nombreArchivo;
        }

        public void setNombreArchivo(String nombreArchivo) {
            this.nombreArchivo = nombreArchivo;
        }

        public String getNombreOriginal() {
            return nombreOriginal;
        }

        public void setNombreOriginal(String nombreOriginal) {
            this.nombreOriginal = nombreOriginal;
        }

        public String getTipoArchivo() {
            return tipoArchivo;
        }

        public void setTipoArchivo(String tipoArchivo) {
            this.tipoArchivo = tipoArchivo;
        }

        public Long getTamanio() {
            return tamanio;
        }

        public void setTamanio(Long tamanio) {
            this.tamanio = tamanio;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getArchivoUrl() {
            return archivoUrl;
        }

        public void setArchivoUrl(String archivoUrl) {
            this.archivoUrl = archivoUrl;
        }

        public Date getFechaCreacion() {
            return fechaCreacion;
        }

        public void setFechaCreacion(Date fechaCreacion) {
            this.fechaCreacion = fechaCreacion;
        }

        public String getUsuarioCreacionNombre() {
            return usuarioCreacionNombre;
        }

        public void setUsuarioCreacionNombre(String usuarioCreacionNombre) {
            this.usuarioCreacionNombre = usuarioCreacionNombre;
        }
    }

    public static class EvidenciaUpdateRequest {
        private String descripcion;

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }
    }
}
