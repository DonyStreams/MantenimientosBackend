package usac.eps.controladores.mantenimientos;

import javax.enterprise.context.RequestScoped;
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
    
    // Directorio base para guardar archivos (ajustar según tu servidor)
    private static final String BASE_DIR = System.getProperty("user.home") + File.separator + "contratos-archivos";
    
    @POST
    @Path("/upload")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public Response uploadFile(InputStream inputStream, 
                              @HeaderParam("X-Filename") String fileName,
                              @HeaderParam("X-Content-Type") String contentType) {
        try {
            System.out.println("📤 Iniciando upload de archivo: " + fileName);
            
            if (fileName == null || fileName.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Debe enviar el nombre del archivo en el header X-Filename\"}")
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
            String fileExtension = "";
            int lastDot = fileName.lastIndexOf('.');
            if (lastDot > 0) {
                fileExtension = fileName.substring(lastDot);
                fileName = fileName.substring(0, lastDot);
            }
            
            String uniqueFileName = fileName + "_" + timestamp + fileExtension;
            java.nio.file.Path filePath = baseDir.resolve(uniqueFileName);

            // Guardar archivo
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            
            long fileSize = Files.size(filePath);
            System.out.println("✅ Archivo guardado: " + uniqueFileName + " (" + fileSize + " bytes)");

            // Respuesta JSON con información del archivo
            String jsonResponse = String.format(
                "{\"success\": true, \"message\": \"Archivo subido correctamente\", " +
                "\"fileName\": \"%s\", \"originalName\": \"%s\", \"size\": %d, \"path\": \"%s\"}",
                uniqueFileName, fileName + fileExtension, fileSize, filePath.toString()
            );

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
                BASE_DIR, canRead, canWrite, Files.exists(baseDir)
            );
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error en sistema de archivos: " + e.getMessage() + "\"}")
                    .build();
        }
    }
}
