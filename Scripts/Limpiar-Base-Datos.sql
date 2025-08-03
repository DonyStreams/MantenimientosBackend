-- ===============================
-- SCRIPT DE LIMPIEZA COMPLETA - SISTEMA MANTENIMIENTOS INACIF
-- ===============================
-- Versión: 1.0
-- Fecha: 2025-08-03
-- Descripción: Script para limpiar completamente la base de datos

PRINT '=============================================='
PRINT 'INICIANDO LIMPIEZA COMPLETA DE BASE DE DATOS'
PRINT 'SISTEMA DE MANTENIMIENTOS INACIF'
PRINT 'Fecha: ' + CONVERT(VARCHAR, GETDATE(), 120)
PRINT '=============================================='

-- ===============================
-- FASE 1: ELIMINAR VISTAS
-- ===============================
PRINT 'FASE 1: Eliminando vistas...'

IF EXISTS (SELECT * FROM sys.views WHERE name = 'VW_AlertasMantenimiento')
BEGIN
    DROP VIEW VW_AlertasMantenimiento;
    PRINT '   ✓ Vista VW_AlertasMantenimiento eliminada'
END

IF EXISTS (SELECT * FROM sys.views WHERE name = 'vw_DashboardMantenimientos')
BEGIN
    DROP VIEW vw_DashboardMantenimientos;
    PRINT '   ✓ Vista vw_DashboardMantenimientos eliminada'
END

-- ===============================
-- FASE 2: ELIMINAR PROCEDIMIENTOS ALMACENADOS
-- ===============================
PRINT 'FASE 2: Eliminando procedimientos almacenados...'

IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[GenerarNotificacionesAutomaticas]') AND type in (N'P', N'PC'))
BEGIN
    DROP PROCEDURE [dbo].[GenerarNotificacionesAutomaticas];
    PRINT '   ✓ Procedimiento GenerarNotificacionesAutomaticas eliminado'
END

IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SP_CalcularProximoMantenimiento]') AND type in (N'P', N'PC'))
BEGIN
    DROP PROCEDURE [dbo].[SP_CalcularProximoMantenimiento];
    PRINT '   ✓ Procedimiento SP_CalcularProximoMantenimiento eliminado'
END

IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SP_DashboardAlertas]') AND type in (N'P', N'PC'))
BEGIN
    DROP PROCEDURE [dbo].[SP_DashboardAlertas];
    PRINT '   ✓ Procedimiento SP_DashboardAlertas eliminado'
END

-- ===============================
-- FASE 3: ELIMINAR FUNCIONES
-- ===============================
PRINT 'FASE 3: Eliminando funciones...'

IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[ObtenerProximosMantenimientos]') AND type in (N'FN', N'IF', N'TF'))
BEGIN
    DROP FUNCTION [dbo].[ObtenerProximosMantenimientos];
    PRINT '   ✓ Función ObtenerProximosMantenimientos eliminada'
END

IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[FN_EquipoNecesitaMantenimiento]') AND type in (N'FN'))
BEGIN
    DROP FUNCTION [dbo].[FN_EquipoNecesitaMantenimiento];
    PRINT '   ✓ Función FN_EquipoNecesitaMantenimiento eliminada'
END

-- ===============================
-- FASE 4: ELIMINAR TRIGGERS
-- ===============================
PRINT 'FASE 4: Eliminando triggers...'

IF EXISTS (SELECT * FROM sys.objects WHERE name = 'TR_ProgramacionMantenimiento_UpdateFechaModificacion')
BEGIN
    DROP TRIGGER TR_ProgramacionMantenimiento_UpdateFechaModificacion;
    PRINT '   ✓ Trigger TR_ProgramacionMantenimiento_UpdateFechaModificacion eliminado'
END

-- ===============================
-- FASE 5: ELIMINAR RESTRICCIONES FK (FOREIGN KEYS)
-- ===============================
PRINT 'FASE 5: Eliminando restricciones de llaves foráneas...'

