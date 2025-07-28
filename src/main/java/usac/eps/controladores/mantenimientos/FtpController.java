package usac.eps.controladores.mantenimientos;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
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
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(FTP_HOST, FTP_PORT);
            boolean login = ftpClient.login(FTP_USER, FTP_PASS);
            if (login) {
                ftpClient.logout();
                return Response.ok("Conexión FTP exitosa").build();
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).entity("No se pudo autenticar en el servidor FTP")
                        .build();
            }
        } catch (IOException e) {
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
        FTPClient ftpClient = new FTPClient();
        boolean uploaded = false;
        String rutaCompleta = null;
        try {
            if (fileName == null || fileName.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Debe enviar el nombre del archivo en el header X-Filename").build();
            }

            ftpClient.connect(FTP_HOST, FTP_PORT);
            boolean login = ftpClient.login(FTP_USER, FTP_PASS);
            if (!login) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("No se pudo autenticar en el servidor FTP").build();
            }
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            // Crear el directorio /imagenes/equipos si no existe
            String directorio = "/imagenes/equipos";
            ftpClient.makeDirectory(directorio);

            // Guardar el archivo en la ruta /imagenes/equipos/nombreArchivo
            rutaCompleta = directorio + "/" + fileName;

            // Verificar si el archivo ya existe
            if (ftpClient.listFiles(rutaCompleta) != null && ftpClient.listFiles(rutaCompleta).length > 0) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("Ya existe un archivo con ese nombre en la ruta: " + rutaCompleta).build();
            }

            uploaded = ftpClient.storeFile(rutaCompleta, inputStream);
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al subir archivo al FTP: " + e.getMessage()).build();
        } finally {
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
        }
        if (uploaded) {
            String json = String.format("{\"mensaje\":\"Archivo subido correctamente al FTP\",\"ruta\":\"%s\"}",
                    rutaCompleta);
            return Response.ok(json).type(MediaType.APPLICATION_JSON).build();
        } else {
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
}
