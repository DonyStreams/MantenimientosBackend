package usac.eps.controladores.mantenimientos;

import usac.eps.modelos.mantenimientos.AreaModel;
import usac.eps.modelos.mantenimientos.UsuarioMantenimientoModel;
import usac.eps.repositorios.mantenimientos.AreaRepository;
import usac.eps.repositorios.mantenimientos.UsuarioMantenimientoRepository;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/areas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class AreaController {
    private static final Logger LOGGER = Logger.getLogger(AreaController.class.getName());

    @Inject
    private AreaRepository areaRepository;

    @Inject
    private UsuarioMantenimientoRepository usuarioRepository;

    @Context
    private HttpServletRequest request;

    @GET
    public List<AreaModel> getAll() {
        return areaRepository.findAll();
    }

    @GET
    @Path("/activos")
    public List<AreaModel> getActivos() {
        return areaRepository.findByEstado(true);
    }

    @GET
    @Path("/{id}")
    public AreaModel getById(@PathParam("id") Integer id) {
        return areaRepository.findByIdArea(id);
    }

    @POST
    public Response create(AreaModel area) {
        try {
            // Obtener usuario desde el contexto de Keycloak
            UsuarioMantenimientoModel usuario = obtenerUsuarioActual();

            // Establecer fechas y usuario de creación
            area.setFechaCreacion(new Date());
            area.setUsuarioCreacion(usuario);

            areaRepository.save(area);
            return Response.status(Response.Status.CREATED).entity(area).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al crear área", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al crear área: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Integer id, AreaModel area) {
        try {
            // Verificar que el área existe
            AreaModel areaExistente = areaRepository.findByIdArea(id);
            if (areaExistente == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            // Establecer el ID y preservar campos que no deben cambiar
            area.setIdArea(id);

            // IMPORTANTE: Preservar la fecha de creación original
            area.setFechaCreacion(areaExistente.getFechaCreacion());

            // Preservar el usuario creador original
            area.setUsuarioCreacion(areaExistente.getUsuarioCreacion());

            // Establecer usuario de modificación
            UsuarioMantenimientoModel usuarioModificacion = obtenerUsuarioActual();
            area.setUsuarioModificacion(usuarioModificacion);
            area.setFechaModificacion(new Date());

            areaRepository.save(area);
            return Response.ok(area).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar área", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al actualizar área: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Integer id) {
        AreaModel area = areaRepository.findByIdArea(id);
        if (area != null) {
            try {
                areaRepository.deleteByIdArea(id);
                return Response.noContent().build();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error al eliminar área", e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("{\"error\": \"Error al eliminar área: " + e.getMessage() + "\"}")
                        .build();
            }
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    /**
     * Método helper para obtener el usuario actual desde Keycloak
     */
    private UsuarioMantenimientoModel obtenerUsuarioActual() {
        try {
            String keycloakId = (String) request.getAttribute("keycloakId");
            String username = (String) request.getAttribute("username");

            if (keycloakId != null) {
                // Buscar usuario en la base de datos
                UsuarioMantenimientoModel usuario = usuarioRepository.findByKeycloakId(keycloakId);
                if (usuario != null) {
                    return usuario;
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al obtener usuario actual", e);
        }

        // Si no se puede obtener el usuario, devolver null
        return null;
    }
}
