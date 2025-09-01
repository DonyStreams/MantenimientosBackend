package usac.eps.repositorios.mantenimientos;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.Modifying;
import usac.eps.modelos.mantenimientos.AreaModel;
import java.util.List;

@Repository
public interface AreaRepository extends EntityRepository<AreaModel, Integer> {
    AreaModel findByIdArea(Integer idArea);

    List<AreaModel> findByEstado(Boolean estado);

    @Modifying
    @Query("DELETE FROM AreaModel a WHERE a.idArea = ?1")
    void deleteByIdArea(Integer idArea);
}
