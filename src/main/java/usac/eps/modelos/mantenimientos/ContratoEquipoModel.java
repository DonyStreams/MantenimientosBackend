package usac.eps.modelos.mantenimientos;

import java.io.Serializable;
import javax.persistence.*;

@Entity
@Table(name = "Contrato_Equipo")
public class ContratoEquipoModel implements Serializable {
    @EmbeddedId
    private ContratoEquipoId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idContrato")
    @JoinColumn(name = "id_contrato", referencedColumnName = "id_contrato")
    private ContratoModel contrato;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idEquipo")
    @JoinColumn(name = "id_equipo", referencedColumnName = "id_equipo")
    private EquipoModel equipo;

    // Getters y setters
}
