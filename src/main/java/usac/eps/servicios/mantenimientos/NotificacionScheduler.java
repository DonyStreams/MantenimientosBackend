package usac.eps.servicios.mantenimientos;

import usac.eps.repositorios.mantenimientos.ConfiguracionAlertaRepository;

import javax.annotation.PostConstruct;
import javax.ejb.*;
import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Scheduler automático para verificación de alertas
 * Se ejecuta diariamente a la hora configurada en la BASE DE DATOS
 * 
 * La configuración se almacena en la tabla Configuracion_Alertas con
 * tipo_alerta='scheduler_config'
 * El campo dias_anticipacion almacena hora*100+minuto (ej: 830 = 8:30 AM)
 * El campo activa indica si el scheduler está habilitado
 * 
 * Todos los cambios de configuración se persisten automáticamente en BD
 * NO requiere recompilar ni reiniciar el servidor para cambiar la configuración
 * 
 * También puede ejecutarse manualmente via API REST
 */
@Singleton
@Startup
public class NotificacionScheduler {

    private static final Logger LOGGER = Logger.getLogger(NotificacionScheduler.class.getName());
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    @Inject
    private NotificacionService notificacionService;

    @Inject
    private ConfiguracionAlertaRepository configuracionRepository;

    // Cache en memoria (se sincroniza con BD)
    private boolean schedulerHabilitado = true;
    private int horaEjecucion = 8;
    private int minutoEjecucion = 0;
    private Date ultimaEjecucion;
    private String ultimoResultado;

    @PostConstruct
    public void init() {
        // Primero inicializar configuraciones por defecto si no existen
        try {
            configuracionRepository.inicializarConfiguracionesPorDefecto();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "No se pudieron inicializar configuraciones por defecto", e);
        }

        // Cargar configuración desde la BD
        loadConfiguracionFromDB();

