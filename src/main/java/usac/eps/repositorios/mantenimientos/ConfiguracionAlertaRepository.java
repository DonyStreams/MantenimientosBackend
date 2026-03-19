package usac.eps.repositorios.mantenimientos;

import usac.eps.modelos.mantenimientos.ConfiguracionAlertaModel;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Repositorio para gestión de configuración de alertas
 */
@ApplicationScoped
public class ConfiguracionAlertaRepository {

    private static final Logger LOGGER = Logger.getLogger(ConfiguracionAlertaRepository.class.getName());
    private static final String SCHEDULER_CONFIG_TYPE = "scheduler_config";

    @PersistenceContext(unitName = "usac.eps_ControlSuministros")
    private EntityManager em;

    /**
     * Obtiene todas las configuraciones de alertas (excepto scheduler_config)
     */
    public List<ConfiguracionAlertaModel> findAll() {
        return em.createQuery(
                "SELECT c FROM ConfiguracionAlertaModel c " +
                        "WHERE c.tipoAlerta <> :schedulerType " +
                        "ORDER BY c.nombre",
                ConfiguracionAlertaModel.class)
                .setParameter("schedulerType", SCHEDULER_CONFIG_TYPE)
                .getResultList();
    }

    /**
     * Obtiene configuraciones activas (excepto scheduler_config)
     */
    public List<ConfiguracionAlertaModel> findActivas() {
        return em.createQuery(
                "SELECT c FROM ConfiguracionAlertaModel c " +
                        "WHERE c.activa = true AND c.tipoAlerta <> :schedulerType",
                ConfiguracionAlertaModel.class)
                .setParameter("schedulerType", SCHEDULER_CONFIG_TYPE)
                .getResultList();
    }

