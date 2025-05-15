Use  INACIF;


----------------------------REQUISICIONES-----------------------
CREATE TABLE TipoRequisicion (
    IdTipoRequisicion int IDENTITY(1,1) PRIMARY KEY NOT NULL,
    Nombre varchar(255) UNIQUE NOT NULL ,
	Descripcion varchar(500),
	FechaModificacion DATETIME NOT NULL
);

CREATE TABLE Unidad (
    IdUnidad int IDENTITY(1,1) PRIMARY KEY,		
    Nombre varchar(255) UNIQUE NOT NULL,	
	Descripcion varchar(500),
	FechaModificacion DATETIME NOT NULL
);

--------------------------------------------------------------

CREATE TABLE Departamento (
    IdDepartamento int IDENTITY(1,1) PRIMARY KEY,
    Nombre varchar(255) UNIQUE NOT NULL,
	FechaModificacion DATETIME NOT NULL
);


CREATE TABLE Rol (
    IdRol int IDENTITY(1,1) PRIMARY KEY,
    Nombre varchar(255) UNIQUE NOT NULL,
	Descripcion varchar(500),
	FechaModificacion DATETIME NOT NULL
);


CREATE TABLE Sede (
    IdSede int IDENTITY(1,1) PRIMARY KEY,
	IdDepartamento int,
    Nombre varchar(255),
	Direccion varchar(255),	
	FechaModificacion DATETIME NOT NULL,
	CONSTRAINT fk_IdDepartamento FOREIGN KEY (IdDepartamento) REFERENCES Departamento(IdDepartamento), --ON DELETE CASCADE,
	CONSTRAINT uq_Sede_Nombre_Departamento UNIQUE (Nombre, IdDepartamento)
);


CREATE TABLE Usuario (
    IdUsuario bigint IDENTITY(1,1) PRIMARY KEY,
	IdRol int,	
	IdSede int,
	Usuario varchar(25) UNIQUE NOT NULL,
	Nombre varchar(255) NOT NULL,
	Apellido varchar(255) NOT NULL,
	Direccion varchar(255) NOT NULL,
	Correo VARCHAR(100) UNIQUE,
	Telefono VARCHAR(20), 
	FechaModificacion DATETIME NOT NULL,	
	CONSTRAINT fk_IdRol FOREIGN KEY (IdRol) REFERENCES Rol(IdRol), -- ON DELETE CASCADE,	
	CONSTRAINT fk_IdSede FOREIGN KEY (IdSede) REFERENCES Sede(IdSede) -- ON DELETE CASCADE	
);


CREATE TABLE TipoProducto (
    IdTipoProducto int IDENTITY(1,1) PRIMARY KEY,
	Nombre varchar(255) UNIQUE NOT NULL,
    Descripcion varchar(255),
	FechaModificacion DATETIME NOT NULL		
);


CREATE TABLE UnidadMedidaProducto (
    IdUnidadMedidaProducto int IDENTITY(1,1) PRIMARY KEY,
	Nombre varchar(255) UNIQUE NOT NULL,
    Descripcion varchar(255),
	FechaModificacion DATETIME NOT NULL	
);

CREATE TABLE PresentacionProducto (
    IdPresentacionProducto int IDENTITY(1,1) PRIMARY KEY,
	Nombre varchar(255) UNIQUE NOT NULL,
    Descripcion varchar(255),				
	FechaModificacion DATETIME NOT NULL	
);



CREATE TABLE Producto (
    IdProducto int IDENTITY(1,1) PRIMARY KEY,
	IdTipoProducto int NULL,	
	IdUnidadMedidaProducto int NULL,
	IdPresentacionProducto int NULL,
	Nombre varchar(255) UNIQUE NOT NULL,
	PrecioCompra float,
	PrecioVenta	float,
	PrecioUnitario float,
    Descripcion varchar(255),
	FechaModificacion DATETIME NOT NULL	
	CONSTRAINT fk_IdTipoProducto FOREIGN KEY (IdTipoProducto) REFERENCES TipoProducto(IdTipoProducto),	
	CONSTRAINT fk_IdUnidadMedidaProducto FOREIGN KEY (IdUnidadMedidaProducto) REFERENCES UnidadMedidaProducto(IdUnidadMedidaProducto),	
	CONSTRAINT fk_IdPresentacionProducto FOREIGN KEY (IdPresentacionProducto) REFERENCES PresentacionProducto(IdPresentacionProducto),	
);

--------------------------------------------------------------


