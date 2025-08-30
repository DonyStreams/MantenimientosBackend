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
    @Path("/activos")
    public List<ProveedorModel> getActivos() {
        return proveedorRepository.findByEstado(true);
    }

    @GET
    @Path("/{id}")
    public ProveedorModel getById(@PathParam("id") Integer id) {
        return proveedorRepository.findByIdProveedor(id);
    }

    @POST
    public Response create(ProveedorModel proveedor) {
        try {
            // Las fechas y estado por defecto se establecen automáticamente con @PrePersist
            proveedorRepository.save(proveedor);
            return Response.status(Response.Status.CREATED).entity(proveedor).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al crear proveedor: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Integer id, ProveedorModel proveedor) {
        try {
            // Verificar que el proveedor existe
            ProveedorModel proveedorExistente = proveedorRepository.findByIdProveedor(id);
            if (proveedorExistente == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            // Establecer el ID y preservar campos que no deben cambiar
            proveedor.setIdProveedor(id);

            // IMPORTANTE: Preservar la fecha de creación original
            proveedor.setFechaCreacion(proveedorExistente.getFechaCreacion());

            // Preservar el usuario creador original
            proveedor.setUsuarioCreacion(proveedorExistente.getUsuarioCreacion());

            // La fecha de modificación se establece automáticamente con @PreUpdate
            proveedorRepository.save(proveedor);
            return Response.ok(proveedor).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al actualizar proveedor: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Integer id) {
        ProveedorModel proveedor = proveedorRepository.findByIdProveedor(id);
        if (proveedor != null) {
            try {
                proveedorRepository.deleteByIdProveedor(id);
                return Response.noContent().build();
            } catch (Exception e) {
                e.printStackTrace();
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("{\"error\": \"Error al eliminar proveedor: " + e.getMessage() + "\"}")
                        .build();
            }
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
