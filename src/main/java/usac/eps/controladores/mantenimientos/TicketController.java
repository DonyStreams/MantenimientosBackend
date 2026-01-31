package usac.eps.controladores.mantenimientos;

import usac.eps.modelos.mantenimientos.TicketModel;
import usac.eps.repositorios.mantenimientos.TicketRepository;
import usac.eps.servicios.mantenimientos.BitacoraService;
import usac.eps.servicios.mantenimientos.EmailService;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Path("/tickets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class TicketController {

    @Inject
    private TicketRepository ticketRepository;

    @Inject
    private BitacoraService bitacoraService;

    @Inject
    private EmailService emailService;

    @PersistenceContext(unitName = "usac.eps_ControlSuministros")
    private EntityManager em;

    private static final String EVIDENCIAS_DIR = System.getProperty("user.home") + File.separator + "inacif-evidencias"
            + File.separator + "tickets";
    private static final String[] ALLOWED_EXTENSIONS = { ".jpg", ".jpeg", ".png", ".gif", ".webp", ".pdf",
            ".doc", ".docx", ".xls", ".xlsx" };

    // ===============================
    // CRUD BÁSICO DE TICKETS
    // ===============================

    @GET
    public Response getAll() {
        try {
            System.out.println("📋 Obteniendo todos los tickets");

            String sql = "SELECT t.id, t.descripcion, t.prioridad, t.estado, " +
                    "t.fecha_creacion, t.fecha_modificacion, t.fecha_cierre, " +
                    "e.id_equipo as equipo_id, e.nombre as equipo_nombre, e.codigo_inacif as equipo_codigo, " +
                    "uc.id as usuario_creador_id, uc.nombre_completo as usuario_creador, " +
                    "ua.id as usuario_asignado_id, ua.nombre_completo as usuario_asignado " +
                    "FROM Tickets t " +
                    "INNER JOIN Equipos e ON t.equipo_id = e.id_equipo " +
                    "LEFT JOIN Usuarios uc ON t.usuario_creador_id = uc.id " +
                    "LEFT JOIN Usuarios ua ON t.usuario_asignado_id = ua.id " +
                    "WHERE t.estado != 'Inactivo' " +
                    "ORDER BY t.fecha_creacion DESC";

            @SuppressWarnings("unchecked")
            List<Object[]> resultados = em.createNativeQuery(sql).getResultList();

            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{\"tickets\": [");

            for (int i = 0; i < resultados.size(); i++) {
                Object[] row = resultados.get(i);
                if (i > 0)
                    jsonBuilder.append(",");

                jsonBuilder.append("{")
                        .append("\"id\": ").append(row[0]).append(",")
                        .append("\"descripcion\": \"")
                        .append(row[1] != null ? row[1].toString().replace("\"", "\\\"") : "").append("\",")
                        .append("\"prioridad\": \"").append(row[2] != null ? row[2] : "").append("\",")
                        .append("\"estado\": \"").append(row[3] != null ? row[3] : "").append("\",")
                        .append("\"fechaCreacion\": \"").append(row[4] != null ? row[4] : "").append("\",")
                        .append("\"fechaModificacion\": \"").append(row[5] != null ? row[5] : "").append("\",")
                        .append("\"fechaCierre\": \"").append(row[6] != null ? row[6] : "").append("\",")
                        .append("\"equipoId\": ").append(row[7] != null ? row[7] : "null").append(",")
                        .append("\"equipoNombre\": \"").append(row[8] != null ? row[8] : "").append("\",")
                        .append("\"equipoCodigo\": \"").append(row[9] != null ? row[9] : "").append("\",")
                        .append("\"usuarioCreadorId\": ").append(row[10] != null ? row[10] : "null").append(",")
                        .append("\"usuarioCreador\": \"").append(row[11] != null ? row[11] : "").append("\",")
                        .append("\"usuarioAsignadoId\": ").append(row[12] != null ? row[12] : "null").append(",")
                        .append("\"usuarioAsignado\": \"").append(row[13] != null ? row[13] : "").append("\"")
                        .append("}");
            }

            jsonBuilder.append("], \"total\": ").append(resultados.size()).append(", \"success\": true}");

            return Response.ok(jsonBuilder.toString()).build();

        } catch (Exception e) {
            System.out.println("❌ Error al obtener tickets: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al obtener tickets: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Integer id) {
        try {
            System.out.println("🔍 Obteniendo ticket por ID: " + id);

            TicketModel ticket = ticketRepository.findById(id);
            if (ticket == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Ticket no encontrado\"}")
                        .build();
            }

            // Construir respuesta con datos completos
            String sql = "SELECT t.id, t.descripcion, t.prioridad, t.estado, " +
                    "t.fecha_creacion, t.fecha_modificacion, t.fecha_cierre, " +
                    "e.nombre as equipo_nombre, e.codigo_inacif as equipo_codigo, e.id_equipo, " +
                    "uc.id as usuario_creador_id, uc.nombre_completo as usuario_creador, " +
                    "ua.id as usuario_asignado_id, ua.nombre_completo as usuario_asignado " +
                    "FROM Tickets t " +
                    "INNER JOIN Equipos e ON t.equipo_id = e.id_equipo " +
                    "LEFT JOIN Usuarios uc ON t.usuario_creador_id = uc.id " +
                    "LEFT JOIN Usuarios ua ON t.usuario_asignado_id = ua.id " +
                    "WHERE t.id = ?";

            Object[] row = (Object[]) em.createNativeQuery(sql)
                    .setParameter(1, id)
                    .getSingleResult();

            String jsonResponse = "{" +
                    "\"id\": " + row[0] + "," +
                    "\"descripcion\": \"" + (row[1] != null ? row[1].toString().replace("\"", "\\\"") : "") + "\"," +
                    "\"prioridad\": \"" + (row[2] != null ? row[2] : "") + "\"," +
                    "\"estado\": \"" + (row[3] != null ? row[3] : "") + "\"," +
                    "\"fechaCreacion\": \"" + (row[4] != null ? row[4] : "") + "\"," +
                    "\"fechaModificacion\": \"" + (row[5] != null ? row[5] : "") + "\"," +
                    "\"fechaCierre\": \"" + (row[6] != null ? row[6] : "") + "\"," +
                    "\"equipoNombre\": \"" + (row[7] != null ? row[7] : "") + "\"," +
                    "\"equipoCodigo\": \"" + (row[8] != null ? row[8] : "") + "\"," +
                    "\"equipoId\": " + (row[9] != null ? row[9] : "null") + "," +
                    "\"usuarioCreadorId\": " + (row[10] != null ? row[10] : "null") + "," +
                    "\"usuarioCreador\": \"" + (row[11] != null ? row[11] : "") + "\"," +
                    "\"usuarioAsignadoId\": " + (row[12] != null ? row[12] : "null") + "," +
                    "\"usuarioAsignado\": \"" + (row[13] != null ? row[13] : "") + "\"," +
                    "\"success\": true" +
                    "}";

            return Response.ok(jsonResponse).build();

        } catch (Exception e) {
            System.out.println("❌ Error al obtener ticket: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al obtener ticket: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @POST
    @Transactional
    public Response create(String jsonData) {
        try {
            System.out.println("📝 Creando nuevo ticket: " + jsonData);

            // Parsear JSON manualmente usando regex
            String descripcion = extractJsonValue(jsonData, "descripcion");
            String prioridad = extractJsonValue(jsonData, "prioridad");
            String estado = extractJsonValue(jsonData, "estado");
            String equipoIdStr = extractJsonValue(jsonData, "equipoId");
            String usuarioCreadorIdStr = extractJsonValue(jsonData, "usuarioCreadorId");
            String usuarioAsignadoIdStr = extractJsonValue(jsonData, "usuarioAsignadoId");

            // Valores por defecto
            if (prioridad == null || prioridad.isEmpty())
                prioridad = "Media";
            if (estado == null || estado.isEmpty())
                estado = "Abierto";

            // Validaciones básicas
            if (descripcion == null || descripcion.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"La descripción es obligatoria\", \"success\": false}")
                        .build();
            }

            int equipoId = 0;
            int usuarioCreadorId = 0;
            Integer usuarioAsignadoId = null;

            try {
                equipoId = Integer.parseInt(equipoIdStr);
                usuarioCreadorId = Integer.parseInt(usuarioCreadorIdStr);
                if (usuarioAsignadoIdStr != null && !usuarioAsignadoIdStr.isEmpty()
                        && !usuarioAsignadoIdStr.equals("null")) {
                    usuarioAsignadoId = Integer.parseInt(usuarioAsignadoIdStr);
                }
            } catch (NumberFormatException e) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"IDs deben ser números válidos\", \"success\": false}")
                        .build();
            }

            if (equipoId <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Debe especificar un equipo válido\", \"success\": false}")
                        .build();
            }

            if (usuarioCreadorId <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Debe especificar un usuario creador válido\", \"success\": false}")
                        .build();
            }

            // Crear el ticket usando SQL nativo
            String insertSQL = "INSERT INTO Tickets (descripcion, prioridad, estado, equipo_id, usuario_creador_id, usuario_asignado_id, fecha_creacion, usuario_creacion) "
                    +
                    "VALUES (?, ?, ?, ?, ?, ?, GETDATE(), ?)";

            Query insertQuery = em.createNativeQuery(insertSQL);
            insertQuery.setParameter(1, descripcion);
            insertQuery.setParameter(2, prioridad);
            insertQuery.setParameter(3, estado);
            insertQuery.setParameter(4, equipoId);
            insertQuery.setParameter(5, usuarioCreadorId);
            insertQuery.setParameter(6, usuarioAsignadoId);
            insertQuery.setParameter(7, usuarioCreadorId); // usuario_creacion

            int result = insertQuery.executeUpdate();

            if (result > 0) {
                // Obtener el ID del ticket recién creado
                String getIdSQL = "SELECT IDENT_CURRENT('Tickets')";
                Integer ticketId = ((Number) em.createNativeQuery(getIdSQL).getSingleResult()).intValue();

                // Obtener el nombre del usuario creador para la bitácora
                String usuarioCreadorNombre = "Sistema";
                try {
                    String getNombreSQL = "SELECT nombre_completo FROM Usuarios WHERE id = ?";
                    Object nombreResult = em.createNativeQuery(getNombreSQL)
                            .setParameter(1, usuarioCreadorId)
                            .getSingleResult();
                    if (nombreResult != null) {
                        usuarioCreadorNombre = nombreResult.toString();
                    }
                } catch (Exception e) {
                    System.out.println("⚠️ No se pudo obtener nombre del usuario creador");
                }

                // Registrar en bitácora
                String descripcionCorta = descripcion.length() > 50 ? descripcion.substring(0, 47) + "..."
                        : descripcion;
                bitacoraService.registrarTicketCreado(equipoId, ticketId, descripcionCorta, usuarioCreadorId,
                        usuarioCreadorNombre);

                System.out.println("✅ Ticket creado exitosamente");
                return Response.status(Response.Status.CREATED)
                        .entity("{\"message\": \"Ticket creado exitosamente\", \"success\": true}")
                        .build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("{\"error\": \"No se pudo crear el ticket\", \"success\": false}")
                        .build();
            }

        } catch (Exception e) {
            System.out.println("❌ Error al crear ticket: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al crear ticket: " + e.getMessage() + "\", \"success\": false}")
                    .build();
        }
    }

    // Método auxiliar para parsear JSON manualmente
    private String extractJsonValue(String json, String key) {
        try {
            String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]*)\"|\"" + key + "\"\\s*:\\s*([^,}\\s]+)";
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(json);
            if (m.find()) {
                String value1 = m.group(1);
                String value2 = m.group(2);
                return value1 != null ? value1 : value2;
            }
            return null;
        } catch (Exception e) {
            System.out.println("❌ Error extrayendo valor JSON para " + key + ": " + e.getMessage());
            return null;
        }
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") Integer id, String jsonData) {
        try {
            System.out.println("✏️ Actualizando ticket ID: " + id + " - Data: " + jsonData);

            if (jsonData == null || jsonData.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Datos de ticket requeridos\"}")
                        .build();
            }

            // Parseo manual del JSON (similar al POST)
            String descripcion = extractJsonValue(jsonData, "descripcion");
            String estado = extractJsonValue(jsonData, "estado");
            String prioridad = extractJsonValue(jsonData, "prioridad");
            String equipoIdStr = extractJsonValue(jsonData, "equipoId");
            String usuarioAsignadoIdStr = extractJsonValue(jsonData, "usuarioAsignadoId");
            String usuarioModificadorIdStr = extractJsonValue(jsonData, "usuarioModificadorId");
            String usuarioModificadorNombre = extractJsonValue(jsonData, "usuarioModificadorNombre");

            // Obtener el ID del usuario que modifica (por defecto 1 si no viene)
            Integer usuarioModificadorId = 1;
            if (usuarioModificadorIdStr != null && !usuarioModificadorIdStr.trim().isEmpty()) {
                try {
                    usuarioModificadorId = Integer.parseInt(usuarioModificadorIdStr);
                } catch (NumberFormatException e) {
                    System.out.println("⚠️ ID de usuario modificador inválido: " + usuarioModificadorIdStr);
                }
            }

            // Si no viene el nombre, obtenerlo de la BD
            if ((usuarioModificadorNombre == null || usuarioModificadorNombre.trim().isEmpty())
                    && usuarioModificadorId != null) {
                try {
                    String getNombreModSQL = "SELECT nombre_completo FROM Usuarios WHERE id = ?";
                    Object nombreResult = em.createNativeQuery(getNombreModSQL)
                            .setParameter(1, usuarioModificadorId)
                            .getSingleResult();
                    usuarioModificadorNombre = nombreResult != null ? nombreResult.toString() : "Sistema";
                } catch (Exception e) {
                    usuarioModificadorNombre = "Sistema";
                }
            }

            // Verificar que el ticket existe y obtener valores actuales para comparar
            String selectActualSQL = "SELECT estado, prioridad, equipo_id, usuario_asignado_id FROM Tickets WHERE id = ?";
            @SuppressWarnings("unchecked")
            java.util.List<Object[]> resultadosActuales = em.createNativeQuery(selectActualSQL)
                    .setParameter(1, id)
                    .getResultList();

            if (resultadosActuales.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Ticket no encontrado\"}")
                        .build();
            }

            Object[] datosActuales = resultadosActuales.get(0);
            String estadoActual = datosActuales[0] != null ? datosActuales[0].toString() : null;
            String prioridadActual = datosActuales[1] != null ? datosActuales[1].toString() : null;
            Integer equipoIdActual = datosActuales[2] != null ? ((Number) datosActuales[2]).intValue() : null;
            Integer usuarioAsignadoActual = datosActuales[3] != null ? ((Number) datosActuales[3]).intValue() : null;

            // Construir SQL de actualización dinámicamente
            StringBuilder updateSQL = new StringBuilder(
                    "UPDATE Tickets SET fecha_modificacion = GETDATE(), usuario_modificacion = 1");
            java.util.List<Object> parametros = new java.util.ArrayList<>();

            if (descripcion != null && !descripcion.trim().isEmpty()) {
                updateSQL.append(", descripcion = ?");
                parametros.add(descripcion);
            }

            if (estado != null && !estado.trim().isEmpty()) {
                updateSQL.append(", estado = ?");
                parametros.add(estado);
            }

            if (prioridad != null && !prioridad.trim().isEmpty()) {
                updateSQL.append(", prioridad = ?");
                parametros.add(prioridad);
            }

            Integer equipoIdNuevo = equipoIdActual;
            if (equipoIdStr != null && !equipoIdStr.trim().isEmpty()) {
                try {
                    equipoIdNuevo = Integer.parseInt(equipoIdStr);
                    updateSQL.append(", equipo_id = ?");
                    parametros.add(equipoIdNuevo);
                } catch (NumberFormatException e) {
                    System.out.println("⚠️ ID de equipo inválido: " + equipoIdStr);
                }
            }

            Integer usuarioAsignadoNuevo = usuarioAsignadoActual;
            if (usuarioAsignadoIdStr != null && !usuarioAsignadoIdStr.trim().isEmpty()
                    && !usuarioAsignadoIdStr.equals("null")) {
                try {
                    usuarioAsignadoNuevo = Integer.parseInt(usuarioAsignadoIdStr);
                    updateSQL.append(", usuario_asignado_id = ?");
                    parametros.add(usuarioAsignadoNuevo);
                } catch (NumberFormatException e) {
                    System.out.println("⚠️ ID de usuario asignado inválido: " + usuarioAsignadoIdStr);
                }
            }

            updateSQL.append(" WHERE id = ?");
            parametros.add(id);

            // Ejecutar actualización
            javax.persistence.Query query = em.createNativeQuery(updateSQL.toString());
            for (int i = 0; i < parametros.size(); i++) {
                query.setParameter(i + 1, parametros.get(i));
            }

            int updatedRows = query.executeUpdate();

            if (updatedRows > 0) {
                System.out.println("✅ Ticket actualizado exitosamente");

                // Registrar cambios en bitácora
                Integer equipoParaBitacora = equipoIdNuevo != null ? equipoIdNuevo : equipoIdActual;
                if (equipoParaBitacora != null) {
                    // Registrar cambio de estado
                    if (estado != null && !estado.trim().isEmpty() && !estado.equals(estadoActual)) {
                        bitacoraService.registrarTicketCambioEstado(equipoParaBitacora, id, estadoActual, estado,
                                usuarioModificadorId, usuarioModificadorNombre);
                    }

                    // Registrar cambio de prioridad
                    if (prioridad != null && !prioridad.trim().isEmpty() && !prioridad.equals(prioridadActual)) {
                        bitacoraService.registrarTicketCambioPrioridad(equipoParaBitacora, id, prioridadActual,
                                prioridad, usuarioModificadorId, usuarioModificadorNombre);

                        // 🚨 ENVIAR NOTIFICACIÓN SI EL TICKET CAMBIA A PRIORIDAD CRÍTICA
                        if (prioridad.equalsIgnoreCase("Crítica") || prioridad.equalsIgnoreCase("Criticaa")) {
                            try {
                                // Obtener información completa del ticket y equipo para el correo
                                String infoSQL = "SELECT t.descripcion, e.nombre, e.codigo_inacif, e.ubicacion, " +
                                        "ua.nombre_completo " +
                                        "FROM Tickets t " +
                                        "INNER JOIN Equipos e ON t.equipo_id = e.id_equipo " +
                                        "LEFT JOIN Usuarios ua ON t.usuario_asignado_id = ua.id " +
                                        "WHERE t.id = ?";

                                Object[] infoTicket = (Object[]) em.createNativeQuery(infoSQL)
                                        .setParameter(1, id)
                                        .getSingleResult();

                                String descripcionTicket = infoTicket[0] != null ? infoTicket[0].toString()
                                        : "Sin descripción";
                                String nombreEquipo = infoTicket[1] != null ? infoTicket[1].toString() : "Desconocido";
                                String codigoInacif = infoTicket[2] != null ? infoTicket[2].toString() : "N/A";
                                String ubicacionEquipo = infoTicket[3] != null ? infoTicket[3].toString()
                                        : "No especificada";
                                String usuarioAsig = infoTicket[4] != null ? infoTicket[4].toString() : "Sin asignar";

                                emailService.notificarTicketCritico(id, descripcionTicket, nombreEquipo,
                                        codigoInacif, usuarioAsig, ubicacionEquipo);

                                System.out.println("📧 Notificación de ticket crítico enviada - Ticket #" + id);
                            } catch (Exception emailEx) {
                                System.out.println(
                                        "⚠️ Error al enviar notificación de ticket crítico: " + emailEx.getMessage());
                                // No interrumpir el flujo si falla el correo
                            }
                        }
                    }

                    // Registrar asignación de usuario
                    if (usuarioAsignadoNuevo != null && !usuarioAsignadoNuevo.equals(usuarioAsignadoActual)) {
                        // Obtener nombre del usuario asignado
                        String getNombreSQL = "SELECT nombre_completo FROM Usuarios WHERE id = ?";
                        @SuppressWarnings("unchecked")
                        java.util.List<Object> nombresResult = em.createNativeQuery(getNombreSQL)
                                .setParameter(1, usuarioAsignadoNuevo)
                                .getResultList();
                        String nombreUsuario = !nombresResult.isEmpty() ? nombresResult.get(0).toString()
                                : "Usuario #" + usuarioAsignadoNuevo;
                        bitacoraService.registrarTicketAsignado(equipoParaBitacora, id, nombreUsuario,
                                usuarioModificadorId, usuarioModificadorNombre);
                    }
                }

                // Obtener el ticket actualizado
                String selectSQL = "SELECT t.id, t.descripcion, t.estado, t.prioridad, t.fecha_creacion, " +
                        "t.fecha_modificacion, e.nombre as equipo_nombre, " +
                        "uc.nombre_completo as usuario_creador, ua.nombre_completo as usuario_asignado " +
                        "FROM Tickets t " +
                        "LEFT JOIN Equipos e ON t.equipo_id = e.id_equipo " +
                        "LEFT JOIN Usuarios uc ON t.usuario_creador_id = uc.id " +
                        "LEFT JOIN Usuarios ua ON t.usuario_asignado_id = ua.id " +
                        "WHERE t.id = ?";

                Object[] result = (Object[]) em.createNativeQuery(selectSQL)
                        .setParameter(1, id)
                        .getSingleResult();

                // Construir respuesta JSON
                StringBuilder jsonBuilder = new StringBuilder();
                jsonBuilder.append("{");
                jsonBuilder.append("\"id\": ").append(result[0]).append(",");
                jsonBuilder.append("\"descripcion\": \"").append(result[1] != null ? result[1] : "").append("\",");
                jsonBuilder.append("\"estado\": \"").append(result[2] != null ? result[2] : "").append("\",");
                jsonBuilder.append("\"prioridad\": \"").append(result[3] != null ? result[3] : "").append("\",");
                jsonBuilder.append("\"fechaCreacion\": \"").append(result[4] != null ? result[4] : "").append("\",");
                jsonBuilder.append("\"fechaModificacion\": \"").append(result[5] != null ? result[5] : "")
                        .append("\",");
                jsonBuilder.append("\"equipoNombre\": \"").append(result[6] != null ? result[6] : "").append("\",");
                jsonBuilder.append("\"usuarioCreador\": \"").append(result[7] != null ? result[7] : "").append("\",");
                jsonBuilder.append("\"usuarioAsignado\": \"").append(result[8] != null ? result[8] : "").append("\"");
                jsonBuilder.append("}");

                return Response.ok(jsonBuilder.toString()).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("{\"error\": \"No se pudo actualizar el ticket\"}")
                        .build();
            }

        } catch (Exception e) {
            System.out.println("❌ Error al actualizar ticket: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al actualizar ticket: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Integer id) {
        try {
            System.out.println("🗑️ Eliminando ticket ID: " + id);

            // Primero eliminar el historial asociado al ticket
            String deleteHistorialSQL = "DELETE FROM Historial_Equipo WHERE ticket_id = ?";
            em.createNativeQuery(deleteHistorialSQL).setParameter(1, id).executeUpdate();
            System.out.println("   ✓ Historial del ticket eliminado");

            // Eliminar comentarios asociados
            String deleteComentariosSQL = "DELETE FROM Comentarios_Ticket WHERE ticket_id = ?";
            em.createNativeQuery(deleteComentariosSQL).setParameter(1, id).executeUpdate();
            System.out.println("   ✓ Comentarios del ticket eliminados");

            // Eliminar evidencias asociadas al ticket
            String deleteEvidenciasSQL = "DELETE FROM Evidencias WHERE entidad_relacionada = 'ticket' AND entidad_id = ?";
            em.createNativeQuery(deleteEvidenciasSQL).setParameter(1, id).executeUpdate();
            System.out.println("   ✓ Evidencias del ticket eliminadas");

            // Finalmente eliminar el ticket
            String deleteTicketSQL = "DELETE FROM Tickets WHERE id = ?";
            Query deleteQuery = em.createNativeQuery(deleteTicketSQL);
            deleteQuery.setParameter(1, id);

            int result = deleteQuery.executeUpdate();

            if (result > 0) {
                System.out.println("✅ Ticket eliminado correctamente");
                return Response.ok("{\"message\": \"Ticket eliminado correctamente\", \"success\": true}")
                        .build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Ticket no encontrado\", \"success\": false}")
                        .build();
            }

        } catch (Exception e) {
            System.out.println("❌ Error al eliminar ticket: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al eliminar ticket: " + e.getMessage() + "\", \"success\": false}")
                    .build();
        }
    }

    // ===============================
    // ENDPOINTS ESPECÍFICOS DE TICKETS
    // ===============================

    @GET
    @Path("/estadisticas")
    public Response getEstadisticas() {
        try {
            System.out.println("📊 Obteniendo estadísticas de tickets");

            // Estadísticas por estado
            String sqlEstados = "SELECT estado, COUNT(*) as total FROM Tickets GROUP BY estado";
            @SuppressWarnings("unchecked")
            List<Object[]> estadoResultados = em.createNativeQuery(sqlEstados).getResultList();

            // Estadísticas por prioridad
            String sqlPrioridades = "SELECT prioridad, COUNT(*) as total FROM Tickets GROUP BY prioridad";
            @SuppressWarnings("unchecked")
            List<Object[]> prioridadResultados = em.createNativeQuery(sqlPrioridades).getResultList();

            // Total de tickets
            String sqlTotal = "SELECT COUNT(*) FROM Tickets";
            Number totalTickets = (Number) em.createNativeQuery(sqlTotal).getSingleResult();

            // Construir respuesta JSON
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{\"estadisticas\": {");

            // Por estado
            jsonBuilder.append("\"porEstado\": [");
            for (int i = 0; i < estadoResultados.size(); i++) {
                Object[] row = estadoResultados.get(i);
                if (i > 0)
                    jsonBuilder.append(",");
                jsonBuilder.append("{\"estado\": \"").append(row[0]).append("\", \"total\": ").append(row[1])
                        .append("}");
            }
            jsonBuilder.append("],");

            // Por prioridad
            jsonBuilder.append("\"porPrioridad\": [");
            for (int i = 0; i < prioridadResultados.size(); i++) {
                Object[] row = prioridadResultados.get(i);
                if (i > 0)
                    jsonBuilder.append(",");
                jsonBuilder.append("{\"prioridad\": \"").append(row[0]).append("\", \"total\": ").append(row[1])
                        .append("}");
            }
            jsonBuilder.append("],");

            jsonBuilder.append("\"totalTickets\": ").append(totalTickets)
                    .append("}, \"success\": true}");

            return Response.ok(jsonBuilder.toString()).build();

        } catch (Exception e) {
            System.out.println("❌ Error al obtener estadísticas: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al obtener estadísticas: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @GET
    @Path("/{id}/comentarios")
    public Response getComentarios(@PathParam("id") Integer ticketId) {
        try {
            System.out.println("💬 Obteniendo comentarios para ticket: " + ticketId);

            String sql = "SELECT c.id, c.comentario, c.fecha_creacion, " +
                    "u.nombre_completo, tc.nombre as tipo_comentario, " +
                    "c.estado_anterior, c.estado_nuevo " +
                    "FROM Comentarios_Ticket c " +
                    "INNER JOIN Usuarios u ON c.usuario_id = u.id " +
                    "INNER JOIN Tipos_Comentario tc ON c.tipo_comentario_id = tc.id_tipo " +
                    "WHERE c.ticket_id = ? " +
                    "ORDER BY c.fecha_creacion DESC";

            @SuppressWarnings("unchecked")
            List<Object[]> resultados = em.createNativeQuery(sql)
                    .setParameter(1, ticketId)
                    .getResultList();

            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{\"comentarios\": [");

            for (int i = 0; i < resultados.size(); i++) {
                Object[] row = resultados.get(i);
                if (i > 0)
                    jsonBuilder.append(",");

                jsonBuilder.append("{")
                        .append("\"id\": ").append(row[0]).append(",")
                        .append("\"comentario\": \"")
                        .append(row[1] != null ? row[1].toString().replace("\"", "\\\"") : "").append("\",")
                        .append("\"fechaCreacion\": \"").append(row[2]).append("\",")
                        .append("\"usuario\": \"").append(row[3]).append("\",")
                        .append("\"tipoComentario\": \"").append(row[4]).append("\",")
                        .append("\"estadoAnterior\": ")
                        .append(row[5] != null ? "\"" + row[5] + "\"" : "null").append(",")
                        .append("\"estadoNuevo\": ")
                        .append(row[6] != null ? "\"" + row[6] + "\"" : "null")
                        .append("}");
            }

            jsonBuilder.append("], \"total\": ").append(resultados.size())
                    .append(", \"success\": true}");

            return Response.ok(jsonBuilder.toString()).build();

        } catch (Exception e) {
            System.out.println("❌ Error al obtener comentarios: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al obtener comentarios: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @POST
    @Path("/{id}/comentarios")
    @Transactional
    public Response agregarComentario(@PathParam("id") Integer ticketId, String jsonData) {
        try {
            System.out.println("💬 Agregando comentario al ticket: " + ticketId);
            System.out.println("📄 Datos recibidos: " + jsonData);

            if (jsonData == null || jsonData.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Datos del comentario requeridos\"}")
                        .build();
            }

            // Parsear JSON
            String comentario = extractJsonValue(jsonData, "comentario");
            String tipoComentario = extractJsonValue(jsonData, "tipoComentario");
            String estadoAnteriorRequest = extractJsonValue(jsonData, "estadoAnterior");
            String nuevoEstado = extractJsonValue(jsonData, "nuevoEstado");

            System.out.println("🔍 DEBUG - comentario: " + comentario);
            System.out.println("🔍 DEBUG - tipoComentario: " + tipoComentario);
            System.out.println("🔍 DEBUG - estadoAnterior (request): " + estadoAnteriorRequest);
            System.out.println("🔍 DEBUG - nuevoEstado: " + nuevoEstado);

            // Validaciones
            if (comentario == null || comentario.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"El comentario es requerido\"}")
                        .build();
            }

            if (tipoComentario == null || tipoComentario.trim().isEmpty()) {
                tipoComentario = "Seguimiento"; // Valor por defecto
            }

            // Verificar que el ticket existe
            String existeSQL = "SELECT COUNT(*) FROM Tickets WHERE id = ?";
            Integer existe = (Integer) em.createNativeQuery(existeSQL)
                    .setParameter(1, ticketId)
                    .getSingleResult();

            if (existe == 0) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Ticket no encontrado\"}")
                        .build();
            }

            // Obtener estado actual del ticket (para el historial)
            String estadoActualSQL = "SELECT estado FROM Tickets WHERE id = ?";
            String estadoActual = (String) em.createNativeQuery(estadoActualSQL)
                    .setParameter(1, ticketId)
                    .getSingleResult();

            // Obtener el ID del tipo de comentario con mapeo correcto
            String tipoComentarioIdSQL = "SELECT id_tipo FROM Tipos_Comentario WHERE LOWER(nombre) = LOWER(?)";
            Integer tipoComentarioId;

            // Mapear valores del frontend a los valores de la BD
            String tipoComentarioBD = tipoComentario;
            if ("Seguimiento".equalsIgnoreCase(tipoComentario)) {
                tipoComentarioBD = "seguimiento";
            } else if ("Técnico".equalsIgnoreCase(tipoComentario)) {
                tipoComentarioBD = "técnico";
            } else if ("Alerta".equalsIgnoreCase(tipoComentario)) {
                tipoComentarioBD = "alerta";
            } else if ("Resolución".equalsIgnoreCase(tipoComentario)) {
                tipoComentarioBD = "resolución";
            } else {
                tipoComentarioBD = "seguimiento"; // Por defecto
            }

            try {
                tipoComentarioId = (Integer) em.createNativeQuery(tipoComentarioIdSQL)
                        .setParameter(1, tipoComentarioBD)
                        .getSingleResult();
                System.out.println(
                        "✅ Tipo de comentario encontrado: " + tipoComentarioBD + " (ID: " + tipoComentarioId + ")");
            } catch (Exception e) {
                // Si no existe el tipo, usar el ID 2 por defecto (seguimiento)
                tipoComentarioId = 2; // 'seguimiento' debería tener ID 2
                System.out.println("⚠️ Tipo de comentario no encontrado: " + tipoComentarioBD
                        + ", usando ID por defecto: " + tipoComentarioId);
            }

            // Para simplificar, usar usuario ID 1 (después implementaremos JWT)
            Integer usuarioId = 1;

            // ==========================================
            // VALIDACIÓN ANTI-DUPLICADOS DE COMENTARIOS
            // ==========================================

            // Verificar si ya existe el mismo comentario del mismo usuario en los últimos 5
            // minutos
            String checkDuplicadoSQL = "SELECT COUNT(*) FROM Comentarios_Ticket " +
                    "WHERE ticket_id = ? AND usuario_id = ? AND comentario = ? " +
                    "AND fecha_creacion >= DATEADD(MINUTE, -5, GETDATE())";

            Integer comentariosDuplicados = (Integer) em.createNativeQuery(checkDuplicadoSQL)
                    .setParameter(1, ticketId)
                    .setParameter(2, usuarioId)
                    .setParameter(3, comentario)
                    .getSingleResult();

            if (comentariosDuplicados > 0) {
                System.out.println(
                        "⚠️ Comentario duplicado detectado para usuario " + usuarioId + " en ticket " + ticketId);
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"No se puede agregar el mismo comentario dos veces en un período corto de tiempo\", \"success\": false}")
                        .build();
            }

            // Insertar el comentario (con estado si hay cambio)
            String insertComentarioSQL;

            // Usar estadoAnterior del request si viene, sino usar el estadoActual de BD
            String estadoAnteriorFinal = (estadoAnteriorRequest != null && !estadoAnteriorRequest.trim().isEmpty())
                    ? estadoAnteriorRequest
                    : estadoActual;

            System.out.println("🔍 DEBUG - Evaluando cambio de estado:");
            System.out.println("   - nuevoEstado: '" + nuevoEstado + "'");
            System.out.println("   - estadoAnterior (request): '" + estadoAnteriorRequest + "'");
            System.out.println("   - estadoActual (BD): '" + estadoActual + "'");
            System.out.println("   - estadoAnteriorFinal: '" + estadoAnteriorFinal + "'");
            System.out.println("   - nuevoEstado != null: " + (nuevoEstado != null));
            System.out.println(
                    "   - !nuevoEstado.trim().isEmpty(): " + (nuevoEstado != null && !nuevoEstado.trim().isEmpty()));

            // Hay cambio de estado si tenemos nuevoEstado y es diferente al
            // estadoAnteriorFinal
            boolean hayEstadosCambio = nuevoEstado != null && !nuevoEstado.trim().isEmpty()
                    && estadoAnteriorFinal != null && !estadoAnteriorFinal.equals(nuevoEstado);

            if (hayEstadosCambio) {
                System.out.println("✅ HAY CAMBIO DE ESTADO - Insertando con columnas de estado: " + estadoAnteriorFinal
                        + " → " + nuevoEstado);
                // Si hay cambio de estado, incluir columnas de estado
                insertComentarioSQL = "INSERT INTO Comentarios_Ticket (ticket_id, comentario, usuario_id, tipo_comentario_id, estado_anterior, estado_nuevo, fecha_creacion) "
                        + "VALUES (?, ?, ?, ?, ?, ?, GETDATE())";

                em.createNativeQuery(insertComentarioSQL)
                        .setParameter(1, ticketId)
                        .setParameter(2, comentario)
                        .setParameter(3, usuarioId)
                        .setParameter(4, tipoComentarioId)
                        .setParameter(5, estadoAnteriorFinal) // estado_anterior
                        .setParameter(6, nuevoEstado) // estado_nuevo
                        .executeUpdate();
            } else {
                System.out.println("❌ NO HAY CAMBIO DE ESTADO - Insertando sin columnas de estado");
                // Si no hay cambio de estado, insertar sin columnas de estado
                insertComentarioSQL = "INSERT INTO Comentarios_Ticket (ticket_id, comentario, usuario_id, tipo_comentario_id, fecha_creacion) "
                        + "VALUES (?, ?, ?, ?, GETDATE())";

                em.createNativeQuery(insertComentarioSQL)
                        .setParameter(1, ticketId)
                        .setParameter(2, comentario)
                        .setParameter(3, usuarioId)
                        .setParameter(4, tipoComentarioId)
                        .executeUpdate();
            }

            // Si hay cambio de estado, actualizar el ticket y registrar en historial
            if (nuevoEstado != null && !nuevoEstado.trim().isEmpty() && !nuevoEstado.equals(estadoActual)) {
                System.out.println("🔄 Cambiando estado de '" + estadoActual + "' a '" + nuevoEstado + "'");

                // Actualizar estado del ticket
                String updateEstadoSQL = "UPDATE Tickets SET estado = ?, fecha_modificacion = GETDATE(), usuario_modificacion = ? WHERE id = ?";
                em.createNativeQuery(updateEstadoSQL)
                        .setParameter(1, nuevoEstado)
                        .setParameter(2, usuarioId)
                        .setParameter(3, ticketId)
                        .executeUpdate();

                // Obtener equipo_id del ticket para registrar en bitácora
                String getEquipoSQL = "SELECT equipo_id FROM Tickets WHERE id = ?";
                Integer equipoId = (Integer) em.createNativeQuery(getEquipoSQL)
                        .setParameter(1, ticketId)
                        .getSingleResult();

                if (equipoId != null) {
                    // Obtener nombre del usuario para la bitácora
                    String usuarioNombre = "Sistema";
                    try {
                        String getNombreSQL = "SELECT nombre_completo FROM Usuarios WHERE id = ?";
                        Object nombreResult = em.createNativeQuery(getNombreSQL)
                                .setParameter(1, usuarioId)
                                .getSingleResult();
                        if (nombreResult != null) {
                            usuarioNombre = nombreResult.toString();
                        }
                    } catch (Exception e) {
                        System.out.println("⚠️ No se pudo obtener nombre del usuario");
                    }

                    // Registrar el cambio de estado en la bitácora
                    bitacoraService.registrarTicketCambioEstado(equipoId, ticketId, estadoActual, nuevoEstado,
                            usuarioId, usuarioNombre);

                    // Si el ticket se cerró/resolvió, registrar también como resuelto
                    if (nuevoEstado.equalsIgnoreCase("Cerrado") || nuevoEstado.equalsIgnoreCase("Resuelto")) {
                        String comentarioCorto = comentario != null && comentario.length() > 50
                                ? comentario.substring(0, 47) + "..."
                                : (comentario != null ? comentario : "Sin detalles");
                        bitacoraService.registrarTicketResuelto(equipoId, ticketId, comentarioCorto, usuarioId,
                                usuarioNombre);
                    }
                }

                System.out.println("✅ Estado del ticket actualizado de '" + estadoActual + "' a '" + nuevoEstado + "'");
            }

            System.out.println("✅ Comentario agregado exitosamente");

            return Response.ok("{\"message\": \"Comentario agregado exitosamente\", \"success\": true}").build();

        } catch (Exception e) {
            System.out.println("❌ Error al agregar comentario: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al agregar comentario: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @GET
    @Path("/{id}/evidencias")
    public Response getEvidencias(@PathParam("id") Integer ticketId) {
        try {
            System.out.println("📁 Obteniendo evidencias para ticket: " + ticketId);

            String sql = "SELECT id, nombre_archivo, nombre_original, tipo_archivo, tamanio, " +
                    "descripcion, archivo_url, fecha_creacion " +
                    "FROM Evidencias " +
                    "WHERE entidad_relacionada = 'ticket' AND entidad_id = ? " +
                    "ORDER BY fecha_creacion DESC";

            @SuppressWarnings("unchecked")
            List<Object[]> resultados = em.createNativeQuery(sql)
                    .setParameter(1, ticketId)
                    .getResultList();

            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{\"evidencias\": [");

            for (int i = 0; i < resultados.size(); i++) {
                Object[] row = resultados.get(i);
                if (i > 0)
                    jsonBuilder.append(",");

                jsonBuilder.append("{")
                        .append("\"id\": ").append(row[0]).append(",")
                        .append("\"nombreArchivo\": \"").append(row[1] != null ? row[1] : "").append("\",")
                        .append("\"nombreOriginal\": \"").append(row[2] != null ? row[2] : "").append("\",")
                        .append("\"tipoArchivo\": \"").append(row[3] != null ? row[3] : "").append("\",")
                        .append("\"tamanio\": ").append(row[4] != null ? row[4] : 0).append(",")
                        .append("\"descripcion\": \"")
                        .append(row[5] != null ? row[5].toString().replace("\"", "\\\"") : "").append("\",")
                        .append("\"archivoUrl\": \"").append(row[6] != null ? row[6] : "").append("\",")
                        .append("\"fechaCreacion\": \"").append(row[7] != null ? row[7] : "").append("\"")
                        .append("}");
            }

            jsonBuilder.append("], \"total\": ").append(resultados.size())
                    .append(", \"success\": true}");

            return Response.ok(jsonBuilder.toString()).build();

        } catch (Exception e) {
            System.out.println("❌ Error al obtener evidencias: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al obtener evidencias: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @POST
    @Path("/{id}/evidencias")
    @Transactional
    public Response addEvidencia(@PathParam("id") Integer ticketId, String jsonData) {
        try {
            System.out.println("📎 Agregando evidencia al ticket: " + ticketId);
            System.out.println("Datos recibidos: " + jsonData);

            // Extraer datos del JSON
            String archivoUrl = extractJsonValue(jsonData, "archivoUrl");
            String descripcion = extractJsonValue(jsonData, "descripcion");

            if (archivoUrl == null || archivoUrl.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"La URL del archivo es obligatoria\", \"success\": false}")
                        .build();
            }

            // Insertar evidencia
            String insertSQL = "INSERT INTO Evidencias (entidad_relacionada, entidad_id, archivo_url, descripcion, fecha_creacion) "
                    +
                    "VALUES ('ticket', ?, ?, ?, GETDATE())";

            em.createNativeQuery(insertSQL)
                    .setParameter(1, ticketId)
                    .setParameter(2, archivoUrl)
                    .setParameter(3, descripcion != null ? descripcion : "")
                    .executeUpdate();

            System.out.println("✅ Evidencia agregada correctamente");
            return Response.ok("{\"message\": \"Evidencia agregada correctamente\", \"success\": true}").build();

        } catch (Exception e) {
            System.out.println("❌ Error al agregar evidencia: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al agregar evidencia: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @POST
    @Path("/{id}/evidencias/upload")
    @Consumes({ MediaType.APPLICATION_OCTET_STREAM, MediaType.MULTIPART_FORM_DATA, "image/*", "application/*" })
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response uploadEvidenciaArchivo(
            @PathParam("id") Integer ticketId,
            InputStream inputStream,
            @HeaderParam("X-Filename") String fileName,
            @HeaderParam("X-Descripcion") String descripcion,
            @HeaderParam("X-Usuario-Id") String usuarioIdHeader,
            @HeaderParam("X-Usuario-Nombre") String usuarioNombreHeader) {

        try {
            if (ticketRepository.findById(ticketId) == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Ticket no encontrado\"}")
                        .build();
            }

            if (fileName == null || fileName.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Debe enviar el nombre del archivo en el encabezado X-Filename\"}")
                        .build();
            }

            if (!isValidEvidenciaFile(fileName)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Tipo de archivo no permitido\"}")
                        .build();
            }

            // Obtener datos del usuario que sube la evidencia
            Integer usuarioId = 1;
            String usuarioNombre = "Sistema";
            if (usuarioIdHeader != null && !usuarioIdHeader.trim().isEmpty()) {
                try {
                    usuarioId = Integer.parseInt(usuarioIdHeader);
                } catch (NumberFormatException e) {
                    System.out.println("⚠️ ID de usuario inválido en header");
                }
            }
            if (usuarioNombreHeader != null && !usuarioNombreHeader.trim().isEmpty()) {
                try {
                    usuarioNombre = URLDecoder.decode(usuarioNombreHeader, "UTF-8");
                } catch (Exception e) {
                    System.out.println("⚠️ Error decodificando nombre de usuario");
                }
            } else if (usuarioId != 1) {
                // Obtener nombre de la BD si no viene en header
                try {
                    String getNombreSQL = "SELECT nombre_completo FROM Usuarios WHERE id = ?";
                    Object nombreResult = em.createNativeQuery(getNombreSQL)
                            .setParameter(1, usuarioId)
                            .getSingleResult();
                    if (nombreResult != null) {
                        usuarioNombre = nombreResult.toString();
                    }
                } catch (Exception e) {
                    System.out.println("⚠️ No se pudo obtener nombre del usuario");
                }
            }

            java.nio.file.Path ticketDir = Paths.get(EVIDENCIAS_DIR, String.valueOf(ticketId));
            if (!Files.exists(ticketDir)) {
                Files.createDirectories(ticketDir);
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
            String extension = getFileExtension(fileName);
            String baseName = getFileNameWithoutExtension(fileName);
            String uniqueFileName = baseName + "_" + timestamp + extension;

            java.nio.file.Path filePath = ticketDir.resolve(uniqueFileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            long fileSize = Files.size(filePath);

            String mimeType = detectMimeType(fileName);
            String decodedDescripcion = descripcion != null ? URLDecoder.decode(descripcion, "UTF-8") : "";
            String archivoUrl = "/MantenimientosBackend/api/tickets/" + ticketId + "/evidencias/download/"
                    + uniqueFileName;

            String insertSQL = "INSERT INTO Evidencias (entidad_relacionada, entidad_id, nombre_archivo, nombre_original, "
                    +
                    "tipo_archivo, tamanio, descripcion, archivo_url, fecha_creacion) " +
                    "VALUES ('ticket', ?, ?, ?, ?, ?, ?, ?, GETDATE())";

            em.createNativeQuery(insertSQL)
                    .setParameter(1, ticketId)
                    .setParameter(2, uniqueFileName)
                    .setParameter(3, fileName)
                    .setParameter(4, mimeType)
                    .setParameter(5, fileSize)
                    .setParameter(6, decodedDescripcion)
                    .setParameter(7, archivoUrl)
                    .executeUpdate();

            // Registrar en bitácora la evidencia agregada
            try {
                String getEquipoSQL = "SELECT equipo_id FROM Tickets WHERE id = ?";
                Integer equipoId = (Integer) em.createNativeQuery(getEquipoSQL)
                        .setParameter(1, ticketId)
                        .getSingleResult();
                if (equipoId != null) {
                    bitacoraService.registrarTicketEvidencia(equipoId, ticketId, fileName, usuarioId, usuarioNombre);
                }
            } catch (Exception ex) {
                System.out.println("⚠️ No se pudo registrar evidencia en bitácora: " + ex.getMessage());
            }

            String jsonResponse = String.format(
                    "{\"nombreArchivo\": \"%s\", \"nombreOriginal\": \"%s\", \"tipoArchivo\": \"%s\", " +
                            "\"tamanio\": %d, \"descripcion\": \"%s\", \"archivoUrl\": \"%s\"}",
                    uniqueFileName, fileName, mimeType, fileSize, escapeJson(decodedDescripcion), archivoUrl);

            return Response.status(Response.Status.CREATED).entity(jsonResponse).build();

        } catch (Exception e) {
            System.out.println("❌ Error al subir evidencia de ticket: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al guardar archivo: " + e.getMessage() + "\"}")
                    .build();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    @DELETE
    @Path("/{ticketId}/evidencias/{evidenciaId}")
    @Transactional
    public Response deleteEvidencia(@PathParam("ticketId") Integer ticketId,
            @PathParam("evidenciaId") Integer evidenciaId) {
        try {
            System.out.println("🗑️ Eliminando evidencia " + evidenciaId + " del ticket " + ticketId);

            String selectSQL = "SELECT nombre_archivo FROM Evidencias WHERE id = ? AND entidad_relacionada = 'ticket' AND entidad_id = ?";
            @SuppressWarnings("unchecked")
            List<Object> results = em.createNativeQuery(selectSQL)
                    .setParameter(1, evidenciaId)
                    .setParameter(2, ticketId)
                    .getResultList();

            if (results.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Evidencia no encontrada\", \"success\": false}")
                        .build();
            }

            String nombreArchivo = (String) results.get(0);
            if (nombreArchivo != null && !nombreArchivo.isEmpty()) {
                java.nio.file.Path filePath = Paths.get(EVIDENCIAS_DIR, String.valueOf(ticketId), nombreArchivo);
                try {
                    Files.deleteIfExists(filePath);
                } catch (IOException e) {
                    System.out.println("⚠️ No se pudo eliminar archivo físico: " + e.getMessage());
                }
            }

            String deleteSQL = "DELETE FROM Evidencias WHERE id = ? AND entidad_relacionada = 'ticket' AND entidad_id = ?";
            int result = em.createNativeQuery(deleteSQL)
                    .setParameter(1, evidenciaId)
                    .setParameter(2, ticketId)
                    .executeUpdate();

            if (result > 0) {
                System.out.println("✅ Evidencia eliminada correctamente");
                return Response.ok("{\"message\": \"Evidencia eliminada correctamente\", \"success\": true}").build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Evidencia no encontrada\", \"success\": false}")
                        .build();
            }

        } catch (Exception e) {
            System.out.println("❌ Error al eliminar evidencia: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al eliminar evidencia: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @GET
    @Path("/{id}/evidencias/download/{fileName}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadEvidencia(@PathParam("id") Integer ticketId, @PathParam("fileName") String fileName) {
        try {
            java.nio.file.Path filePath = Paths.get(EVIDENCIAS_DIR, String.valueOf(ticketId), fileName);
            if (!Files.exists(filePath)) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Archivo no encontrado")
                        .build();
            }

            byte[] data = Files.readAllBytes(filePath);
            String mimeType = detectMimeType(fileName);

            return Response.ok(data)
                    .type(mimeType)
                    .header("Content-Disposition", "inline; filename=\"" + fileName + "\"")
                    .build();

        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al descargar archivo")
                    .build();
        }
    }

    // ===== Helpers evidencia =====
    private boolean isValidEvidenciaFile(String fileName) {
        String lower = fileName.toLowerCase();
        for (String ext : ALLOWED_EXTENSIONS) {
            if (lower.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    private String getFileExtension(String fileName) {
        int idx = fileName.lastIndexOf('.');
        return idx >= 0 ? fileName.substring(idx) : "";
    }

    private String getFileNameWithoutExtension(String fileName) {
        int idx = fileName.lastIndexOf('.');
        return idx >= 0 ? fileName.substring(0, idx) : fileName;
    }

    private String detectMimeType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg"))
            return "image/jpeg";
        if (lower.endsWith(".png"))
            return "image/png";
        if (lower.endsWith(".gif"))
            return "image/gif";
        if (lower.endsWith(".webp"))
            return "image/webp";
        if (lower.endsWith(".pdf"))
            return "application/pdf";
        if (lower.endsWith(".doc"))
            return "application/msword";
        if (lower.endsWith(".docx"))
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (lower.endsWith(".xls"))
            return "application/vnd.ms-excel";
        if (lower.endsWith(".xlsx"))
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        return "application/octet-stream";
    }

    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
