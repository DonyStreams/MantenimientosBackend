package usac.eps.modelos.mantenimientos;

import javax.persistence.*;
import java.util.Date;

/**
 * Entidad para configuración de alertas automáticas
 * Permite definir días de anticipación, horarios y tipos de alerta
 */
@Entity
@Table(name = "Configuracion_Alertas")
@NamedQueries({
        @NamedQuery(name = "ConfiguracionAlerta.findAll", query = "SELECT c FROM ConfiguracionAlertaModel c ORDER BY c.nombre"),
        @NamedQuery(name = "ConfiguracionAlerta.findActivas", query = "SELECT c FROM ConfiguracionAlertaModel c WHERE c.activa = true"),
        @NamedQuery(name = "ConfiguracionAlerta.findByTipo", query = "SELECT c FROM ConfiguracionAlertaModel c WHERE c.tipoAlerta = :tipo AND c.activa = true")
})
public class ConfiguracionAlertaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_configuracion")
    private Integer idConfiguracion;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "tipo_alerta", nullable = false, length = 50)
    private String tipoAlerta;

    @Column(name = "dias_anticipacion")
    private Integer diasAnticipacion = 30;

    @Column(name = "activa")
    private Boolean activa = true;

    @Column(name = "usuarios_notificar", columnDefinition = "NVARCHAR(MAX)")
    private String usuariosNotificar;

    @Column(name = "fecha_creacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion = new Date();

    @Column(name = "usuario_creacion")
    private Integer usuarioCreacion;

    // Constructores
    public ConfiguracionAlertaModel() {
    }

    public ConfiguracionAlertaModel(String nombre, String tipoAlerta, Integer diasAnticipacion) {
        this.nombre = nombre;
        this.tipoAlerta = tipoAlerta;
        this.diasAnticipacion = diasAnticipacion;
        this.activa = true;
        this.fechaCreacion = new Date();
    }

    // Getters y Setters
    public Integer getIdConfiguracion() {
        return idConfiguracion;
    }

    public void setIdConfiguracion(Integer idConfiguracion) {
        this.idConfiguracion = idConfiguracion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTipoAlerta() {
        return tipoAlerta;
    }

    public void setTipoAlerta(String tipoAlerta) {
        this.tipoAlerta = tipoAlerta;
    }

    public Integer getDiasAnticipacion() {
        return diasAnticipacion;
    }

    public void setDiasAnticipacion(Integer diasAnticipacion) {
        this.diasAnticipacion = diasAnticipacion;
    }

    public Boolean getActiva() {
        return activa;
    }

    public void setActiva(Boolean activa) {
        this.activa = activa;
    }

    public String getUsuariosNotificar() {
        return usuariosNotificar;
    }

    public void setUsuariosNotificar(String usuariosNotificar) {
        this.usuariosNotificar = usuariosNotificar;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Integer getUsuarioCreacion() {
        return usuarioCreacion;
    }

    public void setUsuarioCreacion(Integer usuarioCreacion) {
        this.usuarioCreacion = usuarioCreacion;
    }

    @Override
    public String toString() {
        return "ConfiguracionAlertaModel{" +
                "idConfiguracion=" + idConfiguracion +
                ", nombre='" + nombre + '\'' +
                ", tipoAlerta='" + tipoAlerta + '\'' +
                ", diasAnticipacion=" + diasAnticipacion +
                ", activa=" + activa +
                '}';
    }
}
