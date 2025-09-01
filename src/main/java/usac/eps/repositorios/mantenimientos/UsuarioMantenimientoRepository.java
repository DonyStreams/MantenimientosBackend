package usac.eps.repositorios.mantenimientos;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;
import usac.eps.modelos.mantenimientos.UsuarioMantenimientoModel;

import java.util.List;

@Repository
public interface UsuarioMantenimientoRepository extends EntityRepository<UsuarioMantenimientoModel, Integer> {
    UsuarioMantenimientoModel findById(Integer id);

    UsuarioMantenimientoModel findByKeycloakId(String keycloakId);

    UsuarioMantenimientoModel findByCorreo(String correo);

    List<UsuarioMantenimientoModel> findByActivo(Boolean activo);
}
