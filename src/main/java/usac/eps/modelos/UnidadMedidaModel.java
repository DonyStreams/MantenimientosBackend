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
@Table(name = "UnidadMedidaProducto")
public class UnidadMedidaModel implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdUnidadMedidaProducto")
    private int idUnidadMedidaProducto;

    @Column(name = "Nombre", length = 255, unique = true, nullable = false)
    private String nombre;

     @Column(name = "Descripcion", length = 500)
    private String descripcion;

    @Column(name = "FechaModificacion",nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaModificacion;

    public int getIdUnidadMedida() {
        return idUnidadMedidaProducto;
    }

    public void setIdUnidadMedida(int idUnidadMedidaProducto) {
        this.idUnidadMedidaProducto = idUnidadMedidaProducto;
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

    public Date getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(Date fechaCreacion) {
        this.fechaModificacion = fechaCreacion;
    }
    
    
    
}