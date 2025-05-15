package usac.eps.modelos;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

/**
 *
 * @author Brandon Soto prueba commit
 */
@Entity
@Table(name = "Usuario")
public class UsuarioModel implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdUsuario")
    private int idUsuario;
    
    @ManyToOne
    @JoinColumn(name = "IdRol", referencedColumnName = "IdRol")
    @NotNull
    private RolModel rolModel;    
    
    @ManyToOne
    @JoinColumn(name = "IdSede", referencedColumnName = "IdSede")
    @NotNull
    private SedeModel sedeModel;    
    
    @Column(name = "Usuario", length = 25,unique = true, nullable = false)
    private String usuario;

    @Column(name = "Nombre", length = 255,nullable = false)
    private String nombre;
    
    @Column(name = "Apellido", length = 255,nullable = false)
    private String apellido;
    
    @Column(name = "Direccion", length = 255,nullable = false)
    private String direccion;
    
    @Column(name = "Correo", length = 100,unique = true)
    private String correo;
    
    @Column(name = "Telefono", length = 20)
    private String telefono;
    
    @Column(name = "FechaModificacion",nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaModificacion;                

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public RolModel getRolModel() {
        return rolModel;
    }

    public void setRolModel(RolModel rolModel) {
        this.rolModel = rolModel;
    }

    public SedeModel getSedeModel() {
        return sedeModel;
    }

    public void setSedeModel(SedeModel sedeModel) {
        this.sedeModel = sedeModel;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Date getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(Date fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }
    
    
}