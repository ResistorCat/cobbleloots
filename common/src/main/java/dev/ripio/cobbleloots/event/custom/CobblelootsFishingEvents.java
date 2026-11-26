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

    /**
     * Calculates the effective probability of fishing up a loot ball given the base chance,
     * Luck of the Sea multiplier, and enchantment level.
     *
     * @param baseChance     Base fishing probability (typically in [0.0, 1.0])
     * @param luckMultiplier Multiplicative factor applied per level of Luck of the Sea
     * @param luckLevel      Level of the Luck of the Sea enchantment on the rod
     * @return Effective probability clamped to [0.0, 1.0]
     */
    public static float calculateFishingChance(float baseChance, float luckMultiplier, int luckLevel) {
        if (baseChance <= 0.0F) {
            return 0.0F;
        }

        float chance = baseChance;
        if (luckLevel > 0 && luckMultiplier > 0.0F) {
            chance *= (float) Math.pow(luckMultiplier, luckLevel);
        }

        return Math.min(chance, 1.0F);
    }

    public static void onFishingSuccess(ServerLevel level, ServerPlayer player, BlockPos hookPos, ItemStack rodStack) {
        if (!CobblelootsConfig.fishing_enabled) {
            return;
        }

        int luckLevel = getLuckOfTheSeaLevel(level, rodStack);
        float chance = calculateFishingChance(
                CobblelootsConfig.fishing_chance,
                CobblelootsConfig.fishing_luck_of_the_sea_multiplier,
                luckLevel);

        if (chance <= 0.0F || level.getRandom().nextFloat() >= chance) {
            return;
        }

        LevelChunk chunk = level.getChunkAt(hookPos);
        var lootBall = CobblelootsLootBallEvents.spawnLootBall(level, chunk, hookPos, level.getRandom(),
                CobblelootsSourceType.FISHING, player, rodStack);

        if (lootBall == null) {
            return;
        }

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

    private static int getLuckOfTheSeaLevel(ServerLevel level, ItemStack rodStack) {
        if (rodStack == null || rodStack.isEmpty()) {
            return 0;
        }

        return level.registryAccess()
                .lookup(Registries.ENCHANTMENT)
                .flatMap(registry -> registry.get(Enchantments.LUCK_OF_THE_SEA))
                .map(enchantment -> EnchantmentHelper.getItemEnchantmentLevel(enchantment, rodStack))
                .orElse(0);
    }
}
