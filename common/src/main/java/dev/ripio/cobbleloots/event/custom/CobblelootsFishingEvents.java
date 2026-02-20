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
        var lootBall = CobblelootsLootBallEvents.spawnLootBall(level, chunk, hookPos, level.getRandom(),
                CobblelootsSourceType.FISHING, player, rodStack);

        if (lootBall != null) {
            // Apply despawn delay if enabled
            if (CobblelootsConfig.fishing_despawn_time > 0) {
                lootBall.setDespawnTick(level.getGameTime() + CobblelootsConfig.fishing_despawn_time);
            }

            // Move loot ball towards player
            double d0 = player.getX() - lootBall.getX();
            double d1 = player.getY() - lootBall.getY();
            double d2 = player.getZ() - lootBall.getZ();
            double d3 = 0.1D;
            lootBall.setDeltaMovement(d0 * d3, d1 * d3 + Math.sqrt(Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2)) * 0.08D,
                    d2 * d3);
        }
    }
}
