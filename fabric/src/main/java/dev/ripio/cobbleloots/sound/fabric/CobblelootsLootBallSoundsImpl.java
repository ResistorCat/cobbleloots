package dev.ripio.cobbleloots.sound.fabric;


import dev.ripio.cobbleloots.Cobbleloots;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

import static dev.ripio.cobbleloots.util.CobblelootsUtils.cobblelootsResource;

public class CobblelootsLootBallSoundsImpl {
  private static final SoundEvent LOOT_BALL_OPEN = Registry.register(
      BuiltInRegistries.SOUND_EVENT,
      cobblelootsResource("loot_ball_open"),
      SoundEvent.createVariableRangeEvent(
          cobblelootsResource("loot_ball_open")
      )
  );

  public static void registerSounds() {
    Cobbleloots.LOGGER.info("Registering sounds");
  }

  public static SoundEvent getFanfare() {
    return LOOT_BALL_OPEN;
  }
}
