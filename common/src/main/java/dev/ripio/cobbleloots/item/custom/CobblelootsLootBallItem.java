package dev.ripio.cobbleloots.item.custom;

import dev.ripio.cobbleloots.entity.custom.CobblelootsLootBall;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.ripio.cobbleloots.entity.CobblelootsEntities.getLootBallEntityType;
import static dev.ripio.cobbleloots.util.CobblelootsUtils.cobblelootsText;

public class CobblelootsLootBallItem extends Item {
  public CobblelootsLootBallItem(Item.Properties properties) {
    super(properties);
  }

  @Override
  public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, List<Component> list,
      TooltipFlag tooltipFlag) {
    // Add the default tooltip for the loot ball item
    list.add(cobblelootsText("item.cobbleloots.loot_ball.tooltip.1").withStyle(ChatFormatting.GRAY));
  }

  @Override
  public @NotNull InteractionResult useOn(UseOnContext useOnContext) {
    Level level = useOnContext.getLevel();

    if (!this.placeLootBall(useOnContext)) {
      return InteractionResult.FAIL;
    }

    return InteractionResult.sidedSuccess(level.isClientSide());
  }
  

  /*
   * Places a loot ball entity at the clicked position in the world.
   * @param useOnContext The context of the use action, containing player and clicked position.
   * @return true if the loot ball was successfully placed, false otherwise.
   * This method creates a new loot ball entity, sets its position and rotation based on the click location,
   * and adds it to the world. It also plays a sound effect and triggers a game event for placing the entity.
   * If the item stack has custom data, it reads that data into the loot ball entity.
   * The item stack is then reduced by one to reflect the use of the item.
   */
  private boolean placeLootBall(UseOnContext useOnContext) {
    Level level = useOnContext.getLevel();
    if (level instanceof ServerLevel serverLevel) {
      // Get the loot ball entity type
      EntityType<CobblelootsLootBall> entityType = getLootBallEntityType();

      // Create the loot ball entity
      CobblelootsLootBall lootBall = entityType.create(serverLevel);
      if (lootBall == null) {
        return false; // Failed to create loot ball entity
      }

      // Set the position and rotation of the loot ball
      lootBall.moveTo(
          useOnContext.getClickLocation(),
          Mth.wrapDegrees(useOnContext.getRotation() - 180.0F),
          0.0F);

      // Place the loot ball in the world
      level.addFreshEntity(lootBall);
      level.playSound(null, lootBall.getX(), lootBall.getY(), lootBall.getZ(), SoundEvents.LANTERN_PLACE,
          SoundSource.BLOCKS, 0.75F, 0.8F);

      // Game event for placing the loot ball
      lootBall.gameEvent(GameEvent.ENTITY_PLACE, useOnContext.getPlayer());

      // Set loot ball data from item stack if available
      if (useOnContext.getItemInHand().has(DataComponents.CUSTOM_DATA)) {
        CustomData customData = useOnContext.getItemInHand().getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        lootBall.readAdditionalSaveData(customData.copyTag());
      }

      // Remove the item from the player's inventory
      useOnContext.getItemInHand().shrink(1);
  }

    return false;
  }
}
