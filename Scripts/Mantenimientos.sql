-- ===============================
-- MODELO DE BASE DE DATOS MEJORADO - INACIF
-- ===============================

-- TABLA USUARIOS (Integración con Keycloak)
CREATE TABLE Usuarios (
    id INT PRIMARY KEY IDENTITY(1,1),
    keycloak_id UNIQUEIDENTIFIER NOT NULL UNIQUE,
    nombre_completo VARCHAR(100),
    correo VARCHAR(100),
    activo BIT DEFAULT 1
);

-- CATÁLOGO DE ÁREAS
CREATE TABLE Areas (
    id_area INT PRIMARY KEY IDENTITY(1,1),
    codigo_area VARCHAR(20),
    nombre VARCHAR(100),
    tipo_area VARCHAR(50), -- Técnico Científico / Administrativo Financiero
    estado BIT,
    fecha_creacion DATETIME DEFAULT GETDATE(),
    usuario_creacion INT,
    fecha_modificacion DATETIME,
    usuario_modificacion INT,
    FOREIGN KEY (usuario_creacion) REFERENCES Usuarios(id),
    FOREIGN KEY (usuario_modificacion) REFERENCES Usuarios(id)
);

-- CATÁLOGO DE EQUIPOS
CREATE TABLE Equipos (
    id_equipo INT PRIMARY KEY IDENTITY(1,1),
    nombre VARCHAR(100),
    codigo_inacif VARCHAR(50),
    marca VARCHAR(50),
    modelo VARCHAR(50),
    numero_inventario VARCHAR(50) UNIQUE,
    numero_serie VARCHAR(50),
    ubicacion VARCHAR(100),
    magnitud_medicion VARCHAR(100),
    rango_capacidad VARCHAR(100),
    manual_fabricante VARCHAR(100),
    fotografia VARCHAR(255),
    software_firmware VARCHAR(100),
    condiciones_operacion VARCHAR(255),
    descripcion TEXT,
    estado BIT,
    id_area INT,
    fecha_creacion DATETIME DEFAULT GETDATE(),
    usuario_creacion INT,
    fecha_modificacion DATETIME,
    usuario_modificacion INT,
    FOREIGN KEY (id_area) REFERENCES Areas(id_area),
    FOREIGN KEY (usuario_creacion) REFERENCES Usuarios(id),
    FOREIGN KEY (usuario_modificacion) REFERENCES Usuarios(id)
);

-- HISTORIAL DE EQUIPOS
CREATE TABLE Historial_Equipo (
    id_historial INT PRIMARY KEY IDENTITY(1,1),
    id_equipo INT,
    fecha_registro DATETIME DEFAULT GETDATE(),
    descripcion TEXT,
    FOREIGN KEY (id_equipo) REFERENCES Equipos(id_equipo)
);

-- TIPOS DE MANTENIMIENTO
CREATE TABLE Tipos_Mantenimiento (
    id_tipo INT PRIMARY KEY IDENTITY(1,1),
    codigo VARCHAR(20),
    nombre VARCHAR(50),
    estado BIT,
    fecha_creacion DATETIME DEFAULT GETDATE(),
    usuario_creacion INT,
    fecha_modificacion DATETIME,
    usuario_modificacion INT,
    FOREIGN KEY (usuario_creacion) REFERENCES Usuarios(id),
    FOREIGN KEY (usuario_modificacion) REFERENCES Usuarios(id)
);

-- PROVEEDORES DE SERVICIO
CREATE TABLE Proveedores (
    id_proveedor INT PRIMARY KEY IDENTITY(1,1),
    nit VARCHAR(20) UNIQUE,
    nombre VARCHAR(100),
    estado BIT,
    fecha_creacion DATETIME DEFAULT GETDATE(),
    usuario_creacion INT,
    fecha_modificacion DATETIME,
    usuario_modificacion INT,
    FOREIGN KEY (usuario_creacion) REFERENCES Usuarios(id),
    FOREIGN KEY (usuario_modificacion) REFERENCES Usuarios(id)
);

