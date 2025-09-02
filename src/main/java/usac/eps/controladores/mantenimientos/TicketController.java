package usac.eps.controladores.mantenimientos;

import usac.eps.modelos.mantenimientos.TicketModel;
import usac.eps.repositorios.mantenimientos.TicketRepository;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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

    @PersistenceContext(unitName = "usac.eps_ControlSuministros")
    private EntityManager em;

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
                    "uc.nombre_completo as usuario_creador, ua.nombre_completo as usuario_asignado " +
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
                        .append("\"usuarioCreador\": \"").append(row[10] != null ? row[10] : "").append("\",")
                        .append("\"usuarioAsignado\": \"").append(row[11] != null ? row[11] : "").append("\"")
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
                    "uc.nombre_completo as usuario_creador, ua.nombre_completo as usuario_asignado " +
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
                    "\"usuarioCreador\": \"" + (row[10] != null ? row[10] : "") + "\"," +
                    "\"usuarioAsignado\": \"" + (row[11] != null ? row[11] : "") + "\"," +
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

            // Verificar que el ticket existe
            String existeSQL = "SELECT COUNT(*) FROM Tickets WHERE id = ?";
            Integer existe = (Integer) em.createNativeQuery(existeSQL)
                    .setParameter(1, id)
                    .getSingleResult();

            if (existe == 0) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Ticket no encontrado\"}")
                        .build();
            }

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

            if (equipoIdStr != null && !equipoIdStr.trim().isEmpty()) {
                try {
                    Integer equipoId = Integer.parseInt(equipoIdStr);
                    updateSQL.append(", equipo_id = ?");
                    parametros.add(equipoId);
                } catch (NumberFormatException e) {
                    System.out.println("⚠️ ID de equipo inválido: " + equipoIdStr);
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
    public Response delete(@PathParam("id") Integer id) {
        try {
            System.out.println("🗑️ Desactivando ticket ID: " + id);

            // Soft delete: cambiar estado a "Inactivo" en lugar de eliminar físicamente
            String updateSQL = "UPDATE Tickets SET estado = 'Inactivo', fecha_modificacion = GETDATE() WHERE id = ?";

            Query updateQuery = em.createNativeQuery(updateSQL);
            updateQuery.setParameter(1, id);

            int result = updateQuery.executeUpdate();

            if (result > 0) {
                System.out.println("✅ Ticket desactivado correctamente (soft delete)");
                return Response.ok("{\"message\": \"Ticket desactivado correctamente\", \"success\": true}")
                        .build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Ticket no encontrado\", \"success\": false}")
                        .build();
            }

        } catch (Exception e) {
            System.out.println("❌ Error al desactivar ticket: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al desactivar ticket: " + e.getMessage() + "\", \"success\": false}")
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
            String nuevoEstado = extractJsonValue(jsonData, "nuevoEstado");

            System.out.println("🔍 DEBUG - comentario: " + comentario);
            System.out.println("🔍 DEBUG - tipoComentario: " + tipoComentario);
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

            System.out.println("🔍 DEBUG - Evaluando cambio de estado:");
            System.out.println("   - nuevoEstado: '" + nuevoEstado + "'");
            System.out.println("   - estadoActual: '" + estadoActual + "'");
            System.out.println("   - nuevoEstado != null: " + (nuevoEstado != null));
            System.out.println(
                    "   - !nuevoEstado.trim().isEmpty(): " + (nuevoEstado != null && !nuevoEstado.trim().isEmpty()));
            System.out.println("   - !nuevoEstado.equals(estadoActual): "
                    + (nuevoEstado != null && !nuevoEstado.equals(estadoActual)));

            if (nuevoEstado != null && !nuevoEstado.trim().isEmpty() && !nuevoEstado.equals(estadoActual)) {
                System.out.println("✅ HAY CAMBIO DE ESTADO - Insertando con columnas de estado");
                // Si hay cambio de estado, incluir columnas de estado
                insertComentarioSQL = "INSERT INTO Comentarios_Ticket (ticket_id, comentario, usuario_id, tipo_comentario_id, estado_anterior, estado_nuevo, fecha_creacion) "
                        + "VALUES (?, ?, ?, ?, ?, ?, GETDATE())";

                em.createNativeQuery(insertComentarioSQL)
                        .setParameter(1, ticketId)
                        .setParameter(2, comentario)
                        .setParameter(3, usuarioId)
                        .setParameter(4, tipoComentarioId)
                        .setParameter(5, estadoActual) // estado_anterior
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

            String sql = "SELECT id, archivo_url, descripcion, fecha_creacion " +
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
                        .append("\"archivoUrl\": \"").append(row[1]).append("\",")
                        .append("\"descripcion\": \"")
                        .append(row[2] != null ? row[2].toString().replace("\"", "\\\"") : "").append("\",")
                        .append("\"fechaCreacion\": \"").append(row[3]).append("\"")
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
}
