# Especificación de Diseño: DEV-8 - Objetos Valiosos de Pokémon

**Fecha:** 2026-09-21  
**Ticket Linear:** [DEV-8](https://linear.app/ripiodev/issue/DEV-8/cobbleloots-registrar-objetos-valiosos-de-pokemon-con-texturas-lore-y)  
**Estado:** Aprobado para Planificación  

---

## 1. Contexto y Objetivos

En los juegos de Pokémon existen objetos clásicos conocidos como *Valuable Items* (objetos valiosos) cuyo propósito principal es ser vendidos por dinero a coleccionistas, tiendas y anticuarios.

El objetivo de esta especificación es definir, registrar e implementar la identidad visual, clases, comportamiento y lore de 10 objetos valiosos clásicos dentro de **Cobbleloots** para Minecraft **1.21.1** (compatible tanto con Fabric como con NeoForge). Estos objetos estarán disponibles en el juego, completamente texturizados en estilo pixel-art 16x16, integrados en la pestaña creativa de Cobbleloots y con soporte multilenguaje (inglés, español y portugués).

---

## 2. Catálogo de Objetos Valiosos y Matriz de Rareza

Se registran 10 objetos bajo el namespace `cobbleloots`:

| ID de Registro | Nombre en Inglés | Nombre en Español | Rareza Minecraft | Lore Inmersivo (Bulbapedia) |
| :--- | :--- | :--- | :--- | :--- |
| `cobbleloots:nugget` | Nugget | Pepita | `COMMON` (Blanco) | *A nugget of pure, glittering gold.* / *Una pepita de oro puro y reluciente.* |
| `cobbleloots:big_nugget` | Big Nugget | Maxipepita | `UNCOMMON` (Amarillo) | *A large nugget of pure gold that gives off a lustrous gleam.* / *Una gran pepita de oro puro que desprende un brillo lustroso.* |
| `cobbleloots:pearl` | Pearl | Perla | `COMMON` (Blanco) | *A relatively small, shiny pearl that has a lovely silver luster.* / *Una perla pequeña y brillante con un hermoso lustre plateado.* |
| `cobbleloots:big_pearl` | Big Pearl | Perla grande | `UNCOMMON` (Amarillo) | *A quite-large pearl that sparkles in a pretty silvery color.* / *Una perla de gran tamaño que destella con un lindo color plateado.* |
| `cobbleloots:pearl_string` | Pearl String | Sarta de perlas | `RARE` (Cian / Aqua) | *Very large pearls, sparkling with a pure luster, strung together.* / *Grandes perlas unidas en una sarta que resplandecen con un brillo puro.* |
| `cobbleloots:stardust` | Stardust | Polvo estelar | `COMMON` (Blanco) | *Lovely red sand that sparkles softly like starlight.* / *Fina arena roja que centellea suavemente como la luz de las estrellas.* |
| `cobbleloots:star_piece` | Star Piece | Trozo estrella | `UNCOMMON` (Amarillo) | *A small shard of a pretty gem that sparkles in a red color.* / *Pequeño fragmento de gema preciosa que brilla intensamente de color rojo.* |
| `cobbleloots:comet_shard` | Comet Shard | Fragmento cometa | `RARE` (Cian / Aqua) | *A shard that fell from a comet. It shines with an otherworldly light.* / *Fragmento caído de un cometa que irradia una luz de otro mundo.* |
| `cobbleloots:rare_bone` | Rare Bone | Hueso raro | `UNCOMMON` (Amarillo) | *A rare bone that is extremely valuable for Pokémon archaeology.* / *Un hueso excepcional de gran valor para la arqueología Pokémon.* |
| `cobbleloots:balm_mushroom` | Balm Mushroom | Seta aroma | `RARE` (Cian / Aqua) | *A rare mushroom that gives off a delicate, pleasing fragrance.* / *Un hongo muy poco común que desprende una fragancia dulce y delicada.* |

### Propiedades de Juego Comunes
- **Tamaño de stack:** 64 unidades (`.stacksTo(64)`).
- **Consumible:** No (todos son ítems de tesoro/coleccionismo puro).
- **Línea de pie comercial:**
  - **EN**: `Can be sold at a high price to shops and collectors.`
  - **ES**: `Puede venderse a alto precio a comerciantes y coleccionistas.`
  - **PT**: `Pode ser vendido por um alto preço para lojas e colecionadores.`

---

## 3. Arquitectura de Clases y Registro Multi-Loader

### 3.1 Clase Personalizada: `CobblelootsValuableItem`
- **Ubicación:** `common/src/main/java/dev/ripio/cobbleloots/item/custom/CobblelootsValuableItem.java`
- **Herencia:** `net.minecraft.world.item.Item`
- **Comportamiento en `appendHoverText`:**
  1. Agrega el componente de descripción inmersiva: `Component.translatable(this.getDescriptionId() + ".desc").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)`.
  2. Agrega el componente comercial: `Component.translatable("item.cobbleloots.valuable.commercial_value").withStyle(ChatFormatting.GOLD)`.

### 3.2 Abstracción en `common/`: `CobblelootsItems.java`
- Método factoría:
  ```java
  public static CobblelootsValuableItem createValuableItem(Rarity rarity) {
      return new CobblelootsValuableItem(new Item.Properties().rarity(rarity).stacksTo(64));
  }
  ```
- Métodos `@ExpectPlatform` individuales:
  - `public static Item getNuggetItem()`
  - `public static Item getBigNuggetItem()`
  - `public static Item getPearlItem()`
  - `public static Item getBigPearlItem()`
  - `public static Item getPearlStringItem()`
  - `public static Item getStardustItem()`
  - `public static Item getStarPieceItem()`
  - `public static Item getCometShardItem()`
  - `public static Item getRareBoneItem()`
  - `public static Item getBalmMushroomItem()`
- Lista auxiliar inmutable `List<Supplier<Item>> getValuableItems()` para ordenar e iterar fácilmente los ítems.

### 3.3 Implementación Fabric: `CobblelootsItemsImpl.java`
- Registra cada ítem en `BuiltInRegistries.ITEM` con su `cobblelootsResource(id)`.
- Provee las implementaciones de los métodos `@ExpectPlatform`.

### 3.4 Implementación NeoForge: `CobblelootsItemsImpl.java`
- Registra cada ítem en `public static final DeferredRegister.Items ITEMS` usando `ITEMS.register(id, () -> CobblelootsItems.createValuableItem(rarity))`.
- Provee las implementaciones de los métodos `@ExpectPlatform` resolviendo `DeferredItem.get()`.

### 3.5 Pestaña Creativa
- En `CobblelootsItems.addCreativeTabItems(CreativeModeTab.Output output)`:
  - Se agregan todos los ítems valiosos invocando `output.accept(...)` en el orden estipulado después de las botínbolas especiales.

---

## 4. Recursos Visuales y Modelos

### 4.1 Modelos JSON 2D (`assets/cobbleloots/models/item/`)
Para cada uno de los 10 objetos se define:
```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "cobbleloots:item/<item_id>"
  }
}
```

### 4.2 Texturas Pixel-Art 16x16 (`assets/cobbleloots/textures/item/`)
Cada textura será un archivo PNG RGBA de 16x16 píxeles con una estética coherente con Cobblemon y Minecraft:
1. `nugget.png`
2. `big_nugget.png`
3. `pearl.png`
4. `big_pearl.png`
5. `pearl_string.png`
6. `stardust.png`
7. `star_piece.png`
8. `comet_shard.png`
9. `rare_bone.png`
10. `balm_mushroom.png`

---

## 5. Localización e Internacionalización (i18n)

Se actualizarán/crearán las siguientes traducciones en `common/src/main/resources/assets/cobbleloots/lang/`:
- `en_us.json`
- `es_ec.json`
- `es_es.json`
- `pt_br.json`

Claves requeridas por cada ítem:
- `item.cobbleloots.<id>`: Nombre del ítem.
- `item.cobbleloots.<id>.desc`: Lore inmersivo.
- `item.cobbleloots.valuable.commercial_value`: Leyenda de valor comercial compartida.

---

## 6. Verificación y Criterios de Aceptación

1. **Compilación Limpia:**
   - `./gradlew build` ejecuta sin errores en los 3 submódulos (`common`, `fabric`, `neoforge`).
2. **Pestaña Creativa:**
   - En el cliente, los 10 objetos aparecen con sus nombres, colores de rareza y texturas en la pestaña `Cobbleloots`.
3. **Tooltips y Lore:**
   - Al pasar el cursor sobre cualquier objeto valioso, se observa el nombre con el color de rareza, la descripción en cursiva gris y el texto comercial dorado.
4. **Multiplataforma:**
   - Funciona idénticamente en clientes y servidores Fabric y NeoForge 1.21.1.
