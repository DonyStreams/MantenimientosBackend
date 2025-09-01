package usac.eps.repositorios.mantenimientos;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.Repository;
import usac.eps.modelos.mantenimientos.ContratoModel;

import java.util.Date;
import java.util.List;

/**
 * Repositorio para gestión de contratos de mantenimiento
 */
@Repository
public interface ContratoRepository extends EntityRepository<ContratoModel, Integer> {

    // Consultas básicas existentes
    ContratoModel findByIdContrato(Integer idContrato);

    List<ContratoModel> findByEstado(Boolean estado);

    // 🚀 NUEVAS CONSULTAS AÑADIDAS

    // Consultas ordenadas
    List<ContratoModel> findByEstadoOrderByFechaCreacionDesc(Boolean estado);

    List<ContratoModel> findByProveedorIdProveedorOrderByFechaCreacionDesc(Integer proveedorId);

    // Consultas de negocio para gestión de contratos
    @Query("SELECT c FROM ContratoModel c WHERE c.estado = true AND c.fechaFin >= CURRENT_DATE ORDER BY c.fechaFin ASC")
    List<ContratoModel> findVigentes();

    @Query("SELECT c FROM ContratoModel c WHERE c.estado = true AND c.fechaFin BETWEEN CURRENT_DATE AND ?1 ORDER BY c.fechaFin ASC")
    List<ContratoModel> findPorVencer(Date fechaLimite);

    @Query("SELECT c FROM ContratoModel c WHERE c.estado = true AND c.fechaFin < CURRENT_DATE ORDER BY c.fechaFin DESC")
    List<ContratoModel> findVencidos();

    @Query("SELECT c FROM ContratoModel c WHERE c.frecuencia = ?1 AND c.estado = true ORDER BY c.fechaFin ASC")
    List<ContratoModel> findByFrecuencia(String frecuencia);

    // Estadísticas para dashboard
    @Query("SELECT COUNT(c) FROM ContratoModel c WHERE c.estado = true")
    Long countActivos();

    @Query("SELECT COUNT(c) FROM ContratoModel c WHERE c.estado = true AND c.fechaFin >= CURRENT_DATE")
    Long countVigentes();

    @Query("SELECT COUNT(c) FROM ContratoModel c WHERE c.estado = true AND c.fechaFin BETWEEN CURRENT_DATE AND ?1")
    Long countPorVencer(Date fechaLimite);

    @Query("SELECT COUNT(c) FROM ContratoModel c WHERE c.estado = true AND c.fechaFin < CURRENT_DATE")
    Long countVencidos();

    @Query("SELECT COUNT(c) FROM ContratoModel c WHERE c.estado = ?1")
    Long countByEstado(Boolean estado);

    // Búsquedas avanzadas con filtros múltiples
    @Query("SELECT DISTINCT c FROM ContratoModel c " +
            "LEFT JOIN c.equipos ce LEFT JOIN ce.equipo e " +
            "WHERE (?1 IS NULL OR LOWER(c.descripcion) LIKE LOWER(CONCAT('%', ?1, '%'))) " +
            "AND (?2 IS NULL OR c.proveedor.idProveedor = ?2) " +
            "AND (?3 IS NULL OR c.frecuencia = ?3) " +
            "AND (?4 IS NULL OR c.estado = ?4) " +
            "AND (?5 IS NULL OR e.idEquipo = ?5) " +
            "ORDER BY c.fechaCreacion DESC")
    List<ContratoModel> buscarConFiltros(String descripcion, Integer proveedorId, String frecuencia, Boolean estado,
            Integer equipoId);
}
