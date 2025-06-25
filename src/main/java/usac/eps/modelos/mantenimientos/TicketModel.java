package usac.eps.modelos.mantenimientos;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.*;

@Entity
@Table(name = "Tickets")
public class TicketModel implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipo_id", referencedColumnName = "id_equipo")
    private EquipoModel equipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_creador_id", referencedColumnName = "id")
    private UsuarioMantenimientoModel usuarioCreador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_asignado_id", referencedColumnName = "id")
    private UsuarioMantenimientoModel usuarioAsignado;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "prioridad", length = 20)
    private String prioridad;

    @Column(name = "estado", length = 20)
    private String estado;

    @Column(name = "fecha_creacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;

    @Column(name = "fecha_modificacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaModificacion;

    @Column(name = "fecha_cierre")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCierre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_creacion", referencedColumnName = "id")
    private UsuarioMantenimientoModel usuarioCreacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_modificacion", referencedColumnName = "id")
    private UsuarioMantenimientoModel usuarioModificacion;

    @OneToMany(mappedBy = "ticket", fetch = FetchType.LAZY)
    private List<ComentarioTicketModel> comentarios;

    // Getters y setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public EquipoModel getEquipo() { return equipo; }
    public void setEquipo(EquipoModel equipo) { this.equipo = equipo; }
    public UsuarioMantenimientoModel getUsuarioCreador() { return usuarioCreador; }
    public void setUsuarioCreador(UsuarioMantenimientoModel usuarioCreador) { this.usuarioCreador = usuarioCreador; }
    public UsuarioMantenimientoModel getUsuarioAsignado() { return usuarioAsignado; }
    public void setUsuarioAsignado(UsuarioMantenimientoModel usuarioAsignado) { this.usuarioAsignado = usuarioAsignado; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getPrioridad() { return prioridad; }
    public void setPrioridad(String prioridad) { this.prioridad = prioridad; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public Date getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(Date fechaModificacion) { this.fechaModificacion = fechaModificacion; }
    public Date getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(Date fechaCierre) { this.fechaCierre = fechaCierre; }
    public UsuarioMantenimientoModel getUsuarioCreacion() { return usuarioCreacion; }
    public void setUsuarioCreacion(UsuarioMantenimientoModel usuarioCreacion) { this.usuarioCreacion = usuarioCreacion; }
    public UsuarioMantenimientoModel getUsuarioModificacion() { return usuarioModificacion; }
    public void setUsuarioModificacion(UsuarioMantenimientoModel usuarioModificacion) { this.usuarioModificacion = usuarioModificacion; }
    public List<ComentarioTicketModel> getComentarios() { return comentarios; }
    public void setComentarios(List<ComentarioTicketModel> comentarios) { this.comentarios = comentarios; }
}
