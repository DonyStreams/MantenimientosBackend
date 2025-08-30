package usac.eps.repositorios.mantenimientos;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.Modifying;
import usac.eps.modelos.mantenimientos.TipoMantenimientoModel;
import java.util.List;

@Repository
public interface TipoMantenimientoRepository extends EntityRepository<TipoMantenimientoModel, Integer> {
    TipoMantenimientoModel findByIdTipo(Integer idTipo);

    @Query("SELECT t FROM TipoMantenimientoModel t WHERE t.estado = true ORDER BY t.nombre ASC")
    List<TipoMantenimientoModel> findActivos();

    @Modifying
    @Query("DELETE FROM TipoMantenimientoModel t WHERE t.idTipo = ?1")
    void deleteByIdTipo(Integer idTipo);
}
