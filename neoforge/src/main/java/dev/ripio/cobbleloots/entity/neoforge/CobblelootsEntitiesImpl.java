package dev.ripio.cobbleloots.entity.neoforge;

import dev.ripio.cobbleloots.Cobbleloots;
import dev.ripio.cobbleloots.entity.CobblelootsEntities;
import dev.ripio.cobbleloots.entity.custom.CobblelootsLootBall;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class CobblelootsEntitiesImpl {
  public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Cobbleloots.MOD_ID);

  private static final Supplier<EntityType<CobblelootsLootBall>> LOOT_BALL_ENTITY_TYPE = ENTITY_TYPES.register(
      "loot_ball",
      CobblelootsEntities::getBaseLootBallEntityType);

  public static EntityType<CobblelootsLootBall> getLootBallEntityType() {
    return LOOT_BALL_ENTITY_TYPE.get();
  }

  public static void registerEntities(IEventBus modEventBus) {
    Cobbleloots.LOGGER.info("Registering entities");
    ENTITY_TYPES.register(modEventBus);
  }
}
