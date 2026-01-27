-- =========================================================================
-- MIGRACIÓN: Agregar columna 'estado' a tabla Equipos
-- Fecha: 2026-01-16
-- Descripción: Agrega el campo estado con 3 valores posibles:
--              - Activo (operando normalmente)
--              - Inactivo (fuera de servicio)
--              - Critico (requiere atención urgente)
-- =========================================================================

-- 1. Agregar la columna estado con valor por defecto 'Activo'
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'Equipos' AND COLUMN_NAME = 'estado'
)
BEGIN
    ALTER TABLE Equipos ADD estado VARCHAR(20) DEFAULT 'Activo' NOT NULL;
    PRINT '✅ Columna estado agregada correctamente';
END
ELSE
BEGIN
    PRINT '⚠️ La columna estado ya existe';
END
GO

-- 2. Crear constraint para validar los valores permitidos
IF NOT EXISTS (
    SELECT 1 FROM sys.check_constraints 
    WHERE name = 'CK_Equipos_Estado' AND parent_object_id = OBJECT_ID('Equipos')
)
BEGIN
    ALTER TABLE Equipos 
    ADD CONSTRAINT CK_Equipos_Estado 
    CHECK (estado IN ('Activo', 'Inactivo', 'Critico'));
    PRINT '✅ Constraint CK_Equipos_Estado creada correctamente';
END
ELSE
BEGIN
    PRINT '⚠️ El constraint CK_Equipos_Estado ya existe';
END
GO

-- 3. Actualizar equipos existentes a estado 'Activo' (si no tienen valor)
UPDATE Equipos 
SET estado = 'Activo' 
WHERE estado IS NULL;

PRINT '✅ Migración completada exitosamente';
GO

-- 4. Verificar la migración
SELECT 
    'Total equipos: ' + CAST(COUNT(*) AS VARCHAR) AS Info
FROM Equipos;

SELECT 
    estado, 
    COUNT(*) AS cantidad 
FROM Equipos 
GROUP BY estado;
GO
