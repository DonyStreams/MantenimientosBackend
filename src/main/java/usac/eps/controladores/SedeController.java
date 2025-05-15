package usac.eps.controladores;

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
import usac.eps.modelos.SedeModel;
import usac.eps.repositorios.DepartamentoRepository;
import usac.eps.repositorios.SedeRepository;

@Path("/Sede")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class SedeController {

    @Inject
    private SedeRepository sedeRepository;

    @Inject
    private DepartamentoRepository departamentoRepository;

    @GET
    public List<SedeModel> getAll() {
        return sedeRepository.findAll();
    }

    @GET
    @Path("PorDepartamento/{id:[0-9][0-9]*}")
    public Response findByDepartamento(@PathParam("id") final int id) {
        DepartamentoModel departamento = departamentoRepository.findByIdDepartamento(id);
        if (departamento == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        List<SedeModel> sedes = sedeRepository.findByDepartamentoModel(departamento);

        if (sedes.isEmpty()) {            
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {            
            return Response.ok(sedes).build();
        }
    }

    @GET
    @Path("/{id:[0-9][0-9]*}")
    public Response findById(@PathParam("id") final int id) {
        try {
            SedeModel sede = sedeRepository.findByIdSede(id);
            if (sede == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(sede).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @POST
    public Response create(@Valid final SedeModel sede) {
        try {
            SedeModel result = sedeRepository.saveAndFlush(sede);
            return Response.created(UriBuilder.fromResource(SedeController.class)
                    .path(Integer.valueOf(result.getIdSede()).toString()).build())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id:[0-9][0-9]*}")
    public Response update(@PathParam("id") int id, final SedeModel entity) {
        try {
            SedeModel existingEntity = sedeRepository.findByIdSede(id);

            if (existingEntity == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            existingEntity.setNombre((entity.getNombre() != null) ? entity.getNombre() : existingEntity.getNombre());
            existingEntity.setDireccion((entity.getDireccion() != null) ? entity.getDireccion() : existingEntity.getDireccion());
            existingEntity.setDepartamentoModel((entity.getDepartamentoModel() != null) ? entity.getDepartamentoModel() : existingEntity.getDepartamentoModel());
            existingEntity.setFechaModificacion(entity.getFechaModificacion());

            SedeModel result = sedeRepository.saveAndFlush(existingEntity);
            return Response.created(UriBuilder.fromResource(SedeController.class)
                    .path(Integer.valueOf(result.getIdSede()).toString()).build())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }

    }

    @DELETE
    @Path("/{id:[0-9][0-9]*}")
    public Response deleteById(@PathParam("id") final int id) {
        try {
            SedeModel entity = sedeRepository.findByIdSede(id);
            if (entity == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            sedeRepository.attachAndRemove(entity);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }

    }

}
