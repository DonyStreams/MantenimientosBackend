-- ===============================
-- MIGRACIÓN: AGREGAR RELACIÓN CONTRATO A PROGRAMACIONES
-- ===============================
-- Fecha: 2025-09-02
-- Descripción: Agregar columna id_contrato a Programaciones_Mantenimiento

PRINT 'Agregando columna id_contrato a Programaciones_Mantenimiento...'

-- Agregar la columna id_contrato
ALTER TABLE Programaciones_Mantenimiento 
ADD id_contrato INT NULL;

-- Agregar la clave foránea
ALTER TABLE Programaciones_Mantenimiento 
ADD CONSTRAINT FK_Programaciones_Contrato 
FOREIGN KEY (id_contrato) REFERENCES Contratos(id_contrato);

-- Crear índice para mejorar performance
CREATE INDEX IX_Programaciones_Contrato 
ON Programaciones_Mantenimiento(id_contrato);

PRINT 'Migración completada exitosamente!'
PRINT 'Nota: Las programaciones existentes tendrán id_contrato = NULL hasta que sean actualizadas manualmente.'
