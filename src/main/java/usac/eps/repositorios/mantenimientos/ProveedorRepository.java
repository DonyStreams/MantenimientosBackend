package usac.eps.repositorios.mantenimientos;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;
import usac.eps.modelos.mantenimientos.ProveedorModel;

@Repository
public interface ProveedorRepository extends EntityRepository<ProveedorModel, Integer> {
    ProveedorModel findByIdProveedor(Integer idProveedor);
}
