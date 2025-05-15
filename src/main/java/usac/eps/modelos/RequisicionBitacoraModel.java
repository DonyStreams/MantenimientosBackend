package usac.eps.modelos;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "RequisicionBitacora")
public class RequisicionBitacoraModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdRequisicionBitacora")
    private Long idRequisicionBitacora;

    @ManyToOne
    @JoinColumn(name = "IdRequisicion", nullable = false, foreignKey = @ForeignKey(name = "fk_IdRequisicion"))
    private RequisicionModel requisicion;

    @ManyToOne
    @JoinColumn(name = "IdUsuario", nullable = false, foreignKey = @ForeignKey(name = "fk_IdUsuario"))
    private UsuarioModel usuario;

    @Column(name = "Accion", length = 50, nullable = false)
    private String accion;

    //@Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FechaAccion", nullable = false)
    private String fechaAccion;

    @Column(name = "Estado", length = 50, nullable = false)
    private String estado;

    // Getters y Setters

    public Long getIdRequisicionBitacora() {
        return idRequisicionBitacora;
    }

    public void setIdRequisicionBitacora(Long idRequisicionBitacora) {
        this.idRequisicionBitacora = idRequisicionBitacora;
    }

    public RequisicionModel getRequisicion() {
        return requisicion;
    }

    public void setRequisicion(RequisicionModel requisicion) {
        this.requisicion = requisicion;
    }

    public UsuarioModel getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioModel usuario) {
        this.usuario = usuario;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getFechaAccion() {
        return fechaAccion;
    }

    public void setFechaAccion(String fechaAccion) {
        this.fechaAccion = fechaAccion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
