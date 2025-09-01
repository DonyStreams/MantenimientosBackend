package usac.eps.controladores.mantenimientos;

import usac.eps.modelos.mantenimientos.AreaModel;
import usac.eps.repositorios.mantenimientos.AreaRepository;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/areas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class AreaController {
    @Inject
    private AreaRepository areaRepository;

    @GET
    public List<AreaModel> getAll() {
        return areaRepository.findAll();
    }

    @GET
    @Path("/activos")
    public List<AreaModel> getActivos() {
        return areaRepository.findByEstado(true);
    }

    @GET
    @Path("/{id}")
    public AreaModel getById(@PathParam("id") Integer id) {
        return areaRepository.findByIdArea(id);
    }

    @POST
    public Response create(AreaModel area) {
        try {
            // Las fechas y estado por defecto se establecen automáticamente con @PrePersist
            areaRepository.save(area);
            return Response.status(Response.Status.CREATED).entity(area).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al crear área: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Integer id, AreaModel area) {
        try {
            // Verificar que el área existe
            AreaModel areaExistente = areaRepository.findByIdArea(id);
            if (areaExistente == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            // Establecer el ID y preservar campos que no deben cambiar
            area.setIdArea(id);

            // IMPORTANTE: Preservar la fecha de creación original
            area.setFechaCreacion(areaExistente.getFechaCreacion());

            // Preservar el usuario creador original
            area.setUsuarioCreacion(areaExistente.getUsuarioCreacion());

            // La fecha de modificación se establece automáticamente con @PreUpdate
            areaRepository.save(area);
            return Response.ok(area).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al actualizar área: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Integer id) {
        AreaModel area = areaRepository.findByIdArea(id);
        if (area != null) {
            try {
                areaRepository.deleteByIdArea(id);
                return Response.noContent().build();
            } catch (Exception e) {
                e.printStackTrace();
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("{\"error\": \"Error al eliminar área: " + e.getMessage() + "\"}")
                        .build();
            }
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
