package usac.eps.controladores.mantenimientos;

import usac.eps.modelos.mantenimientos.EvidenciaModel;
import usac.eps.repositorios.mantenimientos.EvidenciaRepository;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/evidencias")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class EvidenciaController {
    @Inject
    private EvidenciaRepository evidenciaRepository;

    @GET
    public List<EvidenciaModel> getAll() {
        return evidenciaRepository.findAll();
    }

    @GET
    @Path("/{id}")
    public EvidenciaModel getById(@PathParam("id") Integer id) {
        return evidenciaRepository.findById(id);
    }

    @POST
    public Response create(EvidenciaModel evidencia) {
        evidenciaRepository.save(evidencia);
        return Response.status(Response.Status.CREATED).entity(evidencia).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Integer id, EvidenciaModel evidencia) {
        evidencia.setId(id);
        evidenciaRepository.save(evidencia);
        return Response.ok(evidencia).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Integer id) {
        EvidenciaModel evidencia = evidenciaRepository.findById(id);
        if (evidencia != null) {
            evidenciaRepository.remove(evidencia);
            return Response.noContent().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
