package usac.eps.modelos.mantenimientos;

import java.io.Serializable;
import javax.persistence.*;

@Entity
@Table(name = "Tipos_Comentario")
public class TipoComentarioModel implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo")
    private Integer idTipo;

    @Column(name = "nombre", length = 50, unique = true)
    private String nombre;

    // Getters y setters
    public Integer getIdTipo() { return idTipo; }
    public void setIdTipo(Integer idTipo) { this.idTipo = idTipo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
