package usac.eps.controladores.mantenimientos;

import usac.eps.modelos.mantenimientos.ComentarioEjecucionModel;
import usac.eps.modelos.mantenimientos.EjecucionMantenimientoModel;
import usac.eps.modelos.mantenimientos.UsuarioMantenimientoModel;
import usac.eps.repositorios.mantenimientos.ComentarioEjecucionRepository;
import usac.eps.repositorios.mantenimientos.EjecucionMantenimientoRepository;
import usac.eps.repositorios.mantenimientos.UsuarioMantenimientoRepository;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;

@Path("/comentarios-ejecucion")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ComentarioEjecucionController {

    @Inject
    private ComentarioEjecucionRepository comentarioRepository;

    @Inject
    private EjecucionMantenimientoRepository ejecucionRepository;

    @Inject
    private UsuarioMantenimientoRepository usuarioRepository;

    @GET
    public List<Map<String, Object>> getAll() {
        return comentarioRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Integer id) {
        ComentarioEjecucionModel comentario = comentarioRepository.findById(id);
        if (comentario == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(toDTO(comentario)).build();
    }

    @GET
    @Path("/ejecucion/{idEjecucion}")
    public List<Map<String, Object>> getByEjecucion(@PathParam("idEjecucion") Integer idEjecucion) {
        return comentarioRepository.findByIdEjecucion(idEjecucion).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @POST
    public Response create(Map<String, Object> request) {
        try {
            Integer idEjecucion = convertToInteger(request.get("idEjecucion"));
            Integer usuarioId = convertToInteger(request.get("usuarioId"));
            String tipoComentario = (String) request.get("tipoComentario");
            String comentarioTexto = (String) request.get("comentario");
            String estadoAnterior = (String) request.get("estadoAnterior");
            String estadoNuevo = (String) request.get("estadoNuevo");

            if (idEjecucion == null || comentarioTexto == null || comentarioTexto.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Se requiere idEjecucion y comentario")
                        .build();
            }

            EjecucionMantenimientoModel ejecucion = ejecucionRepository.findByIdEjecucion(idEjecucion);
            if (ejecucion == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Ejecución no encontrada")
                        .build();
            }

            ComentarioEjecucionModel comentario = new ComentarioEjecucionModel();
            comentario.setEjecucion(ejecucion);
            comentario.setComentario(comentarioTexto);
            comentario.setTipoComentario(tipoComentario != null ? tipoComentario : "SEGUIMIENTO");
            comentario.setFechaCreacion(new Date());

            if (usuarioId != null) {
                UsuarioMantenimientoModel usuario = usuarioRepository.findById(usuarioId);
                comentario.setUsuario(usuario);
            }

            // Si hay cambio de estado, registrarlo
            if (estadoAnterior != null && estadoNuevo != null && !estadoAnterior.equals(estadoNuevo)) {
                comentario.setEstadoAnterior(estadoAnterior);
                comentario.setEstadoNuevo(estadoNuevo);

                // Actualizar el estado de la ejecución
                ejecucion.setEstado(estadoNuevo);

                // Si se completa, actualizar fecha de cierre
                if ("COMPLETADO".equals(estadoNuevo)) {
                    ejecucion.setFechaCierre(new Date());
                }

                // Si se inicia, actualizar fecha de inicio
                if ("EN_PROCESO".equals(estadoNuevo) && ejecucion.getFechaInicioTrabajo() == null) {
                    ejecucion.setFechaInicioTrabajo(new Date());
                }

                ejecucionRepository.save(ejecucion);
            }

            comentarioRepository.save(comentario);

            return Response.status(Response.Status.CREATED)
                    .entity(toDTO(comentario))
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al crear comentario: " + e.getMessage())
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Integer id) {
        ComentarioEjecucionModel comentario = comentarioRepository.findById(id);
        if (comentario == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        comentarioRepository.remove(comentario);
        return Response.noContent().build();
    }

    private Map<String, Object> toDTO(ComentarioEjecucionModel model) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", model.getId());
        dto.put("idEjecucion", model.getEjecucion() != null ? model.getEjecucion().getIdEjecucion() : null);
        dto.put("tipoComentario", model.getTipoComentario());
        dto.put("comentario", model.getComentario());
        dto.put("estadoAnterior", model.getEstadoAnterior());
        dto.put("estadoNuevo", model.getEstadoNuevo());
        dto.put("fechaCreacion", model.getFechaCreacion());

        if (model.getUsuario() != null) {
            dto.put("usuarioId", model.getUsuario().getId());
            dto.put("usuario", model.getUsuario().getNombreCompleto());
        } else {
            dto.put("usuario", "Sistema");
        }

        return dto;
    }

    private Integer convertToInteger(Object value) {
        if (value == null)
            return null;
        if (value instanceof Integer)
            return (Integer) value;
        if (value instanceof Number)
            return ((Number) value).intValue();
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