-- Función para eliminar todas las FK automáticamente
DECLARE @sql NVARCHAR(MAX) = ''
SELECT @sql = @sql + 'ALTER TABLE [' + SCHEMA_NAME(schema_id) + '].[' + OBJECT_NAME(parent_object_id) + '] DROP CONSTRAINT [' + name + '];' + CHAR(13)
FROM sys.foreign_keys

IF LEN(@sql) > 0
BEGIN
    EXEC sp_executesql @sql
    PRINT '   ✓ Todas las restricciones FK eliminadas'
END

-- ===============================
-- FASE 6: ELIMINAR TABLAS EN ORDEN CORRECTO
-- ===============================
PRINT 'FASE 6: Eliminando tablas...'

-- Tablas de relaciones y dependientes primero
DROP TABLE IF EXISTS Evidencias;
PRINT '   ✓ Tabla Evidencias eliminada'

DROP TABLE IF EXISTS Comentarios_Ticket;
PRINT '   ✓ Tabla Comentarios_Ticket eliminada'

DROP TABLE IF EXISTS Tipos_Comentario;
PRINT '   ✓ Tabla Tipos_Comentario eliminada'

DROP TABLE IF EXISTS Tickets;
PRINT '   ✓ Tabla Tickets eliminada'

DROP TABLE IF EXISTS Programaciones_Mantenimiento;
PRINT '   ✓ Tabla Programaciones_Mantenimiento eliminada'

DROP TABLE IF EXISTS Documentos_Contrato;
PRINT '   ✓ Tabla Documentos_Contrato eliminada'

DROP TABLE IF EXISTS Notificaciones;
PRINT '   ✓ Tabla Notificaciones eliminada'

DROP TABLE IF EXISTS Configuracion_Alertas;
PRINT '   ✓ Tabla Configuracion_Alertas eliminada'

DROP TABLE IF EXISTS Seguimiento_Estado_Mantenimiento;
PRINT '   ✓ Tabla Seguimiento_Estado_Mantenimiento eliminada'

DROP TABLE IF EXISTS Ejecuciones_Mantenimiento;
PRINT '   ✓ Tabla Ejecuciones_Mantenimiento eliminada'

DROP TABLE IF EXISTS Contrato_Tipo_Mantenimiento;
PRINT '   ✓ Tabla Contrato_Tipo_Mantenimiento eliminada'

DROP TABLE IF EXISTS Contrato_Equipo;
PRINT '   ✓ Tabla Contrato_Equipo eliminada'

DROP TABLE IF EXISTS Contratos;
PRINT '   ✓ Tabla Contratos eliminada'

DROP TABLE IF EXISTS Estados_Mantenimiento;
PRINT '   ✓ Tabla Estados_Mantenimiento eliminada'

DROP TABLE IF EXISTS Proveedores;
PRINT '   ✓ Tabla Proveedores eliminada'

DROP TABLE IF EXISTS Tipos_Mantenimiento;
PRINT '   ✓ Tabla Tipos_Mantenimiento eliminada'

DROP TABLE IF EXISTS Historial_Equipo;
PRINT '   ✓ Tabla Historial_Equipo eliminada'

DROP TABLE IF EXISTS Equipos;
PRINT '   ✓ Tabla Equipos eliminada'

DROP TABLE IF EXISTS Areas;
PRINT '   ✓ Tabla Areas eliminada'

DROP TABLE IF EXISTS Usuarios;
PRINT '   ✓ Tabla Usuarios eliminada'

-- ===============================
-- FASE 7: ELIMINAR ÍNDICES HUÉRFANOS (SI QUEDAN)
-- ===============================
PRINT 'FASE 7: Limpiando índices huérfanos...'

