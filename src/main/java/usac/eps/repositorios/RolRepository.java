package usac.eps.repositorios;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;
import usac.eps.modelos.RolModel;


@Repository
public interface RolRepository extends EntityRepository <RolModel, Long> {

    RolModel findByIdRol(int id);   
     
}
