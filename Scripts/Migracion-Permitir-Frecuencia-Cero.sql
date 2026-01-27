-- ============================================
-- Script: Permitir frecuencia 0 en programaciones
-- Fecha: 2026-01-19
-- Descripción: Elimina/modifica el constraint que impide frecuencia = 0
-- ============================================

USE MantenimientosINACIF;
GO

PRINT '==============================================='
PRINT 'MIGRACIÓN: Permitir frecuencia 0 (único)'
PRINT '==============================================='
PRINT ''

-- 1. Buscar y mostrar constraints en la columna frecuencia_dias
PRINT '1. Buscando constraints en frecuencia_dias...'
SELECT 
    cc.name AS NombreConstraint,
    cc.definition AS Definicion,
    t.name AS Tabla
FROM sys.check_constraints cc
JOIN sys.tables t ON cc.parent_object_id = t.object_id
WHERE t.name = 'Programaciones_Mantenimiento'
  AND cc.definition LIKE '%frecuencia%';

-- 2. Eliminar el constraint existente (nombre generado automáticamente)
PRINT ''
PRINT '2. Eliminando constraint de frecuencia...'

-- Buscar el nombre exacto del constraint
DECLARE @constraintName NVARCHAR(200);
SELECT @constraintName = cc.name 
FROM sys.check_constraints cc
JOIN sys.tables t ON cc.parent_object_id = t.object_id
WHERE t.name = 'Programaciones_Mantenimiento'
  AND cc.definition LIKE '%frecuencia%';

IF @constraintName IS NOT NULL
BEGIN
    DECLARE @sql NVARCHAR(500) = 'ALTER TABLE Programaciones_Mantenimiento DROP CONSTRAINT ' + @constraintName;
    EXEC sp_executesql @sql;
    PRINT '   ✓ Constraint eliminado: ' + @constraintName;
END
ELSE
BEGIN
    PRINT '   ⚠ No se encontró constraint de frecuencia'
END

-- 3. Crear nuevo constraint que permita 0 (frecuencia única)
PRINT ''
PRINT '3. Creando nuevo constraint que permite 0...'
ALTER TABLE Programaciones_Mantenimiento 
ADD CONSTRAINT CK_Programacion_Frecuencia 
CHECK (frecuencia_dias >= 0);  -- Permite 0 para programaciones únicas

PRINT '   ✓ Nuevo constraint creado: CK_Programacion_Frecuencia (frecuencia >= 0)'

-- 4. Verificar
PRINT ''
PRINT '4. Verificación:'
SELECT 
    cc.name AS NombreConstraint,
    cc.definition AS Definicion
FROM sys.check_constraints cc
JOIN sys.tables t ON cc.parent_object_id = t.object_id
WHERE t.name = 'Programaciones_Mantenimiento'
  AND cc.definition LIKE '%frecuencia%';

PRINT ''
PRINT '==============================================='
PRINT '✅ MIGRACIÓN COMPLETADA'
PRINT '==============================================='
PRINT ''
PRINT 'Ahora se permite:'
PRINT '  - frecuencia_dias = 0  → Programación única (se ejecuta una vez)'
PRINT '  - frecuencia_dias > 0  → Programación recurrente'
GO
