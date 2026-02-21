package dev.ripio.cobbleloots.event.compat;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.fishing.PokerodReelEvent;
import dev.ripio.cobbleloots.Cobbleloots;
import dev.ripio.cobbleloots.event.custom.CobblelootsFishingEvents;
import kotlin.Unit;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.FishingHook;

import java.lang.reflect.Field;

public class CobblemonCompatEvents {

    public static void register() {
        CobblemonEvents.POKEROD_REEL.subscribe(Priority.NORMAL, (event) -> {
            handlePokerodReel(event);
            return Unit.INSTANCE;
        });
    }

    private static void handlePokerodReel(PokerodReelEvent event) {
        ServerPlayer player = (ServerPlayer) event.getPlayer();
        FishingHook bobber = player.fishing;

        if (bobber != null) {
            boolean hasCatch = false;
            try {
                // Check 'caughtFish' (boolean) which was identified in logs
                Field caughtFishField = getFieldRecursive(bobber.getClass(), "caughtFish");
                if (caughtFishField != null) {
                    caughtFishField.setAccessible(true);
                    hasCatch = (boolean) caughtFishField.get(bobber);
                } else {
                    // Fallback to checking 'hookCountdown' > 0 if caughtFish is missing?
                    Field hookCountdownField = getFieldRecursive(bobber.getClass(), "hookCountdown");
                    if (hookCountdownField != null) {
                        hookCountdownField.setAccessible(true);
                        int hookCountdown = (int) hookCountdownField.get(bobber);
                        if (hookCountdown > 0) {
                            hasCatch = true;
                        }
                    }
                }
            } catch (Exception e) {
                Cobbleloots.LOGGER.error("Failed to check bobber state via reflection", e);
            }

            if (hasCatch) {
                CobblelootsFishingEvents.onFishingSuccess((net.minecraft.server.level.ServerLevel) player.level(),
                        player, bobber.blockPosition(),
                        event.getRod());
            }
        }
    }

    // Helper to find field in superclasses
    private static Field getFieldRecursive(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }
}
