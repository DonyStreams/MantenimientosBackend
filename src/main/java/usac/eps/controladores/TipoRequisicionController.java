package usac.eps.controladores;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import java.util.List;
import javax.validation.Valid;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import usac.eps.modelos.TipoRequisicionModel;
import usac.eps.repositorios.TipoRequisicionRepository;

@Path("/TipoRequisicion")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class TipoRequisicionController {

    @Inject
    private TipoRequisicionRepository tipoRequisicionRepository;

    @GET
    public List<TipoRequisicionModel> getAll() {
        return tipoRequisicionRepository.findAll();
    }

    @GET
    @Path("/{id:[0-9][0-9]*}")
    public Response findById(@PathParam("id") final int id) {
        try {
            TipoRequisicionModel tipoRequisicion = tipoRequisicionRepository.findByIdTipoRequisicion(id);
            if (tipoRequisicion == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(tipoRequisicion).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @POST
    public Response create(@Valid final TipoRequisicionModel tipoRequisicion) {
        try {
            TipoRequisicionModel result = tipoRequisicionRepository.saveAndFlush(tipoRequisicion);
            return Response.created(UriBuilder.fromResource(TipoRequisicionController.class)
                            .path(Integer.valueOf(result.getIdTipoRequisicion()).toString()).build())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id:[0-9][0-9]*}")
    public Response update(@PathParam("id") int id, final TipoRequisicionModel entity) {
        try {
            TipoRequisicionModel existingEntity = tipoRequisicionRepository.findByIdTipoRequisicion(id);

            if (existingEntity == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
//            if (entity == null) {
//                return Response.status(Response.Status.NOT_FOUND).build();
//            }

            existingEntity.setNombre(entity.getNombre());
            existingEntity.setDescripcion(entity.getDescripcion());
            existingEntity.setFechaModificacion(entity.getFechaModificacion());

            TipoRequisicionModel result = tipoRequisicionRepository.saveAndFlush(existingEntity);
            return Response.created(UriBuilder.fromResource(TipoRequisicionController.class)
                            .path(Integer.valueOf(result.getIdTipoRequisicion()).toString()).build())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }

    }
    
    @DELETE
    @Path("/{id:[0-9][0-9]*}")
    public Response deleteById(@PathParam("id") final int id) {
        try {
           TipoRequisicionModel entity = tipoRequisicionRepository.findByIdTipoRequisicion(id);
        if (entity == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        tipoRequisicionRepository.attachAndRemove(entity);
        return Response.ok().build(); 
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
        
    }

}
