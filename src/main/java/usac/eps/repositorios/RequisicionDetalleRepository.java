package usac.eps.repositorios;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;
import usac.eps.modelos.RequisicionDetalleModel;
import usac.eps.modelos.RequisicionModel;
import usac.eps.modelos.ProductoModel;

import java.util.List;

@Repository
public interface RequisicionDetalleRepository extends EntityRepository<RequisicionDetalleModel, Long> {

    RequisicionDetalleModel findByIdRequisicionDetalle(Long id);
    
    //Busca todos los detalles por requisicion
    List<RequisicionDetalleModel> findByRequisicion(RequisicionModel requisicion);

    //Busca todos los detalles en los que esta incluido un producto
    List<RequisicionDetalleModel> findByProducto(ProductoModel producto);

    
}
