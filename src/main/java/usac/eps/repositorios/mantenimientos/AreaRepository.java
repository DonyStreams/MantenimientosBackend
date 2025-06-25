package usac.eps.repositorios.mantenimientos;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;
import usac.eps.modelos.mantenimientos.AreaModel;

@Repository
public interface AreaRepository extends EntityRepository<AreaModel, Integer> {
    AreaModel findByIdArea(Integer idArea);
}
