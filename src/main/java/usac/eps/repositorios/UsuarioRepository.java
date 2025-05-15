package usac.eps.repositorios;

import java.util.List;
import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;
import usac.eps.modelos.RolModel;
import usac.eps.modelos.SedeModel;
import usac.eps.modelos.UsuarioModel;


@Repository
public interface UsuarioRepository extends EntityRepository <UsuarioModel, Long> {

    UsuarioModel findByIdUsuario(Long id);
    
    // Método para obtener todos los usuarios asociados a un Rol
    List<RolModel> findByRolModel(RolModel rol);
    
    // Método para obtener todos los usuarios asociados a una Sede
    List<SedeModel> findBySedeModel(SedeModel sede);
     
}
