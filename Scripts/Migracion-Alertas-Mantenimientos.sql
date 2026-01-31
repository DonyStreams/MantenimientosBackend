-- Insertar configuraciones de alertas escalonadas para mantenimientos
-- (similar a las de contratos)

-- Verificar si ya existen
IF NOT EXISTS (SELECT 1 FROM Configuracion_Alertas WHERE tipo_alerta = 'mantenimiento_proximo_30')
BEGIN
    INSERT INTO Configuracion_Alertas (nombre, descripcion, tipo_alerta, dias_anticipacion, activa, fecha_creacion)
    VALUES ('Mantenimiento próximo (30 días)', 'Alerta cuando un mantenimiento programado está a 30 días o menos', 'mantenimiento_proximo_30', 30, 1, GETDATE());
END

IF NOT EXISTS (SELECT 1 FROM Configuracion_Alertas WHERE tipo_alerta = 'mantenimiento_proximo_15')
BEGIN
    INSERT INTO Configuracion_Alertas (nombre, descripcion, tipo_alerta, dias_anticipacion, activa, fecha_creacion)
    VALUES ('Mantenimiento próximo (15 días)', 'Alerta cuando un mantenimiento programado está a 15 días o menos', 'mantenimiento_proximo_15', 15, 1, GETDATE());
END

IF NOT EXISTS (SELECT 1 FROM Configuracion_Alertas WHERE tipo_alerta = 'mantenimiento_proximo_7')
BEGIN
    INSERT INTO Configuracion_Alertas (nombre, descripcion, tipo_alerta, dias_anticipacion, activa, fecha_creacion)
    VALUES ('Mantenimiento próximo (7 días)', 'Alerta urgente cuando un mantenimiento programado está a 7 días o menos', 'mantenimiento_proximo_7', 7, 1, GETDATE());
END

IF NOT EXISTS (SELECT 1 FROM Configuracion_Alertas WHERE tipo_alerta = 'mantenimiento_vencido')
BEGIN
    INSERT INTO Configuracion_Alertas (nombre, descripcion, tipo_alerta, dias_anticipacion, activa, fecha_creacion)
    VALUES ('Mantenimiento vencido', 'Alerta crítica cuando un mantenimiento programado ha vencido', 'mantenimiento_vencido', 0, 1, GETDATE());
END

-- Mostrar todas las configuraciones
SELECT id_configuracion, nombre, tipo_alerta, dias_anticipacion, activa 
FROM Configuracion_Alertas 
ORDER BY tipo_alerta;
