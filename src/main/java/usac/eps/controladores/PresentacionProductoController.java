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
import usac.eps.modelos.PresentacionProductoModel;
import usac.eps.repositorios.PresentacionProductoRepository;

@Path("/PresentacionProducto")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class PresentacionProductoController {

    @Inject
    private PresentacionProductoRepository presentacionProductoRepository;

    @GET
    public List<PresentacionProductoModel> getAll() {
        return presentacionProductoRepository.findAll();
    }

    @GET
    @Path("/{id:[0-9][0-9]*}")
    public Response findById(@PathParam("id") final int id) {
        try {
            PresentacionProductoModel presentacionProducto = presentacionProductoRepository.findByIdPresentacionProducto(id);
            if (presentacionProducto == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(presentacionProducto).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @POST
    public Response create(@Valid final PresentacionProductoModel presentacionProducto) {
        try {
            PresentacionProductoModel result = presentacionProductoRepository.saveAndFlush(presentacionProducto);
            return Response.created(UriBuilder.fromResource(PresentacionProductoController.class)
                            .path(Integer.valueOf(result.getIdPresentacionProducto()).toString()).build())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id:[0-9][0-9]*}")
    public Response update(@PathParam("id") int id, final PresentacionProductoModel entity) {
        try {
            PresentacionProductoModel existingEntity = presentacionProductoRepository.findByIdPresentacionProducto(id);

            if (existingEntity == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
//            if (entity == null) {
//                return Response.status(Response.Status.NOT_FOUND).build();
//            }

            existingEntity.setNombre(entity.getNombre());
            existingEntity.setDescripcion(entity.getDescripcion());
            existingEntity.setFechaModificacion(entity.getFechaModificacion());

            PresentacionProductoModel result = presentacionProductoRepository.saveAndFlush(existingEntity);
            return Response.created(UriBuilder.fromResource(PresentacionProductoController.class)
                            .path(Integer.valueOf(result.getIdPresentacionProducto()).toString()).build())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }

    }
    
    @DELETE
    @Path("/{id:[0-9][0-9]*}")
    public Response deleteById(@PathParam("id") final int id) {
        try {
           PresentacionProductoModel entity = presentacionProductoRepository.findByIdPresentacionProducto(id);
        if (entity == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        presentacionProductoRepository.attachAndRemove(entity);
        return Response.ok().build(); 
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
        
    }

}
