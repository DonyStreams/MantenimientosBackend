-- ============================================
-- Script: Agregar nuevos tipos de evento al historial
-- Fecha: 2026-01-19
-- Descripción: Amplía los tipos de evento permitidos en Historial_Programacion
-- ============================================

USE MantenimientosINACIF;
GO

PRINT '==============================================='
PRINT 'MIGRACIÓN: Agregar tipos de evento al historial'
PRINT '==============================================='
PRINT ''

-- 1. Eliminar el constraint existente
IF EXISTS (SELECT * FROM sys.check_constraints WHERE name = 'CK_HistorialProg_TipoEvento')
BEGIN
    PRINT '1. Eliminando constraint anterior...'
    ALTER TABLE Historial_Programacion DROP CONSTRAINT CK_HistorialProg_TipoEvento;
    PRINT '   ✓ Constraint eliminado'
END
ELSE
BEGIN
    PRINT '1. Constraint no existe, continuando...'
END

-- 2. Crear el nuevo constraint con más tipos
PRINT '2. Creando nuevo constraint con tipos ampliados...'
ALTER TABLE Historial_Programacion 
ADD CONSTRAINT CK_HistorialProg_TipoEvento 
CHECK (tipo_evento IN (
    'EJECUTADO',      -- Cuando se ejecuta un mantenimiento
    'SALTADO',        -- Cuando se descarta/salta una fecha programada
    'REPROGRAMADO',   -- Cuando se cambia la fecha manualmente
    'EDITADO',        -- Cuando se editan los datos de la programación
    'PAUSADO',        -- Cuando se pausa/desactiva la programación
    'ACTIVADO',       -- Cuando se reactiva la programación
    'CREADO'          -- Cuando se crea la programación (opcional)
));
PRINT '   ✓ Nuevo constraint creado'

-- 3. Verificar
PRINT ''
PRINT '3. Verificación:'
SELECT 
    name AS NombreConstraint,
    definition AS Definicion
FROM sys.check_constraints 
WHERE name = 'CK_HistorialProg_TipoEvento';

PRINT ''
PRINT '==============================================='
PRINT '✅ MIGRACIÓN COMPLETADA'
PRINT '==============================================='
PRINT ''
PRINT 'Tipos de evento permitidos:'
PRINT '  - EJECUTADO:    Mantenimiento ejecutado'
PRINT '  - SALTADO:      Fecha descartada/saltada'
PRINT '  - REPROGRAMADO: Fecha cambiada manualmente'
PRINT '  - EDITADO:      Datos de programación editados'
PRINT '  - PAUSADO:      Programación pausada'
PRINT '  - ACTIVADO:     Programación reactivada'
PRINT '  - CREADO:       Programación creada'
GO
