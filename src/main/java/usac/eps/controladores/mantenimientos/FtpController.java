package usac.eps.controladores.mantenimientos;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import usac.eps.util.ConfigUtil;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

@Path("/ftp")
public class FtpController {

    private static final String FTP_HOST = ConfigUtil.get("ftp.host");
    private static final int FTP_PORT = Integer.parseInt(ConfigUtil.get("ftp.port"));
    private static final String FTP_USER = ConfigUtil.get("ftp.user");
    private static final String FTP_PASS = ConfigUtil.get("ftp.pass");

    @GET
    @Path("/test")
    public Response testConnection() {
        System.out.println("🔵 [FTP] ========== TEST CONNECTION ==========");
        System.out.println("🔵 [FTP] Host: " + FTP_HOST);
        System.out.println("🔵 [FTP] Port: " + FTP_PORT);
        System.out.println("🔵 [FTP] User: " + FTP_USER);
        System.out.println("🔵 [FTP] Pass: " + (FTP_PASS != null ? "***configurado***" : "NULL"));

        FTPClient ftpClient = new FTPClient();
        try {
            System.out.println("🔵 [FTP] Conectando...");
            ftpClient.connect(FTP_HOST, FTP_PORT);
            System.out.println("🔵 [FTP] Conexión establecida, reply: " + ftpClient.getReplyString());

            System.out.println("🔵 [FTP] Intentando login...");
            boolean login = ftpClient.login(FTP_USER, FTP_PASS);
            System.out.println("🔵 [FTP] Login resultado: " + (login ? "EXITOSO" : "FALLIDO"));
            System.out.println("🔵 [FTP] Reply code: " + ftpClient.getReplyCode());
            System.out.println("🔵 [FTP] Reply string: " + ftpClient.getReplyString());

            if (login) {
                System.out.println("🟢 [FTP] ========== CONNECTION OK ==========");
                ftpClient.logout();
                return Response.ok("Conexión FTP exitosa").build();
            } else {
                System.out.println("🔴 [FTP] ========== LOGIN FAILED ==========");
                return Response.status(Response.Status.UNAUTHORIZED).entity("No se pudo autenticar en el servidor FTP")
                        .build();
            }
        } catch (IOException e) {
            System.out.println("🔴 [FTP] ========== CONNECTION ERROR ==========");
            System.out.println("🔴 [FTP] Excepción: " + e.getClass().getSimpleName());
            System.out.println("🔴 [FTP] Mensaje: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error de conexión FTP: " + e.getMessage()).build();
        } finally {
            try {
                ftpClient.disconnect();
            } catch (Exception ignored) {
            }
        }
    }

