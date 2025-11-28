package usac.eps.controladores.mantenimientos;

import usac.eps.modelos.mantenimientos.*;

import javax.enterprise.context.RequestScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/reportes")
@RequestScoped
public class ReportesController {

    private static final Logger LOGGER = Logger.getLogger(ReportesController.class.getName());
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private static final SimpleDateFormat DATE_PARSER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    @PersistenceContext
    private EntityManager em;

    /**
     * Genera reporte de equipos en PDF (simplificado como texto por ahora)
     */
    @GET
    @Path("/equipos/pdf")
    @Produces("application/pdf")
    public Response generarReporteEquiposPDF(
            @QueryParam("fechaInicio") String fechaInicio,
            @QueryParam("fechaFin") String fechaFin,
            @QueryParam("idArea") Integer idArea) {

        try {
            LOGGER.info("Generando reporte de equipos en PDF - Area: " + idArea + ", FechaInicio: " + fechaInicio
                    + ", FechaFin: " + fechaFin);

            // Construir query con filtros
            String jpql = "SELECT e FROM EquipoModel e LEFT JOIN FETCH e.area WHERE 1=1";
            if (idArea != null) {
                jpql += " AND e.idArea = :idArea";
            }
            // Por ahora no filtramos por fecha en equipos (no tienen fecha en el modelo
            // base)

            javax.persistence.TypedQuery<EquipoModel> query = em.createQuery(jpql, EquipoModel.class);
            if (idArea != null) {
                query.setParameter("idArea", idArea);
            }

            List<EquipoModel> equipos = query.setMaxResults(1000).getResultList();

            // Generar contenido del reporte
            StringBuilder contenido = new StringBuilder();
            contenido.append("REPORTE DE EQUIPOS\n");
            contenido.append("Fecha de generación: ")
                    .append(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()))
                    .append("\n");
            if (idArea != null) {
                // Buscar el nombre del área
                try {
                    AreaModel area = em.find(AreaModel.class, idArea);
                    if (area != null) {
                        contenido.append("Filtrado por área: ").append(area.getNombre()).append(" (ID: ").append(idArea)
                                .append(")\n");
                    } else {
                        contenido.append("Filtrado por área ID: ").append(idArea).append(" (No encontrada)\n");
                    }
                } catch (Exception ex) {
                    contenido.append("Filtrado por área ID: ").append(idArea).append("\n");
                }
            } else {
                contenido.append("Mostrando todas las áreas\n");
            }
            for (int i = 0; i < 80; i++)
                contenido.append("=");
            contenido.append("\n\n");

            for (EquipoModel equipo : equipos) {
                contenido.append("Nombre: ").append(equipo.getNombre()).append("\n");
                contenido.append("Código INACIF: ")
                        .append(equipo.getCodigoInacif() != null ? equipo.getCodigoInacif() : "N/A").append("\n");
                contenido.append("Marca: ").append(equipo.getMarca() != null ? equipo.getMarca() : "N/A")
                        .append(" - Modelo: ").append(equipo.getModelo() != null ? equipo.getModelo() : "N/A")
                        .append("\n");
                contenido.append("Ubicación: ").append(equipo.getUbicacion() != null ? equipo.getUbicacion() : "N/A")
                        .append("\n");
                contenido.append("Área: ").append(equipo.getArea() != null ? equipo.getArea().getNombre() : "Sin área")
                        .append("\n");
                for (int i = 0; i < 80; i++)
                    contenido.append("-");
                contenido.append("\n");
            }

            contenido.append("\nTotal de equipos encontrados: ").append(equipos.size());

            byte[] bytes = contenido.toString().getBytes("UTF-8");

            return Response.ok(bytes)
                    .header("Content-Disposition", "attachment; filename=reporte_equipos.txt")
                    .header("Content-Type", "text/plain")
                    .build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al generar reporte de equipos PDF", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }

    /**
     * Genera reporte de equipos en Excel (CSV simplificado)
     */
    @GET
    @Path("/equipos/excel")
    @Produces("application/vnd.ms-excel")
    public Response generarReporteEquiposExcel(
            @QueryParam("fechaInicio") String fechaInicio,
            @QueryParam("fechaFin") String fechaFin,
            @QueryParam("idArea") Integer idArea) {

        try {
            LOGGER.info("Generando reporte de equipos en Excel");

            // Obtener equipos
            String jpql = "SELECT e FROM EquipoModel e LEFT JOIN FETCH e.area";
            if (idArea != null) {
                jpql += " WHERE e.idArea = :idArea";
            }

            javax.persistence.TypedQuery<EquipoModel> query = em.createQuery(jpql, EquipoModel.class);
            if (idArea != null) {
                query.setParameter("idArea", idArea);
            }
            List<EquipoModel> equipos = query.setMaxResults(100).getResultList();

            // Generar CSV
            StringBuilder csv = new StringBuilder();
            csv.append("ID,Nombre,Código INACIF,Marca,Modelo,N° Serie,Ubicación,Área\n");

            for (EquipoModel equipo : equipos) {
                csv.append(equipo.getIdEquipo()).append(",");
                csv.append("\"").append(equipo.getNombre()).append("\",");
                csv.append("\"").append(equipo.getCodigoInacif() != null ? equipo.getCodigoInacif() : "").append("\",");
                csv.append("\"").append(equipo.getMarca() != null ? equipo.getMarca() : "").append("\",");
                csv.append("\"").append(equipo.getModelo() != null ? equipo.getModelo() : "").append("\",");
                csv.append("\"").append(equipo.getNumeroSerie() != null ? equipo.getNumeroSerie() : "").append("\",");
                csv.append("\"").append(equipo.getUbicacion() != null ? equipo.getUbicacion() : "").append("\",");
                csv.append("\"").append(equipo.getArea() != null ? equipo.getArea().getNombre() : "").append("\"");
                csv.append("\n");
            }

            byte[] bytes = csv.toString().getBytes("UTF-8");

            return Response.ok(bytes)
                    .header("Content-Disposition", "attachment; filename=reporte_equipos.csv")
                    .header("Content-Type", "text/csv; charset=UTF-8")
                    .build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al generar reporte de equipos Excel", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }

    /**
     * Genera reporte de mantenimientos en PDF
     */
    @GET
    @Path("/mantenimientos/pdf")
    @Produces("text/plain")
    public Response generarReporteMantenimientosPDF(
            @QueryParam("fechaInicio") String fechaInicio,
            @QueryParam("fechaFin") String fechaFin,
            @QueryParam("idArea") Integer idArea) {

        try {
            LOGGER.info("Generando reporte de mantenimientos TXT");

            // Query de ejecuciones de mantenimiento
            String jpql = "SELECT e FROM EjecucionMantenimientoModel e LEFT JOIN FETCH e.equipo LEFT JOIN FETCH e.contrato WHERE 1=1";

            if (fechaInicio != null && !fechaInicio.isEmpty()) {
                jpql += " AND e.fechaEjecucion >= :fechaInicio";
            }
            if (fechaFin != null && !fechaFin.isEmpty()) {
                jpql += " AND e.fechaEjecucion <= :fechaFin";
            }

            javax.persistence.TypedQuery<EjecucionMantenimientoModel> query = em.createQuery(jpql,
                    EjecucionMantenimientoModel.class);

            if (fechaInicio != null && !fechaInicio.isEmpty()) {
                try {
                    Date inicio = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(fechaInicio.substring(0, 19));
                    query.setParameter("fechaInicio", inicio);
                } catch (Exception e) {
                    LOGGER.warning("Error parseando fechaInicio: " + e.getMessage());
                }
            }
            if (fechaFin != null && !fechaFin.isEmpty()) {
                try {
                    Date fin = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(fechaFin.substring(0, 19));
                    query.setParameter("fechaFin", fin);
                } catch (Exception e) {
                    LOGGER.warning("Error parseando fechaFin: " + e.getMessage());
                }
            }

            List<EjecucionMantenimientoModel> ejecuciones = query.setMaxResults(1000).getResultList();

            StringBuilder contenido = new StringBuilder();
            contenido.append("REPORTE DE MANTENIMIENTOS\n");
            contenido.append("Fecha de generación: ")
                    .append(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date())).append("\n");
            if (fechaInicio != null)
                contenido.append("Desde: ").append(fechaInicio.substring(0, 10)).append("\n");
            if (fechaFin != null)
                contenido.append("Hasta: ").append(fechaFin.substring(0, 10)).append("\n");
            for (int i = 0; i < 80; i++)
                contenido.append("=");
            contenido.append("\n\n");

            for (EjecucionMantenimientoModel ej : ejecuciones) {
                contenido.append("Equipo: ").append(ej.getEquipo() != null ? ej.getEquipo().getNombre() : "N/A")
                        .append("\n");
                contenido.append("Fecha: ")
                        .append(ej.getFechaEjecucion() != null
                                ? new SimpleDateFormat("dd/MM/yyyy").format(ej.getFechaEjecucion())
                                : "N/A")
                        .append("\n");
                contenido.append("Estado: ").append(ej.getEstado() != null ? ej.getEstado() : "N/A").append("\n");
                contenido.append("Contrato: ")
                        .append(ej.getContrato() != null ? ej.getContrato().getDescripcion() : "N/A").append("\n");
                for (int i = 0; i < 80; i++)
                    contenido.append("-");
                contenido.append("\n");
            }

            contenido.append("\nTotal de mantenimientos: ").append(ejecuciones.size());

            return Response.ok(contenido.toString().getBytes("UTF-8"))
                    .header("Content-Disposition", "attachment; filename=reporte_mantenimientos.txt")
                    .header("Content-Type", "text/plain")
                    .build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al generar reporte de mantenimientos", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }

    @GET
    @Path("/mantenimientos/excel")
    @Produces("text/csv")
    public Response generarReporteMantenimientosExcel(
            @QueryParam("fechaInicio") String fechaInicio,
            @QueryParam("fechaFin") String fechaFin,
            @QueryParam("idArea") Integer idArea) {

        try {
            // Query de ejecuciones
            String jpql = "SELECT e FROM EjecucionMantenimientoModel e LEFT JOIN FETCH e.equipo LEFT JOIN FETCH e.contrato WHERE 1=1";

            if (fechaInicio != null && !fechaInicio.isEmpty()) {
                jpql += " AND e.fechaEjecucion >= :fechaInicio";
            }
            if (fechaFin != null && !fechaFin.isEmpty()) {
                jpql += " AND e.fechaEjecucion <= :fechaFin";
            }

            javax.persistence.TypedQuery<EjecucionMantenimientoModel> query = em.createQuery(jpql,
                    EjecucionMantenimientoModel.class);

            if (fechaInicio != null && !fechaInicio.isEmpty()) {
                try {
                    Date inicio = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(fechaInicio.substring(0, 19));
                    query.setParameter("fechaInicio", inicio);
                } catch (Exception e) {
                }
            }
            if (fechaFin != null && !fechaFin.isEmpty()) {
                try {
                    Date fin = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(fechaFin.substring(0, 19));
                    query.setParameter("fechaFin", fin);
                } catch (Exception e) {
                }
            }

            List<EjecucionMantenimientoModel> ejecuciones = query.setMaxResults(1000).getResultList();

            StringBuilder csv = new StringBuilder();
            csv.append("ID,Equipo,Fecha Ejecución,Estado,Contrato\n");

            for (EjecucionMantenimientoModel ej : ejecuciones) {
                csv.append(ej.getIdEjecucion()).append(",");
                csv.append("\"").append(ej.getEquipo() != null ? ej.getEquipo().getNombre() : "").append("\",");
                csv.append("\"")
                        .append(ej.getFechaEjecucion() != null
                                ? new SimpleDateFormat("dd/MM/yyyy").format(ej.getFechaEjecucion())
                                : "")
                        .append("\",");
                csv.append("\"").append(ej.getEstado() != null ? ej.getEstado() : "").append("\",");
                csv.append("\"").append(ej.getContrato() != null ? ej.getContrato().getDescripcion() : "").append("\"");
                csv.append("\n");
            }

            return Response.ok(csv.toString().getBytes("UTF-8"))
                    .header("Content-Disposition", "attachment; filename=reporte_mantenimientos.csv")
                    .header("Content-Type", "text/csv")
                    .build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al generar reporte", e);
            return Response.serverError().build();
        }
    }

    // Reportes de Contratos
    @GET
    @Path("/contratos/pdf")
    @Produces("text/plain")
    public Response generarReporteContratosPDF(
            @QueryParam("fechaInicio") String fechaInicio,
            @QueryParam("fechaFin") String fechaFin,
            @QueryParam("idArea") Integer idArea) {
        try {
            List<ContratoModel> contratos = em
                    .createQuery("SELECT c FROM ContratoModel c LEFT JOIN FETCH c.proveedor", ContratoModel.class)
                    .setMaxResults(1000).getResultList();

            StringBuilder contenido = new StringBuilder();
            contenido.append("REPORTE DE CONTRATOS\n");
            contenido.append("Fecha: ").append(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()))
                    .append("\n");
            for (int i = 0; i < 80; i++)
                contenido.append("=");
            contenido.append("\n\n");

            for (ContratoModel c : contratos) {
                contenido.append("Descripción: ").append(c.getDescripcion() != null ? c.getDescripcion() : "N/A")
                        .append("\n");
                contenido.append("Proveedor: ").append(c.getProveedor() != null ? c.getProveedor().getNombre() : "N/A")
                        .append("\n");
                contenido.append("Fecha Inicio: ")
                        .append(c.getFechaInicio() != null
                                ? new SimpleDateFormat("dd/MM/yyyy").format(c.getFechaInicio())
                                : "N/A")
                        .append("\n");
                contenido.append("Fecha Fin: ").append(
                        c.getFechaFin() != null ? new SimpleDateFormat("dd/MM/yyyy").format(c.getFechaFin()) : "N/A")
                        .append("\n");
                for (int i = 0; i < 80; i++)
                    contenido.append("-");
                contenido.append("\n");
            }

            contenido.append("\nTotal: ").append(contratos.size());

            return Response.ok(contenido.toString().getBytes("UTF-8"))
                    .header("Content-Disposition", "attachment; filename=reporte_contratos.txt")
                    .header("Content-Type", "text/plain")
                    .build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/contratos/excel")
    @Produces("text/csv")
    public Response generarReporteContratosExcel(
            @QueryParam("fechaInicio") String fechaInicio,
            @QueryParam("fechaFin") String fechaFin,
            @QueryParam("idArea") Integer idArea) {
        try {
            List<ContratoModel> contratos = em
                    .createQuery("SELECT c FROM ContratoModel c LEFT JOIN FETCH c.proveedor", ContratoModel.class)
                    .setMaxResults(1000).getResultList();

            StringBuilder csv = new StringBuilder();
            csv.append("ID,Descripción,Proveedor,Fecha Inicio,Fecha Fin,Costo\n");

            for (ContratoModel c : contratos) {
                csv.append(c.getIdContrato()).append(",");
                csv.append("\"").append(c.getDescripcion() != null ? c.getDescripcion() : "").append("\",");
                csv.append("\"").append(c.getProveedor() != null ? c.getProveedor().getNombre() : "").append("\",");
                csv.append("\"").append(
                        c.getFechaInicio() != null ? new SimpleDateFormat("dd/MM/yyyy").format(c.getFechaInicio()) : "")
                        .append("\",");
                csv.append("\"").append(
                        c.getFechaFin() != null ? new SimpleDateFormat("dd/MM/yyyy").format(c.getFechaFin()) : "")
                        .append("\",");
                csv.append("\"").append("N/A").append("\"");
                csv.append("\n");
            }

            return Response.ok(csv.toString().getBytes("UTF-8"))
                    .header("Content-Disposition", "attachment; filename=reporte_contratos.csv")
                    .header("Content-Type", "text/csv")
                    .build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    // Reportes de Proveedores
    @GET
    @Path("/proveedores/pdf")
    @Produces("text/plain")
    public Response generarReporteProveedoresPDF() {
        try {
            List<ProveedorModel> proveedores = em.createQuery("SELECT p FROM ProveedorModel p", ProveedorModel.class)
                    .setMaxResults(1000).getResultList();

            StringBuilder contenido = new StringBuilder();
            contenido.append("REPORTE DE PROVEEDORES\n");
            contenido.append("Fecha: ").append(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()))
                    .append("\n");
            for (int i = 0; i < 80; i++)
                contenido.append("=");
            contenido.append("\n\n");

            for (ProveedorModel p : proveedores) {
                contenido.append("ID: ").append(p.getIdProveedor()).append("\n");
                contenido.append("Nombre: ").append(p.getNombre() != null ? p.getNombre() : "N/A").append("\n");
                for (int i = 0; i < 80; i++)
                    contenido.append("-");
                contenido.append("\n");
            }

            contenido.append("\nTotal: ").append(proveedores.size());

            return Response.ok(contenido.toString().getBytes("UTF-8"))
                    .header("Content-Disposition", "attachment; filename=reporte_proveedores.txt")
                    .header("Content-Type", "text/plain")
                    .build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/proveedores/excel")
    @Produces("text/csv")
    public Response generarReporteProveedoresExcel() {
        try {
            List<ProveedorModel> proveedores = em.createQuery("SELECT p FROM ProveedorModel p", ProveedorModel.class)
                    .setMaxResults(1000).getResultList();

            StringBuilder csv = new StringBuilder();
            csv.append("ID,Nombre\n");

            for (ProveedorModel p : proveedores) {
                csv.append(p.getIdProveedor()).append(",");
                csv.append("\"").append(p.getNombre() != null ? p.getNombre() : "").append("\"");
                csv.append("\n");
            }

            return Response.ok(csv.toString().getBytes("UTF-8"))
                    .header("Content-Disposition", "attachment; filename=reporte_proveedores.csv")
                    .header("Content-Type", "text/csv")
                    .build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    // Reportes de Programaciones
    @GET
    @Path("/programaciones/pdf")
    @Produces("text/plain")
    public Response generarReporteProgramacionesPDF() {
        try {
            List<ProgramacionMantenimientoModel> programaciones = em
                    .createQuery("SELECT p FROM ProgramacionMantenimientoModel p LEFT JOIN FETCH p.equipo",
                            ProgramacionMantenimientoModel.class)
                    .setMaxResults(1000).getResultList();

            StringBuilder contenido = new StringBuilder();
            contenido.append("REPORTE DE PROGRAMACIONES\n");
            contenido.append("Fecha: ").append(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()))
                    .append("\n");
            for (int i = 0; i < 80; i++)
                contenido.append("=");
            contenido.append("\n\n");

            for (ProgramacionMantenimientoModel p : programaciones) {
                contenido.append("Equipo: ").append(p.getEquipo() != null ? p.getEquipo().getNombre() : "N/A")
                        .append("\n");
                contenido.append("Frecuencia (días): ")
                        .append(p.getFrecuenciaDias() != null ? p.getFrecuenciaDias() : "N/A").append("\n");
                contenido.append("Próximo: ")
                        .append(p.getFechaProximoMantenimiento() != null
                                ? new SimpleDateFormat("dd/MM/yyyy").format(p.getFechaProximoMantenimiento())
                                : "N/A")
                        .append("\n");
                contenido.append("Activa: ").append(p.getActiva() != null && p.getActiva() ? "Sí" : "No").append("\n");
                for (int i = 0; i < 80; i++)
                    contenido.append("-");
                contenido.append("\n");
            }

            contenido.append("\nTotal: ").append(programaciones.size());

            return Response.ok(contenido.toString().getBytes("UTF-8"))
                    .header("Content-Disposition", "attachment; filename=reporte_programaciones.txt")
                    .header("Content-Type", "text/plain")
                    .build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/programaciones/excel")
    @Produces("text/csv")
    public Response generarReporteProgramacionesExcel() {
        try {
            List<ProgramacionMantenimientoModel> programaciones = em
                    .createQuery("SELECT p FROM ProgramacionMantenimientoModel p LEFT JOIN FETCH p.equipo",
                            ProgramacionMantenimientoModel.class)
                    .setMaxResults(1000).getResultList();

            StringBuilder csv = new StringBuilder();
            csv.append("ID,Equipo,Frecuencia (días),Último Mantenimiento,Próximo Mantenimiento,Activa\n");

            for (ProgramacionMantenimientoModel p : programaciones) {
                csv.append(p.getIdProgramacion()).append(",");
                csv.append("\"").append(p.getEquipo() != null ? p.getEquipo().getNombre() : "").append("\",");
                csv.append("\"").append(p.getFrecuenciaDias() != null ? p.getFrecuenciaDias() : "").append("\",");
                csv.append("\"")
                        .append(p.getFechaUltimoMantenimiento() != null
                                ? new SimpleDateFormat("dd/MM/yyyy").format(p.getFechaUltimoMantenimiento())
                                : "")
                        .append("\",");
                csv.append("\"")
                        .append(p.getFechaProximoMantenimiento() != null
                                ? new SimpleDateFormat("dd/MM/yyyy").format(p.getFechaProximoMantenimiento())
                                : "")
                        .append("\",");
                csv.append("\"").append(p.getActiva() != null && p.getActiva() ? "Sí" : "No").append("\"");
                csv.append("\n");
            }

            return Response.ok(csv.toString().getBytes("UTF-8"))
                    .header("Content-Disposition", "attachment; filename=reporte_programaciones.csv")
                    .header("Content-Type", "text/csv")
                    .build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    // Reportes de Tickets
    @GET
    @Path("/tickets/pdf")
    @Produces("text/plain")
    public Response generarReporteTicketsPDF() {
        try {
            List<TicketModel> tickets = em
                    .createQuery("SELECT t FROM TicketModel t LEFT JOIN FETCH t.equipo", TicketModel.class)
                    .setMaxResults(1000).getResultList();

            StringBuilder contenido = new StringBuilder();
            contenido.append("REPORTE DE TICKETS\n");
            contenido.append("Fecha: ").append(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()))
                    .append("\n");
            for (int i = 0; i < 80; i++)
                contenido.append("=");
            contenido.append("\n\n");

            for (TicketModel t : tickets) {
                contenido.append("ID: ").append(t.getId()).append("\n");
                contenido.append("Equipo: ").append(t.getEquipo() != null ? t.getEquipo().getNombre() : "N/A")
                        .append("\n");
                contenido.append("Fecha Creación: ")
                        .append(t.getFechaCreacion() != null
                                ? new SimpleDateFormat("dd/MM/yyyy").format(t.getFechaCreacion())
                                : "N/A")
                        .append("\n");
                contenido.append("Estado: ").append(t.getEstado() != null ? t.getEstado() : "N/A").append("\n");
                for (int i = 0; i < 80; i++)
                    contenido.append("-");
                contenido.append("\n");
            }

            contenido.append("\nTotal: ").append(tickets.size());

            return Response.ok(contenido.toString().getBytes("UTF-8"))
                    .header("Content-Disposition", "attachment; filename=reporte_tickets.txt")
                    .header("Content-Type", "text/plain")
                    .build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/tickets/excel")
    @Produces("text/csv")
    public Response generarReporteTicketsExcel() {
        try {
            List<TicketModel> tickets = em
                    .createQuery("SELECT t FROM TicketModel t LEFT JOIN FETCH t.equipo", TicketModel.class)
                    .setMaxResults(1000).getResultList();

            StringBuilder csv = new StringBuilder();
            csv.append("ID,Equipo,Fecha Creación,Estado\n");

            for (TicketModel t : tickets) {
                csv.append(t.getId()).append(",");
                csv.append("\"").append(t.getEquipo() != null ? t.getEquipo().getNombre() : "").append("\",");
                csv.append("\"")
                        .append(t.getFechaCreacion() != null
                                ? new SimpleDateFormat("dd/MM/yyyy").format(t.getFechaCreacion())
                                : "")
                        .append("\",");
                csv.append("\"").append(t.getEstado() != null ? t.getEstado() : "").append("\"");
                csv.append("\n");
            }

            return Response.ok(csv.toString().getBytes("UTF-8"))
                    .header("Content-Disposition", "attachment; filename=reporte_tickets.csv")
                    .header("Content-Type", "text/csv")
                    .build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }
}
