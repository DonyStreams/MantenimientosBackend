package usac.eps.controladores;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import usac.eps.modelos.RequisicionDetalleModel;
import usac.eps.repositorios.ProductoRepository;
import usac.eps.repositorios.RequisicionRepository;

import java.util.List;
import usac.eps.repositorios.RequisicionDetalleRepository;

@Path("/RequisicionDetalle")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class RequisicionDetalleController {

    @Inject
    private RequisicionDetalleRepository requisicionDetalleRepository;

    @Inject
    private ProductoRepository productoRepository;

    @Inject
    private RequisicionRepository requisicionRepository;

    @GET
    public List<RequisicionDetalleModel> getAll() {
        return requisicionDetalleRepository.findAll();
    }

    @GET
    @Path("/{id:[0-9][0-9]*}")
    public Response getById(@PathParam("id") final Long id) {
        RequisicionDetalleModel detalle = requisicionDetalleRepository.findByIdRequisicionDetalle(id);
        if (detalle == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(detalle).build();
    }

    @GET
    @Path("PorRequisicion/{id:[0-9][0-9]*}")
    public Response findByRequisicion(@PathParam("id") final Long id) {
        List<RequisicionDetalleModel> detalles = requisicionDetalleRepository.findByRequisicion(requisicionRepository.findByIdRequisicion(id));
        if (detalles.isEmpty()) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        return Response.ok(detalles).build();
    }

    @GET
    @Path("PorProducto/{id:[0-9][0-9]*}")
    public Response findByProducto(@PathParam("id") final int id) {
        List<RequisicionDetalleModel> detalles = requisicionDetalleRepository.findByProducto(productoRepository.findByIdProducto(id));
        if (detalles.isEmpty()) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        return Response.ok(detalles).build();
    }

    @POST
    public Response create(RequisicionDetalleModel detalle) {
        try {
            RequisicionDetalleModel result = requisicionDetalleRepository.saveAndFlush(detalle);
            return Response.created(UriBuilder.fromResource(RequisicionDetalleController.class)
                    .path(Long.toString(result.getIdDetalleRequisicion())).build())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id:[0-9][0-9]*}")
    public Response update(@PathParam("id") Long id, RequisicionDetalleModel entity) {
        try {
            RequisicionDetalleModel existingEntity = requisicionDetalleRepository.findByIdRequisicionDetalle(id);

            if (existingEntity == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            existingEntity.setRequisicion(entity.getRequisicion());
            existingEntity.setProducto(entity.getProducto());
            existingEntity.setCantidadSolicitada(entity.getCantidadSolicitada());

            RequisicionDetalleModel result = requisicionDetalleRepository.saveAndFlush(existingEntity);
             return Response.created(UriBuilder.fromResource(RequisicionController.class)
                    .path(Long.toString(result.getIdDetalleRequisicion())).build())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{id:[0-9][0-9]*}")
    public Response deleteById(@PathParam("id") Long id) {
        try {
            RequisicionDetalleModel entity = requisicionDetalleRepository.findByIdRequisicionDetalle(id);
            if (entity == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            requisicionDetalleRepository.attachAndRemove(entity);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
