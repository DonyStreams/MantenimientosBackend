package usac.eps.repositorios;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;
import usac.eps.modelos.RequisicionBitacoraModel;
import usac.eps.modelos.RequisicionModel;
import usac.eps.modelos.UsuarioModel;

import java.util.List;
import usac.eps.modelos.RequisicionDetalleModel;

@Repository
public interface RequisicionBitacoraRepository extends EntityRepository<RequisicionBitacoraModel, Long> {

    RequisicionBitacoraModel findByIdRequisicionBitacora(Long id);
    
    List<RequisicionBitacoraModel> findByRequisicion(RequisicionModel requisicion);

    List<RequisicionBitacoraModel> findByUsuario(UsuarioModel usuario);
}
