# Plantilla Recomendada para Tickets de Linear (Agent-Ready)

Utiliza esta estructura al crear tickets en Linear para maximizar la tasa de éxito al resolverlos con asistentes de IA o desarrollo en equipo.

---

## 1. Convención de Título
El título del ticket **DEBE** seguir estrictamente la siguiente estructura en **español**:
```text
[<REPO>] <título descriptivo en español>
```
- **`<REPO>`**: Nombre del repositorio / proyecto (por ejemplo `[Cobbleloots]`).
- **`<título>`**: Resumen claro y conciso de la tarea o mejora en español.

*Ejemplos:*
- `[Cobbleloots] Añadir MCP de Linear`
- `[Cobbleloots] Optimizar Gradle CI con filtro condicional de rutas para cambios que no son de código`
- `[Cobbleloots] Corregir renderizado del Loot Ball en Fabric`

## 2. Convención de Etiquetas (Tags)
Asignar la etiqueta correspondiente en Linear según la naturaleza del trabajo:
- `Feature`: Nueva funcionalidad o capacidad incorporada.
- `Bug`: Corrección de errores, crashes o comportamientos inesperados.
- `Improvement`: Optimización, refactorización o mejoras de infraestructura/CI.

---

## 3. Estructura del Cuerpo del Ticket

```markdown
## Contexto y Objetivo
<!-- Explica brevemente qué problema resuelve este ticket o qué nueva funcionalidad se desea incorporar en Cobbleloots. -->

## Criterios de Aceptación
<!-- Lista explícita de condiciones verificables para que la tarea se considere completa. -->
- [ ] El comando/mecánica ... funciona según lo especificado.
- [ ] El archivo de configuración o tabla de loot ... incluye las nuevas opciones.
- [ ] La solución funciona tanto en Fabric como en NeoForge (1.21.1).
- [ ] El proyecto compila limpiamente con `./gradlew build`.

## Áreas / Archivos Sugeridos (Opcional pero Recomendado)
- `common/src/main/java/dev/ripio/cobbleloots/...`
- `common/src/main/resources/data/cobbleloots/...`

## Restricciones y Notas Técnicas
- Mantener compatibilidad con Cobblemon 1.7.3+1.21.1.
- No modificar directamente el archivo `CHANGELOG.md`.
- No alterar formatos existentes de configuración sin incluir migración.
```
