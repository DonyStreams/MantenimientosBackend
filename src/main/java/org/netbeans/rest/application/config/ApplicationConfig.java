/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.netbeans.rest.application.config;

import java.util.Set;
import javax.ws.rs.core.Application;
import org.apache.cxf.jaxrs.provider.MultipartProvider;

/**
 *
 * @author USER
 */
@javax.ws.rs.ApplicationPath("/api")
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        addRestResourceClasses(resources);
        return resources;
    }

    /**
     * Do not modify addRestResourceClasses() method.
     * It is automatically populated with
     * all resources defined in the project.
     * If required, comment out calling this method in getClasses().
     */
    private void addRestResourceClasses(Set<Class<?>> resources) {
        // Controllers de mantenimientos
        resources.add(usac.eps.controladores.mantenimientos.AlertaMantenimientoController.class);
        resources.add(usac.eps.controladores.mantenimientos.AreaController.class);
        resources.add(usac.eps.controladores.mantenimientos.ComentarioTicketController.class);
        resources.add(usac.eps.controladores.mantenimientos.ContratoController.class);
        resources.add(usac.eps.controladores.mantenimientos.EjecucionMantenimientoController.class);
        resources.add(usac.eps.controladores.mantenimientos.EquipoController.class);
        resources.add(usac.eps.controladores.mantenimientos.EvidenciaController.class);
        resources.add(usac.eps.controladores.mantenimientos.HistorialEquipoController.class);
        resources.add(usac.eps.controladores.mantenimientos.MantenimientoController.class);
        resources.add(usac.eps.controladores.mantenimientos.ProgramacionMantenimientoController.class);
        resources.add(usac.eps.controladores.mantenimientos.ProveedorController.class);
        resources.add(usac.eps.controladores.mantenimientos.TicketController.class);
        resources.add(usac.eps.controladores.mantenimientos.TipoComentarioController.class);
        resources.add(usac.eps.controladores.mantenimientos.TipoMantenimientoController.class);
        resources.add(usac.eps.controladores.mantenimientos.UsuarioController.class);
        resources.add(usac.eps.controladores.mantenimientos.FtpController.class);

        // Controller de autenticación
        resources.add(usac.eps.controladores.auth.AuthController.class);

        // Filtros de seguridad
        resources.add(usac.eps.seguridad.JWTAuthenticationFilter.class);

        // Providers
        resources.add(MultipartProvider.class);
    }

}
