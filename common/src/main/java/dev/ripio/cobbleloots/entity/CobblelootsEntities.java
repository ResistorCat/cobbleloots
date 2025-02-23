package dev.ripio.cobbleloots.entity;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.ripio.cobbleloots.entity.custom.CobblelootsLootBall;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class CobblelootsEntities {
  public static EntityType<CobblelootsLootBall> getBaseLootBallEntityType() {
    return EntityType.Builder.of(CobblelootsLootBall::new, MobCategory.MISC).sized(0.5f, 0.5f).build("loot_ball");
  }

  @ExpectPlatform
  public static EntityType<CobblelootsLootBall> getLootBallEntityType() {
    throw new AssertionError();
  }
}
