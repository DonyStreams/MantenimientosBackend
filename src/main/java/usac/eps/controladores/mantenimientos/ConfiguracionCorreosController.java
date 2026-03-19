package usac.eps.controladores.mantenimientos;

import usac.eps.modelos.mantenimientos.ConfiguracionAlertaModel;
import usac.eps.repositorios.mantenimientos.ConfiguracionAlertaRepository;
import usac.eps.seguridad.RequiresRole;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Path("/configuracion-correos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ConfiguracionCorreosController {

    @Inject
    private ConfiguracionAlertaRepository configuracionRepository;

    @GET
    @RequiresRole({ "ADMIN" })
    public Response listarConfiguraciones() {
        List<Map<String, Object>> respuesta = new ArrayList<>();

        // Solo mostrar lo que existe en ConfiguracionAlerta (excluye scheduler_config)
        List<ConfiguracionAlertaModel> configs = configuracionRepository.findAll();
        for (ConfiguracionAlertaModel config : configs) {
            String correos = config.getUsuariosNotificar();
            Map<String, Object> item = new HashMap<>();
            item.put("tipoAlerta", config.getTipoAlerta());
            item.put("nombre", config.getNombre());
            item.put("descripcion", config.getDescripcion() != null ? config.getDescripcion() : "");
            item.put("correos", correos != null ? correos : "");
            item.put("idConfiguracion", config.getIdConfiguracion());
            respuesta.add(item);
        }

        return Response.ok(respuesta).build();
    }

    @PUT
    @Path("/{tipo}")
    @Transactional
    @RequiresRole({ "ADMIN" })
    public Response actualizarConfiguracion(@PathParam("tipo") String tipo,
            Map<String, String> payload) {
        ConfiguracionAlertaModel config = configuracionRepository.findByTipo(tipo);

        if (config == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Tipo de alerta no encontrado"))
                    .build();
        }

        String correosRaw = payload != null ? payload.getOrDefault("correos", "") : "";
        String correos = normalizarCorreos(correosRaw);

        config.setUsuariosNotificar(correos.isEmpty() ? null : correos);
        ConfiguracionAlertaModel guardada = configuracionRepository.save(config);

        return Response.ok(Map.of(
                "tipoAlerta", guardada.getTipoAlerta(),
                "correos", guardada.getUsuariosNotificar() != null ? guardada.getUsuariosNotificar() : "",
                "idConfiguracion", guardada.getIdConfiguracion()))
                .build();
    }

    private String normalizarCorreos(String correos) {
        if (correos == null) {
            return "";
        }

        String[] partes = correos.split("[;,\n\r]+");
        Set<String> unicos = new LinkedHashSet<>();
        for (String parte : partes) {
            String correo = parte.trim();
            if (!correo.isEmpty()) {
                unicos.add(correo);
            }
        }
        return String.join(",", new ArrayList<>(unicos));
    }

}
