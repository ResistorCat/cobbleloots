package dev.ripio.cobbleloots.entity.custom;

import dev.ripio.cobbleloots.config.CobblelootsConfig;
import dev.ripio.cobbleloots.config.CobblelootsLootBallEmptyBehavior;
import dev.ripio.cobbleloots.data.CobblelootsDataProvider;
import dev.ripio.cobbleloots.data.custom.CobblelootsLootBallData;
import dev.ripio.cobbleloots.data.custom.CobblelootsLootBallVariantData;
import dev.ripio.cobbleloots.item.CobblelootsItems;
import dev.ripio.cobbleloots.sound.CobblelootsLootBallSounds;
import dev.ripio.cobbleloots.util.CobblelootsDefinitions;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.item.component.CustomData;
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
  public static final int LOOT_BALL_OPENING_TICKS = 50;
  private static final int LOOT_BALL_OPENING_DROP_TICK = 25;

  // Particle effects
  private static final float PARTICLE_SPAWN_CHANCE = 0.05f;
  private static final double PARTICLE_OFFSET_MULTIPLIER = 0.1d;
  private static final double PARTICLE_Y_FIXED_OFFSET = 0.50d;
  private static final double PARTICLE_VELOCITY_MULTIPLIER = 0.1d;

  // Opening logic
  private boolean isOpening = false;
  private ServerPlayer pendingOpener = null;
  private boolean wasInvisible = false;
  private static final EntityDataAccessor<Integer> OPENING_TICKS = SynchedEntityData.defineId(CobblelootsLootBall.class,
      EntityDataSerializers.INT);

  // Synched Entity Data (Server <-> Client)
  private static final EntityDataAccessor<Boolean> SPARKS = SynchedEntityData.defineId(CobblelootsLootBall.class,
      EntityDataSerializers.BOOLEAN);
  private static final EntityDataAccessor<Boolean> INVISIBLE = SynchedEntityData.defineId(CobblelootsLootBall.class,
      EntityDataSerializers.BOOLEAN);
  private static final EntityDataAccessor<String> CUSTOM_TEXTURE = SynchedEntityData.defineId(CobblelootsLootBall.class,
      EntityDataSerializers.STRING);
  private static final EntityDataAccessor<String> LOOT_BALL_DATA_ID = SynchedEntityData
      .defineId(CobblelootsLootBall.class, EntityDataSerializers.STRING);
  private static final EntityDataAccessor<String> VARIANT_ID = SynchedEntityData.defineId(CobblelootsLootBall.class,
      EntityDataSerializers.STRING);
  private static final EntityDataAccessor<CompoundTag> LOOT_BALL_CLIENT_DATA = SynchedEntityData
      .defineId(CobblelootsLootBall.class, EntityDataSerializers.COMPOUND_TAG);
  private static final EntityDataAccessor<ItemStack> DISPLAY_ITEM = SynchedEntityData.defineId(
      CobblelootsLootBall.class,
      EntityDataSerializers.ITEM_STACK);

  // NBT Tags
  public static final String TAG_SPARKS = "Sparks";
  public static final String TAG_INVISIBLE = "Invisible";
  public static final String TAG_CUSTOM_TEXTURE = "Texture";
  public static final String TAG_LOOT_BALL_DATA_ID = "LootBallData";
  public static final String TAG_VARIANT_ID = "Variant";

  private static final String TAG_OPENERS = "Openers";
  private static final String TAG_USES = "Uses";
  private static final String TAG_MULTIPLIER = "Multiplier";
  private static final String TAG_DESPAWN_TICK = "DespawnTick";
  private static final String TAG_PLAYER_TIMER = "PlayerTimer";
  private static final String TAG_XP = "XP";

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

  // Game balance constants
  private static final float DEFAULT_MULTIPLIER = CobblelootsConfig.loot_ball_default_multiplier;
  private static final int DEFAULT_USES = CobblelootsConfig.loot_ball_default_uses;
  private static final long DEFAULT_DESPAWN_TICK = CobblelootsConfig.loot_ball_default_despawn_tick;
  private static final long DEFAULT_PLAYER_TIMER = CobblelootsConfig.loot_ball_default_player_cooldown;
  private static final int DEFAULT_XP = CobblelootsConfig.loot_ball_default_xp;

  // Variables
  protected final Map<UUID, Long> openers = new HashMap<>();
  protected int uses = DEFAULT_USES;
  protected float multiplier = DEFAULT_MULTIPLIER;
  protected long despawnTick = DEFAULT_DESPAWN_TICK;
  protected long playerTimer = DEFAULT_PLAYER_TIMER;
  protected int xp = DEFAULT_XP;

  // --- Constructors --

  /**
   * Constructs a new CobblelootsLootBall entity.
   *
   * @param entityType The entity type
   * @param level      The level where the entity exists
   */
  public CobblelootsLootBall(EntityType<? extends LivingEntity> entityType, Level level) {
    super(entityType, level);
  }

  /**
   * Creates the default attributes for loot ball entities.
   * This entity has a custom gravity value to control its floating behavior.
   *
   * @return Builder with the entity's base attributes
   */
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
    this.getEntityData().set(INVISIBLE, bl);
    super.setInvisible(bl);
  }

  @Override
  public boolean isInvisible() {
    return this.getEntityData().get(INVISIBLE);
  }

  @Override
  public @NotNull InteractionResult interact(Player player, InteractionHand interactionHand) {
    if (!this.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
      handleServerSideInteraction(serverPlayer, interactionHand);
    }

    this.setChanged();
    return InteractionResult.SUCCESS;
  }

  private void handleServerSideInteraction(ServerPlayer serverPlayer, InteractionHand interactionHand) {
    ItemStack handStack = serverPlayer.getItemInHand(interactionHand).copy();

    if (serverPlayer.isCreative()) {
      handleCreativeModeInteraction(serverPlayer, handStack);
    } else if (!serverPlayer.isSpectator()) {
      // Survival/Adventure mode
      this.tryOpen(serverPlayer);
    }
  }

  private void handleCreativeModeInteraction(ServerPlayer serverPlayer, ItemStack handStack) {
    if (handStack.isEmpty()) {
      // Empty hand in creative mode toggles visibility
      this.toggleVisibility(serverPlayer);
    } else if (handStack.is(Items.HONEYCOMB) && this.isInvisible()) {
      // Honeycomb on invisible loot ball toggles sparks
      this.toggleSparks(serverPlayer);
    } else {
      // Any other item sets the loot ball content
      this.setLootBallItem(handStack, serverPlayer);
    }
  }

  // --- LivingEntity methods ---

  @Override
  public boolean hurt(DamageSource damageSource, float f) {
    Entity attacker = damageSource.getEntity();
    if (!(attacker instanceof Player player)) {
      return false;
    }

    this.lastHurtByPlayerTime = 100;
    if (this.level().isClientSide()) {
      return false; // Only handle damage on the server side
    }

    if (!(player instanceof ServerPlayer serverPlayer)) {
      return false;
    }

    ItemStack itemStack = serverPlayer.getMainHandItem();
    if (!itemStack.isEmpty()) {
      return false; // Only handle empty hand attacks
    }

    return handleEmptyHandAttack(serverPlayer);
  }

  private boolean handleEmptyHandAttack(ServerPlayer serverPlayer) {
    if (!serverPlayer.isCreative() && this.getRemainingUses() != 0) {
      // Loot ball still has uses - block the attack
      serverPlayer.playNotifySound(SoundEvents.SHIELD_BLOCK, SoundSource.BLOCKS, 0.3f, 1.0f);
      return false;
    }

    if (this.isOpening) {
      // If loot ball is currently opening, block the attack
      serverPlayer.playNotifySound(SoundEvents.SHIELD_BLOCK, SoundSource.BLOCKS, 0.3f, 1.0f);
      return false;
    }

    // Drop loot if the loot ball has any in Creative mode
    if (!this.isEmpty() && serverPlayer.isCreative()) {
      this.spawnAtLocation(this.getItem(0));
    }

    // In survival mode, we drop the loot ball item itself without any loot
    if (!serverPlayer.isCreative()
        && (CobblelootsConfig.loot_ball_empty_behavior == CobblelootsLootBallEmptyBehavior.DROP_MANUAL
            || CobblelootsConfig.loot_ball_empty_behavior == CobblelootsLootBallEmptyBehavior.DROP_AUTOMATIC)) {
      this.spawnAtLocation(this.getSurvivalLootBallItem());
    }

    // Play breaking sound and remove the entity
    this.playSound(SoundEvents.ARMOR_STAND_BREAK, 0.5F, 1.0F);
    this.discard();
    return true;
  }

  @Override
  protected void defineSynchedData(SynchedEntityData.Builder builder) {
    super.defineSynchedData(builder);
    builder.define(SPARKS, true);
    builder.define(INVISIBLE, false);
    builder.define(CUSTOM_TEXTURE, "");
    builder.define(LOOT_BALL_DATA_ID, "");
    builder.define(VARIANT_ID, "");
    builder.define(OPENING_TICKS, 0);

    builder.define(LOOT_BALL_CLIENT_DATA, new CompoundTag());
    builder.define(DISPLAY_ITEM, ItemStack.EMPTY);
  }

  @Override
  public void addAdditionalSaveData(CompoundTag compoundTag) {
    super.addAdditionalSaveData(compoundTag);

    // Save synced entity data
    saveEntityVisualProperties(compoundTag);
    saveEntityIdentificationData(compoundTag);

    // Save non-synced data
    saveOpeners(compoundTag);
    saveNumericalProperties(compoundTag);
  }

  /**
   * Save visual properties like sparks and invisibility
   */
  private void saveEntityVisualProperties(CompoundTag compoundTag) {
    compoundTag.putBoolean(TAG_SPARKS, this.hasSparks());
    compoundTag.putBoolean(TAG_INVISIBLE, this.isInvisible());

    // Only save custom texture if it's not empty
    String customTexture = this.getEntityData().get(CUSTOM_TEXTURE);
    if (customTexture != null && !customTexture.isEmpty()) {
      compoundTag.putString(TAG_CUSTOM_TEXTURE, customTexture);
    }
  }

  /**
   * Save data that identifies what type of loot ball this is
   */
  private void saveEntityIdentificationData(CompoundTag compoundTag) {
    String lootBallDataId = this.getLootBallDataId();
    if (lootBallDataId != null && !lootBallDataId.isEmpty()) {
      compoundTag.putString(TAG_LOOT_BALL_DATA_ID, lootBallDataId);
    }

    String variantId = this.getVariantId();
    if (variantId != null && !variantId.isEmpty()) {
      compoundTag.putString(TAG_VARIANT_ID, variantId);
    }
  }

  /**
   * Save the list of players who have opened this loot ball
   */
  private void saveOpeners(CompoundTag compoundTag) {
    if (this.openers.isEmpty()) {
      return;
    }

    ListTag openersTag = new ListTag();
    for (Map.Entry<UUID, Long> entry : this.openers.entrySet()) {
      CompoundTag openerTag = new CompoundTag();
      openerTag.putUUID("UUID", entry.getKey());
      openerTag.putLong("Timestamp", entry.getValue());
      openersTag.add(openerTag);
    }
    compoundTag.put(TAG_OPENERS, openersTag);
  }

  /**
   * Save numerical properties like uses and multiplier,
   * but only if they differ from defaults to save space.
   */
  private void saveNumericalProperties(CompoundTag compoundTag) {
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

    if (this.xp != DEFAULT_XP) {
      compoundTag.putInt(TAG_XP, this.xp);
    }
  }

  @Override
  public void readAdditionalSaveData(CompoundTag compoundTag) {
    super.readAdditionalSaveData(compoundTag);

    // Read synced data with appropriate defaults
    this.setSparks(compoundTag.getBoolean(TAG_SPARKS));
    this.setInvisible(compoundTag.getBoolean(TAG_INVISIBLE));

    // Read resource locations with validation
    readTextureFromTag(compoundTag);
    readLootBallDataFromTag(compoundTag);

    // Read variant ID if present
    if (compoundTag.contains(TAG_VARIANT_ID)) {
      this.setVariantId(compoundTag.getString(TAG_VARIANT_ID));
    }

    // Read non-synced data
    readOpenersFromTag(compoundTag);
    readNumericalValuesFromTag(compoundTag);
  }

  private void readTextureFromTag(CompoundTag compoundTag) {
    if (compoundTag.contains(TAG_CUSTOM_TEXTURE)) {
      String texturePath = compoundTag.getString(TAG_CUSTOM_TEXTURE);
      if (!texturePath.isEmpty()) {
        ResourceLocation textureLocation = ResourceLocation.tryParse(texturePath);
        if (textureLocation != null) {
          this.setTexture(textureLocation);
        }
      }
    }
  }

  private void readLootBallDataFromTag(CompoundTag compoundTag) {
    if (compoundTag.contains(TAG_LOOT_BALL_DATA_ID)) {
      String dataPath = compoundTag.getString(TAG_LOOT_BALL_DATA_ID);
      if (!dataPath.isEmpty()) {
        ResourceLocation lootBallDataLocation = ResourceLocation.tryParse(dataPath);
        if (lootBallDataLocation != null) {
          this.setLootBallDataId(lootBallDataLocation);
        }
      }
    }
  }

  private void readOpenersFromTag(CompoundTag compoundTag) {
    if (compoundTag.contains(TAG_OPENERS)) {
      ListTag openersTag = compoundTag.getList(TAG_OPENERS, CompoundTag.TAG_COMPOUND);
      this.openers.clear(); // Clear existing openers first

      for (int i = 0; i < openersTag.size(); i++) {
        CompoundTag openerTag = openersTag.getCompound(i);
        if (openerTag.contains("UUID") && openerTag.contains("Timestamp")) {
          UUID uuid = openerTag.getUUID("UUID");
          long timestamp = openerTag.getLong("Timestamp");
          this.openers.put(uuid, timestamp);
        }
      }
    }
  }

  private void readNumericalValuesFromTag(CompoundTag compoundTag) {
    // Read with appropriate defaults
    this.uses = compoundTag.contains(TAG_USES) ? compoundTag.getInt(TAG_USES) : DEFAULT_USES;

    this.multiplier = compoundTag.contains(TAG_MULTIPLIER) ? compoundTag.getFloat(TAG_MULTIPLIER) : DEFAULT_MULTIPLIER;

    this.despawnTick = compoundTag.contains(TAG_DESPAWN_TICK) ? compoundTag.getLong(TAG_DESPAWN_TICK)
        : DEFAULT_DESPAWN_TICK;

    this.playerTimer = compoundTag.contains(TAG_PLAYER_TIMER) ? compoundTag.getLong(TAG_PLAYER_TIMER)
        : DEFAULT_PLAYER_TIMER;

    this.xp = compoundTag.contains(TAG_XP) ? compoundTag.getInt(TAG_XP) : DEFAULT_XP;
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
      if (this.getDespawnTick() > 0L && this.level().getGameTime() >= this.getDespawnTick()) {
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

    trySpawnParticles();

    this.level().getProfiler().pop();
  }

  private void trySpawnParticles() {
    // Only attempt particle spawning at the configured chance
    if (this.random.nextFloat() > PARTICLE_SPAWN_CHANCE) {
      return;
    }

    // Calculate random position near the entity
    double x = this.getX() + this.random.nextGaussian() * PARTICLE_OFFSET_MULTIPLIER;
    double y = this.getY() + PARTICLE_Y_FIXED_OFFSET + this.random.nextGaussian() * PARTICLE_OFFSET_MULTIPLIER;
    double z = this.getZ() + this.random.nextGaussian() * PARTICLE_OFFSET_MULTIPLIER;

    // Calculate random velocity components
    double vx = this.random.nextGaussian() * PARTICLE_VELOCITY_MULTIPLIER;
    double vy = this.random.nextGaussian() * PARTICLE_VELOCITY_MULTIPLIER;
    double vz = this.random.nextGaussian() * PARTICLE_VELOCITY_MULTIPLIER;

    // Spawn appropriate particle type based on entity state
    if (this.isInvisible() && this.hasSparks()) {
      this.level().addParticle(ParticleTypes.ELECTRIC_SPARK, x, y, z, vx, vy, vz);
    }

    if (this.isInWater()) {
      this.level().addParticle(ParticleTypes.BUBBLE_COLUMN_UP, x, y, z, vx, vy, vz);
    }
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
  public ResourceLocation getLootTableLocation() {
    ResourceLocation tableLocation = super.getLootTableLocation();
    if (tableLocation == null) {
      // Get loot table from loot ball data
      CobblelootsLootBallData lootBallData = this.getLootBallData();
      if (lootBallData == null)
        return CobblelootsDefinitions.EMPTY_LOCATION;

      CobblelootsLootBallVariantData variantData = this.getVariantData();

      if (variantData != null && variantData.getLootTable() != null &&
          !variantData.getLootTable().equals(CobblelootsDefinitions.EMPTY_LOCATION)) {
        // Use variant loot table if available
        tableLocation = variantData.getLootTable();
      } else if (lootBallData.getLootTable() != null &&
          !lootBallData.getLootTable().equals(CobblelootsDefinitions.EMPTY_LOCATION)) {
        // Fallback to loot ball data loot table
        tableLocation = lootBallData.getLootTable();
      } else {
        // No valid loot table found
        return CobblelootsDefinitions.EMPTY_LOCATION;
      }

    }
    return tableLocation;
  }

  // --- CobblelootsLootBall methods ---
  /**
   * Attempts to open the loot ball for a server player.
   * This checks various conditions like cooldown and if the player already opened
   * it.
   */
  private void tryOpen(ServerPlayer serverPlayer) {
    // Validate opening state
    if (!canPlayerOpenLootBall(serverPlayer)) {
      return;
    }

    // Generate loot
    this.unpackLootTable(serverPlayer);

    // Get the first item to display
    ItemStack displayItem = ItemStack.EMPTY;
    for (ItemStack item : this.itemStacks) {
      if (!item.isEmpty()) {
        displayItem = item.copy();
        break;
      }
    }

    // Start opening sequence if there's loot to give
    if (!this.isEmpty()) {
      startOpeningAnimation(serverPlayer, displayItem);
    } else {
      // Notify player that loot ball is empty
      serverPlayer.sendSystemMessage(cobblelootsText(TEXT_INFO_NO_LOOT).withStyle(ChatFormatting.GRAY), true);
    }
  }

  /**
   * Checks if the player can open this loot ball right now.
   * 
   * @return true if opening can proceed, false otherwise
   */
  private boolean canPlayerOpenLootBall(ServerPlayer serverPlayer) {
    // Check if loot ball is already being opened
    if (this.isOpening) {
      serverPlayer.sendSystemMessage(cobblelootsText(TEXT_ERROR_IS_OPENING).withStyle(ChatFormatting.RED), true);
      return false;
    }

    // Check if player already opened this loot ball
    if (this.isOpener(serverPlayer)) {
      if (!handleAlreadyOpenedError(serverPlayer)) {
        return false; // Player already opened this loot ball or cooldown is still active
      }
    }

    // Check if remaining uses are zero
    if (this.getRemainingUses() == 0) {
      serverPlayer.sendSystemMessage(cobblelootsText(TEXT_ERROR_ALREADY_OPENED).withStyle(ChatFormatting.RED), true);
      return false;
    }

    return true;
  }

  /**
   * Handle the case where a player tries to open a loot ball they've already
   * opened
   */
  private boolean handleAlreadyOpenedError(ServerPlayer serverPlayer) {
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

    // Default message for already opened loot ball
    serverPlayer.sendSystemMessage(cobblelootsText(TEXT_ERROR_ALREADY_OPENED).withStyle(ChatFormatting.RED), true);
    return false;
  }

  /**
   * Starts the opening animation sequence for the loot ball
   */
  private void startOpeningAnimation(ServerPlayer serverPlayer, ItemStack displayItem) {
    this.pendingOpener = serverPlayer;
    this.isOpening = true;
    this.wasInvisible = this.isInvisible();
    this.setInvisible(false);
    this.setOpeningTicks(LOOT_BALL_OPENING_TICKS);
    this.setDisplayItem(displayItem);

    if (this.getDespawnTick() > 0L) {
      // If a despawn tick is set, extend it to allow for opening
      this.setDespawnTick(this.level().getGameTime() + LOOT_BALL_OPENING_TICKS * 5);
    }
  }

  private long getPlayerTimer() {
    return this.playerTimer;
  }

  private void setOpeningTicks(int i) {
    this.getEntityData().set(OPENING_TICKS, i);
  }

  public int getOpeningTicks() {
    return this.getEntityData().get(OPENING_TICKS);
  }

  private void open(ServerPlayer serverPlayer) {
    if (serverPlayer == null) {
      return;
    }

    // Give items to player
    deliverItemsToPlayer(serverPlayer);

    // Stop displaying the item as it has been received
    this.setDisplayItem(ItemStack.EMPTY);

    // Give experience points if enabled
    awardExperienceIfEnabled(serverPlayer);

    // Handle uses count
    decrementUsesIfNotInfinite();

    // Clear inventory if using loot table for future regeneration
    clearInventoryIfUsingLootTable();

    // Mark entity as changed
    this.setChanged();
  }

  private void deliverItemsToPlayer(ServerPlayer serverPlayer) {
    for (ItemStack itemStack : this.itemStacks) {
      if (itemStack.isEmpty()) {
        continue;
      }

      ItemStack deliveredItem = prepareItemForDelivery(itemStack.copy());
      notifyPlayerAboutItem(serverPlayer, deliveredItem);
      serverPlayer.getInventory().placeItemBackInInventory(deliveredItem);

      // Track that this player opened the loot ball
      this.addOpener(serverPlayer);

      // Play sounds for successful opening
      this.playSound(CobblelootsLootBallSounds.getPopItemSound());
      serverPlayer.playNotifySound(CobblelootsLootBallSounds.getFanfare(), SoundSource.BLOCKS, 1f, 1.0f);
    }
  }

  private ItemStack prepareItemForDelivery(ItemStack item) {
    // Apply multiplier to item count
    int multipliedCount = (int) Math.ceil(item.getCount() * this.getMultiplier());
    item.setCount(multipliedCount);
    return item;
  }

  private void notifyPlayerAboutItem(ServerPlayer serverPlayer, ItemStack item) {
    Component message;
    if (this.getMultiplier() > 1.0f) {
      message = cobblelootsText(TEXT_OPEN_SUCCESS_BONUS,
          String.valueOf(this.getMultiplier()),
          item.getHoverName().getString(),
          String.valueOf(item.getCount())).withStyle(ChatFormatting.AQUA);
    } else {
      message = cobblelootsText(TEXT_OPEN_SUCCESS,
          item.getHoverName().getString(),
          String.valueOf(item.getCount())).withStyle(ChatFormatting.AQUA);
    }
    serverPlayer.sendSystemMessage(message, true);
  }

  private void awardExperienceIfEnabled(ServerPlayer serverPlayer) {
    if (CobblelootsConfig.loot_ball_xp_enabled) {
      if (this.getXP() > 0) {
        serverPlayer.giveExperiencePoints(this.getXP());
      }
    }
  }

  private void decrementUsesIfNotInfinite() {
    if (!this.isInfinite()) {
      this.setRemainingUses(this.getRemainingUses() - 1);
    }
  }

  private void clearInventoryIfUsingLootTable() {
    ResourceLocation lootTableLocation = this.getLootTableLocation();
    if (lootTableLocation != null && !lootTableLocation.equals(CobblelootsDefinitions.EMPTY_LOCATION)) {
      this.clearContent();
    }
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
    return this.getEntityData().get(SPARKS);
  }

  public String getLootBallDataId() {
    return this.getEntityData().get(LOOT_BALL_DATA_ID);
  }

  public String getVariantId() {
    return this.getEntityData().get(VARIANT_ID);
  }

  @Nullable
  public CobblelootsLootBallData getLootBallData() {
    // Try to parse the current id
    ResourceLocation dataLocation = ResourceLocation.tryParse(this.getLootBallDataId());
    if (dataLocation == null)
      return null;
    // Return the loot ball data if found
    return CobblelootsDataProvider.getLootBallData(dataLocation);
  }

  @Nullable
  public CobblelootsLootBallVariantData getVariantData() {
    // Try to get loot ball data
    CobblelootsLootBallData lootBallData = this.getLootBallData();
    if (lootBallData == null)
      return null;
    // Return the variant data if found
    return lootBallData.getVariants().get(this.getVariantId());
  }

  public CompoundTag getLootBallClientData() {
    return this.getEntityData().get(LOOT_BALL_CLIENT_DATA);
  }

  public void updateLootBallClientData() {
    CompoundTag compoundTag = new CompoundTag();

    // Add texture data
    ResourceLocation texture = this.getTextureFromServerData();
    if (texture != null) {
      compoundTag.putString(TAG_CUSTOM_TEXTURE, texture.toString());
    }

    // Update the entity data
    this.getEntityData().set(LOOT_BALL_CLIENT_DATA, compoundTag);
  }

  @Override
  public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
    super.onSyncedDataUpdated(key);

    // If the data that affects appearance changes, update client data
    if (key.equals(CUSTOM_TEXTURE) || key.equals(LOOT_BALL_DATA_ID) || key.equals(VARIANT_ID)) {
      if (!this.level().isClientSide()) {
        this.updateLootBallClientData();
      }
    }
  }

  public String getTextureId() {
    return this.getTexture().toString();
  }

  @NotNull
  public ResourceLocation getTexture() {
    ResourceLocation textureLocation;

    if (this.level().isClientSide()) {
      textureLocation = getTextureFromClientData();
    } else {
      textureLocation = getTextureFromServerData();
    }

    return textureLocation != null ? textureLocation : CobblelootsDefinitions.EMPTY_LOCATION;
  }

  @Nullable
  private ResourceLocation getTextureFromClientData() {
    CompoundTag compoundTag = this.getLootBallClientData();
    if (compoundTag == null || !compoundTag.contains(TAG_CUSTOM_TEXTURE)) {
      return null;
    }

    String texturePath = compoundTag.getString(TAG_CUSTOM_TEXTURE);
    if (texturePath.isEmpty()) {
      return null;
    }

    return ResourceLocation.tryParse(texturePath);
  }

  @Nullable
  private ResourceLocation getTextureFromServerData() {
    // Check custom texture first (highest priority)
    ResourceLocation customLocation = getCustomTextureLocation();
    if (customLocation != null) {
      return customLocation;
    }

    // Get texture from loot ball data and variants (second priority)
    return getLootBallDataTexture();
  }

  @Nullable
  private ResourceLocation getCustomTextureLocation() {
    String customTexture = this.getEntityData().get(CUSTOM_TEXTURE);
    if (customTexture.isEmpty()) {
      return null;
    }
    return ResourceLocation.tryParse(customTexture);
  }

  @Nullable
  private ResourceLocation getLootBallDataTexture() {
    CobblelootsLootBallData lootBallData = this.getLootBallData();
    if (lootBallData == null) {
      return null;
    }

    // Check variant texture
    String variantId = this.getVariantId();
    if (!variantId.isEmpty()) {
      CobblelootsLootBallVariantData variantData = lootBallData.getVariants().get(variantId);
      if (variantData != null && variantData.getTexture() != null &&
          !variantData.getTexture().equals(CobblelootsDefinitions.EMPTY_LOCATION)) {
        return variantData.getTexture();
      }
    }

    // Fallback to default texture
    return lootBallData.getTexture();
  }

  public void setSparks(boolean sparks) {
    this.getEntityData().set(SPARKS, sparks);
  }

  public void setVariantId(String variantId) {
    this.getEntityData().set(VARIANT_ID, variantId);
    updateLootBallClientData();
  }

  public void setLootBallDataId(ResourceLocation lootBallData) {
    this.getEntityData().set(LOOT_BALL_DATA_ID, lootBallData.toString());
    updateLootBallClientData();
  }

  public void setTexture(ResourceLocation texture) {
    this.getEntityData().set(CUSTOM_TEXTURE, texture.toString());
    updateLootBallClientData();
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

  public int getXP() {
    // Check if XP is set directly
    if (this.xp != DEFAULT_XP) {
      return this.xp;
    }

    // If loot ball data is set, check if it has XP defined
    CobblelootsLootBallData lootBallData = this.getLootBallData();
    if (lootBallData != null && lootBallData.getXp() > 0) {
      return lootBallData.getXp();
    }

    // Default to configured value if no specific XP is set
    return this.xp;
  }

  // --- Private methods ---
  private void toggleVisibility(ServerPlayer serverPlayer) {
    serverPlayer.sendSystemMessage(cobblelootsText(TEXT_TOGGLE_VISIBILITY).withStyle(ChatFormatting.AQUA), true);
    serverPlayer.playNotifySound(CobblelootsLootBallSounds.getToggleInvisibilitySound(), SoundSource.BLOCKS, 0.5f,
        1.0f);
    this.setInvisible(!this.isInvisible());
  }

  private void toggleSparks(ServerPlayer serverPlayer) {
    serverPlayer.sendSystemMessage(cobblelootsText(TEXT_TOGGLE_SPARKS).withStyle(ChatFormatting.AQUA), true);
    serverPlayer.playNotifySound(CobblelootsLootBallSounds.getToggleSparksSound(this.hasSparks()), SoundSource.BLOCKS,
        0.5f, 1.0f);
    this.setSparks(!this.hasSparks());
  }

  private void setLootBallItem(ItemStack itemStack, ServerPlayer serverPlayer) {
    this.setItem(0, itemStack);
    if (!serverPlayer.isCreative())
      serverPlayer.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
    serverPlayer.sendSystemMessage(
        cobblelootsText(TEXT_SET_ITEM, itemStack.getHoverName().getString(), String.valueOf(itemStack.getCount()))
            .withStyle(ChatFormatting.AQUA),
        true);
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
    CompoundTag tag = createSurvivalLootBallItemTag();

    // Only set custom data component if we have data to save
    if (!tag.isEmpty()) {
      lootBallItem.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    // Add item name if custom name is set
    Component nameComponent = getLootBallNameComponent();
    if (!nameComponent.equals(Component.empty())) {
      lootBallItem.set(DataComponents.CUSTOM_NAME, nameComponent);
    }

    return lootBallItem;
  }

  private ItemStack getCreativeLootBallItem() {
    // Create base item
    ItemStack lootBallItem = new ItemStack(CobblelootsItems.getLootBallItem());
    CompoundTag tag = createCreativeLootBallItemTag();

    // Set custom data component if we have data to save
    if (!tag.isEmpty()) {
      lootBallItem.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    // Add item name if custom name is set
    Component nameComponent = getLootBallNameComponent();
    if (!nameComponent.equals(Component.empty())) {
      lootBallItem.set(DataComponents.CUSTOM_NAME, nameComponent);
    }

    return lootBallItem;
  }

  /**
   * Creates the name component for this loot ball item.
   * This is used in survival mode to display the loot ball's properties.
   */
  private Component getLootBallNameComponent() {
    CobblelootsLootBallData data = this.getLootBallData();
    CobblelootsLootBallVariantData variantData = this.getVariantData();
    if (variantData != null) {
      return variantData.getName();
    } else if (data != null) {
      return data.getName();
    }
    return Component.empty();
  }

  /**
   * Creates the tag containing all data needed to reconstruct this loot ball.
   * This is just for decorative purposes in survival mode.
   */
  private CompoundTag createSurvivalLootBallItemTag() {
    CompoundTag tag = new CompoundTag();

    // Save important identification data
    tag.putString(TAG_LOOT_BALL_DATA_ID, this.getLootBallDataId());
    tag.putString(TAG_VARIANT_ID, this.getVariantId());
    tag.putString(TAG_CUSTOM_TEXTURE, this.getTextureId());

    // Clear visibility and other properties
    tag.putBoolean(TAG_INVISIBLE, false);
    tag.putInt(TAG_USES, 0);
    tag.putLong(TAG_DESPAWN_TICK, 0L);

    return tag;
  }

  /**
   * Creates the tag containing all data needed to reconstruct this loot ball.
   * This is used in creative mode to allow players to set properties.
   */
  private CompoundTag createCreativeLootBallItemTag() {
    CompoundTag tag = new CompoundTag();

    // Save important identification data
    tag.putString(TAG_LOOT_BALL_DATA_ID, this.getLootBallDataId());
    tag.putString(TAG_VARIANT_ID, this.getVariantId());
    tag.putString(TAG_CUSTOM_TEXTURE, this.getTextureId());

    // Save visual properties
    tag.putBoolean(TAG_SPARKS, this.hasSparks());
    tag.putBoolean(TAG_INVISIBLE, this.isInvisible());

    // Save numerical properties
    tag.putInt(TAG_USES, this.getRemainingUses());
    tag.putFloat(TAG_MULTIPLIER, this.getMultiplier());
    tag.putLong(TAG_DESPAWN_TICK, this.getDespawnTick());
    tag.putLong(TAG_PLAYER_TIMER, this.getPlayerTimer());
    tag.putInt(TAG_XP, this.getXP());

    return tag;
  }

  private void openingTick() {
    if (this.level().isClientSide()) {
      handleClientAnimations();
    } else {
      handleServerAnimations();
    }
  }

  private void handleClientAnimations() {
    int currentTicks = this.getOpeningTicks();
    boolean animationStarted = this.openingAnimationState.isStarted();

    if (currentTicks > 0 && !animationStarted) {
      this.openingAnimationState.start(this.tickCount);
    } else if (currentTicks == 0 && animationStarted) {
      this.openingAnimationState.stop();
    }
  }

  private void handleServerAnimations() {
    int currentTicks = this.getOpeningTicks();

    if (currentTicks > 0) {
      // Handle specific animation stages
      if (currentTicks == LOOT_BALL_OPENING_TICKS) {
        // Start opening animation and play sound
        this.playSound(CobblelootsLootBallSounds.getLidOpenSound());

        // Spawn particles if enabled
        if (CobblelootsConfig.loot_ball_effects_enabled) {
          spawnOpeningParticles();
        }
      } else if (currentTicks == LOOT_BALL_OPENING_DROP_TICK) {
        // Give loot at the specific tick
        this.open(this.pendingOpener);
      }

      // Decrement opening tick counter
      this.setOpeningTicks(currentTicks - 1);
    } else if (currentTicks == 0 && this.pendingOpener != null) {
      this.pendingOpener = null;
      this.isOpening = false;
      this.setInvisible(this.wasInvisible);
      this.setDisplayItem(ItemStack.EMPTY);
      this.playSound(CobblelootsLootBallSounds.getLidCloseSound());

      // Handle empty loot ball based on configured behavior
      if (CobblelootsConfig.loot_ball_empty_behavior == CobblelootsLootBallEmptyBehavior.DESTROY) {
        if (!this.isInfinite() && this.getRemainingUses() <= 0) {
          this.discard();
          return;
        }
      } else if (CobblelootsConfig.loot_ball_empty_behavior == CobblelootsLootBallEmptyBehavior.DROP_AUTOMATIC) {
        if (!this.isInfinite() && this.getRemainingUses() <= 0) {
          this.spawnAtLocation(this.getSurvivalLootBallItem());
          this.playSound(SoundEvents.ITEM_FRAME_REMOVE_ITEM);
          this.discard();
        }
      }
    }
  }

  @Override
  public ItemStack getPickResult() {
    return this.getCreativeLootBallItem();
  }

  public void setDisplayItem(ItemStack item) {
    this.getEntityData().set(DISPLAY_ITEM, item);
  }

  public ItemStack getDisplayItem() {
    return this.getEntityData().get(DISPLAY_ITEM);
  }

  private void spawnOpeningParticles() {
    if (this.level().isClientSide())
      return;

    // White sparkles rising like a fountain
    ((net.minecraft.server.level.ServerLevel) this.level()).sendParticles(
        ParticleTypes.FIREWORK,
        this.getX(), this.getY() + 0.5, this.getZ(),
        10, 0.2, 0.2, 0.2, 0.1);

    // Some happy villager particles
    ((net.minecraft.server.level.ServerLevel) this.level()).sendParticles(
        ParticleTypes.HAPPY_VILLAGER,
        this.getX(), this.getY() + 0.5, this.getZ(),
        5, 0.3, 0.3, 0.3, 0.1);
  }
}
