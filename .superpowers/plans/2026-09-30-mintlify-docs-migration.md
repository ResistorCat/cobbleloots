# Migración de Documentación a Mintlify Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Migrar el subsistema de documentación de Cobbleloots desde MkDocs Material (Python/GitHub Pages) hacia Mintlify (MDX/Mintlify Cloud) con selector de versiones integrado (`Stable` vs `Alpha`), validación en CI con Node.js 22, y eliminación de `mkdocs.yml`.

**Architecture:** Todos los archivos de documentación y configuración residen exclusivamente en `docs/` con `docs/docs.json`. Los contenidos se transforman a `.mdx` con componentes nativos (`<CardGroup>`, `<Note>`, `<Tabs>`, `<Tip>`). La validación se ejecuta en `.github/workflows/ci.yml` con `mint validate` y `mint broken-links`, delegando el hosting continuo a la GitHub App oficial de Mintlify.

**Tech Stack:** Mintlify (`mint` CLI), MDX, Node.js 22, GitHub Actions.

**Spec:** [`.superpowers/specs/2026-09-30-mintlify-docs-migration-design.md`](file:///c:/Users/franc/GitHub/cobbleloots/.superpowers/specs/2026-09-30-mintlify-docs-migration-design.md)

## Global Constraints
- Toda la documentación debe vivir estrictamente dentro de `docs/`.
- Usar `docs.json` (el estándar moderno de Mintlify; nunca usar `mint.json`).
- Normalizar todos los enlaces internos a rutas raíz relativas sin extensión (ej: `[Comandos](/reference/commands)`).
- No modificar el código de Java/Kotlin del mod ni el archivo `CHANGELOG.md`.
- No alterar formatos ni ubicaciones fuera de `docs/` y `.github/workflows/ci.yml`.

---

### Task 1: Scaffolding de Configuración (`docs/docs.json`) y Estructura Base

**Files:**
- Create: `docs/docs.json`
- Test: `cd docs && npx mint validate`

**Interfaces:**
- Produces: Configuración completa de Mintlify (`docs.json`) con esquema `$schema`, metadatos de sitio, colores, logo, favicon, enlaces de navbar y navegación jerárquica con selector multiversión (`navigation.versions`).

- [ ] **Step 1: Crear `docs/docs.json`**
  Escribir la configuración con `$schema: "https://mintlify.com/docs.json"`, colores (`primary: #6366F1`, `light: #4F46E5`, `dark: #818CF8`), logo, favicon, versiones (`Stable (2.4.x)` y `Alpha (2.5.x)`), y árbol de navegación para Overview, Loot Balls, Guides y Reference.
- [ ] **Step 2: Ejecutar validación inicial**
  Run: `cd docs && npx mint validate`
  Expected: Advertencias sobre archivos `.md` que deben ser `.mdx` o faltantes en el árbol.
- [ ] **Step 3: Commit inicial de configuración**
  ```bash
  git add docs/docs.json
  git commit -m "chore(docs): scaffold mintlify configuration docs.json with multiversion switcher"
  ```

---

### Task 2: Transformación de Páginas a MDX (Loot Balls, Guías y Referencias)

**Files:**
- Create: `docs/index.mdx` (reemplaza `docs/index.md`)
- Create: `docs/loot_balls/**/*.mdx` (22 archivos de loot balls + `docs/loot_balls/index.mdx`)
- Create: `docs/guides/**/*.mdx` (`configuration.mdx`, `how-to/creative.mdx`, `how-to/datapack.mdx`)
- Create: `docs/reference/**/*.mdx` (`commands.mdx`, `biome_tags.mdx`)
- Delete: archivos `.md` originales en `docs/`
- Test: `cd docs && npx mint validate`

**Interfaces:**
- Produces: Todos los documentos convertidos a MDX con frontmatter YAML válido (`title`, `description`), componentes nativos `<Note>`, `<Tip>`, `<Warning>`, `<Tabs>`, `<Tab>`, `<CardGroup>`, y sin sintaxis obsoleta de Python-Markdown (`markdown="span"`, directivas de imagen).

- [ ] **Step 1: Crear script de migración temporal**
  Crear script auxiliar en `scratch/convert-docs.mjs` que:
  - Renombre `.md` a `.mdx`.
  - Convierta `!!! note "Título"` a `<Note>**Título**: ...</Note>` y `!!! tip` a `<Tip>...</Tip>`.
  - Elimine `markdown="span"` y directivas de Python-Markdown `{ width="..." }`.
  - Normalice las rutas de imágenes relativas `../../assets/...` a `/assets/...`.
  - Asegure frontmatter con `title` y `description`.
- [ ] **Step 2: Ejecutar script de conversión por lote**
  Run: `node scratch/convert-docs.mjs`
  Expected: Archivos `.mdx` generados y archivos `.md` antiguos eliminados.
- [ ] **Step 3: Refinar manualmente `docs/index.mdx`**
  Implementar el layout interactivo con `<CardGroup cols={2}>` y componentes `<Card>` enlazando a `/loot_balls`, `/reference/commands`, `/guides/how-to/creative` y `/guides/how-to/datapack`.
- [ ] **Step 4: Refinar `docs/guides/how-to/creative.mdx` y `datapack.mdx`**
  Convertir tabs de loaders (`=== "Fabric"`, `=== "NeoForge"`) a `<Tabs><Tab title="...">...</Tab></Tabs>`.
- [ ] **Step 5: Ejecutar validación de Mintlify**
  Run: `cd docs && npx mint validate`
  Expected: PASS (0 errores en archivos MDX).
- [ ] **Step 6: Commit de páginas MDX**
  ```bash
  git add docs/
  git commit -m "feat(docs): convert documentation pages to mintlify mdx components"
  ```

---

### Task 3: Verificación de Enlaces Internos y Normalización

**Files:**
- Modify: `docs/**/*.mdx` (enlaces internos que aún tengan `.md` o rutas relativas rotas)
- Test: `cd docs && npx mint broken-links`

**Interfaces:**
- Produces: 100% de enlaces internos limpios, relativos a la raíz, sin extensión de archivo (requisito estricto de Mintlify).

- [ ] **Step 1: Ejecutar detector de enlaces rotos**
  Run: `cd docs && npx mint broken-links`
  Expected: Identificar cualquier enlace roto o con extensión `.md`.
- [ ] **Step 2: Corregir enlaces detectados**
  Asegurar que todos los enlaces apunten a `/loot_balls/tier-1-common/poke`, `/reference/commands`, etc.
- [ ] **Step 3: Re-ejecutar detector de enlaces rotos**
  Run: `cd docs && npx mint broken-links`
  Expected: 0 broken links.
- [ ] **Step 4: Commit**
  ```bash
  git add docs/
  git commit -m "fix(docs): resolve and normalize all internal links for mintlify"
  ```

---

### Task 4: Pipeline de CI/CD en GitHub Actions y Limpieza de MkDocs

**Files:**
- Modify: `.github/workflows/ci.yml`
- Delete: `mkdocs.yml`
- Test: `git status` y validación de sintaxis de CI

**Interfaces:**
- Produces: Workflow de GitHub Actions actualizado que ejecuta `mint validate` y `mint broken-links` en Node 22 solo cuando se modifican archivos en `docs/**`.

- [ ] **Step 1: Actualizar `.github/workflows/ci.yml`**
  Reemplazar los pasos de Python y `mkdocs build / gh-deploy` por Node.js 22, instalación de `mint` CLI, `mint validate` y `mint broken-links` con filtro de rutas `paths: ["docs/**", ".github/workflows/ci.yml"]`.
- [ ] **Step 2: Eliminar `mkdocs.yml`**
  Run: `git rm mkdocs.yml`
- [ ] **Step 3: Verificar que el árbol de trabajo esté limpio**
  Run: `cd docs && npx mint validate && npx mint broken-links`
  Expected: Todo verde, validación limpia.
- [ ] **Step 4: Commit**
  ```bash
  git add .github/workflows/ci.yml mkdocs.yml
  git commit -m "ci(docs): replace mkdocs with mintlify validation in github actions"
  ```

---

## Plan Self-Review
- **Spec Coverage:**
  - `docs.json` con versions: Cubierto en Task 1.
  - Transformación MDX (loot balls, guides, index con CardGroup): Cubierto en Task 2.
  - Normalización de enlaces internos: Cubierto en Task 3.
  - Actualización de CI y eliminación de MkDocs: Cubierto en Task 4.
- **No Placeholders:** Verificado.
- **Type Consistency:** Todas las rutas siguen el estándar root-relative de Mintlify.
