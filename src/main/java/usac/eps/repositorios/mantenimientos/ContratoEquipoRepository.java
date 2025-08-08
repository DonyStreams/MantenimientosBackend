package usac.eps.repositorios.mantenimientos;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.Repository;
import usac.eps.modelos.mantenimientos.ContratoEquipoModel;
import usac.eps.modelos.mantenimientos.ContratoEquipoId;
import usac.eps.modelos.mantenimientos.EquipoModel;

import java.util.List;

@Repository
public interface ContratoEquipoRepository extends EntityRepository<ContratoEquipoModel, ContratoEquipoId> {

    @Query("SELECT ce.equipo FROM ContratoEquipoModel ce WHERE ce.contrato.idContrato = ?1")
    List<EquipoModel> findEquiposByContratoId(Integer contratoId);
}
