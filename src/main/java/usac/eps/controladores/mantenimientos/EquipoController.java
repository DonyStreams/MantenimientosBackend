package usac.eps.controladores.mantenimientos;

import usac.eps.modelos.mantenimientos.EquipoModel;
import usac.eps.modelos.mantenimientos.AreaModel;
import usac.eps.modelos.mantenimientos.UsuarioMantenimientoModel;
import usac.eps.repositorios.mantenimientos.EquipoRepository;
import usac.eps.repositorios.mantenimientos.UsuarioMantenimientoRepository;
import usac.eps.seguridad.RequiresRole;
import usac.eps.servicios.mantenimientos.BitacoraService;
import usac.eps.servicios.mantenimientos.EmailService;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/equipos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class EquipoController {
    @Inject
    private EquipoRepository equipoRepository;

    @Inject
    private BitacoraService bitacoraService;

    @Inject
    private EmailService emailService;

    @Inject
    private UsuarioMantenimientoRepository usuarioRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Context
    private HttpServletRequest request;

    private static final Logger LOGGER = Logger.getLogger(EquipoController.class.getName());

    @GET
    public Response getAll(
            @QueryParam("idCategoria") Integer idCategoria,
            @QueryParam("estado") String estado) {
        try {
            // Consulta con JOIN para obtener el nombre del área
            String jpql = "SELECT e FROM EquipoModel e LEFT JOIN FETCH e.area LEFT JOIN FETCH e.categoria";

            // Construir cláusula WHERE dinámicamente
            List<String> condiciones = new ArrayList<>();
            if (idCategoria != null) {
                condiciones.add("e.idCategoria = :idCategoria");
            }
            if (estado != null && !estado.trim().isEmpty()) {
                condiciones.add("e.estado = :estado");
            }

            if (!condiciones.isEmpty()) {
                jpql += " WHERE " + String.join(" AND ", condiciones);
            }

            javax.persistence.TypedQuery<EquipoModel> query = entityManager.createQuery(jpql, EquipoModel.class);
            if (idCategoria != null) {
                query.setParameter("idCategoria", idCategoria);
            }
            if (estado != null && !estado.trim().isEmpty()) {
                query.setParameter("estado", estado);
            }

            List<EquipoModel> equipos = query.getResultList();

            // Crear lista de mapas con los datos del equipo + nombre del área
            List<Map<String, Object>> result = new ArrayList<>();
            for (EquipoModel equipo : equipos) {
                Map<String, Object> equipoMap = new HashMap<>();
                equipoMap.put("idEquipo", equipo.getIdEquipo());
                equipoMap.put("nombre", equipo.getNombre());
                equipoMap.put("codigoInacif", equipo.getCodigoInacif());
                equipoMap.put("marca", equipo.getMarca());
                equipoMap.put("modelo", equipo.getModelo());
                equipoMap.put("numeroSerie", equipo.getNumeroSerie());
                equipoMap.put("ubicacion", equipo.getUbicacion());
                equipoMap.put("magnitudMedicion", equipo.getMagnitudMedicion());
                equipoMap.put("rangoCapacidad", equipo.getRangoCapacidad());
                equipoMap.put("manualFabricante", equipo.getManualFabricante());
                equipoMap.put("fotografia", equipo.getFotografia());
                equipoMap.put("softwareFirmware", equipo.getSoftwareFirmware());
                equipoMap.put("condicionesOperacion", equipo.getCondicionesOperacion());
                equipoMap.put("descripcion", equipo.getDescripcion());
                equipoMap.put("idArea", equipo.getIdArea());
                equipoMap.put("idCategoria", equipo.getIdCategoria());
                equipoMap.put("estado", equipo.getEstado() != null ? equipo.getEstado() : "Activo");

                // Obtener nombre del área
                String areaNombre = null;
                if (equipo.getArea() != null) {
                    areaNombre = equipo.getArea().getNombre();
                } else if (equipo.getIdArea() != null) {
                    // Si el área no se cargó con el FETCH, buscarla manualmente
                    try {
                        AreaModel area = entityManager.find(AreaModel.class, equipo.getIdArea());
                        areaNombre = area != null ? area.getNombre() : null;
                    } catch (Exception ex) {
                        LOGGER.log(Level.WARNING, "No se pudo cargar área con ID: " + equipo.getIdArea(), ex);
                    }
                }
                equipoMap.put("areaNombre", areaNombre);

                String categoriaNombre = null;
                if (equipo.getCategoria() != null) {
                    categoriaNombre = equipo.getCategoria().getNombre();
                } else if (equipo.getIdCategoria() != null) {
                    try {
                        Object nombreCategoria = entityManager
                                .createQuery("SELECT c.nombre FROM CategoriaEquipoModel c WHERE c.idCategoria = :id")
                                .setParameter("id", equipo.getIdCategoria())
                                .getSingleResult();
                        categoriaNombre = nombreCategoria != null ? nombreCategoria.toString() : null;
                    } catch (Exception ex) {
                        LOGGER.log(Level.WARNING, "No se pudo cargar categoría con ID: " + equipo.getIdCategoria(), ex);
                    }
                }
                equipoMap.put("categoriaNombre", categoriaNombre);

                result.add(equipoMap);
            }

            return Response.ok(result).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener equipos", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    public EquipoModel getById(@PathParam("id") Integer id) {
        return equipoRepository.findByIdEquipo(id);
    }

    /**
     * 🔒 Crear equipo: ADMIN, SUPERVISOR, TECNICO_EQUIPOS
     */
    @POST
    @Transactional
    @RequiresRole({ "ADMIN", "SUPERVISOR", "TECNICO_EQUIPOS" })
    public Response create(EquipoModel equipo) {
        try {
            System.out.println("➕ Creando nuevo equipo: " + equipo.getNombre());

            // Validar que el código INACIF no exista
            if (equipo.getCodigoInacif() != null && !equipo.getCodigoInacif().trim().isEmpty()) {
                Optional<EquipoModel> existente = equipoRepository
                        .findOptionalByCodigoInacif(equipo.getCodigoInacif().trim());
                if (existente.isPresent()) {
                    return Response.status(Response.Status.CONFLICT)
                            .entity("{\"error\": \"Ya existe un equipo con el código INACIF: "
                                    + equipo.getCodigoInacif() + "\"}")
                            .build();
                }
            }

            // Limpiar espacios en blanco
            if (equipo.getCodigoInacif() != null) {
                equipo.setCodigoInacif(equipo.getCodigoInacif().trim());
            }

            equipo.setFechaCreacion(new java.util.Date());
            equipo.setFechaModificacion(new java.util.Date());

            // Usar persist directamente en lugar de save
            entityManager.persist(equipo);
            entityManager.flush();

            System.out.println("✅ Equipo guardado con ID: " + equipo.getIdEquipo());

            // Obtener usuario desde el contexto de Keycloak
            String usuarioNombre = "Sistema";
            Integer usuarioId = null;

            try {
                String keycloakId = (String) request.getAttribute("keycloakId");
                String username = (String) request.getAttribute("username");

                if (keycloakId != null) {
                    System.out.println("🔑 Usuario autenticado: " + username + " (Keycloak ID: " + keycloakId + ")");

                    // Buscar usuario en la base de datos
                    UsuarioMantenimientoModel usuario = usuarioRepository.findByKeycloakId(keycloakId);
                    if (usuario != null) {
                        usuarioId = usuario.getId();
                        usuarioNombre = usuario.getNombreCompleto() != null ? usuario.getNombreCompleto() : username;
                        System.out.println("👤 Usuario encontrado: " + usuarioNombre + " (ID: " + usuarioId + ")");
                    } else {
                        System.out.println("⚠️ Usuario no encontrado en BD, usando username: " + username);
                        usuarioNombre = username;
                    }
                }
            } catch (Exception e) {
                System.out.println("⚠️ No se pudo obtener usuario de Keycloak: " + e.getMessage());
            }

            // Registrar creación en historial
            if (equipo.getIdEquipo() != null) {
                registrarHistorialSimplificado(equipo.getIdEquipo(), "CREACION",
                        "Equipo '" + equipo.getNombre() + "' registrado en el sistema",
                        usuarioId, usuarioNombre);
                entityManager.flush();
            } else {
                System.out.println("⚠️ No se pudo registrar historial: Equipo sin ID");
            }

            System.out.println("✅ Equipo creado exitosamente con ID: " + equipo.getIdEquipo());
            return Response.status(Response.Status.CREATED).entity(equipo).build();

        } catch (Exception e) {
            System.out.println("❌ Error al crear equipo: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al crear equipo: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * 🔒 Actualizar equipo: ADMIN, SUPERVISOR, TECNICO_EQUIPOS
     */
    @PUT
    @Path("/{id}")
    @Transactional
    @RequiresRole({ "ADMIN", "SUPERVISOR", "TECNICO_EQUIPOS" })
    public Response update(@PathParam("id") Integer id, EquipoModel equipo) {
        try {
            System.out.println("📝 Actualizando equipo ID: " + id);

            // Obtener equipo anterior
            EquipoModel equipoAnterior = equipoRepository.findByIdEquipo(id);
            if (equipoAnterior == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Equipo no encontrado\"}")
                        .build();
            }

            // Validar código INACIF único (solo si cambió)
            if (equipo.getCodigoInacif() != null && !equipo.getCodigoInacif().trim().isEmpty()) {
                String nuevoCodigoInacif = equipo.getCodigoInacif().trim();
                String codigoAnterior = equipoAnterior.getCodigoInacif();

                // Solo validar si el código cambió
                if (!nuevoCodigoInacif.equals(codigoAnterior)) {
                    Optional<EquipoModel> existente = equipoRepository.findOptionalByCodigoInacif(nuevoCodigoInacif);
                    if (existente.isPresent() && !existente.get().getIdEquipo().equals(id)) {
                        return Response.status(Response.Status.CONFLICT)
                                .entity("{\"error\": \"Ya existe un equipo con el código INACIF: " + nuevoCodigoInacif
                                        + "\"}")
                                .build();
                    }
                }
                equipo.setCodigoInacif(nuevoCodigoInacif);
            }

            // IMPORTANTE: Preservar fecha de creación original
            equipo.setFechaCreacion(equipoAnterior.getFechaCreacion());

            // Establecer nueva fecha de modificación
            equipo.setFechaModificacion(new java.util.Date());

            // Establecer el ID
            equipo.setIdEquipo(id);

            // Obtener usuario desde el contexto de Keycloak
            String usuarioNombre = "Sistema";
            Integer usuarioId = null;

            try {
                String keycloakId = (String) request.getAttribute("keycloakId");
                String username = (String) request.getAttribute("username");

                if (keycloakId != null) {
                    UsuarioMantenimientoModel usuario = usuarioRepository.findByKeycloakId(keycloakId);
                    if (usuario != null) {
                        usuarioId = usuario.getId();
                        usuarioNombre = usuario.getNombreCompleto() != null ? usuario.getNombreCompleto() : username;
                    } else {
                        usuarioNombre = username;
                    }
                }
            } catch (Exception e) {
                System.out.println("⚠️ No se pudo obtener usuario de Keycloak: " + e.getMessage());
            }

            // Actualizar datos del equipo
            equipo.setIdEquipo(id);
            equipo.setFechaModificacion(new java.util.Date());
            equipoRepository.save(equipo);

            // Registrar en historial simplificado
            boolean cambioImagen = (equipoAnterior.getFotografia() != null
                    && !equipoAnterior.getFotografia().equals(equipo.getFotografia()));
            boolean cambioUbicacion = (equipoAnterior.getUbicacion() != null
                    && !equipoAnterior.getUbicacion().equals(equipo.getUbicacion()));
            boolean cambioEstado = (equipoAnterior.getEstado() != null
                    && !equipoAnterior.getEstado().equals(equipo.getEstado()));

            if (cambioImagen) {
                // Cambio de imagen
                registrarHistorialSimplificado(id, "CAMBIO_IMAGEN",
                        "Fotografía del equipo actualizada",
                        usuarioId, usuarioNombre);
                System.out.println("📷 Imagen actualizada");
            } else if (cambioUbicacion) {
                // Cambio de ubicación
                registrarHistorialSimplificado(id, "CAMBIO_UBICACION",
                        "Ubicación cambiada de '" + equipoAnterior.getUbicacion() + "' a '" + equipo.getUbicacion()
                                + "'",
                        usuarioId, usuarioNombre);
                System.out.println("📍 Ubicación actualizada");
            } else if (cambioEstado) {
                // Cambio de estado
                registrarHistorialSimplificado(id, "CAMBIO_ESTADO",
                        "Estado cambiado de '" + equipoAnterior.getEstado() + "' a '" + equipo.getEstado() + "'",
                        usuarioId, usuarioNombre);
                System.out.println("🔄 Estado actualizado");

                // 🚨 ENVIAR NOTIFICACIÓN SI EL EQUIPO CAMBIA A ESTADO CRÍTICO
                if (equipo.getEstado() != null && equipo.getEstado().equalsIgnoreCase("Critico")) {
                    try {
                        String motivoCambio = "Estado cambiado de '" + equipoAnterior.getEstado() + "' a 'Crítico' por "
                                + usuarioNombre;

                        emailService.notificarEquipoCritico(
                                id,
                                equipo.getNombre() != null ? equipo.getNombre() : "Sin nombre",
                                equipo.getCodigoInacif() != null ? equipo.getCodigoInacif() : "N/A",
                                equipo.getUbicacion() != null ? equipo.getUbicacion() : "No especificada",
                                equipoAnterior.getEstado() != null ? equipoAnterior.getEstado() : "Desconocido",
                                motivoCambio);

                        System.out.println("📧 Notificación de equipo crítico enviada - Equipo #" + id);
                    } catch (Exception emailEx) {
                        System.out
                                .println("⚠️ Error al enviar notificación de equipo crítico: " + emailEx.getMessage());
                        // No interrumpir el flujo si falla el correo
                    }
                }
            } else {
                // Edición general
                registrarHistorialSimplificado(id, "EDICION_GENERAL",
                        "Información del equipo actualizada",
                        usuarioId, usuarioNombre);
                System.out.println("✏️ Información general actualizada");
            }

            System.out.println("✅ Equipo actualizado correctamente");
            return Response.ok(equipo).build();

        } catch (Exception e) {
            System.out.println("❌ Error al actualizar equipo: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al actualizar equipo: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * Registra un cambio en el historial simplificado
     */
    private void registrarHistorialSimplificado(Integer equipoId, String tipoCambio,
            String descripcion, Integer usuarioId,
            String usuarioNombre) {
        try {
            // Crear el historial usando JPA
            String sql = "INSERT INTO Historial_Equipo (id_equipo, tipo_cambio, descripcion, usuario_id, usuario_nombre, fecha_registro) "
                    +
                    "VALUES (?, ?, ?, ?, ?, GETDATE())";

            entityManager.createNativeQuery(sql)
                    .setParameter(1, equipoId)
                    .setParameter(2, tipoCambio)
                    .setParameter(3, descripcion)
                    .setParameter(4, usuarioId)
                    .setParameter(5, usuarioNombre)
                    .executeUpdate();

            // Flush inmediato para asegurar persistencia
            entityManager.flush();

            System.out.println("📝 Historial registrado: " + tipoCambio + " para equipo ID: " + equipoId);
        } catch (Exception e) {
            System.out.println("⚠️ Error al registrar historial: " + e.getMessage());
            e.printStackTrace();
            // No fallar la operación principal si falla el historial
        }
    }

    /**
     * 🔒 Eliminar equipo: Solo ADMIN
     */
    @DELETE
    @Path("/{id}")
    @Transactional
    @RequiresRole({ "ADMIN" })
    public Response delete(@PathParam("id") Integer id) {
        EquipoModel equipo = equipoRepository.findByIdEquipo(id);
        if (equipo == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Equipo no encontrado\"}")
                    .build();
        }

        try {
            // Verificar si tiene programaciones asociadas
            String queryProgramaciones = "SELECT COUNT(*) FROM Programaciones_Mantenimiento WHERE id_equipo = ?";
            Integer countProgramaciones = ((Number) entityManager.createNativeQuery(queryProgramaciones)
                    .setParameter(1, id)
                    .getSingleResult()).intValue();

            // Verificar si tiene ejecuciones asociadas
            String queryEjecuciones = "SELECT COUNT(*) FROM Ejecuciones_Mantenimiento WHERE id_equipo = ?";
            Integer countEjecuciones = ((Number) entityManager.createNativeQuery(queryEjecuciones)
                    .setParameter(1, id)
                    .getSingleResult()).intValue();

            // Verificar si tiene historial
            String queryHistorial = "SELECT COUNT(*) FROM Historial_Equipo WHERE id_equipo = ?";
            Integer countHistorial = ((Number) entityManager.createNativeQuery(queryHistorial)
                    .setParameter(1, id)
                    .getSingleResult()).intValue();

            // Verificar si tiene tickets asociados
            String queryTickets = "SELECT COUNT(*) FROM Tickets WHERE equipo_id = ?";
            Integer countTickets = ((Number) entityManager.createNativeQuery(queryTickets)
                    .setParameter(1, id)
                    .getSingleResult()).intValue();

            if (countProgramaciones > 0 || countEjecuciones > 0 || countHistorial > 0 || countTickets > 0) {
                StringBuilder mensaje = new StringBuilder(
                        "No se puede eliminar el equipo porque tiene registros relacionados: ");
                boolean hayRelaciones = false;

                if (countProgramaciones > 0) {
                    mensaje.append(countProgramaciones).append(" programación(es)");
                    hayRelaciones = true;
                }
                if (countEjecuciones > 0) {
                    if (hayRelaciones)
                        mensaje.append(", ");
                    mensaje.append(countEjecuciones).append(" ejecución(es)");
                    hayRelaciones = true;
                }
                if (countTickets > 0) {
                    if (hayRelaciones)
                        mensaje.append(", ");
                    mensaje.append(countTickets).append(" ticket(s)");
                    hayRelaciones = true;
                }
                if (countHistorial > 0) {
                    if (hayRelaciones)
                        mensaje.append(", ");
                    mensaje.append(countHistorial).append(" registro(s) de historial");
                }

                LOGGER.warning("⚠️ No se puede eliminar equipo " + id + ": " + mensaje);
                return Response.status(Response.Status.CONFLICT)
                        .entity("{\"error\": \"" + mensaje.toString() + "\"}")
                        .build();
            }

            // Si no tiene relaciones, eliminar
            EquipoModel managedEquipo = entityManager.merge(equipo);
            entityManager.remove(managedEquipo);
            entityManager.flush();

            LOGGER.info("✅ Equipo " + id + " eliminado correctamente");
            return Response.noContent().build();

        } catch (Exception e) {
            LOGGER.severe("❌ Error al eliminar equipo: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al eliminar el equipo: " + e.getMessage() + "\"}")
                    .build();
        }
    }
}
