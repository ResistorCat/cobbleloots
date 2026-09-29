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
