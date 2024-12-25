package dev.ripio.cobbleloots.entity.custom;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CobblelootsLootBall extends PathfinderMob {
    // Inventory
    private final NonNullList<ItemStack> itemStacks = NonNullList.withSize(1, ItemStack.EMPTY);

    protected CobblelootsLootBall(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }
}
