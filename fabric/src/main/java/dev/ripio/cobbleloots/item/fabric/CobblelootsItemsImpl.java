package dev.ripio.cobbleloots.item.fabric;

import dev.ripio.cobbleloots.Cobbleloots;
import dev.ripio.cobbleloots.item.CobblelootsItems;
import dev.ripio.cobbleloots.item.custom.CobblelootsLootBallItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import static dev.ripio.cobbleloots.item.CobblelootsItems.getBaseLootBallItem;
import static dev.ripio.cobbleloots.util.CobblelootsUtils.cobblelootsResource;
import static dev.ripio.cobbleloots.util.CobblelootsUtils.cobblelootsText;

public class CobblelootsItemsImpl {
  // ITEMS
  private static final CobblelootsLootBallItem LOOT_BALL_ITEM = Registry.register(BuiltInRegistries.ITEM, cobblelootsResource("loot_ball"), getBaseLootBallItem());

  public static CobblelootsLootBallItem getLootBallItem() {
    return LOOT_BALL_ITEM;
  }

  // CREATIVE MODE TAB
  public static final ResourceKey<CreativeModeTab> COBBLELOOTS_TAB_KEY = ResourceKey.create(
      BuiltInRegistries.CREATIVE_MODE_TAB.key(), cobblelootsResource("cobbleloots_tab"));

  public static final CreativeModeTab COBBLELOOTS_TAB = FabricItemGroup.builder()
      .icon(() -> new ItemStack(getLootBallItem()))
      .title(cobblelootsText("creativeModeTab.cobbleloots"))
      .build();

  public static void registerItems() {
    Cobbleloots.LOGGER.info("Registering items");
    Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, COBBLELOOTS_TAB_KEY, COBBLELOOTS_TAB);
    ItemGroupEvents.modifyEntriesEvent(COBBLELOOTS_TAB_KEY).register(CobblelootsItems::addCreativeTabItems);
  }
}
