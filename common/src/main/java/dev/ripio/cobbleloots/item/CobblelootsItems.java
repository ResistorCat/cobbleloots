package dev.ripio.cobbleloots.item;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.ripio.cobbleloots.entity.CobblelootsEntities;
import dev.ripio.cobbleloots.item.custom.CobblelootsLootBallItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

public class CobblelootsItems {
  // Items
  public static CobblelootsLootBallItem getBaseLootBallItem() {
    return new CobblelootsLootBallItem(new Item.Properties(), CobblelootsEntities.getLootBallEntityType());
  }

  @ExpectPlatform
  public static CobblelootsLootBallItem getLootBallItem() {
    throw new AssertionError();
  }

  // Creative mode tab
  public static void addCreativeTabItems(CreativeModeTab.Output output) {
    output.accept(getLootBallItem());
  }

  public static void addCreativeTabItems(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output output) {
    addCreativeTabItems(output);
  }

}
