<!--
PLANTILLA OBLIGATORIA DE PULL REQUEST PARA AGENTES
El título del PR DEBE seguir estrictamente el formato:
[<linear-issue-id>] <type>(<scope>): <description>
Ejemplo: [DEV-5] feat(commands): loot balls reset command
NOTA: El título del PR y su <description> DEBEN estar siempre en INGLÉS (nunca en español).
-->

### ¿Qué se hizo?
<!--
Descripción breve en lenguaje natural de no más de 3 párrafos sobre el trabajo que se hizo.
Párrafos cortos de no más de 3 líneas cada uno.
Se permite usar bullet points para explicar lo hecho, en especial si es una narración secuencial.
-->

### ¿Cómo se ve?
<!--
Descripción de lo que debería ver distinto o nuevo quien revise esta PR al probarlo localmente.
Si son cambios técnicos sin artefacto visual, mostrar lo más relevante del código u otros artefactos no-visuales producidos por los cambios.
-->

### ¿Cómo testearlo?
<!--
RUNBOOK DETALLADO PASO A PASO para reproducir los cambios de manera local y hacerle QA de producto a la PR.
Esta es la sección más importante del cuerpo de la PR y la que definirá si esta se aprueba o no.
-->
1. Iniciar cliente con `./gradlew :fabric:runClient` o `./gradlew :neoforge:runClient`.
2. ...
3. ...

### Notas adicionales
<!--
Bullet points con cualquier detalle de suma importancia que deba saber el reviewer sobre los cambios antes de revisar la PR.
Incluir supuestos de diseño tomados no especificados en el ticket, y qué plataformas afectan los cambios.
-->
- **Supuestos de diseño**:
  - ...
- **Plataformas afectadas**:
  - [ ] Common
  - [ ] Fabric
  - [ ] NeoForge
- **Changelog**:
  - [ ] Fragmento creado en inglés en `.changelog/<linear-issue-id>.md` (o N/A si no afecta a jugadores/admins)
- **Documentación**:
  - [ ] Actualizada en `docs/` y `MODINFO.md` (o N/A si es refactor/tooling interno)
- **Detalles para el reviewer**:
  - ...

