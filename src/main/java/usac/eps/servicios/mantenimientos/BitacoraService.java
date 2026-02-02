package usac.eps.servicios.mantenimientos;

import usac.eps.modelos.mantenimientos.EquipoModel;
import usac.eps.modelos.mantenimientos.HistorialEquipoModel;
import usac.eps.repositorios.mantenimientos.HistorialEquipoRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servicio para registrar automáticamente eventos en la bitácora de equipos
 */
@ApplicationScoped
public class BitacoraService {

    private static final Logger LOGGER = Logger.getLogger(BitacoraService.class.getName());

    @Inject
    private HistorialEquipoRepository historialRepository;

    /**
     * Registra un evento en la bitácora de un equipo
     */
    public void registrar(EquipoModel equipo, String descripcion) {
        try {
            HistorialEquipoModel historial = new HistorialEquipoModel();
            historial.setEquipo(equipo);
            historial.setDescripcion(descripcion);
            historial.setFechaRegistro(new Date());

            historialRepository.save(historial);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al registrar evento en bitácora", e);
        }
    }

    /**
     * Registra un evento usando el ID del equipo
     */
    public void registrar(Integer idEquipo, String descripcion) {
        try {
            HistorialEquipoModel historial = new HistorialEquipoModel();

            // Crear un equipo temporal solo con el ID para la relación
            EquipoModel equipo = new EquipoModel();
            equipo.setIdEquipo(idEquipo);

            historial.setEquipo(equipo);
            historial.setDescripcion(descripcion);
            historial.setFechaRegistro(new Date());

            historialRepository.save(historial);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al registrar evento en bitácora por ID", e);
        }
    }

    // Métodos helper para eventos comunes

    public void registrarCreacion(EquipoModel equipo) {
        registrar(equipo, "Equipo creado en el sistema");
    }

    public void registrarActualizacion(EquipoModel equipo, String cambios) {
        registrar(equipo, "Datos del equipo actualizados: " + cambios);
    }

    public void registrarCambioUbicacion(EquipoModel equipo, String ubicacionAnterior, String ubicacionNueva) {
        registrar(equipo, "Equipo movido de '" + ubicacionAnterior + "' a '" + ubicacionNueva + "'");
    }

    public void registrarCambioCondicion(EquipoModel equipo, String condicionAnterior, String condicionNueva) {
        registrar(equipo, "Condición operativa cambiada de '" + condicionAnterior + "' a '" + condicionNueva + "'");
    }

    public void registrarMantenimiento(Integer idEquipo, String tipoMantenimiento, String proveedor) {
        registrar(idEquipo, "Mantenimiento " + tipoMantenimiento + " ejecutado por " + proveedor);
    }

    public void registrarCalibracion(Integer idEquipo, String certificado) {
        registrar(idEquipo, "Equipo calibrado, certificado #" + certificado);
    }

    public void registrarTicketCreado(Integer idEquipo, Integer ticketId, String descripcionBreve, Integer usuarioId,
            String usuarioNombre) {
        registrarConTipo(idEquipo, ticketId, "Ticket de falla #" + ticketId + " creado: " + descripcionBreve,
                "TICKET_CREADO",
                usuarioId, usuarioNombre);
    }

    public void registrarTicketResuelto(Integer idEquipo, Integer ticketId, String solucion, Integer usuarioId,
            String usuarioNombre) {
        registrarConTipo(idEquipo, ticketId, "Ticket #" + ticketId + " resuelto: " + solucion, "TICKET_RESUELTO",
                usuarioId, usuarioNombre);
    }

    /**
     * Registra cambio de estado del ticket
     */
    public void registrarTicketCambioEstado(Integer idEquipo, Integer ticketId, String estadoAnterior,
            String estadoNuevo, Integer usuarioId, String usuarioNombre) {
        String descripcion = String.format("Ticket #%d cambió de estado: %s → %s", ticketId, estadoAnterior,
                estadoNuevo);
        registrarConTipo(idEquipo, ticketId, descripcion, "TICKET_ESTADO", usuarioId, usuarioNombre);
    }

    /**
     * Registra cuando se agrega una evidencia al ticket
     */
    public void registrarTicketEvidencia(Integer idEquipo, Integer ticketId, String nombreArchivo, Integer usuarioId,
            String usuarioNombre) {
        String descripcion = String.format("Evidencia agregada al ticket #%d: %s", ticketId, nombreArchivo);
        registrarConTipo(idEquipo, ticketId, descripcion, "TICKET_EVIDENCIA", usuarioId, usuarioNombre);
    }

    /**
     * Registra cambio de prioridad del ticket
     */
    public void registrarTicketCambioPrioridad(Integer idEquipo, Integer ticketId, String prioridadAnterior,
            String prioridadNueva, Integer usuarioId, String usuarioNombre) {
        String descripcion = String.format("Ticket #%d cambió de prioridad: %s → %s", ticketId, prioridadAnterior,
                prioridadNueva);
        registrarConTipo(idEquipo, ticketId, descripcion, "TICKET_PRIORIDAD", usuarioId, usuarioNombre);
    }

    /**
     * Registra asignación de usuario al ticket
     */
    public void registrarTicketAsignado(Integer idEquipo, Integer ticketId, String usuarioAsignado, Integer usuarioId,
            String usuarioNombre) {
        String descripcion = String.format("Ticket #%d asignado a: %s", ticketId, usuarioAsignado);
        registrarConTipo(idEquipo, ticketId, descripcion, "TICKET_ASIGNADO", usuarioId, usuarioNombre);
    }

    /**
     * Registra un evento con tipo de cambio especificado y usuario (sin ticketId)
     */
    private void registrarConTipo(Integer idEquipo, String descripcion, String tipoCambio, Integer usuarioId,
            String usuarioNombre) {
        registrarConTipo(idEquipo, null, descripcion, tipoCambio, usuarioId, usuarioNombre);
    }

    /**
     * Registra un evento con tipo de cambio especificado, ticketId y usuario
     */
    private void registrarConTipo(Integer idEquipo, Integer ticketId, String descripcion, String tipoCambio,
            Integer usuarioId, String usuarioNombre) {
        try {
            HistorialEquipoModel historial = new HistorialEquipoModel();

            // Crear un equipo temporal solo con el ID para la relación
            EquipoModel equipo = new EquipoModel();
            equipo.setIdEquipo(idEquipo);

            historial.setEquipo(equipo);
            historial.setTicketId(ticketId);
            historial.setDescripcion(descripcion);
            historial.setFechaRegistro(new Date());
            historial.setTipoCambio(tipoCambio);
            historial.setUsuarioId(usuarioId);
            historial.setUsuarioNombre(usuarioNombre);

            historialRepository.save(historial);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al registrar evento con tipo", e);
        }
    }

    public void registrarContratoAsociado(Integer idEquipo, String contratoNumero) {
        registrar(idEquipo, "Equipo vinculado al contrato #" + contratoNumero);
    }

    public void registrarVencimientoGarantia(Integer idEquipo, Date fechaVencimiento) {
        registrar(idEquipo, "Garantía vencida en fecha: " + fechaVencimiento);
    }
}
