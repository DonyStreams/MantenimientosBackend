package usac.eps.modelos;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "RequisicionDetalle")
public class RequisicionDetalleModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdRequisicionDetalle")
    private Long idRequisicionDetalle;

    @ManyToOne
    @JoinColumn(name = "IdRequisicion", nullable = false)
    private RequisicionModel requisicion;

    @ManyToOne
    @JoinColumn(name = "IdProducto", nullable = false)
    private ProductoModel producto;

    @Column(name = "CantidadSolicitada")
    private int cantidadSolicitada;

    public Long getIdDetalleRequisicion() {
        return idRequisicionDetalle;
    }

    public void setIdDetalleRequisicion(Long idDetalleRequisicion) {
        this.idRequisicionDetalle = idDetalleRequisicion;
    }

    public RequisicionModel getRequisicion() {
        return requisicion;
    }

    public void setRequisicion(RequisicionModel requisicion) {
        this.requisicion = requisicion;
    }

    public ProductoModel getProducto() {
        return producto;
    }

    public void setProducto(ProductoModel producto) {
        this.producto = producto;
    }

    public int getCantidadSolicitada() {
        return cantidadSolicitada;
    }

    public void setCantidadSolicitada(int cantidadSolicitada) {
        this.cantidadSolicitada = cantidadSolicitada;
    }
}
