package dev.ripio.cobbleloots.data.custom.filter;

import dev.ripio.cobbleloots.util.CobblelootsDefinitions;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class CobblelootsBlockFilter {
    private final TagKey<Block> spawn;
    private final TagKey<Block> base;

    public CobblelootsBlockFilter(TagKey<Block> spawn, TagKey<Block> base) {
        this.spawn = spawn;
        this.base = base;
    }

    public TagKey<Block> getSpawn() {
        return spawn;
    }

    public TagKey<Block> getBase() {
        return base;
    }

    public boolean isSpawnable(BlockState block) {
        return this.spawn.equals(CobblelootsDefinitions.EMPTY_BLOCK_TAG) || block.is(this.spawn);
    }

    public boolean isBase(BlockState block) {
        return this.base.equals(CobblelootsDefinitions.EMPTY_BLOCK_TAG) || block.is(this.base);
    }
}
