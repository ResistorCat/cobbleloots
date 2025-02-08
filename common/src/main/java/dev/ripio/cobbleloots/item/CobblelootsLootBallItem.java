package dev.ripio.cobbleloots.item;

import com.mojang.serialization.MapCodec;
import dev.ripio.cobbleloots.entity.custom.CobblelootsLootBall;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class CobblelootsLootBallItem extends Item {
  private static final MapCodec<EntityType<?>> ENTITY_TYPE_FIELD_CODEC;
  private final EntityType<?> defaultType;

  public CobblelootsLootBallItem(Properties properties, EntityType<? extends LivingEntity> lootBallEntityType) {
    super(properties);
    this.defaultType = lootBallEntityType;
  }

  @Override
  public @NotNull InteractionResult useOn(UseOnContext useOnContext) {
    Level level = useOnContext.getLevel();
    BlockPlaceContext blockPlaceContext = new BlockPlaceContext(useOnContext);
    BlockPos blockPos = blockPlaceContext.getClickedPos();
    Direction direction = useOnContext.getClickedFace();
    ItemStack itemStack = useOnContext.getItemInHand();
    //Vec3 vec3 = Vec3.atBottomCenterOf(blockPos);
    BlockState blockState = level.getBlockState(blockPos);
    BlockPos blockPos2;
    if (blockState.getCollisionShape(level, blockPos).isEmpty()) {
      blockPos2 = blockPos;
    } else {
      blockPos2 = blockPos.relative(direction);
    }
    //EntityType<?> entityType = this.getLootBallType(itemStack);

    //if (entityType.spawn((ServerLevel) level, itemStack, useOnContext.getPlayer(), blockPos2, MobSpawnType.SPAWN_EGG, true, !Objects.equals(blockPos, blockPos2) && direction == Direction.UP) != null) {
    //  itemStack.shrink(1);
    //  level.gameEvent(useOnContext.getPlayer(), GameEvent.ENTITY_PLACE, blockPos);
    //}

    if (level instanceof ServerLevel serverLevel) {
      Consumer<CobblelootsLootBall> consumer = EntityType.createDefaultStackConfig(serverLevel, itemStack, useOnContext.getPlayer());
      EntityType<CobblelootsLootBall> entityType = (EntityType<CobblelootsLootBall>) this.getLootBallType(itemStack);
      CobblelootsLootBall cobblelootsLootBall = entityType.create(serverLevel, consumer, blockPos2, MobSpawnType.SPAWN_EGG, true, true);

      if (cobblelootsLootBall == null) {
        return InteractionResult.FAIL;
      }

      float f = (float) Mth.floor((Mth.wrapDegrees(useOnContext.getRotation() - 180.0F) + 22.5F) / 45.0F) * 45.0F;
      //Cobbleloots.LOGGER.info(String.valueOf(f));
      //float f = (float) Mth.wrapDegrees(useOnContext.getRotation() - 180.0F) + 22.5F;
      //cobblelootsLootBall.moveTo(cobblelootsLootBall.getX(), cobblelootsLootBall.getY(), cobblelootsLootBall.getZ(), (float) Mth.wrapDegrees(0.0F), 0.0F);
      //cobblelootsLootBall.setYHeadRot(f);
      //Cobbleloots.LOGGER.info(String.valueOf(cobblelootsLootBall.getLookAngle()));
      cobblelootsLootBall.moveTo(blockPos2.getX() + 0.5F, blockPos2.getY(), blockPos2.getZ() + 0.5F, f, 0.0F);
      serverLevel.addFreshEntityWithPassengers(cobblelootsLootBall);
      level.playSound(null, cobblelootsLootBall.getX(), cobblelootsLootBall.getY(), cobblelootsLootBall.getZ(), SoundEvents.LANTERN_PLACE, SoundSource.BLOCKS, 0.75F, 0.8F);
      cobblelootsLootBall.gameEvent(GameEvent.ENTITY_PLACE, useOnContext.getPlayer());
    }

    itemStack.shrink(1);

    return InteractionResult.sidedSuccess(level.isClientSide);
    //Consumer<ArmorStand> consumer2 = EntityType.createDefaultStackConfig(serverLevel, itemStack, useOnContext.getPlayer());
    //ArmorStand armorStand = (ArmorStand)EntityType.ARMOR_STAND.create(serverLevel, consumer2, blockPos, MobSpawnType.SPAWN_EGG, true, true);
    //if (armorStand == null) {
    //  return InteractionResult.FAIL;
    //}

    //float f = (float) Mth.floor((Mth.wrapDegrees(useOnContext.getRotation() - 180.0F) + 22.5F) / 45.0F) * 45.0F;
    //armorStand.moveTo(armorStand.getX(), armorStand.getY(), armorStand.getZ(), f, 0.0F);
    //serverLevel.addFreshEntityWithPassengers(armorStand);
    //level.playSound((Player)null, armorStand.getX(), armorStand.getY(), armorStand.getZ(), SoundEvents.ARMOR_STAND_PLACE, SoundSource.BLOCKS, 0.75F, 0.8F);
    //armorStand.gameEvent(GameEvent.ENTITY_PLACE, useOnContext.getPlayer());

    //itemStack.shrink(1);
    //return InteractionResult.sidedSuccess(level.isClientSide);

    //return InteractionResult.FAIL;

    //return InteractionResult.CONSUME;
  }

  public EntityType<?> getLootBallType(ItemStack itemStack) {
    CustomData customData = itemStack.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY);
    return !customData.isEmpty() ? customData.read(ENTITY_TYPE_FIELD_CODEC).result().orElse(this.defaultType) : this.defaultType;
  }

  static {
    ENTITY_TYPE_FIELD_CODEC = BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("id");
  }
}
