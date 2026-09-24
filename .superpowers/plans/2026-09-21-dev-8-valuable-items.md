# DEV-8 Valuable Items Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement, register, texture, and document 10 classic Pokémon Valuable Items (`nugget`, `big_nugget`, `pearl`, `big_pearl`, `pearl_string`, `stardust`, `star_piece`, `comet_shard`, `rare_bone`, `balm_mushroom`) in Cobbleloots across Fabric and NeoForge.

**Architecture:** Create a common custom item class `CobblelootsValuableItem` in `common/` that handles lore and commercial tooltip rendering. Register items through Architectury `@ExpectPlatform` delegates in `CobblelootsItems`, binding to `BuiltInRegistries.ITEM` in Fabric and `DeferredRegister.Items` in NeoForge. Expose items in the Cobbleloots creative tab, provide 16x16 pixel-art textures and 2D item models, and localize across `en_us`, `es_ec`, `es_es`, and `pt_br`.

**Tech Stack:** Minecraft 1.21.1 (Mojang official mappings), Java 21, Architectury Loom, Fabric API, NeoForge 21.1.182.

**Spec:** [.superpowers/specs/2026-09-21-dev-8-valuable-items-design.md](file:///C:/Users/franc/GitHub/cobbleloots/.worktrees/ripio/dev-8-cobbleloots-registrar-objetos-valiosos-de-pokemon-con/.superpowers/specs/2026-09-21-dev-8-valuable-items-design.md)

## Global Constraints
- Target Minecraft version: `1.21.1`.
- Target Java version: `21`.
- All item logic and declarations must be housed in `common/`.
- Platform registration code must stay strictly inside `fabric/` and `neoforge/`.
- No platform leakage: Never import `net.fabricmc.*` or `net.neoforged.*` in `common/`.
- Early returns convention must be followed.
- Never edit `CHANGELOG.md` directly; create `.changelog/DEV-8.md`.
- Both Fabric and NeoForge must compile cleanly with `./gradlew build`.

---

### Task 1: Core Valuable Item Class

**Files:**
- Create: `common/src/main/java/dev/ripio/cobbleloots/item/custom/CobblelootsValuableItem.java`

**Interfaces:**
- Produces:
  - `dev.ripio.cobbleloots.item.custom.CobblelootsValuableItem`
  - Constructor: `CobblelootsValuableItem(Item.Properties properties)`
  - Methods: `appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag)`

- [ ] **Step 1: Create `CobblelootsValuableItem.java` in `common/`**

```java
package dev.ripio.cobbleloots.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class CobblelootsValuableItem extends Item {

  public CobblelootsValuableItem(Properties properties) {
    super(properties);
  }

  @Override
  public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
    tooltipComponents.add(
        Component.translatable(this.getDescriptionId() + ".desc")
            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
    );
    tooltipComponents.add(
        Component.translatable("item.cobbleloots.valuable.commercial_value")
            .withStyle(ChatFormatting.GOLD)
    );
    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
  }
}
```

- [ ] **Step 2: Verify compilation**

Run: `.\gradlew :common:compileJava`  
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add common/src/main/java/dev/ripio/cobbleloots/item/custom/CobblelootsValuableItem.java
git commit -m "feat(item): add CobblelootsValuableItem class with lore and commercial tooltip"
```

---

### Task 2: Common Registration Abstraction & Creative Tab Integration

**Files:**
- Modify: `common/src/main/java/dev/ripio/cobbleloots/item/CobblelootsItems.java`

**Interfaces:**
- Consumes:
  - `CobblelootsValuableItem`
- Produces:
  - `CobblelootsItems.createValuableItem(Rarity rarity)`
  - `CobblelootsItems.getNuggetItem()`
  - `CobblelootsItems.getBigNuggetItem()`
  - `CobblelootsItems.getPearlItem()`
  - `CobblelootsItems.getBigPearlItem()`
  - `CobblelootsItems.getPearlStringItem()`
  - `CobblelootsItems.getStardustItem()`
  - `CobblelootsItems.getStarPieceItem()`
  - `CobblelootsItems.getCometShardItem()`
  - `CobblelootsItems.getRareBoneItem()`
  - `CobblelootsItems.getBalmMushroomItem()`
  - `CobblelootsItems.getValuableItems()`

- [ ] **Step 1: Add factory and `@ExpectPlatform` methods to `CobblelootsItems.java`**

Update `common/src/main/java/dev/ripio/cobbleloots/item/CobblelootsItems.java` to define:
- Factory method:
  ```java
  public static CobblelootsValuableItem createValuableItem(Rarity rarity) {
    return new CobblelootsValuableItem(new Item.Properties().rarity(rarity).stacksTo(64));
  }
  ```
- 10 `@ExpectPlatform` methods returning `Item`:
  - `getNuggetItem()`, `getBigNuggetItem()`, `getPearlItem()`, `getBigPearlItem()`, `getPearlStringItem()`, `getStardustItem()`, `getStarPieceItem()`, `getCometShardItem()`, `getRareBoneItem()`, `getBalmMushroomItem()`.
- Helper list `getValuableItems()`:
  ```java
  public static List<Supplier<Item>> getValuableItems() {
    return List.of(
        CobblelootsItems::getNuggetItem,
        CobblelootsItems::getBigNuggetItem,
        CobblelootsItems::getPearlItem,
        CobblelootsItems::getBigPearlItem,
        CobblelootsItems::getPearlStringItem,
        CobblelootsItems::getStardustItem,
        CobblelootsItems::getStarPieceItem,
        CobblelootsItems::getCometShardItem,
        CobblelootsItems::getRareBoneItem,
        CobblelootsItems::getBalmMushroomItem
    );
  }
  ```
- Update `addCreativeTabItems` to iterate `getValuableItems()`:
  ```java
  // Valuable Items
  for (Supplier<Item> itemSupplier : getValuableItems()) {
    output.accept(itemSupplier.get());
  }
  ```

- [ ] **Step 2: Verify `common` compiles**

Run: `.\gradlew :common:compileJava`  
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add common/src/main/java/dev/ripio/cobbleloots/item/CobblelootsItems.java
git commit -m "feat(item): add valuable items factory, ExpectPlatform accessors, and creative tab entries"
```

---

### Task 3: Multi-Loader Registration (Fabric & NeoForge)

**Files:**
- Modify: `fabric/src/main/java/dev/ripio/cobbleloots/item/fabric/CobblelootsItemsImpl.java`
- Modify: `neoforge/src/main/java/dev/ripio/cobbleloots/item/neoforge/CobblelootsItemsImpl.java`

**Interfaces:**
- Consumes:
  - `CobblelootsItems.createValuableItem(Rarity)`
- Produces:
  - Platform implementations of all 10 `@ExpectPlatform` getters in `CobblelootsItemsImpl` for Fabric and NeoForge.

- [ ] **Step 1: Implement Fabric registration in `fabric/.../CobblelootsItemsImpl.java`**

In `fabric/src/main/java/dev/ripio/cobbleloots/item/fabric/CobblelootsItemsImpl.java`:
- Register all 10 items in `BuiltInRegistries.ITEM` using `cobblelootsResource("<id>")` and `createValuableItem(rarity)`:
  - `nugget`: `Rarity.COMMON`
  - `big_nugget`: `Rarity.UNCOMMON`
  - `pearl`: `Rarity.COMMON`
  - `big_pearl`: `Rarity.UNCOMMON`
  - `pearl_string`: `Rarity.RARE`
  - `stardust`: `Rarity.COMMON`
  - `star_piece`: `Rarity.UNCOMMON`
  - `comet_shard`: `Rarity.RARE`
  - `rare_bone`: `Rarity.UNCOMMON`
  - `balm_mushroom`: `Rarity.RARE`
- Implement each corresponding static getter returning the registered item.

- [ ] **Step 2: Implement NeoForge registration in `neoforge/.../CobblelootsItemsImpl.java`**

In `neoforge/src/main/java/dev/ripio/cobbleloots/item/neoforge/CobblelootsItemsImpl.java`:
- Register all 10 items with `ITEMS.register("<id>", () -> CobblelootsItems.createValuableItem(rarity))`:
  - `nugget`: `Rarity.COMMON`
  - `big_nugget`: `Rarity.UNCOMMON`
  - `pearl`: `Rarity.COMMON`
  - `big_pearl`: `Rarity.UNCOMMON`
  - `pearl_string`: `Rarity.RARE`
  - `stardust`: `Rarity.COMMON`
  - `star_piece`: `Rarity.UNCOMMON`
  - `comet_shard`: `Rarity.RARE`
  - `rare_bone`: `Rarity.UNCOMMON`
  - `balm_mushroom`: `Rarity.RARE`
- Implement each corresponding static getter returning `DEFERRED_ITEM.get()`.

- [ ] **Step 3: Verify Fabric and NeoForge compilation**

Run: `.\gradlew compileJava`  
Expected: BUILD SUCCESSFUL across `:common:compileJava`, `:fabric:compileJava`, and `:neoforge:compileJava`.

- [ ] **Step 4: Commit**

```bash
git add fabric/src/main/java/dev/ripio/cobbleloots/item/fabric/CobblelootsItemsImpl.java neoforge/src/main/java/dev/ripio/cobbleloots/item/neoforge/CobblelootsItemsImpl.java
git commit -m "feat(item): register valuable items on Fabric and NeoForge platforms"
```

---

### Task 4: 2D Item Models & Pixel-Art Textures

**Files:**
- Create: `common/src/main/resources/assets/cobbleloots/models/item/nugget.json`
- Create: `common/src/main/resources/assets/cobbleloots/models/item/big_nugget.json`
- Create: `common/src/main/resources/assets/cobbleloots/models/item/pearl.json`
- Create: `common/src/main/resources/assets/cobbleloots/models/item/big_pearl.json`
- Create: `common/src/main/resources/assets/cobbleloots/models/item/pearl_string.json`
- Create: `common/src/main/resources/assets/cobbleloots/models/item/stardust.json`
- Create: `common/src/main/resources/assets/cobbleloots/models/item/star_piece.json`
- Create: `common/src/main/resources/assets/cobbleloots/models/item/comet_shard.json`
- Create: `common/src/main/resources/assets/cobbleloots/models/item/rare_bone.json`
- Create: `common/src/main/resources/assets/cobbleloots/models/item/balm_mushroom.json`
- Create: `common/src/main/resources/assets/cobbleloots/textures/item/nugget.png`
- Create: `common/src/main/resources/assets/cobbleloots/textures/item/big_nugget.png`
- Create: `common/src/main/resources/assets/cobbleloots/textures/item/pearl.png`
- Create: `common/src/main/resources/assets/cobbleloots/textures/item/big_pearl.png`
- Create: `common/src/main/resources/assets/cobbleloots/textures/item/pearl_string.png`
- Create: `common/src/main/resources/assets/cobbleloots/textures/item/stardust.png`
- Create: `common/src/main/resources/assets/cobbleloots/textures/item/star_piece.png`
- Create: `common/src/main/resources/assets/cobbleloots/textures/item/comet_shard.png`
- Create: `common/src/main/resources/assets/cobbleloots/textures/item/rare_bone.png`
- Create: `common/src/main/resources/assets/cobbleloots/textures/item/balm_mushroom.png`

**Interfaces:**
- Produces:
  - 10 item model JSONs referencing `minecraft:item/generated` and layer0 texture `cobbleloots:item/<id>`.
  - 10 16x16 pixel-art PNG textures.

- [ ] **Step 1: Create 10 item model JSON files**

For each item `X` in `[nugget, big_nugget, pearl, big_pearl, pearl_string, stardust, star_piece, comet_shard, rare_bone, balm_mushroom]`, create `common/src/main/resources/assets/cobbleloots/models/item/X.json`:
```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "cobbleloots:item/X"
  }
}
```

- [ ] **Step 2: Generate 10 16x16 pixel-art PNG textures**

Generate authentic 16x16 pixel-art PNG images for each item in `common/src/main/resources/assets/cobbleloots/textures/item/` using Python `Pillow` or PNG encoder:
- `nugget.png`: Pure sparkling golden nugget.
- `big_nugget.png`: Chunky, lustrous faceted big gold nugget.
- `pearl.png`: Small silvery shiny pearl.
- `big_pearl.png`: Large gleaming silver pearl.
- `pearl_string.png`: String of luminous connected pearls.
- `stardust.png`: Shimmering red celestial dust pouch/powder.
- `star_piece.png`: Red crystalline star-shaped gem shard.
- `comet_shard.png`: Astral cyan/deep blue crystal shard.
- `rare_bone.png`: Ancient fossilized bone artifact.
- `balm_mushroom.png`: Purple-capped fragrant bulbous mushroom.

- [ ] **Step 3: Verify assets with `processResources`**

Run: `.\gradlew processResources`  
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add common/src/main/resources/assets/cobbleloots/models/item/ common/src/main/resources/assets/cobbleloots/textures/item/
git commit -m "feat(assets): add 2D item models and 16x16 pixel-art textures for valuable items"
```

---

### Task 5: Multi-Language Localization (i18n)

**Files:**
- Modify: `common/src/main/resources/assets/cobbleloots/lang/en_us.json`
- Modify: `common/src/main/resources/assets/cobbleloots/lang/es_ec.json`
- Create/Modify: `common/src/main/resources/assets/cobbleloots/lang/es_es.json`
- Modify: `common/src/main/resources/assets/cobbleloots/lang/pt_br.json`

**Interfaces:**
- Produces:
  - Translation keys:
    - `item.cobbleloots.valuable.commercial_value`
    - `item.cobbleloots.<id>`
    - `item.cobbleloots.<id>.desc`

- [ ] **Step 1: Update `en_us.json`**

Add the English keys:
```json
  "item.cobbleloots.valuable.commercial_value": "Can be sold at a high price to shops and collectors.",
  "item.cobbleloots.nugget": "Nugget",
  "item.cobbleloots.nugget.desc": "A nugget of pure, glittering gold.",
  "item.cobbleloots.big_nugget": "Big Nugget",
  "item.cobbleloots.big_nugget.desc": "A large nugget of pure gold that gives off a lustrous gleam.",
  "item.cobbleloots.pearl": "Pearl",
  "item.cobbleloots.pearl.desc": "A relatively small, shiny pearl that has a lovely silver luster.",
  "item.cobbleloots.big_pearl": "Big Pearl",
  "item.cobbleloots.big_pearl.desc": "A quite-large pearl that sparkles in a pretty silvery color.",
  "item.cobbleloots.pearl_string": "Pearl String",
  "item.cobbleloots.pearl_string.desc": "Very large pearls, sparkling with a pure luster, strung together.",
  "item.cobbleloots.stardust": "Stardust",
  "item.cobbleloots.stardust.desc": "Lovely red sand that sparkles softly like starlight.",
  "item.cobbleloots.star_piece": "Star Piece",
  "item.cobbleloots.star_piece.desc": "A small shard of a pretty gem that sparkles in a red color.",
  "item.cobbleloots.comet_shard": "Comet Shard",
  "item.cobbleloots.comet_shard.desc": "A shard that fell from a comet. It shines with an otherworldly light.",
  "item.cobbleloots.rare_bone": "Rare Bone",
  "item.cobbleloots.rare_bone.desc": "A rare bone that is extremely valuable for Pokémon archaeology.",
  "item.cobbleloots.balm_mushroom": "Balm Mushroom",
  "item.cobbleloots.balm_mushroom.desc": "A rare mushroom that gives off a delicate, pleasing fragrance."
```

- [ ] **Step 2: Update `es_ec.json` & Create `es_es.json`**

Add the Spanish keys:
```json
  "item.cobbleloots.valuable.commercial_value": "Puede venderse a alto precio a comerciantes y coleccionistas.",
  "item.cobbleloots.nugget": "Pepita",
  "item.cobbleloots.nugget.desc": "Una pepita de oro puro y reluciente.",
  "item.cobbleloots.big_nugget": "Maxipepita",
  "item.cobbleloots.big_nugget.desc": "Una gran pepita de oro puro que desprende un brillo lustroso.",
  "item.cobbleloots.pearl": "Perla",
  "item.cobbleloots.pearl.desc": "Una perla pequeña y brillante con un hermoso lustre plateado.",
  "item.cobbleloots.big_pearl": "Perla grande",
  "item.cobbleloots.big_pearl.desc": "Una perla de gran tamaño que destella con un lindo color plateado.",
  "item.cobbleloots.pearl_string": "Sarta de perlas",
  "item.cobbleloots.pearl_string.desc": "Grandes perlas unidas en una sarta que resplandecen con un brillo puro.",
  "item.cobbleloots.stardust": "Polvo estelar",
  "item.cobbleloots.stardust.desc": "Fina arena roja que centellea suavemente como la luz de las estrellas.",
  "item.cobbleloots.star_piece": "Trozo estrella",
  "item.cobbleloots.star_piece.desc": "Pequeño fragmento de gema preciosa que brilla intensamente de color rojo.",
  "item.cobbleloots.comet_shard": "Fragmento cometa",
  "item.cobbleloots.comet_shard.desc": "Fragmento caído de un cometa que irradia una luz de otro mundo.",
  "item.cobbleloots.rare_bone": "Hueso raro",
  "item.cobbleloots.rare_bone.desc": "Un hueso excepcional de gran valor para la arqueología Pokémon.",
  "item.cobbleloots.balm_mushroom": "Seta aroma",
  "item.cobbleloots.balm_mushroom.desc": "Un hongo muy poco común que desprende una fragancia dulce y delicada."
```

- [ ] **Step 3: Update `pt_br.json`**

Add the Portuguese keys:
```json
  "item.cobbleloots.valuable.commercial_value": "Pode ser vendido por um alto preço para lojas e colecionadores.",
  "item.cobbleloots.nugget": "Pepita",
  "item.cobbleloots.nugget.desc": "Uma pepita de ouro puro e cintilante.",
  "item.cobbleloots.big_nugget": "Maxipepita",
  "item.cobbleloots.big_nugget.desc": "Uma grande pepita de ouro puro que emite um brilho lustroso.",
  "item.cobbleloots.pearl": "Pérola",
  "item.cobbleloots.pearl.desc": "Uma pérola relativamente pequena e brilhante com um belo brilho prateado.",
  "item.cobbleloots.big_pearl": "Pérola Grande",
  "item.cobbleloots.big_pearl.desc": "Uma pérola bastante grande que brilha em uma linda cor prateada.",
  "item.cobbleloots.pearl_string": "Colar de Pérolas",
  "item.cobbleloots.pearl_string.desc": "Pérolas muito grandes, brilhando com um brilho puro, enfiadas juntas.",
  "item.cobbleloots.stardust": "Poeira Estelar",
  "item.cobbleloots.stardust.desc": "Uma bela areia vermelha que brilha suavemente como a luz das estrelas.",
  "item.cobbleloots.star_piece": "Pedaço de Estrela",
  "item.cobbleloots.star_piece.desc": "Um pequeno fragmento de uma bela gema que brilha em uma cor vermelha.",
  "item.cobbleloots.comet_shard": "Fragmento de Cometa",
  "item.cobbleloots.comet_shard.desc": "Um fragmento caído de um cometa. Ele brilha com uma luz de outro mundo.",
  "item.cobbleloots.rare_bone": "Osso Raro",
  "item.cobbleloots.rare_bone.desc": "Um osso raro extremamente valioso para a arqueologia Pokémon.",
  "item.cobbleloots.balm_mushroom": "Cogumelo Calmante",
  "item.cobbleloots.balm_mushroom.desc": "Um cogumelo raro que exala uma fragrância delicada e agradável."
```

- [ ] **Step 4: Commit**

```bash
git add common/src/main/resources/assets/cobbleloots/lang/
git commit -m "feat(lang): add valuable items translations for English, Spanish, and Portuguese"
```

---

### Task 6: Documentation, Changelog Fragment & Full Verification

**Files:**
- Create: `.changelog/DEV-8.md`
- Modify: `MODINFO.md`
- Modify: `docs/index.md` (or relevant feature doc if applicable)

- [ ] **Step 1: Create Changelog Fragment `.changelog/DEV-8.md`**

```markdown
### Añadido
- **Objetos Valiosos de Pokémon**: Se incorporaron 10 objetos clásicos de valor comercial (`nugget`, `big_nugget`, `pearl`, `big_pearl`, `pearl_string`, `stardust`, `star_piece`, `comet_shard`, `rare_bone`, `balm_mushroom`) con texturas pixel-art personalizadas, lore inmersivo y visualización en la pestaña creativa.
```

- [ ] **Step 2: Update `MODINFO.md`**

Update `MODINFO.md` to reference the newly available valuable items collection.

- [ ] **Step 3: Run Full Build Verification**

Run: `.\gradlew build`  
Expected: BUILD SUCCESSFUL across all projects (`common`, `fabric`, `neoforge`).

- [ ] **Step 4: Commit**

```bash
git add .changelog/DEV-8.md MODINFO.md docs/
git commit -m "docs: add DEV-8 changelog fragment and update mod documentation"
```
