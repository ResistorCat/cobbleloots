# Plantilla para Tickets de Linear

Utiliza esta estructura al crear tickets en Linear. Debe ser legible por cualquier persona, sin necesidad de contexto técnico ni del proyecto.

---

## 1. Convención de Título
El título del ticket **DEBE** seguir esta estructura, en **español**:
```text
[<REPO>] <título>
```
- **`<REPO>`**: Nombre del repositorio / proyecto (por ejemplo `[Cobbleloots]`).
- **`<título>`**: Resumen claro y conciso de la tarea.

*Ejemplo:* `[Cobbleloots] Registrar Tiny Mushroom como objeto valioso`

## 2. Convención de Etiquetas (Tags)
Asignar la etiqueta correspondiente en Linear según la naturaleza del trabajo:
- `Feature`: Nueva funcionalidad o capacidad incorporada.
- `Bug`: Corrección de errores, crashes o comportamientos inesperados.
- `Improvement`: Optimización, refactorización o mejoras de infraestructura/CI.

---

## 3. Estructura del Cuerpo del Ticket

```markdown
**Por qué**: <Explicación breve, máximo 2 líneas, de por qué existe esta issue.>

**Cómo**: <Explicación breve, máximo 3 líneas, de cómo se piensa resolver, para alguien sin contexto técnico ni del proyecto.>

**Restricciones**:

- <Restricción 1>
- <Restricción 2>

**Herramientas**: <Herramientas, repositorios y tecnologías que se utilizarán.>
```

*Ejemplo:*

```markdown
**Por qué**: El Tiny Mushroom aún no cuenta como objeto valioso, así que no aparece entre los botines raros.

**Cómo**: Se agrega el objeto a la lista de objetos valiosos del mod y se comprueba en el juego que aparezca.

**Restricciones**:

- Mantener compatibilidad con Cobblemon 1.7.3+1.21.1.
- Funcionar en Fabric y NeoForge.

**Herramientas**: Repo cobbleloots, Gradle, datapacks de Cobbleloots.
```
