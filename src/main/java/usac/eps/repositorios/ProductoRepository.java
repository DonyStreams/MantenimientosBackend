package usac.eps.repositorios;

import java.util.List;
import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;
import usac.eps.modelos.PresentacionProductoModel;
import usac.eps.modelos.ProductoModel;
import usac.eps.modelos.TipoProductoModel;
import usac.eps.modelos.UnidadMedidaModel;


@Repository
public interface ProductoRepository extends EntityRepository <ProductoModel, Long> {

    ProductoModel findByIdProducto(int id);
    
    // Método para obtener todas los productos asociados a un tipo específico
    List<ProductoModel> findByTipoProductoModel(TipoProductoModel tipoProducto);
    
    // Método para obtener todas los productos asociados a una unidad de Medida
    List<ProductoModel> findByUnidadMedidaModel(UnidadMedidaModel unidadMedida);
    
    // Método para obtener todas los productos asociados a un tipo de presentacion del producto
    List<ProductoModel> findByPresentacionProductoModel(PresentacionProductoModel presentacionProducto);
     
}
