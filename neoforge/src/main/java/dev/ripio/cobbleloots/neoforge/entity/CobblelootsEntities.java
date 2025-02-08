package dev.ripio.cobbleloots.neoforge.entity;

import dev.ripio.cobbleloots.Cobbleloots;
import dev.ripio.cobbleloots.entity.custom.CobblelootsLootBall;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class CobblelootsEntities {
  public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Cobbleloots.MOD_ID);

  public static final Supplier<EntityType<CobblelootsLootBall>> LOOT_BALL = ENTITY_TYPES.register(
      "loot_ball",
      () -> EntityType.Builder.of(CobblelootsLootBall::new, MobCategory.MISC).sized(0.5f, 0.5f).build("loot_ball"));

  public static void register(IEventBus modEventBus) {
    ENTITY_TYPES.register(modEventBus);
  }
}
