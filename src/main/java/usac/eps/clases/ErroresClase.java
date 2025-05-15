/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package usac.eps.clases;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import usac.eps.modelos.RegistroErroresModel;
import usac.eps.repositorios.ErroresRepository;

/**
 *
 * @author Brandon Soto
 */
@ApplicationScoped
public class ErroresClase {

    @Inject
    private ErroresRepository erroresRepository;

    private LocalDateTime fechaHoraActual;
    private String fechaISO;

    public void insertarErrores(String mensaje, String DetalleError) {

        // Obtén la fecha y hora actual
        LocalDateTime now = LocalDateTime.now();
        // Define el formato deseado
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SS");
        // Formatea la fecha y hora
        String formattedDateTime = now.format(formatter);

        RegistroErroresModel errores = new RegistroErroresModel();
        errores.setMensajeError(mensaje);
        errores.setDetallesError(DetalleError);
        errores.setFechaRegistro(formattedDateTime);
        erroresRepository.saveAndFlush(errores);
    }

}
