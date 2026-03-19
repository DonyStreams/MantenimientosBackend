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
    private static final int VENCIDOS_LOOKBACK_DIAS = 3650;

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
     * Verifica y crea alertas para mantenimientos próximos a vencer o vencidos.
     * Los umbrales de días se leen dinámicamente desde ConfiguracionAlerta,
     * buscando todos los registros activos con prefijo "mantenimiento_proximo".
     */
    @Transactional
    public List<NotificacionModel> verificarMantenimientosProximos() {
        List<NotificacionModel> notificacionesCreadas = new ArrayList<>();

        try {
            // Cargar todas las configuraciones activas de mantenimiento ordenadas por
            // diasAnticipacion ASC
            List<ConfiguracionAlertaModel> configs = configuracionRepository
                    .findActivasByTipoPrefix("mantenimiento_proximo");
            ConfiguracionAlertaModel configVencido = configuracionRepository.findByTipo("mantenimiento_vencido");
            boolean vencidoHabilitado = configVencido == null || configVencido.getActiva();

            if (configs.isEmpty() && !vencidoHabilitado) {
                LOGGER.info("⏭️ Todas las alertas de mantenimientos deshabilitadas - omitiendo verificación");
                return notificacionesCreadas;
            }

            // Calcular el rango máximo dinámicamente
            int maxDias = configs.stream()
                    .mapToInt(ConfiguracionAlertaModel::getDiasAnticipacion)
                    .max().orElse(30);
            // Para alertas vencidas se usa una ventana fija interna (no configurable por
            // días).
            int vencidosLookback = VENCIDOS_LOOKBACK_DIAS;
            int negBound = vencidoHabilitado ? -vencidosLookback : 0;

            LOGGER.info("🔍 Verificando mantenimientos con " + configs.size() +
                    " configuraciones activas (máx " + maxDias + " días, negBound:" + negBound + ", vencido:"
                    + vencidoHabilitado + ")");

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
                            "AND DATEDIFF(day, GETDATE(), p.fecha_proximo_mantenimiento) BETWEEN ? AND ? "
                            +
                            "ORDER BY p.fecha_proximo_mantenimiento ASC")
                    .setParameter(1, negBound)
                    .setParameter(2, maxDias)
                    .getResultList();

            LOGGER.info("📋 Programaciones encontradas: " + resultados.size());

            for (Object[] row : resultados) {
                Integer programacionId = toInteger(row[0]);
                Date fechaProxima = (Date) row[2];
                String equipoNombre = (String) row[3];
                String codigoInacif = (String) row[4];
                Integer diasRestantes = toInteger(row[5]);
                String tipoMantenimiento = (String) row[6];

                LOGGER.info("📌 Procesando: " + equipoNombre + " - días: " + diasRestantes);

                String tipoAlerta;
                String prioridad;
                String urgencia;
                boolean alertaHabilitada;

                if (diasRestantes < 0) {
                    tipoAlerta = "mantenimiento_vencido";
                    alertaHabilitada = vencidoHabilitado;
                    prioridad = "Alta";
                    urgencia = "VENCIDO hace " + Math.abs(diasRestantes) + " días";
                } else {
                    // Buscar la primera config (orden ASC) donde diasRestantes <= umbral
                    ConfiguracionAlertaModel matchingConfig = null;
                    for (ConfiguracionAlertaModel config : configs) {
                        if (diasRestantes <= config.getDiasAnticipacion()) {
                            matchingConfig = config;
                            break;
                        }
                    }
                    if (matchingConfig == null) {
                        LOGGER.info("⏭️ Sin configuración aplicable para programación #" + programacionId + " ("
                                + diasRestantes + " días)");
                        continue;
                    }

                    tipoAlerta = matchingConfig.getTipoAlerta();
                    alertaHabilitada = true; // ya filtrado por activa
                    urgencia = diasRestantes == 0 ? "HOY"
                            : diasRestantes == 1 ? "MAÑANA" : "en " + diasRestantes + " días";

                    // Prioridad según posición relativa en la lista ordenada
                    int idx = configs.indexOf(matchingConfig);
                    if (idx == 0)
                        prioridad = "Alta";
                    else if (idx == configs.size() - 1)
                        prioridad = "Baja";
                    else
                        prioridad = "Media";
                }

                if (!alertaHabilitada) {
                    LOGGER.info(
                            "⏭️ Alerta " + tipoAlerta + " deshabilitada - omitiendo programación #" + programacionId);
                    continue;
                }

                if (notificacionRepository.existeNotificacionReciente(tipoAlerta, "programacion", programacionId, 24)) {
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
                        urgencia, equipoNombre, codigoInacif != null ? codigoInacif : "Sin código",
                        tipoMantenimiento, DATE_FORMAT.format(fechaProxima));

                NotificacionModel notificacion = crearNotificacionInterna(
                        tipoAlerta, titulo, mensaje, prioridad, "programacion", programacionId);
                notificacionesCreadas.add(notificacion);

                LOGGER.info("📢 Alerta creada: " + titulo + " (días: " + diasRestantes + ", tipo: " + tipoAlerta + ")");

                try {
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
     * Verifica y crea alertas para contratos próximos a vencer o vencidos.
     * Los umbrales de días se leen dinámicamente desde ConfiguracionAlerta,
     * buscando todos los registros activos con prefijo "contrato_proximo".
     */
    @Transactional
    public List<NotificacionModel> verificarContratosProximos() {
        List<NotificacionModel> notificacionesCreadas = new ArrayList<>();

        try {
            // Cargar todas las configuraciones activas de contrato ordenadas por
            // diasAnticipacion ASC
            List<ConfiguracionAlertaModel> configs = configuracionRepository
                    .findActivasByTipoPrefix("contrato_proximo");
            ConfiguracionAlertaModel configVencido = configuracionRepository.findByTipo("contrato_vencido");
            boolean vencidoHabilitado = configVencido == null || configVencido.getActiva();

            if (configs.isEmpty() && !vencidoHabilitado) {
                LOGGER.info("⏭️ Todas las alertas de contratos deshabilitadas - omitiendo verificación");
                return notificacionesCreadas;
            }

            // Calcular el rango máximo dinámicamente
            int maxDias = configs.stream()
                    .mapToInt(ConfiguracionAlertaModel::getDiasAnticipacion)
                    .max().orElse(30);
            // Para alertas vencidas se usa una ventana fija interna (no configurable por
            // días).
            int vencidosLookback = VENCIDOS_LOOKBACK_DIAS;
            int negBound = vencidoHabilitado ? -vencidosLookback : 0;

            LOGGER.info("🔍 Verificando contratos con " + configs.size() +
                    " configuraciones activas (máx " + maxDias + " días, negBound:" + negBound + ", vencido:"
                    + vencidoHabilitado + ")");

            @SuppressWarnings("unchecked")
            List<Object[]> resultados = em.createNativeQuery(
                    "SELECT c.id_contrato, c.descripcion, c.fecha_fin, " +
                            "       p.nombre as proveedor_nombre, " +
                            "       DATEDIFF(day, GETDATE(), c.fecha_fin) as dias_restantes " +
                            "FROM Contratos c " +
                            "INNER JOIN Proveedores p ON c.id_proveedor = p.id_proveedor " +
                            "WHERE c.estado = 1 " +
                            "AND c.fecha_fin IS NOT NULL " +
                            "AND DATEDIFF(day, GETDATE(), c.fecha_fin) BETWEEN ? AND ? " +
                            "ORDER BY c.fecha_fin ASC")
                    .setParameter(1, negBound)
                    .setParameter(2, maxDias)
                    .getResultList();

            LOGGER.info("📋 Contratos encontrados próximos a vencer: " + resultados.size());

            for (Object[] row : resultados) {
                Integer contratoId = toInteger(row[0]);
                String descripcion = (String) row[1];
                Date fechaFin = (Date) row[2];
                String proveedorNombre = (String) row[3];
                Integer diasRestantes = toInteger(row[4]);

                String tipoAlerta;
                String prioridad;
                String urgencia;
                boolean alertaHabilitada;

                if (diasRestantes < 0) {
                    tipoAlerta = "contrato_vencido";
                    alertaHabilitada = vencidoHabilitado;
                    prioridad = "Alta";
                    urgencia = "VENCIDO hace " + Math.abs(diasRestantes) + " días";
                } else {
                    // Buscar la primera config (orden ASC) donde diasRestantes <= umbral
                    ConfiguracionAlertaModel matchingConfig = null;
                    for (ConfiguracionAlertaModel config : configs) {
                        if (diasRestantes <= config.getDiasAnticipacion()) {
                            matchingConfig = config;
                            break;
                        }
                    }
                    if (matchingConfig == null) {
                        LOGGER.info("⏭️ Sin configuración aplicable para contrato #" + contratoId + " (" + diasRestantes
                                + " días)");
                        continue;
                    }

                    tipoAlerta = matchingConfig.getTipoAlerta();
                    alertaHabilitada = true; // ya filtrado por activa
                    urgencia = diasRestantes == 0 ? "VENCE HOY" : "Vence en " + diasRestantes + " días";

                    // Prioridad según posición relativa en la lista ordenada
                    int idx = configs.indexOf(matchingConfig);
                    if (idx == 0)
                        prioridad = "Alta";
                    else if (idx == configs.size() - 1)
                        prioridad = "Baja";
                    else
                        prioridad = "Media";
                }

                if (!alertaHabilitada) {
                    LOGGER.info("⏭️ Alerta " + tipoAlerta + " deshabilitada - omitiendo contrato #" + contratoId);
                    continue;
                }

                if (notificacionRepository.existeNotificacionReciente(tipoAlerta, "contrato", contratoId, 24)) {
                    LOGGER.info("⏭️ Notificación ya existe para contrato #" + contratoId + " (" + tipoAlerta + ")");
                    continue;
                }

                String identificador = (descripcion != null && !descripcion.isEmpty())
                        ? descripcion.substring(0, Math.min(50, descripcion.length()))
                        : "Contrato #" + contratoId;

                String titulo = diasRestantes < 0
                        ? "[VENCIDO] Contrato: " + identificador
                        : "Contrato proximo a vencer: " + identificador;
                String mensaje = String.format(
                        "%s - El contrato con %s " + (diasRestantes < 0 ? "venció" : "vence") + " el %s. " +
                                "Descripción: %s. Por favor gestione la renovación o nueva contratación.",
                        urgencia, proveedorNombre, DATE_FORMAT.format(fechaFin),
                        descripcion != null ? descripcion : "Sin descripción");

                NotificacionModel notificacion = crearNotificacionInterna(
                        tipoAlerta, titulo, mensaje, prioridad, "contrato", contratoId);
                notificacionesCreadas.add(notificacion);

                LOGGER.info("📢 Alerta creada: " + titulo + " (días: " + diasRestantes + ", tipo: " + tipoAlerta + ")");

                try {
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
     * Verifica equipos en estado crítico y genera notificación/correo.
     * Se ejecuta en verificación manual y scheduler para cubrir equipos que ya
     * estaban
     * críticos aunque no hayan cambiado de estado recientemente.
     */
    @Transactional
    public List<NotificacionModel> verificarEquiposCriticos() {
        List<NotificacionModel> notificacionesCreadas = new ArrayList<>();

        try {
            ConfiguracionAlertaModel configCritico = configuracionRepository.findByTipo("equipo_critico");
            boolean criticoHabilitado = configCritico == null || configCritico.getActiva();

            if (!criticoHabilitado) {
                LOGGER.info("⏭️ Alerta equipo_critico deshabilitada - omitiendo verificación");
                return notificacionesCreadas;
            }

            @SuppressWarnings("unchecked")
            List<Object[]> resultados = em.createNativeQuery(
                    "SELECT e.id_equipo, e.nombre, e.codigo_inacif, e.ubicacion, e.estado " +
                            "FROM Equipos e " +
                            "WHERE UPPER(LTRIM(RTRIM(ISNULL(e.estado, '')))) IN ('CRITICO', 'CRÍTICO')")
                    .getResultList();

            LOGGER.info("📋 Equipos críticos encontrados: " + resultados.size());

            for (Object[] row : resultados) {
                Integer equipoId = toInteger(row[0]);
                String equipoNombre = (String) row[1];
                String codigoInacif = (String) row[2];
                String ubicacion = (String) row[3];

                if (notificacionRepository.existeNotificacionReciente("equipo_critico", "equipo", equipoId, 24)) {
                    LOGGER.info("⏭️ Ya existe notificación reciente para equipo crítico #" + equipoId);
                    continue;
                }

                String nombreSeguro = (equipoNombre == null || equipoNombre.trim().isEmpty())
                        ? ("Equipo #" + equipoId)
                        : equipoNombre;
                String codigoSeguro = (codigoInacif == null || codigoInacif.trim().isEmpty())
                        ? "N/A"
                        : codigoInacif;
                String ubicacionSegura = (ubicacion == null || ubicacion.trim().isEmpty())
                        ? "No especificada"
                        : ubicacion;

                String titulo = "[CRITICO] Equipo: " + nombreSeguro;
                String mensaje = String.format(
                        "El equipo %s (%s) se encuentra en estado CRITICO. Ubicación: %s. " +
                                "Por favor atender de forma inmediata.",
                        nombreSeguro, codigoSeguro, ubicacionSegura);

                NotificacionModel notificacion = crearNotificacionInterna(
                        "equipo_critico", titulo, mensaje, "Alta", "equipo", equipoId);
                notificacionesCreadas.add(notificacion);

                try {
                    emailService.notificarEquipoCritico(
                            equipoId,
                            nombreSeguro,
                            codigoSeguro,
                            ubicacionSegura,
                            "Crítico",
                            "Detectado por verificación automática/manual");
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "No se pudo enviar correo de equipo crítico", e);
                }
            }

            LOGGER.info("✅ Verificación de equipos críticos completada. " + notificacionesCreadas.size()
                    + " alertas creadas.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ Error al verificar equipos críticos", e);
        }

        return notificacionesCreadas;
    }

    /**
     * Verifica tickets con prioridad crítica y genera notificación/correo.
     * Se ejecuta en verificación manual y scheduler para cubrir tickets que ya
     * estaban críticos sin requerir cambio reciente de prioridad.
     */
    @Transactional
    public List<NotificacionModel> verificarTicketsCriticos() {
        List<NotificacionModel> notificacionesCreadas = new ArrayList<>();

        try {
            ConfiguracionAlertaModel configCritico = configuracionRepository.findByTipo("ticket_critico");
            boolean criticoHabilitado = configCritico == null || configCritico.getActiva();

            if (!criticoHabilitado) {
                LOGGER.info("⏭️ Alerta ticket_critico deshabilitada - omitiendo verificación");
                return notificacionesCreadas;
            }

            @SuppressWarnings("unchecked")
            List<Object[]> resultados = em.createNativeQuery(
                    "SELECT t.id, t.descripcion, t.estado, t.prioridad, " +
                            "       e.nombre AS equipo_nombre, e.codigo_inacif, e.ubicacion, " +
                            "       ua.nombre_completo AS usuario_asignado " +
                            "FROM Tickets t " +
                            "LEFT JOIN Equipos e ON t.equipo_id = e.id_equipo " +
                            "LEFT JOIN Usuarios ua ON t.usuario_asignado_id = ua.id " +
                            "WHERE UPPER(REPLACE(REPLACE(LTRIM(RTRIM(ISNULL(t.prioridad, ''))), 'Í', 'I'), 'í', 'i')) "
                            +
                            "      IN ('CRITICA', 'CRITICAA') " +
                            "AND UPPER(REPLACE(REPLACE(LTRIM(RTRIM(ISNULL(t.estado, ''))), 'Ó', 'O'), 'ó', 'o')) " +
                            "      NOT IN ('CERRADO', 'RESUELTO', 'FINALIZADO', 'CANCELADO')")
                    .getResultList();

            LOGGER.info("📋 Tickets críticos encontrados: " + resultados.size());

            for (Object[] row : resultados) {
                Integer ticketId = toInteger(row[0]);
                String descripcion = (String) row[1];
                String equipoNombre = (String) row[4];
                String codigoInacif = (String) row[5];
                String ubicacion = (String) row[6];
                String usuarioAsignado = (String) row[7];

                if (notificacionRepository.existeNotificacionReciente("ticket_critico", "ticket", ticketId, 24)) {
                    LOGGER.info("⏭️ Ya existe notificación reciente para ticket crítico #" + ticketId);
                    continue;
                }

                String descSegura = (descripcion == null || descripcion.trim().isEmpty())
                        ? "Sin descripción"
                        : descripcion;
                String equipoSeguro = (equipoNombre == null || equipoNombre.trim().isEmpty())
                        ? "Sin equipo"
                        : equipoNombre;
                String codigoSeguro = (codigoInacif == null || codigoInacif.trim().isEmpty())
                        ? "N/A"
                        : codigoInacif;
                String ubicacionSegura = (ubicacion == null || ubicacion.trim().isEmpty())
                        ? "No especificada"
                        : ubicacion;
                String usuarioAsignadoSeguro = (usuarioAsignado == null || usuarioAsignado.trim().isEmpty())
                        ? "Sin asignar"
                        : usuarioAsignado;

                String titulo = "[CRITICO] Ticket #" + ticketId + " - " + equipoSeguro;
                String mensaje = String.format(
                        "El ticket #%d está en prioridad CRITICA para el equipo %s (%s). " +
                                "Descripción: %s.",
                        ticketId, equipoSeguro, codigoSeguro, descSegura);

                NotificacionModel notificacion = crearNotificacionInterna(
                        "ticket_critico", titulo, mensaje, "Alta", "ticket", ticketId);
                notificacionesCreadas.add(notificacion);

                try {
                    emailService.notificarTicketCritico(
                            ticketId,
                            descSegura,
                            equipoSeguro,
                            codigoSeguro,
                            usuarioAsignadoSeguro,
                            ubicacionSegura);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "No se pudo enviar correo de ticket crítico", e);
                }
            }

            LOGGER.info("✅ Verificación de tickets críticos completada. " + notificacionesCreadas.size()
                    + " alertas creadas.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ Error al verificar tickets críticos", e);
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

        LOGGER.info("📋 Verificando equipos en estado crítico...");
        List<NotificacionModel> equiposCriticos = verificarEquiposCriticos();

        LOGGER.info("📋 Verificando tickets en prioridad crítica...");
        List<NotificacionModel> ticketsCriticos = verificarTicketsCriticos();

        resultado.put("alertasMantenimiento", mantenimientos.size());
        resultado.put("alertasContrato", contratos.size());
        resultado.put("alertasEquipoCritico", equiposCriticos.size());
        resultado.put("alertasTicketCritico", ticketsCriticos.size());
        resultado.put("totalAlertas",
                mantenimientos.size() + contratos.size() + equiposCriticos.size() + ticketsCriticos.size());

        LOGGER.info("📊📊📊 VERIFICACIÓN COMPLETA - Resumen: " +
                mantenimientos.size() + " mantenimientos, " +
                contratos.size() + " contratos, " +
                equiposCriticos.size() + " equipos críticos, " +
                ticketsCriticos.size() + " tickets críticos 📊📊📊");

        return resultado;
    }

    /**
     * Limpia notificaciones antiguas leídas
     */
    @Transactional
    public int limpiarNotificacionesAntiguas(int diasAtras) {
        return notificacionRepository.eliminarAnterioresA(diasAtras);
    }

    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            return Integer.parseInt((String) value);
        }
        throw new IllegalArgumentException("No se pudo convertir a Integer: " + value.getClass().getName());
    }

}
