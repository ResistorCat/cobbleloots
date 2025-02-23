package dev.ripio.cobbleloots.entity.fabric;

import dev.ripio.cobbleloots.Cobbleloots;
import dev.ripio.cobbleloots.entity.custom.CobblelootsLootBall;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;

import static dev.ripio.cobbleloots.entity.CobblelootsEntities.getBaseLootBallEntityType;
import static dev.ripio.cobbleloots.util.CobblelootsUtils.cobblelootsResource;

public class CobblelootsEntitiesImpl {
  private static final EntityType<CobblelootsLootBall> LOOT_BALL_ENTITY_TYPE = Registry.register(BuiltInRegistries.ENTITY_TYPE, cobblelootsResource("loot_ball"), getBaseLootBallEntityType());

  public static EntityType<CobblelootsLootBall> getLootBallEntityType() {
    return LOOT_BALL_ENTITY_TYPE;
  }

  public static void registerEntities() {
    Cobbleloots.LOGGER.info("Registering entities");
    FabricDefaultAttributeRegistry.register(LOOT_BALL_ENTITY_TYPE, CobblelootsLootBall.createAttributes());
  }
}
