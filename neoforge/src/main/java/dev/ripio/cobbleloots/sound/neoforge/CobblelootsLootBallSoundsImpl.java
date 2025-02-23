package dev.ripio.cobbleloots.sound.neoforge;

import dev.ripio.cobbleloots.Cobbleloots;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CobblelootsLootBallSoundsImpl {
  private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, Cobbleloots.MOD_ID);

  private static final Holder<SoundEvent> LOOT_BALL_OPEN = SOUND_EVENTS.register("loot_ball_open", SoundEvent::createVariableRangeEvent);

  public static SoundEvent getFanfare() {
    return LOOT_BALL_OPEN.value();
  }

  public static void registerSounds(IEventBus modEventBus) {
    SOUND_EVENTS.register(modEventBus);
  }
}