        LOGGER.info("═══════════════════════════════════════════════════════════");
        LOGGER.info("📅 SCHEDULER DE NOTIFICACIONES INICIALIZADO");
        LOGGER.info("   Hora de ejecución: " + String.format("%02d:%02d", horaEjecucion, minutoEjecucion));
        LOGGER.info("   Estado: " + (schedulerHabilitado ? "✅ HABILITADO" : "❌ DESHABILITADO"));
        LOGGER.info("   Configuración: BASE DE DATOS (sin recompilación)");
        LOGGER.info("═══════════════════════════════════════════════════════════");
    }

    /**
     * Carga configuración del scheduler desde la BASE DE DATOS
     * Esta es la fuente de verdad para la configuración
     */
    private void loadConfiguracionFromDB() {
        try {
            Map<String, Object> config = configuracionRepository.getSchedulerConfig();

            this.horaEjecucion = (Integer) config.get("hora");
            this.minutoEjecucion = (Integer) config.get("minuto");
            this.schedulerHabilitado = (Boolean) config.get("habilitado");

            LOGGER.info("⚙️ Configuración del scheduler cargada desde BD: " +
                    String.format("%02d:%02d", horaEjecucion, minutoEjecucion) +
                    " - " + (schedulerHabilitado ? "Habilitado" : "Deshabilitado"));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al cargar configuración de BD, usando valores por defecto", e);
            // Mantener valores por defecto
            this.horaEjecucion = 8;
            this.minutoEjecucion = 0;
            this.schedulerHabilitado = true;
        }
    }

    /**
     * Tarea programada que se ejecuta cada minuto
     * Verifica si es la hora Y minuto configurados para ejecutar las verificaciones
     * 
     * IMPORTANTE: Siempre recarga la configuración desde la BD antes de comparar
     * Esto permite cambiar la hora sin reiniciar el servidor
     */
    @Schedule(hour = "*", minute = "*", second = "0", persistent = false)
    public void verificarHoraProgramada() {
        // SIEMPRE recargar configuración desde BD (permite cambios sin reinicio)
        loadConfiguracionFromDB();

        if (!schedulerHabilitado) {
            return;
        }

        java.util.Calendar cal = java.util.Calendar.getInstance();
        int horaActual = cal.get(java.util.Calendar.HOUR_OF_DAY);
        int minutoActual = cal.get(java.util.Calendar.MINUTE);

        // Solo ejecutar cuando coincide HORA Y MINUTO exactos
        if (horaActual == horaEjecucion && minutoActual == minutoEjecucion) {
            LOGGER.info("⏰ Hora programada alcanzada (" + horaEjecucion + ":" +
                    String.format("%02d", minutoEjecucion) + "), ejecutando verificación...");
            ejecutarVerificacionProgramada();
        }
    }

    /**
     * Ejecuta la verificación programada de alertas
     */
    @Asynchronous
    public void ejecutarVerificacionProgramada() {
        LOGGER.info("═══════════════════════════════════════════════════════════");
        LOGGER.info("📅 INICIO DE VERIFICACIÓN PROGRAMADA DE ALERTAS");
        LOGGER.info("   Fecha/Hora: " + DATE_FORMAT.format(new Date()));
        LOGGER.info("═══════════════════════════════════════════════════════════");

        try {
            Map<String, Object> resultado = notificacionService.ejecutarVerificacionCompleta();

            ultimaEjecucion = new Date();
            ultimoResultado = String.format("OK - Mantenimientos: %d, Contratos: %d",
                    resultado.get("alertasMantenimiento"),
                    resultado.get("alertasContrato"));

            LOGGER.info("═══════════════════════════════════════════════════════════");
            LOGGER.info("✅ VERIFICACIÓN COMPLETADA");
            LOGGER.info("   Alertas de mantenimiento: " + resultado.get("alertasMantenimiento"));
            LOGGER.info("   Alertas de contratos: " + resultado.get("alertasContrato"));
            LOGGER.info("   Total alertas creadas: " + resultado.get("totalAlertas"));
            LOGGER.info("═══════════════════════════════════════════════════════════");

            // Limpiar notificaciones antiguas (más de 90 días)
            int eliminadas = notificacionService.limpiarNotificacionesAntiguas(90);
            if (eliminadas > 0) {
                LOGGER.info("🗑️ Se eliminaron " + eliminadas + " notificaciones antiguas");
            }

        } catch (Exception e) {
            ultimoResultado = "ERROR: " + e.getMessage();
            LOGGER.log(Level.SEVERE, "❌ Error durante la verificación programada", e);
        }
    }

    /**
     * Ejecuta verificación manual (desde API REST)
     */
    public Map<String, Object> ejecutarVerificacionManual() {
        LOGGER.info("🔄 Ejecutando verificación manual solicitada...");
        Map<String, Object> resultado = notificacionService.ejecutarVerificacionCompleta();
        ultimaEjecucion = new Date();
        ultimoResultado = "MANUAL - " + resultado.get("totalAlertas") + " alertas";
        return resultado;
    }

    // ==================== GETTERS Y SETTERS CON PERSISTENCIA ====================

    public boolean isSchedulerHabilitado() {
        return schedulerHabilitado;
    }

    /**
     * Habilita/deshabilita el scheduler y PERSISTE en BD
     */
    public void setSchedulerHabilitado(boolean habilitado) {
        this.schedulerHabilitado = habilitado;

        // Persistir en BD
        try {
            configuracionRepository.setSchedulerHabilitado(habilitado);
            LOGGER.info("⚙️ Scheduler " + (habilitado ? "HABILITADO" : "DESHABILITADO") + " (persistido en BD)");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al persistir estado del scheduler", e);
        }
    }

    public int getHoraEjecucion() {
        return horaEjecucion;
    }

    /**
     * Actualiza la hora de ejecución y PERSISTE en BD
     */
    public void setHoraEjecucion(int hora) {
        if (hora >= 0 && hora <= 23) {
            this.horaEjecucion = hora;

            // Persistir en BD
            try {
                configuracionRepository.setSchedulerHorario(hora, this.minutoEjecucion);
                LOGGER.info("⚙️ Hora de ejecución actualizada a: " + hora + " (persistido en BD)");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error al persistir hora del scheduler", e);
            }
        }
    }

    public int getMinutoEjecucion() {
        return minutoEjecucion;
    }

    /**
     * Actualiza el minuto de ejecución y PERSISTE en BD
     */
    public void setMinutoEjecucion(int minuto) {
        if (minuto >= 0 && minuto <= 59) {
            this.minutoEjecucion = minuto;

            // Persistir en BD
            try {
                configuracionRepository.setSchedulerHorario(this.horaEjecucion, minuto);
                LOGGER.info("⚙️ Minuto de ejecución actualizado a: " + minuto + " (persistido en BD)");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error al persistir minuto del scheduler", e);
            }
        }
    }

    /**
     * Actualiza hora y minuto en una sola operación y PERSISTE en BD
     */
    public void setHorarioEjecucion(int hora, int minuto) {
        if (hora >= 0 && hora <= 23 && minuto >= 0 && minuto <= 59) {
            this.horaEjecucion = hora;
            this.minutoEjecucion = minuto;

            // Persistir en BD
            try {
                configuracionRepository.setSchedulerHorario(hora, minuto);
                LOGGER.info("⚙️ Horario actualizado a: " + String.format("%02d:%02d", hora, minuto)
                        + " (persistido en BD)");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error al persistir horario del scheduler", e);
            }
        }
    }

    public Date getUltimaEjecucion() {
        return ultimaEjecucion;
    }

    public String getUltimoResultado() {
        return ultimoResultado;
    }

    /**
     * Obtiene estado actual del scheduler
     */
    public Map<String, Object> getEstado() {
        // Recargar desde BD para mostrar datos actualizados
        loadConfiguracionFromDB();

        Map<String, Object> estado = new java.util.HashMap<>();
        estado.put("habilitado", schedulerHabilitado);
        estado.put("horaEjecucion", horaEjecucion);
        estado.put("minutoEjecucion", minutoEjecucion);
        estado.put("horaProgramada", String.format("%02d:%02d", horaEjecucion, minutoEjecucion));
        estado.put("ultimaEjecucion", ultimaEjecucion != null ? DATE_FORMAT.format(ultimaEjecucion) : "Nunca");
        estado.put("ultimoResultado", ultimoResultado != null ? ultimoResultado : "Sin datos");
        estado.put("horaActual", DATE_FORMAT.format(new Date()));
        estado.put("fuenteConfiguracion", "Base de Datos");
        return estado;
    }

    /**
     * Fuerza recarga de configuración desde BD (útil para sincronizar cambios
     * externos)
     */
    public void recargarConfiguracion() {
        loadConfiguracionFromDB();
        LOGGER.info("🔄 Configuración recargada desde BD");
    }
}
