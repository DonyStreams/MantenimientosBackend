package usac.eps.controladores.mantenimientos;

import usac.eps.modelos.mantenimientos.HistorialEquipoModel;
import usac.eps.repositorios.mantenimientos.HistorialEquipoRepository;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/historial-equipos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class HistorialEquipoController {
    @Inject
    private HistorialEquipoRepository historialEquipoRepository;

    @GET
    public List<HistorialEquipoModel> getAll() {
        return historialEquipoRepository.findAll();
    }

    @GET
    @Path("/{id}")
    public HistorialEquipoModel getById(@PathParam("id") Integer id) {
        return historialEquipoRepository.findByIdHistorial(id);
    }

    @POST
    public Response create(HistorialEquipoModel historial) {
        historialEquipoRepository.save(historial);
        return Response.status(Response.Status.CREATED).entity(historial).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Integer id, HistorialEquipoModel historial) {
        historial.setIdHistorial(id);
        historialEquipoRepository.save(historial);
        return Response.ok(historial).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Integer id) {
        HistorialEquipoModel historial = historialEquipoRepository.findByIdHistorial(id);
        if (historial != null) {
            historialEquipoRepository.remove(historial);
            return Response.noContent().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
