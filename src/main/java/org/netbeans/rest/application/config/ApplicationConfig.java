/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.netbeans.rest.application.config;

import java.util.Set;
import javax.ws.rs.core.Application;

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
        resources.add(usac.eps.controladores.Autenticacion.class);
        resources.add(usac.eps.controladores.DepartamentoController.class);
        resources.add(usac.eps.controladores.PresentacionProductoController.class);
        resources.add(usac.eps.controladores.ProductoController.class);
        resources.add(usac.eps.controladores.RequisicionBitacoraController.class);
        resources.add(usac.eps.controladores.RequisicionController.class);
        resources.add(usac.eps.controladores.RequisicionDetalleController.class);
        resources.add(usac.eps.controladores.RolController.class);
        resources.add(usac.eps.controladores.SedeController.class);
        resources.add(usac.eps.controladores.StockBitacoraController.class);
        resources.add(usac.eps.controladores.StockController.class);
        resources.add(usac.eps.controladores.TipoProductoController.class);
        resources.add(usac.eps.controladores.TipoRequisicionController.class);
        resources.add(usac.eps.controladores.UnidadController.class);
        resources.add(usac.eps.controladores.UnidadMedidaController.class);
        resources.add(usac.eps.controladores.UsuarioController.class);
        resources.add(usac.eps.controladores.mantenimientos.AreaController.class);
        resources.add(usac.eps.controladores.mantenimientos.ComentarioTicketController.class);
        resources.add(usac.eps.controladores.mantenimientos.ContratoController.class);
        resources.add(usac.eps.controladores.mantenimientos.EjecucionMantenimientoController.class);
        resources.add(usac.eps.controladores.mantenimientos.EquipoController.class);
        resources.add(usac.eps.controladores.mantenimientos.EvidenciaController.class);
        resources.add(usac.eps.controladores.mantenimientos.HistorialEquipoController.class);
        resources.add(usac.eps.controladores.mantenimientos.ProveedorController.class);
        resources.add(usac.eps.controladores.mantenimientos.TicketController.class);
        resources.add(usac.eps.controladores.mantenimientos.TipoComentarioController.class);
        resources.add(usac.eps.controladores.mantenimientos.TipoMantenimientoController.class);
    }

}
