package usac.eps.controladores.mantenimientos;

import usac.eps.modelos.mantenimientos.*;
import usac.eps.seguridad.RequiresRole;

import javax.enterprise.context.RequestScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Controlador de reportes
 * 🔒 Solo ADMIN y SUPERVISOR pueden acceder a reportes
 */
@Path("/reportes")
@RequestScoped
@RequiresRole({ "ADMIN", "SUPERVISOR" }) // Proteger todo el controlador
public class ReportesController {

    private static final Logger LOGGER = Logger.getLogger(ReportesController.class.getName());
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private static final SimpleDateFormat DATE_PARSER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private static final Pattern CAMBIO_ESTADO_PATTERN = Pattern
            .compile("Estado cambiado de '([^']*)' a '([^']*)'", Pattern.CASE_INSENSITIVE);

    @PersistenceContext
    private EntityManager em;

    @GET
    @Path("/equipos-estado-tiempo/dashboard")
    @Produces(MediaType.APPLICATION_JSON)
    public Response obtenerDashboardEquiposEstadoTiempo(
            @QueryParam("fechaInicio") String fechaInicio,
            @QueryParam("fechaFin") String fechaFin,
            @QueryParam("idArea") Integer idArea,
            @QueryParam("idCategoria") Integer idCategoria,
            @QueryParam("agruparPor") @DefaultValue("area") String agruparPor) {
        try {
            AnalisisEstadoTiempoResponse response = calcularAnalisisEstadoTiempo(
                    fechaInicio,
                    fechaFin,
                    idArea,
                    idCategoria,
                    agruparPor);
            return Response.ok(response).build();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error al obtener dashboard de estado-tiempo", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al calcular dashboard: " + ex.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/equipos-estado-tiempo/pdf")
    @Produces("text/plain")
    public Response generarReporteEquiposEstadoTiempoTxt(
            @QueryParam("fechaInicio") String fechaInicio,
            @QueryParam("fechaFin") String fechaFin,
            @QueryParam("idArea") Integer idArea,
            @QueryParam("idCategoria") Integer idCategoria,
            @QueryParam("agruparPor") @DefaultValue("area") String agruparPor) {
        try {
            AnalisisEstadoTiempoResponse analisis = calcularAnalisisEstadoTiempo(
                    fechaInicio,
                    fechaFin,
                    idArea,
                    idCategoria,
                    agruparPor);

            StringBuilder contenido = new StringBuilder();
            contenido.append("REPORTE DE EQUIPOS POR ")
                    .append("categoria".equalsIgnoreCase(analisis.agrupadoPor) ? "CATEGORIA" : "AREA")
                    .append("\n");
            contenido.append("Fecha de generación: ").append(SDF.format(new Date())).append("\n");
            contenido.append("Rango: ").append(analisis.fechaInicio).append(" -> ").append(analisis.fechaFin)
                    .append("\n");
            contenido.append("Criterio operación: estado ACTIVO\n");
            contenido.append("\nGLOSARIO\n");
            contenido.append("- Horas Operación (Activo): tiempo con estado ACTIVO dentro del rango.\n");
            contenido.append("- Horas Crítico: tiempo con estado CRITICO dentro del rango.\n");
            contenido.append("- Horas Inactivo: tiempo con estado INACTIVO dentro del rango.\n");
            contenido.append("- Porcentajes: se calculan sobre la ventana de análisis del equipo dentro del rango.\n");
            contenido.append("- Nota: los porcentajes pueden no sumar 100% si existen estados no clasificados.\n");
            for (int i = 0; i < 100; i++) {
                contenido.append("=");
            }
            contenido.append("\n\nRESUMEN POR GRUPO\n");

            for (ResumenGrupoDTO grupo : analisis.resumenPorGrupo) {
                contenido.append("Grupo: ").append(grupo.grupo).append("\n");
                contenido.append("Equipos: ").append(grupo.totalEquipos).append("\n");
                contenido.append("Horas Operación (Activo): ").append(formatearDecimal(grupo.horasOperacion))
                        .append("\n");
                contenido.append("Horas Crítico: ").append(formatearDecimal(grupo.horasCritico)).append("\n");
                contenido.append("Horas Inactivo: ").append(formatearDecimal(grupo.horasInactivo)).append("\n");
                contenido.append("% Operación: ").append(formatearDecimal(grupo.porcentajeOperacion)).append("%\n");
                contenido.append("% Crítico: ").append(formatearDecimal(grupo.porcentajeCritico)).append("%\n");
                contenido.append("% Inactivo: ").append(formatearDecimal(grupo.porcentajeInactivo)).append("%\n");
                for (int i = 0; i < 100; i++) {
                    contenido.append("-");
                }
                contenido.append("\n");
            }

            contenido.append("\nDETALLE POR EQUIPO\n");
            for (DetalleEquipoEstadoTiempoDTO detalle : analisis.detalleEquipos) {
                contenido.append("Equipo: ").append(detalle.nombreEquipo).append(" (ID: ").append(detalle.idEquipo)
                        .append(")\n");
                contenido.append("Área: ").append(detalle.areaNombre).append(" | Categoría: ")
                        .append(detalle.categoriaNombre)
                        .append("\n");
                contenido.append("Estado base: ").append(detalle.estadoBase).append("\n");
                contenido.append("Horas Operación (Activo): ").append(formatearDecimal(detalle.horasOperacion))
                        .append("\n");
                contenido.append("Horas Crítico: ").append(formatearDecimal(detalle.horasCritico)).append("\n");
                contenido.append("Horas Inactivo: ").append(formatearDecimal(detalle.horasInactivo)).append("\n");
                contenido.append("% Operación: ").append(formatearDecimal(detalle.porcentajeOperacion)).append("%\n");
                contenido.append("% Crítico: ").append(formatearDecimal(detalle.porcentajeCritico)).append("%\n");
                contenido.append("% Inactivo: ").append(formatearDecimal(detalle.porcentajeInactivo)).append("%\n");
                for (int i = 0; i < 100; i++) {
                    contenido.append("-");
                }
                contenido.append("\n");
            }

            return Response.ok(contenido.toString().getBytes("UTF-8"))
                    .header("Content-Disposition", "attachment; filename=reporte_equipos_estado_tiempo.txt")
                    .header("Content-Type", "text/plain")
                    .build();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error al generar reporte TXT de estado-tiempo", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al generar reporte: " + ex.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/equipos-estado-tiempo/excel")
    @Produces("text/csv")
    public Response generarReporteEquiposEstadoTiempoCsv(
            @QueryParam("fechaInicio") String fechaInicio,
            @QueryParam("fechaFin") String fechaFin,
            @QueryParam("idArea") Integer idArea,
            @QueryParam("idCategoria") Integer idCategoria,
            @QueryParam("agruparPor") @DefaultValue("area") String agruparPor) {
        try {
            AnalisisEstadoTiempoResponse analisis = calcularAnalisisEstadoTiempo(
                    fechaInicio,
                    fechaFin,
                    idArea,
                    idCategoria,
                    agruparPor);

            StringBuilder csv = new StringBuilder();
            csv.append(
                    "Tipo de Registro,Grupo de Resumen,ID Equipo,Nombre Equipo,Área,Categoría,Estado Inicial en Rango,Horas en Operación (Activo),Horas en Crítico,Horas en Inactivo,Porcentaje en Operación (Activo),Porcentaje en Crítico,Porcentaje en Inactivo\n");
            csv.append(
                    "GLOSARIO,Operación=ACTIVO / Crítico=CRITICO / Inactivo=INACTIVO,,,,,Porcentajes calculados sobre la ventana de análisis del equipo,,,,,,\n");

            for (ResumenGrupoDTO grupo : analisis.resumenPorGrupo) {
                csv.append("RESUMEN,")
                        .append(escapeCsv(grupo.grupo)).append(",,,,")
                        .append(",,")
                        .append(formatearDecimal(grupo.horasOperacion)).append(",")
                        .append(formatearDecimal(grupo.horasCritico)).append(",")
                        .append(formatearDecimal(grupo.horasInactivo)).append(",")
                        .append(formatearDecimal(grupo.porcentajeOperacion)).append(",")
                        .append(formatearDecimal(grupo.porcentajeCritico)).append(",")
                        .append(formatearDecimal(grupo.porcentajeInactivo))
                        .append("\n");
            }

            for (DetalleEquipoEstadoTiempoDTO detalle : analisis.detalleEquipos) {
                csv.append("DETALLE,")
                        .append(escapeCsv("categoria".equalsIgnoreCase(analisis.agrupadoPor)
                                ? detalle.categoriaNombre
                                : detalle.areaNombre))
                        .append(",")
                        .append(detalle.idEquipo).append(",")
                        .append(escapeCsv(detalle.nombreEquipo)).append(",")
                        .append(escapeCsv(detalle.areaNombre)).append(",")
                        .append(escapeCsv(detalle.categoriaNombre)).append(",")
                        .append(escapeCsv(detalle.estadoBase)).append(",")
                        .append(formatearDecimal(detalle.horasOperacion)).append(",")
                        .append(formatearDecimal(detalle.horasCritico)).append(",")
                        .append(formatearDecimal(detalle.horasInactivo)).append(",")
                        .append(formatearDecimal(detalle.porcentajeOperacion)).append(",")
                        .append(formatearDecimal(detalle.porcentajeCritico)).append(",")
                        .append(formatearDecimal(detalle.porcentajeInactivo))
                        .append("\n");
            }

            return Response.ok(csv.toString().getBytes("UTF-8"))
                    .header("Content-Disposition", "attachment; filename=reporte_equipos_estado_tiempo.csv")
                    .header("Content-Type", "text/csv; charset=UTF-8")
                    .build();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error al generar reporte CSV de estado-tiempo", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al generar reporte: " + ex.getMessage()))
                    .build();
        }
    }

    private AnalisisEstadoTiempoResponse calcularAnalisisEstadoTiempo(
            String fechaInicioRaw,
            String fechaFinRaw,
            Integer idArea,
            Integer idCategoria,
            String agruparPorRaw) {

        Date fechaFin = parseFechaFlexible(fechaFinRaw);
        if (fechaFin == null) {
            fechaFin = new Date();
        }

        Date fechaInicio = parseFechaFlexible(fechaInicioRaw);
        if (fechaInicio == null) {
            fechaInicio = Date.from(fechaFin.toInstant().minus(30, ChronoUnit.DAYS));
        }

        if (fechaInicio.after(fechaFin)) {
            Date temp = fechaInicio;
            fechaInicio = fechaFin;
            fechaFin = temp;
        }

        String agruparPor = "categoria".equalsIgnoreCase(agruparPorRaw) ? "categoria" : "area";

        String jpqlEquipos = "SELECT e FROM EquipoModel e " +
                "LEFT JOIN FETCH e.area a " +
                "LEFT JOIN FETCH e.categoria c " +
                "WHERE 1=1";

        if (idArea != null) {
            jpqlEquipos += " AND e.idArea = :idArea";
        }
        if (idCategoria != null) {
            jpqlEquipos += " AND e.idCategoria = :idCategoria";
        }

        javax.persistence.TypedQuery<EquipoModel> queryEquipos = em.createQuery(jpqlEquipos, EquipoModel.class);
        if (idArea != null) {
            queryEquipos.setParameter("idArea", idArea);
        }
        if (idCategoria != null) {
            queryEquipos.setParameter("idCategoria", idCategoria);
        }

        List<EquipoModel> equipos = queryEquipos.setMaxResults(5000).getResultList();
        List<DetalleEquipoEstadoTiempoDTO> detalleEquipos = new ArrayList<>();
        Map<String, ResumenGrupoDTO> resumenPorGrupo = new HashMap<>();

        long duracionTotalMs = Math.max(1L, fechaFin.getTime() - fechaInicio.getTime());

        for (EquipoModel equipo : equipos) {
            Date inicioEquipo = fechaInicio;
            if (equipo.getFechaCreacion() != null && equipo.getFechaCreacion().after(inicioEquipo)) {
                inicioEquipo = equipo.getFechaCreacion();
            }

            if (!inicioEquipo.before(fechaFin)) {
                continue;
            }

            List<HistorialEquipoModel> cambiosHastaFin = em.createQuery(
                    "SELECT h FROM HistorialEquipoModel h " +
                            "WHERE h.equipo.idEquipo = :idEquipo " +
                            "AND h.tipoCambio = 'CAMBIO_ESTADO' " +
                            "AND h.fechaRegistro <= :fechaFin " +
                            "ORDER BY h.fechaRegistro ASC",
                    HistorialEquipoModel.class)
                    .setParameter("idEquipo", equipo.getIdEquipo())
                    .setParameter("fechaFin", fechaFin)
                    .setMaxResults(5000)
                    .getResultList();

            EstadoTimeline timeline = calcularTimelineEquipo(equipo, cambiosHastaFin, inicioEquipo, fechaFin);
            double horasOperacion = timeline.millisOperacion / 3600000.0;
            double horasCritico = timeline.millisCritico / 3600000.0;
            double horasInactivo = timeline.millisInactivo / 3600000.0;
            long duracionVentanaEquipoMs = Math.max(1L, fechaFin.getTime() - inicioEquipo.getTime());
            double porcentajeOperacion = (timeline.millisOperacion * 100.0) / duracionVentanaEquipoMs;
            double porcentajeCritico = (timeline.millisCritico * 100.0) / duracionVentanaEquipoMs;
            double porcentajeInactivo = (timeline.millisInactivo * 100.0) / duracionVentanaEquipoMs;

            String areaNombre = resolverNombreArea(equipo);
            String categoriaNombre = resolverNombreCategoria(equipo);

            DetalleEquipoEstadoTiempoDTO detalle = new DetalleEquipoEstadoTiempoDTO();
            detalle.idEquipo = equipo.getIdEquipo();
            detalle.nombreEquipo = equipo.getNombre() != null ? equipo.getNombre() : "Sin nombre";
            detalle.areaNombre = areaNombre;
            detalle.categoriaNombre = categoriaNombre;
            detalle.estadoBase = timeline.estadoInicial;
            detalle.horasOperacion = redondear(horasOperacion);
            detalle.horasCritico = redondear(horasCritico);
            detalle.horasInactivo = redondear(horasInactivo);
            detalle.porcentajeOperacion = redondear(porcentajeOperacion);
            detalle.porcentajeCritico = redondear(porcentajeCritico);
            detalle.porcentajeInactivo = redondear(porcentajeInactivo);
            detalleEquipos.add(detalle);

            String claveGrupo = "categoria".equalsIgnoreCase(agruparPor) ? categoriaNombre : areaNombre;
            ResumenGrupoDTO resumen = resumenPorGrupo.computeIfAbsent(claveGrupo, k -> {
                ResumenGrupoDTO nuevo = new ResumenGrupoDTO();
                nuevo.grupo = k;
                return nuevo;
            });

            resumen.totalEquipos += 1;
            resumen.horasOperacion += detalle.horasOperacion;
            resumen.horasCritico += detalle.horasCritico;
            resumen.horasInactivo += detalle.horasInactivo;
            resumen.porcentajeOperacion += detalle.porcentajeOperacion;
            resumen.porcentajeCritico += detalle.porcentajeCritico;
            resumen.porcentajeInactivo += detalle.porcentajeInactivo;
        }

        List<ResumenGrupoDTO> resumenOrdenado = new ArrayList<>(resumenPorGrupo.values());
        for (ResumenGrupoDTO resumen : resumenOrdenado) {
            if (resumen.totalEquipos > 0) {
                resumen.porcentajeOperacion = redondear(resumen.porcentajeOperacion / resumen.totalEquipos);
                resumen.porcentajeCritico = redondear(resumen.porcentajeCritico / resumen.totalEquipos);
                resumen.porcentajeInactivo = redondear(resumen.porcentajeInactivo / resumen.totalEquipos);
            }
            resumen.horasOperacion = redondear(resumen.horasOperacion);
            resumen.horasCritico = redondear(resumen.horasCritico);
            resumen.horasInactivo = redondear(resumen.horasInactivo);
        }

        resumenOrdenado.sort(Comparator.comparing(r -> r.grupo == null ? "" : r.grupo));
        detalleEquipos.sort(Comparator.comparing(d -> d.nombreEquipo == null ? "" : d.nombreEquipo));

        AnalisisEstadoTiempoResponse response = new AnalisisEstadoTiempoResponse();
        response.agrupadoPor = agruparPor;
        response.fechaInicio = toIsoString(fechaInicio);
        response.fechaFin = toIsoString(fechaFin);
        response.totalEquipos = detalleEquipos.size();
        response.definiciones = Map.of(
                "horasOperacion", "Tiempo con estado ACTIVO dentro del rango de análisis.",
                "horasCritico", "Tiempo con estado CRITICO dentro del rango de análisis.",
                "horasInactivo", "Tiempo con estado INACTIVO dentro del rango de análisis.",
                "porcentajes", "Porcentajes calculados sobre la ventana de análisis del equipo.",
                "nota", "Los porcentajes pueden no sumar 100% si hay estados no clasificados.");
        response.resumenPorGrupo = resumenOrdenado;
        response.detalleEquipos = detalleEquipos;
        return response;
    }

    private String resolverNombreArea(EquipoModel equipo) {
        if (equipo.getArea() != null && equipo.getArea().getNombre() != null
                && !equipo.getArea().getNombre().trim().isEmpty()) {
            return equipo.getArea().getNombre();
        }

        if (equipo.getIdArea() != null) {
            try {
                AreaModel area = em.find(AreaModel.class, equipo.getIdArea());
                if (area != null && area.getNombre() != null && !area.getNombre().trim().isEmpty()) {
                    return area.getNombre();
                }
            } catch (Exception ex) {
                LOGGER.log(Level.FINE, "No se pudo resolver nombre de área para equipo " + equipo.getIdEquipo(), ex);
            }
        }

        return "Sin área";
    }

    private String resolverNombreCategoria(EquipoModel equipo) {
        if (equipo.getCategoria() != null && equipo.getCategoria().getNombre() != null
                && !equipo.getCategoria().getNombre().trim().isEmpty()) {
            return equipo.getCategoria().getNombre();
        }

        if (equipo.getIdCategoria() != null) {
            try {
                CategoriaEquipoModel categoria = em.find(CategoriaEquipoModel.class, equipo.getIdCategoria());
                if (categoria != null && categoria.getNombre() != null && !categoria.getNombre().trim().isEmpty()) {
                    return categoria.getNombre();
                }
            } catch (Exception ex) {
                LOGGER.log(Level.FINE,
                        "No se pudo resolver nombre de categoría para equipo " + equipo.getIdEquipo(),
                        ex);
            }
        }

        return "Sin categoría";
    }

    private EstadoTimeline calcularTimelineEquipo(
            EquipoModel equipo,
            List<HistorialEquipoModel> cambiosHastaFin,
            Date fechaInicio,
            Date fechaFin) {

        EstadoTimeline timeline = new EstadoTimeline();
        String estadoInicial = resolverEstadoInicial(equipo, cambiosHastaFin, fechaInicio);
        timeline.estadoInicial = estadoInicial;

        Date cursor = fechaInicio;
        String estadoActual = estadoInicial;

        for (HistorialEquipoModel cambio : cambiosHastaFin) {
            if (cambio.getFechaRegistro() == null || cambio.getFechaRegistro().before(fechaInicio)) {
                continue;
            }

            if (cambio.getFechaRegistro().after(fechaFin)) {
                break;
            }

            long tramo = Math.max(0L, cambio.getFechaRegistro().getTime() - cursor.getTime());
            acumularPorEstado(timeline, estadoActual, tramo);

            EstadoCambioParseado parse = parseCambioEstado(cambio.getDescripcion());
            if (parse.estadoNuevo != null) {
                estadoActual = parse.estadoNuevo;
            }
            cursor = cambio.getFechaRegistro();
        }

        long tramoFinal = Math.max(0L, fechaFin.getTime() - cursor.getTime());
        acumularPorEstado(timeline, estadoActual, tramoFinal);
        return timeline;
    }

    private String resolverEstadoInicial(EquipoModel equipo, List<HistorialEquipoModel> cambiosHastaFin,
            Date fechaInicio) {
        String estadoEquipo = normalizarEstado(equipo.getEstado());

        HistorialEquipoModel ultimoAntesInicio = null;
        HistorialEquipoModel primeroDesdeInicio = null;

        for (HistorialEquipoModel cambio : cambiosHastaFin) {
            if (cambio.getFechaRegistro() == null) {
                continue;
            }
            if (!cambio.getFechaRegistro().after(fechaInicio)) {
                ultimoAntesInicio = cambio;
            } else if (primeroDesdeInicio == null) {
                primeroDesdeInicio = cambio;
            }
        }

        if (ultimoAntesInicio != null) {
            EstadoCambioParseado parse = parseCambioEstado(ultimoAntesInicio.getDescripcion());
            if (parse.estadoNuevo != null) {
                return parse.estadoNuevo;
            }
        }

        if (primeroDesdeInicio != null) {
            EstadoCambioParseado parse = parseCambioEstado(primeroDesdeInicio.getDescripcion());
            if (parse.estadoAnterior != null) {
                return parse.estadoAnterior;
            }
        }

        return estadoEquipo;
    }

    private void acumularPorEstado(EstadoTimeline timeline, String estado, long millis) {
        String normalizado = normalizarEstado(estado);
        if ("ACTIVO".equals(normalizado)) {
            timeline.millisOperacion += millis;
        }
        if ("CRITICO".equals(normalizado)) {
            timeline.millisCritico += millis;
        }
        if ("INACTIVO".equals(normalizado)) {
            timeline.millisInactivo += millis;
        }
    }

    private EstadoCambioParseado parseCambioEstado(String descripcion) {
        EstadoCambioParseado parseado = new EstadoCambioParseado();
        if (descripcion == null || descripcion.trim().isEmpty()) {
            return parseado;
        }

        Matcher matcher = CAMBIO_ESTADO_PATTERN.matcher(descripcion);
        if (matcher.find()) {
            parseado.estadoAnterior = normalizarEstado(matcher.group(1));
            parseado.estadoNuevo = normalizarEstado(matcher.group(2));
            return parseado;
        }

        String upper = descripcion.toUpperCase(Locale.ROOT);
        if (upper.contains("CRITICO") || upper.contains("CRÍTICO")) {
            parseado.estadoNuevo = "CRITICO";
        } else if (upper.contains("ACTIVO")) {
            parseado.estadoNuevo = "ACTIVO";
        } else if (upper.contains("INACTIVO")) {
            parseado.estadoNuevo = "INACTIVO";
        }
        return parseado;
    }

    private String normalizarEstado(String estado) {
        if (estado == null) {
            return "DESCONOCIDO";
        }
        String texto = estado.trim().toUpperCase(Locale.ROOT)
                .replace("Á", "A")
                .replace("É", "E")
                .replace("Í", "I")
                .replace("Ó", "O")
                .replace("Ú", "U");
        if ("CRÍTICO".equals(texto)) {
            return "CRITICO";
        }
        return texto;
    }

    private Date parseFechaFlexible(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String raw = value.trim();
        try {
            return Date.from(Instant.parse(raw));
        } catch (DateTimeParseException ignored) {
        }

        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

        for (DateTimeFormatter formatter : formatters) {
            try {
                LocalDateTime dt = LocalDateTime.parse(raw, formatter);
                return Date.from(dt.atZone(ZoneId.systemDefault()).toInstant());
            } catch (DateTimeParseException ignored) {
            }
        }

        try {
            LocalDate date = LocalDate.parse(raw, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (DateTimeParseException ignored) {
        }

        try {
            return DATE_PARSER.parse(raw.length() >= 19 ? raw.substring(0, 19) : raw);
        } catch (ParseException ignored) {
            return null;
        }
    }

    private String toIsoString(Date date) {
        return date.toInstant().toString();
    }

    private double redondear(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private String formatearDecimal(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private String escapeCsv(String value) {
        String limpio = value == null ? "" : value.replace("\"", "\"\"");
        return "\"" + limpio + "\"";
    }

    private static class EstadoTimeline {
        private String estadoInicial;
        private long millisOperacion;
        private long millisCritico;
        private long millisInactivo;
    }

    private static class EstadoCambioParseado {
        private String estadoAnterior;
        private String estadoNuevo;
    }

    public static class DetalleEquipoEstadoTiempoDTO {
        public Integer idEquipo;
        public String nombreEquipo;
        public String areaNombre;
        public String categoriaNombre;
        public String estadoBase;
        public double horasOperacion;
        public double horasCritico;
        public double horasInactivo;
        public double porcentajeOperacion;
        public double porcentajeCritico;
        public double porcentajeInactivo;
    }

    public static class ResumenGrupoDTO {
        public String grupo;
        public int totalEquipos;
        public double horasOperacion;
        public double horasCritico;
        public double horasInactivo;
        public double porcentajeOperacion;
        public double porcentajeCritico;
        public double porcentajeInactivo;
    }

    public static class AnalisisEstadoTiempoResponse {
        public String agrupadoPor;
        public String fechaInicio;
        public String fechaFin;
        public int totalEquipos;
        public Map<String, String> definiciones;
        public List<ResumenGrupoDTO> resumenPorGrupo;
        public List<DetalleEquipoEstadoTiempoDTO> detalleEquipos;
    }

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
                    LOGGER.log(Level.WARNING, "Error al obtener área para reporte", ex);
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
                    LOGGER.log(Level.WARNING, "Error al parsear fechaInicio", e);
                }
            }
            if (fechaFin != null && !fechaFin.isEmpty()) {
                try {
                    Date fin = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(fechaFin.substring(0, 19));
                    query.setParameter("fechaFin", fin);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error al parsear fechaFin", e);
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
            LOGGER.log(Level.SEVERE, "Error al generar reporte de contratos PDF", e);
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
            LOGGER.log(Level.SEVERE, "Error al generar reporte de contratos Excel", e);
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
            LOGGER.log(Level.SEVERE, "Error al generar reporte de proveedores PDF", e);
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
            LOGGER.log(Level.SEVERE, "Error al generar reporte de proveedores Excel", e);
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
            LOGGER.log(Level.SEVERE, "Error al generar reporte de programaciones PDF", e);
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
            LOGGER.log(Level.SEVERE, "Error al generar reporte de programaciones Excel", e);
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
            LOGGER.log(Level.SEVERE, "Error al generar reporte de tickets PDF", e);
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
            LOGGER.log(Level.SEVERE, "Error al generar reporte de tickets Excel", e);
            return Response.serverError().build();
        }
    }
}
