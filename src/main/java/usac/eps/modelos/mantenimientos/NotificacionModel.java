package usac.eps.modelos.mantenimientos;

import javax.persistence.*;
import javax.json.bind.annotation.JsonbDateFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

/**
 * Entidad para notificaciones del sistema
 * Almacena alertas de mantenimientos, contratos y eventos críticos
 */
@Entity
@Table(name = "Notificaciones")
@NamedQueries({
        @NamedQuery(name = "Notificacion.findAll", query = "SELECT n FROM NotificacionModel n ORDER BY n.fechaCreacion DESC"),
        @NamedQuery(name = "Notificacion.findNoLeidas", query = "SELECT n FROM NotificacionModel n WHERE n.leida = false ORDER BY n.fechaCreacion DESC"),
        @NamedQuery(name = "Notificacion.findByTipo", query = "SELECT n FROM NotificacionModel n WHERE n.tipoNotificacion = :tipo ORDER BY n.fechaCreacion DESC"),
        @NamedQuery(name = "Notificacion.countNoLeidas", query = "SELECT COUNT(n) FROM NotificacionModel n WHERE n.leida = false"),
        @NamedQuery(name = "Notificacion.countByPrioridad", query = "SELECT COUNT(n) FROM NotificacionModel n WHERE n.prioridad = :prioridad AND n.leida = false"),
        @NamedQuery(name = "Notificacion.findRecientes", query = "SELECT n FROM NotificacionModel n WHERE n.fechaCreacion >= :fechaDesde ORDER BY n.fechaCreacion DESC"),
        @NamedQuery(name = "Notificacion.findByEntidad", query = "SELECT n FROM NotificacionModel n WHERE n.entidadRelacionada = :entidad AND n.entidadId = :entidadId ORDER BY n.fechaCreacion DESC")
})
public class NotificacionModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion")
    private Integer idNotificacion;

    @Column(name = "tipo_notificacion", nullable = false, length = 50)
    private String tipoNotificacion;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "mensaje", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String mensaje;

    @Column(name = "entidad_relacionada", length = 50)
    private String entidadRelacionada;

    @Column(name = "entidad_id")
    private Integer entidadId;

    @Column(name = "prioridad", length = 20)
    private String prioridad = "Media";

    @Column(name = "leida")
    private Boolean leida = false;

    @Column(name = "fecha_creacion")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonbDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "America/Guatemala")
    private Date fechaCreacion = new Date();

    @Column(name = "fecha_lectura")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonbDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "America/Guatemala")
    private Date fechaLectura;

    @Column(name = "usuario_destinatario")
    private Integer usuarioDestinatario;

    // Constructores
    public NotificacionModel() {
    }

    public NotificacionModel(String tipoNotificacion, String titulo, String mensaje, String prioridad) {
        this.tipoNotificacion = tipoNotificacion;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.prioridad = prioridad;
        this.fechaCreacion = new Date();
        this.leida = false;
    }

    // Getters y Setters
    public Integer getIdNotificacion() {
        return idNotificacion;
    }

    public void setIdNotificacion(Integer idNotificacion) {
        this.idNotificacion = idNotificacion;
    }

    public String getTipoNotificacion() {
        return tipoNotificacion;
    }

    public void setTipoNotificacion(String tipoNotificacion) {
        this.tipoNotificacion = tipoNotificacion;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getEntidadRelacionada() {
        return entidadRelacionada;
    }

    public void setEntidadRelacionada(String entidadRelacionada) {
        this.entidadRelacionada = entidadRelacionada;
    }

    public Integer getEntidadId() {
        return entidadId;
    }

    public void setEntidadId(Integer entidadId) {
        this.entidadId = entidadId;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }

    public Boolean getLeida() {
        return leida;
    }

    public void setLeida(Boolean leida) {
        this.leida = leida;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Date getFechaLectura() {
        return fechaLectura;
    }

    public void setFechaLectura(Date fechaLectura) {
        this.fechaLectura = fechaLectura;
    }

    public Integer getUsuarioDestinatario() {
        return usuarioDestinatario;
    }

    public void setUsuarioDestinatario(Integer usuarioDestinatario) {
        this.usuarioDestinatario = usuarioDestinatario;
    }

    @Override
    public String toString() {
        return "NotificacionModel{" +
                "idNotificacion=" + idNotificacion +
                ", tipoNotificacion='" + tipoNotificacion + '\'' +
                ", titulo='" + titulo + '\'' +
                ", prioridad='" + prioridad + '\'' +
                ", leida=" + leida +
                '}';
    }
}
