package usac.eps.repositorios;

import usac.eps.modelos.UnidadMedidaModel;
import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;


@Repository
public interface UnidadMedidaRepository extends EntityRepository <UnidadMedidaModel, Long> {

    UnidadMedidaModel findByIdUnidadMedidaProducto(int id);   
     
}
