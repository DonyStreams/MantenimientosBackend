package usac.eps.servicios.mantenimientos;

import usac.eps.modelos.mantenimientos.NotificacionModel;
import usac.eps.modelos.mantenimientos.ConfiguracionAlertaModel;
import usac.eps.repositorios.mantenimientos.NotificacionRepository;
import usac.eps.repositorios.mantenimientos.ConfiguracionAlertaRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servicio para gestión de notificaciones del sistema
 * Incluye verificación de mantenimientos y contratos próximos a vencer
 */
@ApplicationScoped
@Transactional
public class NotificacionService {

    private static final Logger LOGGER = Logger.getLogger(NotificacionService.class.getName());
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    @PersistenceContext(unitName = "usac.eps_ControlSuministros")
    private EntityManager em;

    @Inject
    private NotificacionRepository notificacionRepository;

    @Inject
    private ConfiguracionAlertaRepository configuracionRepository;

    @Inject
    private EmailService emailService;

    /**
     * Obtiene todas las notificaciones
     */
    public List<NotificacionModel> obtenerTodas() {
        return notificacionRepository.findAll();
    }

    /**
     * Obtiene notificaciones no leídas
     */
    public List<NotificacionModel> obtenerNoLeidas() {
        return notificacionRepository.findNoLeidas();
    }

    /**
     * Obtiene contadores de notificaciones
     */
    public Map<String, Long> obtenerContadores() {
        Map<String, Long> contadores = new HashMap<>();
        contadores.put("total", notificacionRepository.countNoLeidas());
        contadores.put("criticas", notificacionRepository.countByPrioridad("Alta"));
        contadores.put("alertas", notificacionRepository.countByPrioridad("Media"));
        contadores.put("informativas", notificacionRepository.countByPrioridad("Baja"));
        return contadores;
    }

    /**
     * Marca notificación como leída
     */
    @Transactional
    public NotificacionModel marcarComoLeida(Integer id) {
        return notificacionRepository.marcarComoLeida(id);
    }

    /**
     * Marca todas como leídas
     */
    @Transactional
    public int marcarTodasComoLeidas() {
        return notificacionRepository.marcarTodasComoLeidas();
    }

    /**
     * Crea una notificación (uso interno - para llamadas desde el mismo servicio)
     */
    private NotificacionModel crearNotificacionInterna(String tipo, String titulo, String mensaje,
            String prioridad, String entidad, Integer entidadId) {
        NotificacionModel notificacion = new NotificacionModel(tipo, titulo, mensaje, prioridad);
        notificacion.setEntidadRelacionada(entidad);
        notificacion.setEntidadId(entidadId);

        // Persistir usando el repositorio que maneja transacciones
        notificacion = notificacionRepository.save(notificacion);
        LOGGER.info("💾 Notificación persistida: " + titulo + " (ID: " + notificacion.getIdNotificacion() + ")");

        return notificacion;
    }

    /**
     * Crea una notificación (uso externo - para llamadas desde otros servicios)
     */
    public NotificacionModel crearNotificacion(String tipo, String titulo, String mensaje,
            String prioridad, String entidad, Integer entidadId) {
        return crearNotificacionInterna(tipo, titulo, mensaje, prioridad, entidad, entidadId);
    }

