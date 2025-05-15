package usac.eps.modelos;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "StockBitacora")
public class StockBitacoraModel implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdStockBitacora")
    private Long idStockBitacora;

    @ManyToOne
    @JoinColumn(name = "IdStock")
    @NotNull
    private StockModel stockModel;

    @ManyToOne
    @JoinColumn(name = "IdUsuario")
    @NotNull
    private UsuarioModel usuarioModel;

    @Column(name = "Accion", length = 50)
    private String accion;

    @Column(name = "Cantidad")
    private int cantidad;

    @Column(name = "Descripcion", length = 250)
    private String descripcion;

    @Column(name = "FechaModificacion", nullable = false)
    private Date fechaModificacion;

    public Long getIdStockBitacora() {
        return idStockBitacora;
    }

    public void setIdStockBitacora(Long idStockBitacora) {
        this.idStockBitacora = idStockBitacora;
    }

    public StockModel getStockModel() {
        return stockModel;
    }

    public void setStockModel(StockModel stockModel) {
        this.stockModel = stockModel;
    }

    public UsuarioModel getUsuarioModel() {
        return usuarioModel;
    }

    public void setUsuarioModel(UsuarioModel usuarioModel) {
        this.usuarioModel = usuarioModel;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
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

    public void setFechaModificacion(Date fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }
}
