package dev.ripio.cobbleloots.item.fabric;

import dev.ripio.cobbleloots.Cobbleloots;
import dev.ripio.cobbleloots.item.CobblelootsItems;
import dev.ripio.cobbleloots.item.custom.CobblelootsLootBallItem;
import dev.ripio.cobbleloots.item.custom.CobblelootsValuableItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import static dev.ripio.cobbleloots.item.CobblelootsItems.createValuableItem;
import static dev.ripio.cobbleloots.item.CobblelootsItems.getBaseLootBallItem;
import static dev.ripio.cobbleloots.item.CobblelootsItems.generateStackWithCobblemonTexture;
import static dev.ripio.cobbleloots.util.CobblelootsUtils.cobblelootsResource;
import static dev.ripio.cobbleloots.util.CobblelootsUtils.cobblelootsText;

public class CobblelootsItemsImpl {
  // ITEMS
  private static final CobblelootsLootBallItem LOOT_BALL_ITEM = Registry.register(BuiltInRegistries.ITEM, cobblelootsResource("loot_ball"), getBaseLootBallItem());

  public static CobblelootsLootBallItem getLootBallItem() {
    return LOOT_BALL_ITEM;
  }

  // Valuable Items
  private static final CobblelootsValuableItem NUGGET_ITEM = Registry.register(BuiltInRegistries.ITEM, cobblelootsResource("nugget"), createValuableItem(Rarity.COMMON));
  private static final CobblelootsValuableItem BIG_NUGGET_ITEM = Registry.register(BuiltInRegistries.ITEM, cobblelootsResource("big_nugget"), createValuableItem(Rarity.UNCOMMON));
  private static final CobblelootsValuableItem PEARL_ITEM = Registry.register(BuiltInRegistries.ITEM, cobblelootsResource("pearl"), createValuableItem(Rarity.COMMON));
  private static final CobblelootsValuableItem BIG_PEARL_ITEM = Registry.register(BuiltInRegistries.ITEM, cobblelootsResource("big_pearl"), createValuableItem(Rarity.UNCOMMON));
  private static final CobblelootsValuableItem PEARL_STRING_ITEM = Registry.register(BuiltInRegistries.ITEM, cobblelootsResource("pearl_string"), createValuableItem(Rarity.RARE));
  private static final CobblelootsValuableItem STARDUST_ITEM = Registry.register(BuiltInRegistries.ITEM, cobblelootsResource("stardust"), createValuableItem(Rarity.COMMON));
  private static final CobblelootsValuableItem STAR_PIECE_ITEM = Registry.register(BuiltInRegistries.ITEM, cobblelootsResource("star_piece"), createValuableItem(Rarity.UNCOMMON));
  private static final CobblelootsValuableItem COMET_SHARD_ITEM = Registry.register(BuiltInRegistries.ITEM, cobblelootsResource("comet_shard"), createValuableItem(Rarity.RARE));
  private static final CobblelootsValuableItem RARE_BONE_ITEM = Registry.register(BuiltInRegistries.ITEM, cobblelootsResource("rare_bone"), createValuableItem(Rarity.UNCOMMON));
  private static final CobblelootsValuableItem BALM_MUSHROOM_ITEM = Registry.register(BuiltInRegistries.ITEM, cobblelootsResource("balm_mushroom"), createValuableItem(Rarity.RARE));

  public static Item getNuggetItem() {
    return NUGGET_ITEM;
  }

  public static Item getBigNuggetItem() {
    return BIG_NUGGET_ITEM;
  }

  public static Item getPearlItem() {
    return PEARL_ITEM;
  }

  public static Item getBigPearlItem() {
    return BIG_PEARL_ITEM;
  }

  public static Item getPearlStringItem() {
    return PEARL_STRING_ITEM;
  }

  public static Item getStardustItem() {
    return STARDUST_ITEM;
  }

  public static Item getStarPieceItem() {
    return STAR_PIECE_ITEM;
  }

  public static Item getCometShardItem() {
    return COMET_SHARD_ITEM;
  }

  public static Item getRareBoneItem() {
    return RARE_BONE_ITEM;
  }

  public static Item getBalmMushroomItem() {
    return BALM_MUSHROOM_ITEM;
  }

  // CREATIVE MODE TAB
  public static final ResourceKey<CreativeModeTab> COBBLELOOTS_TAB_KEY = ResourceKey.create(
      BuiltInRegistries.CREATIVE_MODE_TAB.key(), cobblelootsResource("cobbleloots_tab"));

  public static final CreativeModeTab COBBLELOOTS_TAB = FabricItemGroup.builder()
      .icon(() -> generateStackWithCobblemonTexture("master"))
      .title(cobblelootsText("creativeModeTab.cobbleloots"))
      .build();

  public static void registerItems() {
    Cobbleloots.LOGGER.info("Registering items");
    Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, COBBLELOOTS_TAB_KEY, COBBLELOOTS_TAB);
    ItemGroupEvents.modifyEntriesEvent(COBBLELOOTS_TAB_KEY).register(CobblelootsItems::addCreativeTabItems);
  }
}
