package usac.eps.controladores.mantenimientos;

import usac.eps.modelos.mantenimientos.ProveedorModel;
import usac.eps.modelos.mantenimientos.UsuarioMantenimientoModel;
import usac.eps.repositorios.mantenimientos.ProveedorRepository;
import usac.eps.repositorios.mantenimientos.UsuarioMantenimientoRepository;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;

@Path("/proveedores")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class ProveedorController {
    @Inject
    private ProveedorRepository proveedorRepository;

    @Inject
    private UsuarioMantenimientoRepository usuarioRepository;

    @Context
    private HttpServletRequest request;

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
            // Establecer fecha de creación
            proveedor.setFechaCreacion(new Date());

            // Establecer estado por defecto si no viene
            if (proveedor.getEstado() == null) {
                proveedor.setEstado(true);
            }

            // Asignar SOLO usuario de creación
            try {
                String keycloakId = (String) request.getAttribute("keycloakId");
                if (keycloakId != null) {
                    UsuarioMantenimientoModel usuario = usuarioRepository.findByKeycloakId(keycloakId);
                    if (usuario != null) {
                        proveedor.setUsuarioCreacion(usuario);
                    }
                }
            } catch (Exception e) {
                System.out.println("⚠️ No se pudo asignar usuario de creación: " + e.getMessage());
            }

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
            ProveedorModel proveedorExistente = proveedorRepository.findByIdProveedor(id);
            if (proveedorExistente == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            // Establecer el ID
            proveedor.setIdProveedor(id);

            // Preservar campos de auditoría original
            proveedor.setFechaCreacion(proveedorExistente.getFechaCreacion());
            proveedor.setUsuarioCreacion(proveedorExistente.getUsuarioCreacion());

            // Actualizar fecha de modificación
            proveedor.setFechaModificacion(new Date());

            // Asignar usuario modificador
            try {
                String keycloakId = (String) request.getAttribute("keycloakId");
                if (keycloakId != null) {
                    UsuarioMantenimientoModel usuario = usuarioRepository.findByKeycloakId(keycloakId);
                    if (usuario != null) {
                        proveedor.setUsuarioModificacion(usuario);
                    }
                }
            } catch (Exception e) {
                System.out.println("⚠️ No se pudo asignar usuario de modificación: " + e.getMessage());
            }

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
