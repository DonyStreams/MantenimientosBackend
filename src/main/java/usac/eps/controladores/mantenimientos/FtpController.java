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
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(@Multipart MultipartBody multipartBody) {
        FTPClient ftpClient = new FTPClient();
        try {
            Attachment attachment = multipartBody.getRootAttachment(); // o getAttachment("file") si necesitas por
                                                                       // nombre
            if (attachment == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Archivo no recibido").build();
            }

            InputStream inputStream = attachment.getDataHandler().getInputStream();
            String fileName = attachment.getContentDisposition().getParameter("filename");

            ftpClient.connect(FTP_HOST, FTP_PORT);
            boolean login = ftpClient.login(FTP_USER, FTP_PASS);
            if (!login) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("No se pudo autenticar en el servidor FTP").build();
            }
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            boolean uploaded = ftpClient.storeFile(fileName, inputStream);
            inputStream.close();
            ftpClient.logout();
            ftpClient.disconnect();

            if (uploaded) {
                return Response.ok("Archivo subido correctamente al FTP").build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("No se pudo subir el archivo al FTP").build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al subir archivo al FTP: " + e.getMessage()).build();
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
