package usac.eps.controladores.mantenimientos;

import usac.eps.modelos.mantenimientos.ContratoModel;
import usac.eps.repositorios.mantenimientos.ContratoRepository;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/contratos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class ContratoController {
    @Inject
    private ContratoRepository contratoRepository;

    @GET
    public List<ContratoModel> getAll() {
        return contratoRepository.findAll();
    }

    @GET
    @Path("/activos")
    public List<ContratoModel> getActivos() {
        return contratoRepository.findByEstado(true);
    }

    @GET
    @Path("/{id}")
    public ContratoModel getById(@PathParam("id") Integer id) {
        return contratoRepository.findByIdContrato(id);
    }

    @POST
    public Response create(ContratoModel contrato) {
        contratoRepository.save(contrato);
        return Response.status(Response.Status.CREATED).entity(contrato).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Integer id, ContratoModel contrato) {
        contrato.setIdContrato(id);
        contratoRepository.save(contrato);
        return Response.ok(contrato).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Integer id) {
        ContratoModel contrato = contratoRepository.findByIdContrato(id);
        if (contrato != null) {
            contratoRepository.remove(contrato);
            return Response.noContent().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
