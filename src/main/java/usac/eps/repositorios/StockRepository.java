package usac.eps.repositorios;

import java.util.List;
import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;
import usac.eps.modelos.ProductoModel;
import usac.eps.modelos.SedeModel;
import usac.eps.modelos.StockModel;

@Repository
public interface StockRepository extends EntityRepository<StockModel, Long> {

    StockModel findByIdStock(Long id);

    // Método para obtener todos los registros de Stock asociados a un producto específico
    List<StockModel> findByProductoModel(ProductoModel productoModel);

    // Método para obtener todos los registros de Stock asociados a una sede específica
    List<StockModel> findBySedeModel(SedeModel sedeModel);
}