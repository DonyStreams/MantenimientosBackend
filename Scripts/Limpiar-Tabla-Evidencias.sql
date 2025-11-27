-- Script para limpiar la tabla Evidencias y empezar de cero
-- Ejecutar con precaución

PRINT '== Limpiando tabla Evidencias =='

BEGIN TRY
    BEGIN TRANSACTION;

    -- Eliminar todos los registros de evidencias
    DELETE FROM dbo.Evidencias;
    PRINT '   ✓ Registros eliminados';

    -- Reiniciar el identity (auto-increment)
    DBCC CHECKIDENT ('dbo.Evidencias', RESEED, 0);
    PRINT '   ✓ Identity reiniciado a 0';

    COMMIT TRANSACTION;
    PRINT '== Tabla Evidencias limpiada correctamente =='
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    PRINT '!! Error al limpiar la tabla';
    THROW;
END CATCH;

-- Verificar que está vacía
SELECT COUNT(*) AS 'Registros restantes' FROM dbo.Evidencias;
