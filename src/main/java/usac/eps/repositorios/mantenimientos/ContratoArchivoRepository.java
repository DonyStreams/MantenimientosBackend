package usac.eps.repositorios.mantenimientos;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.Repository;
import usac.eps.modelos.mantenimientos.ContratoArchivoModel;

import java.util.List;

/**
 * Repositorio para gestión de archivos adjuntos de contratos
 */
@Repository
public interface ContratoArchivoRepository extends EntityRepository<ContratoArchivoModel, Integer> {

    // Consultas básicas
    List<ContratoArchivoModel> findByContratoIdContratoAndActivoOrderByFechaSubidaDesc(Integer contratoId,
            Boolean activo);

    List<ContratoArchivoModel> findByTipoArchivoAndActivoOrderByFechaSubidaDesc(String tipoArchivo, Boolean activo);

    // Consultas para gestión de archivos
    @Query("SELECT ca FROM ContratoArchivoModel ca WHERE ca.contrato.idContrato = :contratoId AND ca.activo = true ORDER BY ca.fechaSubida DESC")
    List<ContratoArchivoModel> findArchivosByContrato(Integer contratoId);

    @Query("SELECT ca FROM ContratoArchivoModel ca WHERE ca.nombreArchivo = :nombreArchivo AND ca.activo = true")
    ContratoArchivoModel findByNombreArchivo(String nombreArchivo);

    // Estadísticas
    @Query("SELECT COUNT(ca) FROM ContratoArchivoModel ca WHERE ca.contrato.idContrato = :contratoId AND ca.activo = true")
    Long countArchivosByContrato(Integer contratoId);

    @Query("SELECT SUM(ca.tamano) FROM ContratoArchivoModel ca WHERE ca.contrato.idContrato = :contratoId AND ca.activo = true")
    Long sumTamanoByContrato(Integer contratoId);
}
