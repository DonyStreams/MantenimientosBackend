package usac.eps.repositorios.mantenimientos;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;
import usac.eps.modelos.mantenimientos.TipoMantenimientoModel;

@Repository
public interface TipoMantenimientoRepository extends EntityRepository<TipoMantenimientoModel, Integer> {
    TipoMantenimientoModel findByIdTipo(Integer idTipo);
}