    /**
     * Obtiene configuración por tipo de alerta (incluyendo scheduler)
     */
    public ConfiguracionAlertaModel findByTipo(String tipo) {
        List<ConfiguracionAlertaModel> results = em.createQuery(
                "SELECT c FROM ConfiguracionAlertaModel c WHERE c.tipoAlerta = :tipo",
                ConfiguracionAlertaModel.class)
                .setParameter("tipo", tipo)
                .getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    // ==================== CONFIGURACIÓN DEL SCHEDULER ====================

    /**
     * Obtiene la configuración del scheduler desde la BD
     * 
     * @return Map con keys: habilitado, hora, minuto
     */
    public Map<String, Object> getSchedulerConfig() {
        Map<String, Object> config = new HashMap<>();

        ConfiguracionAlertaModel schedulerConfig = findByTipo(SCHEDULER_CONFIG_TYPE);

        if (schedulerConfig != null) {
            // dias_anticipacion almacena hora*100+minuto (ej: 830 = 8:30)
            int horarioCompacto = schedulerConfig.getDiasAnticipacion();
            config.put("hora", horarioCompacto / 100);
            config.put("minuto", horarioCompacto % 100);
            config.put("habilitado", schedulerConfig.getActiva());
            config.put("idConfiguracion", schedulerConfig.getIdConfiguracion());
        } else {
            // Valores por defecto
            config.put("hora", 8);
            config.put("minuto", 0);
            config.put("habilitado", true);
            config.put("idConfiguracion", null);
        }

        return config;
    }

    /**
     * Guarda la configuración del scheduler en la BD
     */
    @Transactional
    public void saveSchedulerConfig(int hora, int minuto, boolean habilitado) {
        ConfiguracionAlertaModel schedulerConfig = findByTipo(SCHEDULER_CONFIG_TYPE);

        // Compactar hora y minuto: hora*100 + minuto
        int horarioCompacto = hora * 100 + minuto;

        if (schedulerConfig == null) {
            // Crear nuevo registro
            schedulerConfig = new ConfiguracionAlertaModel();
            schedulerConfig.setNombre("Configuración del Scheduler");
            schedulerConfig.setDescripcion("Configuración del scheduler de notificaciones. Formato: hora*100+minuto");
            schedulerConfig.setTipoAlerta(SCHEDULER_CONFIG_TYPE);
            schedulerConfig.setDiasAnticipacion(horarioCompacto);
            schedulerConfig.setActiva(habilitado);
            em.persist(schedulerConfig);
            LOGGER.info("⚙️ Configuración del scheduler creada: " + hora + ":" + String.format("%02d", minuto));
        } else {
            // Actualizar registro existente
            schedulerConfig.setDiasAnticipacion(horarioCompacto);
            schedulerConfig.setActiva(habilitado);
            em.merge(schedulerConfig);
            LOGGER.info("⚙️ Configuración del scheduler actualizada: " + hora + ":" + String.format("%02d", minuto) +
                    " - " + (habilitado ? "Habilitado" : "Deshabilitado"));
        }
    }

    /**
     * Actualiza solo el estado habilitado/deshabilitado del scheduler
     */
    @Transactional
    public void setSchedulerHabilitado(boolean habilitado) {
        ConfiguracionAlertaModel schedulerConfig = findByTipo(SCHEDULER_CONFIG_TYPE);

        if (schedulerConfig != null) {
            schedulerConfig.setActiva(habilitado);
            em.merge(schedulerConfig);
            LOGGER.info("⚙️ Scheduler " + (habilitado ? "HABILITADO" : "DESHABILITADO"));
        } else {
            // Crear con valores por defecto
            saveSchedulerConfig(8, 0, habilitado);
        }
    }

    /**
     * Actualiza solo el horario del scheduler
     */
    @Transactional
    public void setSchedulerHorario(int hora, int minuto) {
        Map<String, Object> config = getSchedulerConfig();
        boolean habilitado = (Boolean) config.get("habilitado");
        saveSchedulerConfig(hora, minuto, habilitado);
    }

    /**
     * Obtiene configuraciones activas cuyo tipo empieza con el prefijo dado,
     * ordenadas por diasAnticipacion ASC (para resolución dinámica de umbrales)
     */
    public List<ConfiguracionAlertaModel> findActivasByTipoPrefix(String prefix) {
        return em.createQuery(
                "SELECT c FROM ConfiguracionAlertaModel c " +
                        "WHERE c.tipoAlerta LIKE :prefix AND c.activa = true " +
                        "ORDER BY c.diasAnticipacion ASC",
                ConfiguracionAlertaModel.class)
                .setParameter("prefix", prefix + "%")
                .getResultList();
    }

    /**
     * Busca configuración por ID
     */
    public ConfiguracionAlertaModel findById(Integer id) {
        return em.find(ConfiguracionAlertaModel.class, id);
    }

    /**
     * Guarda o actualiza una configuración
     */
    @Transactional
    public ConfiguracionAlertaModel save(ConfiguracionAlertaModel config) {
        if (config.getIdConfiguracion() == null) {
            em.persist(config);
            LOGGER.info("⚙️ Configuración de alerta creada: " + config.getNombre());
        } else {
            config = em.merge(config);
            LOGGER.info("⚙️ Configuración de alerta actualizada: " + config.getNombre());
        }
        return config;
    }

    /**
     * Elimina una configuración
     */
    @Transactional
    public void delete(Integer id) {
        ConfiguracionAlertaModel config = findById(id);
        if (config != null) {
            em.remove(config);
            LOGGER.info("🗑️ Configuración de alerta eliminada: " + id);
        }
    }

    /**
     * Inicializa configuraciones por defecto si no existen
     */
    @Transactional
    public void inicializarConfiguracionesPorDefecto() {
        LOGGER.info("⚙️ Verificando configuraciones de alerta...");

        // Verificar y crear configuración del scheduler si no existe
        ConfiguracionAlertaModel schedulerConfig = findByTipo(SCHEDULER_CONFIG_TYPE);
        if (schedulerConfig == null) {
            LOGGER.info("⚙️ Creando configuración del scheduler por defecto...");
            schedulerConfig = new ConfiguracionAlertaModel();
            schedulerConfig.setNombre("Configuración del Scheduler");
            schedulerConfig
                    .setDescripcion("Hora de ejecución del scheduler (formato: hora*100+minuto). Ej: 830 = 8:30 AM");
            schedulerConfig.setTipoAlerta(SCHEDULER_CONFIG_TYPE);
            schedulerConfig.setDiasAnticipacion(800); // 8:00 AM por defecto
            schedulerConfig.setActiva(true);
            em.persist(schedulerConfig);
            LOGGER.info("✅ Configuración del scheduler creada: 8:00 AM");
        }

        // Verificar si ya existen configuraciones de alertas
        List<ConfiguracionAlertaModel> existentes = findAll();
        if (!existentes.isEmpty()) {
            LOGGER.info("⚙️ Configuraciones de alertas ya existentes: " + existentes.size());
            return;
        }

        // Crear configuraciones por defecto
        LOGGER.info("⚙️ Inicializando configuraciones de alerta por defecto...");

        // Alerta de mantenimientos próximos a vencer
        ConfiguracionAlertaModel alertaMantenimiento = new ConfiguracionAlertaModel(
                "Mantenimientos Próximos", "mantenimiento_proximo", 7);
        alertaMantenimiento.setDescripcion("Alerta cuando un mantenimiento está próximo a vencer");
        em.persist(alertaMantenimiento);

        // Alerta de contratos próximos a vencer (30 días)
        ConfiguracionAlertaModel alertaContrato30 = new ConfiguracionAlertaModel(
                "Contratos por Vencer (30 días)", "contrato_proximo_30", 30);
        alertaContrato30.setDescripcion("Alerta cuando un contrato vence en 30 días");
        em.persist(alertaContrato30);

        // Alerta de contratos próximos a vencer (15 días)
        ConfiguracionAlertaModel alertaContrato15 = new ConfiguracionAlertaModel(
                "Contratos por Vencer (15 días)", "contrato_proximo_15", 15);
        alertaContrato15.setDescripcion("Alerta cuando un contrato vence en 15 días");
        em.persist(alertaContrato15);

        // Alerta de contratos próximos a vencer (7 días)
        ConfiguracionAlertaModel alertaContrato7 = new ConfiguracionAlertaModel(
                "Contratos por Vencer (7 días)", "contrato_proximo_7", 7);
        alertaContrato7.setDescripcion("Alerta urgente cuando un contrato vence en 7 días");
        em.persist(alertaContrato7);

        LOGGER.info("✅ Configuraciones de alerta inicializadas correctamente");
    }
}
