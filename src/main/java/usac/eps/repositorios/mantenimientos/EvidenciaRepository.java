package usac.eps.repositorios.mantenimientos;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.Repository;
import usac.eps.modelos.mantenimientos.EvidenciaModel;

import java.util.List;

@Repository
public interface EvidenciaRepository extends EntityRepository<EvidenciaModel, Integer> {
    EvidenciaModel findById(Integer id);

    @Query("SELECT e FROM EvidenciaModel e WHERE e.entidadRelacionada = ?1 AND e.entidadId = ?2 ORDER BY e.fechaCreacion DESC")
    List<EvidenciaModel> findByEntidadRelacionadaAndEntidadId(String entidadRelacionada, Integer entidadId);

    List<EvidenciaModel> findByEntidadRelacionadaOrderByFechaCreacionDesc(String entidadRelacionada);
}
