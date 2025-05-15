package usac.eps.repositorios;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;
import usac.eps.modelos.UnidadModel;


@Repository
public interface UnidadRepository extends EntityRepository <UnidadModel, Long> {

    UnidadModel findByIdUnidad(int id);   
     
}
