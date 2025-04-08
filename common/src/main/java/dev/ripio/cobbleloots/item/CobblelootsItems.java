package dev.ripio.cobbleloots.item;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.ripio.cobbleloots.entity.CobblelootsEntities;
import dev.ripio.cobbleloots.item.custom.CobblelootsLootBallItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

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
    output.accept(new ItemStack(getLootBallItem()));
    output.accept(generateStackWithData("poke"));
    output.accept(generateStackWithData("great"));
    output.accept(generateStackWithData("ultra"));
    output.accept(generateStackWithData("master"));
  }

  public static void addCreativeTabItems(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output output) {
    addCreativeTabItems(output);
  }

  // Private
  private static ItemStack generateStackWithData(String data) {
    CobblelootsLootBallItem lootBallItem = getLootBallItem();
    ItemStack stack = new ItemStack(lootBallItem);
    CompoundTag tag = new CompoundTag();
    tag.putString("LootBallData", "cobbleloots:loot_ball/" + data);
    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    return stack;
  }

}
