package dev.ripio.cobbleloots.neoforge.entity;

import dev.ripio.cobbleloots.Cobbleloots;
import dev.ripio.cobbleloots.entity.custom.CobblelootsLootBall;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

import static dev.ripio.cobbleloots.util.CobblelootsUtils.cobblelootsResource;

@EventBusSubscriber(modid = Cobbleloots.MOD_ID)
public class CobblelootsEntities {
  @SubscribeEvent
  public static void registerEntities(RegisterEvent event) {
    event.register(
        BuiltInRegistries.ENTITY_TYPE,
        registry -> {
          registry.register(cobblelootsResource("loot_ball"));
        }
    );
  }
}
