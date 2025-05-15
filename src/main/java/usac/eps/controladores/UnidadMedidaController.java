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
import usac.eps.modelos.UnidadMedidaModel;
import usac.eps.repositorios.UnidadMedidaRepository;

@Path("/UnidadMedida")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class UnidadMedidaController {

    @Inject
    private UnidadMedidaRepository unidadMedidaRepository;

    @GET
    public List<UnidadMedidaModel> getAll() {
        return unidadMedidaRepository.findAll();
    }

    @GET
    @Path("/{id:[0-9][0-9]*}")
    public Response findById(@PathParam("id") final int id) {
        try {
            UnidadMedidaModel unidadMedida = unidadMedidaRepository.findByIdUnidadMedidaProducto(id);
            if (unidadMedida == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(unidadMedida).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @POST
    public Response create(@Valid final UnidadMedidaModel unidadMedida) {
        try {
            UnidadMedidaModel result = unidadMedidaRepository.saveAndFlush(unidadMedida);
            return Response.created(UriBuilder.fromResource(UnidadMedidaController.class)
                            .path(Integer.valueOf(result.getIdUnidadMedida()).toString()).build())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id:[0-9][0-9]*}")
    public Response update(@PathParam("id") int id, final UnidadMedidaModel entity) {
        try {
            UnidadMedidaModel existingEntity = unidadMedidaRepository.findByIdUnidadMedidaProducto(id);

            if (existingEntity == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
//            if (entity == null) {
//                return Response.status(Response.Status.NOT_FOUND).build();
//            }

            existingEntity.setNombre(entity.getNombre());
            existingEntity.setDescripcion(entity.getDescripcion());
            existingEntity.setFechaModificacion(entity.getFechaModificacion());

            UnidadMedidaModel result = unidadMedidaRepository.saveAndFlush(existingEntity);
            return Response.created(UriBuilder.fromResource(UnidadMedidaController.class)
                            .path(Integer.valueOf(result.getIdUnidadMedida()).toString()).build())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }

    }
    
    @DELETE
    @Path("/{id:[0-9][0-9]*}")
    public Response deleteById(@PathParam("id") final int id) {
        try {
           UnidadMedidaModel entity = unidadMedidaRepository.findByIdUnidadMedidaProducto(id);
        if (entity == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        unidadMedidaRepository.attachAndRemove(entity);
        return Response.ok().build(); 
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
        
    }

}
