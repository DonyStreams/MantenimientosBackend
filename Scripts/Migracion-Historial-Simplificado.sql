-- =====================================================
-- MIGRACIÓN: Sistema de Historial Simplificado
-- Fecha: 2025-11-27
-- Descripción: Agrega campos para trazabilidad de cambios
--              sin registrar cada campo individual
-- =====================================================

USE INACIF_Mantenimientos;
GO

PRINT '🔄 Iniciando migración de Historial_Equipo...'

-- =====================================================
-- 1. AGREGAR NUEVAS COLUMNAS
-- =====================================================

-- Tipo de cambio realizado
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Historial_Equipo') AND name = 'tipo_cambio')
BEGIN
    ALTER TABLE Historial_Equipo ADD tipo_cambio VARCHAR(50);
    PRINT '✅ Columna tipo_cambio agregada'
END
ELSE
BEGIN
    PRINT '⚠️  Columna tipo_cambio ya existe'
END

-- ID del usuario que realizó el cambio
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Historial_Equipo') AND name = 'usuario_id')
BEGIN
    ALTER TABLE Historial_Equipo ADD usuario_id INT;
    PRINT '✅ Columna usuario_id agregada'
END
ELSE
BEGIN
    PRINT '⚠️  Columna usuario_id ya existe'
END

-- Nombre del usuario (denormalizado para histórico)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Historial_Equipo') AND name = 'usuario_nombre')
BEGIN
    ALTER TABLE Historial_Equipo ADD usuario_nombre VARCHAR(100);
    PRINT '✅ Columna usuario_nombre agregada'
END
ELSE
BEGIN
    PRINT '⚠️  Columna usuario_nombre ya existe'
END

GO

-- =====================================================
-- 2. AGREGAR FOREIGN KEY (si no existe)
-- =====================================================

IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_Historial_Usuario')
BEGIN
    ALTER TABLE Historial_Equipo 
    ADD CONSTRAINT FK_Historial_Usuario 
    FOREIGN KEY (usuario_id) REFERENCES Usuarios(id);
    PRINT '✅ Foreign Key FK_Historial_Usuario agregada'
END
ELSE
BEGIN
    PRINT '⚠️  Foreign Key FK_Historial_Usuario ya existe'
END

GO

-- =====================================================
-- 3. ACTUALIZAR REGISTROS EXISTENTES
-- =====================================================

PRINT '🔄 Actualizando registros existentes...'

-- Marcar registros antiguos como ediciones generales
UPDATE Historial_Equipo 
SET tipo_cambio = 'EDICION_GENERAL',
    usuario_nombre = 'Sistema (histórico)'
WHERE tipo_cambio IS NULL;

PRINT '✅ Registros existentes actualizados'

GO

-- =====================================================
-- 4. LIMPIAR REGISTROS DETALLADOS (OPCIONAL)
-- =====================================================

-- Descomentar si quieres eliminar los registros muy detallados
/*
DECLARE @RegistrosEliminados INT;

DELETE FROM Historial_Equipo 
WHERE descripcion LIKE 'Campo%'
   OR descripcion LIKE 'Se cambió%de%a%';

SET @RegistrosEliminados = @@ROWCOUNT;
PRINT CONCAT('🗑️  ', @RegistrosEliminados, ' registros detallados eliminados')
*/

GO

-- =====================================================
-- 5. CREAR ÍNDICES PARA MEJOR RENDIMIENTO
-- =====================================================

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_Historial_Equipo_Tipo')
BEGIN
    CREATE INDEX IX_Historial_Equipo_Tipo ON Historial_Equipo(tipo_cambio);
    PRINT '✅ Índice IX_Historial_Equipo_Tipo creado'
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_Historial_Equipo_Usuario')
BEGIN
    CREATE INDEX IX_Historial_Equipo_Usuario ON Historial_Equipo(usuario_id);
    PRINT '✅ Índice IX_Historial_Equipo_Usuario creado'
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_Historial_Equipo_Fecha')
BEGIN
    CREATE INDEX IX_Historial_Equipo_Fecha ON Historial_Equipo(fecha_registro DESC);
    PRINT '✅ Índice IX_Historial_Equipo_Fecha creado'
END

GO

-- =====================================================
-- 6. DOCUMENTACIÓN DE TIPOS DE CAMBIO
-- =====================================================

PRINT ''
PRINT '📚 TIPOS DE CAMBIO DISPONIBLES:'
PRINT '   - CREACION: Equipo registrado en el sistema'
PRINT '   - EDICION_GENERAL: Información del equipo actualizada'
PRINT '   - CAMBIO_IMAGEN: Fotografía del equipo actualizada'
PRINT '   - CAMBIO_UBICACION: Ubicación física modificada'
PRINT '   - CAMBIO_ESTADO: Estado operativo modificado'
PRINT '   - MANTENIMIENTO: Mantenimiento realizado'
PRINT '   - CALIBRACION: Calibración realizada'
PRINT '   - REPARACION: Reparación realizada'
PRINT ''

-- =====================================================
-- 7. VERIFICACIÓN FINAL
-- =====================================================

PRINT '🔍 Verificando estructura final...'

SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'Historial_Equipo'
ORDER BY ORDINAL_POSITION;

PRINT ''
PRINT '✅ Migración completada exitosamente!'

-- Mostrar total de registros
DECLARE @TotalRegistros INT;
SELECT @TotalRegistros = COUNT(*) FROM Historial_Equipo;
PRINT '📊 Total de registros en Historial_Equipo: ' + CAST(@TotalRegistros AS VARCHAR)
PRINT ''

GO
