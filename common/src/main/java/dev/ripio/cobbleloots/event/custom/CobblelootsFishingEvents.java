package dev.ripio.cobbleloots.event.custom;

import dev.ripio.cobbleloots.config.CobblelootsConfig;
import dev.ripio.cobbleloots.util.enums.CobblelootsSourceType;
import net.minecraft.core.BlockPos;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.chunk.LevelChunk;

public class CobblelootsFishingEvents {

    public static void onFishingSuccess(ServerLevel level, ServerPlayer player, BlockPos hookPos, ItemStack rodStack) {
        if (!CobblelootsConfig.fishing_enabled) {
            return;
        }

        // Calculate chance with Luck of the Sea
        float chance = CobblelootsConfig.fishing_chance;
        int luckLevel = EnchantmentHelper.getItemEnchantmentLevel(
                level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LUCK_OF_THE_SEA),
                rodStack);
        if (luckLevel > 0) {
            chance *= (float) Math.pow(CobblelootsConfig.fishing_luck_of_the_sea_multiplier, luckLevel);
        }

        // Random chance
        if (level.getRandom().nextFloat() > chance) {
            return;
        }

        LevelChunk chunk = level.getChunkAt(hookPos);
        CobblelootsLootBallEvents.spawnLootBall(level, chunk, hookPos, level.getRandom(),
                CobblelootsSourceType.FISHING, player, rodStack);
    }
}
