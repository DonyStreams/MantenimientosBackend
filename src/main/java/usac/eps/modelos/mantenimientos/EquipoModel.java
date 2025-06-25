package usac.eps.modelos.mantenimientos;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.*;

@Entity
@Table(name = "Equipos")
public class EquipoModel implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_equipo")
    private Integer idEquipo;

    @Column(name = "numero_inventario", length = 50, unique = true)
    private String numeroInventario;

    @Column(name = "numero_serie", length = 50)
    private String numeroSerie;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "estado")
    private Boolean estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_area", referencedColumnName = "id_area")
    private AreaModel area;

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

    @OneToMany(mappedBy = "equipo", fetch = FetchType.LAZY)
    private List<HistorialEquipoModel> historialEquipos;

    // Getters y setters
    public Integer getIdEquipo() { return idEquipo; }
    public void setIdEquipo(Integer idEquipo) { this.idEquipo = idEquipo; }
    public String getNumeroInventario() { return numeroInventario; }
    public void setNumeroInventario(String numeroInventario) { this.numeroInventario = numeroInventario; }
    public String getNumeroSerie() { return numeroSerie; }
    public void setNumeroSerie(String numeroSerie) { this.numeroSerie = numeroSerie; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Boolean getEstado() { return estado; }
    public void setEstado(Boolean estado) { this.estado = estado; }
    public AreaModel getArea() { return area; }
    public void setArea(AreaModel area) { this.area = area; }
    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public Date getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(Date fechaModificacion) { this.fechaModificacion = fechaModificacion; }
    public UsuarioMantenimientoModel getUsuarioCreacion() { return usuarioCreacion; }
    public void setUsuarioCreacion(UsuarioMantenimientoModel usuarioCreacion) { this.usuarioCreacion = usuarioCreacion; }
    public UsuarioMantenimientoModel getUsuarioModificacion() { return usuarioModificacion; }
    public void setUsuarioModificacion(UsuarioMantenimientoModel usuarioModificacion) { this.usuarioModificacion = usuarioModificacion; }
    public List<HistorialEquipoModel> getHistorialEquipos() { return historialEquipos; }
    public void setHistorialEquipos(List<HistorialEquipoModel> historialEquipos) { this.historialEquipos = historialEquipos; }
}
