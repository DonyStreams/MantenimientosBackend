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
import usac.eps.modelos.StockBitacoraModel;
import usac.eps.modelos.StockModel;
import usac.eps.modelos.UsuarioModel;
import usac.eps.repositorios.StockBitacoraRepository;
import usac.eps.repositorios.StockRepository;
import usac.eps.repositorios.UsuarioRepository;

@Path("/StockBitacora")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class StockBitacoraController {

    @Inject
    private StockBitacoraRepository stockBitacoraRepository;

    @Inject
    private StockRepository stockRepository;

    @Inject
    private UsuarioRepository usuarioRepository;

    @GET
    public List<StockBitacoraModel> getAll() {
        return stockBitacoraRepository.findAll();
    }

    @GET
    @Path("PorStock/{id:[0-9][0-9]*}")
    public Response findByStock(@PathParam("id") final long id) {
        StockModel stock = stockRepository.findByIdStock(id);
        if (stock == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        List<StockBitacoraModel> stockBitacora = stockBitacoraRepository.findByStockModel(stock);

        if (stockBitacora.isEmpty()) {
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            return Response.ok(stockBitacora).build();
        }
    }

    @GET
    @Path("PorUsuario/{id:[0-9][0-9]*}")
    public Response findByUsuario(@PathParam("id") final Long id) {
        UsuarioModel usuario = usuarioRepository.findByIdUsuario(id);
        if (usuario == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        List<StockBitacoraModel> stockBitacora = stockBitacoraRepository.findByUsuarioModel(usuario);

        if (stockBitacora.isEmpty()) {
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            return Response.ok(stockBitacora).build();
        }
    }

    @GET
    @Path("/{id:[0-9][0-9]*}")
    public Response findById(@PathParam("id") final long id) {
        try {
            StockBitacoraModel stockBitacora = stockBitacoraRepository. findByIdStockBitacora(id);
            if (stockBitacora == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(stockBitacora).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @POST
    public Response create(@Valid final StockBitacoraModel stockBitacora) {
        try {
            StockBitacoraModel result = stockBitacoraRepository.saveAndFlush(stockBitacora);
            return Response.created(UriBuilder.fromResource(StockBitacoraController.class)
                    .path(Long.toString(result.getIdStockBitacora())).build())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id:[0-9][0-9]*}")
    public Response update(@PathParam("id") long id, final StockBitacoraModel entity) {
        try {
            StockBitacoraModel existingEntity = stockBitacoraRepository.findByIdStockBitacora(id);

            if (existingEntity == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            existingEntity.setStockModel((entity.getStockModel() != null) ? entity.getStockModel() : existingEntity.getStockModel());
            existingEntity.setUsuarioModel((entity.getUsuarioModel() != null) ? entity.getUsuarioModel() : existingEntity.getUsuarioModel());
            existingEntity.setAccion((entity.getAccion() != null) ? entity.getAccion() : existingEntity.getAccion());
            existingEntity.setCantidad(entity.getCantidad());
            existingEntity.setDescripcion((entity.getDescripcion() != null) ? entity.getDescripcion() : existingEntity.getDescripcion());
            existingEntity.setFechaModificacion(entity.getFechaModificacion());

            StockBitacoraModel result = stockBitacoraRepository.saveAndFlush(existingEntity);
            return Response.created(UriBuilder.fromResource(StockBitacoraController.class)
                    .path(Long.toString(result.getIdStockBitacora())).build())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{id:[0-9][0-9]*}")
    public Response deleteById(@PathParam("id") long id) {
        try {
            StockBitacoraModel entity = stockBitacoraRepository.findByIdStockBitacora(id);
            if (entity == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            stockBitacoraRepository.attachAndRemove(entity);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }
}
