package usac.eps.controladores.mantenimientos;

import usac.eps.modelos.mantenimientos.TicketModel;
import usac.eps.repositorios.mantenimientos.TicketRepository;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/tickets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class TicketController {
    @Inject
    private TicketRepository ticketRepository;

    @GET
    public List<TicketModel> getAll() {
        return ticketRepository.findAll();
    }

    @GET
    @Path("/{id}")
    public TicketModel getById(@PathParam("id") Integer id) {
        return ticketRepository.findById(id);
    }

    @POST
    public Response create(TicketModel ticket) {
        ticketRepository.save(ticket);
        return Response.status(Response.Status.CREATED).entity(ticket).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Integer id, TicketModel ticket) {
        ticket.setId(id);
        ticketRepository.save(ticket);
        return Response.ok(ticket).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Integer id) {
        TicketModel ticket = ticketRepository.findById(id);
        if (ticket != null) {
            ticketRepository.remove(ticket);
            return Response.noContent().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
