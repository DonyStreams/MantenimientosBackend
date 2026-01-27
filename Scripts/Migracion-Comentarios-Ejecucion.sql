-- =====================================================
-- MIGRACIÓN: Comentarios para Ejecuciones de Mantenimiento
-- Sistema de seguimiento de mantenimientos INACIF
-- =====================================================

-- Crear tabla de Comentarios_Ejecucion
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Comentarios_Ejecucion')
BEGIN
    CREATE TABLE Comentarios_Ejecucion (
        id INT IDENTITY(1,1) PRIMARY KEY,
        id_ejecucion INT NOT NULL,
        usuario_id INT NULL,
        tipo_comentario VARCHAR(50) NOT NULL DEFAULT 'SEGUIMIENTO',
        comentario NVARCHAR(MAX) NOT NULL,
        estado_anterior VARCHAR(50) NULL,
        estado_nuevo VARCHAR(50) NULL,
        fecha_creacion DATETIME NOT NULL DEFAULT GETDATE(),
        
        CONSTRAINT FK_ComentarioEjecucion_Ejecucion 
            FOREIGN KEY (id_ejecucion) REFERENCES Ejecuciones_Mantenimiento(id_ejecucion),
        CONSTRAINT FK_ComentarioEjecucion_Usuario 
            FOREIGN KEY (usuario_id) REFERENCES Usuarios(id)
    );
    
    PRINT 'Tabla Comentarios_Ejecucion creada exitosamente';
END
ELSE
BEGIN
    PRINT 'La tabla Comentarios_Ejecucion ya existe';
END
GO

-- Crear índices para mejorar rendimiento
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_ComentariosEjecucion_Ejecucion')
BEGIN
    CREATE INDEX IX_ComentariosEjecucion_Ejecucion ON Comentarios_Ejecucion(id_ejecucion);
    PRINT 'Índice IX_ComentariosEjecucion_Ejecucion creado';
END
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_ComentariosEjecucion_FechaCreacion')
BEGIN
    CREATE INDEX IX_ComentariosEjecucion_FechaCreacion ON Comentarios_Ejecucion(fecha_creacion DESC);
    PRINT 'Índice IX_ComentariosEjecucion_FechaCreacion creado';
END
GO

-- Verificar la creación
SELECT 
    c.name AS columna,
    t.name AS tipo,
    c.max_length AS longitud,
    c.is_nullable AS permite_null
FROM sys.columns c
INNER JOIN sys.types t ON c.user_type_id = t.user_type_id
WHERE c.object_id = OBJECT_ID('Comentarios_Ejecucion')
ORDER BY c.column_id;