-- CONTRATOS DE MANTENIMIENTO
CREATE TABLE Contratos (
    id_contrato INT PRIMARY KEY IDENTITY(1,1),
    fecha_inicio DATE,
    fecha_fin DATE,
    descripcion TEXT,
    frecuencia VARCHAR(20), -- mensual, anual, semestral, a demanda
    estado BIT,
    id_proveedor INT,
    fecha_creacion DATETIME DEFAULT GETDATE(),
    usuario_creacion INT,
    fecha_modificacion DATETIME,
    usuario_modificacion INT,
    FOREIGN KEY (id_proveedor) REFERENCES Proveedores(id_proveedor),
    FOREIGN KEY (usuario_creacion) REFERENCES Usuarios(id),
    FOREIGN KEY (usuario_modificacion) REFERENCES Usuarios(id)
);

-- RELACIÓN CONTRATO - EQUIPO (Muchos a Muchos)
CREATE TABLE Contrato_Equipo (
    id_contrato INT,
    id_equipo INT,
    PRIMARY KEY (id_contrato, id_equipo),
    FOREIGN KEY (id_contrato) REFERENCES Contratos(id_contrato),
    FOREIGN KEY (id_equipo) REFERENCES Equipos(id_equipo)
);

-- RELACIÓN CONTRATO - TIPO DE MANTENIMIENTO (Muchos a Muchos)
CREATE TABLE Contrato_Tipo_Mantenimiento (
    id_contrato INT,
    id_tipo INT,
    PRIMARY KEY (id_contrato, id_tipo),
    FOREIGN KEY (id_contrato) REFERENCES Contratos(id_contrato),
    FOREIGN KEY (id_tipo) REFERENCES Tipos_Mantenimiento(id_tipo)
);

-- EJECUCIÓN DE MANTENIMIENTO
CREATE TABLE Ejecuciones_Mantenimiento (
    id_ejecucion INT PRIMARY KEY IDENTITY(1,1),
    id_contrato INT,
    id_equipo INT,
    fecha_ejecucion DATETIME DEFAULT GETDATE(),
    bitacora TEXT,
    usuario_responsable INT,
    fecha_creacion DATETIME DEFAULT GETDATE(),
    fecha_modificacion DATETIME,
    usuario_creacion INT,
    usuario_modificacion INT,
    FOREIGN KEY (id_contrato) REFERENCES Contratos(id_contrato),
    FOREIGN KEY (id_equipo) REFERENCES Equipos(id_equipo),
    FOREIGN KEY (usuario_responsable) REFERENCES Usuarios(id),
    FOREIGN KEY (usuario_creacion) REFERENCES Usuarios(id),
    FOREIGN KEY (usuario_modificacion) REFERENCES Usuarios(id)
);

-- TICKETS
CREATE TABLE Tickets (
    id INT PRIMARY KEY IDENTITY(1,1),
    equipo_id INT NOT NULL,
    usuario_creador_id INT NOT NULL,
    usuario_asignado_id INT NULL,
    descripcion NVARCHAR(MAX),
    prioridad VARCHAR(20) CHECK (prioridad IN ('Baja','Media','Alta','Crítica')),
    estado VARCHAR(20) CHECK (estado IN ('Abierto', 'Asignado', 'En Proceso', 'Resuelto', 'Cerrado')),
    fecha_creacion DATETIME DEFAULT GETDATE(),
    fecha_modificacion DATETIME,
    fecha_cierre DATETIME NULL,
    usuario_creacion INT,
    usuario_modificacion INT,
    FOREIGN KEY (equipo_id) REFERENCES Equipos(id_equipo),
    FOREIGN KEY (usuario_creador_id) REFERENCES Usuarios(id),
    FOREIGN KEY (usuario_asignado_id) REFERENCES Usuarios(id),
    FOREIGN KEY (usuario_creacion) REFERENCES Usuarios(id),
    FOREIGN KEY (usuario_modificacion) REFERENCES Usuarios(id)
);

