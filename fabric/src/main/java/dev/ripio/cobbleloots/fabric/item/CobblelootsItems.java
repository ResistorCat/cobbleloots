package dev.ripio.cobbleloots.fabric.item;

import dev.ripio.cobbleloots.Cobbleloots;
import dev.ripio.cobbleloots.fabric.entity.CobblelootsEntities;
import dev.ripio.cobbleloots.item.CobblelootsLootBallItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

import static dev.ripio.cobbleloots.util.CobblelootsUtils.cobblelootsResource;

public class CobblelootsItems {
    public static final CobblelootsLootBallItem LOOT_BALL_ITEM = Registry.register(BuiltInRegistries.ITEM, cobblelootsResource("loot_ball"), new CobblelootsLootBallItem(new Item.Properties(), CobblelootsEntities.LOOT_BALL));

    public static void registerItems() {
      Cobbleloots.LOGGER.info("Registering items");
    }
}