-- Los índices se eliminan automáticamente con las tablas, pero por si acaso
DECLARE @index_sql NVARCHAR(MAX) = ''
SELECT @index_sql = @index_sql + 
    'IF EXISTS (SELECT * FROM sys.indexes WHERE name = ''' + i.name + ''') ' +
    'DROP INDEX [' + i.name + '] ON [' + SCHEMA_NAME(t.schema_id) + '].[' + t.name + '];' + CHAR(13)
FROM sys.indexes i
INNER JOIN sys.tables t ON i.object_id = t.object_id
WHERE i.name LIKE 'IX_%'
AND t.name IN ('Contratos', 'Equipos', 'Tickets', 'Programaciones_Mantenimiento')

IF LEN(@index_sql) > 0
BEGIN
    EXEC sp_executesql @index_sql
    PRINT '   ✓ Índices específicos eliminados'
END

-- ===============================
-- FASE 8: VERIFICACIÓN FINAL
-- ===============================
PRINT 'FASE 8: Verificando limpieza...'

DECLARE @tablas_restantes INT = 0
DECLARE @vistas_restantes INT = 0
DECLARE @procedimientos_restantes INT = 0
DECLARE @funciones_restantes INT = 0

-- Contar objetos del sistema que podrían quedar
SELECT @tablas_restantes = COUNT(*)
FROM sys.tables 
WHERE name IN ('Usuarios', 'Areas', 'Equipos', 'Historial_Equipo', 'Tipos_Mantenimiento', 
               'Proveedores', 'Estados_Mantenimiento', 'Contratos', 'Contrato_Equipo', 
               'Contrato_Tipo_Mantenimiento', 'Ejecuciones_Mantenimiento', 'Tickets', 
               'Tipos_Comentario', 'Comentarios_Ticket', 'Evidencias', 'Seguimiento_Estado_Mantenimiento',
               'Notificaciones', 'Configuracion_Alertas', 'Documentos_Contrato', 'Programaciones_Mantenimiento')

SELECT @vistas_restantes = COUNT(*)
FROM sys.views 
WHERE name IN ('VW_AlertasMantenimiento', 'vw_DashboardMantenimientos')

SELECT @procedimientos_restantes = COUNT(*)
FROM sys.objects 
WHERE type IN ('P', 'PC') 
AND name IN ('GenerarNotificacionesAutomaticas', 'SP_CalcularProximoMantenimiento', 'SP_DashboardAlertas')

SELECT @funciones_restantes = COUNT(*)
FROM sys.objects 
WHERE type IN ('FN', 'IF', 'TF') 
AND name IN ('ObtenerProximosMantenimientos', 'FN_EquipoNecesitaMantenimiento')

PRINT ''
PRINT '=============================================='
PRINT 'RESUMEN DE LIMPIEZA:'
PRINT 'Tablas restantes: ' + CAST(@tablas_restantes AS VARCHAR(10))
PRINT 'Vistas restantes: ' + CAST(@vistas_restantes AS VARCHAR(10))
PRINT 'Procedimientos restantes: ' + CAST(@procedimientos_restantes AS VARCHAR(10))
PRINT 'Funciones restantes: ' + CAST(@funciones_restantes AS VARCHAR(10))

IF (@tablas_restantes + @vistas_restantes + @procedimientos_restantes + @funciones_restantes) = 0
    PRINT 'ESTADO: ✓ LIMPIEZA COMPLETA Y EXITOSA'
ELSE
    PRINT 'ESTADO: ⚠ ALGUNOS OBJETOS PODRÍAN QUEDAR - Verificar manualmente'

PRINT ''
PRINT '✅ BASE DE DATOS LIMPIA Y LISTA PARA NUEVA INSTALACIÓN'
PRINT 'Ahora puedes ejecutar el script de instalación completa'
PRINT '=============================================='
PRINT 'LIMPIEZA COMPLETADA'
PRINT 'Fecha: ' + CONVERT(VARCHAR, GETDATE(), 120)
PRINT '=============================================='
