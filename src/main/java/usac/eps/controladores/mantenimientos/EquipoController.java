package usac.eps.controladores.mantenimientos;

import usac.eps.modelos.mantenimientos.EquipoModel;
import usac.eps.repositorios.mantenimientos.EquipoRepository;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/equipos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class EquipoController {
    @Inject
    private EquipoRepository equipoRepository;

    @GET
    public List<EquipoModel> getAll() {
        return equipoRepository.findAll();
    }

    @GET
    @Path("/{id}")
    public EquipoModel getById(@PathParam("id") Integer id) {
        return equipoRepository.findByIdEquipo(id);
    }

    @POST
    public Response create(EquipoModel equipo) {
        equipoRepository.save(equipo);
        return Response.status(Response.Status.CREATED).entity(equipo).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Integer id, EquipoModel equipo) {
        equipo.setIdEquipo(id);
        equipoRepository.save(equipo);
        return Response.ok(equipo).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Integer id) {
        EquipoModel equipo = equipoRepository.findByIdEquipo(id);
        if (equipo != null) {
            equipoRepository.remove(equipo);
            return Response.noContent().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
