package usac.eps.modelos.mantenimientos;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    private ContratoModel contrato;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idEquipo")
    @JoinColumn(name = "id_equipo", referencedColumnName = "id_equipo")
    private EquipoModel equipo;

    // Getters y setters
    public ContratoEquipoId getId() {
        return id;
    }

    public void setId(ContratoEquipoId id) {
        this.id = id;
    }

    @JsonIgnore
    public ContratoModel getContrato() {
        return contrato;
    }

    public void setContrato(ContratoModel contrato) {
        this.contrato = contrato;
    }

    public EquipoModel getEquipo() {
        return equipo;
    }

    public void setEquipo(EquipoModel equipo) {
        this.equipo = equipo;
    }
}