    /**
     * Verifica y crea alertas para mantenimientos próximos a vencer o vencidos
     * Genera alertas escalonadas: a 30, 15 y 7 días del vencimiento (igual que
     * contratos)
     */
    @Transactional
    public List<NotificacionModel> verificarMantenimientosProximos() {
        List<NotificacionModel> notificacionesCreadas = new ArrayList<>();

        try {
            // Obtener configuraciones escalonadas para mantenimientos
            ConfiguracionAlertaModel config30 = configuracionRepository.findByTipo("mantenimiento_proximo_30");
            ConfiguracionAlertaModel config15 = configuracionRepository.findByTipo("mantenimiento_proximo_15");
            ConfiguracionAlertaModel config7 = configuracionRepository.findByTipo("mantenimiento_proximo_7");
            ConfiguracionAlertaModel configVencido = configuracionRepository.findByTipo("mantenimiento_vencido");

            // Verificar si están habilitadas
            boolean alguna30Habilitada = config30 != null && config30.getActiva();
            boolean alguna15Habilitada = config15 != null && config15.getActiva();
            boolean alguna7Habilitada = config7 != null && config7.getActiva();
            boolean vencidoHabilitado = configVencido == null || configVencido.getActiva(); // Por defecto habilitado

            if (!alguna30Habilitada && !alguna15Habilitada && !alguna7Habilitada && !vencidoHabilitado) {
                LOGGER.info("⏭️ Todas las alertas de mantenimientos deshabilitadas - omitiendo verificación");
                return notificacionesCreadas;
            }

            LOGGER.info("🔍 Verificando mantenimientos (30d:" + alguna30Habilitada +
                    ", 15d:" + alguna15Habilitada + ", 7d:" + alguna7Habilitada +
                    ", vencido:" + vencidoHabilitado + ")...");

            // Consulta para obtener programaciones con mantenimientos próximos O VENCIDOS
            // (hasta 30 días)
            @SuppressWarnings("unchecked")
            List<Object[]> resultados = em.createNativeQuery(
                    "SELECT p.id_programacion, p.id_equipo, p.fecha_proximo_mantenimiento, " +
                            "       e.nombre as equipo_nombre, e.codigo_inacif, " +
                            "       DATEDIFF(day, GETDATE(), p.fecha_proximo_mantenimiento) as dias_restantes, " +
                            "       tm.nombre as tipo_mantenimiento " +
                            "FROM Programaciones_Mantenimiento p " +
                            "INNER JOIN Equipos e ON p.id_equipo = e.id_equipo " +
                            "INNER JOIN Tipos_Mantenimiento tm ON p.id_tipo_mantenimiento = tm.id_tipo " +
                            "WHERE p.activa = 1 " +
                            "AND p.fecha_proximo_mantenimiento IS NOT NULL " +
                            "AND DATEDIFF(day, GETDATE(), p.fecha_proximo_mantenimiento) BETWEEN -30 AND 30 " +
                            "ORDER BY p.fecha_proximo_mantenimiento ASC")
                    .getResultList();

            LOGGER.info("📋 Programaciones encontradas: " + resultados.size());

            for (Object[] row : resultados) {
                Integer programacionId = (Integer) row[0];
                Integer equipoId = (Integer) row[1];
                Date fechaProxima = (Date) row[2];
                String equipoNombre = (String) row[3];
                String codigoInacif = (String) row[4];
                Integer diasRestantes = (Integer) row[5];
                String tipoMantenimiento = (String) row[6];

                LOGGER.info("📌 Procesando: " + equipoNombre + " - días: " + diasRestantes);

                // Determinar qué tipo de alerta generar según los días restantes
                String tipoAlerta;
                boolean alertaHabilitada;
                String prioridad;
                String urgencia;

                if (diasRestantes < 0) {
                    // VENCIDO
                    tipoAlerta = "mantenimiento_vencido";
                    alertaHabilitada = vencidoHabilitado;
                    prioridad = "Alta";
                    urgencia = "VENCIDO hace " + Math.abs(diasRestantes) + " días";
                } else if (diasRestantes <= 7) {
                    tipoAlerta = "mantenimiento_proximo_7";
                    alertaHabilitada = alguna7Habilitada;
                    prioridad = "Alta";
                    if (diasRestantes == 0) {
                        urgencia = "HOY";
                    } else if (diasRestantes == 1) {
                        urgencia = "MAÑANA";
                    } else {
                        urgencia = "en " + diasRestantes + " días";
                    }
                } else if (diasRestantes <= 15) {
                    tipoAlerta = "mantenimiento_proximo_15";
                    alertaHabilitada = alguna15Habilitada;
                    prioridad = "Media";
                    urgencia = "en " + diasRestantes + " días";
                } else {
                    tipoAlerta = "mantenimiento_proximo_30";
                    alertaHabilitada = alguna30Habilitada;
                    prioridad = "Baja";
                    urgencia = "en " + diasRestantes + " días";
                }

                // Si este tipo específico está deshabilitado, omitir
                if (!alertaHabilitada) {
                    LOGGER.info(
                            "⏭️ Alerta " + tipoAlerta + " deshabilitada - omitiendo programación #" + programacionId);
                    continue;
                }

                // Verificar si ya existe notificación reciente (últimas 24h) para este tipo
                if (notificacionRepository.existeNotificacionReciente(
                        tipoAlerta, "programacion", programacionId, 24)) {
                    LOGGER.info("⏭️ Ya existe notificación reciente para programación " + programacionId + " ("
                            + tipoAlerta + ")");
                    continue;
                }

                String titulo = diasRestantes < 0
                        ? "[VENCIDO] Mantenimiento: " + equipoNombre
                        : "Mantenimiento proximo: " + equipoNombre;
                String mensaje = String.format(
                        "%s - El equipo %s (%s) tiene programado un %s para el %s. " +
                                "Por favor coordine la ejecución del mantenimiento.",
                        urgencia,
                        equipoNombre, codigoInacif != null ? codigoInacif : "Sin código",
                        tipoMantenimiento,
                        DATE_FORMAT.format(fechaProxima));

                NotificacionModel notificacion = crearNotificacionInterna(
                        tipoAlerta, titulo, mensaje, prioridad, "programacion", programacionId);
                notificacionesCreadas.add(notificacion);

                LOGGER.info("📢 Alerta creada: " + titulo + " (días: " + diasRestantes + ", tipo: " + tipoAlerta + ")");

                // Enviar correo siempre que se crea una alerta
                try {
                    LOGGER.info(
                            "📧 Enviando correo de mantenimiento (" + tipoAlerta + ", días: " + diasRestantes + ")");
                    emailService.notificarMantenimientoProximo(
                            programacionId, equipoNombre, codigoInacif,
                            tipoMantenimiento, fechaProxima, diasRestantes, tipoAlerta);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "No se pudo enviar correo de mantenimiento próximo", e);
                }
            }

            LOGGER.info("✅ Verificación completada. " + notificacionesCreadas.size()
                    + " alertas de mantenimiento creadas.");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ Error al verificar mantenimientos próximos: " + e.getMessage(), e);
        }

