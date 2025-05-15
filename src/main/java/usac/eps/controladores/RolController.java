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
import usac.eps.modelos.RolModel;
import usac.eps.repositorios.RolRepository;

@Path("/Rol")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class RolController {

    @Inject
    private RolRepository rolRepository;

    @GET
    public List<RolModel> getAll() {
        return rolRepository.findAll();
    }

    @GET
    @Path("/{id:[0-9][0-9]*}")
    public Response findById(@PathParam("id") final int id) {
        try {
            RolModel rol = rolRepository.findByIdRol(id);
            if (rol == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(rol).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @POST
    public Response create(@Valid final RolModel rol) {
        try {
            RolModel result = rolRepository.saveAndFlush(rol);
            return Response.created(UriBuilder.fromResource(RolController.class)
                            .path(Integer.valueOf(result.getIdRol()).toString()).build())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id:[0-9][0-9]*}")
    public Response update(@PathParam("id") int id, final RolModel entity) {
        try {
            RolModel existingEntity = rolRepository.findByIdRol(id);

            if (existingEntity == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }      
            
            existingEntity.setNombre((entity.getNombre() != null) ? entity.getNombre() : existingEntity.getNombre());
            existingEntity.setDescripcion((entity.getDescripcion() != null) ? entity.getDescripcion() : existingEntity.getDescripcion());
            existingEntity.setFechaModificacion(entity.getFechaModificacion());
            
            //existingEntity.setFechaModificacion((entity.getFechaModificacion() != null) ? entity.getFechaModificacion() : existingEntity.getFechaModificacion());
            //existingEntity.setNombre(entity.getNombre());
            //existingEntity.setDescripcion(entity.getDescripcion());
            //existingEntity.setFechaModificacion(entity.getFechaModificacion());

            RolModel result = rolRepository.saveAndFlush(existingEntity);
            return Response.created(UriBuilder.fromResource(RolController.class)
                            .path(Integer.valueOf(result.getIdRol()).toString()).build())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }

    }
    
    @DELETE
    @Path("/{id:[0-9][0-9]*}")
    public Response deleteById(@PathParam("id") final int id) {
        try {
           RolModel entity = rolRepository.findByIdRol(id);
        if (entity == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        rolRepository.attachAndRemove(entity);
        return Response.ok().build(); 
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
        
    }

}
