package usac.eps.repositorios.mantenimientos;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;
import usac.eps.modelos.mantenimientos.ContratoEquipoModel;
import usac.eps.modelos.mantenimientos.ContratoEquipoId;

@Repository
public interface ContratoEquipoRepository extends EntityRepository<ContratoEquipoModel, ContratoEquipoId> {
}
