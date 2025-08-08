package usac.eps.modelos.mantenimientos;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    private ContratoModel contrato;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idTipo")
    @JoinColumn(name = "id_tipo", referencedColumnName = "id_tipo")
    private TipoMantenimientoModel tipoMantenimiento;

    // Getters y setters
    public ContratoTipoMantenimientoId getId() {
        return id;
    }

    public void setId(ContratoTipoMantenimientoId id) {
        this.id = id;
    }

    @JsonIgnore
    public ContratoModel getContrato() {
        return contrato;
    }

    public void setContrato(ContratoModel contrato) {
        this.contrato = contrato;
    }

    public TipoMantenimientoModel getTipoMantenimiento() {
        return tipoMantenimiento;
    }

    public void setTipoMantenimiento(TipoMantenimientoModel tipoMantenimiento) {
        this.tipoMantenimiento = tipoMantenimiento;
    }
}
