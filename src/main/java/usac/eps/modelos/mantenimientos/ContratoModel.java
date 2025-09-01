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

    @Column(name = "id_estado")
    private Integer idEstado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proveedor", referencedColumnName = "id_proveedor")
    @JsonIgnore
    private ProveedorModel proveedor;

    // Campo transiente para recibir el ID del proveedor desde JSON
    @Transient
    private Integer idProveedor;

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

    public Integer getIdEstado() {
        return idEstado;
    }

    public void setIdEstado(Integer idEstado) {
        this.idEstado = idEstado;
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

    // 🔧 MÉTODOS DE NEGOCIO AÑADIDOS

    /**
     * Verifica si el contrato está vigente
     */
    public boolean isVigente() {
        if (estado == null || !estado)
            return false;

        Date hoy = new Date();
        boolean inicioValido = fechaInicio == null || fechaInicio.before(hoy) || fechaInicio.equals(hoy);
        boolean finValido = fechaFin == null || fechaFin.after(hoy) || fechaFin.equals(hoy);

        return inicioValido && finValido;
    }

    /**
     * Verifica si el contrato está próximo a vencer (30 días)
     */
    public boolean isProximoAVencer() {
        if (estado == null || !estado || fechaFin == null)
            return false;

        Date hoy = new Date();
        long diasRestantes = (fechaFin.getTime() - hoy.getTime()) / (1000 * 60 * 60 * 24);

        return diasRestantes <= 30 && diasRestantes >= 0;
    }

    /**
     * Obtiene el estado descriptivo del contrato
     */
    public String getEstadoDescriptivo() {
        if (estado == null || !estado)
            return "Inactivo";

        Date hoy = new Date();

        if (fechaFin != null && fechaFin.before(hoy))
            return "Vencido";
        if (fechaInicio != null && fechaInicio.after(hoy))
            return "Pendiente";
        if (isVigente())
            return "Vigente";

        return "Desconocido";
    }

    /**
     * Obtiene el nombre del proveedor de forma segura
     */
    public String getNombreProveedor() {
        return proveedor != null ? proveedor.getNombre() : "Sin proveedor";
    }

    /**
     * Obtiene el ID del proveedor de forma segura
     */
    public Integer getIdProveedor() {
        // Priorizar el campo transiente si está seteado, sino usar la relación
        if (idProveedor != null) {
            return idProveedor;
        }
        return proveedor != null ? proveedor.getIdProveedor() : null;
    }

    /**
     * Establece el proveedor por ID (usado para deserialización JSON)
     */
    public void setIdProveedor(Integer idProveedor) {
        this.idProveedor = idProveedor;
    }

    /**
     * Obtiene el nombre del usuario creador de forma segura
     */
    public String getNombreUsuarioCreacion() {
        return usuarioCreacion != null ? usuarioCreacion.getNombreCompleto() : "Sistema";
    }

    @Override
    public String toString() {
        return "ContratoModel{" +
                "idContrato=" + idContrato +
                ", fechaInicio=" + fechaInicio +
                ", fechaFin=" + fechaFin +
                ", frecuencia='" + frecuencia + '\'' +
                ", estado=" + estado +
                ", proveedor=" + getNombreProveedor() +
                ", estadoDescriptivo='" + getEstadoDescriptivo() + '\'' +
                '}';
    }
}
