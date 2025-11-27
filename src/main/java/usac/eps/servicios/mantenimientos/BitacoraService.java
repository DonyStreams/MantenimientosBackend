package usac.eps.servicios.mantenimientos;

import usac.eps.modelos.mantenimientos.EquipoModel;
import usac.eps.modelos.mantenimientos.HistorialEquipoModel;
import usac.eps.repositorios.mantenimientos.HistorialEquipoRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Date;
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
            LOGGER.info("📋 Bitácora registrada para equipo " + equipo.getIdEquipo() + ": " + descripcion);
        } catch (Exception e) {
            LOGGER.severe("❌ Error al registrar en bitácora: " + e.getMessage());
            e.printStackTrace();
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
            LOGGER.info("📋 Bitácora registrada para equipo " + idEquipo + ": " + descripcion);
        } catch (Exception e) {
            LOGGER.severe("❌ Error al registrar en bitácora: " + e.getMessage());
            e.printStackTrace();
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
    
    public void registrarTicketCreado(Integer idEquipo, Integer ticketId, String descripcionBreve) {
        registrar(idEquipo, "Ticket de falla #" + ticketId + " creado: " + descripcionBreve);
    }
    
    public void registrarTicketResuelto(Integer idEquipo, Integer ticketId, String solucion) {
        registrar(idEquipo, "Ticket #" + ticketId + " resuelto: " + solucion);
    }
    
    public void registrarContratoAsociado(Integer idEquipo, String contratoNumero) {
        registrar(idEquipo, "Equipo vinculado al contrato #" + contratoNumero);
    }
    
    public void registrarVencimientoGarantia(Integer idEquipo, Date fechaVencimiento) {
        registrar(idEquipo, "Garantía vencida en fecha: " + fechaVencimiento);
    }
}
