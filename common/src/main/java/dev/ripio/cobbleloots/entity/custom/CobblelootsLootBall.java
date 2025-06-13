package dev.ripio.cobbleloots.entity.custom;

import dev.ripio.cobbleloots.config.CobblelootsConfig;
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
  private static final EntityDataAccessor<String> CUSTOM_TEXTURE = SynchedEntityData.defineId(CobblelootsLootBall.class, EntityDataSerializers.STRING);
  private static final EntityDataAccessor<String> LOOT_BALL_DATA_ID = SynchedEntityData.defineId(CobblelootsLootBall.class, EntityDataSerializers.STRING);
  private static final EntityDataAccessor<String> VARIANT_ID = SynchedEntityData.defineId(CobblelootsLootBall.class, EntityDataSerializers.STRING);
  private static final EntityDataAccessor<CompoundTag> LOOT_BALL_CLIENT_DATA = SynchedEntityData.defineId(CobblelootsLootBall.class, EntityDataSerializers.COMPOUND_TAG);

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
  private static final String TEXT_TOOLTIP = "entity.cobbleloots.loot_ball.tooltip";

  // Game balance constants
  private static final float DEFAULT_MULTIPLIER = CobblelootsConfig.getFloatConfig(CobblelootsConfig.LOOT_BALL_DEFAULTS_MULTIPLIER);
  private static final int DEFAULT_USES = CobblelootsConfig.getIntConfig(CobblelootsConfig.LOOT_BALL_DEFAULTS_USES);
  private static final long DEFAULT_DESPAWN_TICK = CobblelootsConfig.getLongConfig(CobblelootsConfig.LOOT_BALL_DEFAULTS_DESPAWN_TICK);
  private static final long DEFAULT_PLAYER_TIMER = CobblelootsConfig.getLongConfig(CobblelootsConfig.LOOT_BALL_DEFAULTS_PLAYER_TIMER);
  private static final int DEFAULT_XP = CobblelootsConfig.getIntConfig(CobblelootsConfig.LOOT_BALL_DEFAULTS_XP);

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
   * @param level The level where the entity exists
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
    builder.define(CUSTOM_TEXTURE, "");
    builder.define(LOOT_BALL_DATA_ID, "");
    builder.define(VARIANT_ID, "");
    builder.define(OPENING_TICKS, 0);

    builder.define(LOOT_BALL_CLIENT_DATA, new CompoundTag());
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
    this.uses = compoundTag.contains(TAG_USES) ?
                compoundTag.getInt(TAG_USES) : DEFAULT_USES;

    this.multiplier = compoundTag.contains(TAG_MULTIPLIER) ?
                      compoundTag.getFloat(TAG_MULTIPLIER) : DEFAULT_MULTIPLIER;

    this.despawnTick = compoundTag.contains(TAG_DESPAWN_TICK) ?
                       compoundTag.getLong(TAG_DESPAWN_TICK) : DEFAULT_DESPAWN_TICK;

    this.playerTimer = compoundTag.contains(TAG_PLAYER_TIMER) ?
                       compoundTag.getLong(TAG_PLAYER_TIMER) : DEFAULT_PLAYER_TIMER;

    this.xp = compoundTag.contains(TAG_XP) ?
              compoundTag.getInt(TAG_XP) : DEFAULT_XP;
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
  public ResourceLocation getLootTableLocation() {
    ResourceLocation tableLocation = super.getLootTableLocation();
    if (tableLocation == null) {
      // Get loot table from loot ball data
      CobblelootsLootBallData lootBallData = this.getLootBallData();
      if (lootBallData == null) return CobblelootsDefinitions.EMPTY_LOCATION;

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
   * This checks various conditions like cooldown and if the player already opened it.
   */
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
  private long getPlayerTimer() {
    return this.playerTimer;
  }

  private void setOpeningTicks(int i) {
    this.getEntityData().set(OPENING_TICKS, i);
  }

  private int getOpeningTicks() {
    return this.getEntityData().get(OPENING_TICKS);
  }

    // Give experience points if enabled
    awardExperienceIfEnabled(serverPlayer);
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
  private void awardExperienceIfEnabled(ServerPlayer serverPlayer) {
    if (CobblelootsConfig.getBooleanConfig(CobblelootsConfig.LOOT_BALL_XP_ENABLED)) {
      if (this.getXP() > 0) {
        serverPlayer.giveExperiencePoints(this.getXP());
      }
    }
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
    if (dataLocation == null) return null;
    // Return the loot ball data if found
    return CobblelootsDataProvider.getLootBallData(dataLocation);
  }

  @Nullable
  public CobblelootsLootBallVariantData getVariantData() {
    // Try to get loot ball data
    CobblelootsLootBallData lootBallData = this.getLootBallData();
    if (lootBallData == null) return null;
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
    serverPlayer.playNotifySound(CobblelootsLootBallSounds.getToggleInvisibilitySound(), SoundSource.BLOCKS, 0.5f, 1.0f);
    this.setInvisible(!this.isInvisible());
  }

  private void toggleSparks(ServerPlayer serverPlayer) {
    serverPlayer.sendSystemMessage(cobblelootsText(TEXT_TOGGLE_SPARKS).withStyle(ChatFormatting.AQUA), true);
    serverPlayer.playNotifySound(CobblelootsLootBallSounds.getToggleSparksSound(this.hasSparks()), SoundSource.BLOCKS, 0.5f, 1.0f);
    this.setSparks(!this.hasSparks());
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
