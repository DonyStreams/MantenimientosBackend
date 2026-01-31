package usac.eps.repositorios.mantenimientos;

import usac.eps.modelos.mantenimientos.NotificacionModel;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * Repositorio para gestión de notificaciones del sistema
 */
@ApplicationScoped
public class NotificacionRepository {
    private static final Logger LOGGER = Logger.getLogger(NotificacionRepository.class.getName());

    @PersistenceContext(unitName = "usac.eps_ControlSuministros")
    private EntityManager em;

    /**
     * Obtiene todas las notificaciones ordenadas por fecha
     */
    public List<NotificacionModel> findAll() {
        return em.createNamedQuery("Notificacion.findAll", NotificacionModel.class)
                .getResultList();
    }

    /**
     * Obtiene notificaciones no leídas
     */
    public List<NotificacionModel> findNoLeidas() {
        return em.createNamedQuery("Notificacion.findNoLeidas", NotificacionModel.class)
                .getResultList();
    }

    /**
     * Obtiene notificaciones por tipo
     */
    public List<NotificacionModel> findByTipo(String tipo) {
        return em.createNamedQuery("Notificacion.findByTipo", NotificacionModel.class)
                .setParameter("tipo", tipo)
                .getResultList();
    }

    /**
     * Cuenta notificaciones no leídas
     */
    public Long countNoLeidas() {
        return em.createNamedQuery("Notificacion.countNoLeidas", Long.class)
                .getSingleResult();
    }

    /**
     * Cuenta notificaciones por prioridad
     */
    public Long countByPrioridad(String prioridad) {
        return em.createNamedQuery("Notificacion.countByPrioridad", Long.class)
                .setParameter("prioridad", prioridad)
                .getSingleResult();
    }

    /**
     * Obtiene notificaciones recientes (últimos N días)
     */
    public List<NotificacionModel> findRecientes(int dias) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -dias);
        return em.createNamedQuery("Notificacion.findRecientes", NotificacionModel.class)
                .setParameter("fechaDesde", cal.getTime())
                .getResultList();
    }

    /**
     * Obtiene notificaciones por entidad relacionada
     */
    public List<NotificacionModel> findByEntidad(String entidad, Integer entidadId) {
        return em.createNamedQuery("Notificacion.findByEntidad", NotificacionModel.class)
                .setParameter("entidad", entidad)
                .setParameter("entidadId", entidadId)
                .getResultList();
    }

    /**
     * Busca una notificación por ID
     */
    public NotificacionModel findById(Integer id) {
        return em.find(NotificacionModel.class, id);
    }

    /**
     * Guarda una nueva notificación - REQUIRES_NEW para forzar commit independiente
     */
    @Transactional(TxType.REQUIRES_NEW)
    public NotificacionModel save(NotificacionModel notificacion) {
        if (notificacion.getIdNotificacion() == null) {
            em.persist(notificacion);
            LOGGER.info("📝 Notificación creada: " + notificacion.getTitulo());
        } else {
            notificacion = em.merge(notificacion);
            LOGGER.info("📝 Notificación actualizada: " + notificacion.getTitulo());
        }
        return notificacion;
    }

    /**
     * Marca una notificación como leída
     */
    @Transactional
    public NotificacionModel marcarComoLeida(Integer id) {
        NotificacionModel notificacion = findById(id);
        if (notificacion != null) {
            notificacion.setLeida(true);
            notificacion.setFechaLectura(new Date());
            em.merge(notificacion);
            LOGGER.info("✅ Notificación marcada como leída: " + id);
        }
        return notificacion;
    }

    /**
     * Marca todas las notificaciones como leídas
     */
    @Transactional
    public int marcarTodasComoLeidas() {
        int updated = em.createQuery(
                "UPDATE NotificacionModel n SET n.leida = true, n.fechaLectura = :fecha WHERE n.leida = false")
                .setParameter("fecha", new Date())
                .executeUpdate();
        LOGGER.info("✅ " + updated + " notificaciones marcadas como leídas");
        return updated;
    }

    /**
     * Elimina una notificación
     */
    @Transactional
    public void delete(Integer id) {
        NotificacionModel notificacion = findById(id);
        if (notificacion != null) {
            em.remove(notificacion);
            LOGGER.info("🗑️ Notificación eliminada: " + id);
        }
    }

    /**
     * Verifica si ya existe una notificación similar (para evitar duplicados)
     */
    public boolean existeNotificacionReciente(String tipo, String entidad, Integer entidadId, int horasAtras) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -horasAtras);

        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(n) FROM NotificacionModel n " +
                        "WHERE n.tipoNotificacion = :tipo " +
                        "AND n.entidadRelacionada = :entidad " +
                        "AND n.entidadId = :entidadId " +
                        "AND n.fechaCreacion >= :fechaDesde",
                Long.class);

        Long count = query.setParameter("tipo", tipo)
                .setParameter("entidad", entidad)
                .setParameter("entidadId", entidadId)
                .setParameter("fechaDesde", cal.getTime())
                .getSingleResult();

        return count > 0;
    }

    /**
     * Elimina notificaciones antiguas (limpieza periódica)
     */
    @Transactional
    public int eliminarAnterioresA(int diasAtras) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -diasAtras);

        int deleted = em.createQuery(
                "DELETE FROM NotificacionModel n WHERE n.leida = true AND n.fechaCreacion < :fecha")
                .setParameter("fecha", cal.getTime())
                .executeUpdate();
        LOGGER.info("🗑️ " + deleted + " notificaciones antiguas eliminadas");
        return deleted;
    }

    /**
     * Elimina TODAS las notificaciones
     */
    @Transactional
    public int eliminarTodas() {
        int deleted = em.createQuery("DELETE FROM NotificacionModel n").executeUpdate();
        LOGGER.info("🗑️ " + deleted + " notificaciones eliminadas (todas)");
        return deleted;
    }
}
