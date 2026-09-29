# Especificación de Diseño: DEV-9 - Integrar Objetos Valiosos en Tablas de Loot y Pesca

**Fecha:** 2026-09-29  
**Ticket Linear:** [DEV-9](https://linear.app/ripiodev/issue/DEV-9/cobbleloots-integrar-objetos-valiosos-en-las-tablas-de-loot-de-loot)  
**Rama de Git:** `ripio/dev-9-cobbleloots-integrar-objetos-valiosos-en-las-tablas-de-loot`  
**Hito:** `Cobbleloots: Hidden Treasures Update`  
**Estado:** Aprobado para Planificación  

---

## 1. Contexto y Objetivos

En el entregable DEV-8 se registraron e implementaron los 10 objetos valiosos clásicos de Pokémon dentro de Cobbleloots (`nugget`, `big_nugget`, `pearl`, `big_pearl`, `pearl_string`, `stardust`, `star_piece`, `comet_shard`, `rare_bone`, `balm_mushroom`).

El objetivo de DEV-9 es distribuir estos tesoros de forma balanceada y data-driven en el mundo del juego:
1. Crear 4 subtablas de loot compartidas organizadas por rareza (`valuable_common`, `valuable_uncommon`, `valuable_rare`, `valuable_ultra_rare`).
2. Integrar estas subtablas con peso moderado (`weight: 2`, equivalente al peso de las vitaminas EV) en las Poké Balls de cada tier correspondiente.
3. Enriquecer temáticamente las Loot Balls acuáticas y de pesca (`lure.json` y `dive.json`) con drops directos de perlas y fósiles antiguos/huesos raros.
4. Actualizar el script de tooling de documentación (`scripts/docs.py`) para dar soporte a texturas del namespace `cobbleloots:` y regenerar `docs/loot_balls/`.
5. Registrar el fragmento de changelog (`.changelog/DEV-9.md`) y actualizar `MODINFO.md`.

---

## 2. Catálogo de Subtablas Compartidas (`loot_ball/shared/`)

Ubicación: `common/src/main/resources/data/cobbleloots/loot_table/loot_ball/shared/`

Todas las subtablas utilizan la estructura estándar de Minecraft con `rolls: 1` y entradas con pesos uniformes (`weight: 1`):

### 2.1 `valuable_common.json`
Contiene los objetos valiosos de nivel común:
```json
{
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "cobbleloots:stardust",
          "weight": 1,
          "functions": [
            {
              "function": "minecraft:set_count",
              "count": {
                "min": 1,
                "max": 3
              }
            }
          ]
        },
        {
          "type": "minecraft:item",
          "name": "cobbleloots:pearl",
          "weight": 1,
          "functions": [
            {
              "function": "minecraft:set_count",
              "count": {
                "min": 1,
                "max": 2
              }
            }
          ]
        },
        {
          "type": "minecraft:item",
          "name": "cobbleloots:nugget",
          "weight": 1,
          "functions": [
            {
              "function": "minecraft:set_count",
              "count": {
                "min": 1,
                "max": 2
              }
            }
          ]
        }
      ]
    }
  ]
}
```

### 2.2 `valuable_uncommon.json`
Contiene los objetos valiosos de nivel poco común:
```json
{
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "cobbleloots:star_piece",
          "weight": 1,
          "functions": [
            {
              "function": "minecraft:set_count",
              "count": {
                "min": 1,
                "max": 2
              }
            }
          ]
        },
        {
          "type": "minecraft:item",
          "name": "cobbleloots:big_pearl",
          "weight": 1,
          "functions": [
            {
              "function": "minecraft:set_count",
              "count": 1
            }
          ]
        },
        {
          "type": "minecraft:item",
          "name": "cobbleloots:rare_bone",
          "weight": 1,
          "functions": [
            {
              "function": "minecraft:set_count",
              "count": 1
            }
          ]
        }
      ]
    }
  ]
}
```

### 2.3 `valuable_rare.json`
Contiene los objetos valiosos de nivel raro:
```json
{
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "cobbleloots:big_nugget",
          "weight": 1,
          "functions": [
            {
              "function": "minecraft:set_count",
              "count": 1
            }
          ]
        },
        {
          "type": "minecraft:item",
          "name": "cobbleloots:balm_mushroom",
          "weight": 1,
          "functions": [
            {
              "function": "minecraft:set_count",
              "count": 1
            }
          ]
        }
      ]
    }
  ]
}
```

### 2.4 `valuable_ultra_rare.json`
Contiene los tesoros legendarios/ultra raros:
```json
{
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "cobbleloots:comet_shard",
          "weight": 1,
          "functions": [
            {
              "function": "minecraft:set_count",
              "count": 1
            }
          ]
        },
        {
          "type": "minecraft:item",
          "name": "cobbleloots:pearl_string",
          "weight": 1,
          "functions": [
            {
              "function": "minecraft:set_count",
              "count": 1
            }
          ]
        }
      ]
    }
  ]
}
```

---

## 3. Integración en Tablas Principales de Loot Balls

Ubicación: `common/src/main/resources/data/cobbleloots/loot_table/loot_ball/`

En cada tabla principal se incorpora una entrada de tipo `"minecraft:loot_table"` en el pool principal existente con peso `2`:

### 3.1 Tier 1 — Común
- **Tablas afectadas:** `poke.json`, `premier.json`, `azure.json`, `citrine.json`, `verdant.json`, `roseate.json`, `slate.json`
- **Entrada añadida:**
  ```json
  {
    "type": "minecraft:loot_table",
    "value": "cobbleloots:loot_ball/shared/valuable_common",
    "weight": 2
  }
  ```

### 3.2 Tier 2 — Poco Común
- **Tablas afectadas:** `great.json`, `safari.json`, `nest.json`, `timer.json`, `quick.json`, `net.json`, `dive.json`, `lure.json`
- **Entrada añadida:**
  ```json
  {
    "type": "minecraft:loot_table",
    "value": "cobbleloots:loot_ball/shared/valuable_uncommon",
    "weight": 2
  }
  ```

### 3.3 Tier 3 — Raro
- **Tablas afectadas:** `ultra.json`, `dusk.json`, `luxury.json`
- **Entrada añadida:**
  ```json
  {
    "type": "minecraft:loot_table",
    "value": "cobbleloots:loot_ball/shared/valuable_rare",
    "weight": 2
  }
  ```

### 3.4 Tier 4 — Ultra Raro
- **Tabla afectada:** `master.json`
- **Entrada añadida:**
  ```json
  {
    "type": "minecraft:loot_table",
    "value": "cobbleloots:loot_ball/shared/valuable_ultra_rare",
    "weight": 2
  }
  ```

*(Nota de balance: Las botínbolas temáticas cerradas `heal.json` (medicina), `rainbow.json` (fuegos y banners) y `pumpkin.json` (Halloween) no reciben tablas compartidas genéricas para preservar su rol específico).*

---

## 4. Enriquecimiento Temático de Pesca y Océano

En Cobbleloots, la pesca con cañas de Cobblemon genera Loot Balls acuáticas al recoger el sedal (`CobblelootsSourceType.FISHING`). Para otorgar un fuerte incentivo temático al pescar y explorar el océano, se configuran drops directos en las tablas de estas bolas:

### 4.1 `lure.json` (Lure Ball)
Obtenible exclusivamente mediante pesca con caña:
- Además de `valuable_uncommon` (weight 2), se agregan directamente al pool principal:
  - `cobbleloots:pearl`: peso 5, cantidad 1-3.
  - `cobbleloots:big_pearl`: peso 3, cantidad 1.
  - `cobbleloots:rare_bone`: peso 2, cantidad 1.

### 4.2 `dive.json` (Dive Ball)
Generación en biomas oceánicos profundos y pesca:
- Además de `valuable_uncommon` (weight 2), se agregan directamente al pool principal:
  - `cobbleloots:pearl`: peso 5, cantidad 1-3.
  - `cobbleloots:big_pearl`: peso 3, cantidad 1.

---

## 5. Tooling de Documentación y Sincronización

### 5.1 Actualización de `scripts/docs.py`
En `copy_item_icon(namespace: str, item_id: str)`:
```python
    elif namespace == "cobbleloots":
        base_item_path = LOOT_BALLS_ASSETS / "textures"
        search_paths = [
            base_item_path / "item",
        ]
        for path in search_paths:
            if (path / texture_name).exists():
                source_file = path / texture_name
                break
```
Al ejecutar `python scripts/docs.py`:
- Copia las texturas 16x16 de `common/src/main/resources/assets/cobbleloots/textures/item/*.png` hacia `docs/assets/items/cobbleloots/*.png`.
- Re-calcula todas las tablas de probabilidad ponderada en `docs/loot_balls/`.

### 5.2 Documentación de Usuario y Changelog
- **`MODINFO.md`**: Actualizar la sección de características documentando la distribución de los objetos valiosos en las Loot Balls y la pesca.
- **`.changelog/DEV-9.md`**: Fragmento en inglés para el release automation:
  ```markdown
  ### Added
  - Integrated Pokémon valuable items (nuggets, pearls, stardust, comet shards, and more) into Loot Ball and fishing loot tables across all rarity tiers.
  ```

---

## 6. Verificación y Criterios de Aceptación

1. **Compilación Limpia:**
   - `./gradlew build` sin errores en `common`, `fabric` y `neoforge`.
2. **Generador de Documentación:**
   - `python scripts/docs.py` finaliza exitosamente y actualiza `docs/loot_balls/`.
3. **Validación de Datos en Juego:**
   - Comprobación en cliente (`./gradlew :fabric:runClient` o `:neoforge:runClient`) verificando que abrir Loot Balls de cada tier suelte los objetos valiosos esperados.
   - Ejecución de `/cobbleloots debug weights` para validar que los pesos se calculan correctamente.
