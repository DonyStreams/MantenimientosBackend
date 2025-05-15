package usac.eps.modelos;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Brandon Soto
 */
@Entity
@Table(name = "TipoRequisicion")
public class TipoRequisicionModel implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdTipoRequisicion")
    private int idTipoRequisicion;

    @Column(name = "Nombre", length = 255, unique = true, nullable = false)
    private String nombre;

     @Column(name = "Descripcion", length = 500)
    private String descripcion;

    @Column(name = "FechaModificacion",nullable = false)    
    private String fechaModificacion;

    public int getIdTipoRequisicion() {
        return idTipoRequisicion;
    }

    public void setIdTipoRequisicion(int idTipoRequisicion) {
        this.idTipoRequisicion = idTipoRequisicion;
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

    public String getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(String fechaCreacion) {
        this.fechaModificacion = fechaCreacion;
    }
    
    
    
}