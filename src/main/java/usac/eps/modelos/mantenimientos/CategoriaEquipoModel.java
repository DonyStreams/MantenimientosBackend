package usac.eps.modelos.mantenimientos;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.*;

@Entity
@Table(name = "Categoria_Equipo")
public class CategoriaEquipoModel implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Integer idCategoria;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "id_padre")
    private Integer idPadre;

    @Column(name = "estado")
    private Boolean estado;

    @Column(name = "fecha_creacion", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;

    @Column(name = "fecha_modificacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaModificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_creacion", referencedColumnName = "id")
    private UsuarioMantenimientoModel usuarioCreacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_padre", referencedColumnName = "id_categoria", insertable = false, updatable = false)
    private CategoriaEquipoModel categoriaPadre;

    @OneToMany(mappedBy = "categoriaPadre", fetch = FetchType.LAZY)
    @JsonbTransient
    private List<CategoriaEquipoModel> subcategorias;

    @OneToMany(mappedBy = "categoria", fetch = FetchType.LAZY)
    @JsonbTransient
    private List<EquipoModel> equipos;

    public Integer getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Integer idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getIdPadre() {
        return idPadre;
    }

    public void setIdPadre(Integer idPadre) {
        this.idPadre = idPadre;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
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

    public UsuarioMantenimientoModel getUsuarioCreacion() {
        return usuarioCreacion;
    }

    public void setUsuarioCreacion(UsuarioMantenimientoModel usuarioCreacion) {
        this.usuarioCreacion = usuarioCreacion;
    }

    public CategoriaEquipoModel getCategoriaPadre() {
        return categoriaPadre;
    }

    public void setCategoriaPadre(CategoriaEquipoModel categoriaPadre) {
        this.categoriaPadre = categoriaPadre;
        this.idPadre = categoriaPadre != null ? categoriaPadre.getIdCategoria() : null;
    }

    public List<CategoriaEquipoModel> getSubcategorias() {
        return subcategorias;
    }

    public void setSubcategorias(List<CategoriaEquipoModel> subcategorias) {
        this.subcategorias = subcategorias;
    }

    public List<EquipoModel> getEquipos() {
        return equipos;
    }

    public void setEquipos(List<EquipoModel> equipos) {
        this.equipos = equipos;
    }

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = new Date();
        if (this.estado == null) {
            this.estado = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.fechaModificacion = new Date();
    }
}
