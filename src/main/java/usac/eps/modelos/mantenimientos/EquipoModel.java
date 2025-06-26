package usac.eps.modelos.mantenimientos;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.*;

@Entity
@Table(name = "Equipos")
public class EquipoModel implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_equipo")
    private Integer idEquipo;

    @Column(name = "numero_inventario", length = 50, unique = true)
    private String numeroInventario;

    @Column(name = "numero_serie", length = 50)
    private String numeroSerie;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "estado")
    private Boolean estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_area", referencedColumnName = "id_area")
    private AreaModel area;

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

    @OneToMany(mappedBy = "equipo", fetch = FetchType.LAZY)
    private List<HistorialEquipoModel> historialEquipos;

    @Column(name = "nombre", length = 100)
    private String nombre;

    @Column(name = "codigo_inacif", length = 50)
    private String codigoInacif;

    @Column(name = "marca", length = 50)
    private String marca;

    @Column(name = "modelo", length = 50)
    private String modelo;

    @Column(name = "ubicacion", length = 100)
    private String ubicacion;

    @Column(name = "magnitud_medicion", length = 100)
    private String magnitudMedicion;

    @Column(name = "rango_capacidad", length = 100)
    private String rangoCapacidad;

    @Column(name = "manual_fabricante", length = 100)
    private String manualFabricante;

    @Column(name = "fotografia", length = 255)
    private String fotografia;

    @Column(name = "software_firmware", length = 100)
    private String softwareFirmware;

    @Column(name = "condiciones_operacion", length = 255)
    private String condicionesOperacion;

    // Getters y setters
    public Integer getIdEquipo() {
        return idEquipo;
    }

    public void setIdEquipo(Integer idEquipo) {
        this.idEquipo = idEquipo;
    }

    public String getNumeroInventario() {
        return numeroInventario;
    }

    public void setNumeroInventario(String numeroInventario) {
        this.numeroInventario = numeroInventario;
    }

    public String getNumeroSerie() {
        return numeroSerie;
    }

    public void setNumeroSerie(String numeroSerie) {
        this.numeroSerie = numeroSerie;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }

    @JsonbTransient
    public AreaModel getArea() {
        return area;
    }

    public void setArea(AreaModel area) {
        this.area = area;
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

    @JsonbTransient
    public List<HistorialEquipoModel> getHistorialEquipos() {
        return historialEquipos;
    }

    public void setHistorialEquipos(List<HistorialEquipoModel> historialEquipos) {
        this.historialEquipos = historialEquipos;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCodigoInacif() {
        return codigoInacif;
    }

    public void setCodigoInacif(String codigoInacif) {
        this.codigoInacif = codigoInacif;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getMagnitudMedicion() {
        return magnitudMedicion;
    }

    public void setMagnitudMedicion(String magnitudMedicion) {
        this.magnitudMedicion = magnitudMedicion;
    }

    public String getRangoCapacidad() {
        return rangoCapacidad;
    }

    public void setRangoCapacidad(String rangoCapacidad) {
        this.rangoCapacidad = rangoCapacidad;
    }

    public String getManualFabricante() {
        return manualFabricante;
    }

    public void setManualFabricante(String manualFabricante) {
        this.manualFabricante = manualFabricante;
    }

    public String getFotografia() {
        return fotografia;
    }

    public void setFotografia(String fotografia) {
        this.fotografia = fotografia;
    }

    public String getSoftwareFirmware() {
        return softwareFirmware;
    }

    public void setSoftwareFirmware(String softwareFirmware) {
        this.softwareFirmware = softwareFirmware;
    }

    public String getCondicionesOperacion() {
        return condicionesOperacion;
    }

    public void setCondicionesOperacion(String condicionesOperacion) {
        this.condicionesOperacion = condicionesOperacion;
    }
}
