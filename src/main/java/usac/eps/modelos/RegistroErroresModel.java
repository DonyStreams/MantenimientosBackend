package usac.eps.modelos;
import javax.persistence.*;

@Entity
@Table(name = "RegistroErrores")
public class RegistroErroresModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idError;
    
    @Column(name = "FechaRegistro")
    private String fechaRegistro;

    
    @Column(name = "MensajeError")
    private String mensajeError;
   
    @Column(name = "DetallesError")
    private String detallesError;

    // Getters y setters

    public int getIdError() {
        return idError;
    }

    public void setIdError(int idError) {
        this.idError = idError;
    }

    public String getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(String fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getMensajeError() {
        return mensajeError;
    }

    public void setMensajeError(String mensajeError) {
        this.mensajeError = mensajeError;
    }

    public String getDetallesError() {
        return detallesError;
    }

    public void setDetallesError(String detallesError) {
        this.detallesError = detallesError;
    }
}