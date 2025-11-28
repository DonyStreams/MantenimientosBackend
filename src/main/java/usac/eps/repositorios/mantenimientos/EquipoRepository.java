package usac.eps.repositorios.mantenimientos;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.Query;
import usac.eps.modelos.mantenimientos.EquipoModel;
import java.util.Optional;

@Repository
public interface EquipoRepository extends EntityRepository<EquipoModel, Integer> {
    EquipoModel findByIdEquipo(Integer idEquipo);

    @Query("SELECT e FROM EquipoModel e WHERE e.codigoInacif = ?1")
    Optional<EquipoModel> findOptionalByCodigoInacif(String codigoInacif);
}
