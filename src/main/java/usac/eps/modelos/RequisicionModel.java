package usac.eps.modelos;

import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "Requisicion")
public class RequisicionModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdRequisicion")
    private Long idRequisicion;

    @ManyToOne
    @JoinColumn(name = "IdTipoRequisicion", nullable = false, foreignKey = @ForeignKey(name = "fk_IdTipoRequisicion"))
    private TipoRequisicionModel tipoRequisicion;

    @ManyToOne
    @JoinColumn(name = "IdUnidad", nullable = false, foreignKey = @ForeignKey(name = "fk_IdUnidad"))
    private UnidadModel unidad;

    @ManyToOne
    @JoinColumn(name = "IdUsuario", nullable = false, foreignKey = @ForeignKey(name = "fk_IdUsuarioReq"))
    private UsuarioModel usuario;

    @OneToMany(mappedBy = "requisicion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RequisicionDetalleModel> detallesRequisicion;
    
    //@Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FechaRequisicion", nullable = false)
    private String fechaRequisicion;

    @Column(name = "Descripcion", length = 500)
    private String descripcion;

    @Column(name = "EstadoActual", length = 50, nullable = false)
    private String estadoActual;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FechaModificacion", nullable = false)
    private Date fechaModificacion;

    public Long getIdRequisicion() {
        return idRequisicion;
    }

    public void setIdRequisicion(Long idRequisicion) {
        this.idRequisicion = idRequisicion;
    }

    public TipoRequisicionModel getTipoRequisicion() {
        return tipoRequisicion;
    }

    public void setTipoRequisicion(TipoRequisicionModel tipoRequisicion) {
        this.tipoRequisicion = tipoRequisicion;
    }

    public UnidadModel getUnidad() {
        return unidad;
    }

    public void setUnidad(UnidadModel unidad) {
        this.unidad = unidad;
    }

    public UsuarioModel getUsuario() {
        return usuario;
    }
    
    public List<RequisicionDetalleModel> getDetallesRequisicion() {
        return detallesRequisicion;
    }

    public void setDetallesRequisicion(List<RequisicionDetalleModel> detallesRequisicion) {
        this.detallesRequisicion = detallesRequisicion;
    }

    public void setUsuario(UsuarioModel usuario) {
        this.usuario = usuario;
    }

    public String getFechaRequisicion() {
        return fechaRequisicion;
    }

    public void setFechaRequisicion(String fechaRequisicion) {
        this.fechaRequisicion = fechaRequisicion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstadoActual() {
        return estadoActual;
    }

    public void setEstadoActual(String estadoActual) {
        this.estadoActual = estadoActual;
    }

    public Date getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(Date fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }
}
