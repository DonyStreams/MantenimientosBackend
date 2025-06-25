package usac.eps.modelos.mantenimientos;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

@Entity
@Table(name = "Ejecuciones_Mantenimiento")
public class EjecucionMantenimientoModel implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ejecucion")
    private Integer idEjecucion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_contrato", referencedColumnName = "id_contrato")
    private ContratoModel contrato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_equipo", referencedColumnName = "id_equipo")
    private EquipoModel equipo;

    @Column(name = "fecha_ejecucion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaEjecucion;

    @Column(name = "bitacora")
    private String bitacora;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_responsable", referencedColumnName = "id")
    private UsuarioMantenimientoModel usuarioResponsable;

    @Column(name = "fecha_creacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;

    @Column(name = "fecha_modificacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaModificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_creacion", referencedColumnName = "id")
    private UsuarioMantenimientoModel usuarioCreacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_modificacion", referencedColumnName = "id")
    private UsuarioMantenimientoModel usuarioModificacion;

    // Getters y setters
    public Integer getIdEjecucion() { return idEjecucion; }
    public void setIdEjecucion(Integer idEjecucion) { this.idEjecucion = idEjecucion; }
    public ContratoModel getContrato() { return contrato; }
    public void setContrato(ContratoModel contrato) { this.contrato = contrato; }
    public EquipoModel getEquipo() { return equipo; }
    public void setEquipo(EquipoModel equipo) { this.equipo = equipo; }
    public Date getFechaEjecucion() { return fechaEjecucion; }
    public void setFechaEjecucion(Date fechaEjecucion) { this.fechaEjecucion = fechaEjecucion; }
    public String getBitacora() { return bitacora; }
    public void setBitacora(String bitacora) { this.bitacora = bitacora; }
    public UsuarioMantenimientoModel getUsuarioResponsable() { return usuarioResponsable; }
    public void setUsuarioResponsable(UsuarioMantenimientoModel usuarioResponsable) { this.usuarioResponsable = usuarioResponsable; }
    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public Date getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(Date fechaModificacion) { this.fechaModificacion = fechaModificacion; }
    public UsuarioMantenimientoModel getUsuarioCreacion() { return usuarioCreacion; }
    public void setUsuarioCreacion(UsuarioMantenimientoModel usuarioCreacion) { this.usuarioCreacion = usuarioCreacion; }
    public UsuarioMantenimientoModel getUsuarioModificacion() { return usuarioModificacion; }
    public void setUsuarioModificacion(UsuarioMantenimientoModel usuarioModificacion) { this.usuarioModificacion = usuarioModificacion; }
}
