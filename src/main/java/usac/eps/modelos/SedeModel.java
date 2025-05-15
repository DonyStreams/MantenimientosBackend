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
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

/**
 *
 * @author Brandon Soto
 */
@Entity
@Table(name = "Sede",uniqueConstraints = @UniqueConstraint(columnNames = {"Nombre", "IdDepartamento"}))
public class SedeModel implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdSede")
    private int idSede;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "IdDepartamento", referencedColumnName = "IdDepartamento")
    @NotNull
    private DepartamentoModel departamentoModel;    
    
    @Column(name = "Nombre", length = 255)
    private String nombre;

    @Column(name = "Direccion", length = 255)
    private String direccion;
    
    @Column(name = "FechaModificacion",nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaModificacion;     

    public int getIdSede() {
        return idSede;
    }

    public void setIdSede(int idSede) {
        this.idSede = idSede;
    }

    public DepartamentoModel getDepartamentoModel() {
        return departamentoModel;
    }

    public void setDepartamentoModel(DepartamentoModel departamentoModel) {
        this.departamentoModel = departamentoModel;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Date getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(Date fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

        
    
}