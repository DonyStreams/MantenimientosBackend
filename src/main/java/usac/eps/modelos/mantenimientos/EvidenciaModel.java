package usac.eps.modelos.mantenimientos;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

@Entity
@Table(name = "Evidencias")
public class EvidenciaModel implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "entidad_relacionada", length = 50)
    private String entidadRelacionada;

    @Column(name = "entidad_id")
    private Integer entidadId;

    @Column(name = "archivo_url", length = 500)
    private String archivoUrl;

    @Column(name = "nombre_archivo", length = 255)
    private String nombreArchivo;

    @Column(name = "nombre_original", length = 255)
    private String nombreOriginal;

    @Column(name = "tipo_archivo", length = 100)
    private String tipoArchivo;

    @Column(name = "tamanio")
    private Long tamanio;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "fecha_creacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_creacion", referencedColumnName = "id")
    private UsuarioMantenimientoModel usuarioCreacion;

    // Getters y setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEntidadRelacionada() {
        return entidadRelacionada;
    }

    public void setEntidadRelacionada(String entidadRelacionada) {
        this.entidadRelacionada = entidadRelacionada;
    }

    public Integer getEntidadId() {
        return entidadId;
    }

    public void setEntidadId(Integer entidadId) {
        this.entidadId = entidadId;
    }

    public String getArchivoUrl() {
        return archivoUrl;
    }

    public void setArchivoUrl(String archivoUrl) {
        this.archivoUrl = archivoUrl;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public String getNombreOriginal() {
        return nombreOriginal;
    }

    public void setNombreOriginal(String nombreOriginal) {
        this.nombreOriginal = nombreOriginal;
    }

    public String getTipoArchivo() {
        return tipoArchivo;
    }

    public void setTipoArchivo(String tipoArchivo) {
        this.tipoArchivo = tipoArchivo;
    }

    public Long getTamanio() {
        return tamanio;
    }

    public void setTamanio(Long tamanio) {
        this.tamanio = tamanio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public UsuarioMantenimientoModel getUsuarioCreacion() {
        return usuarioCreacion;
    }

    public void setUsuarioCreacion(UsuarioMantenimientoModel usuarioCreacion) {
        this.usuarioCreacion = usuarioCreacion;
    }
}
