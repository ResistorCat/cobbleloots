package dev.ripio.cobbleloots.entity.custom;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface CobblelootsInventory extends Container {
  /**
   * Get the items in the inventory.
   *
   * @return the items in the inventory
   */
  NonNullList<ItemStack> getItems();

  static CobblelootsInventory create(NonNullList<ItemStack> itemStacks) {return () -> itemStacks;}

  static CobblelootsInventory withSize(int size) {
    return create(NonNullList.withSize(size, ItemStack.EMPTY));
  }

  @Override
  default int getContainerSize() {
    return getItems().size();
  }

  @Override
  default boolean isEmpty() {
    for (ItemStack itemStack : getItems()) {
      if (!itemStack.isEmpty()) {
        return false;
      }
    }
    return true;
  }

  @Override
  default @NotNull ItemStack getItem(int slot) {
    return getItems().get(slot);
  }

  default @NotNull ItemStack removeItem(int slot, int amount) {
    ItemStack itemStack = getItems().get(slot);
    if (itemStack.isEmpty()) {
      return ItemStack.EMPTY;
    }
    if (itemStack.getCount() <= amount) {
      setItem(slot, ItemStack.EMPTY);
      return itemStack;
    }
    ItemStack result = itemStack.split(amount);
    if (itemStack.isEmpty()) {
      setItem(slot, ItemStack.EMPTY);
    }
    return result;
  }

  @Override
  default @NotNull ItemStack removeItemNoUpdate(int slot) {
    ItemStack itemStack = getItems().get(slot);
    if (itemStack.isEmpty()) {
      return ItemStack.EMPTY;
    }
    setItem(slot, ItemStack.EMPTY);
    return itemStack;
  }

  @Override
  default void setItem(int slot, @NotNull ItemStack itemStack) {
    getItems().set(slot, itemStack);
    if (itemStack.getCount() > getMaxStackSize()) {
      itemStack.setCount(getMaxStackSize());
    }
  }

  @Override
  default void clearContent() {
    getItems().clear();
  }

  @Override
  default void setChanged() {}

  @Override
  default boolean stillValid(@NotNull net.minecraft.world.entity.player.Player player) {
    return true;
  }
}
