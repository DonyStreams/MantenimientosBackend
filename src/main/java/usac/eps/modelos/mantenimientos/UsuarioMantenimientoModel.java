package usac.eps.modelos.mantenimientos;

import java.io.Serializable;
import java.util.UUID;
import javax.persistence.*;

@Entity
@Table(name = "Usuarios")
public class UsuarioMantenimientoModel implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "keycloak_id", unique = true, nullable = false)
    private UUID keycloakId;

    @Column(name = "nombre_completo", length = 100)
    private String nombreCompleto;

    @Column(name = "correo", length = 100)
    private String correo;

    @Column(name = "activo")
    private Boolean activo;

    // Getters y setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public UUID getKeycloakId() { return keycloakId; }
    public void setKeycloakId(UUID keycloakId) { this.keycloakId = keycloakId; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
