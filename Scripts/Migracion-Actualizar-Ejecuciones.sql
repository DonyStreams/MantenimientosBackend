-- Migración: Actualizar tabla de ejecuciones de mantenimiento
-- Objetivo: agregar estado, fechas de trabajo y relación con programaciones

PRINT '== Migración: Actualizando Ejecuciones_Mantenimiento =='

BEGIN TRY
    BEGIN TRANSACTION;

    IF COL_LENGTH('dbo.Ejecuciones_Mantenimiento', 'estado') IS NULL
    BEGIN
        ALTER TABLE dbo.Ejecuciones_Mantenimiento
        ADD estado VARCHAR(20) NULL;
        PRINT '   ✓ Columna estado agregada (temporalmente NULL)';

        -- Asignar valor por defecto a registros existentes
        EXEC sp_executesql N'
            UPDATE dbo.Ejecuciones_Mantenimiento
            SET estado = ''PROGRAMADO''
            WHERE estado IS NULL;
        ';

        IF NOT EXISTS (
                        SELECT 1
                        FROM sys.default_constraints dc
                        JOIN sys.columns c ON c.object_id = dc.parent_object_id AND c.column_id = dc.parent_column_id
                        WHERE dc.parent_object_id = OBJECT_ID('dbo.Ejecuciones_Mantenimiento')
                            AND c.name = 'estado')
        BEGIN
            EXEC sp_executesql N'
                ALTER TABLE dbo.Ejecuciones_Mantenimiento
                ADD CONSTRAINT DF_Ejecuciones_Estado DEFAULT ''PROGRAMADO'' FOR estado;
            ';
            PRINT '   ✓ Default constraint DF_Ejecuciones_Estado creada';
        END

        EXEC sp_executesql N'
            ALTER TABLE dbo.Ejecuciones_Mantenimiento
            ALTER COLUMN estado VARCHAR(20) NOT NULL;
        ';
        PRINT '   ✓ Columna estado actualizada a NOT NULL';
    END

    IF COL_LENGTH('dbo.Ejecuciones_Mantenimiento', 'fecha_inicio_trabajo') IS NULL
    BEGIN
        ALTER TABLE dbo.Ejecuciones_Mantenimiento
        ADD fecha_inicio_trabajo DATETIME NULL;
        PRINT '   ✓ Columna fecha_inicio_trabajo agregada';
    END

    IF COL_LENGTH('dbo.Ejecuciones_Mantenimiento', 'fecha_cierre') IS NULL
    BEGIN
        ALTER TABLE dbo.Ejecuciones_Mantenimiento
        ADD fecha_cierre DATETIME NULL;
        PRINT '   ✓ Columna fecha_cierre agregada';
    END

    IF COL_LENGTH('dbo.Ejecuciones_Mantenimiento', 'id_programacion') IS NULL
    BEGIN
        ALTER TABLE dbo.Ejecuciones_Mantenimiento
        ADD id_programacion INT NULL;
        PRINT '   ✓ Columna id_programacion agregada';

        ALTER TABLE dbo.Ejecuciones_Mantenimiento
        ADD CONSTRAINT FK_Ejecuciones_Programacion
        FOREIGN KEY (id_programacion)
        REFERENCES dbo.Programaciones_Mantenimiento(id_programacion);
        PRINT '   ✓ Llave foránea a Programaciones_Mantenimiento creada';
    END

    COMMIT TRANSACTION;
    PRINT '== Migración completada correctamente ==';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    PRINT '!! Error en la migración, se realizó rollback';
    THROW;
END CATCH;
