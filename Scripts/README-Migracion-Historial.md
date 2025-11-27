# 📋 Migración: Sistema de Historial Simplificado

## 🎯 Objetivo

Transformar el sistema de bitácora de equipos de un modelo **detallado** (registra cada campo modificado) a un modelo **simplificado** (registra solo acciones importantes) con trazabilidad de usuarios.

---

## ❌ Problema Anterior

```
Historial actual:
- Campo 'nombre' cambió de 'A' a 'B'
- Campo 'marca' cambió de 'X' a 'Y'
- Campo 'modelo' cambió de '123' a '456'
- Campo 'ubicacion' cambió de 'Lab 1' a 'Lab 2'
```

**Problemas:**
- ❌ Genera mucho ruido en el historial
- ❌ Difícil de leer para el usuario
- ❌ No identifica quién hizo el cambio
- ❌ Registros innecesarios para cambios menores

---

## ✅ Solución Nueva

```
Historial nuevo:
- EDICION_GENERAL - "Información del equipo actualizada" (Juan Pérez - 2025-11-27)
```

**Ventajas:**
- ✅ Más limpio y legible
- ✅ Identifica quién y cuándo
- ✅ Solo registra cambios importantes
- ✅ Mejor experiencia de usuario

---

## 📊 Tipos de Cambio

| Tipo | Descripción | Cuándo se registra |
|------|-------------|-------------------|
| `CREACION` | Equipo registrado | Al crear nuevo equipo |
| `EDICION_GENERAL` | Información actualizada | Al editar campos generales |
| `CAMBIO_IMAGEN` | Fotografía actualizada | Al cambiar la imagen del equipo |
| `CAMBIO_UBICACION` | Ubicación modificada | Al cambiar ubicación física |
| `CAMBIO_ESTADO` | Estado operativo modificado | Al cambiar estado (Operativo/Fuera de Servicio) |
| `MANTENIMIENTO` | Mantenimiento realizado | Al completar mantenimiento |
| `CALIBRACION` | Calibración realizada | Al completar calibración |
| `REPARACION` | Reparación realizada | Al completar reparación |

---

## 🗄️ Cambios en Base de Datos

### Campos Agregados:

```sql
ALTER TABLE Historial_Equipo ADD tipo_cambio VARCHAR(50);
ALTER TABLE Historial_Equipo ADD usuario_id INT;
ALTER TABLE Historial_Equipo ADD usuario_nombre VARCHAR(100);
```

### Estructura Final:

```sql
CREATE TABLE Historial_Equipo (
    id_historial INT PRIMARY KEY IDENTITY(1,1),
    id_equipo INT,
    tipo_cambio VARCHAR(50),              -- ✨ NUEVO
    descripcion NVARCHAR(MAX),
    usuario_id INT,                       -- ✨ NUEVO
    usuario_nombre VARCHAR(100),          -- ✨ NUEVO
    fecha_registro DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (id_equipo) REFERENCES Equipos(id_equipo),
    FOREIGN KEY (usuario_id) REFERENCES Usuarios(id)  -- ✨ NUEVO
);
```

---

## 🚀 Instrucciones de Migración

### Paso 1: Ejecutar Script SQL

```bash
# En SQL Server Management Studio o Azure Data Studio
1. Abrir: Migracion-Historial-Simplificado.sql
2. Conectar a la base de datos INACIF_Mantenimientos
3. Ejecutar el script completo (F5)
```

El script:
- ✅ Agrega las nuevas columnas
- ✅ Crea la foreign key
- ✅ Actualiza registros existentes
- ✅ Crea índices para rendimiento
- ✅ Muestra la estructura final

### Paso 2: Desplegar Backend

```bash
# En el proyecto MantenimientosBackend
mvn clean package
docker-compose up --build -d
```

Los cambios en `EquipoController.java`:
- ✅ Método `create()` registra CREACION
- ✅ Método `update()` detecta tipo de cambio
- ✅ Nuevo método `registrarHistorialSimplificado()`

### Paso 3: Verificar

1. **Crear un equipo nuevo:**
   - Debe aparecer: `CREACION - "Equipo XXX registrado en el sistema"`

2. **Editar información general:**
   - Debe aparecer: `EDICION_GENERAL - "Información del equipo actualizada"`

3. **Cambiar imagen:**
   - Debe aparecer: `CAMBIO_IMAGEN - "Fotografía del equipo actualizada"`

4. **Cambiar ubicación:**
   - Debe aparecer: `CAMBIO_UBICACION - "Ubicación cambiada de X a Y"`

---

## 🔄 Migración de Datos Existentes

### Opción 1: Mantener Historial Antiguo
El script marca todos los registros existentes como `EDICION_GENERAL` con usuario `Sistema (histórico)`.

### Opción 2: Limpiar Historial Detallado (Opcional)
Descomentar esta sección en el script:

```sql
DELETE FROM Historial_Equipo 
WHERE descripcion LIKE 'Campo%'
   OR descripcion LIKE 'Se cambió%de%a%';
```

---

## 📈 Mejoras Futuras

1. **Integración con Keycloak:**
   ```java
   // Obtener usuario autenticado
   String usuarioNombre = keycloakContext.getPreferredUsername();
   Integer usuarioId = obtenerUsuarioIdDesdeKeycloak();
   ```

2. **Más Tipos de Cambio:**
   - `BAJA_EQUIPO` - Equipo dado de baja
   - `TRANSFERENCIA` - Equipo transferido a otro laboratorio
   - `GARANTIA` - Equipo en garantía

3. **Dashboard de Auditoría:**
   - Vista de quién modificó qué
   - Reporte de actividad por usuario
   - Estadísticas de cambios

---

## 🧪 Pruebas

```sql
-- Ver historial de un equipo
SELECT 
    tipo_cambio,
    descripcion,
    usuario_nombre,
    fecha_registro
FROM Historial_Equipo
WHERE id_equipo = 1
ORDER BY fecha_registro DESC;

-- Cambios por tipo
SELECT 
    tipo_cambio,
    COUNT(*) as total
FROM Historial_Equipo
GROUP BY tipo_cambio
ORDER BY total DESC;

-- Actividad por usuario
SELECT 
    usuario_nombre,
    COUNT(*) as total_cambios
FROM Historial_Equipo
WHERE usuario_id IS NOT NULL
GROUP BY usuario_nombre
ORDER BY total_cambios DESC;
```

---

## 📞 Soporte

Si tienes problemas con la migración, revisa:
1. ✅ La base de datos está conectada
2. ✅ Tienes permisos para ALTER TABLE
3. ✅ El backend se compiló sin errores
4. ✅ Los logs del backend no muestran errores

**Logs importantes:**
- `📝 Historial registrado: [TIPO]`
- `✅ Equipo actualizado correctamente`
- `⚠️ Error al registrar historial` (no crítico)

---

## ✅ Checklist de Migración

- [ ] Script SQL ejecutado sin errores
- [ ] Columnas nuevas verificadas en BD
- [ ] Foreign key creada correctamente
- [ ] Índices creados
- [ ] Backend desplegado
- [ ] Prueba: Crear equipo nuevo
- [ ] Prueba: Editar equipo existente
- [ ] Prueba: Cambiar imagen
- [ ] Prueba: Ver historial en frontend
- [ ] Documentación actualizada

---

**Fecha de implementación:** 2025-11-27  
**Versión:** 1.0  
**Autor:** Sistema de Mantenimientos INACIF