-- TIPOS DE COMENTARIO PARA TICKETS
CREATE TABLE Tipos_Comentario (
    id_tipo INT PRIMARY KEY IDENTITY(1,1),
    nombre VARCHAR(50) UNIQUE -- Ej: 'técnico', 'seguimiento', 'alerta'
);

-- COMENTARIOS DE TICKETS
CREATE TABLE Comentarios_Ticket (
    id INT PRIMARY KEY IDENTITY(1,1),
    ticket_id INT NOT NULL,
    usuario_id INT NOT NULL,
    tipo_comentario_id INT NOT NULL,
    comentario NVARCHAR(MAX),
    fecha_creacion DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (ticket_id) REFERENCES Tickets(id),
    FOREIGN KEY (usuario_id) REFERENCES Usuarios(id),
    FOREIGN KEY (tipo_comentario_id) REFERENCES Tipos_Comentario(id_tipo)
);

-- EVIDENCIAS (para mantenimiento y tickets)
CREATE TABLE Evidencias (
    id INT PRIMARY KEY IDENTITY(1,1),
    entidad_relacionada VARCHAR(50) NOT NULL, -- 'ticket', 'ejecucion_mantenimiento'
    entidad_id INT NOT NULL,
    archivo_url NVARCHAR(500) NOT NULL,
    descripcion TEXT,
    fecha_creacion DATETIME DEFAULT GETDATE()
);


/*Las tablas de auditoría (usuario_creacion, usuario_modificacion, fecha_creacion, etc.) están en todos los catálogos para trazabilidad.

La tabla Usuarios contiene el campo keycloak_id para integrar con Keycloak.

Se creó la tabla Tipos_Comentario para mantener control sobre los tipos posibles.

La tabla Evidencias es genérica para soportar subir múltiples archivos a tickets o mantenimientos.

Se respetaron las relaciones y llaves foráneas para mantener integridad referencial.*/

DROP TABLE IF EXISTS Evidencias;
DROP TABLE IF EXISTS Comentarios_Ticket;
DROP TABLE IF EXISTS Tipos_Comentario;
DROP TABLE IF EXISTS Tickets;
DROP TABLE IF EXISTS Ejecuciones_Mantenimiento;
DROP TABLE IF EXISTS Contrato_Tipo_Mantenimiento;
DROP TABLE IF EXISTS Contrato_Equipo;
DROP TABLE IF EXISTS Contratos;
DROP TABLE IF EXISTS Proveedores;
DROP TABLE IF EXISTS Tipos_Mantenimiento;
DROP TABLE IF EXISTS Historial_Equipo;
DROP TABLE IF EXISTS Equipos;
DROP TABLE IF EXISTS Areas;
DROP TABLE IF EXISTS Usuarios;

-- Usuarios
INSERT INTO Usuarios (keycloak_id, nombre_completo, correo, activo)
VALUES (NEWID(), 'Admin Mantenimientos', 'admin@inacif.gob.gt', 1);

-- Áreas
INSERT INTO Areas (codigo_area, nombre, tipo_area, estado, fecha_creacion, usuario_creacion)
VALUES ('LAB01', 'Laboratorio Criminalística', 'Técnico Científico', 1, GETDATE(), 1);

-- Equipos
INSERT INTO Equipos (nombre, codigo_inacif, marca, modelo, numero_inventario, numero_serie, ubicacion, magnitud_medicion, rango_capacidad, manual_fabricante, fotografia, software_firmware, condiciones_operacion, descripcion, estado, id_area, fecha_creacion, usuario_creacion)
VALUES ('Microscopio óptico', 'INACIF-001', 'MarcaX', 'ModeloY', 'EQ-0001', 'SN-12345', 'Laboratorio 1', '0.01 µm', '50-1000x', 'Manual de usuario', 'foto_microscopio.jpg', 'Firmware 1.0', '20-25°C, 30-70% HR', 'Microscopio para análisis forense', 1, 1, GETDATE(), 1);

