# Especificación de Diseño: Migración de Documentación a Mintlify

**Fecha:** 2026-09-30  
**Ubicación:** `docs/`  
**Tecnología:** [Mintlify](https://mintlify.com) (`docs.json`, MDX, Mintlify CLI)  
**Estado:** Aprobado en diseño  

---

## 1. Visión General y Objetivos

Esta especificación detalla la migración completa del subsistema de documentación de Cobbleloots desde **MkDocs Material** (Python/GitHub Pages) hacia **Mintlify** (MDX/Mintlify Cloud).

### Objetivos Clave
1. **Modernización Visual y Experiencia de Usuario:** Migrar de páginas estáticas de MkDocs a una plataforma interactiva basada en MDX con componentes nativos (`<CardGroup>`, `<Tabs>`, `<Note>`, `<CodeGroup>`).
2. **Soporte Multiversión / Multicanal (Estilo Godot):** Implementar un selector desplegable de versiones en la interfaz (`navigation.versions`) que permita a los jugadores alternar entre el canal **Estable (Release)** y el canal **Alpha (Latest)** apuntando a sus respectivos subdominios de despliegue.
3. **Aislamiento en `docs/`:** Mantener toda la configuración de Mintlify (`docs.json`), assets y archivos `.mdx` estrictamente dentro del subdirectorio `docs/`, preservando limpia la raíz del proyecto para el mod de Minecraft.
4. **Automatización de CI/CD:** Sustituir el build de Python y el despliegue manual a GitHub Pages en `.github/workflows/ci.yml` por validaciones automáticas con Node.js (`mint validate` y `mint broken-links`), delegando el hosting continuo en la GitHub App oficial de Mintlify.
5. **Limpieza y Cero Residuos:** Eliminar por completo `mkdocs.yml`, dependencias de Python y extensiones obsoletas de Python-Markdown (`markdown="span"`, directivas de ancho y sintaxis de Twemoji).

---

## 2. Arquitectura de Mintlify y Configuración (`docs/docs.json`)

Se utiliza el estándar moderno de Mintlify: `docs/docs.json` (el archivo obsoleto `mint.json` no será utilizado).

### 2.1. Identidad Visual y Paleta
- **Nombre del sitio:** `Cobbleloots`
- **Theme:** `mint`
- **Colores:**
  - `primary`: `#6366F1` (Índigo Cobblemon/Cobbleloots)
  - `light`: `#4F46E5`
  - `dark`: `#818CF8`
- **Logo:** `/assets/logo.png`
- **Favicon:** `/assets/ball/model/poke.png`
- **Navbar Links:**
  - Discord: `https://discord.gg/kbykWUH5dV`
  - Modrinth: `https://modrinth.com/mod/cobbleloots`
  - CurseForge: `https://www.curseforge.com/minecraft/mc-mods/cobbleloots`
  - GitHub: `https://github.com/ResistorCat/cobbleloots`

### 2.2. Selector de Versiones y Canales
El selector desplegable de versiones se ubica en el menú lateral y permite cambiar entre ramas de despliegue sin duplicar carpetas en Git:
```json
{
  "navigation": {
    "versions": [
      {
        "version": "Stable (2.4.x)",
        "href": "https://docs.cobbleloots.com",
        "default": true
      },
      {
        "version": "Alpha (2.5.x)",
        "href": "https://alpha.docs.cobbleloots.com"
      }
    ]
  }
}
```

### 2.3. Estructura Jerárquica de Navegación
```json
{
  "$schema": "https://mintlify.com/docs.json",
  "name": "Cobbleloots",
  "theme": "mint",
  "colors": {
    "primary": "#6366F1",
    "light": "#4F46E5",
    "dark": "#818CF8"
  },
  "logo": {
    "light": "/assets/logo.png",
    "dark": "/assets/logo.png"
  },
  "favicon": "/assets/ball/model/poke.png",
  "navbar": {
    "links": [
      { "type": "github", "href": "https://github.com/ResistorCat/cobbleloots" },
      { "type": "discord", "href": "https://discord.gg/kbykWUH5dV" }
    ]
  },
  "navigation": {
    "versions": [
      {
        "version": "Stable (2.4.x)",
        "href": "https://docs.cobbleloots.com",
        "default": true
      },
      {
        "version": "Alpha (2.5.x)",
        "href": "https://alpha.docs.cobbleloots.com"
      }
    ],
    "tabs": [
      {
        "tab": "Documentation",
        "groups": [
          {
            "group": "Overview",
            "pages": ["index"]
          },
          {
            "group": "Loot Balls",
            "pages": [
              "loot_balls/index",
              {
                "group": "Tier 1 — Common",
                "pages": [
                  "loot_balls/tier-1-common/poke",
                  "loot_balls/tier-1-common/citrine",
                  "loot_balls/tier-1-common/verdant",
                  "loot_balls/tier-1-common/azure",
                  "loot_balls/tier-1-common/roseate",
                  "loot_balls/tier-1-common/slate",
                  "loot_balls/tier-1-common/premier"
                ]
              },
              {
                "group": "Tier 2 — Uncommon",
                "pages": [
                  "loot_balls/tier-2-uncommon/great",
                  "loot_balls/tier-2-uncommon/dive",
                  "loot_balls/tier-2-uncommon/heal",
                  "loot_balls/tier-2-uncommon/lure",
                  "loot_balls/tier-2-uncommon/nest",
                  "loot_balls/tier-2-uncommon/net",
                  "loot_balls/tier-2-uncommon/pumpkin",
                  "loot_balls/tier-2-uncommon/quick",
                  "loot_balls/tier-2-uncommon/rainbow",
                  "loot_balls/tier-2-uncommon/safari",
                  "loot_balls/tier-2-uncommon/timer"
                ]
              },
              {
                "group": "Tier 3 — Rare",
                "pages": [
                  "loot_balls/tier-3-rare/ultra",
                  "loot_balls/tier-3-rare/dusk",
                  "loot_balls/tier-3-rare/luxury"
                ]
              },
              {
                "group": "Tier 4 — Ultra Rare",
                "pages": [
                  "loot_balls/tier-4-ultra-rare/master"
                ]
              }
            ]
          },
          {
            "group": "Guides",
            "pages": [
              "guides/configuration",
              "guides/how-to/creative",
              "guides/how-to/datapack"
            ]
          },
          {
            "group": "Reference",
            "pages": [
              "reference/commands",
              "reference/biome_tags"
            ]
          }
        ]
      }
    ]
  }
}
```

---

## 3. Transformación de Contenidos a MDX

Todos los archivos `.md` de `docs/` se renombran y convierten a `.mdx`:

### 3.1. Frontmatter Obligatorio
Cada archivo `.mdx` debe comenzar con metadatos YAML compatibles:
```yaml
---
title: "Título Descriptivo"
description: "Resumen breve para SEO y navegación."
icon: "circle-dot" # Opcional: icono de Lucide o FontAwesome
---
```

### 3.2. Mapeo de Componentes y Sintaxis
1. **Admonitions:**
   - `!!! note "Título"` $\rightarrow$ `<Note>**Título**: ...</Note>`
   - `!!! tip` $\rightarrow$ `<Tip>...</Tip>`
   - `!!! warning` $\rightarrow$ `<Warning>...</Warning>`
2. **Tabs de loaders:**
   - `=== "Fabric"` y `=== "NeoForge"` $\rightarrow$ `<Tabs><Tab title="Fabric">...</Tab><Tab title="NeoForge">...</Tab></Tabs>`.
3. **Página de inicio (`docs/index.mdx`):**
   - Se reemplazan listas planas por un grid interactivo con `<CardGroup cols={2}>` y componentes `<Card>`.
4. **Fichas técnicas y tablas de Loot Balls (22 páginas):**
   - Eliminación de `markdown="span"`, directivas de tamaño `{ width="..." }` e iconos de Material `:material-pokeball:`.
   - Normalización de rutas de imágenes relativas a rutas raíz (`/assets/...`).
   - Renderizado nítido de pixel-art aplicando `style={{ imageRendering: "pixelated" }}` o clases CSS equivalentes.
5. **Enlaces internos:**
   - Rutas relativas a la raíz sin extensión de archivo (ej. `[Comandos](/reference/commands)`).

---

## 4. Pipeline de CI/CD en GitHub Actions

Se actualiza `.github/workflows/ci.yml` para validar la documentación en Node.js 22:

```yaml
name: Documentation CI

on:
  push:
    branches: [main, master, beta, alpha]
    paths:
      - "docs/**"
      - ".github/workflows/ci.yml"
  pull_request:
    branches: [main, master, beta, alpha]
    paths:
      - "docs/**"
      - ".github/workflows/ci.yml"
  workflow_dispatch:

jobs:
  validate:
    name: Validate Mintlify Docs
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: docs
    steps:
      - name: Checkout Repository
        uses: actions/checkout@v4

      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: 22

      - name: Install Mintlify CLI
        run: npm install -g mint

      - name: Validate Docs Schema & Syntax
        run: mint validate

      - name: Check Internal Links
        run: mint broken-links
```

- Se elimina el archivo `mkdocs.yml` de la raíz del repositorio.
- Se elimina el paso de despliegue a GitHub Pages (`mkdocs gh-deploy`), ya que Mintlify Cloud despliega automáticamente.

---

## 5. Estrategia de Migración y Verificación

1. **Scaffolding:** Crear `docs/docs.json` con la configuración validada.
2. **Script de Utilidad Interno (`scratch/convert-docs.mjs`):** Script temporal para procesar en lote las 22 bolas de loot:
   - Renombrar `.md` a `.mdx`.
   - Limpiar `markdown="span"`, tags de iconos y directivas `{ width="..." }`.
   - Convertir admonitions `!!! note` a componentes `<Note>`.
   - Normalizar rutas de assets a `/assets/...`.
3. **Conversión Manual / Refinado de Páginas Clave:**
   - `docs/index.mdx`: Implementar `<CardGroup>`.
   - `docs/guides/*.mdx`: Convertir tabs de Fabric/NeoForge a `<Tabs>`.
   - `docs/reference/*.mdx`: Convertir callouts y tablas de comandos.
4. **Verificación Estricta:**
   - Ejecutar `npx mint validate` dentro de `docs/`.
   - Ejecutar `npx mint broken-links` dentro de `docs/`.
   - Probar hot-reloading local con `npx mint dev`.
5. **Limpieza:**
   - `git rm mkdocs.yml`.
   - Actualizar `.github/workflows/ci.yml`.
