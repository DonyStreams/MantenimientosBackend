package usac.eps.controladores.mantenimientos.mapper;

import usac.eps.controladores.mantenimientos.dto.EjecucionMantenimientoDTO;
import usac.eps.modelos.mantenimientos.EjecucionMantenimientoModel;

/**
 * Utilidad para transformar entidades de ejecuciones de mantenimiento a DTOs
 * ligeros
 */
public final class EjecucionMantenimientoMapper {

    private EjecucionMantenimientoMapper() {
    }

    public static EjecucionMantenimientoDTO toDTO(EjecucionMantenimientoModel model) {
        EjecucionMantenimientoDTO dto = new EjecucionMantenimientoDTO();
        dto.setIdEjecucion(model.getIdEjecucion());
        dto.setFechaEjecucion(model.getFechaEjecucion());
        dto.setFechaInicioTrabajo(model.getFechaInicioTrabajo());
        dto.setFechaCierre(model.getFechaCierre());
        dto.setEstado(model.getEstado());
        dto.setBitacora(model.getBitacora());

        if (model.getContrato() != null) {
            dto.setIdContrato(model.getContrato().getIdContrato());
            dto.setContratoDescripcion(model.getContrato().getDescripcion());
            if (model.getContrato().getProveedor() != null) {
                dto.setIdProveedor(model.getContrato().getProveedor().getIdProveedor());
                dto.setProveedorNombre(model.getContrato().getProveedor().getNombre());
            }
        }

        if (model.getEquipo() != null) {
            dto.setIdEquipo(model.getEquipo().getIdEquipo());
            dto.setEquipoNombre(model.getEquipo().getNombre());
            dto.setEquipoCodigo(model.getEquipo().getCodigoInacif());
            dto.setEquipoUbicacion(model.getEquipo().getUbicacion());
        }

        if (model.getProgramacion() != null) {
            dto.setIdProgramacion(model.getProgramacion().getIdProgramacion());
            dto.setFrecuenciaDias(model.getProgramacion().getFrecuenciaDias());
            dto.setFechaProximoProgramado(model.getProgramacion().getFechaProximoMantenimiento());
            if (model.getProgramacion().getTipoMantenimiento() != null) {
                dto.setTipoMantenimiento(model.getProgramacion().getTipoMantenimiento().getNombre());
            }
        }

        if (model.getUsuarioResponsable() != null) {
            dto.setUsuarioResponsableId(model.getUsuarioResponsable().getId());
            dto.setUsuarioResponsableNombre(model.getUsuarioResponsable().getNombreCompleto());
        }

        return dto;
    }
}
