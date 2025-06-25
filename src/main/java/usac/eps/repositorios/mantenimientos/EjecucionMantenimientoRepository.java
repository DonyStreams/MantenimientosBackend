package usac.eps.repositorios.mantenimientos;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;
import usac.eps.modelos.mantenimientos.EjecucionMantenimientoModel;

@Repository
public interface EjecucionMantenimientoRepository extends EntityRepository<EjecucionMantenimientoModel, Integer> {
    EjecucionMantenimientoModel findByIdEjecucion(Integer idEjecucion);
}
