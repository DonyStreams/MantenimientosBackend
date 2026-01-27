package usac.eps.repositorios.mantenimientos;

import java.util.List;
import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.Repository;
import usac.eps.modelos.mantenimientos.ComentarioEjecucionModel;

@Repository
public interface ComentarioEjecucionRepository extends EntityRepository<ComentarioEjecucionModel, Integer> {

    ComentarioEjecucionModel findById(Integer id);

    @Query("SELECT c FROM ComentarioEjecucionModel c WHERE c.ejecucion.idEjecucion = ?1 ORDER BY c.fechaCreacion DESC")
    List<ComentarioEjecucionModel> findByIdEjecucion(Integer idEjecucion);
}
