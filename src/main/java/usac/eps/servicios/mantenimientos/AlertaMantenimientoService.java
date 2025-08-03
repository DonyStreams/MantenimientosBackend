package usac.eps.servicios.mantenimientos;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;

import usac.eps.modelos.mantenimientos.ProgramacionMantenimientoModel;
import usac.eps.repositorios.mantenimientos.ProgramacionMantenimientoRepository;

/**
 * Servicio para gestión de alertas y notificaciones de mantenimiento
 */
@Singleton
public class AlertaMantenimientoService {

    private static final Logger LOGGER = Logger.getLogger(AlertaMantenimientoService.class.getName());

    @Inject
    private ProgramacionMantenimientoRepository programacionRepository;

    /**
     * Revisa diariamente las programaciones que requieren alerta
     * Se ejecuta todos los días a las 8:00 AM
     */
    @Schedule(hour = "8", minute = "0", second = "0", persistent = false)
    public void revisarAlertasMantenimiento() {
        try {
            LOGGER.info("Iniciando revisión de alertas de mantenimiento...");

            // Obtener programaciones que requieren alerta en los próximos 7 días
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 7);
            Date fechaLimite = cal.getTime();

            List<ProgramacionMantenimientoModel> programacionesConAlerta = programacionRepository
                    .findProgramacionesParaAlerta(fechaLimite);

            LOGGER.info("Se encontraron " + programacionesConAlerta.size() + " programaciones que requieren alerta");

            for (ProgramacionMantenimientoModel programacion : programacionesConAlerta) {
                procesarAlerta(programacion);
            }

        } catch (Exception e) {
            LOGGER.severe("Error al revisar alertas de mantenimiento: " + e.getMessage());
        }
    }

    /**
     * Revisa semanalmente las programaciones vencidas
     * Se ejecuta todos los lunes a las 9:00 AM
     */
    @Schedule(dayOfWeek = "1", hour = "9", minute = "0", second = "0", persistent = false)
    public void revisarMantenimientosVencidos() {
        try {
            LOGGER.info("Iniciando revisión de mantenimientos vencidos...");

            Date fechaActual = new Date();
            List<ProgramacionMantenimientoModel> programacionesVencidas = programacionRepository
                    .findProgramacionesVencidas(fechaActual);

            LOGGER.info("Se encontraron " + programacionesVencidas.size() + " programaciones vencidas");

            for (ProgramacionMantenimientoModel programacion : programacionesVencidas) {
                procesarMantenimientoVencido(programacion);
            }

        } catch (Exception e) {
            LOGGER.severe("Error al revisar mantenimientos vencidos: " + e.getMessage());
        }
    }

    /**
     * Procesa una alerta de mantenimiento próximo
     */
    private void procesarAlerta(ProgramacionMantenimientoModel programacion) {
        try {
            String mensaje = String.format(
                    "ALERTA: El equipo '%s' (Código: %s) requiere mantenimiento de tipo '%s' el %s",
                    programacion.getEquipo().getNombre(),
                    programacion.getEquipo().getCodigoInacif(),
                    programacion.getTipoMantenimiento().getNombre(),
                    programacion.getFechaProximoMantenimiento());

            LOGGER.info("ALERTA DE MANTENIMIENTO: " + mensaje);

            // Aquí se podría integrar con un sistema de notificaciones
            // Por ejemplo: enviar email, crear notificación en base de datos, etc.

        } catch (Exception e) {
            LOGGER.severe("Error al procesar alerta para programación " +
                    programacion.getIdProgramacion() + ": " + e.getMessage());
        }
    }

    /**
     * Procesa un mantenimiento vencido
     */
    private void procesarMantenimientoVencido(ProgramacionMantenimientoModel programacion) {
        try {
            long diasVencido = calcularDiasVencido(programacion.getFechaProximoMantenimiento());

            String mensaje = String.format(
                    "MANTENIMIENTO VENCIDO: El equipo '%s' (Código: %s) tiene mantenimiento vencido desde hace %d días. Tipo: '%s'",
                    programacion.getEquipo().getNombre(),
                    programacion.getEquipo().getCodigoInacif(),
                    diasVencido,
                    programacion.getTipoMantenimiento().getNombre());

            LOGGER.warning("MANTENIMIENTO VENCIDO: " + mensaje);

            // Aquí se podría integrar con un sistema de notificaciones urgentes

        } catch (Exception e) {
            LOGGER.severe("Error al procesar mantenimiento vencido para programación " +
                    programacion.getIdProgramacion() + ": " + e.getMessage());
        }
    }

    /**
     * Calcula los días que lleva vencido un mantenimiento
     */
    private long calcularDiasVencido(Date fechaVencimiento) {
        Date ahora = new Date();
        long diferencia = ahora.getTime() - fechaVencimiento.getTime();
        return diferencia / (24 * 60 * 60 * 1000);
    }

    /**
     * Método manual para generar reporte de estado de mantenimientos
     */
    public String generarReporteEstado() {
        try {
            StringBuilder reporte = new StringBuilder();
            reporte.append("=== REPORTE DE ESTADO DE MANTENIMIENTOS ===\n");
            reporte.append("Fecha: ").append(new Date()).append("\n\n");

            // Programaciones activas
            List<ProgramacionMantenimientoModel> activas = programacionRepository
                    .findByActivaOrderByFechaProximoMantenimiento(true);
            reporte.append("Total de programaciones activas: ").append(activas.size()).append("\n");

            // Próximas alertas (7 días)
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 7);
            List<ProgramacionMantenimientoModel> proximas = programacionRepository
                    .findProgramacionesParaAlerta(cal.getTime());
            reporte.append("Próximas alertas (7 días): ").append(proximas.size()).append("\n");

            // Vencidas
            List<ProgramacionMantenimientoModel> vencidas = programacionRepository
                    .findProgramacionesVencidas(new Date());
            reporte.append("Mantenimientos vencidos: ").append(vencidas.size()).append("\n");

            if (!vencidas.isEmpty()) {
                reporte.append("\n--- MANTENIMIENTOS VENCIDOS ---\n");
                for (ProgramacionMantenimientoModel programacion : vencidas) {
                    long diasVencido = calcularDiasVencido(programacion.getFechaProximoMantenimiento());
                    reporte.append(String.format("- %s (%s): %d días vencido\n",
                            programacion.getEquipo().getNombre(),
                            programacion.getTipoMantenimiento().getNombre(),
                            diasVencido));
                }
            }

            return reporte.toString();

        } catch (Exception e) {
            LOGGER.severe("Error al generar reporte de estado: " + e.getMessage());
            return "Error al generar reporte: " + e.getMessage();
        }
    }
}
