package usac.eps.repositorios.mantenimientos;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;
import usac.eps.modelos.mantenimientos.ContratoModel;
import java.util.List;

@Repository
public interface ContratoRepository extends EntityRepository<ContratoModel, Integer> {
    ContratoModel findByIdContrato(Integer idContrato);

    List<ContratoModel> findByEstado(Boolean estado);
}
