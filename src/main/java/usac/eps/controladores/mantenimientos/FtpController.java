package usac.eps.controladores.mantenimientos;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import usac.eps.util.ConfigUtil;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

@Path("/ftp")
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class FtpController {

    // Configuración FTP usando utilidad general
    private static final String FTP_HOST = ConfigUtil.get("ftp.host", "localhost");
    private static final int FTP_PORT = Integer.parseInt(ConfigUtil.get("ftp.port", "21"));
    private static final String FTP_USER = ConfigUtil.get("ftp.user", "usuario");
    private static final String FTP_PASS = ConfigUtil.get("ftp.pass", "contraseña");

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
                return Response.status(Status.UNAUTHORIZED).entity("No se pudo autenticar en el servidor FTP").build();
            }
        } catch (IOException e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error de conexión FTP: " + e.getMessage())
                    .build();
        } finally {
            try {
                ftpClient.disconnect();
            } catch (Exception ignored) {
            }
        }
    }

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(
            @FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail) {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(FTP_HOST, FTP_PORT);
            boolean login = ftpClient.login(FTP_USER, FTP_PASS);
            if (!login) {
                return Response.status(Status.UNAUTHORIZED).entity("No se pudo autenticar en el servidor FTP").build();
            }
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            boolean done = ftpClient.storeFile(fileDetail.getFileName(), fileInputStream);
            if (done) {
                return Response.status(Status.CREATED)
                        .entity("Archivo subido correctamente: " + fileDetail.getFileName()).build();
            } else {
                return Response.status(Status.INTERNAL_SERVER_ERROR).entity("No se pudo subir el archivo").build();
            }
        } catch (IOException e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error al subir archivo: " + e.getMessage())
                    .build();
        } finally {
            try {
                ftpClient.logout();
            } catch (Exception ignored) {
            }
            try {
                ftpClient.disconnect();
            } catch (Exception ignored) {
            }
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
                return Response.status(Status.UNAUTHORIZED).entity("No se pudo autenticar en el servidor FTP").build();
            }
            boolean deleted = ftpClient.deleteFile(filename);
            if (deleted) {
                return Response.ok("Archivo eliminado: " + filename).build();
            } else {
                return Response.status(Status.NOT_FOUND).entity("No se pudo eliminar el archivo: " + filename).build();
            }
        } catch (IOException e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error al eliminar archivo: " + e.getMessage())
                    .build();
        } finally {
            try {
                ftpClient.logout();
            } catch (Exception ignored) {
            }
            try {
                ftpClient.disconnect();
            } catch (Exception ignored) {
            }
        }
    }
}