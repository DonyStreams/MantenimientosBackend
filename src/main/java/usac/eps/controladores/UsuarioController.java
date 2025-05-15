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
import usac.eps.modelos.RolModel;
import usac.eps.modelos.SedeModel;
import usac.eps.modelos.UsuarioModel;
import usac.eps.repositorios.RolRepository;
import usac.eps.repositorios.SedeRepository;
import usac.eps.repositorios.UsuarioRepository;

@Path("/Usuario")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class UsuarioController {

    @Inject
    private UsuarioRepository usuarioRepository;

    @Inject
    private RolRepository rolRepository;
    
    @Inject
    private SedeRepository sedeRepository;

    @GET
    public List<UsuarioModel> getAll() {
        return usuarioRepository.findAll();
    }

    @GET
    @Path("PorRol/{id:[0-9][0-9]*}")
    public Response findByRol(@PathParam("id") final int id) {
        RolModel rol = rolRepository.findByIdRol(id);
        if (rol == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        List<RolModel> usuarios = usuarioRepository.findByRolModel(rol);
        if (usuarios.isEmpty()) {
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            return Response.ok(usuarios).build();
        }
    }

    @GET
    @Path("PorSede/{id:[0-9][0-9]*}")
    public Response findBySede(@PathParam("id") final int id) {
       SedeModel sede = sedeRepository.findByIdSede(id);
        if (sede == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        List<SedeModel> usuarios = usuarioRepository.findBySedeModel(sede);
        if (usuarios.isEmpty()) {
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            return Response.ok(usuarios).build();
        }
    }
    

    @GET
    @Path("/{id:[0-9][0-9]*}")
    public Response findById(@PathParam("id") final Long id) {
        try {
            UsuarioModel usuario = usuarioRepository.findByIdUsuario(id);
            if (usuario == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(usuario).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @POST
    public Response create(@Valid final UsuarioModel usuario) {
        try {
            UsuarioModel result = usuarioRepository.saveAndFlush(usuario);
            return Response.created(UriBuilder.fromResource(UsuarioController.class)
                    .path(Integer.valueOf(result.getIdUsuario()).toString()).build())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id:[0-9][0-9]*}")
    public Response update(@PathParam("id") Long id, final UsuarioModel entity) {
        try {
            UsuarioModel existingEntity = usuarioRepository.findByIdUsuario(id);

            if (existingEntity == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            existingEntity.setRolModel((entity.getRolModel() != null) ? entity.getRolModel() : existingEntity.getRolModel());
            existingEntity.setSedeModel((entity.getSedeModel() != null) ? entity.getSedeModel() : existingEntity.getSedeModel());
            existingEntity.setUsuario((entity.getUsuario() != null) ? entity.getUsuario() : existingEntity.getUsuario());
            existingEntity.setNombre((entity.getNombre() != null) ? entity.getNombre() : existingEntity.getNombre());
            existingEntity.setApellido((entity.getApellido() != null) ? entity.getApellido() : existingEntity.getApellido());
            existingEntity.setDireccion((entity.getDireccion() != null) ? entity.getDireccion() : existingEntity.getDireccion());
            existingEntity.setCorreo(entity.getCorreo());
            existingEntity.setTelefono(entity.getTelefono());
            existingEntity.setFechaModificacion(entity.getFechaModificacion());

            UsuarioModel result = usuarioRepository.saveAndFlush(existingEntity);
            return Response.created(UriBuilder.fromResource(UsuarioController.class)
                    .path(Integer.valueOf(result.getIdUsuario()).toString()).build())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }

    }

    @DELETE
    @Path("/{id:[0-9][0-9]*}")
    public Response deleteById(@PathParam("id") final Long id) {
        try {
            UsuarioModel entity = usuarioRepository.findByIdUsuario(id);
            if (entity == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            usuarioRepository.attachAndRemove(entity);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }

    }

}
