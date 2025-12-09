package usac.eps.repositorios.mantenimientos;

import java.util.List;
import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.Repository;
import usac.eps.modelos.mantenimientos.CategoriaEquipoModel;

@Repository
public interface CategoriaEquipoRepository extends EntityRepository<CategoriaEquipoModel, Integer> {
    CategoriaEquipoModel findByIdCategoria(Integer idCategoria);

    @Query("SELECT c FROM CategoriaEquipoModel c ORDER BY c.nombre ASC")
    List<CategoriaEquipoModel> findAllOrdenadas();

    @Query("SELECT c FROM CategoriaEquipoModel c WHERE c.estado = ?1 ORDER BY c.nombre ASC")
    List<CategoriaEquipoModel> findByEstadoOrdenadas(Boolean estado);

    @Query("SELECT c FROM CategoriaEquipoModel c WHERE LOWER(c.nombre) = LOWER(?1) AND " +
            "((?2 IS NULL AND c.idPadre IS NULL) OR (c.idPadre = ?2))")
    List<CategoriaEquipoModel> findByNombreAndPadre(String nombre, Integer idPadre);

    @Query("SELECT COUNT(c) FROM CategoriaEquipoModel c WHERE c.idPadre = ?1")
    Long countByPadre(Integer idPadre);
}