    @POST
    @Path("/upload")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public Response uploadFile(InputStream inputStream, @HeaderParam("X-Filename") String fileName) {
        System.out.println("🔵 [FTP] ========== INICIO UPLOAD ==========");
        System.out.println("🔵 [FTP] Archivo recibido: " + fileName);
        System.out.println("🔵 [FTP] InputStream: " + (inputStream != null ? "OK" : "NULL"));

        FTPClient ftpClient = new FTPClient();
        boolean uploaded = false;
        String rutaCompleta = null;
        try {
            System.out.println("🔵 [FTP] Validando nombre de archivo...");
            if (fileName == null || fileName.isEmpty()) {
                System.out.println("🔴 [FTP] ERROR: Nombre de archivo vacío");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Debe enviar el nombre del archivo en el header X-Filename").build();
            }

            System.out.println("🔵 [FTP] Conectando al servidor FTP...");
            System.out.println("🔵 [FTP] Host: " + FTP_HOST + ":" + FTP_PORT);
            System.out.println("🔵 [FTP] Usuario: " + FTP_USER);

            ftpClient.connect(FTP_HOST, FTP_PORT);
            System.out.println("🔵 [FTP] Conexión establecida, intentando login...");

            boolean login = ftpClient.login(FTP_USER, FTP_PASS);
            System.out.println("🔵 [FTP] Login resultado: " + (login ? "EXITOSO" : "FALLIDO"));

            if (!login) {
                System.out.println("🔴 [FTP] ERROR: Autenticación fallida");
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("No se pudo autenticar en el servidor FTP").build();
            }

            System.out.println("🔵 [FTP] Configurando modo binario...");
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            // Crear el directorio /imagenes/equipos si no existe
            String directorio = "/imagenes/equipos";
            System.out.println("🔵 [FTP] Creando directorio: " + directorio);
            ftpClient.makeDirectory(directorio);

            // Guardar el archivo en la ruta /imagenes/equipos/nombreArchivo
            rutaCompleta = directorio + "/" + fileName;
            System.out.println("🔵 [FTP] Ruta completa: " + rutaCompleta);

            // Verificar si el archivo ya existe
            System.out.println("🔵 [FTP] Verificando si el archivo ya existe...");
            if (ftpClient.listFiles(rutaCompleta) != null && ftpClient.listFiles(rutaCompleta).length > 0) {
                System.out.println("🔴 [FTP] ERROR: Archivo ya existe");
                return Response.status(Response.Status.CONFLICT)
                        .entity("Ya existe un archivo con ese nombre en la ruta: " + rutaCompleta).build();
            }

            System.out.println("🔵 [FTP] Subiendo archivo...");
            uploaded = ftpClient.storeFile(rutaCompleta, inputStream);
            System.out.println("🔵 [FTP] Upload resultado: " + (uploaded ? "EXITOSO" : "FALLIDO"));

            if (!uploaded) {
                System.out.println("🔴 [FTP] ERROR: storeFile devolvió false");
                System.out.println("🔴 [FTP] Reply Code: " + ftpClient.getReplyCode());
                System.out.println("🔴 [FTP] Reply String: " + ftpClient.getReplyString());
            }

        } catch (Exception e) {
            System.out.println("🔴 [FTP] EXCEPCIÓN CAPTURADA:");
            System.out.println("🔴 [FTP] Tipo: " + e.getClass().getSimpleName());
            System.out.println("🔴 [FTP] Mensaje: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al subir archivo al FTP: " + e.getMessage()).build();
        } finally {
            System.out.println("🔵 [FTP] Cerrando conexiones...");
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (Exception ignored) {
            }
            try {
                ftpClient.logout();
            } catch (Exception ignored) {
            }
            try {
                ftpClient.disconnect();
            } catch (Exception ignored) {
            }
            System.out.println("🔵 [FTP] Conexiones cerradas");
        }

        if (uploaded) {
            System.out.println("🟢 [FTP] ========== UPLOAD EXITOSO ==========");
            String json = String.format("{\"mensaje\":\"Archivo subido correctamente al FTP\",\"ruta\":\"%s\"}",
                    rutaCompleta);
            return Response.ok(json).type(MediaType.APPLICATION_JSON).build();
        } else {
            System.out.println("🔴 [FTP] ========== UPLOAD FALLIDO ==========");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("No se pudo subir el archivo al FTP").build();
        }
    }

    @DELETE
    @Path("/delete/{filename}")
    public Response deleteFile(@PathParam("filename") String filename) {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(FTP_HOST, FTP_PORT);
            boolean login = ftpClient.login(FTP_USER, FTP_PASS);
            if (!login) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("No se pudo autenticar en el servidor FTP").build();
            }

            boolean deleted = ftpClient.deleteFile(filename);
            ftpClient.logout();
            ftpClient.disconnect();

            if (deleted) {
                return Response.ok("Archivo eliminado: " + filename).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("No se pudo eliminar el archivo: " + filename).build();
            }

        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al eliminar archivo: " + e.getMessage()).build();
        }
    }

    @GET
    @Path("/image/{filename}")
    @Produces("image/*")
    public Response getImage(@PathParam("filename") String filename) {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(FTP_HOST, FTP_PORT);
            boolean login = ftpClient.login(FTP_USER, FTP_PASS);
            if (!login) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("No se pudo autenticar en el servidor FTP").build();
            }

            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            // Construir la ruta completa del archivo
            String rutaCompleta = "/imagenes/equipos/" + filename;

            // Obtener el archivo desde el FTP
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            boolean retrieved = ftpClient.retrieveFile(rutaCompleta, outputStream);

            ftpClient.logout();
            ftpClient.disconnect();

            if (retrieved && outputStream.size() > 0) {
                byte[] imageData = outputStream.toByteArray();

                // Determinar el tipo de contenido basado en la extensión del archivo
                String contentType = "image/jpeg"; // default
                if (filename.toLowerCase().endsWith(".png")) {
                    contentType = "image/png";
                } else if (filename.toLowerCase().endsWith(".gif")) {
                    contentType = "image/gif";
                } else if (filename.toLowerCase().endsWith(".webp")) {
                    contentType = "image/webp";
                }

                return Response.ok(imageData)
                        .header("Content-Type", contentType)
                        .header("Cache-Control", "max-age=3600")
                        .build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Imagen no encontrada: " + filename).build();
            }

        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al obtener imagen: " + e.getMessage()).build();
        }
    }
}