CREATE TABLE Stock (
    IdStock bigint IDENTITY(1,1) PRIMARY KEY,
	IdProducto int,
    IdSede int,
	Cantidad int,
	CantidadMinima int,--la cantidad mínima de unidades que se desea tener en el inventario antes de realizar un pedido de reposición.
	CantidadMaxima int,--la cantidad máxima de unidades que se desea tener en el inventario antes de dejar de recibir pedidos.	
	FechaCaducidad DATETIME, 
	UbicacionFisica varchar(255),--la ubicación física en el almacén donde se encuentra el producto.
	FechaModificacion DATETIME NOT NULL,
	CONSTRAINT fk_IdProducto FOREIGN KEY (IdProducto) REFERENCES Producto(IdProducto),
	CONSTRAINT fk_IdSedeStock FOREIGN KEY (IdSede) REFERENCES Sede(IdSede)	
);

CREATE TABLE StockBitacora (
    IdStockBitacora bigint IDENTITY(1,1) PRIMARY KEY,
	IdStock bigint,
	IdUsuario bigint,
    Accion varchar(50),
	Cantidad int,
	Descripcion varchar(250),
	FechaModificacion DATETIME NOT NULL,
	CONSTRAINT fk_IdStock FOREIGN KEY (IdStock) REFERENCES Stock(IdStock),-- ON DELETE CASCADE,	
	CONSTRAINT fk_IdUsuario FOREIGN KEY (IdUsuario) REFERENCES Usuario(IdUsuario)--ON DELETE NO ACTION,
);


CREATE TABLE Requisicion (
    IdRequisicion bigint IDENTITY(1,1) PRIMARY KEY NOT NULL,
    IdTipoRequisicion int NOT NULL, 
	IdUnidad int NOT NULL,
    IdUsuario bigint NOT NULL, -- La clave foránea que referencia al usuario que solicita la requisición	
    FechaRequisicion DATETIME NOT NULL,
    Descripcion varchar(500),
    EstadoActual varchar(50) NOT NULL, -- Puede ser "Pendiente", "Aprobada", "Rechazada","Despachada" etc.
    FechaModificacion DATETIME NOT NULL,
    CONSTRAINT fk_IdTipoRequisicion FOREIGN KEY (IdTipoRequisicion) REFERENCES TipoRequisicion(IdTipoRequisicion),
	CONSTRAINT fk_IdUnidad FOREIGN KEY (IdUnidad) REFERENCES Unidad(IdUnidad),
    CONSTRAINT fk_IdUsuarioReq FOREIGN KEY (IdUsuario) REFERENCES Usuario(IdUsuario)
);

CREATE TABLE RequisicionDetalle (
	IdRequisicionDetalle bigint IDENTITY(1,1) PRIMARY KEY NOT NULL,
	IdRequisicion bigint NOT NULL,
	IdProducto int NOT NULL,
	CantidadSolicitada int NOT NULL,
	CONSTRAINT fk_IdRequisicion FOREIGN KEY (IdRequisicion) REFERENCES Requisicion(IdRequisicion),
	CONSTRAINT fk_IdProductoDetalleReq FOREIGN KEY (IdProducto) REFERENCES Producto(IdProducto)
);

CREATE TABLE RequisicionBitacora (
    IdRequisicionBitacora bigint IDENTITY(1,1) PRIMARY KEY NOT NULL,
    IdRequisicion bigint NOT NULL,
    IdUsuario bigint NOT NULL,
    Accion varchar(50) NOT NULL, -- Acción realizada (crear, aprobar, eliminar, retener, despachar, etc.)
	FechaAccion DATETIME NOT NULL,
    Estado varchar(50) NOT NULL, -- Estado actual de la requisición        
    CONSTRAINT fk_IdRequisicionBitacora FOREIGN KEY (IdRequisicion) REFERENCES Requisicion(IdRequisicion),
    CONSTRAINT fk_IdUsuarioReqBitacora FOREIGN KEY (IdUsuario) REFERENCES Usuario(IdUsuario)
);

CREATE TABLE RegistroErrores (
    IDError bigint IDENTITY(1,1) PRIMARY KEY NOT NULL,
    FechaRegistro DATETIME,
    MensajeError VARCHAR(255),
    DetallesError TEXT
);

CREATE TABLE RegistroAcciones (
    IDAccion bigint IDENTITY(1,1) PRIMARY KEY NOT NULL,
    FechaAccion DATETIME,
    TipoAccion VARCHAR(50),
    DescripcionAccion TEXT
);



DROP TABLE IF EXISTS StockBitacora;
DROP TABLE IF EXISTS Stock;
DROP TABLE IF EXISTS Producto;
DROP TABLE IF EXISTS Presentacion;
DROP TABLE IF EXISTS UnidadMedida;
DROP TABLE IF EXISTS TipoProducto;
DROP TABLE IF EXISTS Usuario;
DROP TABLE IF EXISTS Sede;
DROP TABLE IF EXISTS Rol;
DROP TABLE IF EXISTS Departamento;
DROP TABLE IF EXISTS Unidad;
DROP TABLE IF EXISTS TipoRequisicion;