package dev.ripio.cobbleloots.data.custom.filter;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.List;

public class CobblelootsPokeRodFilter {

    private final List<ResourceLocation> rods;

    public CobblelootsPokeRodFilter(List<ResourceLocation> rods) {
        this.rods = rods;
    }

    public static final Codec<CobblelootsPokeRodFilter> CODEC = ResourceLocation.CODEC.listOf()
            .xmap(CobblelootsPokeRodFilter::new, CobblelootsPokeRodFilter::getRods);

    public boolean test(ServerLevel level, LevelChunk chunk, BlockPos pos, Player player, ItemStack tool) {
        if (rods == null || rods.isEmpty())
            return true;

        if (tool == null || tool.isEmpty())
            return false;

        ResourceLocation toolId = BuiltInRegistries.ITEM.getKey(tool.getItem());
        return rods.contains(toolId);
    }

    public List<ResourceLocation> getRods() {
        return rods;
    }
}
