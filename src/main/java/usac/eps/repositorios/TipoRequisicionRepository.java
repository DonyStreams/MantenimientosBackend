package usac.eps.repositorios;

import java.util.List;
import usac.eps.modelos.TipoRequisicionModel;
import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;


@Repository
public interface TipoRequisicionRepository extends EntityRepository <TipoRequisicionModel, Long> {

    TipoRequisicionModel findByIdTipoRequisicion(int id);   
     
}
