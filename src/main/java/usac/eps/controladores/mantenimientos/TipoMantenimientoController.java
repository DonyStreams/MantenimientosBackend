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
    @Path("/activos")
    public List<TipoMantenimientoModel> getActivos() {
        return tipoMantenimientoRepository.findActivos();
    }

    @GET
    @Path("/{id}")
    public TipoMantenimientoModel getById(@PathParam("id") Integer id) {
        return tipoMantenimientoRepository.findByIdTipo(id);
    }

    @POST
    public Response create(TipoMantenimientoModel tipo) {
        // Las fechas y estado por defecto se establecen automáticamente con @PrePersist
        tipoMantenimientoRepository.save(tipo);
        return Response.status(Response.Status.CREATED).entity(tipo).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Integer id, TipoMantenimientoModel tipo) {
        // Verificar que el tipo existe
        TipoMantenimientoModel tipoExistente = tipoMantenimientoRepository.findByIdTipo(id);
        if (tipoExistente == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Establecer el ID y preservar campos que no deben cambiar
        tipo.setIdTipo(id);

        // IMPORTANTE: Preservar la fecha de creación original
        tipo.setFechaCreacion(tipoExistente.getFechaCreacion());

        // Preservar el usuario creador original
        tipo.setUsuarioCreacion(tipoExistente.getUsuarioCreacion());

        // La fecha de modificación se establece automáticamente con @PreUpdate
        tipoMantenimientoRepository.save(tipo);
        return Response.ok(tipo).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Integer id) {
        TipoMantenimientoModel tipo = tipoMantenimientoRepository.findByIdTipo(id);
        if (tipo != null) {
            try {
                tipoMantenimientoRepository.deleteByIdTipo(id);
                return Response.noContent().build();
            } catch (Exception e) {
                e.printStackTrace();
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
