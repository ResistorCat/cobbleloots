package dev.ripio.cobbleloots.entity.custom;

import dev.ripio.cobbleloots.config.CobblelootsConfig;
import dev.ripio.cobbleloots.data.CobblelootsDataProvider;
import dev.ripio.cobbleloots.data.custom.CobblelootsLootBallData;
import dev.ripio.cobbleloots.item.CobblelootsItems;
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

import static dev.ripio.cobbleloots.util.CobblelootsUtils.cobblelootsText;

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
  private boolean wasInvisible = false;
  private static final EntityDataAccessor<Integer> OPENING_TICKS = SynchedEntityData.defineId(CobblelootsLootBall.class, EntityDataSerializers.INT);

  // Synched Entity Data (Server <-> Client)
  private static final EntityDataAccessor<Boolean> SPARKS = SynchedEntityData.defineId(CobblelootsLootBall.class, EntityDataSerializers.BOOLEAN);
  private static final EntityDataAccessor<Boolean> INVISIBLE = SynchedEntityData.defineId(CobblelootsLootBall.class, EntityDataSerializers.BOOLEAN);
  private static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(CobblelootsLootBall.class, EntityDataSerializers.STRING);
  private static final EntityDataAccessor<String> LOOT_BALL_DATA = SynchedEntityData.defineId(CobblelootsLootBall.class, EntityDataSerializers.STRING);
  private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(CobblelootsLootBall.class, EntityDataSerializers.INT);

  private static final EntityDataAccessor<CompoundTag> LOOT_BALL_CLIENT_DATA = SynchedEntityData.defineId(CobblelootsLootBall.class, EntityDataSerializers.COMPOUND_TAG);

  // NBT Tags
  private static final String TAG_SPARKS = "Sparks";
  private static final String TAG_INVISIBLE = "Invisible";
  private static final String TAG_TEXTURE = "Texture";
  private static final String TAG_LOOT_BALL_DATA = "LootBallData";
  private static final String TAG_VARIANT = "Variant";

  private static final String TAG_OPENERS = "Openers";
  private static final String TAG_USES = "Uses";
  private static final String TAG_MULTIPLIER = "Multiplier";
  private static final String TAG_DESPAWN_TICK = "DespawnTick";
  private static final String TAG_PLAYER_TIMER = "PlayerTimer";

  // Text keys
  private static final String TEXT_ERROR_IS_OPENING = "entity.cobbleloots.loot_ball.error.is_opening";
  private static final String TEXT_ERROR_ALREADY_OPENED = "entity.cobbleloots.loot_ball.error.already_opened";
  private static final String TEXT_ERROR_COOLDOWN = "entity.cobbleloots.loot_ball.error.cooldown";
  private static final String TEXT_INFO_NO_LOOT = "entity.cobbleloots.loot_ball.info.no_loot";
  private static final String TEXT_OPEN_SUCCESS = "entity.cobbleloots.loot_ball.open.success";
  private static final String TEXT_OPEN_SUCCESS_BONUS = "entity.cobbleloots.loot_ball.open.success.bonus";
  private static final String TEXT_SET_ITEM = "entity.cobbleloots.loot_ball.set.item";
  private static final String TEXT_TOGGLE_VISIBILITY = "entity.cobbleloots.loot_ball.toggle.visibility";
  private static final String TEXT_TOGGLE_SPARKS = "entity.cobbleloots.loot_ball.toggle.sparks";
  private static final String TEXT_TOOLTIP = "entity.cobbleloots.loot_ball.tooltip";

  // Game balance constants
  private static final float DEFAULT_MULTIPLIER = CobblelootsConfig.getFloatConfig(CobblelootsConfig.LOOT_BALL_DEFAULTS_MULTIPLIER);
  private static final int DEFAULT_USES = CobblelootsConfig.getIntConfig(CobblelootsConfig.LOOT_BALL_DEFAULTS_USES);
  private static final long DEFAULT_DESPAWN_TICK = CobblelootsConfig.getLongConfig(CobblelootsConfig.LOOT_BALL_DEFAULTS_DESPAWN_TICK);
  private static final long DEFAULT_PLAYER_TIMER = CobblelootsConfig.getLongConfig(CobblelootsConfig.LOOT_BALL_DEFAULTS_PLAYER_TIMER);

  // Variables
  protected final Map<UUID, Long> openers = new HashMap<>();
  protected int uses = DEFAULT_USES;
  protected float multiplier = DEFAULT_MULTIPLIER;
  protected long despawnTick = DEFAULT_DESPAWN_TICK;
  protected long playerTimer = DEFAULT_PLAYER_TIMER;

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
  protected void updateInvisibilityStatus() {
    this.setInvisible(this.isInvisible());
  }

  @Override
  public void setInvisible(boolean bl) {
    this.entityData.set(INVISIBLE, bl);
    super.setInvisible(bl);
  }

  @Override
  public boolean isInvisible() {
    return this.entityData.get(INVISIBLE);
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
          } else if (itemStack.is(CobblelootsItems.getLootBallItem())) {
            // Loot ball item
            this.showDebugInfo(serverPlayer);
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
    // In survival mode, we should also drop the loot ball item itself
    if (!serverPlayer.isCreative() && CobblelootsConfig.getBooleanConfig(CobblelootsConfig.LOOT_BALL_SURVIVAL_DROP_ENABLED)) {
      this.spawnAtLocation(this.getSurvivalLootBallItem());
            }

    // Play breaking sound and remove the entity
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

    builder.define(LOOT_BALL_CLIENT_DATA, new CompoundTag());
  }

  @Override
  public void addAdditionalSaveData(CompoundTag compoundTag) {
    super.addAdditionalSaveData(compoundTag);
    compoundTag.putBoolean(TAG_SPARKS, this.hasSparks());
    compoundTag.putBoolean(TAG_INVISIBLE, this.isInvisible());
    // Only save non-default values to keep the tag small
    if (this.uses != DEFAULT_USES) {
      compoundTag.putInt(TAG_USES, this.uses);
    }

    if (this.multiplier != DEFAULT_MULTIPLIER) {
      compoundTag.putFloat(TAG_MULTIPLIER, this.multiplier);
    }

    if (this.despawnTick != DEFAULT_DESPAWN_TICK) {
      compoundTag.putLong(TAG_DESPAWN_TICK, this.despawnTick);
    }

    if (this.playerTimer != DEFAULT_PLAYER_TIMER) {
      compoundTag.putLong(TAG_PLAYER_TIMER, this.playerTimer);
    }
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
    // Non-Synced Data
    if (compoundTag.contains(TAG_OPENERS)) {
      ListTag openersTag = compoundTag.getList(TAG_OPENERS, CompoundTag.TAG_COMPOUND);
      for (int i = 0; i < openersTag.size(); i++) {
        CompoundTag openerTag = openersTag.getCompound(i);
    this.uses = compoundTag.contains(TAG_USES) ?
                compoundTag.getInt(TAG_USES) : DEFAULT_USES;

    this.multiplier = compoundTag.contains(TAG_MULTIPLIER) ?
                      compoundTag.getFloat(TAG_MULTIPLIER) : DEFAULT_MULTIPLIER;

    this.despawnTick = compoundTag.contains(TAG_DESPAWN_TICK) ?
                       compoundTag.getLong(TAG_DESPAWN_TICK) : DEFAULT_DESPAWN_TICK;

    this.playerTimer = compoundTag.contains(TAG_PLAYER_TIMER) ?
                       compoundTag.getLong(TAG_PLAYER_TIMER) : DEFAULT_PLAYER_TIMER;
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

    // Server logic
    if (!this.level().isClientSide()) {
      // Despawn logic
      if (this.getDespawnTick() != 0 && this.level().getGameTime() >= this.getDespawnTick()) {
        this.discard();
      }
    }
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
      serverPlayer.sendSystemMessage(cobblelootsText(TEXT_ERROR_IS_OPENING).withStyle(ChatFormatting.RED), true);
      return;
    }
    // Check if the player is already an opener
    if (this.isOpener(serverPlayer)) {
      serverPlayer.sendSystemMessage(cobblelootsText(TEXT_ERROR_ALREADY_OPENED).withStyle(ChatFormatting.RED), true);
      return;
    }
    // Check for cooldown timer
    if (this.getPlayerTimer() > 0) {
      long lastOpenTime = this.openers.getOrDefault(serverPlayer.getUUID(), 0L);
      long timeDiff = this.level().getGameTime() - lastOpenTime;

      // If still in cooldown, show remaining time
      if (timeDiff < this.getPlayerTimer()) {
        long remainingSeconds = (long) Math.ceil((this.getPlayerTimer() - timeDiff) / 20.0f);
        serverPlayer.sendSystemMessage(
            cobblelootsText(TEXT_ERROR_COOLDOWN, String.valueOf(remainingSeconds))
                .withStyle(ChatFormatting.RED),
            true);
        return false;
      } else {
        return true;
      }
    }
  }

  private void setOpeningTicks(int i) {
    this.entityData.set(OPENING_TICKS, i);
  }

  private int getOpeningTicks() {
    return this.entityData.get(OPENING_TICKS);
  }

  private void open(ServerPlayer serverPlayer) {// Give or drop items
    for (ItemStack itemStack : this.itemStacks) {
      if (!itemStack.isEmpty()) {
        if (this.getMultiplier() > 1.0f) {
          int count = itemStack.getCount();
          itemStack.setCount((int) Math.ceil(count * this.getMultiplier()));
          serverPlayer.sendSystemMessage(cobblelootsText(TEXT_OPEN_SUCCESS_BONUS, String.valueOf(this.getMultiplier()), itemStack.getHoverName().getString(), itemStack.getCount()).withStyle(ChatFormatting.AQUA), true);
        } else {
          serverPlayer.sendSystemMessage(cobblelootsText(TEXT_OPEN_SUCCESS, itemStack.getHoverName().getString(), itemStack.getCount()).withStyle(ChatFormatting.AQUA), true);
        }
        serverPlayer.getInventory().placeItemBackInInventory(itemStack);
        this.addOpener(serverPlayer);
        this.playSound(CobblelootsLootBallSounds.getPopItemSound());
        serverPlayer.playNotifySound(CobblelootsLootBallSounds.getFanfare(), SoundSource.BLOCKS, 1f, 1.0f);
      }
    }
    // Give experience points if enabled
    if (CobblelootsConfig.getBooleanConfig(CobblelootsConfig.LOOT_BALL_XP_ENABLED)) {
      int experiencePoints = CobblelootsConfig.getIntConfig(CobblelootsConfig.LOOT_BALL_XP_AMOUNT);
      serverPlayer.giveExperiencePoints(experiencePoints);
    }

    // Check if uses are infinite
    if (!this.isInfinite()) {
      // Clear inventory
      this.clearContent();
      // Decrease uses
      this.setRemainingUses(this.getRemainingUses() - 1);
    }

    // Update
    this.setChanged();
  }

  public boolean isOpener(ServerPlayer serverPlayer) {
    return this.openers.containsKey(serverPlayer.getUUID());
  }

  public void addOpener(ServerPlayer serverPlayer) {
    this.openers.put(serverPlayer.getUUID(), this.level().getGameTime());
  }

  public int getRemainingUses() {
    return this.uses;
  }

  public void setRemainingUses(int uses) {
    this.uses = uses;
  }

  public float getMultiplier() {
    return this.multiplier;
  }

  public void setMultiplier(float multiplier) {
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
    if (textureLocation != null && textureLocation.getPath().isEmpty()) {
      if (this.entityData.get(LOOT_BALL_CLIENT_DATA).contains(TAG_TEXTURE)) {
        String texturePath = this.entityData.get(LOOT_BALL_CLIENT_DATA).getString(TAG_TEXTURE);
        if (!texturePath.isEmpty()) textureLocation = ResourceLocation.tryParse(texturePath);
      } else {
        CobblelootsLootBallData lootBallData = this.getLootBallData();
        if (lootBallData != null) textureLocation = lootBallData.getTexture();
      }
    }
    return textureLocation;
  }

  public void setSparks(boolean sparks) {
    this.entityData.set(SPARKS, sparks);
  }

  public void setVariant(int variant) {
    this.entityData.set(VARIANT, variant);
    CompoundTag compoundTag = this.entityData.get(LOOT_BALL_CLIENT_DATA).copy();
    CobblelootsLootBallData lootBallData = this.getLootBallData();
    if (lootBallData != null) {
      compoundTag.putString(TAG_TEXTURE, lootBallData.getTexture().toString());
    }
    this.entityData.set(LOOT_BALL_CLIENT_DATA, compoundTag);
  }

  public void setLootBallData(ResourceLocation lootBallData) {
    this.entityData.set(LOOT_BALL_DATA, lootBallData.toString());
    CobblelootsLootBallData data = getLootBallData();
    if (data != null) {
      CompoundTag tag = new CompoundTag();
      tag.putString(TAG_TEXTURE, data.getTexture().toString());
      this.entityData.set(LOOT_BALL_CLIENT_DATA, tag);
    }
  }

  public void setTexture(ResourceLocation texture) {
    this.entityData.set(TEXTURE, texture.toString());
  }

  public void setDespawnTick(long tick) {
    this.despawnTick = tick;
  }

  public long getDespawnTick() {
    return this.despawnTick;
  }

  public boolean isInfinite() {
    return this.uses <= -1;
  }

  // --- Private methods ---
  private void toggleVisibility(ServerPlayer serverPlayer) {
    serverPlayer.sendSystemMessage(cobblelootsText(TEXT_TOGGLE_VISIBILITY).withStyle(ChatFormatting.AQUA), true);
    serverPlayer.playNotifySound(CobblelootsLootBallSounds.getToggleInvisibilitySound(), SoundSource.BLOCKS, 0.5f, 1.0f);
    this.setInvisible(!this.isInvisible());
  }

  private void toggleSparks(ServerPlayer serverPlayer) {
    serverPlayer.sendSystemMessage(cobblelootsText(TEXT_TOGGLE_SPARKS).withStyle(ChatFormatting.AQUA), true);
    serverPlayer.playNotifySound(CobblelootsLootBallSounds.getToggleSparksSound(this.hasSparks()), SoundSource.BLOCKS, 0.5f, 1.0f);
    this.setSparks(!this.hasSparks());
  }

  private void showDebugInfo(ServerPlayer serverPlayer) {
    String lootBallDebugInfo = """
        Loot Ball Debug Info:
        - Variant: %d
        - Texture: %s
        - Loot Ball Data: %s
        - Sparks: %s
        - Invisible: %s
        - Opening: %s
        - Uses: %d
        - Multiplier: %.2f
        - Openers: %s
        """.formatted(this.getVariant(), this.getTexture(), this.entityData.get(LOOT_BALL_DATA), this.hasSparks(), this.isInvisible(), this.isOpening, this.getRemainingUses(), this.getMultiplier(), this.openers);
    serverPlayer.sendSystemMessage(Component.literal(lootBallDebugInfo));
  }

  private void setLootBallItem(ItemStack itemStack, ServerPlayer serverPlayer) {
    this.setItem(0, itemStack);
    if (!serverPlayer.isCreative()) serverPlayer.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
    serverPlayer.sendSystemMessage(cobblelootsText(TEXT_SET_ITEM, itemStack.getHoverName().getString(), String.valueOf(itemStack.getCount())).withStyle(ChatFormatting.AQUA), true);
    serverPlayer.playNotifySound(CobblelootsLootBallSounds.getSetItemSound(), SoundSource.BLOCKS, 0.5f, 1.0f);
    this.setChanged();
  }

  /**
   * Creates an item stack that represents this loot ball in survival inventory.
   * This preserves important properties like loot table and variant.
   *
   * @return ItemStack representing this loot ball
   */
  private ItemStack getSurvivalLootBallItem() {
    // Create base item
    ItemStack lootBallItem = new ItemStack(CobblelootsItems.getLootBallItem());
    CompoundTag tag = createLootBallItemTag();

    // Only set custom data component if we have data to save
    if (!tag.isEmpty()) {
      lootBallItem.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    return lootBallItem;
  }

  /**
   * Creates the tag containing all data needed to reconstruct this loot ball.
   * This is just for decorative purposes in survival mode.
   */
  private CompoundTag createLootBallItemTag() {
    CompoundTag tag = new CompoundTag();

    // Save important identification data
    addNonEmptyString(tag, TAG_LOOT_BALL_DATA_ID, this.getLootBallDataId());
    addNonEmptyString(tag, TAG_VARIANT_ID, this.getVariantId());
    addNonEmptyString(tag, TAG_CUSTOM_TEXTURE, this.getEntityData().get(CUSTOM_TEXTURE));

    // Set default visibility properties for dropped items
    tag.putBoolean(TAG_SPARKS, false);
    tag.putBoolean(TAG_INVISIBLE, false);

    // Set remaining uses to 0 as this is a dropped item
    tag.putInt(TAG_USES, 0);

    // Reset multiplier and timers
    tag.putFloat(TAG_MULTIPLIER, 1.0f);
    tag.putLong(TAG_DESPAWN_TICK, 0);
    tag.putLong(TAG_PLAYER_TIMER, 0);

    // Set XP to 0
    tag.putInt(TAG_XP, 0);

    return tag;
  }

  /**
   * Helper to only add non-empty strings to tags
   */
  private void addNonEmptyString(CompoundTag tag, String key, String value) {
    if (value != null && !value.isEmpty()) {
      tag.putString(key, value);
    }
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
        this.setInvisible(this.wasInvisible);
        this.playSound(CobblelootsLootBallSounds.getLidCloseSound());
      }
    }
  }
}
