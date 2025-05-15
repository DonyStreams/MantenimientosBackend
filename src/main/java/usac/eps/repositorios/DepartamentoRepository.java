package usac.eps.repositorios;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;
import usac.eps.modelos.DepartamentoModel;


@Repository
public interface DepartamentoRepository extends EntityRepository <DepartamentoModel, Long> {

    DepartamentoModel findByIdDepartamento(int id);   
     
}
