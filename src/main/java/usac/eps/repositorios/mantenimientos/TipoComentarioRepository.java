package usac.eps.repositorios.mantenimientos;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;
import usac.eps.modelos.mantenimientos.TipoComentarioModel;

@Repository
public interface TipoComentarioRepository extends EntityRepository<TipoComentarioModel, Integer> {
    TipoComentarioModel findByIdTipo(Integer idTipo);
}
