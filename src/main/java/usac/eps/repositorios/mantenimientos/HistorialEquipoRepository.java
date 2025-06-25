package usac.eps.repositorios.mantenimientos;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;
import usac.eps.modelos.mantenimientos.HistorialEquipoModel;

@Repository
public interface HistorialEquipoRepository extends EntityRepository<HistorialEquipoModel, Integer> {
    HistorialEquipoModel findByIdHistorial(Integer idHistorial);
}
