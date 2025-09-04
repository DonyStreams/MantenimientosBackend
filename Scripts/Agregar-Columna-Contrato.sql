-- ===============================
-- MIGRACIÓN SIMPLE: AGREGAR COLUMNA id_contrato
-- ===============================
-- Fecha: 2025-09-02
-- Descripción: Agregar relación entre Programaciones y Contratos

PRINT 'Agregando columna id_contrato a Programaciones_Mantenimiento...'

-- Verificar si la columna ya existe
IF NOT EXISTS (
    SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'Programaciones_Mantenimiento' 
    AND COLUMN_NAME = 'id_contrato'
)
BEGIN
    -- Agregar la columna
    ALTER TABLE Programaciones_Mantenimiento 
    ADD id_contrato INT;
    
    -- Agregar la clave foránea
    ALTER TABLE Programaciones_Mantenimiento 
    ADD CONSTRAINT FK_Programaciones_Contrato 
    FOREIGN KEY (id_contrato) REFERENCES Contratos(id_contrato);
    
    PRINT 'Columna id_contrato agregada exitosamente'
END
ELSE
BEGIN
    PRINT 'La columna id_contrato ya existe'
END

PRINT 'Migración completada'
