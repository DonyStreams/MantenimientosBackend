package usac.eps.repositorios;

import java.util.List;
import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;
import usac.eps.modelos.DepartamentoModel;
import usac.eps.modelos.SedeModel;


@Repository
public interface SedeRepository extends EntityRepository <SedeModel, Long> {

    SedeModel findByIdSede(int id);
    
    // Método para obtener todas las sedes asociadas a un departamento específico
    List<SedeModel> findByDepartamentoModel(DepartamentoModel departamento);
     
}
