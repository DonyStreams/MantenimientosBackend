package usac.eps.modelos.mantenimientos;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ContratoTipoMantenimientoId implements Serializable {
    @Column(name = "id_contrato")
    private Integer idContrato;
    @Column(name = "id_tipo")
    private Integer idTipo;

    // equals y hashCode
    // Getters y setters
    public Integer getIdContrato() {
        return idContrato;
    }

    public void setIdContrato(Integer idContrato) {
        this.idContrato = idContrato;
    }

    public Integer getIdTipo() {
        return idTipo;
    }

    public void setIdTipo(Integer idTipo) {
        this.idTipo = idTipo;
    }
}
