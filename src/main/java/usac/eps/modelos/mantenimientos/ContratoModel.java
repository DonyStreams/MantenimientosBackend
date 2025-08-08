package usac.eps.modelos.mantenimientos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.*;

@Entity
@Table(name = "Contratos")
public class ContratoModel implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contrato")
    private Integer idContrato;

    @Column(name = "fecha_inicio")
    @Temporal(TemporalType.DATE)
    private Date fechaInicio;

    @Column(name = "fecha_fin")
    @Temporal(TemporalType.DATE)
    private Date fechaFin;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "frecuencia", length = 20)
    private String frecuencia;

    @Column(name = "estado")
    private Boolean estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proveedor", referencedColumnName = "id_proveedor")
    @JsonIgnore
    private ProveedorModel proveedor;

    @Column(name = "fecha_creacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;

    @Column(name = "fecha_modificacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaModificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_creacion", referencedColumnName = "id")
    @JsonIgnore
    private UsuarioMantenimientoModel usuarioCreacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_modificacion", referencedColumnName = "id")
    @JsonIgnore
    private UsuarioMantenimientoModel usuarioModificacion;

    @OneToMany(mappedBy = "contrato", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ContratoEquipoModel> equipos;

    @OneToMany(mappedBy = "contrato", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ContratoTipoMantenimientoModel> tiposMantenimiento;

    // Getters y setters
    public Integer getIdContrato() {
        return idContrato;
    }

    public void setIdContrato(Integer idContrato) {
        this.idContrato = idContrato;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Date getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(Date fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFrecuencia() {
        return frecuencia;
    }

    public void setFrecuencia(String frecuencia) {
        this.frecuencia = frecuencia;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }

    @JsonIgnore
    public ProveedorModel getProveedor() {
        return proveedor;
    }

    public void setProveedor(ProveedorModel proveedor) {
        this.proveedor = proveedor;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Date getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(Date fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    @JsonIgnore
    public UsuarioMantenimientoModel getUsuarioCreacion() {
        return usuarioCreacion;
    }

    public void setUsuarioCreacion(UsuarioMantenimientoModel usuarioCreacion) {
        this.usuarioCreacion = usuarioCreacion;
    }

    @JsonIgnore
    public UsuarioMantenimientoModel getUsuarioModificacion() {
        return usuarioModificacion;
    }

    public void setUsuarioModificacion(UsuarioMantenimientoModel usuarioModificacion) {
        this.usuarioModificacion = usuarioModificacion;
    }

    @JsonIgnore
    public List<ContratoEquipoModel> getEquipos() {
        return equipos;
    }

    public void setEquipos(List<ContratoEquipoModel> equipos) {
        this.equipos = equipos;
    }

    @JsonIgnore
    public List<ContratoTipoMantenimientoModel> getTiposMantenimiento() {
        return tiposMantenimiento;
    }

    public void setTiposMantenimiento(List<ContratoTipoMantenimientoModel> tiposMantenimiento) {
        this.tiposMantenimiento = tiposMantenimiento;
    }
}
