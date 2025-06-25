package usac.eps.controladores.mantenimientos;

import usac.eps.modelos.mantenimientos.AreaModel;
import usac.eps.repositorios.mantenimientos.AreaRepository;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/areas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class AreaController {
    @Inject
    private AreaRepository areaRepository;

    @GET
    public List<AreaModel> getAll() {
        return areaRepository.findAll();
    }

    @GET
    @Path("/{id}")
    public AreaModel getById(@PathParam("id") Integer id) {
        return areaRepository.findByIdArea(id);
    }

    @POST
    public Response create(AreaModel area) {
        areaRepository.save(area);
        return Response.status(Response.Status.CREATED).entity(area).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Integer id, AreaModel area) {
        area.setIdArea(id);
        areaRepository.save(area);
        return Response.ok(area).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Integer id) {
        AreaModel area = areaRepository.findByIdArea(id);
        if (area != null) {
            areaRepository.remove(area);
            return Response.noContent().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
