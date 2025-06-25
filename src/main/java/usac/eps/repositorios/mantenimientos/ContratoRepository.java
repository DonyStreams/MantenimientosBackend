package usac.eps.repositorios.mantenimientos;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;
import usac.eps.modelos.mantenimientos.ContratoModel;

@Repository
public interface ContratoRepository extends EntityRepository<ContratoModel, Integer> {
    ContratoModel findByIdContrato(Integer idContrato);
}
