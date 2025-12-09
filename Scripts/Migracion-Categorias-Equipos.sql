-- ===============================
-- MIGRACIÓN: CATEGORÍAS DE EQUIPO
-- ===============================
-- Fecha: 2025-12-03
-- Descripción: Crea la tabla Categoria_Equipo y relaciona Equipos con categorías

PRINT 'Creando tabla Categoria_Equipo...'

IF OBJECT_ID('Categoria_Equipo', 'U') IS NULL
BEGIN
    CREATE TABLE Categoria_Equipo (
        id_categoria INT IDENTITY(1,1) PRIMARY KEY,
        nombre NVARCHAR(120) NOT NULL,
        descripcion NVARCHAR(255) NULL,
        id_padre INT NULL,
        estado BIT NOT NULL CONSTRAINT DF_CategoriaEquipo_Estado DEFAULT(1),
        fecha_creacion DATETIME NOT NULL CONSTRAINT DF_CategoriaEquipo_FCreacion DEFAULT(GETDATE()),
        fecha_modificacion DATETIME NULL,
        usuario_creacion INT NULL
    );

    ALTER TABLE Categoria_Equipo
    ADD CONSTRAINT FK_CategoriaEquipo_Padre
    FOREIGN KEY (id_padre) REFERENCES Categoria_Equipo(id_categoria);

    CREATE UNIQUE INDEX UX_CategoriaEquipo_Nombre
    ON Categoria_Equipo(nombre);

    CREATE INDEX IX_CategoriaEquipo_Padre
    ON Categoria_Equipo(id_padre);
END
ELSE
BEGIN
    PRINT 'Tabla Categoria_Equipo ya existe. Saltando creación.';
END

PRINT 'Agregando columna id_categoria a Equipos...'

IF COL_LENGTH('Equipos', 'id_categoria') IS NULL
BEGIN
    ALTER TABLE Equipos ADD id_categoria INT NULL;
    ALTER TABLE Equipos ADD CONSTRAINT FK_Equipos_Categoria
        FOREIGN KEY (id_categoria) REFERENCES Categoria_Equipo(id_categoria);
    CREATE INDEX IX_Equipos_Categoria ON Equipos(id_categoria);
END
ELSE
BEGIN
    PRINT 'La columna id_categoria ya existe en Equipos.';
END

PRINT 'Migración de categorías completada.'
