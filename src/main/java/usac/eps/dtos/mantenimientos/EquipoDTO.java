package usac.eps.dtos.mantenimientos;

import java.util.Date;

public class EquipoDTO {
    private Integer idEquipo;
    private String numeroInventario;
    private String numeroSerie;
    private String descripcion;
    private Boolean estado;
    private Date fechaCreacion;
    private Date fechaModificacion;

    // Getters y setters
    public Integer getIdEquipo() { return idEquipo; }
    public void setIdEquipo(Integer idEquipo) { this.idEquipo = idEquipo; }
    public String getNumeroInventario() { return numeroInventario; }
    public void setNumeroInventario(String numeroInventario) { this.numeroInventario = numeroInventario; }
    public String getNumeroSerie() { return numeroSerie; }
    public void setNumeroSerie(String numeroSerie) { this.numeroSerie = numeroSerie; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Boolean getEstado() { return estado; }
    public void setEstado(Boolean estado) { this.estado = estado; }
    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public Date getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(Date fechaModificacion) { this.fechaModificacion = fechaModificacion; }
}
