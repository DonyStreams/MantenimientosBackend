package usac.eps.modelos.mantenimientos;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

@Entity
@Table(name = "Historial_Equipo")
public class HistorialEquipoModel implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historial")
    private Integer idHistorial;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_equipo", referencedColumnName = "id_equipo")
    private EquipoModel equipo;

    @Column(name = "fecha_registro")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaRegistro;

    @Column(name = "descripcion")
    private String descripcion;

    // Getters y setters
    public Integer getIdHistorial() {
        return idHistorial;
    }

    public void setIdHistorial(Integer idHistorial) {
        this.idHistorial = idHistorial;
    }

    public EquipoModel getEquipo() {
        return equipo;
    }

    public void setEquipo(EquipoModel equipo) {
        this.equipo = equipo;
    }

    public Date getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Date fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
