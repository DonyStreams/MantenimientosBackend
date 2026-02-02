package usac.eps.servicios;

import usac.eps.modelos.mantenimientos.EquipoModel;
import usac.eps.modelos.mantenimientos.HistorialEquipoModel;
import usac.eps.repositorios.mantenimientos.HistorialEquipoRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servicio para registro automático en la bitácora/historial de equipos
 */
@ApplicationScoped
public class HistorialEquipoService {

    private static final Logger LOGGER = Logger.getLogger(HistorialEquipoService.class.getName());

    @Inject
    private HistorialEquipoRepository historialRepository;

    /**
     * Registra un evento en el historial de un equipo
     * 
     * @param equipo      Equipo relacionado
     * @param descripcion Descripción del evento
     */
    public void registrarEvento(EquipoModel equipo, String descripcion) {
        if (equipo == null || equipo.getIdEquipo() == null) {
            return;
        }

        try {
            HistorialEquipoModel historial = new HistorialEquipoModel();
            historial.setEquipo(equipo);
            historial.setDescripcion(descripcion);
            historial.setFechaRegistro(new Date());

            historialRepository.save(historial);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al registrar historial de equipo", e);
        }
    }

    /**
     * Registra creación de equipo
     */
    public void registrarCreacion(EquipoModel equipo) {
        registrarEvento(equipo, "Equipo creado en el sistema - " + equipo.getNombre());
    }

    /**
     * Registra actualización de equipo
     */
    public void registrarActualizacion(EquipoModel equipo, String detalles) {
        registrarEvento(equipo, "Equipo actualizado: " + detalles);
    }

    /**
     * Registra cambio de ubicación
     */
    public void registrarCambioUbicacion(EquipoModel equipo, String ubicacionAnterior, String ubicacionNueva) {
        String descripcion = String.format("Ubicación cambiada de '%s' a '%s'",
                ubicacionAnterior != null ? ubicacionAnterior : "Sin ubicación",
                ubicacionNueva != null ? ubicacionNueva : "Sin ubicación");
        registrarEvento(equipo, descripcion);
    }

    /**
     * Registra cambio de condiciones operativas
     */
    public void registrarCambioCondicion(EquipoModel equipo, String condicionAnterior, String condicionNueva) {
        String descripcion = String.format("Condición operativa cambiada de '%s' a '%s'",
                condicionAnterior != null ? condicionAnterior : "Sin especificar",
                condicionNueva != null ? condicionNueva : "Sin especificar");
        registrarEvento(equipo, descripcion);
    }

    /**
     * Registra creación de ticket de falla
     */
    public void registrarTicketCreado(EquipoModel equipo, Integer ticketId, String descripcionFalla) {
        String descripcion = String.format("Ticket de falla #%d creado: %s",
                ticketId,
                descripcionFalla.length() > 100 ? descripcionFalla.substring(0, 100) + "..." : descripcionFalla);
        registrarEvento(equipo, descripcion);
    }

    /**
     * Registra resolución de ticket
     */
    public void registrarTicketResuelto(EquipoModel equipo, Integer ticketId, String solucion) {
        String descripcion = String.format("Ticket #%d resuelto: %s",
                ticketId,
                solucion.length() > 100 ? solucion.substring(0, 100) + "..." : solucion);
        registrarEvento(equipo, descripcion);
    }

    /**
     * Registra ejecución de mantenimiento
     */
    public void registrarMantenimiento(EquipoModel equipo, String tipoMantenimiento, String detalles) {
        String descripcion = String.format("Mantenimiento %s ejecutado: %s", tipoMantenimiento, detalles);
        registrarEvento(equipo, descripcion);
    }

    /**
     * Registra asociación con contrato
     */
    public void registrarAsociacionContrato(EquipoModel equipo, String numeroContrato) {
        String descripcion = String.format("Equipo asociado al contrato %s", numeroContrato);
        registrarEvento(equipo, descripcion);
    }
}
