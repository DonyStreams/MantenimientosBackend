package usac.eps.repositorios;

import usac.eps.modelos.PresentacionProductoModel;
import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;


@Repository
public interface PresentacionProductoRepository extends EntityRepository <PresentacionProductoModel, Long> {

    PresentacionProductoModel findByIdPresentacionProducto(int id);   
     
}
