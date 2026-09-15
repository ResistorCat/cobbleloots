# Plantilla Recomendada para Tickets de Linear (Agent-Ready)

Utiliza esta estructura al crear tickets en Linear para maximizar la tasa de éxito de la Software Factory y los agentes autónomos.
Utiliza esta estructura al crear tickets en Linear para maximizar la tasa de éxito al resolverlos con asistentes de IA o desarrollo en equipo.

---

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

