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
import usac.eps.modelos.TipoProductoModel;
import usac.eps.repositorios.TipoProductoRepository;

@Path("/TipoProducto")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class TipoProductoController {

    @Inject
    private TipoProductoRepository tipoProductoRepository;

    @GET
    public List<TipoProductoModel> getAll() {
        return tipoProductoRepository.findAll();
    }

    @GET
    @Path("/{id:[0-9][0-9]*}")
    public Response findById(@PathParam("id") final int id) {
        try {
            TipoProductoModel tipoProducto = tipoProductoRepository.findByIdTipoProducto(id);
            if (tipoProducto == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(tipoProducto).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @POST
    public Response create(@Valid final TipoProductoModel tipoProducto) {
        try {
            TipoProductoModel result = tipoProductoRepository.saveAndFlush(tipoProducto);
            return Response.created(UriBuilder.fromResource(TipoProductoController.class)
                            .path(Integer.valueOf(result.getIdTipoProducto()).toString()).build())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id:[0-9][0-9]*}")
    public Response update(@PathParam("id") int id, final TipoProductoModel entity) {
        try {
            TipoProductoModel existingEntity = tipoProductoRepository.findByIdTipoProducto(id);

            if (existingEntity == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
//            if (entity == null) {
//                return Response.status(Response.Status.NOT_FOUND).build();
//            }

            existingEntity.setNombre(entity.getNombre());
            existingEntity.setDescripcion(entity.getDescripcion());
            existingEntity.setFechaModificacion(entity.getFechaModificacion());

            TipoProductoModel result = tipoProductoRepository.saveAndFlush(existingEntity);
            return Response.created(UriBuilder.fromResource(TipoProductoController.class)
                            .path(Integer.valueOf(result.getIdTipoProducto()).toString()).build())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }

    }
    
    @DELETE
    @Path("/{id:[0-9][0-9]*}")
    public Response deleteById(@PathParam("id") final int id) {
        try {
           TipoProductoModel entity = tipoProductoRepository.findByIdTipoProducto(id);
        if (entity == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        tipoProductoRepository.attachAndRemove(entity);
        return Response.ok().build(); 
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
        
    }

}
