-- Migración: Actualizar tabla Evidencias con nuevas columnas
-- Objetivo: agregar campos para mejor gestión de archivos

PRINT '== Migración: Actualizando tabla Evidencias =='

BEGIN TRY
    BEGIN TRANSACTION;

    IF COL_LENGTH('dbo.Evidencias', 'nombre_archivo') IS NULL
    BEGIN
        ALTER TABLE dbo.Evidencias ADD nombre_archivo VARCHAR(255) NULL;
        PRINT '   ✓ Columna nombre_archivo agregada';
    END

    IF COL_LENGTH('dbo.Evidencias', 'nombre_original') IS NULL
    BEGIN
        ALTER TABLE dbo.Evidencias ADD nombre_original VARCHAR(255) NULL;
        PRINT '   ✓ Columna nombre_original agregada';
    END

    IF COL_LENGTH('dbo.Evidencias', 'tipo_archivo') IS NULL
    BEGIN
        ALTER TABLE dbo.Evidencias ADD tipo_archivo VARCHAR(100) NULL;
        PRINT '   ✓ Columna tipo_archivo agregada';
    END

    IF COL_LENGTH('dbo.Evidencias', 'tamanio') IS NULL
    BEGIN
        ALTER TABLE dbo.Evidencias ADD tamanio BIGINT NULL;
        PRINT '   ✓ Columna tamanio agregada';
    END

    IF COL_LENGTH('dbo.Evidencias', 'usuario_creacion') IS NULL
    BEGIN
        ALTER TABLE dbo.Evidencias ADD usuario_creacion INT NULL;
        PRINT '   ✓ Columna usuario_creacion agregada';

        ALTER TABLE dbo.Evidencias
        ADD CONSTRAINT FK_Evidencias_Usuario
        FOREIGN KEY (usuario_creacion)
        REFERENCES dbo.Usuarios(id);
        PRINT '   ✓ Llave foránea a Usuarios creada';
    END

    -- Crear índice para búsqueda rápida por entidad
    IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_Evidencias_Entidad')
    BEGIN
        CREATE INDEX IX_Evidencias_Entidad 
        ON dbo.Evidencias (entidad_relacionada, entidad_id);
        PRINT '   ✓ Índice IX_Evidencias_Entidad creado';
    END

    COMMIT TRANSACTION;
    PRINT '== Migración completada correctamente ==';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    PRINT '!! Error en la migración, se realizó rollback';
    THROW;
END CATCH;
