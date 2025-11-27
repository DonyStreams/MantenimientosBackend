package usac.eps.controladores.mantenimientos.dto;

import java.util.Date;

public class HistorialEquipoDTO {
    private Integer idHistorial;
    private Integer idEquipo;
    private String equipoNombre;
    private String equipoNumeroSerie;
    private Date fechaRegistro;
    private String descripcion;
    private String tipoCambio;          // NUEVO
    private Integer usuarioId;          // NUEVO
    private String usuarioNombre;       // NUEVO

    // Constructor vacío
    public HistorialEquipoDTO() {
    }

    // Constructor completo
    public HistorialEquipoDTO(Integer idHistorial, Integer idEquipo, String equipoNombre, 
                              String equipoNumeroSerie, Date fechaRegistro, String descripcion,
                              String tipoCambio, Integer usuarioId, String usuarioNombre) {
        this.idHistorial = idHistorial;
        this.idEquipo = idEquipo;
        this.equipoNombre = equipoNombre;
        this.equipoNumeroSerie = equipoNumeroSerie;
        this.fechaRegistro = fechaRegistro;
        this.descripcion = descripcion;
        this.tipoCambio = tipoCambio;
        this.usuarioId = usuarioId;
        this.usuarioNombre = usuarioNombre;
    }

    // Getters y Setters
    public Integer getIdHistorial() {
        return idHistorial;
    }

    public void setIdHistorial(Integer idHistorial) {
        this.idHistorial = idHistorial;
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

    public String getEquipoNumeroSerie() {
        return equipoNumeroSerie;
    }

    public void setEquipoNumeroSerie(String equipoNumeroSerie) {
        this.equipoNumeroSerie = equipoNumeroSerie;
    }

    public Date getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Date fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTipoCambio() {
        return tipoCambio;
    }

    public void setTipoCambio(String tipoCambio) {
        this.tipoCambio = tipoCambio;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }
}
