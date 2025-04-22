package dev.ripio.cobbleloots.entity.custom;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CobblelootsBaseContainerEntity extends LivingEntity implements Container {
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
    if (this.lootTableLocation != null) {
      compoundTag.putString(TAG_LOOT_TABLE, this.lootTableLocation.toString());
      if (this.getLootTableSeed() != 0L) {
        compoundTag.putLong(TAG_LOOT_TABLE_SEED, this.getLootTableSeed());
      }
    } else {
      ContainerHelper.saveAllItems(compoundTag, this.getItemStacks(), this.registryAccess());
    }
  }

  @Override
  public void readAdditionalSaveData(CompoundTag compoundTag) {
    this.clearItemStacks();
    if (compoundTag.contains(TAG_LOOT_TABLE, 8)) {
      this.setLootTableLocation(ResourceLocation.tryParse(compoundTag.getString(TAG_LOOT_TABLE)));
      this.setLootTableSeed(compoundTag.getLong(TAG_LOOT_TABLE_SEED));
    } else {
      ContainerHelper.loadAllItems(compoundTag, this.getItemStacks(), this.registryAccess());
    }
  }

  // -- Container Methods --
  public void clearItemStacks() {
    this.getItemStacks().clear();
  }

  public void setLootTableSeed(long l) {
    this.lootTableSeed = l;
  }

  @Override
  public boolean isEmpty() {
    for (ItemStack itemStack : this.getItemStacks()) {
      if (!itemStack.isEmpty()) {
        return false;
      }
    }
    return true;
  }

  public abstract @NotNull NonNullList<ItemStack> getItemStacks();

  @Override
  public abstract int getContainerSize();

  @Override
  public @NotNull ItemStack getItem(int i) {
    //this.unpackLootTable(null);
    return this.getItemStacks().get(i);
  }

  @Override
  public @NotNull ItemStack removeItem(int i, int j) {
    //this.unpackLootTable(null);
    return ContainerHelper.removeItem(this.getItemStacks(), i, j);
  }

  @Override
  public @NotNull ItemStack removeItemNoUpdate(int i) {
    //this.unpackLootTable(null);
    ItemStack itemStack = this.getItemStacks().get(i);
    if (itemStack.isEmpty()) {
      return ItemStack.EMPTY;
    } else {
      this.getItemStacks().set(i, ItemStack.EMPTY);
      return itemStack;
    }
  }

  @Override
  public void setItem(int i, ItemStack itemStack) {
    //this.unpackLootTable(null);
    this.getItemStacks().set(i, itemStack);
    itemStack.limitSize(this.getMaxStackSize(itemStack));
  }

  @Override
  public void setChanged() {
  }

  @Override
  public boolean stillValid(Player player) {
    return !this.isRemoved() && player.canInteractWithEntity(this.getBoundingBox(), 4.0F);
  }

  @Override
  public void clearContent() {
    //this.unpackLootTable(null);
    this.getItemStacks().clear();
  }

  @Nullable
  public ResourceLocation getLootTableLocation() {
    return this.lootTableLocation;
  }

  protected void unpackLootTable(ServerPlayer serverPlayer) {
    MinecraftServer minecraftServer = this.level().getServer();
    ResourceLocation tableLocation = this.getLootTableLocation();
    if (tableLocation != null && minecraftServer != null && this.isEmpty()) {
      ResourceKey<LootTable> resourceKey = ResourceKey.create(Registries.LOOT_TABLE, tableLocation);
      LootTable lootTable = minecraftServer.reloadableRegistries().getLootTable(resourceKey);
      if (serverPlayer != null) {
        CriteriaTriggers.GENERATE_LOOT.trigger(serverPlayer, resourceKey);
      }

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
