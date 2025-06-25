package usac.eps.controladores.mantenimientos;

import usac.eps.modelos.mantenimientos.TipoComentarioModel;
import usac.eps.repositorios.mantenimientos.TipoComentarioRepository;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/tipos-comentario")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class TipoComentarioController {
    @Inject
    private TipoComentarioRepository tipoComentarioRepository;

    @GET
    public List<TipoComentarioModel> getAll() {
        return tipoComentarioRepository.findAll();
    }

    @GET
    @Path("/{id}")
    public TipoComentarioModel getById(@PathParam("id") Integer id) {
        return tipoComentarioRepository.findByIdTipo(id);
    }

    @POST
    public Response create(TipoComentarioModel tipo) {
        tipoComentarioRepository.save(tipo);
        return Response.status(Response.Status.CREATED).entity(tipo).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Integer id, TipoComentarioModel tipo) {
        tipo.setIdTipo(id);
        tipoComentarioRepository.save(tipo);
        return Response.ok(tipo).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Integer id) {
        TipoComentarioModel tipo = tipoComentarioRepository.findByIdTipo(id);
        if (tipo != null) {
            tipoComentarioRepository.remove(tipo);
            return Response.noContent().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
