package usac.eps.controladores.mantenimientos;

import usac.eps.modelos.mantenimientos.ContratoModel;
import usac.eps.modelos.mantenimientos.EquipoModel;
import usac.eps.modelos.mantenimientos.ProveedorModel;
import usac.eps.modelos.mantenimientos.TipoMantenimientoModel;
import usac.eps.modelos.mantenimientos.UsuarioMantenimientoModel;
import usac.eps.repositorios.mantenimientos.ContratoRepository;
import usac.eps.repositorios.mantenimientos.EquipoRepository;
import usac.eps.repositorios.mantenimientos.ProveedorRepository;
import usac.eps.repositorios.mantenimientos.TipoMantenimientoRepository;
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
import java.util.UUID;
import java.util.stream.Collectors;

@Path("/mantenimientos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class MantenimientoController {

    @Inject
    private ContratoRepository contratoRepository;

    @Inject
    private EquipoRepository equipoRepository;

    @Inject
    private ProveedorRepository proveedorRepository;

    @Inject
    private TipoMantenimientoRepository tipoMantenimientoRepository;

    @Inject
    private UsuarioMantenimientoRepository usuarioRepository;

    @Context
    private HttpServletRequest request;

    /**
     * Obtener todos los mantenimientos (contratos)
     */
    @GET
    public List<ContratoModel> getAll() {
        return contratoRepository.findAll();
    }

    /**
     * Obtener un mantenimiento por ID
     */
    @GET
    @Path("/{id}")
    public ContratoModel getById(@PathParam("id") Integer id) {
        return contratoRepository.findByIdContrato(id);
    }

    /**
     * Crear nuevo mantenimiento (contrato)
     */
    @POST
    public Response create(ContratoModel contrato) {
        try {
            // Obtener usuario actual desde el JWT
            UsuarioMantenimientoModel usuarioActual = obtenerUsuarioActual();
            if (usuarioActual == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Usuario no autenticado").build();
            }

            // Establecer fechas y usuario de creación
            contrato.setFechaCreacion(new Date());
            contrato.setFechaModificacion(new Date());
            contrato.setUsuarioCreacion(usuarioActual);
            contrato.setUsuarioModificacion(usuarioActual);

            // Validar datos requeridos
            if (contrato.getDescripcion() == null || contrato.getDescripcion().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("La descripción es requerida").build();
            }

            if (contrato.getFechaInicio() == null || contrato.getFechaFin() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Las fechas de inicio y fin son requeridas").build();
            }

            if (contrato.getFechaInicio().after(contrato.getFechaFin())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("La fecha de inicio no puede ser posterior a la fecha de fin").build();
            }

            // Establecer estado por defecto
            if (contrato.getEstado() == null) {
                contrato.setEstado(true);
            }

            contratoRepository.save(contrato);
            return Response.status(Response.Status.CREATED).entity(contrato).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al crear mantenimiento: " + e.getMessage()).build();
        }
    }

    /**
     * Actualizar mantenimiento
     */
    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Integer id, ContratoModel contrato) {
        try {
            ContratoModel existente = contratoRepository.findByIdContrato(id);
            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Mantenimiento no encontrado").build();
            }

            // Obtener usuario actual
            UsuarioMantenimientoModel usuarioActual = obtenerUsuarioActual();
            if (usuarioActual == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Usuario no autenticado").build();
            }

            // Actualizar campos
            contrato.setIdContrato(id);
            contrato.setFechaModificacion(new Date());
            contrato.setUsuarioModificacion(usuarioActual);

            // Mantener datos de creación originales
            contrato.setFechaCreacion(existente.getFechaCreacion());
            contrato.setUsuarioCreacion(existente.getUsuarioCreacion());

            contratoRepository.save(contrato);
            return Response.ok(contrato).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al actualizar mantenimiento: " + e.getMessage()).build();
        }
    }

    /**
     * Eliminar mantenimiento
     */
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Integer id) {
        try {
            ContratoModel contrato = contratoRepository.findByIdContrato(id);
            if (contrato != null) {
                contratoRepository.remove(contrato);
                return Response.noContent().build();
            }
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Mantenimiento no encontrado").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al eliminar mantenimiento: " + e.getMessage()).build();
        }
    }

    /**
     * Obtener equipos disponibles para asignar a un mantenimiento
     */
    @GET
    @Path("/equipos-disponibles")
    public List<EquipoModel> getEquiposDisponibles() {
        return equipoRepository.findAll();
    }

    /**
     * Obtener proveedores disponibles
     */
    @GET
    @Path("/proveedores-disponibles")
    public List<ProveedorModel> getProveedoresDisponibles() {
        return proveedorRepository.findAll();
    }

    /**
     * Obtener tipos de mantenimiento disponibles
     */
    @GET
    @Path("/tipos-disponibles")
    public List<TipoMantenimientoModel> getTiposDisponibles() {
        return tipoMantenimientoRepository.findAll();
    }

    /**
     * Obtener mantenimientos por proveedor
     */
    @GET
    @Path("/proveedor/{proveedorId}")
    public List<ContratoModel> getByProveedor(@PathParam("proveedorId") Integer proveedorId) {
        // Implementar filtro por proveedor si es necesario
        return contratoRepository.findAll();
    }

    /**
     * Obtener mantenimientos activos
     */
    @GET
    @Path("/activos")
    public List<ContratoModel> getActivos() {
        // Implementar filtro por estado activo
        return contratoRepository.findAll().stream()
                .filter(c -> c.getEstado() != null && c.getEstado())
                .collect(Collectors.toList());
    }

    /**
     * Método privado para obtener el usuario actual desde el JWT
     */
    private UsuarioMantenimientoModel obtenerUsuarioActual() {
        try {
            // Obtener información del usuario desde el JWT en el request
            String username = (String) request.getAttribute("username");
            String email = (String) request.getAttribute("email");

            if (username == null) {
                return null;
            }

            // Buscar usuario por email o username
            List<UsuarioMantenimientoModel> usuarios = usuarioRepository.findAll();
            for (UsuarioMantenimientoModel usuario : usuarios) {
                if (username.equals(usuario.getNombreCompleto()) ||
                        (email != null && email.equals(usuario.getCorreo()))) {
                    return usuario;
                }
            }

            // Si no existe, crear un usuario temporal (para desarrollo)
            // En producción, todos los usuarios deberían existir en la BD
            UsuarioMantenimientoModel nuevoUsuario = new UsuarioMantenimientoModel();
            nuevoUsuario.setKeycloakId(UUID.randomUUID());
            nuevoUsuario.setNombreCompleto(username);
            nuevoUsuario.setCorreo(email);
            nuevoUsuario.setActivo(true);

            usuarioRepository.save(nuevoUsuario);
            return nuevoUsuario;

        } catch (Exception e) {
            System.err.println("Error al obtener usuario actual: " + e.getMessage());
            return null;
        }
    }
}
