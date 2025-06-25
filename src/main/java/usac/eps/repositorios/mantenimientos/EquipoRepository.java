package usac.eps.repositorios.mantenimientos;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;
import usac.eps.modelos.mantenimientos.EquipoModel;

@Repository
public interface EquipoRepository extends EntityRepository<EquipoModel, Integer> {
    EquipoModel findByIdEquipo(Integer idEquipo);
}
