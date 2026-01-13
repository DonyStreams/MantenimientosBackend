package usac.eps.controladores.mantenimientos;

import usac.eps.modelos.mantenimientos.ContratoModel;
import usac.eps.modelos.mantenimientos.ProveedorModel;
import usac.eps.modelos.mantenimientos.UsuarioMantenimientoModel;
import usac.eps.repositorios.mantenimientos.ContratoRepository;
import usac.eps.repositorios.mantenimientos.ContratoEquipoRepository;
import usac.eps.repositorios.mantenimientos.ProveedorRepository;
import usac.eps.repositorios.mantenimientos.UsuarioMantenimientoRepository;

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
import java.util.Date;

@Path("/contratos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class ContratoController {
    @Inject
    private ContratoRepository contratoRepository;

    @Inject
    private ContratoEquipoRepository contratoEquipoRepository;

    @Inject
    private ProveedorRepository proveedorRepository;

    @Inject
    private UsuarioMantenimientoRepository usuarioRepository;

    @PersistenceContext
    private EntityManager em;

    @Context
    private HttpServletRequest request;

    @GET
    public Response getAll() {
        try {
            // Obtener TODOS los contratos (activos e inactivos)
            List<ContratoModel> contratos = contratoRepository.findAll();
            List<Map<String, Object>> contratosDTO = new ArrayList<>();

            for (ContratoModel contrato : contratos) {
                contratosDTO.add(convertirADTO(contrato));
            }

            System.out.println("📋 Devolviendo " + contratosDTO.size() + " contratos (activos e inactivos)");
            return Response.ok(contratosDTO).build();
        } catch (Exception e) {
            System.out.println("❌ Error al obtener contratos: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al obtener contratos\"}")
                    .build();
        }
    }

    @GET
    @Path("/stats")
    public Response getStats() {
        try {
            // Obtener estadísticas reales usando las consultas del repositorio
            long totalContratos = contratoRepository.count();
            long vigentes = contratoRepository.countVigentes();

            // Calcular fecha límite para contratos por vencer (30 días desde hoy)
            Date fechaLimite = new Date(System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000));
            long porVencer = contratoRepository.countPorVencer(fechaLimite);

            long vencidos = contratoRepository.countVencidos();

            // Contar contratos inactivos (estado = false)
            long inactivos = contratoRepository.countByEstado(false);

            Map<String, Object> stats = new HashMap<>();
            stats.put("total", totalContratos);
            stats.put("vigentes", vigentes);
            stats.put("porVencer", porVencer);
            stats.put("vencidos", vencidos);
            stats.put("inactivos", inactivos);

            System.out.println("📊 Estadísticas calculadas:");
            System.out.println("  - Total: " + totalContratos);
            System.out.println("  - Vigentes: " + vigentes);
            System.out.println("  - Por vencer (30 días): " + porVencer);
            System.out.println("  - Vencidos: " + vencidos);
            System.out.println("  - Inactivos: " + inactivos);

            return Response.ok(stats).build();
        } catch (Exception e) {
            System.out.println("❌ Error al obtener estadísticas: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al obtener estadísticas\"}")
                    .build();
        }
    }

    @GET
    @Path("/activos")
    public Response getActivos() {
        try {
            // Obtener solo contratos activos (estado = true) y vigentes
            List<ContratoModel> contratos = contratoRepository.findByEstado(true);
            List<Map<String, Object>> contratosDTO = new ArrayList<>();

            Date hoy = new Date();
            for (ContratoModel contrato : contratos) {
                // Solo incluir contratos cuya fecha de vencimiento aún no ha pasado
                if (contrato.getFechaFin() == null || contrato.getFechaFin().after(hoy)
                        || contrato.getFechaFin().equals(hoy)) {
                    contratosDTO.add(convertirADTO(contrato));
                }
            }

            System.out.println("📋 Devolviendo " + contratosDTO.size() + " contratos activos y vigentes");
            return Response.ok(contratosDTO).build();
        } catch (Exception e) {
            System.out.println("❌ Error al obtener contratos activos: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al obtener contratos activos\"}")
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Integer id) {
        try {
            ContratoModel contrato = contratoRepository.findByIdContrato(id);
            if (contrato == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Contrato no encontrado\"}")
                        .build();
            }
            return Response.ok(convertirADTO(contrato)).build();
        } catch (Exception e) {
            System.out.println("❌ Error al obtener contrato: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al obtener contrato\"}")
                    .build();
        }
    }

    @POST
    public Response create(ContratoModel contratoInput) {
        try {
            System.out.println("📥 Recibiendo contrato para crear: " + contratoInput);
            System.out.println("📋 Descripción: " + contratoInput.getDescripcion());
            System.out.println("📋 Frecuencia: " + contratoInput.getFrecuencia());
            System.out.println("📋 ID Proveedor: " + contratoInput.getIdProveedor());
            System.out.println("📋 Estado: " + contratoInput.getEstado());

            ContratoModel contrato = new ContratoModel();
            contrato.setDescripcion(contratoInput.getDescripcion());
            contrato.setFrecuencia(contratoInput.getFrecuencia());
            contrato.setEstado(contratoInput.getEstado() != null ? contratoInput.getEstado() : true); // Respetar estado
                                                                                                      // del frontend
            contrato.setIdEstado(1); // Estado inicial: PLANIFICADO
            contrato.setFechaInicio(contratoInput.getFechaInicio());
            contrato.setFechaFin(contratoInput.getFechaFin());

            System.out.println("🔧 Estado seteado antes de guardar: " + contrato.getEstado());
            System.out.println("🔧 ID Estado seteado antes de guardar: " + contrato.getIdEstado());

            // Buscar proveedor por ID si se proporciona idProveedor
            if (contratoInput.getIdProveedor() != null) {
                System.out.println("🔍 Buscando proveedor con ID: " + contratoInput.getIdProveedor());
                ProveedorModel proveedor = proveedorRepository.findByIdProveedor(contratoInput.getIdProveedor());
                if (proveedor != null) {
                    contrato.setProveedor(proveedor);
                    System.out.println("✅ Proveedor asignado: " + proveedor.getNombre());
                } else {
                    System.out.println("⚠️ Proveedor no encontrado con ID: " + contratoInput.getIdProveedor());
                }
            } else {
                System.out.println("⚠️ No se proporcionó ID de proveedor");
            }

            contrato.setFechaCreacion(new Date());

            // Asignar usuario creación desde el token JWT
            try {
                String keycloakId = (String) request.getAttribute("keycloakId");
                String username = (String) request.getAttribute("username");
                System.out.println("🔍 Usuario actual - Keycloak ID: " + keycloakId + ", Username: " + username);

                if (keycloakId != null) {
                    UsuarioMantenimientoModel usuario = usuarioRepository.findByKeycloakId(keycloakId);
                    if (usuario != null) {
                        contrato.setUsuarioCreacion(usuario);
                        System.out.println("✅ Usuario creación asignado: " + usuario.getNombreCompleto());
                    } else {
                        System.out.println("⚠️ Usuario no encontrado en BD para Keycloak ID: " + keycloakId);
                    }
                }
            } catch (Exception e) {
                System.out.println("⚠️ Error al asignar usuario creación: " + e.getMessage());
            }
            contrato = contratoRepository.save(contrato);
            System.out.println("✅ Contrato creado con ID: " + contrato.getIdContrato());

            return Response.status(Response.Status.CREATED).entity(convertirADTO(contrato)).build();
        } catch (Exception e) {
            System.out.println("❌ Error al crear contrato: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al crear contrato: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("id") Integer id, ContratoModel contratoInput) {
        try {
            System.out.println("📝 Actualizando contrato ID: " + id);
            System.out.println("📥 Datos recibidos: " + contratoInput);

            // Buscar contrato existente
            ContratoModel contratoExistente = contratoRepository.findByIdContrato(id);
            if (contratoExistente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Contrato no encontrado\"}")
                        .build();
            }

            System.out.println("📋 Estado actual en BD: " + contratoExistente.getEstado());
            System.out.println("📥 Estado recibido del frontend: " + contratoInput.getEstado());
            System.out.println("📥 Tipo de estado recibido: "
                    + (contratoInput.getEstado() != null ? contratoInput.getEstado().getClass() : "null"));

            // Actualizar campos
            contratoExistente.setDescripcion(contratoInput.getDescripcion());
            contratoExistente.setFrecuencia(contratoInput.getFrecuencia());
            contratoExistente.setFechaInicio(contratoInput.getFechaInicio());
            contratoExistente.setFechaFin(contratoInput.getFechaFin());

            // Manejar estado - usar el del input o mantener el actual si no se envía
            if (contratoInput.getEstado() != null) {
                contratoExistente.setEstado(contratoInput.getEstado());
                System.out.println("🔧 Estado actualizado a: " + contratoInput.getEstado());
            } else {
                System.out.println("⚠️ Estado no proporcionado, manteniendo: " + contratoExistente.getEstado());
            }

            // Manejar ID estado - usar el del input o mantener el actual si no se envía
            if (contratoInput.getIdEstado() != null) {
                contratoExistente.setIdEstado(contratoInput.getIdEstado());
                System.out.println("🔧 ID Estado actualizado a: " + contratoInput.getIdEstado());
            }

            // Buscar proveedor por ID si se proporciona idProveedor
            if (contratoInput.getIdProveedor() != null) {
                System.out.println("🔍 Buscando proveedor con ID: " + contratoInput.getIdProveedor());
                ProveedorModel proveedor = proveedorRepository.findByIdProveedor(contratoInput.getIdProveedor());
                if (proveedor != null) {
                    contratoExistente.setProveedor(proveedor);
                    System.out.println("✅ Proveedor actualizado: " + proveedor.getNombre());
                } else {
                    System.out.println("⚠️ Proveedor no encontrado con ID: " + contratoInput.getIdProveedor());
                }
            }

            // ✅ FECHA Y USUARIO DE MODIFICACIÓN
            contratoExistente.setFechaModificacion(new Date());

            // Asignar usuario modificación desde el token JWT
            try {
                String keycloakId = (String) request.getAttribute("keycloakId");
                String username = (String) request.getAttribute("username");
                System.out.println("🔍 Usuario modificador - Keycloak ID: " + keycloakId + ", Username: " + username);

                if (keycloakId != null) {
                    UsuarioMantenimientoModel usuario = usuarioRepository.findByKeycloakId(keycloakId);
                    if (usuario != null) {
                        contratoExistente.setUsuarioModificacion(usuario);
                        System.out.println("✅ Usuario modificación asignado: " + usuario.getNombreCompleto());
                    } else {
                        System.out.println("⚠️ Usuario no encontrado en BD para Keycloak ID: " + keycloakId);
                    }
                }
            } catch (Exception e) {
                System.out.println("⚠️ Error al asignar usuario modificación: " + e.getMessage());
            }

            contratoExistente = contratoRepository.save(contratoExistente);
            System.out.println("✅ Contrato actualizado con ID: " + contratoExistente.getIdContrato());

            return Response.ok(convertirADTO(contratoExistente)).build();
        } catch (Exception e) {
            System.out.println("❌ Error al actualizar contrato: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al actualizar contrato: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    // 🔧 MÉTODO AUXILIAR PARA CONVERTIR A DTO
    private Map<String, Object> convertirADTO(ContratoModel contrato) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", contrato.getIdContrato());
        dto.put("fechaInicio", contrato.getFechaInicio());
        dto.put("fechaFin", contrato.getFechaFin());
        dto.put("descripcion", contrato.getDescripcion());
        dto.put("frecuencia", contrato.getFrecuencia());
        dto.put("estado", contrato.getEstado());
        dto.put("fechaCreacion", contrato.getFechaCreacion());
        dto.put("fechaModificacion", contrato.getFechaModificacion());

        // Información del proveedor de forma segura
        try {
            if (contrato.getProveedor() != null) {
                dto.put("proveedor", contrato.getProveedor().getNombre());
                dto.put("idProveedor", contrato.getProveedor().getIdProveedor());
            } else {
                dto.put("proveedor", "Sin proveedor");
                dto.put("idProveedor", null);
            }
        } catch (Exception e) {
            dto.put("proveedor", "Error al cargar proveedor");
            dto.put("idProveedor", null);
        }

        // Información del usuario de forma segura
        try {
            if (contrato.getUsuarioCreacion() != null) {
                dto.put("usuarioCreacion", contrato.getUsuarioCreacion().getNombreCompleto());
            } else {
                dto.put("usuarioCreacion", "Sistema");
            }
        } catch (Exception e) {
            dto.put("usuarioCreacion", "Sistema");
        }

        // Estados calculados básicos
        dto.put("vigente", contrato.getEstado() != null ? contrato.getEstado() : false);
        dto.put("proximoAVencer", false); // Por ahora false
        dto.put("estadoDescriptivo", contrato.getEstado() ? "Vigente" : "Inactivo");

        // 🆕 CONTEO DE ARCHIVOS ADJUNTOS
        try {
            String sqlArchivos = "SELECT COUNT(*) FROM Documentos_Contrato WHERE id_contrato = ?";
            Number totalArchivos = (Number) em.createNativeQuery(sqlArchivos)
                    .setParameter(1, contrato.getIdContrato())
                    .getSingleResult();
            dto.put("totalArchivos", totalArchivos.intValue());
        } catch (Exception e) {
            System.out.println(
                    "⚠️ Error al contar archivos para contrato " + contrato.getIdContrato() + ": " + e.getMessage());
            dto.put("totalArchivos", 0);
        }

        return dto;
    }

    /**
     * 🆕 ENDPOINT ESPECÍFICO PARA PROGRAMACIONES
     * Obtiene contratos vigentes (solo por fechas, sin filtrar por equipo/tipo)
     */
    @GET
    @Path("/vigentes")
    public Response getContratosVigentes(
            @QueryParam("equipoId") Integer equipoId,
            @QueryParam("tipoMantenimientoId") Integer tipoMantenimientoId) {
        try {
            System.out.println("🔍 Buscando contratos vigentes (sin filtrar por equipo/tipo)");

            // 🚨 SIMPLIFICADO: Solo contratos vigentes por fechas
            String sql = "SELECT DISTINCT c.id_contrato, c.descripcion, c.fecha_inicio, c.fecha_fin, " +
                    "c.frecuencia, c.estado, " +
                    "p.id_proveedor, p.nombre as proveedor_nombre, p.nit " +
                    "FROM Contratos c " +
                    "INNER JOIN Proveedores p ON c.id_proveedor = p.id_proveedor " +
                    "WHERE c.estado = 1 " +
                    "AND c.fecha_inicio <= GETDATE() " +
                    "AND c.fecha_fin >= GETDATE() " +
                    "ORDER BY c.fecha_fin ASC";

            javax.persistence.Query query = em.createNativeQuery(sql);

            @SuppressWarnings("unchecked")
            List<Object[]> resultados = query.getResultList();

            List<Map<String, Object>> contratosDTO = new ArrayList<>();

            for (Object[] row : resultados) {
                Map<String, Object> dto = new HashMap<>();
                dto.put("idContrato", row[0]);
                dto.put("descripcion", row[1] != null ? row[1].toString() : "Sin descripción");
                dto.put("fechaInicio", row[2] != null ? row[2].toString() : null);
                dto.put("fechaFin", row[3] != null ? row[3].toString() : null);
                dto.put("frecuencia", row[4] != null ? row[4].toString() : "");
                dto.put("estado", row[5] != null ? row[5] : false);

                // Información del proveedor como strings simples
                dto.put("idProveedor", row[6]);
                dto.put("proveedorNombre", row[7] != null ? row[7].toString() : "Sin proveedor");
                dto.put("proveedorNit", row[8] != null ? row[8].toString() : "");

                // Descripción completa para el dropdown
                String descripcion = row[1] != null ? row[1].toString() : "Sin descripción";
                String proveedor = row[7] != null ? row[7].toString() : "Sin proveedor";
                dto.put("descripcionCompleta", descripcion + " - " + proveedor);

                contratosDTO.add(dto);
            }

            System.out.println("✅ Encontrados " + contratosDTO.size() + " contratos vigentes");
            return Response.ok(contratosDTO).build();

        } catch (Exception e) {
            System.out.println("❌ Error al obtener contratos vigentes: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al obtener contratos vigentes\"}")
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Integer id) {
        try {
            System.out.println("🗑️ Intentando eliminar contrato ID: " + id);

            ContratoModel contrato = contratoRepository.findByIdContrato(id);
            if (contrato == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Contrato no encontrado\", \"success\": false}")
                        .build();
            }

            // Primero eliminar documentos asociados
            String deleteDocumentosSQL = "DELETE FROM Documentos_Contrato WHERE id_contrato = ?";
            int documentosEliminados = em.createNativeQuery(deleteDocumentosSQL)
                    .setParameter(1, id)
                    .executeUpdate();
            System.out.println("🗑️ Documentos eliminados: " + documentosEliminados);

            // Luego eliminar relaciones equipo-contrato
            String deleteRelacionesSQL = "DELETE FROM Contrato_Equipo WHERE id_contrato = ?";
            int relacionesEliminadas = em.createNativeQuery(deleteRelacionesSQL)
                    .setParameter(1, id)
                    .executeUpdate();
            System.out.println("🗑️ Relaciones contrato-equipo eliminadas: " + relacionesEliminadas);

            // Finalmente eliminar el contrato usando EntityManager (merge antes de remove)
            ContratoModel managedContrato = em.merge(contrato);
            em.remove(managedContrato);
            em.flush();
            System.out.println("✅ Contrato eliminado correctamente");

            return Response.ok(
                    "{\"message\": \"Contrato y documentos asociados eliminados correctamente\", \"success\": true}")
                    .build();

        } catch (Exception e) {
            System.out.println("❌ Error al eliminar contrato: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al eliminar contrato: " + e.getMessage() + "\", \"success\": false}")
                    .build();
        }
    }
}
