package dev.ripio.cobbleloots.item.custom;

import com.mojang.serialization.MapCodec;
import dev.ripio.cobbleloots.entity.custom.CobblelootsLootBall;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

import static dev.ripio.cobbleloots.util.CobblelootsUtils.cobblelootsText;

public class CobblelootsLootBallItem extends Item {
  private static final MapCodec<EntityType<?>> ENTITY_TYPE_FIELD_CODEC;
  private final EntityType<?> defaultType;

  public CobblelootsLootBallItem(Properties properties, EntityType<? extends LivingEntity> lootBallEntityType) {
    super(properties);
    this.defaultType = lootBallEntityType;
  }

  @Override
  public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
    list.add(cobblelootsText("item.cobbleloots.loot_ball.tooltip.1").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
    list.add(cobblelootsText("item.cobbleloots.loot_ball.tooltip.2").withStyle(ChatFormatting.BOLD, ChatFormatting.RED));
  }

  @Override
  public @NotNull InteractionResult useOn(UseOnContext useOnContext) {
    Level level = useOnContext.getLevel();
    BlockPlaceContext blockPlaceContext = new BlockPlaceContext(useOnContext);
    BlockPos blockPos = blockPlaceContext.getClickedPos();
    Direction direction = useOnContext.getClickedFace();
    ItemStack itemStack = useOnContext.getItemInHand();
    BlockState blockState = level.getBlockState(blockPos);
    BlockPos blockPos2;
    if (blockState.getCollisionShape(level, blockPos).isEmpty()) {
      blockPos2 = blockPos;
    } else {
      blockPos2 = blockPos.relative(direction);
    }

    if (level instanceof ServerLevel serverLevel) {
      Consumer<CobblelootsLootBall> consumer = EntityType.createDefaultStackConfig(serverLevel, itemStack, useOnContext.getPlayer());
      EntityType<CobblelootsLootBall> entityType = (EntityType<CobblelootsLootBall>) this.getLootBallType(itemStack);
      CobblelootsLootBall cobblelootsLootBall = entityType.create(serverLevel, consumer, blockPos2, MobSpawnType.SPAWN_EGG, true, true);

      if (cobblelootsLootBall == null) {
        return InteractionResult.FAIL;
      }

      float f = (float) Mth.floor((Mth.wrapDegrees(useOnContext.getRotation() - 180.0F) + 22.5F) / 45.0F) * 45.0F;
      cobblelootsLootBall.moveTo(blockPos2.getX() + 0.5F, blockPos2.getY(), blockPos2.getZ() + 0.5F, f, 0.0F);
      serverLevel.addFreshEntityWithPassengers(cobblelootsLootBall);
      level.playSound(null, cobblelootsLootBall.getX(), cobblelootsLootBall.getY(), cobblelootsLootBall.getZ(), SoundEvents.LANTERN_PLACE, SoundSource.BLOCKS, 0.75F, 0.8F);
      cobblelootsLootBall.gameEvent(GameEvent.ENTITY_PLACE, useOnContext.getPlayer());
    }

    itemStack.shrink(1);

    return InteractionResult.sidedSuccess(level.isClientSide);
  }

  public EntityType<?> getLootBallType(ItemStack itemStack) {
    CustomData customData = itemStack.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY);
    return !customData.isEmpty() ? customData.read(ENTITY_TYPE_FIELD_CODEC).result().orElse(this.defaultType) : this.defaultType;
  }

  static {
    ENTITY_TYPE_FIELD_CODEC = BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("id");
  }
}
