package usac.eps.repositorios.mantenimientos;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;
import usac.eps.modelos.mantenimientos.UsuarioMantenimientoModel;

import java.util.UUID;

@Repository
public interface UsuarioMantenimientoRepository extends EntityRepository<UsuarioMantenimientoModel, Integer> {
    UsuarioMantenimientoModel findById(Integer id);

    UsuarioMantenimientoModel findByKeycloakId(UUID keycloakId);

    UsuarioMantenimientoModel findByCorreo(String correo);
}
