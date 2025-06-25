package usac.eps.repositorios.mantenimientos;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;
import usac.eps.modelos.mantenimientos.TicketModel;

@Repository
public interface TicketRepository extends EntityRepository<TicketModel, Integer> {
    TicketModel findById(Integer id);
}
