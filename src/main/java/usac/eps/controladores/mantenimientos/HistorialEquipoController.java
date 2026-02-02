package usac.eps.controladores.mantenimientos;

import usac.eps.controladores.mantenimientos.dto.HistorialEquipoDTO;
import usac.eps.modelos.mantenimientos.HistorialEquipoModel;
import usac.eps.repositorios.mantenimientos.HistorialEquipoRepository;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/historial-equipos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class HistorialEquipoController {
    private static final Logger LOGGER = Logger.getLogger(HistorialEquipoController.class.getName());

    @PersistenceContext(unitName = "usac.eps_ControlSuministros")
    private EntityManager em;

    @Inject
    private HistorialEquipoRepository historialEquipoRepository;

    @GET
    public List<HistorialEquipoDTO> getAll() {
        // Usar query con JOIN FETCH para cargar los equipos eagerly
        // Excluir registros de tickets (TICKET_CREADO, TICKET_RESUELTO)
        String jpql = "SELECT h FROM HistorialEquipoModel h LEFT JOIN FETCH h.equipo " +
                "WHERE h.tipoCambio IS NULL OR (h.tipoCambio NOT LIKE 'TICKET_%') " +
                "ORDER BY h.fechaRegistro DESC";
        List<HistorialEquipoModel> historial = em.createQuery(jpql, HistorialEquipoModel.class).getResultList();

        return historial.stream()
                .map(h -> new HistorialEquipoDTO(
                        h.getIdHistorial(),
                        h.getEquipo() != null ? h.getEquipo().getIdEquipo() : null,
                        h.getEquipo() != null ? h.getEquipo().getNombre() : "Equipo eliminado",
                        h.getEquipo() != null ? h.getEquipo().getNumeroSerie() : null,
                        h.getFechaRegistro(),
                        h.getDescripcion(),
                        h.getTipoCambio(),
                        h.getUsuarioId(),
                        h.getUsuarioNombre()))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene solo el historial relacionado con tickets
     */
    @GET
    @Path("/tickets")
    public List<HistorialEquipoDTO> getTicketsHistory() {
        String jpql = "SELECT h FROM HistorialEquipoModel h LEFT JOIN FETCH h.equipo " +
                "WHERE h.tipoCambio LIKE 'TICKET_%' " +
                "ORDER BY h.fechaRegistro DESC";
        List<HistorialEquipoModel> historial = em.createQuery(jpql, HistorialEquipoModel.class).getResultList();

        return historial.stream()
                .map(h -> new HistorialEquipoDTO(
                        h.getIdHistorial(),
                        h.getEquipo() != null ? h.getEquipo().getIdEquipo() : null,
                        h.getEquipo() != null ? h.getEquipo().getNombre() : "Equipo eliminado",
                        h.getEquipo() != null ? h.getEquipo().getNumeroSerie() : null,
                        h.getFechaRegistro(),
                        h.getDescripcion(),
                        h.getTipoCambio(),
                        h.getUsuarioId(),
                        h.getUsuarioNombre()))
                .collect(Collectors.toList());
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
    @Transactional
    public Response delete(@PathParam("id") Integer id) {
        try {
            HistorialEquipoModel historial = historialEquipoRepository.findByIdHistorial(id);
            if (historial == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Registro no encontrado\"}")
                        .build();
            }

            // Merge para gestionar la entidad y luego remover
            HistorialEquipoModel managedHistorial = em.merge(historial);
            em.remove(managedHistorial);
            em.flush();

            return Response.noContent().build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar registro", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al eliminar registro: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @DELETE
    @Path("/batch")
    @Transactional
    public Response deleteMultiple(Map<String, Object> requestBody) {
        try {
            @SuppressWarnings("unchecked")
            List<Integer> ids = (List<Integer>) requestBody.get("ids");

            if (ids == null || ids.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Lista de IDs no puede estar vacía\"}")
                        .build();
            }

            int deletedCount = 0;
            for (Integer id : ids) {
                HistorialEquipoModel historial = em.find(HistorialEquipoModel.class, id);
                if (historial != null) {
                    em.remove(historial);
                    deletedCount++;
                }
            }

            em.flush();

            return Response.ok()
                    .entity("{\"message\": \"Se eliminaron " + deletedCount + " registros correctamente\"}")
                    .build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar registros", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al eliminar registros: " + e.getMessage() + "\"}")
                    .build();
        }
    }
}
