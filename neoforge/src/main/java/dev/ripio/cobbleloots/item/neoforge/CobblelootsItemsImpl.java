package dev.ripio.cobbleloots.item.neoforge;

import dev.ripio.cobbleloots.Cobbleloots;
import dev.ripio.cobbleloots.item.CobblelootsItems;
import dev.ripio.cobbleloots.item.custom.CobblelootsLootBallItem;
import dev.ripio.cobbleloots.item.custom.CobblelootsValuableItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static dev.ripio.cobbleloots.item.CobblelootsItems.createValuableItem;
import static dev.ripio.cobbleloots.item.CobblelootsItems.generateStackWithCobblemonTexture;
import static dev.ripio.cobbleloots.util.CobblelootsUtils.cobblelootsText;

public class CobblelootsItemsImpl {
  // DeferredRegisters
  public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Cobbleloots.MOD_ID);
  public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, Cobbleloots.MOD_ID);

  // Items
  private static final DeferredItem<CobblelootsLootBallItem> LOOT_BALL_ITEM = ITEMS.register("loot_ball", CobblelootsItems::getBaseLootBallItem);

  public static CobblelootsLootBallItem getLootBallItem() {
    return LOOT_BALL_ITEM.get();
  }

  // Valuable Items
  private static final DeferredItem<CobblelootsValuableItem> NUGGET_ITEM = ITEMS.register("nugget", () -> createValuableItem(Rarity.COMMON));
  private static final DeferredItem<CobblelootsValuableItem> BIG_NUGGET_ITEM = ITEMS.register("big_nugget", () -> createValuableItem(Rarity.UNCOMMON));
  private static final DeferredItem<CobblelootsValuableItem> PEARL_ITEM = ITEMS.register("pearl", () -> createValuableItem(Rarity.COMMON));
  private static final DeferredItem<CobblelootsValuableItem> BIG_PEARL_ITEM = ITEMS.register("big_pearl", () -> createValuableItem(Rarity.UNCOMMON));
  private static final DeferredItem<CobblelootsValuableItem> PEARL_STRING_ITEM = ITEMS.register("pearl_string", () -> createValuableItem(Rarity.RARE));
  private static final DeferredItem<CobblelootsValuableItem> STARDUST_ITEM = ITEMS.register("stardust", () -> createValuableItem(Rarity.COMMON));
  private static final DeferredItem<CobblelootsValuableItem> STAR_PIECE_ITEM = ITEMS.register("star_piece", () -> createValuableItem(Rarity.UNCOMMON));
  private static final DeferredItem<CobblelootsValuableItem> COMET_SHARD_ITEM = ITEMS.register("comet_shard", () -> createValuableItem(Rarity.RARE));
  private static final DeferredItem<CobblelootsValuableItem> RARE_BONE_ITEM = ITEMS.register("rare_bone", () -> createValuableItem(Rarity.UNCOMMON));
  private static final DeferredItem<CobblelootsValuableItem> BALM_MUSHROOM_ITEM = ITEMS.register("balm_mushroom", () -> createValuableItem(Rarity.RARE));

  public static Item getNuggetItem() {
    return NUGGET_ITEM.get();
  }

  public static Item getBigNuggetItem() {
    return BIG_NUGGET_ITEM.get();
  }

  public static Item getPearlItem() {
    return PEARL_ITEM.get();
  }

  public static Item getBigPearlItem() {
    return BIG_PEARL_ITEM.get();
  }

  public static Item getPearlStringItem() {
    return PEARL_STRING_ITEM.get();
  }

  public static Item getStardustItem() {
    return STARDUST_ITEM.get();
  }

  public static Item getStarPieceItem() {
    return STAR_PIECE_ITEM.get();
  }

  public static Item getCometShardItem() {
    return COMET_SHARD_ITEM.get();
  }

  public static Item getRareBoneItem() {
    return RARE_BONE_ITEM.get();
  }

  public static Item getBalmMushroomItem() {
    return BALM_MUSHROOM_ITEM.get();
  }

  // Creative Mode Tabs
  public static final Supplier<CreativeModeTab> COBBLELOOTS_TAB = TABS.register("cobbleloots_tab", () -> CreativeModeTab.builder()
      .title(cobblelootsText("creativeModeTab.cobbleloots"))
      .icon(() -> generateStackWithCobblemonTexture("master"))
      .displayItems(CobblelootsItems::addCreativeTabItems)
      .build());

  public static void registerItems(IEventBus eventBus) {
    Cobbleloots.LOGGER.info("Registering items");
    ITEMS.register(eventBus);
    TABS.register(eventBus);
  }
}
