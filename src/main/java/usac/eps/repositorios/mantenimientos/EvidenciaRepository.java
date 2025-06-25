package usac.eps.repositorios.mantenimientos;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;
import usac.eps.modelos.mantenimientos.EvidenciaModel;

@Repository
public interface EvidenciaRepository extends EntityRepository<EvidenciaModel, Integer> {
    EvidenciaModel findById(Integer id);
}
