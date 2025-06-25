package usac.eps.repositorios.mantenimientos;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;
import usac.eps.modelos.mantenimientos.ComentarioTicketModel;

@Repository
public interface ComentarioTicketRepository extends EntityRepository<ComentarioTicketModel, Integer> {
    ComentarioTicketModel findById(Integer id);
}
