package usac.eps.modelos;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "RegistroAcciones")
public class RegistroAccionesModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idAccion;

    
    private String fechaAccion;

    private String tipoAccion;
    
    private String descripcionAccion;

    // Getters y setters

    public int getIdAccion() {
        return idAccion;
    }

    public void setIdAccion(int idAccion) {
        this.idAccion = idAccion;
    }

    public String getFechaAccion() {
        return fechaAccion;
    }

    public void setFechaAccion(String fechaAccion) {
        this.fechaAccion = fechaAccion;
    }

    public String getTipoAccion() {
        return tipoAccion;
    }

    public void setTipoAccion(String tipoAccion) {
        this.tipoAccion = tipoAccion;
    }

    public String getDescripcionAccion() {
        return descripcionAccion;
    }

    public void setDescripcionAccion(String descripcionAccion) {
        this.descripcionAccion = descripcionAccion;
    }
}