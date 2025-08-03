package usac.eps.repositorios.mantenimientos;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.Repository;
import usac.eps.modelos.mantenimientos.ProgramacionMantenimientoModel;
import usac.eps.modelos.mantenimientos.EquipoModel;
import usac.eps.modelos.mantenimientos.TipoMantenimientoModel;

import java.util.Date;
import java.util.List;

@Repository
public interface ProgramacionMantenimientoRepository extends EntityRepository<ProgramacionMantenimientoModel, Integer> {

    /**
     * Busca todas las programaciones activas
     */
    List<ProgramacionMantenimientoModel> findByActivaOrderByFechaProximoMantenimiento(Boolean activa);

    /**
     * Busca programaciones por equipo
     */
    List<ProgramacionMantenimientoModel> findByEquipoAndActivaOrderByFechaProximoMantenimiento(EquipoModel equipo,
            Boolean activa);

    /**
     * Busca programaciones por tipo de mantenimiento
     */
    List<ProgramacionMantenimientoModel> findByTipoMantenimientoAndActivaOrderByFechaProximoMantenimiento(
            TipoMantenimientoModel tipoMantenimiento, Boolean activa);

    /**
     * Busca programaciones que requieren alerta (próximas a vencer)
     */
    @Query("SELECT p FROM ProgramacionMantenimientoModel p WHERE p.activa = true AND p.fechaProximoMantenimiento <= ?1 ORDER BY p.fechaProximoMantenimiento ASC")
    List<ProgramacionMantenimientoModel> findProgramacionesParaAlerta(Date fechaLimite);

    /**
     * Busca programaciones vencidas
     */
    @Query("SELECT p FROM ProgramacionMantenimientoModel p WHERE p.activa = true AND p.fechaProximoMantenimiento < ?1 ORDER BY p.fechaProximoMantenimiento ASC")
    List<ProgramacionMantenimientoModel> findProgramacionesVencidas(Date fechaActual);

    /**
     * Busca programaciones entre fechas
     */
    @Query("SELECT p FROM ProgramacionMantenimientoModel p WHERE p.activa = true AND p.fechaProximoMantenimiento BETWEEN ?1 AND ?2 ORDER BY p.fechaProximoMantenimiento ASC")
    List<ProgramacionMantenimientoModel> findProgramacionesEntreFechas(Date fechaInicio, Date fechaFin);

    /**
     * Busca si existe una programación para un equipo y tipo específico
     */
    ProgramacionMantenimientoModel findByEquipoAndTipoMantenimientoAndActiva(EquipoModel equipo,
            TipoMantenimientoModel tipoMantenimiento, Boolean activa);

    /**
     * Cuenta programaciones activas por equipo
     */
    @Query("SELECT COUNT(p) FROM ProgramacionMantenimientoModel p WHERE p.equipo = ?1 AND p.activa = true")
    Long countProgramacionesActivasByEquipo(EquipoModel equipo);

    /**
     * Busca programaciones por estado de alerta
     */
    @Query("SELECT p FROM ProgramacionMantenimientoModel p WHERE p.activa = true AND " +
            "(p.fechaProximoMantenimiento BETWEEN ?1 AND ?2) " +
            "ORDER BY p.fechaProximoMantenimiento ASC")
    List<ProgramacionMantenimientoModel> findProgramacionesEnAlerta(Date fechaActual, Date fechaAlerta);

    /**
     * Busca próximos mantenimientos por equipo en los siguientes días
     */
    @Query("SELECT p FROM ProgramacionMantenimientoModel p WHERE p.equipo.idEquipo = ?1 AND p.activa = true AND " +
            "p.fechaProximoMantenimiento BETWEEN ?2 AND ?3 " +
            "ORDER BY p.fechaProximoMantenimiento ASC")
    List<ProgramacionMantenimientoModel> findProximosMantenimientosByEquipo(Integer equipoId, Date fechaActual,
            Date fechaLimite);

    /**
     * Actualiza la fecha del último mantenimiento y recalcula el próximo
     */
    @Query("UPDATE ProgramacionMantenimientoModel p SET p.fechaUltimoMantenimiento = ?2, " +
            "p.fechaProximoMantenimiento = ?3, p.fechaModificacion = ?4 " +
            "WHERE p.idProgramacion = ?1")
    int actualizarFechas(Integer idProgramacion, Date fechaUltimo, Date fechaProximo, Date fechaModificacion);
}
