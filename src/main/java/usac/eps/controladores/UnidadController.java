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
import usac.eps.modelos.UnidadModel;
import usac.eps.repositorios.UnidadRepository;

@Path("/Unidad")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class UnidadController {

    @Inject
    private UnidadRepository unidadRepository;

    @GET
    public List<UnidadModel> getAll() {
        return unidadRepository.findAll();
    }

    @GET
    @Path("/{id:[0-9][0-9]*}")
    public Response findById(@PathParam("id") final int id) {
        try {
            UnidadModel unidad = unidadRepository.findByIdUnidad(id);
            if (unidad == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(unidad).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @POST
    public Response create(@Valid final UnidadModel unidad) {
        try {
            UnidadModel result = unidadRepository.saveAndFlush(unidad);
            return Response.created(UriBuilder.fromResource(UnidadController.class)
                            .path(Integer.valueOf(result.getIdUnidad()).toString()).build())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id:[0-9][0-9]*}")
    public Response update(@PathParam("id") int id, final UnidadModel entity) {
        try {
            UnidadModel existingEntity = unidadRepository.findByIdUnidad(id);

            if (existingEntity == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
//            if (entity == null) {
//                return Response.status(Response.Status.NOT_FOUND).build();
//            }

            existingEntity.setNombre(entity.getNombre());
            existingEntity.setDescripcion(entity.getDescripcion());
            existingEntity.setFechaModificacion(entity.getFechaModificacion());

            UnidadModel result = unidadRepository.saveAndFlush(existingEntity);
            return Response.created(UriBuilder.fromResource(UnidadController.class)
                            .path(Integer.valueOf(result.getIdUnidad()).toString()).build())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }

    }
    
    @DELETE
    @Path("/{id:[0-9][0-9]*}")
    public Response deleteById(@PathParam("id") final int id) {
        try {
           UnidadModel entity = unidadRepository.findByIdUnidad(id);
        if (entity == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        unidadRepository.attachAndRemove(entity);
        return Response.ok().build(); 
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
        
    }

}
