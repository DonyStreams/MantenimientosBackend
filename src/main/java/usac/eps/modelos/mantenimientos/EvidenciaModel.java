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

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "fecha_creacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;

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
}
