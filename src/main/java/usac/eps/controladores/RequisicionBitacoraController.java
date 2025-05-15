package usac.eps.controladores;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import usac.eps.modelos.RequisicionBitacoraModel;
import usac.eps.modelos.RequisicionModel;
import usac.eps.modelos.UsuarioModel;
import usac.eps.repositorios.RequisicionBitacoraRepository;
import usac.eps.repositorios.RequisicionRepository;
import usac.eps.repositorios.UsuarioRepository;

@Path("/RequisicionBitacora")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class RequisicionBitacoraController {

    @Inject
    private RequisicionBitacoraRepository requisicionBitacoraRepository;

    @Inject
    private RequisicionRepository requisicionRepository;

    @Inject
    private UsuarioRepository usuarioRepository;

    @GET
    public List<RequisicionBitacoraModel> getAll() {
        return requisicionBitacoraRepository.findAll();
    }

    @GET
    @Path("PorRequisicion/{id:[0-9][0-9]*}")
    public Response findByTipoRequisicion(@PathParam("id") final Long id) {
        try {
            RequisicionModel requisicion = requisicionRepository.findByIdRequisicion(id);
            if (requisicion == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            List<RequisicionBitacoraModel> bitacoras = requisicionBitacoraRepository.findByRequisicion(requisicion);
            if (bitacoras.isEmpty()) {
                return Response.status(Response.Status.NO_CONTENT).build();
            } else {
                return Response.ok(bitacoras).build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("PorUsuario/{id:[0-9][0-9]*}")
    public Response findByUsuario(@PathParam("id") final Long id) {
        try {
            UsuarioModel usuario = usuarioRepository.findByIdUsuario(id);
            if (usuario == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            List<RequisicionBitacoraModel> bitacoras = requisicionBitacoraRepository.findByUsuario(usuario);
            if (bitacoras.isEmpty()) {
                return Response.status(Response.Status.NO_CONTENT).build();
            } else {
                return Response.ok(bitacoras).build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }

    }

    @GET
    @Path("/{id:[0-9][0-9]*}")
    public Response findById(@PathParam("id") final Long id) {
        try {
            RequisicionBitacoraModel bitacora = requisicionBitacoraRepository.findByIdRequisicionBitacora(id);
            if (bitacora == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            } else {
                return Response.ok(bitacora).build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @POST
    public Response create(@Valid final RequisicionBitacoraModel bitacora) {

        try {
            RequisicionBitacoraModel result = requisicionBitacoraRepository.save(bitacora);
            return Response.created(UriBuilder.fromResource(RequisicionBitacoraController.class)
                    .path(Long.toString(result.getIdRequisicionBitacora())).build())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }

    }

    @PUT
    @Path("/{id:[0-9][0-9]*}")
    public Response update(@PathParam("id") Long id, @Valid RequisicionBitacoraModel bitacora) {
        try {
            RequisicionBitacoraModel existingBitacora = requisicionBitacoraRepository.findByIdRequisicionBitacora(id);
            if (existingBitacora == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            bitacora.setIdRequisicionBitacora(id);
            RequisicionBitacoraModel result = requisicionBitacoraRepository.save(bitacora);
            return Response.created(UriBuilder.fromResource(RequisicionBitacoraController.class)
                    .path(Long.toString(result.getIdRequisicionBitacora())).build())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{id:[0-9][0-9]*}")
    public Response delete(@PathParam("id") Long id) {
        try {
            RequisicionBitacoraModel entity = requisicionBitacoraRepository.findByIdRequisicionBitacora(id);
            if (entity == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            requisicionBitacoraRepository.attachAndRemove(entity);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
