package usac.eps.controladores;

//import com.fasterxml.jackson.databind.DeserializationFeature;
//import com.fasterxml.jackson.databind.ObjectMapper;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import java.util.List;
import javax.validation.Valid;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import usac.eps.modelos.DepartamentoModel;
import usac.eps.repositorios.DepartamentoRepository;

@Path("/Departamento")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class DepartamentoController {

    @Inject
    private DepartamentoRepository departamentoRepository;

    @GET
    public List<DepartamentoModel> getAll() {
        return departamentoRepository.findAll();
    }

    @GET
    @Path("/{id:[0-9][0-9]*}")
    public Response findById(@PathParam("id") final int id) {
        try {
            DepartamentoModel departamento = departamentoRepository.findByIdDepartamento(id);
            if (departamento == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(departamento).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @POST
    public Response create(@Valid final DepartamentoModel departamento) {
        try {
// VALIDAR SI LA ESTRUCTURA DE JSON ES VALIDA
//            ObjectMapper objectMapper = new ObjectMapper();
//            Map<String, Object> jsonMap = objectMapper.convertValue(departamento, Map.class);
//
//            // Verificar si hay campos desconocidos
//            Set<String> allowedFields = new HashSet<>();
//            allowedFields.add("nombre");
//            allowedFields.add("fechaModificacion");
//            allowedFields.add("idDepartamento");
//            for (String field : jsonMap.keySet()) {
//                if (!allowedFields.contains(field)) {
//                    return Response.status(Response.Status.CONFLICT).entity(field.toString()).build();
//                }
//            }

            DepartamentoModel result = departamentoRepository.saveAndFlush(departamento);
            return Response.created(UriBuilder.fromResource(DepartamentoController.class)
                    .path(Integer.valueOf(result.getIdDepartamento()).toString()).build())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id:[0-9][0-9]*}")
    public Response update(@PathParam("id") int id, final DepartamentoModel entity) {
        try {
            DepartamentoModel existingEntity = departamentoRepository.findByIdDepartamento(id);

            if (existingEntity == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
//            if (entity == null) {
//                return Response.status(Response.Status.NOT_FOUND).build();
//            }

            existingEntity.setNombre(entity.getNombre());
            existingEntity.setFechaModificacion(entity.getFechaModificacion());

            DepartamentoModel result = departamentoRepository.saveAndFlush(existingEntity);
            return Response.created(UriBuilder.fromResource(DepartamentoController.class)
                    .path(Integer.valueOf(result.getIdDepartamento()).toString()).build())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }

    }

    @DELETE
    @Path("/{id:[0-9][0-9]*}")
    public Response deleteById(@PathParam("id") final int id) {
        try {
            DepartamentoModel entity = departamentoRepository.findByIdDepartamento(id);
            if (entity == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            departamentoRepository.attachAndRemove(entity);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }

    }

}
