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

/**
 *
 * @author Brandon Soto
 */
@Entity
@Table(name = "Producto")
public class ProductoModel implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdProducto")
    private int idProducto;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdTipoProducto", referencedColumnName = "IdTipoProducto")    
    //@NotNull
    private TipoProductoModel tipoProductoModel;    
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdUnidadMedidaProducto", referencedColumnName = "IdUnidadMedidaProducto")    
    //@NotNull
    private UnidadMedidaModel unidadMedidaModel;   
    
    @ManyToOne(fetch = FetchType.LAZY)      
    @JoinColumn(name = "IdPresentacionProducto", referencedColumnName = "IdPresentacionProducto")    
    //@NotNull
    private PresentacionProductoModel presentacionProductoModel;     
    
    @Column(name = "Nombre", length = 255, unique = true, nullable = false)
    private String nombre;

    @Column(name = "PrecioCompra")
    private float precioCompra;
    
    @Column(name = "PrecioVenta")
    private float precioVenta;
    
    @Column(name = "PrecioUnitario")
    private float precioUnitario;
    
    @Column(name = "Descripcion", length = 255)
    private String descripcion;
    
    @Column(name = "FechaModificacion",nullable = false)
    //@Temporal(TemporalType.TIMESTAMP)
    private String fechaModificacion;     

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public TipoProductoModel getTipoProductoModel() {
        return tipoProductoModel;
    }

    public void setTipoProductoModel(TipoProductoModel tipoProductoModel) {
        this.tipoProductoModel = tipoProductoModel;
    }

    public UnidadMedidaModel getUnidadMedidaModel() {
        return unidadMedidaModel;
    }

    public void setUnidadMedidaModel(UnidadMedidaModel unidadMedidaModel) {
        this.unidadMedidaModel = unidadMedidaModel;
    }

    public PresentacionProductoModel getPresentacionProductoModel() {
        return presentacionProductoModel;
    }

    public void setPresentacionProductoModel(PresentacionProductoModel presentacionProductoModel) {
        this.presentacionProductoModel = presentacionProductoModel;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public float getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(float precioCompra) {
        this.precioCompra = precioCompra;
    }

    public float getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(float precioVenta) {
        this.precioVenta = precioVenta;
    }

    public float getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(float precioUnitario) {
        this.precioUnitario = precioUnitario;
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

    public void setFechaModificacion(String fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }
    
    
}