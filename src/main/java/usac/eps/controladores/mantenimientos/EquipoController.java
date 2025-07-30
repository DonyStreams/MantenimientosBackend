package usac.eps.controladores.mantenimientos;

import usac.eps.modelos.mantenimientos.EquipoModel;
import usac.eps.repositorios.mantenimientos.EquipoRepository;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;

@Path("/equipos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class EquipoController {
    @Inject
    private EquipoRepository equipoRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private static final Logger LOGGER = Logger.getLogger(EquipoController.class.getName());

    @GET
    public List<EquipoModel> getAll() {
        List<EquipoModel> equipos = new ArrayList<>(equipoRepository.findAll());
        return equipos;
    }

    @GET
    @Path("/{id}")
    public EquipoModel getById(@PathParam("id") Integer id) {
        return equipoRepository.findByIdEquipo(id);
    }

    @POST
    public Response create(EquipoModel equipo) {
        equipo.setFechaCreacion(new java.util.Date());
        equipo.setFechaModificacion(new java.util.Date());
        equipoRepository.save(equipo);
        return Response.status(Response.Status.CREATED).entity(equipo).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Integer id, EquipoModel equipo) {
        equipo.setIdEquipo(id);
        equipo.setFechaModificacion(new java.util.Date());
        equipoRepository.save(equipo);
        return Response.ok(equipo).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Integer id) {
        EquipoModel equipo = equipoRepository.findByIdEquipo(id);
        if (equipo != null) {
            try {
                EquipoModel managedEquipo = entityManager.merge(equipo);
                entityManager.remove(managedEquipo);
                entityManager.flush();
                return Response.noContent().build();
            } catch (Exception e) {
                LOGGER.severe("Error al eliminar equipo: " + e.getMessage());
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("No se pudo eliminar el equipo: " + e.getMessage()).build();
            }
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
