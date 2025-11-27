package usac.eps.controladores.mantenimientos.dto;

import java.util.Date;

public class EjecucionMantenimientoDTO {
    private Integer idEjecucion;
    private Date fechaEjecucion;
    private Date fechaInicioTrabajo;
    private Date fechaCierre;
    private String estado;
    private String bitacora;

    private Integer idContrato;
    private String contratoDescripcion;
    private Integer idProveedor;
    private String proveedorNombre;

    private Integer idEquipo;
    private String equipoNombre;
    private String equipoCodigo;
    private String equipoUbicacion;

    private Integer idProgramacion;
    private Integer frecuenciaDias;
    private Date fechaProximoProgramado;

    private Integer usuarioResponsableId;
    private String usuarioResponsableNombre;

    public Integer getIdEjecucion() {
        return idEjecucion;
    }

    public void setIdEjecucion(Integer idEjecucion) {
        this.idEjecucion = idEjecucion;
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

    public Integer getIdContrato() {
        return idContrato;
    }

    public void setIdContrato(Integer idContrato) {
        this.idContrato = idContrato;
    }

    public String getContratoDescripcion() {
        return contratoDescripcion;
    }

    public void setContratoDescripcion(String contratoDescripcion) {
        this.contratoDescripcion = contratoDescripcion;
    }

    public Integer getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(Integer idProveedor) {
        this.idProveedor = idProveedor;
    }

    public String getProveedorNombre() {
        return proveedorNombre;
    }

    public void setProveedorNombre(String proveedorNombre) {
        this.proveedorNombre = proveedorNombre;
    }

    public Integer getIdEquipo() {
        return idEquipo;
    }

    public void setIdEquipo(Integer idEquipo) {
        this.idEquipo = idEquipo;
    }

    public String getEquipoNombre() {
        return equipoNombre;
    }

    public void setEquipoNombre(String equipoNombre) {
        this.equipoNombre = equipoNombre;
    }

    public String getEquipoCodigo() {
        return equipoCodigo;
    }

    public void setEquipoCodigo(String equipoCodigo) {
        this.equipoCodigo = equipoCodigo;
    }

    public String getEquipoUbicacion() {
        return equipoUbicacion;
    }

    public void setEquipoUbicacion(String equipoUbicacion) {
        this.equipoUbicacion = equipoUbicacion;
    }

    public Integer getIdProgramacion() {
        return idProgramacion;
    }

    public void setIdProgramacion(Integer idProgramacion) {
        this.idProgramacion = idProgramacion;
    }

    public Integer getFrecuenciaDias() {
        return frecuenciaDias;
    }

    public void setFrecuenciaDias(Integer frecuenciaDias) {
        this.frecuenciaDias = frecuenciaDias;
    }

    public Date getFechaProximoProgramado() {
        return fechaProximoProgramado;
    }

    public void setFechaProximoProgramado(Date fechaProximoProgramado) {
        this.fechaProximoProgramado = fechaProximoProgramado;
    }

    public Integer getUsuarioResponsableId() {
        return usuarioResponsableId;
    }

    public void setUsuarioResponsableId(Integer usuarioResponsableId) {
        this.usuarioResponsableId = usuarioResponsableId;
    }

    public String getUsuarioResponsableNombre() {
        return usuarioResponsableNombre;
    }

    public void setUsuarioResponsableNombre(String usuarioResponsableNombre) {
        this.usuarioResponsableNombre = usuarioResponsableNombre;
    }
}
