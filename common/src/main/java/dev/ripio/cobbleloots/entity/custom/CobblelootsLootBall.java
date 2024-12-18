package dev.ripio.cobbleloots.entity.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CobblelootsLootBall extends PathfinderMob {
    public static final Codec<CobblelootsLootBall> LOOT_BALL_CODEC = RecordCodecBuilder.create(instance ->
        instance.group( // Define the fields within the instance
            Codec.STRING.fieldOf("s").forGetter(SomeObject::s), // String
            Codec.INT.optionalFieldOf("i", 0).forGetter(SomeObject::i), // Integer, defaults to 0 if field not present
            Codec.BOOL.fieldOf("b").forGetter(SomeObject::b) // Boolean
        ).apply(instance, SomeObject::new) // Define how to create the object
    );

    // Inventory
    private final NonNullList<ItemStack> itemStacks = NonNullList.withSize(1, ItemStack.EMPTY);

    protected CobblelootsLootBall(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }
}
