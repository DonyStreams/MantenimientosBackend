# 📧 Ejemplos de Correos de Notificación

Este documento muestra ejemplos visuales de los correos electrónicos que se envían automáticamente.

---

## 🚨 Ejemplo 1: Notificación de Ticket Crítico

**Asunto:** 🚨 TICKET CRÍTICO #45 - Espectrofotómetro UV-Visible

**Destinatarios:** admin@inacif.gob.gt, jefatura@inacif.gob.gt

**Contenido:**

```
┌─────────────────────────────────────────────────────┐
│            🚨 ALERTA DE TICKET CRÍTICO             │
│                                                     │
│  Se ha registrado un ticket con PRIORIDAD CRÍTICA  │
│            que requiere atención inmediata:         │
└─────────────────────────────────────────────────────┘

Ticket ID: #45
─────────────────────────────────────────────────────

Equipo: Espectrofotómetro UV-Visible
─────────────────────────────────────────────────────

Código INACIF: LAB-UV-001
─────────────────────────────────────────────────────

Ubicación: Laboratorio de Química - Edificio A, Piso 2
─────────────────────────────────────────────────────

Usuario Asignado: Juan Pérez González
─────────────────────────────────────────────────────

Descripción:
El equipo presenta error crítico en la calibración automática.
No se pueden realizar análisis. Se requiere revisión urgente
del módulo de lámpara y sistema óptico.

─────────────────────────────────────────────────────
Este es un mensaje automático del Sistema de 
Mantenimientos INACIF. Por favor, no responda 
a este correo.
```

---

## ⚠️ Ejemplo 2: Notificación de Equipo en Estado Crítico

**Asunto:** ⚠️ EQUIPO EN ESTADO CRÍTICO - Cromatógrafo de Gases (LAB-CG-003)

**Destinatarios:** admin@inacif.gob.gt, jefatura@inacif.gob.gt

**Contenido:**

```
┌─────────────────────────────────────────────────────┐
│           ⚠️ EQUIPO EN ESTADO CRÍTICO              │
│                                                     │
│   Un equipo ha cambiado a ESTADO CRÍTICO y        │
│            requiere revisión urgente                │
└─────────────────────────────────────────────────────┘

Equipo ID: #23
─────────────────────────────────────────────────────

Nombre: Cromatógrafo de Gases GC-2030
─────────────────────────────────────────────────────

Código INACIF: LAB-CG-003
─────────────────────────────────────────────────────

Ubicación: Laboratorio de Toxicología - Edificio B, Piso 1
─────────────────────────────────────────────────────

Estado Anterior: Activo
─────────────────────────────────────────────────────

Estado Nuevo: CRÍTICO
─────────────────────────────────────────────────────

Motivo del cambio:
Estado cambiado de 'Activo' a 'Crítico' por 
María López Rodríguez

─────────────────────────────────────────────────────
Este es un mensaje automático del Sistema de 
Mantenimientos INACIF. Por favor, no responda 
a este correo.
```

---

## 🎨 Vista HTML Real

Los correos se envían en formato HTML con estilos profesionales:

### Características visuales:
- **Encabezado con color de alerta** (rojo para tickets críticos, naranja para equipos críticos)
- **Información organizada en filas** con etiquetas claras
- **Tipografía legible** (Arial, sans-serif)
- **Diseño responsive** adaptable a dispositivos móviles
- **Colores institucionales** profesionales
- **Footer informativo** con descargo de responsabilidad

### Estructura HTML:
```html
<!DOCTYPE html>
<html>
<head>
    <meta charset='UTF-8'>
    <style>
        /* Estilos para un correo profesional */
        body { font-family: Arial, sans-serif; }
        .header { background: #dc3545; color: white; }
        .content { background: #f9f9f9; padding: 20px; }
        .critical { color: #dc3545; font-weight: bold; }
    </style>
</head>
<body>
    <!-- Contenido del correo -->
</body>
</html>
```

