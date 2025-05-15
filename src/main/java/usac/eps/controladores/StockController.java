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
import usac.eps.modelos.ProductoModel;
import usac.eps.modelos.SedeModel;
import usac.eps.modelos.StockModel;
import usac.eps.repositorios.ProductoRepository;
import usac.eps.repositorios.SedeRepository;
import usac.eps.repositorios.StockRepository;

@Path("/Stock")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class StockController {

    @Inject
    private StockRepository stockRepository;

    @Inject
    private ProductoRepository productoRepository;

    @Inject
    private SedeRepository sedeRepository;

    @GET
    public List<StockModel> getAll() {
        return stockRepository.findAll();
    }

    @GET
    @Path("PorProducto/{id:[0-9][0-9]*}")
    public Response findByProducto(@PathParam("id") final int id) {
        ProductoModel producto = productoRepository.findByIdProducto(id);
        if (producto == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        List<StockModel> stock = stockRepository.findByProductoModel(producto);

        if (stock.isEmpty()) {
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            return Response.ok(stock).build();
        }
    }

    @GET
    @Path("PorSede/{id:[0-9][0-9]*}")
    public Response findBySede(@PathParam("id") final int id) {
        SedeModel sede = sedeRepository.findByIdSede(id);
        if (sede == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        List<StockModel> stock = stockRepository.findBySedeModel(sede);

        if (stock.isEmpty()) {
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            return Response.ok(stock).build();
        }
    }

    @GET
    @Path("/{id:[0-9][0-9]*}")
    public Response findById(@PathParam("id") final long id) {
        try {
            StockModel stock = stockRepository.findByIdStock(id);
            if (stock == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(stock).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @POST
    public Response create(@Valid final StockModel stock) {
        try {
            StockModel result = stockRepository.saveAndFlush(stock);
            return Response.created(UriBuilder.fromResource(StockController.class)
                    .path(Long.toString(result.getIdStock())).build())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id:[0-9][0-9]*}")
    public Response update(@PathParam("id") long id, final StockModel entity) {
        try {
            StockModel existingEntity = stockRepository.findByIdStock(id);

            if (existingEntity == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            existingEntity.setProductoModel((entity.getProductoModel() != null) ? entity.getProductoModel() : existingEntity.getProductoModel());
            existingEntity.setSedeModel((entity.getSedeModel() != null) ? entity.getSedeModel() : existingEntity.getSedeModel());
            existingEntity.setCantidad(entity.getCantidad());
            existingEntity.setCantidadMinima(entity.getCantidadMinima());
            existingEntity.setCantidadMaxima(entity.getCantidadMaxima());
            existingEntity.setFechaCaducidad(entity.getFechaCaducidad());
            existingEntity.setUbicacionFisica((entity.getUbicacionFisica() != null) ? entity.getUbicacionFisica() : existingEntity.getUbicacionFisica());
            existingEntity.setFechaModificacion(entity.getFechaModificacion());

            StockModel result = stockRepository.saveAndFlush(existingEntity);
            return Response.created(UriBuilder.fromResource(StockController.class)
                    .path(Long.toString(result.getIdStock())).build())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{id:[0-9][0-9]*}")
    public Response deleteById(@PathParam("id") long id) {
        try {
            StockModel entity = stockRepository.findByIdStock(id);
            if (entity == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            stockRepository.attachAndRemove(entity);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }
}
