-- =====================================================
-- VERIFICACIÓN: Columnas en Historial_Equipo
-- =====================================================

USE INACIF_Mantenimientos;
GO

PRINT '🔍 Verificando estructura de Historial_Equipo...'
PRINT ''

-- Ver todas las columnas
SELECT 
    COLUMN_NAME as 'Columna',
    DATA_TYPE as 'Tipo',
    CHARACTER_MAXIMUM_LENGTH as 'Tamaño',
    IS_NULLABLE as 'Permite NULL'
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'Historial_Equipo'
ORDER BY ORDINAL_POSITION;

PRINT ''
PRINT '✅ Columnas que DEBEN existir:'
PRINT '   - tipo_cambio (varchar 50)'
PRINT '   - usuario_id (int)'
PRINT '   - usuario_nombre (varchar 100)'
PRINT ''

-- Verificar si las columnas nuevas existen
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Historial_Equipo') AND name = 'tipo_cambio')
    PRINT '✅ Columna tipo_cambio EXISTE'
ELSE
    PRINT '❌ Columna tipo_cambio NO EXISTE - Ejecuta Migracion-Historial-Simplificado.sql'

IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Historial_Equipo') AND name = 'usuario_id')
    PRINT '✅ Columna usuario_id EXISTE'
ELSE
    PRINT '❌ Columna usuario_id NO EXISTE - Ejecuta Migracion-Historial-Simplificado.sql'

IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Historial_Equipo') AND name = 'usuario_nombre')
    PRINT '✅ Columna usuario_nombre EXISTE'
ELSE
    PRINT '❌ Columna usuario_nombre NO EXISTE - Ejecuta Migracion-Historial-Simplificado.sql'

PRINT ''
PRINT '📊 Registros en Historial_Equipo:'
SELECT COUNT(*) as 'Total Registros' FROM Historial_Equipo;

PRINT ''
PRINT '🔍 Últimos 5 registros:'
SELECT TOP 5
    id_historial,
    id_equipo,
    tipo_cambio,
    usuario_nombre,
    descripcion,
    fecha_registro
FROM Historial_Equipo
ORDER BY fecha_registro DESC;

GO
