package usac.eps.controladores.mantenimientos;

import usac.eps.modelos.mantenimientos.EjecucionMantenimientoModel;
import usac.eps.repositorios.mantenimientos.EjecucionMantenimientoRepository;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/ejecuciones-mantenimiento")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class EjecucionMantenimientoController {
    @Inject
    private EjecucionMantenimientoRepository ejecucionMantenimientoRepository;

    @GET
    public List<EjecucionMantenimientoModel> getAll() {
        return ejecucionMantenimientoRepository.findAll();
    }

    @GET
    @Path("/{id}")
    public EjecucionMantenimientoModel getById(@PathParam("id") Integer id) {
        return ejecucionMantenimientoRepository.findByIdEjecucion(id);
    }

    @POST
    public Response create(EjecucionMantenimientoModel ejecucion) {
        ejecucionMantenimientoRepository.save(ejecucion);
        return Response.status(Response.Status.CREATED).entity(ejecucion).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Integer id, EjecucionMantenimientoModel ejecucion) {
        ejecucion.setIdEjecucion(id);
        ejecucionMantenimientoRepository.save(ejecucion);
        return Response.ok(ejecucion).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Integer id) {
        EjecucionMantenimientoModel ejecucion = ejecucionMantenimientoRepository.findByIdEjecucion(id);
        if (ejecucion != null) {
            ejecucionMantenimientoRepository.remove(ejecucion);
            return Response.noContent().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
