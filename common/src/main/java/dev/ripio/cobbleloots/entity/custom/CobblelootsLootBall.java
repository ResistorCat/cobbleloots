package dev.ripio.cobbleloots.entity.custom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CobblelootsLootBall extends Display {
    private static final String TAG_ITEM = "item";
    private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK_ID;
    private final SlotAccess slot = SlotAccess.of(this::getItemStack, this::setItemStack);
    @Nullable
    private Display.ItemDisplay.ItemRenderState itemRenderState;

    public CobblelootsLootBall(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ITEM_STACK_ID, ItemStack.EMPTY);
    }

    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        super.onSyncedDataUpdated(entityDataAccessor);
//        if (DATA_ITEM_STACK_ID.equals(entityDataAccessor) || DATA_ITEM_DISPLAY_ID.equals(entityDataAccessor)) {
        if (DATA_ITEM_STACK_ID.equals(entityDataAccessor)) {
            this.updateRenderState = true;
        }

    }

    private ItemStack getItemStack() {
        return (ItemStack)this.entityData.get(DATA_ITEM_STACK_ID);
    }

    private void setItemStack(ItemStack itemStack) {
        this.entityData.set(DATA_ITEM_STACK_ID, itemStack);
    }

    private void setItemTransform(ItemDisplayContext itemDisplayContext) {
        //this.entityData.set(DATA_ITEM_DISPLAY_ID, itemDisplayContext.getId());
    }

    private ItemDisplayContext getItemTransform() {
        return ItemDisplayContext.NONE;
        //return ItemDisplayContext.BY_ID.apply(this.entityData.get(DATA_ITEM_DISPLAY_ID));
    }

    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.contains("item")) {
            this.setItemStack(ItemStack.parse(this.registryAccess(), compoundTag.getCompound("item")).orElse(ItemStack.EMPTY));
        } else {
            this.setItemStack(ItemStack.EMPTY);
        }
    }

    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        if (!this.getItemStack().isEmpty()) {
            compoundTag.put("item", this.getItemStack().save(this.registryAccess()));
        }

        ItemDisplayContext.CODEC.encodeStart(NbtOps.INSTANCE, this.getItemTransform()).ifSuccess((tag) -> compoundTag.put("item_display", tag));
    }

    public @NotNull SlotAccess getSlot(int i) {
        return i == 0 ? this.slot : SlotAccess.NULL;
    }

    @Nullable
    public Display.ItemDisplay.ItemRenderState itemRenderState() {
        return this.itemRenderState;
    }

    protected void updateRenderSubState(boolean bl, float f) {
        ItemStack itemStack = this.getItemStack();
        itemStack.setEntityRepresentation(this);
        this.itemRenderState = new ItemDisplay.ItemRenderState(itemStack, this.getItemTransform());
    }

    static {
        DATA_ITEM_STACK_ID = SynchedEntityData.defineId(CobblelootsLootBall.class, EntityDataSerializers.ITEM_STACK);
        //DATA_ITEM_DISPLAY_ID = SynchedEntityData.defineId(ItemDisplay.class, EntityDataSerializers.BYTE);
    }

    public static record ItemRenderState(ItemStack itemStack, ItemDisplayContext itemTransform) {
        public ItemRenderState(ItemStack itemStack, ItemDisplayContext itemTransform) {
            this.itemStack = itemStack;
            this.itemTransform = itemTransform;
        }

        public ItemStack itemStack() {
            return this.itemStack;
        }

        public ItemDisplayContext itemTransform() {
            return this.itemTransform;
        }
    }
}
