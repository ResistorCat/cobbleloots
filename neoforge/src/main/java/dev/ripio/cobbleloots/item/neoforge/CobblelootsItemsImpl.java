package dev.ripio.cobbleloots.item.neoforge;

import dev.ripio.cobbleloots.Cobbleloots;
import dev.ripio.cobbleloots.item.CobblelootsItems;
import dev.ripio.cobbleloots.item.custom.CobblelootsLootBallItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

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

  // Creative Mode Tabs
  public static final Supplier<CreativeModeTab> COBBLELOOTS_TAB = TABS.register("cobbleloots_tab", () -> CreativeModeTab.builder()
      .title(cobblelootsText("creativeModeTab.cobbleloots"))
      .icon(() -> new ItemStack(getLootBallItem()))
      .displayItems(CobblelootsItems::addCreativeTabItems)
      .build());

  public static void registerItems(IEventBus eventBus) {
    Cobbleloots.LOGGER.info("Registering items");
    ITEMS.register(eventBus);
    TABS.register(eventBus);
  }
}
