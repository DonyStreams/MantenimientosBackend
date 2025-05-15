package usac.eps.repositorios;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;
import usac.eps.modelos.RegistroErroresModel;


@Repository
public interface ErroresRepository extends EntityRepository <RegistroErroresModel, Long> {  
    
}
