package usac.eps.controladores.mantenimientos;

import usac.eps.modelos.mantenimientos.ProveedorModel;
import usac.eps.repositorios.mantenimientos.ProveedorRepository;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/proveedores")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class ProveedorController {
    @Inject
    private ProveedorRepository proveedorRepository;

    @GET
    public List<ProveedorModel> getAll() {
        return proveedorRepository.findAll();
    }

    @GET
    @Path("/{id}")
    public ProveedorModel getById(@PathParam("id") Integer id) {
        return proveedorRepository.findByIdProveedor(id);
    }

    @POST
    public Response create(ProveedorModel proveedor) {
        proveedorRepository.save(proveedor);
        return Response.status(Response.Status.CREATED).entity(proveedor).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Integer id, ProveedorModel proveedor) {
        proveedor.setIdProveedor(id);
        proveedorRepository.save(proveedor);
        return Response.ok(proveedor).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Integer id) {
        ProveedorModel proveedor = proveedorRepository.findByIdProveedor(id);
        if (proveedor != null) {
            proveedorRepository.remove(proveedor);
            return Response.noContent().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
