package dev.ripio.cobbleloots.fabric.entity;

import dev.ripio.cobbleloots.Cobbleloots;
import dev.ripio.cobbleloots.entity.custom.CobblelootsLootBall;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import static dev.ripio.cobbleloots.util.CobblelootsUtils.cobblelootsResource;

public class CobblelootsEntities {
  public static final EntityType<CobblelootsLootBall> LOOT_BALL = Registry.register(BuiltInRegistries.ENTITY_TYPE, cobblelootsResource("loot_ball"), EntityType.Builder.of(CobblelootsLootBall::new, MobCategory.MISC).sized(0.5f, 0.5f).build("loot_ball"));

  public static void registerEntities() {
    Cobbleloots.LOGGER.info("Registering entities");
    FabricDefaultAttributeRegistry.register(LOOT_BALL, CobblelootsLootBall.createAttributes());
  }
}
