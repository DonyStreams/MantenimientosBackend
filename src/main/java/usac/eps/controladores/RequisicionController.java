package usac.eps.controladores;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import usac.eps.clases.ErroresClase;
import usac.eps.modelos.RegistroErroresModel;
import usac.eps.modelos.RequisicionDetalleModel;
import usac.eps.modelos.RequisicionModel;
import usac.eps.modelos.TipoRequisicionModel;
import usac.eps.modelos.UnidadModel;
import usac.eps.modelos.UsuarioModel;
import usac.eps.repositorios.ErroresRepository;
import usac.eps.repositorios.RequisicionRepository;
import usac.eps.repositorios.TipoRequisicionRepository;
import usac.eps.repositorios.UnidadRepository;
import usac.eps.repositorios.UsuarioRepository;

@Path("/Requisicion")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class RequisicionController {

    @Inject
    private RequisicionRepository requisicionRepository;

    @Inject
    private TipoRequisicionRepository tipoRequisicionRepository;

    @Inject
    private UnidadRepository unidadRepository;

    @Inject
    private UsuarioRepository usuarioRepository;

    @Inject
    private ErroresClase errores;

    @GET
    public List<RequisicionModel> getAll() {
        return requisicionRepository.findAll();
    }

    @GET
    @Path("PorTipo/{id:[0-9][0-9]*}")
    public Response findByTipoRequisicion(@PathParam("id") final int id) {
        TipoRequisicionModel tipoRequisicion = tipoRequisicionRepository.findByIdTipoRequisicion(id);
        if (tipoRequisicion == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        List<RequisicionModel> requisiciones = requisicionRepository.findByTipoRequisicion(tipoRequisicion);
        if (requisiciones.isEmpty()) {
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            return Response.ok(requisiciones).build();
        }
    }

    @GET
    @Path("PorUnidad/{id:[0-9][0-9]*}")
    public Response findByUnidad(@PathParam("id") final int id) {
        UnidadModel unidad = unidadRepository.findByIdUnidad(id);
        if (unidad == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        List<RequisicionModel> requisiciones = requisicionRepository.findByUnidad(unidad);
        if (requisiciones.isEmpty()) {
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            return Response.ok(requisiciones).build();
        }
    }

    @GET
    @Path("PorUsuario/{id:[0-9][0-9]*}")
    public Response findByUsuario(@PathParam("id") final Long id) {
        UsuarioModel usuario = usuarioRepository.findByIdUsuario(id);
        if (usuario == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        List<RequisicionModel> requisiciones = requisicionRepository.findByUsuario(usuario);
        if (requisiciones.isEmpty()) {
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            return Response.ok(requisiciones).build();
        }
    }

    @GET
    @Path("/{id:[0-9][0-9]*}")
    public Response findById(@PathParam("id") final long id) {
        try {
            RequisicionModel requisicion = requisicionRepository.findByIdRequisicion(id);
            if (requisicion == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(requisicion).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @POST
    @Transactional
    public Response create(@Valid final RequisicionModel requisicion) {             
        
        
        try {            
            errores=new ErroresClase();
            List<RequisicionDetalleModel> detalles = requisicion.getDetallesRequisicion();
            
            // Asignar la requisición a cada detalle
            for (RequisicionDetalleModel detalle : detalles) {
                detalle.setRequisicion(requisicion);
            }

            //RequisicionModel result = requisicionRepository.saveAndFlush(requisicion);
            // Asignar los detalles a la requisición
            requisicion.setDetallesRequisicion(requisicion.getDetallesRequisicion());

            // Guardar la requisición con sus detalles
            RequisicionModel resultReq = requisicionRepository.saveAndFlush(requisicion);                
           
            errores.insertarErrores("Error al  momento de crear la requisición.","Pruebas sdasdasdasgd");            
            
            return Response.created(UriBuilder.fromResource(RequisicionController.class)
                    .path(Long.toString(resultReq.getIdRequisicion())).build())
                    .entity(resultReq.getIdRequisicion())
                    .build();
        } catch (Exception e) {               
            
            //errores.insertarErrores("Error al  momento de crear la requisición.",e.getMessage());

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id:[0-9][0-9]*}")
    public Response update(@PathParam("id") long id, final RequisicionModel entity) {
        try {
            RequisicionModel existingEntity = requisicionRepository.findByIdRequisicion(id);

            if (existingEntity == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            existingEntity.setTipoRequisicion((entity.getTipoRequisicion() != null) ? entity.getTipoRequisicion() : existingEntity.getTipoRequisicion());
            existingEntity.setUnidad((entity.getUnidad() != null) ? entity.getUnidad() : existingEntity.getUnidad());
            existingEntity.setUsuario((entity.getUsuario() != null) ? entity.getUsuario() : existingEntity.getUsuario());
            existingEntity.setFechaRequisicion(entity.getFechaRequisicion());
            existingEntity.setDescripcion((entity.getDescripcion() != null) ? entity.getDescripcion() : existingEntity.getDescripcion());
            existingEntity.setEstadoActual((entity.getEstadoActual() != null) ? entity.getEstadoActual() : existingEntity.getEstadoActual());
            existingEntity.setFechaModificacion(entity.getFechaModificacion());

            RequisicionModel result = requisicionRepository.saveAndFlush(existingEntity);
            return Response.created(UriBuilder.fromResource(RequisicionController.class)
                    .path(Long.toString(result.getIdRequisicion())).build())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{id:[0-9][0-9]*}")
    public Response deleteById(@PathParam("id") final long id) {
        try {
            RequisicionModel entity = requisicionRepository.findByIdRequisicion(id);
            if (entity == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            requisicionRepository.attachAndRemove(entity);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
