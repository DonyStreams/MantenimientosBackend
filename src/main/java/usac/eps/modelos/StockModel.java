package usac.eps.modelos;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "Stock")
public class StockModel implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdStock")
    private Long idStock;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "IdProducto", referencedColumnName = "IdProducto")
    private ProductoModel productoModel;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "IdSede", referencedColumnName = "IdSede")
    private SedeModel sedeModel;
    
    @Column(name = "Cantidad")
    private int cantidad;
    
    @Column(name = "CantidadMinima")
    private int cantidadMinima;
    
    @Column(name = "CantidadMaxima")
    private int cantidadMaxima;
    
    @Column(name = "FechaCaducidad")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCaducidad;
    
    @Column(name = "UbicacionFisica", length = 255)
    private String ubicacionFisica;
    
    @Column(name = "FechaModificacion", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaModificacion;

    public Long getIdStock() {
        return idStock;
    }

    public void setIdStock(Long idStock) {
        this.idStock = idStock;
    }

    public ProductoModel getProductoModel() {
        return productoModel;
    }

    public void setProductoModel(ProductoModel productoModel) {
        this.productoModel = productoModel;
    }

    public SedeModel getSedeModel() {
        return sedeModel;
    }

    public void setSedeModel(SedeModel sedeModel) {
        this.sedeModel = sedeModel;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public int getCantidadMinima() {
        return cantidadMinima;
    }

    public void setCantidadMinima(int cantidadMinima) {
        this.cantidadMinima = cantidadMinima;
    }

    public int getCantidadMaxima() {
        return cantidadMaxima;
    }

    public void setCantidadMaxima(int cantidadMaxima) {
        this.cantidadMaxima = cantidadMaxima;
    }

    public Date getFechaCaducidad() {
        return fechaCaducidad;
    }

    public void setFechaCaducidad(Date fechaCaducidad) {
        this.fechaCaducidad = fechaCaducidad;
    }

    public String getUbicacionFisica() {
        return ubicacionFisica;
    }

    public void setUbicacionFisica(String ubicacionFisica) {
        this.ubicacionFisica = ubicacionFisica;
    }

    public Date getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(Date fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }
}
