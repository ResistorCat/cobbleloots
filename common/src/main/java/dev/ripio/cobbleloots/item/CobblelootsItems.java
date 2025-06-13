package dev.ripio.cobbleloots.item;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.ripio.cobbleloots.item.custom.CobblelootsLootBallItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

import static dev.ripio.cobbleloots.entity.custom.CobblelootsLootBall.TAG_CUSTOM_TEXTURE;
import static dev.ripio.cobbleloots.entity.custom.CobblelootsLootBall.TAG_LOOT_BALL_DATA_ID;
import static dev.ripio.cobbleloots.entity.custom.CobblelootsLootBall.TAG_VARIANT_ID;

import java.util.List;

public class CobblelootsItems {
  // Items
  public static CobblelootsLootBallItem getBaseLootBallItem() {
    return new CobblelootsLootBallItem(new Item.Properties());
  }

  @ExpectPlatform
  public static CobblelootsLootBallItem getLootBallItem() {
    throw new AssertionError();
  }

  // Creative mode tab
  public static void addCreativeTabItems(CreativeModeTab.Output output) {
    // Creative Balls
    output.accept(new ItemStack(getLootBallItem()));

    // Normal Balls
    output.accept(generateStackWithCobblemonTexture("poke"));
    output.accept(generateStackWithCobblemonTexture("citrine"));
    output.accept(generateStackWithCobblemonTexture("verdant"));
    output.accept(generateStackWithCobblemonTexture("azure"));
    output.accept(generateStackWithCobblemonTexture("roseate"));
    output.accept(generateStackWithCobblemonTexture("slate"));
    output.accept(generateStackWithCobblemonTexture("premier"));
    output.accept(generateStackWithCobblemonTexture("great"));
    output.accept(generateStackWithCobblemonTexture("ultra"));
    output.accept(generateStackWithCobblemonTexture("safari"));
    output.accept(generateStackWithCobblemonTexture("fast"));
    output.accept(generateStackWithCobblemonTexture("level"));
    output.accept(generateStackWithCobblemonTexture("lure"));
    output.accept(generateStackWithCobblemonTexture("heavy"));
    output.accept(generateStackWithCobblemonTexture("love"));
    output.accept(generateStackWithCobblemonTexture("friend"));
    output.accept(generateStackWithCobblemonTexture("moon"));
    output.accept(generateStackWithCobblemonTexture("sport"));
    output.accept(generateStackWithCobblemonTexture("park"));
    output.accept(generateStackWithCobblemonTexture("net"));
    output.accept(generateStackWithCobblemonTexture("dive"));
    output.accept(generateStackWithCobblemonTexture("nest"));
    output.accept(generateStackWithCobblemonTexture("repeat"));
    output.accept(generateStackWithCobblemonTexture("timer"));
    output.accept(generateStackWithCobblemonTexture("luxury"));
    output.accept(generateStackWithCobblemonTexture("dusk"));
    output.accept(generateStackWithCobblemonTexture("heal"));
    output.accept(generateStackWithCobblemonTexture("quick"));
    output.accept(generateStackWithCobblemonTexture("dream"));
    output.accept(generateStackWithCobblemonTexture("beast"));
    output.accept(generateStackWithCobblemonTexture("master"));
    output.accept(generateStackWithCobblemonTexture("cherish"));

    // Ancient Balls
    output.accept(generateStackWithCobblemonTexture("ancient_poke"));
    output.accept(generateStackWithCobblemonTexture("ancient_citrine"));
    output.accept(generateStackWithCobblemonTexture("ancient_verdant"));
    output.accept(generateStackWithCobblemonTexture("ancient_azure"));
    output.accept(generateStackWithCobblemonTexture("ancient_roseate"));
    output.accept(generateStackWithCobblemonTexture("ancient_slate"));
    output.accept(generateStackWithCobblemonTexture("ancient_ivory"));
    output.accept(generateStackWithCobblemonTexture("ancient_great"));
    output.accept(generateStackWithCobblemonTexture("ancient_ultra"));
    output.accept(generateStackWithCobblemonTexture("ancient_feather"));
    output.accept(generateStackWithCobblemonTexture("ancient_wing"));
    output.accept(generateStackWithCobblemonTexture("ancient_jet"));
    output.accept(generateStackWithCobblemonTexture("ancient_heavy"));
    output.accept(generateStackWithCobblemonTexture("ancient_leaden"));
    output.accept(generateStackWithCobblemonTexture("ancient_gigaton"));
    output.accept(generateStackWithCobblemonTexture("ancient_origin"));

  }

  public static void addCreativeTabItems(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output output) {
    addCreativeTabItems(output);
  }

  // Private
  private static ItemStack generateStackWithData(String dataId, String variantId) {
    CobblelootsLootBallItem lootBallItem = getLootBallItem();
    ItemStack stack = new ItemStack(lootBallItem);
    CompoundTag tag = new CompoundTag();
    tag.putString(TAG_LOOT_BALL_DATA_ID, "cobbleloots:loot_ball/" + dataId);
    tag.putString(TAG_VARIANT_ID, variantId);
    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    return stack;
  }

  private static ItemStack generateStackWithData(String dataId) {
    return generateStackWithData(dataId, "");
  }

  private static ItemStack generateStackWithCobblemonTexture(String textureId) {
    return generateStackWithTexture("cobblemon:textures/poke_balls/" + textureId + "_ball");
  }

  private static ItemStack generateStackWithTexture(String path) {
    CobblelootsLootBallItem lootBallItem = getLootBallItem();
    ItemStack stack = new ItemStack(lootBallItem);
    CompoundTag tag = new CompoundTag();
    tag.putString(TAG_CUSTOM_TEXTURE, path + ".png");
    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    stack.set(DataComponents.CUSTOM_NAME, Component.translatable("item.cobbleloots.loot_ball.custom_texture", path));
    stack.set(DataComponents.LORE, new ItemLore(List.of(Component.translatable("item.cobbleloots.loot_ball.custom_texture.lore"))));
    return stack;
  }

}
