package usac.eps.repositorios;


import java.util.List;
import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;
import usac.eps.modelos.StockBitacoraModel;
import usac.eps.modelos.StockModel;
import usac.eps.modelos.UsuarioModel;

@Repository
public interface StockBitacoraRepository extends EntityRepository<StockBitacoraModel, Long> {

    StockBitacoraModel findByIdStockBitacora(Long id);

    // Método para obtener todas las entradas de StockBitacora relacionadas con un Stock específico.
    List<StockBitacoraModel> findByStockModel(StockModel stock);

    // Método para obtener todas las entradas de StockBitacoraModel relacionadas con un UsuarioModel específico.
    List<StockBitacoraModel> findByUsuarioModel(UsuarioModel usuario);    
}
