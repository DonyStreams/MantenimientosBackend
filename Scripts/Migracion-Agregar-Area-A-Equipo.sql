-- =====================================================
-- MIGRACIÓN: Agregar relación de Área a Equipos
-- Fecha: 2025-11-27
-- Descripción: Relacionar cada equipo con un área/laboratorio
-- =====================================================

USE INACIF_Mantenimientos;
GO

PRINT '🔧 Iniciando migración: Agregar área a equipos...'
PRINT ''

-- Verificar si la columna ya existe
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Equipos') AND name = 'id_area')
BEGIN
    PRINT '➕ Agregando columna id_area a tabla Equipos...'
    
    -- Agregar columna id_area (NULL por defecto para equipos existentes)
    ALTER TABLE Equipos
    ADD id_area INT NULL;
    
    PRINT '✅ Columna id_area agregada correctamente'
    PRINT ''
    
    -- Agregar la clave foránea
    PRINT '🔗 Creando relación con tabla Areas...'
    ALTER TABLE Equipos
    ADD CONSTRAINT FK_Equipos_Areas 
    FOREIGN KEY (id_area) REFERENCES Areas(id_area);
    
    PRINT '✅ Relación creada correctamente'
    PRINT ''
    
    -- Crear índice para mejorar performance en consultas por área
    PRINT '📊 Creando índice para área...'
    CREATE INDEX IDX_Equipos_Area ON Equipos(id_area);
    
    PRINT '✅ Índice creado correctamente'
    PRINT ''
    
    PRINT '========================================='
    PRINT '✅ MIGRACIÓN COMPLETADA CON ÉXITO'
    PRINT '========================================='
    PRINT ''
    PRINT '📝 NOTAS:'
    PRINT '- Los equipos existentes tienen id_area = NULL'
    PRINT '- Debes asignar un área a cada equipo'
    PRINT '- Desde el frontend podrás seleccionar el área al crear/editar equipos'
    PRINT ''
    
END
ELSE
BEGIN
    PRINT '⚠️ La columna id_area ya existe en la tabla Equipos'
    PRINT 'No es necesario ejecutar esta migración'
END

GO
