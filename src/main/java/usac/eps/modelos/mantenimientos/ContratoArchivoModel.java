package usac.eps.modelos.mantenimientos;

import javax.persistence.*;
import java.util.Date;

/**
 * Modelo para almacenar archivos adjuntos de contratos
 * Permite adjuntar documentos PDF, imágenes, etc. a los contratos
 */
@Entity
@Table(name = "Contratos_Archivos")
@NamedQueries({
        @NamedQuery(name = "ContratoArchivoModel.findByContrato", query = "SELECT ca FROM ContratoArchivoModel ca WHERE ca.contrato.idContrato = :contratoId ORDER BY ca.fechaSubida DESC"),
        @NamedQuery(name = "ContratoArchivoModel.findByTipo", query = "SELECT ca FROM ContratoArchivoModel ca WHERE ca.tipoArchivo = :tipo ORDER BY ca.fechaSubida DESC")
})
public class ContratoArchivoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_archivo")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_contrato", nullable = false)
    private ContratoModel contrato;

    @Column(name = "nombre_original", nullable = false, length = 255)
    private String nombreOriginal;

    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo; // Nombre único en el sistema de archivos

    @Column(name = "ruta_archivo", nullable = false, length = 500)
    private String rutaArchivo;

    @Column(name = "tipo_archivo", length = 50)
    private String tipoArchivo; // PDF, DOC, IMG, etc.

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "tamano")
    private Long tamano; // En bytes

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "fecha_subida")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaSubida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_subida")
    private UsuarioMantenimientoModel usuarioSubida;

    @Column(name = "activo")
    private Boolean activo;

    // Constructores
    public ContratoArchivoModel() {
        this.fechaSubida = new Date();
        this.activo = true;
    }

    public ContratoArchivoModel(ContratoModel contrato, String nombreOriginal,
            String nombreArchivo, String rutaArchivo,
            String tipoArchivo, String mimeType, Long tamano) {
        this();
        this.contrato = contrato;
        this.nombreOriginal = nombreOriginal;
        this.nombreArchivo = nombreArchivo;
        this.rutaArchivo = rutaArchivo;
        this.tipoArchivo = tipoArchivo;
        this.mimeType = mimeType;
        this.tamano = tamano;
    }

    // Métodos de negocio

    /**
     * Obtiene la extensión del archivo
     */
    public String getExtension() {
        if (nombreOriginal == null)
            return "";
        int lastDot = nombreOriginal.lastIndexOf('.');
        return lastDot > 0 ? nombreOriginal.substring(lastDot + 1).toLowerCase() : "";
    }

    /**
     * Verifica si es un archivo de imagen
     */
    public boolean isImagen() {
        String ext = getExtension();
        return ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png") ||
                ext.equals("gif") || ext.equals("bmp") || ext.equals("webp");
    }

    /**
     * Verifica si es un archivo PDF
     */
    public boolean isPdf() {
        return "pdf".equals(getExtension());
    }

    /**
     * Verifica si es un documento de Office
     */
    public boolean isDocumento() {
        String ext = getExtension();
        return ext.equals("doc") || ext.equals("docx") || ext.equals("xls") ||
                ext.equals("xlsx") || ext.equals("ppt") || ext.equals("pptx");
    }

    /**
     * Obtiene el tamaño formateado en KB/MB
     */
    public String getTamanoFormateado() {
        if (tamano == null)
            return "0 KB";

        if (tamano < 1024)
            return tamano + " B";
        if (tamano < 1024 * 1024)
            return String.format("%.1f KB", tamano / 1024.0);
        return String.format("%.1f MB", tamano / (1024.0 * 1024.0));
    }

    /**
     * Obtiene el ícono CSS según el tipo de archivo
     */
    public String getIconoCss() {
        if (isPdf())
            return "pi pi-file-pdf";
        if (isImagen())
            return "pi pi-image";
        if (isDocumento())
            return "pi pi-file-word";
        return "pi pi-file";
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ContratoModel getContrato() {
        return contrato;
    }

    public void setContrato(ContratoModel contrato) {
        this.contrato = contrato;
    }

    public String getNombreOriginal() {
        return nombreOriginal;
    }

    public void setNombreOriginal(String nombreOriginal) {
        this.nombreOriginal = nombreOriginal;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public void setRutaArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    public String getTipoArchivo() {
        return tipoArchivo;
    }

    public void setTipoArchivo(String tipoArchivo) {
        this.tipoArchivo = tipoArchivo;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Long getTamano() {
        return tamano;
    }

    public void setTamano(Long tamano) {
        this.tamano = tamano;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Date getFechaSubida() {
        return fechaSubida;
    }

    public void setFechaSubida(Date fechaSubida) {
        this.fechaSubida = fechaSubida;
    }

    public UsuarioMantenimientoModel getUsuarioSubida() {
        return usuarioSubida;
    }

    public void setUsuarioSubida(UsuarioMantenimientoModel usuarioSubida) {
        this.usuarioSubida = usuarioSubida;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return "ContratoArchivoModel{" +
                "id=" + id +
                ", nombreOriginal='" + nombreOriginal + '\'' +
                ", tipoArchivo='" + tipoArchivo + '\'' +
                ", tamano=" + getTamanoFormateado() +
                ", fechaSubida=" + fechaSubida +
                '}';
    }
}