        return notificacionesCreadas;
    }

    /**
     * Verifica y crea alertas para contratos próximos a vencer o vencidos
     * Genera alertas escalonadas: a 30, 15 y 7 días del vencimiento, y vencidos
     */
    @Transactional
    public List<NotificacionModel> verificarContratosProximos() {
        List<NotificacionModel> notificacionesCreadas = new ArrayList<>();

        try {
            // Verificar si las alertas de contratos están habilitadas
            ConfiguracionAlertaModel config30 = configuracionRepository.findByTipo("contrato_proximo_30");
            ConfiguracionAlertaModel config15 = configuracionRepository.findByTipo("contrato_proximo_15");
            ConfiguracionAlertaModel config7 = configuracionRepository.findByTipo("contrato_proximo_7");
            ConfiguracionAlertaModel configVencido = configuracionRepository.findByTipo("contrato_vencido");

            // Verificar cuáles están habilitadas
            boolean alguna30Habilitada = config30 != null && config30.getActiva();
            boolean alguna15Habilitada = config15 != null && config15.getActiva();
            boolean alguna7Habilitada = config7 != null && config7.getActiva();
            boolean vencidoHabilitado = configVencido == null || configVencido.getActiva(); // Por defecto habilitado

            if (!alguna30Habilitada && !alguna15Habilitada && !alguna7Habilitada && !vencidoHabilitado) {
                LOGGER.info("⏭️ Todas las alertas de contratos deshabilitadas - omitiendo verificación");
                return notificacionesCreadas;
            }

            LOGGER.info("🔍 Verificando contratos (30d:" + alguna30Habilitada +
                    ", 15d:" + alguna15Habilitada + ", 7d:" + alguna7Habilitada +
                    ", vencido:" + vencidoHabilitado + ")...");

            // Consulta para obtener contratos que vencen en los próximos 30 días O YA
            // VENCIDOS
            @SuppressWarnings("unchecked")
            List<Object[]> resultados = em.createNativeQuery(
                    "SELECT c.id_contrato, c.descripcion, c.fecha_fin, " +
                            "       p.nombre as proveedor_nombre, " +
                            "       DATEDIFF(day, GETDATE(), c.fecha_fin) as dias_restantes " +
                            "FROM Contratos c " +
                            "INNER JOIN Proveedores p ON c.id_proveedor = p.id_proveedor " +
                            "WHERE c.estado = 1 " +
                            "AND c.fecha_fin IS NOT NULL " +
                            "AND DATEDIFF(day, GETDATE(), c.fecha_fin) BETWEEN -30 AND 30 " +
                            "ORDER BY c.fecha_fin ASC")
                    .getResultList();

            LOGGER.info("📋 Contratos encontrados próximos a vencer: " + resultados.size());

            for (Object[] row : resultados) {
                Integer contratoId = (Integer) row[0];
                String descripcion = (String) row[1];
                Date fechaFin = (Date) row[2];
                String proveedorNombre = (String) row[3];
                Integer diasRestantes = (Integer) row[4];

                // Determinar qué tipo de alerta generar según los días restantes
                String tipoAlerta;
                boolean alertaHabilitada;
                String prioridad;
                String urgencia;

                if (diasRestantes < 0) {
                    // VENCIDO
                    tipoAlerta = "contrato_vencido";
                    alertaHabilitada = vencidoHabilitado;
                    prioridad = "Alta";
                    urgencia = "VENCIDO hace " + Math.abs(diasRestantes) + " días";
                } else if (diasRestantes <= 7) {
                    tipoAlerta = "contrato_proximo_7";
                    alertaHabilitada = alguna7Habilitada;
                    prioridad = "Alta";
                    urgencia = diasRestantes == 0 ? "VENCE HOY" : "URGENTE (" + diasRestantes + " días)";
                } else if (diasRestantes <= 15) {
                    tipoAlerta = "contrato_proximo_15";
                    alertaHabilitada = alguna15Habilitada;
                    prioridad = "Media";
                    urgencia = "Vence en " + diasRestantes + " días";
                } else {
                    tipoAlerta = "contrato_proximo_30";
                    alertaHabilitada = alguna30Habilitada;
                    prioridad = "Baja";
                    urgencia = "Vence en " + diasRestantes + " días";
                }

                // Si este tipo específico está deshabilitado, omitir
                if (!alertaHabilitada) {
                    LOGGER.info("⏭️ Alerta " + tipoAlerta + " deshabilitada - omitiendo contrato #" + contratoId);
                    continue;
                }

                // Verificar si ya existe notificación reciente (últimas 24h) para este contrato
                // y tipo
                if (notificacionRepository.existeNotificacionReciente(
                        tipoAlerta, "contrato", contratoId, 24)) {
                    LOGGER.info("⏭️ Notificación ya existe para contrato #" + contratoId + " (" + tipoAlerta + ")");
                    continue;
                }

                // Usar descripción o ID como identificador
                String identificador = (descripcion != null && !descripcion.isEmpty())
                        ? descripcion.substring(0, Math.min(50, descripcion.length()))
                        : "Contrato #" + contratoId;

                String titulo = diasRestantes < 0
                        ? "[VENCIDO] Contrato: " + identificador
                        : "Contrato proximo a vencer: " + identificador;
                String mensaje = String.format(
                        "%s - El contrato con %s " + (diasRestantes < 0 ? "venció" : "vence") + " el %s. " +
                                "Descripción: %s. Por favor gestione la renovación o nueva contratación.",
                        urgencia, proveedorNombre,
                        DATE_FORMAT.format(fechaFin),
                        descripcion != null ? descripcion : "Sin descripción");

                NotificacionModel notificacion = crearNotificacionInterna(
                        tipoAlerta, titulo, mensaje, prioridad, "contrato", contratoId);
                notificacionesCreadas.add(notificacion);

                LOGGER.info("📢 Alerta creada: " + titulo + " (días: " + diasRestantes + ", tipo: " + tipoAlerta + ")");

                // Enviar correo según el tipo de alerta que disparó
                // contrato_proximo_7 = urgente, contrato_proximo_15 = medio,
                // contrato_proximo_30 = informativo
                // Enviamos correo siempre que se crea una alerta (ya está dentro del rango
                // configurado)
                try {
                    LOGGER.info("📧 Enviando correo de contrato próximo a vencer (días: " + diasRestantes +
                            ", tipo: " + tipoAlerta + ")");
                    emailService.notificarContratoProximo(
                            contratoId, identificador, descripcion,
                            proveedorNombre, fechaFin, diasRestantes, tipoAlerta);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "No se pudo enviar correo de contrato próximo", e);
                }
            }

            LOGGER.info(
                    "✅ Verificación completada. " + notificacionesCreadas.size() + " alertas de contratos creadas.");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ Error al verificar contratos próximos", e);
        }

        return notificacionesCreadas;
    }

    /**
     * Ejecuta todas las verificaciones de alertas
     */
    @Transactional
    public Map<String, Object> ejecutarVerificacionCompleta() {
        LOGGER.info("🚀🚀🚀 INICIANDO VERIFICACIÓN COMPLETA DE ALERTAS 🚀🚀🚀");
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("fechaEjecucion", new Date());

        LOGGER.info("📋 Verificando mantenimientos próximos y vencidos...");
        List<NotificacionModel> mantenimientos = verificarMantenimientosProximos();

        LOGGER.info("📋 Verificando contratos próximos a vencer...");
        List<NotificacionModel> contratos = verificarContratosProximos();

        resultado.put("alertasMantenimiento", mantenimientos.size());
        resultado.put("alertasContrato", contratos.size());
        resultado.put("totalAlertas", mantenimientos.size() + contratos.size());

        LOGGER.info("📊📊📊 VERIFICACIÓN COMPLETA - Resumen: " +
                mantenimientos.size() + " mantenimientos, " +
                contratos.size() + " contratos 📊📊📊");

        return resultado;
    }

    /**
     * Limpia notificaciones antiguas leídas
     */
    @Transactional
    public int limpiarNotificacionesAntiguas(int diasAtras) {
        return notificacionRepository.eliminarAnterioresA(diasAtras);
    }
}
