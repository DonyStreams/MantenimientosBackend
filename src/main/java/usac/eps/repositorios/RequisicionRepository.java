package usac.eps.repositorios;

import java.util.List;
import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;
import usac.eps.modelos.RequisicionModel;
import usac.eps.modelos.TipoRequisicionModel;
import usac.eps.modelos.UnidadModel;
import usac.eps.modelos.UsuarioModel;

@Repository
public interface RequisicionRepository extends EntityRepository<RequisicionModel, Long> {

    RequisicionModel findByIdRequisicion(Long id);

    // Método para obtener todas las requisiciones asociadas a un TipoRequisicion
    List<RequisicionModel> findByTipoRequisicion(TipoRequisicionModel tipoRequisicion);

    // Método para obtener todas las requisiciones asociadas a una Unidad
    List<RequisicionModel> findByUnidad(UnidadModel unidad);

    // Método para obtener todas las requisiciones asociadas a un Usuario
    List<RequisicionModel> findByUsuario(UsuarioModel usuario);
}
