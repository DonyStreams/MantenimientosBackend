package usac.eps.modelos.mantenimientos;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

@Entity
@Table(name = "Comentarios_Ticket")
public class ComentarioTicketModel implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", referencedColumnName = "id")
    private TicketModel ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", referencedColumnName = "id")
    private UsuarioMantenimientoModel usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_comentario_id", referencedColumnName = "id_tipo")
    private TipoComentarioModel tipoComentario;

    @Column(name = "comentario")
    private String comentario;

    @Column(name = "fecha_creacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;

    // Getters y setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public TicketModel getTicket() {
        return ticket;
    }

    public void setTicket(TicketModel ticket) {
        this.ticket = ticket;
    }

    public UsuarioMantenimientoModel getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioMantenimientoModel usuario) {
        this.usuario = usuario;
    }

    public TipoComentarioModel getTipoComentario() {
        return tipoComentario;
    }

    public void setTipoComentario(TipoComentarioModel tipoComentario) {
        this.tipoComentario = tipoComentario;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
