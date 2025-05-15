package usac.eps.repositorios;

import usac.eps.modelos.TipoProductoModel;
import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;


@Repository
public interface TipoProductoRepository extends EntityRepository <TipoProductoModel, Long> {

    TipoProductoModel findByIdTipoProducto(int id);   
     
}
