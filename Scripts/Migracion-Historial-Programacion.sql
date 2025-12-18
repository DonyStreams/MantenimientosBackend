-- ===============================
-- MIGRACIÓN: HISTORIAL DE PROGRAMACIONES
-- ===============================
-- Descripción: Agrega tabla para registrar eventos de programaciones
--              (EJECUTADO, SALTADO, REPROGRAMADO) y campos adicionales
-- Fecha: 2025-12-16
-- Versión: 1.0

PRINT '=============================================='
PRINT 'MIGRACIÓN: HISTORIAL DE PROGRAMACIONES'
PRINT 'Fecha: ' + CONVERT(VARCHAR, GETDATE(), 120)
PRINT '=============================================='

-- ===============================
-- FASE 1: CREAR TABLA HISTORIAL_PROGRAMACION
-- ===============================
PRINT 'FASE 1: Creando tabla Historial_Programacion...'

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Historial_Programacion')
BEGIN
    CREATE TABLE Historial_Programacion (
        id_historial INT IDENTITY(1,1) PRIMARY KEY,
        id_programacion INT NOT NULL,
        tipo_evento VARCHAR(20) NOT NULL, -- 'EJECUTADO', 'SALTADO', 'REPROGRAMADO'
        fecha_original DATE NOT NULL,     -- Fecha que estaba programada
        fecha_nueva DATE NULL,            -- Nueva fecha (si aplica en reprogramación)
        motivo NVARCHAR(500) NULL,        -- Justificación del usuario
        id_ejecucion INT NULL,            -- Referencia a ejecución (si tipo=EJECUTADO)
        usuario_id INT NULL,
        fecha_registro DATETIME DEFAULT GETDATE(),
        
        CONSTRAINT FK_HistorialProg_Programacion 
            FOREIGN KEY (id_programacion) REFERENCES Programaciones_Mantenimiento(id_programacion),
        CONSTRAINT FK_HistorialProg_Usuario 
            FOREIGN KEY (usuario_id) REFERENCES Usuarios(id),
        CONSTRAINT FK_HistorialProg_Ejecucion 
            FOREIGN KEY (id_ejecucion) REFERENCES Ejecuciones_Mantenimiento(id_ejecucion),
        CONSTRAINT CK_HistorialProg_TipoEvento 
            CHECK (tipo_evento IN ('EJECUTADO', 'SALTADO', 'REPROGRAMADO'))
    );
    
    -- Índices para rendimiento
    CREATE INDEX IX_HistorialProg_Programacion ON Historial_Programacion (id_programacion);
    CREATE INDEX IX_HistorialProg_TipoEvento ON Historial_Programacion (tipo_evento);
    CREATE INDEX IX_HistorialProg_FechaRegistro ON Historial_Programacion (fecha_registro);
    
    PRINT '   ✓ Tabla Historial_Programacion creada'
END
ELSE
BEGIN
    PRINT '   ⚠ Tabla Historial_Programacion ya existe'
END

-- ===============================
-- FASE 2: AGREGAR COLUMNAS A EJECUCIONES_MANTENIMIENTO
-- ===============================
PRINT 'FASE 2: Agregando columnas a Ejecuciones_Mantenimiento...'

-- Columna: id_tipo_mantenimiento (para saber si fue preventivo/correctivo/calibración)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Ejecuciones_Mantenimiento') AND name = 'id_tipo_mantenimiento')
BEGIN
    ALTER TABLE Ejecuciones_Mantenimiento ADD id_tipo_mantenimiento INT NULL;
    
    ALTER TABLE Ejecuciones_Mantenimiento ADD CONSTRAINT FK_Ejecucion_TipoMantenimiento
        FOREIGN KEY (id_tipo_mantenimiento) REFERENCES Tipos_Mantenimiento(id_tipo);
    
    PRINT '   ✓ Columna id_tipo_mantenimiento agregada'
END
ELSE
BEGIN
    PRINT '   ⚠ Columna id_tipo_mantenimiento ya existe'
END

