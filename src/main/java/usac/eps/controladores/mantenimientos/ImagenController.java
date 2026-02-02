package usac.eps.controladores.mantenimientos;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/imagenes")
public class ImagenController {
    private static final Logger LOGGER = Logger.getLogger(ImagenController.class.getName());

    // Directorio base para guardar imágenes
    private static final String BASE_DIR = System.getProperty("user.home") + File.separator + "inacif-imagenes"
            + File.separator + "equipos";

    @POST
    @Path("/upload")
    @Consumes({ MediaType.APPLICATION_OCTET_STREAM, MediaType.MULTIPART_FORM_DATA, "image/*" })
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadImage(InputStream inputStream,
            @HeaderParam("X-Filename") String fileName) {
        try {
            if (fileName == null || fileName.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Debe enviar el nombre del archivo en el header X-Filename\"}")
                        .build();
            }

            // Validar que sea una imagen
            if (!isValidImageFile(fileName)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Solo se permiten archivos de imagen (jpg, jpeg, png, gif, webp)\"}")
                        .build();
            }

            // Crear directorio si no existe
            java.nio.file.Path baseDir = Paths.get(BASE_DIR);
            if (!Files.exists(baseDir)) {
                Files.createDirectories(baseDir);
            }

            // Generar nombre único para evitar colisiones
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileExtension = getFileExtension(fileName);
            String baseFileName = getFileNameWithoutExtension(fileName);

            String uniqueFileName = baseFileName + "_" + timestamp + fileExtension;
            java.nio.file.Path filePath = baseDir.resolve(uniqueFileName);

            // Guardar imagen
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);

            long fileSize = Files.size(filePath);

            // Respuesta JSON con información de la imagen
            String jsonResponse = String.format(
                    "{\"success\": true, \"message\": \"Imagen subida correctamente\", " +
                            "\"fileName\": \"%s\", \"originalName\": \"%s\", \"size\": %d, " +
                            "\"url\": \"/MantenimientosBackend/api/imagenes/view/%s\"}",
                    uniqueFileName, fileName, fileSize, uniqueFileName);

            return Response.ok(jsonResponse)
                    .type(MediaType.APPLICATION_JSON)
                    .build();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al subir imagen", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al guardar imagen: " + e.getMessage() + "\"}")
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

    @GET
    @Path("/view/{fileName}")
    @Produces("image/*")
    public Response viewImage(@PathParam("fileName") String fileName) {
        try {
            java.nio.file.Path filePath = Paths.get(BASE_DIR, fileName);

            if (!Files.exists(filePath)) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Imagen no encontrada")
                        .build();
            }

            // Determinar el tipo de contenido basado en la extensión
            String contentType = getContentType(fileName);

            // Leer el archivo
            byte[] imageData = Files.readAllBytes(filePath);

            return Response.ok(imageData)
                    .type(contentType)
                    .header("Cache-Control", "public, max-age=3600")
                    .build();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al servir imagen", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al cargar imagen")
                    .build();
        }
    }

    @GET
    @Path("/test")
    @Produces(MediaType.APPLICATION_JSON)
    public Response testImageSystem() {
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
            LOGGER.log(Level.SEVERE, "Error en sistema de imágenes", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error en sistema de imágenes: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    // Métodos auxiliares
    private boolean isValidImageFile(String fileName) {
        if (fileName == null)
            return false;
        String extension = getFileExtension(fileName).toLowerCase();
        return extension.equals(".jpg") || extension.equals(".jpeg") ||
                extension.equals(".png") || extension.equals(".gif") ||
                extension.equals(".webp") || extension.equals(".bmp");
    }

    private String getFileExtension(String fileName) {
        if (fileName == null)
            return "";
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot) : "";
    }

    private String getFileNameWithoutExtension(String fileName) {
        if (fileName == null)
            return "";
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(0, lastDot) : fileName;
    }

    private String getContentType(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        switch (extension) {
            case ".jpg":
            case ".jpeg":
                return "image/jpeg";
            case ".png":
                return "image/png";
            case ".gif":
                return "image/gif";
            case ".webp":
                return "image/webp";
            case ".bmp":
                return "image/bmp";
            default:
                return "image/jpeg";
        }
    }
}