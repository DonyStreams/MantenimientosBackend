package usac.eps.modelos.mantenimientos;

import java.io.Serializable;
import javax.persistence.*;

@Entity
@Table(name = "Contrato_Tipo_Mantenimiento")
public class ContratoTipoMantenimientoModel implements Serializable {
    @EmbeddedId
    private ContratoTipoMantenimientoId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idContrato")
    @JoinColumn(name = "id_contrato", referencedColumnName = "id_contrato")
    private ContratoModel contrato;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idTipo")
    @JoinColumn(name = "id_tipo", referencedColumnName = "id_tipo")
    private TipoMantenimientoModel tipoMantenimiento;

    // Getters y setters
}