---

## 📋 Información Incluida en las Notificaciones

### Notificación de Ticket Crítico
✅ ID del ticket  
✅ Equipo afectado  
✅ Código INACIF  
✅ Ubicación física  
✅ Usuario asignado  
✅ Descripción detallada del problema  

### Notificación de Equipo Crítico
✅ ID del equipo  
✅ Nombre del equipo  
✅ Código INACIF  
✅ Ubicación física  
✅ Estado anterior  
✅ Estado nuevo (Crítico)  
✅ Motivo del cambio de estado  

---

## 🔔 Flujo de Notificaciones

```
┌─────────────────────────┐
│  Usuario actualiza      │
│  Ticket a "Crítica"     │
│  o Equipo a "Critico"   │
└───────────┬─────────────┘
            │
            ▼
┌─────────────────────────┐
│  Sistema detecta el     │
│  cambio de estado       │
└───────────┬─────────────┘
            │
            ▼
┌─────────────────────────┐
│  EmailService consulta  │
│  información completa   │
└───────────┬─────────────┘
            │
            ▼
┌─────────────────────────┐
│  Genera correo HTML     │
│  con template           │
└───────────┬─────────────┘
            │
            ▼
┌─────────────────────────┐
│  Envía correo vía SMTP  │
│  a destinatarios        │
└───────────┬─────────────┘
            │
            ▼
┌─────────────────────────┐
│  Registra en logs       │
│  el resultado           │
└─────────────────────────┘
```

---

## ⚡ Tiempos de Entrega

Los correos se envían de forma **asíncrona** para no bloquear la operación del usuario:

- **Detección del cambio:** Inmediata (< 1 segundo)
- **Generación del correo:** < 1 segundo
- **Envío SMTP:** 2-5 segundos (depende del servidor)
- **Recepción:** 1-30 segundos (depende del proveedor de correo)

**Tiempo total estimado:** Menos de 1 minuto desde el cambio hasta la recepción.

---

## 📊 Casos de Uso

### Caso 1: Falla Crítica en Equipo de Análisis
```
1. Técnico detecta falla grave en equipo
2. Crea ticket con prioridad "Crítica"
3. Sistema envía correo a admin y jefatura
4. Jefatura recibe alerta en su teléfono
5. Se coordina atención inmediata
```

### Caso 2: Equipo Fuera de Servicio
```
1. Usuario cambia estado de equipo a "Critico"
2. Sistema envía correo a admin y jefatura
3. Se genera ticket automático si es necesario
4. Se planifica mantenimiento correctivo urgente
```

---

## 🎯 Beneficios del Sistema de Notificaciones

✅ **Respuesta rápida** a situaciones críticas  
✅ **Comunicación automática** sin intervención manual  
✅ **Trazabilidad completa** de alertas enviadas  
✅ **Reducción de tiempos de respuesta** ante fallas  
✅ **Mejora en la gestión** de equipos críticos  
✅ **Cumplimiento de protocolos** de calidad  

---

## 📧 Personalización

Para personalizar los templates de correo, edite el archivo:
```
src/main/java/usac/eps/servicios/mantenimientos/EmailService.java
```

Métodos a modificar:
- `generarHtmlTicketCritico()` - Template para tickets críticos
- `generarHtmlEquipoCritico()` - Template para equipos críticos

---

## 🔐 Seguridad y Privacidad

- ✅ Los correos **NO contienen información sensible** como contraseñas
- ✅ Solo se envían a **destinatarios autorizados** configurados
- ✅ La conexión SMTP usa **cifrado TLS/SSL**
- ✅ Las credenciales se almacenan en **archivo de configuración protegido**
- ✅ Los logs **no registran contraseñas** de correo

---

## 📞 Contacto

Para modificar destinatarios o agregar nuevas notificaciones, contacte al equipo de desarrollo.
