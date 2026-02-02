package usac.eps.controladores.mantenimientos;

import usac.eps.modelos.mantenimientos.TipoMantenimientoModel;
import usac.eps.modelos.mantenimientos.UsuarioMantenimientoModel;
import usac.eps.repositorios.mantenimientos.TipoMantenimientoRepository;
import usac.eps.repositorios.mantenimientos.UsuarioMantenimientoRepository;
import usac.eps.seguridad.RequiresRole;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;

@Path("/tipos-mantenimiento")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class TipoMantenimientoController {
    @Inject
    private TipoMantenimientoRepository tipoMantenimientoRepository;

    @Inject
    private UsuarioMantenimientoRepository usuarioRepository;

    @Context
    private HttpServletRequest request;

    @GET
    public List<TipoMantenimientoModel> getAll() {
        return tipoMantenimientoRepository.findAll();
    }

    @GET
    @Path("/activos")
    public List<TipoMantenimientoModel> getActivos() {
        return tipoMantenimientoRepository.findActivos();
    }

    @GET
    @Path("/{id}")
    public TipoMantenimientoModel getById(@PathParam("id") Integer id) {
        return tipoMantenimientoRepository.findByIdTipo(id);
    }

    @POST
    public Response create(TipoMantenimientoModel tipo) {
        try {
            // Establecer fecha de creación
            tipo.setFechaCreacion(new Date());

            // Establecer estado por defecto si no viene
            if (tipo.getEstado() == null) {
                tipo.setEstado(true);
            }

            // Asignar SOLO usuario de creación (no modificación en creación)
            try {
                String keycloakId = (String) request.getAttribute("keycloakId");
                if (keycloakId != null) {
                    UsuarioMantenimientoModel usuario = usuarioRepository.findByKeycloakId(keycloakId);
                    if (usuario != null) {
                        tipo.setUsuarioCreacion(usuario);
                    }
                }
            } catch (Exception e) {
                System.out.println("⚠️ No se pudo asignar usuario de creación: " + e.getMessage());
            }

            tipoMantenimientoRepository.save(tipo);
            return Response.status(Response.Status.CREATED).entity(tipo).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Integer id, TipoMantenimientoModel tipo) {
        try {
            TipoMantenimientoModel tipoExistente = tipoMantenimientoRepository.findByIdTipo(id);
            if (tipoExistente == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            // Establecer el ID
            tipo.setIdTipo(id);

            // Preservar campos de auditoría original
            tipo.setFechaCreacion(tipoExistente.getFechaCreacion());
            tipo.setUsuarioCreacion(tipoExistente.getUsuarioCreacion());

            // Actualizar fecha de modificación
            tipo.setFechaModificacion(new Date());

            // Asignar usuario modificador
            try {
                String keycloakId = (String) request.getAttribute("keycloakId");
                if (keycloakId != null) {
                    UsuarioMantenimientoModel usuario = usuarioRepository.findByKeycloakId(keycloakId);
                    if (usuario != null) {
                        tipo.setUsuarioModificacion(usuario);
                    }
                }
            } catch (Exception e) {
                System.out.println("⚠️ No se pudo asignar usuario de modificación: " + e.getMessage());
            }

            tipoMantenimientoRepository.save(tipo);
            return Response.ok(tipo).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @RequiresRole({ "ADMIN" })
    public Response delete(@PathParam("id") Integer id) {
        TipoMantenimientoModel tipo = tipoMantenimientoRepository.findByIdTipo(id);
        if (tipo != null) {
            try {
                tipoMantenimientoRepository.deleteByIdTipo(id);
                return Response.noContent().build();
            } catch (Exception e) {
                e.printStackTrace();
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
