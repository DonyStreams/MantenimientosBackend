package usac.eps.controladores.mantenimientos;

import javax.enterprise.context.RequestScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controlador para manejo de archivos de contratos (sin FTP)
 * Guarda archivos localmente en el servidor
 */
@Path("/archivos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class ArchivoController {

    @PersistenceContext
    private EntityManager em;

    // Directorio base para guardar archivos (coincide con el volumen Docker)
    private static final String BASE_DIR = System.getProperty("user.home") + File.separator + "contratos-archivos";

    @POST
    @Path("/upload/{idContrato}")
    @Consumes({ MediaType.APPLICATION_OCTET_STREAM, MediaType.MULTIPART_FORM_DATA, "application/pdf",
            "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document" })
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response uploadFile(InputStream inputStream,
            @PathParam("idContrato") int idContrato,
            @HeaderParam("X-Filename") String fileName) {
        try {
            System.out.println("📤 Iniciando upload de archivo: " + fileName);

            if (fileName == null || fileName.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Debe enviar el nombre del archivo en el header X-Filename\"}")
                        .build();
            }

            // 🆕 Validar que sea un archivo de contrato válido
            if (!isValidContractFile(fileName)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Solo se permiten archivos PDF, DOC y DOCX para contratos\"}")
                        .build();
            }

            // Crear directorio si no existe
            java.nio.file.Path baseDir = Paths.get(BASE_DIR);
            if (!Files.exists(baseDir)) {
                Files.createDirectories(baseDir);
                System.out.println("📁 Directorio creado: " + BASE_DIR);
            }

            // Generar nombre único para evitar colisiones
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileExtension = getFileExtension(fileName);
            String baseFileName = getFileNameWithoutExtension(fileName);

            String uniqueFileName = baseFileName + "_" + timestamp + fileExtension;
            java.nio.file.Path filePath = baseDir.resolve(uniqueFileName);

            // Guardar archivo
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);

            long fileSize = Files.size(filePath);
            System.out.println("✅ Archivo guardado: " + uniqueFileName + " (" + fileSize + " bytes)");

            // 🆕 GUARDAR EN BASE DE DATOS
            try {
                String tipoMime = getContentType(fileName);

                // Usar SQL nativo para insertar en Documentos_Contrato
                String sql = "INSERT INTO Documentos_Contrato (id_contrato, nombre_archivo, ruta_archivo, " +
                        "tipo_documento, tamanio_archivo, tipo_mime, fecha_subida, usuario_subida) " +
                        "VALUES (?, ?, ?, ?, ?, ?, GETDATE(), 1)"; // usuario_subida=1 temporal

                em.createNativeQuery(sql)
                        .setParameter(1, idContrato)
                        .setParameter(2, fileName) // Nombre original
                        .setParameter(3, uniqueFileName) // Ruta del archivo único
                        .setParameter(4, fileExtension.substring(1).toUpperCase()) // PDF, DOC, DOCX
                        .setParameter(5, fileSize)
                        .setParameter(6, tipoMime)
                        .executeUpdate();

                System.out.println("✅ Registro guardado en BD para contrato: " + idContrato);

            } catch (Exception dbEx) {
                System.out.println("⚠️ Error al guardar en BD (archivo ya guardado): " + dbEx.getMessage());
                // El archivo físico ya se guardó, continuamos
            }

            // Respuesta JSON con información del archivo
            String jsonResponse = String.format(
                    "{\"success\": true, \"message\": \"Archivo subido correctamente\", " +
                            "\"fileName\": \"%s\", \"originalName\": \"%s\", \"size\": %d, " +
                            "\"downloadUrl\": \"/MantenimientosBackend/api/archivos/download/%s\", " +
                            "\"contratoId\": %d}",
                    uniqueFileName, fileName, fileSize, uniqueFileName, idContrato);

            return Response.ok(jsonResponse)
                    .type(MediaType.APPLICATION_JSON)
                    .build();

        } catch (IOException e) {
            System.out.println("❌ Error al subir archivo: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al guardar archivo: " + e.getMessage() + "\"}")
                    .build();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                System.out.println("⚠️ Error al cerrar InputStream: " + e.getMessage());
            }
        }
    }

    @GET
    @Path("/download/{fileName}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadFile(@PathParam("fileName") String fileName) {
        try {
            System.out.println("📥 Descargando archivo: " + fileName);

            java.nio.file.Path filePath = Paths.get(BASE_DIR, fileName);

            if (!Files.exists(filePath)) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Archivo no encontrado: " + fileName + "\"}")
                        .build();
            }

            File file = filePath.toFile();

            return Response.ok(file)
                    .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                    .header("Content-Length", file.length())
                    .build();

        } catch (Exception e) {
            System.out.println("❌ Error al descargar archivo: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al descargar archivo: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @DELETE
    @Path("/delete/{fileName}")
    public Response deleteFile(@PathParam("fileName") String fileName) {
        try {
            System.out.println("🗑️ Eliminando archivo: " + fileName);

            java.nio.file.Path filePath = Paths.get(BASE_DIR, fileName);

            if (!Files.exists(filePath)) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Archivo no encontrado: " + fileName + "\"}")
                        .build();
            }

            Files.delete(filePath);
            System.out.println("✅ Archivo eliminado: " + fileName);

            return Response.ok("{\"success\": true, \"message\": \"Archivo eliminado correctamente\"}")
                    .build();

        } catch (IOException e) {
            System.out.println("❌ Error al eliminar archivo: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al eliminar archivo: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @GET
    @Path("/contrato/{idContrato}/count")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getArchivosPorContrato(@PathParam("idContrato") int idContrato) {
        try {
            System.out.println("📊 Consultando archivos para contrato: " + idContrato);

            String sql = "SELECT COUNT(*) as total FROM Documentos_Contrato WHERE id_contrato = ?";

            Number result = (Number) em.createNativeQuery(sql)
                    .setParameter(1, idContrato)
                    .getSingleResult();

            int totalArchivos = result.intValue();

            String jsonResponse = String.format(
                    "{\"contratoId\": %d, \"totalArchivos\": %d, \"success\": true}",
                    idContrato, totalArchivos);

            return Response.ok(jsonResponse).build();

        } catch (Exception e) {
            System.out.println("❌ Error al consultar archivos: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al consultar archivos: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @GET
    @Path("/contrato/{idContrato}/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getListaArchivosPorContrato(@PathParam("idContrato") int idContrato) {
        try {
            System.out.println("📋 Listando archivos para contrato: " + idContrato);

            String sql = "SELECT id_documento, nombre_archivo, ruta_archivo, tipo_documento, " +
                    "tamanio_archivo, tipo_mime, fecha_subida " +
                    "FROM Documentos_Contrato WHERE id_contrato = ? ORDER BY fecha_subida DESC";

            @SuppressWarnings("unchecked")
            java.util.List<Object[]> resultados = em.createNativeQuery(sql)
                    .setParameter(1, idContrato)
                    .getResultList();

            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{\"contratoId\": ").append(idContrato)
                    .append(", \"archivos\": [");

            for (int i = 0; i < resultados.size(); i++) {
                Object[] row = resultados.get(i);
                if (i > 0)
                    jsonBuilder.append(",");

                jsonBuilder.append("{")
                        .append("\"id\": ").append(row[0]).append(",")
                        .append("\"nombreOriginal\": \"").append(row[1]).append("\",")
                        .append("\"nombreArchivo\": \"").append(row[2]).append("\",")
                        .append("\"tipoDocumento\": \"").append(row[3]).append("\",")
                        .append("\"tamano\": ").append(row[4]).append(",")
                        .append("\"tipoMime\": \"").append(row[5]).append("\",")
                        .append("\"fechaSubida\": \"").append(row[6]).append("\"")
                        .append("}");
            }

            jsonBuilder.append("], \"total\": ").append(resultados.size())
                    .append(", \"success\": true}");

            return Response.ok(jsonBuilder.toString()).build();

        } catch (Exception e) {
            System.out.println("❌ Error al listar archivos: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al listar archivos: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @GET
    @Path("/test")
    public Response testFileSystem() {
        try {
            java.nio.file.Path baseDir = Paths.get(BASE_DIR);

            // Crear directorio si no existe
            if (!Files.exists(baseDir)) {
                Files.createDirectories(baseDir);
            }

            // Verificar permisos
            boolean canRead = Files.isReadable(baseDir);
            boolean canWrite = Files.isWritable(baseDir);

            String response = String.format(
                    "{\"status\": \"OK\", \"directory\": \"%s\", \"canRead\": %b, \"canWrite\": %b, \"exists\": %b}",
                    BASE_DIR, canRead, canWrite, Files.exists(baseDir));

            return Response.ok(response).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error en sistema de archivos: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    // 🔧 MÉTODOS AUXILIARES PARA CONTRATOS

    /**
     * Valida que el archivo sea un documento válido para contratos
     */
    private boolean isValidContractFile(String fileName) {
        if (fileName == null)
            return false;
        String extension = getFileExtension(fileName).toLowerCase();
        return extension.equals(".pdf") ||
                extension.equals(".doc") ||
                extension.equals(".docx");
    }

    /**
     * Obtiene la extensión del archivo
     */
    private String getFileExtension(String fileName) {
        if (fileName == null)
            return "";
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot) : "";
    }

    /**
     * Obtiene el nombre del archivo sin la extensión
     */
    private String getFileNameWithoutExtension(String fileName) {
        if (fileName == null)
            return "";
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(0, lastDot) : fileName;
    }

    /**
     * Determina el tipo de contenido basado en la extensión del archivo
     */
    private String getContentType(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        switch (extension) {
            case ".pdf":
                return "application/pdf";
            case ".doc":
                return "application/msword";
            case ".docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            default:
                return "application/octet-stream";
        }
    }
}
