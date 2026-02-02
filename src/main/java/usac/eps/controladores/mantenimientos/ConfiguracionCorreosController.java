package usac.eps.controladores.mantenimientos;

import usac.eps.modelos.mantenimientos.ConfiguracionAlertaModel;
import usac.eps.repositorios.mantenimientos.ConfiguracionAlertaRepository;
import usac.eps.seguridad.RequiresRole;
import usac.eps.servicios.mantenimientos.EmailService;

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

    private static final List<CorreoTipo> TIPOS = List.of(
            new CorreoTipo("equipo_critico", "Equipo crítico", "Correo cuando un equipo cambia a estado crítico"),
            new CorreoTipo("ticket_critico", "Ticket crítico", "Correo cuando un ticket cambia a prioridad crítica"),
            new CorreoTipo("mantenimiento_proximo_30", "Mantenimiento próximo (30 días)",
                    "Alertas de mantenimiento a 30 días"),
            new CorreoTipo("mantenimiento_proximo_15", "Mantenimiento próximo (15 días)",
                    "Alertas de mantenimiento a 15 días"),
            new CorreoTipo("mantenimiento_proximo_7", "Mantenimiento próximo (7 días)",
                    "Alertas de mantenimiento a 7 días"),
            new CorreoTipo("mantenimiento_vencido", "Mantenimiento vencido", "Alertas de mantenimiento vencido"),
            new CorreoTipo("contrato_proximo_30", "Contrato próximo (30 días)", "Alertas de contrato a 30 días"),
            new CorreoTipo("contrato_proximo_15", "Contrato próximo (15 días)", "Alertas de contrato a 15 días"),
            new CorreoTipo("contrato_proximo_7", "Contrato próximo (7 días)", "Alertas de contrato a 7 días"),
            new CorreoTipo("contrato_vencido", "Contrato vencido", "Alertas de contrato vencido"));

    @Inject
    private ConfiguracionAlertaRepository configuracionRepository;

    @Inject
    private EmailService emailService;

    @GET
    @RequiresRole({ "ADMIN" })
    public Response listarConfiguraciones() {
        List<Map<String, Object>> respuesta = new ArrayList<>();

        for (CorreoTipo tipo : TIPOS) {
            ConfiguracionAlertaModel config = configuracionRepository.findByTipo(tipo.tipo);
            String correos = config != null ? config.getUsuariosNotificar() : null;

            if (correos == null || correos.trim().isEmpty()) {
                correos = emailService.getDefaultDestinatarios();
            }

            Map<String, Object> item = new HashMap<>();
            item.put("tipoAlerta", tipo.tipo);
            item.put("nombre", tipo.nombre);
            item.put("descripcion", tipo.descripcion);
            item.put("correos", correos);
            item.put("idConfiguracion", config != null ? config.getIdConfiguracion() : null);
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
        CorreoTipo tipoDef = TIPOS.stream()
                .filter(t -> t.tipo.equalsIgnoreCase(tipo))
                .findFirst()
                .orElse(null);

        if (tipoDef == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Tipo de alerta no válido"))
                    .build();
        }

        String correosRaw = payload != null ? payload.getOrDefault("correos", "") : "";
        String correos = normalizarCorreos(correosRaw);

        ConfiguracionAlertaModel config = configuracionRepository.findByTipo(tipoDef.tipo);
        if (config == null) {
            config = new ConfiguracionAlertaModel();
            config.setNombre(tipoDef.nombre);
            config.setDescripcion(tipoDef.descripcion);
            config.setTipoAlerta(tipoDef.tipo);
            config.setDiasAnticipacion(0);
            config.setActiva(true);
        }

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

    private static class CorreoTipo {
        private final String tipo;
        private final String nombre;
        private final String descripcion;

        private CorreoTipo(String tipo, String nombre, String descripcion) {
            this.tipo = tipo;
            this.nombre = nombre;
            this.descripcion = descripcion;
        }
    }
}
