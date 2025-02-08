package dev.ripio.cobbleloots.entity.custom;

import dev.ripio.cobbleloots.Cobbleloots;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CobblelootsBaseContainerEntity extends LivingEntity implements ContainerEntity {
  // Slots
  private static final NonNullList<ItemStack> EMPTY_STACK_LIST = NonNullList.createWithCapacity(0);

  // Loot Table
  protected ResourceLocation lootTableLocation;
  protected long lootTableSeed;

  // NBT Keys
  private static final String TAG_LOOT_TABLE = "LootTable";
  private static final String TAG_LOOT_TABLE_SEED = "LootTableSeed";

  // -- Constructors --

  protected CobblelootsBaseContainerEntity(EntityType<? extends LivingEntity> entityType, Level level) {
    super(entityType, level);
  }

  // -- LivingEntity Methods --

  @Override
  public @NotNull Iterable<ItemStack> getArmorSlots() {
    return EMPTY_STACK_LIST;
  }

  @Override
  public @NotNull ItemStack getItemBySlot(EquipmentSlot equipmentSlot) {
    return ItemStack.EMPTY;
  }

  @Override
  public void setItemSlot(EquipmentSlot equipmentSlot, ItemStack itemStack) {}

  @Override
  public @NotNull HumanoidArm getMainArm() {
    return HumanoidArm.LEFT;
  }

  @Override
  protected boolean shouldDropLoot() {
    return false;
  }

  @Override
  public void addAdditionalSaveData(CompoundTag compoundTag) {
    super.addAdditionalSaveData(compoundTag);
    if (this.getLootTableLocation() != null) {
      compoundTag.putString(TAG_LOOT_TABLE, this.getLootTableLocation().toString());
      if (this.getLootTableSeed() != 0L) {
        compoundTag.putLong(TAG_LOOT_TABLE_SEED, this.getLootTableSeed());
      }
    } else {
      ContainerHelper.saveAllItems(compoundTag, this.getItemStacks(), this.registryAccess());
    }
  }

  @Override
  public void readAdditionalSaveData(CompoundTag compoundTag) {
    super.readAdditionalSaveData(compoundTag);
    this.readChestVehicleSaveData(compoundTag, this.registryAccess());
  }

  // -- ContainerEntity Methods --


  @Override
  public void clearItemStacks() {
    this.getItemStacks().clear();
  }

//  @Override
//  public @NotNull ResourceKey<LootTable> getLootTable() {
//    return ResourceKey.create(Registries.LOOT_TABLE, this.lootTableLocation);
//  }

  @Override
  public void setLootTable(@Nullable ResourceKey<LootTable> resourceKey) {
  }

  @Override
  public void setLootTableSeed(long l) {
    this.lootTableSeed = l;
  }

//  @Override
//  public long getLootTableSeed() {
//    return this.lootTableSeed;
//  }

  public abstract @NotNull NonNullList<ItemStack> getItemStacks();

  @Override
  public abstract int getContainerSize();

  @Override
  public @NotNull ItemStack getItem(int i) {
    this.unpackLootTable(null);
    return this.getItemStacks().get(i);
  }

  @Override
  public @NotNull ItemStack removeItem(int i, int j) {
    return this.removeChestVehicleItem(i, j);
  }

  @Override
  public @NotNull ItemStack removeItemNoUpdate(int i) {
    return this.removeChestVehicleItemNoUpdate(i);
  }

  @Override
  public void setItem(int i, ItemStack itemStack) {
    this.setChestVehicleItem(i, itemStack);
  }

  @Override
  public void setChanged() {
  }

  @Override
  public boolean stillValid(Player player) {
    return this.isChestVehicleStillValid(player);
  }

  @Override
  public void clearContent() {
    this.clearChestVehicleContent();
  }

  @Override
  public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
    return null;
  }

  @Nullable
  public ResourceLocation getLootTableLocation() {
    return this.lootTableLocation;
  }

  protected void unpackLootTable(ServerPlayer serverPlayer) {
    MinecraftServer minecraftServer = this.level().getServer();
    if (this.getLootTableLocation() != null && minecraftServer != null) {
      ResourceKey<LootTable> resourceKey = ResourceKey.create(Registries.LOOT_TABLE, this.getLootTableLocation());
      Cobbleloots.LOGGER.info("Unpacking loot table: {}", resourceKey.location());
      LootTable lootTable = minecraftServer.reloadableRegistries().getLootTable(resourceKey);
      if (serverPlayer != null) {
        CriteriaTriggers.GENERATE_LOOT.trigger(serverPlayer, resourceKey);
      }

      this.setLootTableLocation(null);  // TEMPORAL FIX
      LootParams.Builder builder = (new LootParams.Builder((ServerLevel)this.level())).withParameter(LootContextParams.ORIGIN, this.position());
      if (serverPlayer != null) {
        builder.withLuck(serverPlayer.getLuck()).withParameter(LootContextParams.THIS_ENTITY, serverPlayer);
      }

      lootTable.fill(this, builder.create(LootContextParamSets.CHEST), this.lootTableSeed);
    }
  }

  private void setLootTableLocation(ResourceLocation resourceLocation) {
    this.lootTableLocation = resourceLocation;
  }


}
