package dev.ripio.cobbleloots.entity.custom;

import dev.ripio.cobbleloots.data.CobblelootsDataProvider;
import dev.ripio.cobbleloots.data.lootball.CobblelootsLootBallData;
import dev.ripio.cobbleloots.sound.CobblelootsLootBallSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CobblelootsLootBall extends CobblelootsBaseContainerEntity {
  // Inventory
  private static final int CONTAINER_SIZE = 1;
  private final NonNullList<ItemStack> itemStacks = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);

  // Animations
  public final AnimationState openingAnimationState = new AnimationState();
  private static final int LOOT_BALL_OPENING_TICKS = 50;
  private static final int LOOT_BALL_OPENING_DROP_TICK = 25;

  // Opening logic
  private boolean isOpening = false;
  private ServerPlayer pendingOpener = null;
  private static final EntityDataAccessor<Integer> OPENING_TICKS = SynchedEntityData.defineId(CobblelootsLootBall.class, EntityDataSerializers.INT);

  // Synched Entity Data (Server <-> Client)
  private static final EntityDataAccessor<Boolean> SPARKS = SynchedEntityData.defineId(CobblelootsLootBall.class, EntityDataSerializers.BOOLEAN);
  private static final EntityDataAccessor<Boolean> INVISIBLE = SynchedEntityData.defineId(CobblelootsLootBall.class, EntityDataSerializers.BOOLEAN);
  private static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(CobblelootsLootBall.class, EntityDataSerializers.STRING);
  private static final EntityDataAccessor<String> LOOT_BALL_DATA = SynchedEntityData.defineId(CobblelootsLootBall.class, EntityDataSerializers.STRING);
  private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(CobblelootsLootBall.class, EntityDataSerializers.INT);

  // NBT Tags
  private static final String TAG_SPARKS = "Sparks";
  private static final String TAG_INVISIBLE = "Invisible";
  private static final String TAG_TEXTURE = "Texture";
  private static final String TAG_LOOT_BALL_DATA = "LootBallData";
  private static final String TAG_VARIANT = "Variant";

  private static final String TAG_OPENERS = "Openers";
  private static final String TAG_USES = "Uses";
  private static final String TAG_MULTIPLIER = "Multiplier";

  protected final Map<UUID, Long> openers = new HashMap<>();
  protected int uses = 1;
  protected float multiplier = 1.0f;

  // --- Constructors --

  public CobblelootsLootBall(EntityType<? extends LivingEntity> entityType, Level level) {
    super(entityType, level);
  }

  public static AttributeSupplier.Builder createAttributes() {
    return Animal.createMobAttributes().add(Attributes.GRAVITY, 0.1d);
  }

  // --- Entity methods ---

  @Override
  public boolean fireImmune() {
    return true;
  }

  @Override
  public void setYRot(float f) {
    super.setYRot(f);
    super.setYHeadRot(f);
    super.setYBodyRot(f);
  }

  @Override
  public boolean canSpawnSprintParticle() {
    return false;
  }

  @Override
  public void setInvisible(boolean bl) {
    super.setInvisible(bl);
    this.entityData.set(INVISIBLE, bl);
  }

  @Override
  public @NotNull InteractionResult interact(Player player, InteractionHand interactionHand) {
    if (!this.level().isClientSide()) {
      // Server side
      if (player instanceof ServerPlayer serverPlayer) {
        ItemStack itemStack = serverPlayer.getItemInHand(interactionHand).copy();

        // Game mode check
        if (serverPlayer.isCreative()) {
          // Creative mode
          if (itemStack.isEmpty()) {
            // Empty hand
            this.toggleVisibility(serverPlayer);
          } else if (itemStack.is(Items.HONEYCOMB) && this.isInvisible()) {
            // Honeycomb and invisible
            this.toggleSparks(serverPlayer);
          } else {
            // Other item
            this.setLootBallItem(itemStack, serverPlayer);
          }
        } else if (!serverPlayer.isSpectator()) {
          // Survival/Adventure mode
          this.tryOpen(serverPlayer);
        }

      }
    }

    this.setChanged();

    return InteractionResult.SUCCESS;
  }

  // --- LivingEntity methods ---

  @Override
  public boolean hurt(DamageSource damageSource, float f) {
    Entity attacker = damageSource.getEntity();
    if (attacker instanceof Player player) {
      this.lastHurtByPlayerTime = 100;
      if (!this.level().isClientSide()) {
        if (player instanceof ServerPlayer serverPlayer) {
          ItemStack itemStack = serverPlayer.getMainHandItem();
          // Check if hand is empty
          if (itemStack.isEmpty()) {
            if (!serverPlayer.isCreative()) {
              // Check if left uses are different from 0
              if (this.getRemainingUses() != 0) {
                serverPlayer.playNotifySound(SoundEvents.SHIELD_BLOCK, SoundSource.BLOCKS, 0.3f, 1.0f);
                return false;
              }
              // Check if the loot ball is empty and drop the loot
              if (!this.isEmpty()) this.spawnAtLocation(this.getItem(0));
              // Spawn the loot ball item
              this.spawnAtLocation(this.getLootBallItem());
            } else {
              // If the player is in creative mode, destroy the loot ball and drop the loot
              if (!this.isEmpty()) this.spawnAtLocation(this.getItem(0));
            }
            this.playSound(SoundEvents.ARMOR_STAND_BREAK, 0.5F, 1.0F);
            this.discard();
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  protected void defineSynchedData(SynchedEntityData.Builder builder) {
    super.defineSynchedData(builder);
    builder.define(SPARKS, true);
    builder.define(INVISIBLE, false);
    builder.define(TEXTURE, "");
    builder.define(LOOT_BALL_DATA, "");
    builder.define(VARIANT, -1);
    builder.define(OPENING_TICKS, 0);
  }

  @Override
  public void addAdditionalSaveData(CompoundTag compoundTag) {
    super.addAdditionalSaveData(compoundTag);
    if (!this.hasSparks()) compoundTag.putBoolean(TAG_SPARKS, this.hasSparks());
    if (!this.isInvisible()) compoundTag.putBoolean(TAG_INVISIBLE, this.isInvisible());
    if (!this.entityData.get(TEXTURE).isEmpty()) compoundTag.putString(TAG_TEXTURE, this.entityData.get(TEXTURE));
    if (this.getLootBallData() != null) compoundTag.putString(TAG_LOOT_BALL_DATA, this.entityData.get(LOOT_BALL_DATA));
    if (this.getVariant() != -1) compoundTag.putInt(TAG_VARIANT, this.getVariant());
    // Non-Synched Data
    if (!this.openers.isEmpty()) {
      ListTag openersTag = new ListTag();
      for (Map.Entry<UUID, Long> entry : this.openers.entrySet()) {
        CompoundTag openerTag = new CompoundTag();
        openerTag.putUUID("UUID", entry.getKey());
        openerTag.putLong("Timestamp", entry.getValue());
        openersTag.add(openerTag);
      }
      compoundTag.put(TAG_OPENERS, openersTag);
    }
    if (this.uses != 1) compoundTag.putInt(TAG_USES, this.uses);
    if (this.multiplier != 1.0f) compoundTag.putFloat(TAG_MULTIPLIER, this.multiplier);
  }

  @Override
  public void readAdditionalSaveData(CompoundTag compoundTag) {
    super.readAdditionalSaveData(compoundTag);
    if (compoundTag.contains(TAG_SPARKS)) this.setSparks(compoundTag.getBoolean(TAG_SPARKS));
    if (compoundTag.contains(TAG_INVISIBLE)) this.setInvisible(compoundTag.getBoolean(TAG_INVISIBLE));
    if (compoundTag.contains(TAG_TEXTURE)) {
      ResourceLocation textureLocation = ResourceLocation.tryParse(compoundTag.getString(TAG_TEXTURE));
      if (textureLocation != null) this.setTexture(textureLocation);
    }
    if (compoundTag.contains(TAG_LOOT_BALL_DATA)) {
      ResourceLocation lootBallDataLocation = ResourceLocation.tryParse(compoundTag.getString(TAG_LOOT_BALL_DATA));
      if (lootBallDataLocation != null) this.setLootBallData(lootBallDataLocation);
    }
    if (compoundTag.contains(TAG_VARIANT)) this.setVariant(compoundTag.getInt(TAG_VARIANT));
    // Non-Synched Data
    if (compoundTag.contains(TAG_OPENERS)) {
      ListTag openersTag = compoundTag.getList(TAG_OPENERS, CompoundTag.TAG_COMPOUND);
      for (int i = 0; i < openersTag.size(); i++) {
        CompoundTag openerTag = openersTag.getCompound(i);
        UUID uuid = openerTag.getUUID("UUID");
        long timestamp = openerTag.getLong("Timestamp");
        this.openers.put(uuid, timestamp);
      }
    }
    if (compoundTag.contains(TAG_USES)) this.uses = compoundTag.getInt(TAG_USES);
    if (compoundTag.contains(TAG_MULTIPLIER)) this.multiplier = compoundTag.getFloat(TAG_MULTIPLIER);
  }

  @Override
  public boolean isPushable() {
    return false;
  }

  @Override
  public void setYHeadRot(float f) {
    super.setYHeadRot(f);
    super.setYBodyRot(f);
  }

  @Override
  public void setYBodyRot(float f) {
    super.setYBodyRot(f);
    super.setYHeadRot(f);
  }

  @Override
  public void tick() {
    super.tick();
    this.openingTick();
  }

  @Override
  public boolean canBreatheUnderwater() {
    return true;
  }

  @Override
  public void baseTick() {
    super.baseTick();
    this.level().getProfiler().push("cobblelootsLootBallEntityBaseTick");
    if (this.isInvisible() && this.hasSparks()) {
      // Sparks
      if (this.random.nextFloat() <= 0.05f) {
        double x = this.getX() + this.random.nextGaussian() * 0.1d;
        double y = this.getY() + 0.50d + this.random.nextGaussian() * 0.1d;
        double z = this.getZ() + this.random.nextGaussian() * 0.1d;
        this.level().addParticle(ParticleTypes.ELECTRIC_SPARK, x, y, z, this.random.nextGaussian()*0.1d, this.random.nextGaussian()*0.1d, this.random.nextGaussian()*0.1d);
      }
    }
    if (this.isInWater()) {
      if (this.random.nextFloat() <= 0.05f) {
        // Water bubbles
        double x = this.getX() + this.random.nextGaussian() * 0.1d;
        double y = this.getY() + 0.50d + this.random.nextGaussian() * 0.1d;
        double z = this.getZ() + this.random.nextGaussian() * 0.1d;
        this.level().addParticle(ParticleTypes.BUBBLE_COLUMN_UP, x, y, z, this.random.nextGaussian() * 0.1d, this.random.nextGaussian() * 0.1d, this.random.nextGaussian() * 0.1d);
      }
    }
    this.level().getProfiler().pop();
  }

  // --- CobblelootsBaseContainerEntity methods ---

  @Override
  public @NotNull NonNullList<ItemStack> getItemStacks() {
    return this.itemStacks;
  }

  @Override
  public int getContainerSize() {
    return CONTAINER_SIZE;
  }

  @Override
  public @Nullable ResourceLocation getLootTableLocation() {
    ResourceLocation tableLocation = super.getLootTableLocation();
    if (tableLocation == null) {
      CobblelootsLootBallData lootBallData = this.getLootBallData();
      if (lootBallData != null) tableLocation = lootBallData.getLootTable();
    }
    return tableLocation;
  }

  // --- CobblelootsLootBall methods ---
  private void tryOpen(ServerPlayer serverPlayer) {
    // Check if someone is already opening the loot ball
    if (this.isOpening) {
      serverPlayer.sendSystemMessage(Component.translatable("entity.cobbleloots.loot_ball.error.is_opening").withStyle(ChatFormatting.RED), true);
      return;
    }
    // Check if the player is already an opener
    if (this.isOpener(serverPlayer)) {
      serverPlayer.sendSystemMessage(Component.translatable("entity.cobbleloots.loot_ball.error.already_opened").withStyle(ChatFormatting.RED), true);
      return;
    }
    // Generate loot
    this.unpackLootTable(serverPlayer);

    // Check if loot ball is empty
    if (!this.isEmpty()) {
      // Set opening animation
      this.pendingOpener = serverPlayer;
      this.isOpening = true;
      this.setOpeningTicks(LOOT_BALL_OPENING_TICKS);
    }
  }

  private void setOpeningTicks(int i) {
    this.entityData.set(OPENING_TICKS, i);
  }

  private int getOpeningTicks() {
    return this.entityData.get(OPENING_TICKS);
  }

  private void open(ServerPlayer serverPlayer) {
    // Drop items
    for (ItemStack itemStack : this.itemStacks) {
      if (!itemStack.isEmpty()) {
        this.spawnAtLocation(itemStack);
        this.addOpener(serverPlayer);
        this.playSound(CobblelootsLootBallSounds.getPopItemSound());
        serverPlayer.playNotifySound(CobblelootsLootBallSounds.getFanfare(), SoundSource.BLOCKS, 1f, 1.0f);
      }
    }
    // Give experience points
    int experiencePoints = 2;
    serverPlayer.giveExperiencePoints(experiencePoints);
    // Send message
    serverPlayer.sendSystemMessage(Component.translatable("entity.cobbleloots.loot_ball.open").withStyle(ChatFormatting.AQUA), true);
    // Clear the inventory
    this.clearContent();
    // Decrease uses
    this.setRemainingUses(this.getRemainingUses() - 1);
    // Update
    this.setChanged();
  }

  private boolean isOpener(ServerPlayer serverPlayer) {
    return this.openers.containsKey(serverPlayer.getUUID());
  }

  private void addOpener(ServerPlayer serverPlayer) {
    this.openers.put(serverPlayer.getUUID(), this.level().getGameTime());
  }

  private int getRemainingUses() {
    return this.uses;
  }

  private void setRemainingUses(int uses) {
    this.uses = uses;
  }

  private float getMultiplier() {
    return this.multiplier;
  }

  private void setMultiplier(float multiplier) {
    this.multiplier = multiplier;
  }

  public boolean hasSparks() {
    return this.entityData.get(SPARKS);
  }

  public int getVariant() {
    return this.entityData.get(VARIANT);
  }

  @Nullable
  public CobblelootsLootBallData getLootBallData() {
    ResourceLocation dataLocation = ResourceLocation.tryParse(this.entityData.get(LOOT_BALL_DATA));
    return CobblelootsDataProvider.getLootBallData(dataLocation, this.getVariant());
  }

  @Nullable
  public ResourceLocation getTexture() {
    ResourceLocation textureLocation = ResourceLocation.tryParse(this.entityData.get(TEXTURE));
    if (textureLocation == null || textureLocation.getPath().isEmpty()) {
      CobblelootsLootBallData lootBallData = this.getLootBallData();
      if (lootBallData != null) textureLocation = lootBallData.getTexture();
    }
    return textureLocation;
  }

  public void setSparks(boolean sparks) {
    this.entityData.set(SPARKS, sparks);
  }

  public void setVariant(int variant) {
    this.entityData.set(VARIANT, variant);
  }

  public void setLootBallData(ResourceLocation lootBallData) {
    this.entityData.set(LOOT_BALL_DATA, lootBallData.toString());
  }

  public void setTexture(ResourceLocation texture) {
    this.entityData.set(TEXTURE, texture.toString());
  }

  // --- Private methods ---
  private void toggleVisibility(ServerPlayer serverPlayer) {
    serverPlayer.sendSystemMessage(Component.translatable("entity.cobbleloots.loot_ball.visibility").withStyle(ChatFormatting.AQUA), true);
    serverPlayer.playNotifySound(CobblelootsLootBallSounds.getToggleInvisibilitySound(), SoundSource.BLOCKS, 0.5f, 1.0f);
    this.setInvisible(!this.isInvisible());
  }

  private void toggleSparks(ServerPlayer serverPlayer) {
    serverPlayer.sendSystemMessage(Component.translatable("entity.cobbleloots.loot_ball.sparks").withStyle(ChatFormatting.AQUA), true);
    serverPlayer.playNotifySound(CobblelootsLootBallSounds.getToggleSparksSound(this.hasSparks()), SoundSource.BLOCKS, 0.5f, 1.0f);
    this.setSparks(!this.hasSparks());
  }

  private void setLootBallItem(ItemStack itemStack, ServerPlayer serverPlayer) {
    this.setItem(0, itemStack);
    if (!serverPlayer.isCreative()) serverPlayer.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
    serverPlayer.sendSystemMessage(Component.translatable("entity.cobbleloots.loot_ball.item").withStyle(ChatFormatting.AQUA), true);
    serverPlayer.playNotifySound(CobblelootsLootBallSounds.getSetItemSound(), SoundSource.BLOCKS, 0.5f, 1.0f);
    this.setChanged();
  }

  private ItemStack getLootBallItem() {
    return ItemStack.EMPTY;
  }

  private void openingTick() {
    if (this.level().isClientSide()) {
      // Client-Logic
      if (this.getOpeningTicks() > 0 && !this.openingAnimationState.isStarted()) {
        this.openingAnimationState.start(this.tickCount);
      } else if (this.getOpeningTicks() == 0 && this.openingAnimationState.isStarted()) {
        this.openingAnimationState.stop();
      }
    } else {
      // Server-Logic
      if (this.getOpeningTicks() == LOOT_BALL_OPENING_TICKS) {
        this.setOpeningTicks(this.getOpeningTicks() - 1);
        this.playSound(CobblelootsLootBallSounds.getLidOpenSound());
      } else if (this.getOpeningTicks() > LOOT_BALL_OPENING_DROP_TICK) {
        this.setOpeningTicks(this.getOpeningTicks() - 1);
      } else if (this.getOpeningTicks() == LOOT_BALL_OPENING_DROP_TICK) {
        this.open(this.pendingOpener);
        this.setOpeningTicks(this.getOpeningTicks() - 1);
      } else if (this.getOpeningTicks() > 0) {
        this.setOpeningTicks(this.getOpeningTicks() - 1);
      } else if (this.getOpeningTicks() == 0 && this.pendingOpener != null) {
        this.pendingOpener = null;
        this.isOpening = false;
        this.playSound(CobblelootsLootBallSounds.getLidCloseSound());
      }
    }
  }
}
