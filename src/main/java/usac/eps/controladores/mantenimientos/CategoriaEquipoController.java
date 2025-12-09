package usac.eps.controladores.mantenimientos;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import usac.eps.modelos.mantenimientos.CategoriaEquipoModel;
import usac.eps.modelos.mantenimientos.UsuarioMantenimientoModel;
import usac.eps.repositorios.mantenimientos.CategoriaEquipoRepository;
import usac.eps.repositorios.mantenimientos.UsuarioMantenimientoRepository;

@Path("/categorias-equipos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class CategoriaEquipoController {
    @Inject
    private CategoriaEquipoRepository categoriaRepository;

    @Inject
    private UsuarioMantenimientoRepository usuarioRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Context
    private HttpServletRequest request;

    @GET
    public Response getAll(@QueryParam("soloActivas") @DefaultValue("false") boolean soloActivas) {
        List<CategoriaEquipoModel> categorias = soloActivas
                ? categoriaRepository.findByEstadoOrdenadas(true)
                : categoriaRepository.findAllOrdenadas();

        List<CategoriaDTO> resultado = new ArrayList<>();
        for (CategoriaEquipoModel categoria : categorias) {
            resultado.add(toDTO(categoria, false));
        }
        return Response.ok(resultado).build();
    }

    @GET
    @Path("/tree")
    public Response getTree(@QueryParam("soloActivas") @DefaultValue("true") boolean soloActivas) {
        List<CategoriaEquipoModel> categorias = soloActivas
                ? categoriaRepository.findByEstadoOrdenadas(true)
                : categoriaRepository.findAllOrdenadas();

        Map<Integer, CategoriaDTO> nodos = new HashMap<>();
        List<CategoriaDTO> roots = new ArrayList<>();

        // Crear nodos
        for (CategoriaEquipoModel categoria : categorias) {
            nodos.put(categoria.getIdCategoria(), toDTO(categoria, true));
        }

        // Construir árbol
        for (CategoriaDTO nodo : nodos.values()) {
            if (nodo.getIdPadre() != null) {
                CategoriaDTO padre = nodos.get(nodo.getIdPadre());
                if (padre != null) {
                    padre.getSubcategorias().add(nodo);
                } else {
                    roots.add(nodo);
                }
            } else {
                roots.add(nodo);
            }
        }

        return Response.ok(roots).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Integer id) {
        CategoriaEquipoModel categoria = categoriaRepository.findByIdCategoria(id);
        if (categoria == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Categoría no encontrada\"}")
                    .build();
        }
        return Response.ok(toDTO(categoria, true)).build();
    }

    @POST
    @Transactional
    public Response create(CategoriaEquipoModel categoriaInput) {
        try {
            validarNombre(categoriaInput.getNombre(), categoriaInput.getIdPadre(), null);
            CategoriaEquipoModel padre = validarPadre(categoriaInput.getIdPadre(), null);

            CategoriaEquipoModel categoria = new CategoriaEquipoModel();
            categoria.setNombre(categoriaInput.getNombre());
            categoria.setDescripcion(categoriaInput.getDescripcion());
            categoria.setEstado(categoriaInput.getEstado() != null ? categoriaInput.getEstado() : true);
            categoria.setIdPadre(categoriaInput.getIdPadre());
            categoria.setCategoriaPadre(padre);
            categoria.setUsuarioCreacion(obtenerUsuarioActual());
            categoria.setFechaCreacion(new Date());

            categoriaRepository.save(categoria);
            return Response.status(Response.Status.CREATED).entity(toDTO(categoria, false)).build();
        } catch (WebApplicationException ex) {
            return ex.getResponse();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al crear la categoría: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") Integer id, CategoriaEquipoModel categoriaInput) {
        try {
            CategoriaEquipoModel categoria = categoriaRepository.findByIdCategoria(id);
            if (categoria == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Categoría no encontrada\"}")
                        .build();
            }

            validarNombre(categoriaInput.getNombre(), categoriaInput.getIdPadre(), id);
            CategoriaEquipoModel padre = validarPadre(categoriaInput.getIdPadre(), id);

            categoria.setNombre(categoriaInput.getNombre());
            categoria.setDescripcion(categoriaInput.getDescripcion());
            categoria
                    .setEstado(categoriaInput.getEstado() != null ? categoriaInput.getEstado() : categoria.getEstado());
            categoria.setIdPadre(categoriaInput.getIdPadre());
            categoria.setCategoriaPadre(padre);
            categoria.setFechaModificacion(new Date());

            categoriaRepository.save(categoria);
            return Response.ok(toDTO(categoria, false)).build();
        } catch (WebApplicationException ex) {
            return ex.getResponse();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error al actualizar la categoría: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Integer id) {
        CategoriaEquipoModel categoria = categoriaRepository.findByIdCategoria(id);
        if (categoria == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Categoría no encontrada\"}")
                    .build();
        }

        Long hijos = categoriaRepository.countByPadre(id);
        if (hijos != null && hijos > 0) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\": \"La categoría tiene subcategorías asociadas\"}")
                    .build();
        }

        Long equipos = (Long) entityManager
                .createQuery("SELECT COUNT(e) FROM EquipoModel e WHERE e.idCategoria = :id")
                .setParameter("id", id)
                .getSingleResult();

        if (equipos != null && equipos > 0) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\": \"La categoría tiene equipos asociados\"}")
                    .build();
        }

        categoriaRepository.remove(categoria);
        return Response.noContent().build();
    }

    private void validarNombre(String nombre, Integer idPadre, Integer idActual) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"El nombre es obligatorio\"}")
                    .build());
        }

        List<CategoriaEquipoModel> existentes = categoriaRepository.findByNombreAndPadre(nombre.trim(), idPadre);
        if (existentes != null) {
            for (CategoriaEquipoModel existente : existentes) {
                if (existente != null && (idActual == null || !existente.getIdCategoria().equals(idActual))) {
                    throw new WebApplicationException(Response.status(Response.Status.CONFLICT)
                            .entity("{\"error\": \"Ya existe una categoría con ese nombre en el mismo nivel\"}")
                            .build());
                }
            }
        }
    }

    private CategoriaEquipoModel validarPadre(Integer idPadre, Integer idActual) {
        if (idPadre == null) {
            return null;
        }

        if (idActual != null && idPadre.equals(idActual)) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Una categoría no puede ser su propio padre\"}")
                    .build());
        }

        CategoriaEquipoModel padre = categoriaRepository.findByIdCategoria(idPadre);
        if (padre == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"La categoría padre no existe\"}")
                    .build());
        }

        if (idActual != null && esDescendiente(idActual, idPadre)) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"No se puede asignar un hijo como padre\"}")
                    .build());
        }

        return padre;
    }

    private boolean esDescendiente(Integer categoriaId, Integer posiblePadreId) {
        Integer actual = posiblePadreId;
        int maxDepth = 50; // evitar ciclos infinitos
        while (actual != null && maxDepth-- > 0) {
            if (actual.equals(categoriaId)) {
                return true;
            }
            CategoriaEquipoModel categoria = categoriaRepository.findByIdCategoria(actual);
            if (categoria == null) {
                break;
            }
            actual = categoria.getIdPadre();
        }
        return false;
    }

    private UsuarioMantenimientoModel obtenerUsuarioActual() {
        try {
            String keycloakId = (String) request.getAttribute("keycloakId");
            if (keycloakId != null) {
                return usuarioRepository.findByKeycloakId(keycloakId);
            }
        } catch (Exception e) {
            System.out.println("⚠️ No se pudo obtener usuario de Keycloak: " + e.getMessage());
        }
        return null;
    }

    private CategoriaDTO toDTO(CategoriaEquipoModel categoria, boolean incluirHijos) {
        CategoriaDTO dto = new CategoriaDTO();
        dto.setId(categoria.getIdCategoria());
        dto.setNombre(categoria.getNombre());
        dto.setDescripcion(categoria.getDescripcion());
        dto.setEstado(categoria.getEstado());
        dto.setIdPadre(categoria.getIdPadre());
        dto.setPadreNombre(categoria.getCategoriaPadre() != null ? categoria.getCategoriaPadre().getNombre() : null);
        dto.setTotalEquipos(contarEquipos(categoria.getIdCategoria()));
        dto.setCreatedAt(categoria.getFechaCreacion());
        dto.setUpdatedAt(categoria.getFechaModificacion());
        if (incluirHijos) {
            dto.setSubcategorias(new ArrayList<>());
        }
        return dto;
    }

    private Long contarEquipos(Integer idCategoria) {
        return (Long) entityManager
                .createQuery("SELECT COUNT(e) FROM EquipoModel e WHERE e.idCategoria = :id")
                .setParameter("id", idCategoria)
                .getSingleResult();
    }

    public static class CategoriaDTO {
        private Integer id;
        private String nombre;
        private String descripcion;
        private Boolean estado;
        private Integer idPadre;
        private String padreNombre;
        private Long totalEquipos;
        private Date createdAt;
        private Date updatedAt;
        private List<CategoriaDTO> subcategorias;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }

        public Boolean getEstado() {
            return estado;
        }

        public void setEstado(Boolean estado) {
            this.estado = estado;
        }

        public Integer getIdPadre() {
            return idPadre;
        }

        public void setIdPadre(Integer idPadre) {
            this.idPadre = idPadre;
        }

        public String getPadreNombre() {
            return padreNombre;
        }

        public void setPadreNombre(String padreNombre) {
            this.padreNombre = padreNombre;
        }

        public Long getTotalEquipos() {
            return totalEquipos;
        }

        public void setTotalEquipos(Long totalEquipos) {
            this.totalEquipos = totalEquipos;
        }

        public Date getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Date createdAt) {
            this.createdAt = createdAt;
        }

        public Date getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(Date updatedAt) {
            this.updatedAt = updatedAt;
        }

        public List<CategoriaDTO> getSubcategorias() {
            if (subcategorias == null) {
                subcategorias = new ArrayList<>();
            }
            return subcategorias;
        }

        public void setSubcategorias(List<CategoriaDTO> subcategorias) {
            this.subcategorias = subcategorias;
        }
    }
}