-- Columna: fecha_programada (fecha esperada, distinta a fecha_ejecucion real)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Ejecuciones_Mantenimiento') AND name = 'fecha_programada')
BEGIN
    ALTER TABLE Ejecuciones_Mantenimiento ADD fecha_programada DATE NULL;
    PRINT '   ✓ Columna fecha_programada agregada'
END
ELSE
BEGIN
    PRINT '   ⚠ Columna fecha_programada ya existe'
END

-- ===============================
-- FASE 3: VISTA PARA MÉTRICAS DE CUMPLIMIENTO
-- ===============================
PRINT 'FASE 3: Creando vista de métricas...'

IF EXISTS (SELECT * FROM sys.views WHERE name = 'VW_MetricasCumplimiento')
    DROP VIEW VW_MetricasCumplimiento;

EXEC('
CREATE VIEW VW_MetricasCumplimiento AS
SELECT 
    YEAR(fecha_registro) AS anio,
    MONTH(fecha_registro) AS mes,
    COUNT(*) AS total_eventos,
    SUM(CASE WHEN tipo_evento = ''EJECUTADO'' THEN 1 ELSE 0 END) AS ejecutados,
    SUM(CASE WHEN tipo_evento = ''SALTADO'' THEN 1 ELSE 0 END) AS saltados,
    SUM(CASE WHEN tipo_evento = ''REPROGRAMADO'' THEN 1 ELSE 0 END) AS reprogramados,
    CAST(
        CASE 
            WHEN COUNT(*) > 0 
            THEN (SUM(CASE WHEN tipo_evento = ''EJECUTADO'' THEN 1 ELSE 0 END) * 100.0) / COUNT(*)
            ELSE 0 
        END 
    AS DECIMAL(5,2)) AS porcentaje_cumplimiento
FROM Historial_Programacion
GROUP BY YEAR(fecha_registro), MONTH(fecha_registro)
');

PRINT '   ✓ Vista VW_MetricasCumplimiento creada'

-- ===============================
-- FASE 4: PROCEDIMIENTO PARA DESCARTAR (SALTAR)
-- ===============================
PRINT 'FASE 4: Creando procedimiento SP_DescartarProgramacion...'

IF EXISTS (SELECT * FROM sys.procedures WHERE name = 'SP_DescartarProgramacion')
    DROP PROCEDURE SP_DescartarProgramacion;

EXEC('
CREATE PROCEDURE SP_DescartarProgramacion
    @id_programacion INT,
    @motivo NVARCHAR(500) = NULL,
    @usuario_id INT = NULL
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @fecha_original DATE;
    DECLARE @frecuencia_dias INT;
    DECLARE @nueva_fecha DATE;
    
    -- Obtener datos actuales
    SELECT 
        @fecha_original = fecha_proximo_mantenimiento,
        @frecuencia_dias = frecuencia_dias
    FROM Programaciones_Mantenimiento
    WHERE id_programacion = @id_programacion;
    
    IF @fecha_original IS NULL
    BEGIN
        RAISERROR(''Programación no encontrada'', 16, 1);
        RETURN;
    END
    
    -- Calcular nueva fecha (avanzar según frecuencia)
    SET @nueva_fecha = DATEADD(DAY, @frecuencia_dias, @fecha_original);
    
    -- Insertar en historial
    INSERT INTO Historial_Programacion (
        id_programacion, 
        tipo_evento, 
        fecha_original, 
        fecha_nueva, 
        motivo, 
        usuario_id
    )
    VALUES (
        @id_programacion, 
        ''SALTADO'', 
        @fecha_original, 
        @nueva_fecha, 
        ISNULL(@motivo, ''Descartado por usuario''),
        @usuario_id
    );
    
    -- Actualizar programación
    UPDATE Programaciones_Mantenimiento
    SET 
        fecha_proximo_mantenimiento = @nueva_fecha,
        fecha_modificacion = GETDATE(),
        usuario_modificacion = @usuario_id
    WHERE id_programacion = @id_programacion;
    
    -- Retornar nueva fecha
    SELECT @nueva_fecha AS nueva_fecha_proximo;
END
');

PRINT '   ✓ Procedimiento SP_DescartarProgramacion creado'

-- ===============================
-- FASE 5: PROCEDIMIENTO PARA REGISTRAR EJECUCIÓN COMPLETADA
-- ===============================
PRINT 'FASE 5: Creando procedimiento SP_CompletarEjecucion...'

IF EXISTS (SELECT * FROM sys.procedures WHERE name = 'SP_CompletarEjecucion')
    DROP PROCEDURE SP_CompletarEjecucion;

EXEC('
CREATE PROCEDURE SP_CompletarEjecucion
    @id_ejecucion INT,
    @usuario_id INT = NULL
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @id_programacion INT;
    DECLARE @fecha_programada DATE;
    DECLARE @frecuencia_dias INT;
    DECLARE @fecha_ejecucion_real DATE = GETDATE();
    
    -- Obtener datos de la ejecución
    SELECT 
        @id_programacion = e.id_programacion,
        @fecha_programada = ISNULL(e.fecha_programada, CAST(e.fecha_ejecucion AS DATE))
    FROM Ejecuciones_Mantenimiento e
    WHERE e.id_ejecucion = @id_ejecucion;
    
    IF @id_programacion IS NULL
    BEGIN
        -- Si no tiene programación asociada, solo actualizar estado
        UPDATE Ejecuciones_Mantenimiento
        SET estado = ''COMPLETADO'',
            fecha_cierre = GETDATE(),
            fecha_modificacion = GETDATE(),
            usuario_modificacion = @usuario_id
        WHERE id_ejecucion = @id_ejecucion;
        RETURN;
    END
    
    -- Obtener frecuencia de la programación
    SELECT @frecuencia_dias = frecuencia_dias
    FROM Programaciones_Mantenimiento
    WHERE id_programacion = @id_programacion;
    
    -- Actualizar ejecución
    UPDATE Ejecuciones_Mantenimiento
    SET estado = ''COMPLETADO'',
        fecha_cierre = GETDATE(),
        fecha_modificacion = GETDATE(),
        usuario_modificacion = @usuario_id
    WHERE id_ejecucion = @id_ejecucion;
    
    -- Insertar en historial
    INSERT INTO Historial_Programacion (
        id_programacion, 
        tipo_evento, 
        fecha_original, 
        id_ejecucion,
        usuario_id
    )
    VALUES (
        @id_programacion, 
        ''EJECUTADO'', 
        @fecha_programada,
        @id_ejecucion,
        @usuario_id
    );
    
    -- Actualizar programación: mover fechas
    UPDATE Programaciones_Mantenimiento
    SET 
        fecha_ultimo_mantenimiento = @fecha_ejecucion_real,
        fecha_proximo_mantenimiento = DATEADD(DAY, @frecuencia_dias, @fecha_ejecucion_real),
        fecha_modificacion = GETDATE(),
        usuario_modificacion = @usuario_id
    WHERE id_programacion = @id_programacion;
END
');

PRINT '   ✓ Procedimiento SP_CompletarEjecucion creado'

-- ===============================
-- VERIFICACIÓN FINAL
-- ===============================
PRINT ''
PRINT '=============================================='
PRINT 'VERIFICACIÓN DE MIGRACIÓN'
PRINT '=============================================='

SELECT 'Historial_Programacion' AS Tabla, COUNT(*) AS Columnas 
FROM sys.columns WHERE object_id = OBJECT_ID('Historial_Programacion')
UNION ALL
SELECT 'Ejecuciones_Mantenimiento (nuevas)', 
    (SELECT COUNT(*) FROM sys.columns 
     WHERE object_id = OBJECT_ID('Ejecuciones_Mantenimiento') 
     AND name IN ('id_tipo_mantenimiento', 'fecha_programada'));

PRINT ''
PRINT '✅ MIGRACIÓN COMPLETADA EXITOSAMENTE'
PRINT '=============================================='
