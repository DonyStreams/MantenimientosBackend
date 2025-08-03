package usac.eps.modelos.mantenimientos;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

/**
 * Entidad para la programación automática de mantenimientos
 */
@Entity
@Table(name = "Programaciones_Mantenimiento")
public class ProgramacionMantenimientoModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_programacion")
    private Integer idProgramacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_equipo", referencedColumnName = "id_equipo", nullable = false)
    private EquipoModel equipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_mantenimiento", referencedColumnName = "id_tipo", nullable = false)
    private TipoMantenimientoModel tipoMantenimiento;

    @Column(name = "frecuencia_dias", nullable = false)
    private Integer frecuenciaDias;

    @Column(name = "fecha_ultimo_mantenimiento")
    @Temporal(TemporalType.DATE)
    private Date fechaUltimoMantenimiento;

    @Column(name = "fecha_proximo_mantenimiento")
    @Temporal(TemporalType.DATE)
    private Date fechaProximoMantenimiento;

    @Column(name = "dias_alerta_previa")
    private Integer diasAlertaPrevia = 7;

    @Column(name = "activa")
    private Boolean activa = true;

    @Lob
    @Column(name = "observaciones")
    private String observaciones;

    // Campos de auditoría
    @Column(name = "fecha_creacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;

    @Column(name = "fecha_modificacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaModificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_creacion", referencedColumnName = "id")
    private UsuarioMantenimientoModel usuarioCreacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_modificacion", referencedColumnName = "id")
    private UsuarioMantenimientoModel usuarioModificacion;

    // Constructores
    public ProgramacionMantenimientoModel() {
        this.fechaCreacion = new Date();
        this.fechaModificacion = new Date();
    }

    public ProgramacionMantenimientoModel(EquipoModel equipo, TipoMantenimientoModel tipoMantenimiento,
            Integer frecuenciaDias) {
        this();
        this.equipo = equipo;
        this.tipoMantenimiento = tipoMantenimiento;
        this.frecuenciaDias = frecuenciaDias;
        calcularProximoMantenimiento();
    }

    // Métodos de auditoría
    @PrePersist
    protected void onCreate() {
        fechaCreacion = new Date();
        fechaModificacion = new Date();
        if (fechaProximoMantenimiento == null) {
            calcularProximoMantenimiento();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaModificacion = new Date();
    }

    // Métodos de negocio
    public void calcularProximoMantenimiento() {
        if (fechaUltimoMantenimiento != null && frecuenciaDias != null) {
            long proximoTiempo = fechaUltimoMantenimiento.getTime() + (frecuenciaDias * 24L * 60L * 60L * 1000L);
            this.fechaProximoMantenimiento = new Date(proximoTiempo);
        }
    }

    public boolean requiereAlerta() {
        if (fechaProximoMantenimiento == null || !activa) {
            return false;
        }

        Date hoy = new Date();
        long tiempoRestante = fechaProximoMantenimiento.getTime() - hoy.getTime();
        long diasRestantes = tiempoRestante / (24L * 60L * 60L * 1000L);

        return diasRestantes <= diasAlertaPrevia;
    }

    public boolean estaVencida() {
        if (fechaProximoMantenimiento == null || !activa) {
            return false;
        }

        Date hoy = new Date();
        return fechaProximoMantenimiento.before(hoy);
    }

    // Getters y Setters
    public Integer getIdProgramacion() {
        return idProgramacion;
    }

    public void setIdProgramacion(Integer idProgramacion) {
        this.idProgramacion = idProgramacion;
    }

    public EquipoModel getEquipo() {
        return equipo;
    }

    public void setEquipo(EquipoModel equipo) {
        this.equipo = equipo;
    }

    public TipoMantenimientoModel getTipoMantenimiento() {
        return tipoMantenimiento;
    }

    public void setTipoMantenimiento(TipoMantenimientoModel tipoMantenimiento) {
        this.tipoMantenimiento = tipoMantenimiento;
    }

    public Integer getFrecuenciaDias() {
        return frecuenciaDias;
    }

    public void setFrecuenciaDias(Integer frecuenciaDias) {
        this.frecuenciaDias = frecuenciaDias;
        calcularProximoMantenimiento();
    }

    public Date getFechaUltimoMantenimiento() {
        return fechaUltimoMantenimiento;
    }

    public void setFechaUltimoMantenimiento(Date fechaUltimoMantenimiento) {
        this.fechaUltimoMantenimiento = fechaUltimoMantenimiento;
        calcularProximoMantenimiento();
    }

    public Date getFechaProximoMantenimiento() {
        return fechaProximoMantenimiento;
    }

    public void setFechaProximoMantenimiento(Date fechaProximoMantenimiento) {
        this.fechaProximoMantenimiento = fechaProximoMantenimiento;
    }

    public Integer getDiasAlertaPrevia() {
        return diasAlertaPrevia;
    }

    public void setDiasAlertaPrevia(Integer diasAlertaPrevia) {
        this.diasAlertaPrevia = diasAlertaPrevia;
    }

    public Boolean getActiva() {
        return activa;
    }

    public void setActiva(Boolean activa) {
        this.activa = activa;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Date getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(Date fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public UsuarioMantenimientoModel getUsuarioCreacion() {
        return usuarioCreacion;
    }

    public void setUsuarioCreacion(UsuarioMantenimientoModel usuarioCreacion) {
        this.usuarioCreacion = usuarioCreacion;
    }

    public UsuarioMantenimientoModel getUsuarioModificacion() {
        return usuarioModificacion;
    }

    public void setUsuarioModificacion(UsuarioMantenimientoModel usuarioModificacion) {
        this.usuarioModificacion = usuarioModificacion;
    }

    @Override
    public String toString() {
        return "ProgramacionMantenimientoModel{" +
                "idProgramacion=" + idProgramacion +
                ", equipo=" + (equipo != null ? equipo.getNombre() : null) +
                ", tipoMantenimiento=" + (tipoMantenimiento != null ? tipoMantenimiento.getNombre() : null) +
                ", frecuenciaDias=" + frecuenciaDias +
                ", fechaProximoMantenimiento=" + fechaProximoMantenimiento +
                ", activa=" + activa +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ProgramacionMantenimientoModel))
            return false;
        ProgramacionMantenimientoModel that = (ProgramacionMantenimientoModel) o;
        return idProgramacion != null && idProgramacion.equals(that.idProgramacion);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
