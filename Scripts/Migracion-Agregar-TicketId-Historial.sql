-- ===============================
-- MIGRACIÓN: Agregar ticket_id a Historial_Equipo
-- ===============================
-- Fecha: 2026-01-25
-- Descripción: Agrega columna ticket_id para vincular historial con tickets
--              Permite eliminar historial cuando se elimina un ticket

PRINT '🔄 Iniciando migración - Agregar ticket_id a Historial_Equipo...'
PRINT ''

-- ===============================
-- PASO 1: Agregar columna ticket_id
-- ===============================
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Historial_Equipo') AND name = 'ticket_id')
BEGIN
    ALTER TABLE Historial_Equipo ADD ticket_id INT NULL;
    PRINT '✅ Columna ticket_id agregada a Historial_Equipo'
END
ELSE
BEGIN
    PRINT '⏭️ Columna ticket_id ya existe'
END

-- ===============================
-- PASO 2: Crear índice para búsquedas por ticket
-- ===============================
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_Historial_Equipo_Ticket')
BEGIN
    CREATE INDEX IX_Historial_Equipo_Ticket ON Historial_Equipo(ticket_id);
    PRINT '✅ Índice IX_Historial_Equipo_Ticket creado'
END
ELSE
BEGIN
    PRINT '⏭️ Índice IX_Historial_Equipo_Ticket ya existe'
END

-- ===============================
-- VERIFICACIÓN FINAL
-- ===============================
PRINT ''
PRINT '📋 Estructura actual de Historial_Equipo:'
SELECT 
    COLUMN_NAME as Columna,
    DATA_TYPE as Tipo,
    IS_NULLABLE as Nullable
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'Historial_Equipo'
ORDER BY ORDINAL_POSITION;

PRINT ''
PRINT '✅ Migración completada exitosamente'
PRINT '=============================================='
