package usac.eps.controladores.mantenimientos;

import usac.eps.modelos.mantenimientos.ComentarioTicketModel;
import usac.eps.repositorios.mantenimientos.ComentarioTicketRepository;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/comentarios-ticket")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class ComentarioTicketController {
    @Inject
    private ComentarioTicketRepository comentarioTicketRepository;

    @GET
    public List<ComentarioTicketModel> getAll() {
        return comentarioTicketRepository.findAll();
    }

    @GET
    @Path("/{id}")
    public ComentarioTicketModel getById(@PathParam("id") Integer id) {
        return comentarioTicketRepository.findById(id);
    }

    @POST
    public Response create(ComentarioTicketModel comentario) {
        comentarioTicketRepository.save(comentario);
        return Response.status(Response.Status.CREATED).entity(comentario).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Integer id, ComentarioTicketModel comentario) {
        comentario.setId(id);
        comentarioTicketRepository.save(comentario);
        return Response.ok(comentario).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Integer id) {
        ComentarioTicketModel comentario = comentarioTicketRepository.findById(id);
        if (comentario != null) {
            comentarioTicketRepository.remove(comentario);
            return Response.noContent().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
