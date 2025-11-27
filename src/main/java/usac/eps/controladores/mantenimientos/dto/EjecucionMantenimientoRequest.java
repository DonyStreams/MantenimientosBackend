package usac.eps.controladores.mantenimientos.dto;

import java.util.Date;

public class EjecucionMantenimientoRequest {
    private Integer idContrato;
    private Integer idEquipo;
    private Integer idProgramacion;
    private Integer usuarioResponsableId;

    private Date fechaEjecucion;
    private Date fechaInicioTrabajo;
    private Date fechaCierre;

    private String estado;
    private String bitacora;

    public Integer getIdContrato() {
        return idContrato;
    }

    public void setIdContrato(Integer idContrato) {
        this.idContrato = idContrato;
    }

    public Integer getIdEquipo() {
        return idEquipo;
    }

    public void setIdEquipo(Integer idEquipo) {
        this.idEquipo = idEquipo;
    }

    public Integer getIdProgramacion() {
        return idProgramacion;
    }

    public void setIdProgramacion(Integer idProgramacion) {
        this.idProgramacion = idProgramacion;
    }

    public Integer getUsuarioResponsableId() {
        return usuarioResponsableId;
    }

    public void setUsuarioResponsableId(Integer usuarioResponsableId) {
        this.usuarioResponsableId = usuarioResponsableId;
    }

    public Date getFechaEjecucion() {
        return fechaEjecucion;
    }

    public void setFechaEjecucion(Date fechaEjecucion) {
        this.fechaEjecucion = fechaEjecucion;
    }

    public Date getFechaInicioTrabajo() {
        return fechaInicioTrabajo;
    }

    public void setFechaInicioTrabajo(Date fechaInicioTrabajo) {
        this.fechaInicioTrabajo = fechaInicioTrabajo;
    }

    public Date getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(Date fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getBitacora() {
        return bitacora;
    }

    public void setBitacora(String bitacora) {
        this.bitacora = bitacora;
    }
}