-- Ejemplo de inserción de un equipo forense con todos los campos nuevos
INSERT INTO Equipos (
    nombre,
    codigo_inacif,
    marca,
    modelo,
    numero_inventario,
    numero_serie,
    ubicacion,
    magnitud_medicion,
    rango_capacidad,
    manual_fabricante,
    fotografia,
    software_firmware,
    condiciones_operacion,
    descripcion,
    estado,
    id_area,
    fecha_creacion,
    usuario_creacion
) VALUES (
    'Microscopio Forense',
    'INACIF-002',
    'Nikon',
    'E200',
    'INV-2025-002',
    'SN987654321',
    'Laboratorio Central',
    'Aumento óptico',
    '40x–1000x',
    'MAN-002 Microscopio E200',
    'ruta/imagen/microscopio2.jpg',
    'FW v2.1.0',
    'Temperatura 20-25°C, Humedad <60%',
    'Microscopio para análisis de muestras biológicas en criminalística',
    1,
    1, -- id_area
    GETDATE(),
    1  -- usuario_creacion
);

-- Historial de Equipo
INSERT INTO Historial_Equipo (id_equipo, fecha_registro, descripcion)
VALUES (1, GETDATE(), 'Equipo recibido en laboratorio');

-- Tipos de Mantenimiento
INSERT INTO Tipos_Mantenimiento (codigo, nombre, estado, fecha_creacion, usuario_creacion)
VALUES ('PREV', 'Preventivo', 1, GETDATE(), 1);

-- Proveedores
INSERT INTO Proveedores (nit, nombre, estado, fecha_creacion, usuario_creacion)
VALUES ('1234567-8', 'Proveedor S.A.', 1, GETDATE(), 1);

-- Contratos
INSERT INTO Contratos (fecha_inicio, fecha_fin, descripcion, frecuencia, estado, id_proveedor, fecha_creacion, usuario_creacion)
VALUES ('2025-01-01', '2025-12-31', 'Contrato anual de mantenimiento', 'anual', 1, 1, GETDATE(), 1);

-- Contrato_Equipo
INSERT INTO Contrato_Equipo (id_contrato, id_equipo)
VALUES (1, 1);

-- Contrato_Tipo_Mantenimiento
INSERT INTO Contrato_Tipo_Mantenimiento (id_contrato, id_tipo)
VALUES (1, 1);

-- Ejecuciones de Mantenimiento
INSERT INTO Ejecuciones_Mantenimiento (id_contrato, id_equipo, fecha_ejecucion, bitacora, usuario_responsable, fecha_creacion, usuario_creacion)
VALUES (1, 1, GETDATE(), 'Mantenimiento preventivo realizado', 1, GETDATE(), 1);

-- Tickets
INSERT INTO Tickets (equipo_id, usuario_creador_id, descripcion, prioridad, estado, fecha_creacion, usuario_creacion)
VALUES (1, 1, 'El microscopio no enciende', 'Alta', 'Abierto', GETDATE(), 1);

-- Tipos de Comentario
INSERT INTO Tipos_Comentario (nombre)
VALUES ('técnico'), ('seguimiento'), ('alerta');

-- Comentarios de Ticket
INSERT INTO Comentarios_Ticket (ticket_id, usuario_id, tipo_comentario_id, comentario, fecha_creacion)
VALUES (1, 1, 1, 'Se revisó el cableado.', GETDATE());

-- Evidencias
INSERT INTO Evidencias (entidad_relacionada, entidad_id, archivo_url, descripcion, fecha_creacion)
VALUES ('ticket', 1, 'https://servidor/archivos/evidencia1.jpg', 'Foto del equipo', GETDATE());