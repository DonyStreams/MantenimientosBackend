USE [inventarios]
GO
SET QUOTED_IDENTIFIER ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET QUOTED_IDENTIFIER ON
GO
/****** Object:  Table [dbo].[Areas]    Script Date: 24/02/2026 23:58:48 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Areas](
	[id_area] [int] IDENTITY(1,1) NOT NULL,
	[codigo_area] [varchar](20) NULL,
	[nombre] [varchar](100) NULL,
	[tipo_area] [varchar](50) NULL,
	[estado] [bit] NULL,
	[fecha_creacion] [datetime] NULL,
	[usuario_creacion] [int] NULL,
	[fecha_modificacion] [datetime] NULL,
	[usuario_modificacion] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[id_area] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
SET QUOTED_IDENTIFIER ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET QUOTED_IDENTIFIER ON
GO
/****** Object:  Table [dbo].[Categoria_Equipo]    Script Date: 24/02/2026 23:58:48 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Categoria_Equipo](
	[id_categoria] [int] IDENTITY(1,1) NOT NULL,
	[nombre] [nvarchar](120) NOT NULL,
	[descripcion] [nvarchar](255) NULL,
	[id_padre] [int] NULL,
	[estado] [bit] NOT NULL,
	[fecha_creacion] [datetime] NOT NULL,
	[fecha_modificacion] [datetime] NULL,
	[usuario_creacion] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[id_categoria] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Comentarios_Ejecucion]    Script Date: 24/02/2026 23:58:48 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Comentarios_Ejecucion](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[id_ejecucion] [int] NOT NULL,
	[usuario_id] [int] NULL,
	[tipo_comentario] [varchar](50) NOT NULL,
	[comentario] [nvarchar](max) NOT NULL,
	[estado_anterior] [varchar](50) NULL,
	[estado_nuevo] [varchar](50) NULL,
	[fecha_creacion] [datetime] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Comentarios_Ticket]    Script Date: 24/02/2026 23:58:48 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Comentarios_Ticket](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[ticket_id] [int] NOT NULL,
	[usuario_id] [int] NOT NULL,
	[tipo_comentario_id] [int] NOT NULL,
	[comentario] [nvarchar](max) NULL,
	[fecha_creacion] [datetime] NULL,
	[estado_anterior] [varchar](50) NULL,
	[estado_nuevo] [varchar](50) NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Configuracion_Alertas]    Script Date: 24/02/2026 23:58:48 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Configuracion_Alertas](
	[id_configuracion] [int] IDENTITY(1,1) NOT NULL,
	[nombre] [varchar](100) NOT NULL,
	[descripcion] [varchar](255) NULL,
	[tipo_alerta] [varchar](50) NOT NULL,
	[dias_anticipacion] [int] NULL,
	[activa] [bit] NULL,
	[usuarios_notificar] [nvarchar](max) NULL,
	[fecha_creacion] [datetime] NULL,
	[usuario_creacion] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[id_configuracion] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Contrato_Equipo]    Script Date: 24/02/2026 23:58:48 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Contrato_Equipo](
	[id_contrato] [int] NOT NULL,
	[id_equipo] [int] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[id_contrato] ASC,
	[id_equipo] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Contrato_Tipo_Mantenimiento]    Script Date: 24/02/2026 23:58:48 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Contrato_Tipo_Mantenimiento](
	[id_contrato] [int] NOT NULL,
	[id_tipo] [int] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[id_contrato] ASC,
	[id_tipo] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Contratos]    Script Date: 24/02/2026 23:58:48 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Contratos](
	[id_contrato] [int] IDENTITY(1,1) NOT NULL,
	[fecha_inicio] [date] NULL,
	[fecha_fin] [date] NULL,
	[descripcion] [nvarchar](max) NULL,
	[frecuencia] [varchar](20) NULL,
	[estado] [bit] NULL,
	[id_estado] [int] NULL,
	[id_proveedor] [int] NULL,
	[fecha_creacion] [datetime] NULL,
	[usuario_creacion] [int] NULL,
	[fecha_modificacion] [datetime] NULL,
	[usuario_modificacion] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[id_contrato] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Documentos_Contrato]    Script Date: 24/02/2026 23:58:48 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Documentos_Contrato](
	[id_documento] [int] IDENTITY(1,1) NOT NULL,
	[id_contrato] [int] NOT NULL,
	[nombre_archivo] [varchar](255) NOT NULL,
	[ruta_archivo] [varchar](500) NOT NULL,
	[tipo_documento] [varchar](50) NULL,
	[tamanio_archivo] [bigint] NULL,
	[tipo_mime] [varchar](100) NULL,
	[fecha_subida] [datetime] NULL,
	[usuario_subida] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[id_documento] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Ejecuciones_Mantenimiento]    Script Date: 24/02/2026 23:58:48 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Ejecuciones_Mantenimiento](
	[id_ejecucion] [int] IDENTITY(1,1) NOT NULL,
	[id_contrato] [int] NULL,
	[id_equipo] [int] NULL,
	[fecha_ejecucion] [datetime] NULL,
	[bitacora] [nvarchar](max) NULL,
	[usuario_responsable] [int] NULL,
	[fecha_creacion] [datetime] NULL,
	[fecha_modificacion] [datetime] NULL,
	[usuario_creacion] [int] NULL,
	[usuario_modificacion] [int] NULL,
	[estado] [varchar](20) NOT NULL,
	[fecha_inicio_trabajo] [datetime] NULL,
	[fecha_cierre] [datetime] NULL,
	[id_programacion] [int] NULL,
	[id_tipo_mantenimiento] [int] NULL,
	[fecha_programada] [date] NULL,
PRIMARY KEY CLUSTERED 
(
	[id_ejecucion] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
SET QUOTED_IDENTIFIER ON
GO
/****** Object:  Table [dbo].[Equipos]    Script Date: 24/02/2026 23:58:48 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Equipos](
	[id_equipo] [int] IDENTITY(1,1) NOT NULL,
	[nombre] [varchar](100) NULL,
	[codigo_inacif] [varchar](50) NULL,
	[marca] [varchar](50) NULL,
	[modelo] [varchar](50) NULL,
	[numero_serie] [varchar](50) NULL,
	[ubicacion] [varchar](100) NULL,
	[magnitud_medicion] [varchar](100) NULL,
	[rango_capacidad] [varchar](100) NULL,
	[manual_fabricante] [varchar](100) NULL,
	[fotografia] [varchar](255) NULL,
	[software_firmware] [varchar](100) NULL,
	[condiciones_operacion] [varchar](255) NULL,
	[descripcion] [nvarchar](max) NULL,
	[fecha_creacion] [datetime] NULL,
	[fecha_modificacion] [datetime] NULL,
	[id_area] [int] NULL,
	[id_categoria] [int] NULL,
	[estado] [varchar](20) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[id_equipo] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Estados_Mantenimiento]    Script Date: 24/02/2026 23:58:48 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Estados_Mantenimiento](
	[id_estado] [int] IDENTITY(1,1) NOT NULL,
	[codigo] [varchar](20) NOT NULL,
	[nombre] [varchar](50) NOT NULL,
	[descripcion] [varchar](255) NULL,
	[color] [varchar](7) NULL,
	[orden_secuencia] [int] NULL,
	[es_estado_inicial] [bit] NULL,
	[es_estado_final] [bit] NULL,
	[activo] [bit] NULL,
	[fecha_creacion] [datetime] NULL,
	[usuario_creacion] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[id_estado] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED 
(
	[codigo] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Evidencias]    Script Date: 24/02/2026 23:58:48 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Evidencias](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[entidad_relacionada] [varchar](50) NOT NULL,
	[entidad_id] [int] NOT NULL,
	[archivo_url] [nvarchar](500) NOT NULL,
	[descripcion] [nvarchar](max) NULL,
	[fecha_creacion] [datetime] NULL,
	[nombre_archivo] [varchar](255) NULL,
	[nombre_original] [varchar](255) NULL,
	[tipo_archivo] [varchar](100) NULL,
	[tamanio] [bigint] NULL,
	[usuario_creacion] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Historial_Equipo]    Script Date: 24/02/2026 23:58:48 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Historial_Equipo](
	[id_historial] [int] IDENTITY(1,1) NOT NULL,
	[id_equipo] [int] NULL,
	[fecha_registro] [datetime] NULL,
	[descripcion] [nvarchar](max) NULL,
	[tipo_cambio] [varchar](50) NULL,
	[usuario_id] [int] NULL,
	[usuario_nombre] [varchar](100) NULL,
	[ticket_id] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[id_historial] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
SET QUOTED_IDENTIFIER ON
GO
/****** Object:  Table [dbo].[Historial_Programacion]    Script Date: 24/02/2026 23:58:48 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Historial_Programacion](
	[id_historial] [int] IDENTITY(1,1) NOT NULL,
	[id_programacion] [int] NOT NULL,
	[tipo_evento] [varchar](20) NOT NULL,
	[fecha_original] [date] NOT NULL,
	[fecha_nueva] [date] NULL,
	[motivo] [nvarchar](500) NULL,
	[id_ejecucion] [int] NULL,
	[usuario_id] [int] NULL,
	[fecha_registro] [datetime] NULL,
PRIMARY KEY CLUSTERED 
(
	[id_historial] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
SET QUOTED_IDENTIFIER ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET QUOTED_IDENTIFIER ON
GO
/****** Object:  Table [dbo].[Notificaciones]    Script Date: 24/02/2026 23:58:48 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Notificaciones](
	[id_notificacion] [int] IDENTITY(1,1) NOT NULL,
	[tipo_notificacion] [varchar](50) NOT NULL,
	[titulo] [varchar](200) NOT NULL,
	[mensaje] [nvarchar](max) NOT NULL,
	[entidad_relacionada] [varchar](50) NULL,
	[entidad_id] [int] NULL,
	[prioridad] [varchar](20) NULL,
	[leida] [bit] NULL,
	[fecha_creacion] [datetime] NULL,
	[fecha_lectura] [datetime] NULL,
	[usuario_destinatario] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[id_notificacion] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Programaciones_Mantenimiento]    Script Date: 24/02/2026 23:58:48 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Programaciones_Mantenimiento](
	[id_programacion] [int] IDENTITY(1,1) NOT NULL,
	[id_equipo] [int] NOT NULL,
	[id_tipo_mantenimiento] [int] NOT NULL,
	[frecuencia_dias] [int] NOT NULL,
	[fecha_ultimo_mantenimiento] [date] NULL,
	[fecha_proximo_mantenimiento] [date] NULL,
	[dias_alerta_previa] [int] NULL,
	[activa] [bit] NULL,
	[observaciones] [nvarchar](max) NULL,
	[fecha_creacion] [datetime2](7) NULL,
	[fecha_modificacion] [datetime2](7) NULL,
	[usuario_creacion] [int] NULL,
	[usuario_modificacion] [int] NULL,
	[id_contrato] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[id_programacion] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Proveedores]    Script Date: 24/02/2026 23:58:48 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Proveedores](
	[id_proveedor] [int] IDENTITY(1,1) NOT NULL,
	[nit] [varchar](20) NULL,
	[nombre] [varchar](100) NULL,
	[estado] [bit] NULL,
	[fecha_creacion] [datetime] NULL,
	[usuario_creacion] [int] NULL,
	[fecha_modificacion] [datetime] NULL,
	[usuario_modificacion] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[id_proveedor] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED 
(
	[nit] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
SET QUOTED_IDENTIFIER ON
GO
SET QUOTED_IDENTIFIER ON
GO
/****** Object:  Table [dbo].[Tickets]    Script Date: 24/02/2026 23:58:48 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Tickets](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[equipo_id] [int] NOT NULL,
	[usuario_creador_id] [int] NOT NULL,
	[usuario_asignado_id] [int] NULL,
	[descripcion] [nvarchar](max) NULL,
	[prioridad] [varchar](20) NULL,
	[estado] [varchar](20) NULL,
	[fecha_creacion] [datetime] NULL,
	[fecha_modificacion] [datetime] NULL,
	[fecha_cierre] [datetime] NULL,
	[usuario_creacion] [int] NULL,
	[usuario_modificacion] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Tipos_Comentario]    Script Date: 24/02/2026 23:58:48 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Tipos_Comentario](
	[id_tipo] [int] IDENTITY(1,1) NOT NULL,
	[nombre] [varchar](50) NULL,
PRIMARY KEY CLUSTERED 
(
	[id_tipo] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED 
(
	[nombre] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Tipos_Mantenimiento]    Script Date: 24/02/2026 23:58:48 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Tipos_Mantenimiento](
	[id_tipo] [int] IDENTITY(1,1) NOT NULL,
	[codigo] [varchar](20) NULL,
	[nombre] [varchar](50) NULL,
	[estado] [bit] NULL,
	[fecha_creacion] [datetime] NULL,
	[usuario_creacion] [int] NULL,
	[fecha_modificacion] [datetime] NULL,
	[usuario_modificacion] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[id_tipo] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Usuarios]    Script Date: 24/02/2026 23:58:48 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Usuarios](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[keycloak_id] [uniqueidentifier] NOT NULL,
	[nombre_completo] [varchar](100) NULL,
	[correo] [varchar](100) NULL,
	[activo] [bit] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED 
(
	[keycloak_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
ALTER TABLE [dbo].[Areas] ADD  DEFAULT (getdate()) FOR [fecha_creacion]
GO
ALTER TABLE [dbo].[Categoria_Equipo] ADD  CONSTRAINT [DF_CategoriaEquipo_Estado]  DEFAULT ((1)) FOR [estado]
GO
ALTER TABLE [dbo].[Categoria_Equipo] ADD  CONSTRAINT [DF_CategoriaEquipo_FCreacion]  DEFAULT (getdate()) FOR [fecha_creacion]
GO
ALTER TABLE [dbo].[Comentarios_Ejecucion] ADD  DEFAULT ('SEGUIMIENTO') FOR [tipo_comentario]
GO
ALTER TABLE [dbo].[Comentarios_Ejecucion] ADD  DEFAULT (getdate()) FOR [fecha_creacion]
GO
ALTER TABLE [dbo].[Comentarios_Ticket] ADD  DEFAULT (getdate()) FOR [fecha_creacion]
GO
ALTER TABLE [dbo].[Configuracion_Alertas] ADD  DEFAULT ((30)) FOR [dias_anticipacion]
GO
ALTER TABLE [dbo].[Configuracion_Alertas] ADD  DEFAULT ((1)) FOR [activa]
GO
ALTER TABLE [dbo].[Configuracion_Alertas] ADD  DEFAULT (getdate()) FOR [fecha_creacion]
GO
ALTER TABLE [dbo].[Contratos] ADD  DEFAULT (getdate()) FOR [fecha_creacion]
GO
ALTER TABLE [dbo].[Documentos_Contrato] ADD  DEFAULT (getdate()) FOR [fecha_subida]
GO
ALTER TABLE [dbo].[Ejecuciones_Mantenimiento] ADD  DEFAULT (getdate()) FOR [fecha_ejecucion]
GO
ALTER TABLE [dbo].[Ejecuciones_Mantenimiento] ADD  DEFAULT (getdate()) FOR [fecha_creacion]
GO
ALTER TABLE [dbo].[Ejecuciones_Mantenimiento] ADD  CONSTRAINT [DF_Ejecuciones_Estado]  DEFAULT ('PROGRAMADO') FOR [estado]
GO
ALTER TABLE [dbo].[Equipos] ADD  DEFAULT (getdate()) FOR [fecha_creacion]
GO
ALTER TABLE [dbo].[Equipos] ADD  DEFAULT ('Activo') FOR [estado]
GO
ALTER TABLE [dbo].[Estados_Mantenimiento] ADD  DEFAULT ((0)) FOR [es_estado_inicial]
GO
ALTER TABLE [dbo].[Estados_Mantenimiento] ADD  DEFAULT ((0)) FOR [es_estado_final]
GO
ALTER TABLE [dbo].[Estados_Mantenimiento] ADD  DEFAULT ((1)) FOR [activo]
GO
ALTER TABLE [dbo].[Estados_Mantenimiento] ADD  DEFAULT (getdate()) FOR [fecha_creacion]
GO
ALTER TABLE [dbo].[Evidencias] ADD  DEFAULT (getdate()) FOR [fecha_creacion]
GO
ALTER TABLE [dbo].[Historial_Equipo] ADD  DEFAULT (getdate()) FOR [fecha_registro]
GO
ALTER TABLE [dbo].[Historial_Programacion] ADD  DEFAULT (getdate()) FOR [fecha_registro]
GO
ALTER TABLE [dbo].[Notificaciones] ADD  DEFAULT ('Media') FOR [prioridad]
GO
ALTER TABLE [dbo].[Notificaciones] ADD  DEFAULT ((0)) FOR [leida]
GO
ALTER TABLE [dbo].[Notificaciones] ADD  DEFAULT (getdate()) FOR [fecha_creacion]
GO
ALTER TABLE [dbo].[Programaciones_Mantenimiento] ADD  DEFAULT ((7)) FOR [dias_alerta_previa]
GO
ALTER TABLE [dbo].[Programaciones_Mantenimiento] ADD  DEFAULT ((1)) FOR [activa]
GO
ALTER TABLE [dbo].[Programaciones_Mantenimiento] ADD  DEFAULT (getdate()) FOR [fecha_creacion]
GO
ALTER TABLE [dbo].[Programaciones_Mantenimiento] ADD  DEFAULT (getdate()) FOR [fecha_modificacion]
GO
ALTER TABLE [dbo].[Proveedores] ADD  DEFAULT (getdate()) FOR [fecha_creacion]
GO
ALTER TABLE [dbo].[Tickets] ADD  DEFAULT (getdate()) FOR [fecha_creacion]
GO
ALTER TABLE [dbo].[Tipos_Mantenimiento] ADD  DEFAULT (getdate()) FOR [fecha_creacion]
GO
ALTER TABLE [dbo].[Usuarios] ADD  DEFAULT ((1)) FOR [activo]
GO
ALTER TABLE [dbo].[Areas]  WITH CHECK ADD FOREIGN KEY([usuario_creacion])
REFERENCES [dbo].[Usuarios] ([id])
GO
ALTER TABLE [dbo].[Areas]  WITH CHECK ADD FOREIGN KEY([usuario_modificacion])
REFERENCES [dbo].[Usuarios] ([id])
GO
ALTER TABLE [dbo].[Categoria_Equipo]  WITH CHECK ADD  CONSTRAINT [FK_CategoriaEquipo_Padre] FOREIGN KEY([id_padre])
REFERENCES [dbo].[Categoria_Equipo] ([id_categoria])
GO
ALTER TABLE [dbo].[Categoria_Equipo] CHECK CONSTRAINT [FK_CategoriaEquipo_Padre]
GO
ALTER TABLE [dbo].[Comentarios_Ejecucion]  WITH CHECK ADD  CONSTRAINT [FK_ComentarioEjecucion_Ejecucion] FOREIGN KEY([id_ejecucion])
REFERENCES [dbo].[Ejecuciones_Mantenimiento] ([id_ejecucion])
GO
ALTER TABLE [dbo].[Comentarios_Ejecucion] CHECK CONSTRAINT [FK_ComentarioEjecucion_Ejecucion]
GO
ALTER TABLE [dbo].[Comentarios_Ejecucion]  WITH CHECK ADD  CONSTRAINT [FK_ComentarioEjecucion_Usuario] FOREIGN KEY([usuario_id])
REFERENCES [dbo].[Usuarios] ([id])
GO
ALTER TABLE [dbo].[Comentarios_Ejecucion] CHECK CONSTRAINT [FK_ComentarioEjecucion_Usuario]
GO
ALTER TABLE [dbo].[Comentarios_Ticket]  WITH CHECK ADD FOREIGN KEY([ticket_id])
REFERENCES [dbo].[Tickets] ([id])
GO
ALTER TABLE [dbo].[Comentarios_Ticket]  WITH CHECK ADD FOREIGN KEY([tipo_comentario_id])
REFERENCES [dbo].[Tipos_Comentario] ([id_tipo])
GO
ALTER TABLE [dbo].[Comentarios_Ticket]  WITH CHECK ADD FOREIGN KEY([usuario_id])
REFERENCES [dbo].[Usuarios] ([id])
GO
ALTER TABLE [dbo].[Configuracion_Alertas]  WITH CHECK ADD FOREIGN KEY([usuario_creacion])
REFERENCES [dbo].[Usuarios] ([id])
GO
ALTER TABLE [dbo].[Contrato_Equipo]  WITH CHECK ADD FOREIGN KEY([id_contrato])
REFERENCES [dbo].[Contratos] ([id_contrato])
GO
ALTER TABLE [dbo].[Contrato_Equipo]  WITH CHECK ADD FOREIGN KEY([id_equipo])
REFERENCES [dbo].[Equipos] ([id_equipo])
GO
ALTER TABLE [dbo].[Contrato_Tipo_Mantenimiento]  WITH CHECK ADD FOREIGN KEY([id_contrato])
REFERENCES [dbo].[Contratos] ([id_contrato])
GO
ALTER TABLE [dbo].[Contrato_Tipo_Mantenimiento]  WITH CHECK ADD FOREIGN KEY([id_tipo])
REFERENCES [dbo].[Tipos_Mantenimiento] ([id_tipo])
GO
ALTER TABLE [dbo].[Contratos]  WITH CHECK ADD FOREIGN KEY([id_estado])
REFERENCES [dbo].[Estados_Mantenimiento] ([id_estado])
GO
ALTER TABLE [dbo].[Contratos]  WITH CHECK ADD FOREIGN KEY([id_proveedor])
REFERENCES [dbo].[Proveedores] ([id_proveedor])
GO
ALTER TABLE [dbo].[Contratos]  WITH CHECK ADD FOREIGN KEY([usuario_creacion])
REFERENCES [dbo].[Usuarios] ([id])
GO
ALTER TABLE [dbo].[Contratos]  WITH CHECK ADD FOREIGN KEY([usuario_modificacion])
REFERENCES [dbo].[Usuarios] ([id])
GO
ALTER TABLE [dbo].[Documentos_Contrato]  WITH CHECK ADD FOREIGN KEY([id_contrato])
REFERENCES [dbo].[Contratos] ([id_contrato])
GO
ALTER TABLE [dbo].[Documentos_Contrato]  WITH CHECK ADD FOREIGN KEY([usuario_subida])
REFERENCES [dbo].[Usuarios] ([id])
GO
ALTER TABLE [dbo].[Ejecuciones_Mantenimiento]  WITH CHECK ADD FOREIGN KEY([id_contrato])
REFERENCES [dbo].[Contratos] ([id_contrato])
GO
ALTER TABLE [dbo].[Ejecuciones_Mantenimiento]  WITH CHECK ADD FOREIGN KEY([id_equipo])
REFERENCES [dbo].[Equipos] ([id_equipo])
GO
ALTER TABLE [dbo].[Ejecuciones_Mantenimiento]  WITH CHECK ADD FOREIGN KEY([usuario_responsable])
REFERENCES [dbo].[Usuarios] ([id])
GO
ALTER TABLE [dbo].[Ejecuciones_Mantenimiento]  WITH CHECK ADD FOREIGN KEY([usuario_creacion])
REFERENCES [dbo].[Usuarios] ([id])
GO
ALTER TABLE [dbo].[Ejecuciones_Mantenimiento]  WITH CHECK ADD FOREIGN KEY([usuario_modificacion])
REFERENCES [dbo].[Usuarios] ([id])
GO
ALTER TABLE [dbo].[Ejecuciones_Mantenimiento]  WITH CHECK ADD  CONSTRAINT [FK_Ejecucion_TipoMantenimiento] FOREIGN KEY([id_tipo_mantenimiento])
REFERENCES [dbo].[Tipos_Mantenimiento] ([id_tipo])
GO
ALTER TABLE [dbo].[Ejecuciones_Mantenimiento] CHECK CONSTRAINT [FK_Ejecucion_TipoMantenimiento]
GO
ALTER TABLE [dbo].[Ejecuciones_Mantenimiento]  WITH CHECK ADD  CONSTRAINT [FK_Ejecuciones_Programacion] FOREIGN KEY([id_programacion])
REFERENCES [dbo].[Programaciones_Mantenimiento] ([id_programacion])
GO
ALTER TABLE [dbo].[Ejecuciones_Mantenimiento] CHECK CONSTRAINT [FK_Ejecuciones_Programacion]
GO
ALTER TABLE [dbo].[Equipos]  WITH CHECK ADD  CONSTRAINT [FK_Equipos_Areas] FOREIGN KEY([id_area])
REFERENCES [dbo].[Areas] ([id_area])
GO
ALTER TABLE [dbo].[Equipos] CHECK CONSTRAINT [FK_Equipos_Areas]
GO
ALTER TABLE [dbo].[Equipos]  WITH CHECK ADD  CONSTRAINT [FK_Equipos_Categoria] FOREIGN KEY([id_categoria])
REFERENCES [dbo].[Categoria_Equipo] ([id_categoria])
GO
ALTER TABLE [dbo].[Equipos] CHECK CONSTRAINT [FK_Equipos_Categoria]
GO
ALTER TABLE [dbo].[Estados_Mantenimiento]  WITH CHECK ADD FOREIGN KEY([usuario_creacion])
REFERENCES [dbo].[Usuarios] ([id])
GO
ALTER TABLE [dbo].[Evidencias]  WITH CHECK ADD  CONSTRAINT [FK_Evidencias_Usuario] FOREIGN KEY([usuario_creacion])
REFERENCES [dbo].[Usuarios] ([id])
GO
ALTER TABLE [dbo].[Evidencias] CHECK CONSTRAINT [FK_Evidencias_Usuario]
GO
ALTER TABLE [dbo].[Historial_Equipo]  WITH CHECK ADD FOREIGN KEY([id_equipo])
REFERENCES [dbo].[Equipos] ([id_equipo])
GO
ALTER TABLE [dbo].[Historial_Equipo]  WITH CHECK ADD  CONSTRAINT [FK_Historial_Usuario] FOREIGN KEY([usuario_id])
REFERENCES [dbo].[Usuarios] ([id])
GO
ALTER TABLE [dbo].[Historial_Equipo] CHECK CONSTRAINT [FK_Historial_Usuario]
GO
ALTER TABLE [dbo].[Historial_Programacion]  WITH CHECK ADD  CONSTRAINT [FK_HistorialProg_Ejecucion] FOREIGN KEY([id_ejecucion])
REFERENCES [dbo].[Ejecuciones_Mantenimiento] ([id_ejecucion])
GO
ALTER TABLE [dbo].[Historial_Programacion] CHECK CONSTRAINT [FK_HistorialProg_Ejecucion]
GO
ALTER TABLE [dbo].[Historial_Programacion]  WITH CHECK ADD  CONSTRAINT [FK_HistorialProg_Programacion] FOREIGN KEY([id_programacion])
REFERENCES [dbo].[Programaciones_Mantenimiento] ([id_programacion])
GO
ALTER TABLE [dbo].[Historial_Programacion] CHECK CONSTRAINT [FK_HistorialProg_Programacion]
GO
ALTER TABLE [dbo].[Historial_Programacion]  WITH CHECK ADD  CONSTRAINT [FK_HistorialProg_Usuario] FOREIGN KEY([usuario_id])
REFERENCES [dbo].[Usuarios] ([id])
GO
ALTER TABLE [dbo].[Historial_Programacion] CHECK CONSTRAINT [FK_HistorialProg_Usuario]
GO
ALTER TABLE [dbo].[Notificaciones]  WITH CHECK ADD FOREIGN KEY([usuario_destinatario])
REFERENCES [dbo].[Usuarios] ([id])
GO
ALTER TABLE [dbo].[Programaciones_Mantenimiento]  WITH CHECK ADD  CONSTRAINT [FK_Programaciones_Contrato] FOREIGN KEY([id_contrato])
REFERENCES [dbo].[Contratos] ([id_contrato])
GO
ALTER TABLE [dbo].[Programaciones_Mantenimiento] CHECK CONSTRAINT [FK_Programaciones_Contrato]
GO
ALTER TABLE [dbo].[Programaciones_Mantenimiento]  WITH CHECK ADD  CONSTRAINT [FK_ProgramacionMantenimiento_Equipo] FOREIGN KEY([id_equipo])
REFERENCES [dbo].[Equipos] ([id_equipo])
GO
ALTER TABLE [dbo].[Programaciones_Mantenimiento] CHECK CONSTRAINT [FK_ProgramacionMantenimiento_Equipo]
GO
ALTER TABLE [dbo].[Programaciones_Mantenimiento]  WITH CHECK ADD  CONSTRAINT [FK_ProgramacionMantenimiento_TipoMantenimiento] FOREIGN KEY([id_tipo_mantenimiento])
REFERENCES [dbo].[Tipos_Mantenimiento] ([id_tipo])
GO
ALTER TABLE [dbo].[Programaciones_Mantenimiento] CHECK CONSTRAINT [FK_ProgramacionMantenimiento_TipoMantenimiento]
GO
ALTER TABLE [dbo].[Programaciones_Mantenimiento]  WITH CHECK ADD  CONSTRAINT [FK_ProgramacionMantenimiento_UsuarioCreacion] FOREIGN KEY([usuario_creacion])
REFERENCES [dbo].[Usuarios] ([id])
GO
ALTER TABLE [dbo].[Programaciones_Mantenimiento] CHECK CONSTRAINT [FK_ProgramacionMantenimiento_UsuarioCreacion]
GO
ALTER TABLE [dbo].[Programaciones_Mantenimiento]  WITH CHECK ADD  CONSTRAINT [FK_ProgramacionMantenimiento_UsuarioModificacion] FOREIGN KEY([usuario_modificacion])
REFERENCES [dbo].[Usuarios] ([id])
GO
ALTER TABLE [dbo].[Programaciones_Mantenimiento] CHECK CONSTRAINT [FK_ProgramacionMantenimiento_UsuarioModificacion]
GO
ALTER TABLE [dbo].[Proveedores]  WITH CHECK ADD FOREIGN KEY([usuario_creacion])
REFERENCES [dbo].[Usuarios] ([id])
GO
ALTER TABLE [dbo].[Proveedores]  WITH CHECK ADD FOREIGN KEY([usuario_modificacion])
REFERENCES [dbo].[Usuarios] ([id])
GO
ALTER TABLE [dbo].[Tickets]  WITH CHECK ADD FOREIGN KEY([equipo_id])
REFERENCES [dbo].[Equipos] ([id_equipo])
GO
ALTER TABLE [dbo].[Tickets]  WITH CHECK ADD FOREIGN KEY([usuario_creador_id])
REFERENCES [dbo].[Usuarios] ([id])
GO
ALTER TABLE [dbo].[Tickets]  WITH CHECK ADD FOREIGN KEY([usuario_asignado_id])
REFERENCES [dbo].[Usuarios] ([id])
GO
ALTER TABLE [dbo].[Tickets]  WITH CHECK ADD FOREIGN KEY([usuario_creacion])
REFERENCES [dbo].[Usuarios] ([id])
GO
ALTER TABLE [dbo].[Tickets]  WITH CHECK ADD FOREIGN KEY([usuario_modificacion])
REFERENCES [dbo].[Usuarios] ([id])
GO
ALTER TABLE [dbo].[Tipos_Mantenimiento]  WITH CHECK ADD FOREIGN KEY([usuario_creacion])
REFERENCES [dbo].[Usuarios] ([id])
GO
ALTER TABLE [dbo].[Tipos_Mantenimiento]  WITH CHECK ADD FOREIGN KEY([usuario_modificacion])
REFERENCES [dbo].[Usuarios] ([id])
GO
ALTER TABLE [dbo].[Equipos]  WITH CHECK ADD  CONSTRAINT [CK_Equipos_Estado] CHECK  (([estado]='Critico' OR [estado]='Inactivo' OR [estado]='Activo'))
GO
ALTER TABLE [dbo].[Equipos] CHECK CONSTRAINT [CK_Equipos_Estado]
GO
ALTER TABLE [dbo].[Historial_Programacion]  WITH CHECK ADD  CONSTRAINT [CK_HistorialProg_TipoEvento] CHECK  (([tipo_evento]='EJECUTADO' OR [tipo_evento]='SALTADO' OR [tipo_evento]='REPROGRAMADO' OR [tipo_evento]='ACTIVADO' OR [tipo_evento]='PAUSADO' OR [tipo_evento]='EDITADO' OR [tipo_evento]='CREADO'))
GO
ALTER TABLE [dbo].[Historial_Programacion] CHECK CONSTRAINT [CK_HistorialProg_TipoEvento]
GO
ALTER TABLE [dbo].[Programaciones_Mantenimiento]  WITH CHECK ADD CHECK  (([dias_alerta_previa]>=(0)))
GO
ALTER TABLE [dbo].[Programaciones_Mantenimiento]  WITH CHECK ADD  CONSTRAINT [CK_Programacion_Frecuencia] CHECK  (([frecuencia_dias]>=(0)))
GO
ALTER TABLE [dbo].[Programaciones_Mantenimiento] CHECK CONSTRAINT [CK_Programacion_Frecuencia]
GO
ALTER TABLE [dbo].[Tickets]  WITH CHECK ADD CHECK  (([estado]='Cerrado' OR [estado]='Resuelto' OR [estado]='En Proceso' OR [estado]='Asignado' OR [estado]='Abierto'))
GO
ALTER TABLE [dbo].[Tickets]  WITH CHECK ADD CHECK  (([prioridad]='Crítica' OR [prioridad]='Alta' OR [prioridad]='Media' OR [prioridad]='Baja'))
GO
SET QUOTED_IDENTIFIER ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET QUOTED_IDENTIFIER ON
GO

