-- =============================================
-- MIGRACIÓN: Configuración del Scheduler y Alertas
-- =============================================
-- Este script configura el scheduler de notificaciones
-- y las alertas que REALMENTE usa el sistema
-- Fecha: 2026-01-30
-- =============================================

PRINT 'Configurando el sistema de notificaciones...'
PRINT ''

-- =============================================
-- 1. CONFIGURACIÓN DEL SCHEDULER
-- =============================================
IF NOT EXISTS (SELECT 1 FROM Configuracion_Alertas WHERE tipo_alerta = 'scheduler_config')
BEGIN
    INSERT INTO Configuracion_Alertas (nombre, descripcion, tipo_alerta, dias_anticipacion, activa, usuarios_notificar, fecha_creacion)
    VALUES (
        'Configuración del Scheduler',
        'Hora de ejecución del scheduler. Formato: hora*100+minuto (ej: 800=8:00 AM, 1430=2:30 PM)',
        'scheduler_config',
        800,  -- 8:00 AM por defecto
        1,    -- Habilitado
        NULL,
        GETDATE()
    );
    PRINT '✓ Configuración del scheduler creada (8:00 AM, habilitado)'
END
ELSE
BEGIN
    PRINT '✓ Configuración del scheduler ya existe'
END

-- =============================================
-- 2. ALERTAS DE MANTENIMIENTOS (USADAS)
-- =============================================
IF NOT EXISTS (SELECT 1 FROM Configuracion_Alertas WHERE tipo_alerta = 'mantenimiento_proximo')
BEGIN
    INSERT INTO Configuracion_Alertas (nombre, descripcion, tipo_alerta, dias_anticipacion, activa, fecha_creacion)
    VALUES ('Mantenimientos Próximos', 'Alerta cuando un mantenimiento está próximo a su fecha programada', 'mantenimiento_proximo', 7, 1, GETDATE());
    PRINT '✓ Configuración de mantenimientos próximos creada (7 días)'
END

-- =============================================
-- 3. ALERTAS DE CONTRATOS (USADAS - escalonadas)
-- =============================================
IF NOT EXISTS (SELECT 1 FROM Configuracion_Alertas WHERE tipo_alerta = 'contrato_proximo_30')
BEGIN
    INSERT INTO Configuracion_Alertas (nombre, descripcion, tipo_alerta, dias_anticipacion, activa, fecha_creacion)
    VALUES ('Contratos por Vencer (30 días)', 'Primera alerta de contrato próximo a vencer', 'contrato_proximo_30', 30, 1, GETDATE());
    PRINT '✓ Configuración de contratos 30 días creada'
END

IF NOT EXISTS (SELECT 1 FROM Configuracion_Alertas WHERE tipo_alerta = 'contrato_proximo_15')
BEGIN
    INSERT INTO Configuracion_Alertas (nombre, descripcion, tipo_alerta, dias_anticipacion, activa, fecha_creacion)
    VALUES ('Contratos por Vencer (15 días)', 'Segunda alerta de contrato próximo a vencer', 'contrato_proximo_15', 15, 1, GETDATE());
    PRINT '✓ Configuración de contratos 15 días creada'
END

IF NOT EXISTS (SELECT 1 FROM Configuracion_Alertas WHERE tipo_alerta = 'contrato_proximo_7')
BEGIN
    INSERT INTO Configuracion_Alertas (nombre, descripcion, tipo_alerta, dias_anticipacion, activa, fecha_creacion)
    VALUES ('Contratos por Vencer (7 días)', 'Alerta urgente de contrato próximo a vencer', 'contrato_proximo_7', 7, 1, GETDATE());
    PRINT '✓ Configuración de contratos 7 días creada'
END

-- =============================================
-- 4. ELIMINAR CONFIGURACIONES REDUNDANTES/NO USADAS
-- =============================================
PRINT ''
PRINT 'Eliminando configuraciones redundantes...'

-- Eliminar "Vencimiento de Contrato" (duplica contrato_proximo_30)
IF EXISTS (SELECT 1 FROM Configuracion_Alertas WHERE tipo_alerta = 'vencimiento_contrato')
BEGIN
    DELETE FROM Configuracion_Alertas WHERE tipo_alerta = 'vencimiento_contrato';
    PRINT '✓ Eliminada: Vencimiento de Contrato (redundante con contrato_proximo_30)'
END

-- Eliminar "Mantenimiento Atrasado" (no implementado en el servicio actual)
IF EXISTS (SELECT 1 FROM Configuracion_Alertas WHERE tipo_alerta = 'mantenimiento_atrasado')
BEGIN
    DELETE FROM Configuracion_Alertas WHERE tipo_alerta = 'mantenimiento_atrasado';
    PRINT '✓ Eliminada: Mantenimiento Atrasado (no implementado)'
END

-- Eliminar "Equipo Sin Mantenimiento" (no implementado en el servicio actual)
IF EXISTS (SELECT 1 FROM Configuracion_Alertas WHERE tipo_alerta = 'equipo_sin_mantenimiento')
BEGIN
    DELETE FROM Configuracion_Alertas WHERE tipo_alerta = 'equipo_sin_mantenimiento';
    PRINT '✓ Eliminada: Equipo Sin Mantenimiento (no implementado)'
END

PRINT ''
PRINT '============================================='
PRINT '✅ MIGRACIÓN COMPLETADA'
PRINT '============================================='
PRINT ''
PRINT 'Configuraciones activas del sistema:'
PRINT ''

SELECT 
    tipo_alerta AS [Tipo],
    nombre AS [Nombre],
    CASE tipo_alerta 
        WHEN 'scheduler_config' THEN 
            CAST(dias_anticipacion / 100 AS VARCHAR) + ':' + 
            RIGHT('0' + CAST(dias_anticipacion % 100 AS VARCHAR), 2) + ' hrs'
        ELSE 
            CAST(dias_anticipacion AS VARCHAR) + ' días'
    END AS [Valor],
    CASE activa WHEN 1 THEN 'Activo' ELSE 'Inactivo' END AS [Estado]
FROM Configuracion_Alertas 
WHERE tipo_alerta IN ('scheduler_config', 'mantenimiento_proximo', 'contrato_proximo_30', 'contrato_proximo_15', 'contrato_proximo_7')
ORDER BY 
    CASE tipo_alerta 
        WHEN 'scheduler_config' THEN 0
        WHEN 'mantenimiento_proximo' THEN 1
        WHEN 'contrato_proximo_30' THEN 2
        WHEN 'contrato_proximo_15' THEN 3
        WHEN 'contrato_proximo_7' THEN 4
    END;
