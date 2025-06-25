package usac.eps.repositorios.mantenimientos;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;
import usac.eps.modelos.mantenimientos.ContratoTipoMantenimientoModel;
import usac.eps.modelos.mantenimientos.ContratoTipoMantenimientoId;

@Repository
public interface ContratoTipoMantenimientoRepository
        extends EntityRepository<ContratoTipoMantenimientoModel, ContratoTipoMantenimientoId> {
}
