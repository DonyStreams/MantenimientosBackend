package usac.eps.modelos.mantenimientos;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ContratoEquipoId implements Serializable {
    @Column(name = "id_contrato")
    private Integer idContrato;
    @Column(name = "id_equipo")
    private Integer idEquipo;

    // equals y hashCode
    // Getters y setters
    public Integer getIdContrato() {
        return idContrato;
    }

    public void setIdContrato(Integer idContrato) {
        this.idContrato = idContrato;
    }

    public Integer getIdEquipo() {
        return idEquipo;
    }

    public void setIdEquipo(Integer idEquipo) {
        this.idEquipo = idEquipo;
    }
}
