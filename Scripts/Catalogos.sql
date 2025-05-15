-- Tipos de Requisicion
INSERT INTO TipoRequisicion VALUES ('Activos Fungibles','',GETDATE());
INSERT INTO TipoRequisicion VALUES ('Activos Fijos','',GETDATE());
INSERT INTO TipoRequisicion VALUES ('Materiales','',GETDATE());

-- Unidad
INSERT INTO Unidad VALUES ('Inform�tica','',GETDATE());
INSERT INTO Unidad VALUES ('Unidad de Infraestructura','',GETDATE());
INSERT INTO Unidad VALUES ('Comisi�n de Recepci�n','',GETDATE());

-- Departamento
INSERT INTO Departamento VALUES ('Alta Verapaz',GETDATE());
INSERT INTO Departamento VALUES ('Baja Verapaz',GETDATE());
INSERT INTO Departamento VALUES ('Chimaltenango',GETDATE());
INSERT INTO Departamento VALUES ('Chiquimula',GETDATE());
INSERT INTO Departamento VALUES ('El Progreso',GETDATE());
INSERT INTO Departamento VALUES ('Escuintla',GETDATE());
INSERT INTO Departamento VALUES ('Guatemala',GETDATE());
INSERT INTO Departamento VALUES ('Huehuetenango',GETDATE());
INSERT INTO Departamento VALUES ('Izabal',GETDATE());
INSERT INTO Departamento VALUES ('Jalapa',GETDATE());
INSERT INTO Departamento VALUES ('Jutiapa',GETDATE());
INSERT INTO Departamento VALUES ('Pet�n',GETDATE());
INSERT INTO Departamento VALUES ('Quetzaltenango',GETDATE());
INSERT INTO Departamento VALUES ('Quich�',GETDATE());
INSERT INTO Departamento VALUES ('Retalhuleu',GETDATE());
INSERT INTO Departamento VALUES ('Sacatep�quez',GETDATE());
INSERT INTO Departamento VALUES ('San Marcos',GETDATE());
INSERT INTO Departamento VALUES ('Santa Rosa',GETDATE());
INSERT INTO Departamento VALUES ('Solola',GETDATE());
INSERT INTO Departamento VALUES ('Suchitep�quez',GETDATE());
INSERT INTO Departamento VALUES ('Totonicap�n',GETDATE());
INSERT INTO Departamento VALUES ('Zacapa',GETDATE());

-- Roles
INSERT INTO Rol VALUES ('SuperAdministrador','',GETDATE());
INSERT INTO Rol VALUES ('Administrador','',GETDATE());
INSERT INTO Rol VALUES ('Operador Sede','',GETDATE());
INSERT INTO Rol VALUES ('Operador Almacen','',GETDATE());

-- Sedes
INSERT INTO Sede VALUES (7,'SEDE GUATEMALA', 'CIUDAD DE GUATEMALA',GETDATE());
INSERT INTO Sede VALUES (3,'SEDE CHIMALTENANGO', '',GETDATE());

-- Usuario
INSERT INTO Usuario VALUES (1,1,'DonyStreams','Brandon','Soto','CIUDAD','sotobrandon198@gmail.com','58555448',GETDATE())
INSERT INTO Usuario VALUES (1,2,'Nefertari','Rossmery','Pacheco','Lote 18 Manzana F','prueba@gmail.com','58555448',GETDATE())
INSERT INTO Usuario VALUES (1,1,'Nefertari2','Rossmery','Pacheco','Lote 18 Manzana F','prueba2@gmail.com','58555448',GETDATE())
INSERT INTO Usuario VALUES (3,2,'UserOpSede','userSede','prueba sede','ciudad gt','sede1@gmail.com','58555448',GETDATE())

-- Tipos de Producto
INSERT INTO TipoProducto VALUES ('Drones y sistemas de mapeo','',GETDATE());
INSERT INTO TipoProducto VALUES ('Herramientas de autenticaci�n de medios','',GETDATE());
INSERT INTO TipoProducto VALUES ('Microscopios y comparadores bal�sticos','',GETDATE());
INSERT INTO TipoProducto VALUES ('Materiales de embalaje y etiquetado','',GETDATE());
INSERT INTO TipoProducto VALUES ('Kits de recolecci�n de evidencia','',GETDATE());

-- Unidad de Medida
INSERT INTO UnidadMedidaProducto VALUES ('metros','Longitud',GETDATE());
INSERT INTO UnidadMedidaProducto VALUES ('litros','Volumen',GETDATE());
INSERT INTO UnidadMedidaProducto VALUES ('gramos','Peso',GETDATE());

-- Presentacion producto
INSERT INTO PresentacionProducto VALUES ('Unidad','',GETDATE());
INSERT INTO PresentacionProducto VALUES ('Caja','',GETDATE());
INSERT INTO PresentacionProducto VALUES ('Paquete','',GETDATE());
INSERT INTO PresentacionProducto VALUES ('Botella','',GETDATE());
INSERT INTO PresentacionProducto VALUES ('Lata','',GETDATE());
INSERT INTO PresentacionProducto VALUES ('Bolsa','',GETDATE());
INSERT INTO PresentacionProducto VALUES ('Pildora','',GETDATE());
INSERT INTO PresentacionProducto VALUES ('Aerosol','',GETDATE());
INSERT INTO PresentacionProducto VALUES ('Liquido','',GETDATE());
INSERT INTO PresentacionProducto VALUES ('Frasco','',GETDATE());

-- Producto

INSERT INTO Producto VALUES (5,NULL,1,'Kit de Recolecci�n 2','10',NULL,'10','sdas',GETDATE());
INSERT INTO Producto VALUES (5,NULL,1,'Kit de Recolecci�n 5','10',NULL,'10','sdas2',GETDATE());
INSERT INTO Producto VALUES (NULL,1,1,'Kit de Recolecci�n 3','10',NULL,'10',NULL,GETDATE());
INSERT INTO Producto VALUES (NULL,1,1,'Kit de Recolecci�n 6','10',NULL,'10',NULL,GETDATE());
INSERT INTO Producto VALUES (1,1,1,'Kit de Recolecci�n 4','11',NULL,'10',NULL,GETDATE());
INSERT INTO Producto VALUES (2,2,2,'Kit de Recolecci�n 8','11',NULL,'10',NULL,GETDATE());

