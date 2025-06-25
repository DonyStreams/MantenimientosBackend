package usac.eps.controladores.mantenimientos;

import usac.eps.modelos.mantenimientos.TipoMantenimientoModel;
import usac.eps.repositorios.mantenimientos.TipoMantenimientoRepository;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/tipos-mantenimiento")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class TipoMantenimientoController {
    @Inject
    private TipoMantenimientoRepository tipoMantenimientoRepository;

    @GET
    public List<TipoMantenimientoModel> getAll() {
        return tipoMantenimientoRepository.findAll();
    }

    @GET
    @Path("/{id}")
    public TipoMantenimientoModel getById(@PathParam("id") Integer id) {
        return tipoMantenimientoRepository.findByIdTipo(id);
    }

    @POST
    public Response create(TipoMantenimientoModel tipo) {
        tipoMantenimientoRepository.save(tipo);
        return Response.status(Response.Status.CREATED).entity(tipo).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Integer id, TipoMantenimientoModel tipo) {
        tipo.setIdTipo(id);
        tipoMantenimientoRepository.save(tipo);
        return Response.ok(tipo).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Integer id) {
        TipoMantenimientoModel tipo = tipoMantenimientoRepository.findByIdTipo(id);
        if (tipo != null) {
            tipoMantenimientoRepository.remove(tipo);
            return Response.noContent().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
