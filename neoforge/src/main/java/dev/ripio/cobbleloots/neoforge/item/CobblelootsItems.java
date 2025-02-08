package dev.ripio.cobbleloots.neoforge.item;

import dev.ripio.cobbleloots.Cobbleloots;
import dev.ripio.cobbleloots.item.CobblelootsLootBallItem;
import dev.ripio.cobbleloots.neoforge.entity.CobblelootsEntities;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CobblelootsItems {
  public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Cobbleloots.MOD_ID);

  public static final DeferredItem<CobblelootsLootBallItem> LOOT_BALL_ITEM = ITEMS.register("loot_ball", () -> new CobblelootsLootBallItem(new Item.Properties(), CobblelootsEntities.LOOT_BALL.get()));

  public static void register(IEventBus eventBus) {
    ITEMS.register(eventBus);
  }
}
