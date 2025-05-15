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
import usac.eps.modelos.ProductoModel;
import usac.eps.modelos.TipoProductoModel;
import usac.eps.modelos.UnidadMedidaModel;
import usac.eps.repositorios.PresentacionProductoRepository;
import usac.eps.repositorios.ProductoRepository;
import usac.eps.repositorios.TipoProductoRepository;
import usac.eps.repositorios.UnidadMedidaRepository;

@Path("/Producto")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class ProductoController {

    @Inject
    private ProductoRepository productoRepository;

    @Inject
    private TipoProductoRepository tipoProductoRepository;

    @Inject
    private UnidadMedidaRepository unidadMedidaRepository;

    @Inject
    private PresentacionProductoRepository presentacionProductoRepository;

    @GET
    public List<ProductoModel> getAll() {
        return productoRepository.findAll();
    }

    @GET
    @Path("PorTipo/{id:[0-9][0-9]*}")
    public Response findByTipoProducto(@PathParam("id") final int id) {
        TipoProductoModel tipoProducto = tipoProductoRepository.findByIdTipoProducto(id);
        if (tipoProducto == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        List<ProductoModel> productos = productoRepository.findByTipoProductoModel(tipoProducto);

        if (productos.isEmpty()) {
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            return Response.ok(productos).build();
        }
    }

    @GET
    @Path("PorUnidadMedida/{id:[0-9][0-9]*}")
    public Response findByUnidadMedida(@PathParam("id") final int id) {
        UnidadMedidaModel unidadMedida = unidadMedidaRepository.findByIdUnidadMedidaProducto(id);
        if (unidadMedida == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        List<ProductoModel> productos = productoRepository.findByUnidadMedidaModel(unidadMedida);

        if (productos.isEmpty()) {
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            return Response.ok(productos).build();
        }

    }

    @GET
    @Path("PorPresentacion/{id:[0-9][0-9]*}")
    public Response findByPresentacion(@PathParam("id") final int id) {
        PresentacionProductoModel presentacionProducto = presentacionProductoRepository.findByIdPresentacionProducto(id);
        if (presentacionProducto == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        List<ProductoModel> productos = productoRepository.findByPresentacionProductoModel(presentacionProducto);

        if (productos.isEmpty()) {
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            return Response.ok(productos).build();
        }
    }

    @GET
    @Path("/{id:[0-9][0-9]*}")
    public Response findById(@PathParam("id") final int id) {
        try {
            ProductoModel producto = productoRepository.findByIdProducto(id);
            if (producto == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(producto).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @POST
    public Response create(@Valid final ProductoModel producto) {
        try {
            ProductoModel result = productoRepository.saveAndFlush(producto);
            return Response.created(UriBuilder.fromResource(ProductoController.class)
                    .path(Integer.valueOf(result.getIdProducto()).toString()).build())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id:[0-9][0-9]*}")
    public Response update(@PathParam("id") int id, final ProductoModel entity) {
        try {
            ProductoModel existingEntity = productoRepository.findByIdProducto(id);

            if (existingEntity == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            existingEntity.setTipoProductoModel((entity.getTipoProductoModel() != null) ? entity.getTipoProductoModel() : existingEntity.getTipoProductoModel());
            existingEntity.setUnidadMedidaModel((entity.getUnidadMedidaModel() != null) ? entity.getUnidadMedidaModel() : existingEntity.getUnidadMedidaModel());
            existingEntity.setPresentacionProductoModel((entity.getPresentacionProductoModel() != null) ? entity.getPresentacionProductoModel() : existingEntity.getPresentacionProductoModel());
            existingEntity.setNombre((entity.getNombre() != null) ? entity.getNombre() : existingEntity.getNombre());
            existingEntity.setPrecioCompra(entity.getPrecioCompra());
            existingEntity.setPrecioVenta(entity.getPrecioVenta());
            existingEntity.setPrecioUnitario(entity.getPrecioUnitario());
            existingEntity.setDescripcion(entity.getDescripcion());
            existingEntity.setFechaModificacion(entity.getFechaModificacion());

            ProductoModel result = productoRepository.saveAndFlush(existingEntity);
            return Response.created(UriBuilder.fromResource(ProductoController.class)
                    .path(Integer.valueOf(result.getIdProducto()).toString()).build())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }

    }

    @DELETE
    @Path("/{id:[0-9][0-9]*}")
    public Response deleteById(@PathParam("id") final int id) {
        try {
            ProductoModel entity = productoRepository.findByIdProducto(id);
            if (entity == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            productoRepository.attachAndRemove(entity);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }

    }

}
