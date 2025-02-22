package dev.ripio.cobbleloots.sound;

import com.cobblemon.mod.common.CobblemonSounds;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public class CobblelootsLootBallSounds {
  @ExpectPlatform
  public static SoundEvent getFanfare() {
    throw new AssertionError();
  }

  public static SoundEvent getSetItemSound() {
    return SoundEvents.DECORATED_POT_INSERT;
  }

  public static SoundEvent getToggleInvisibilitySound() {
    return SoundEvents.WANDERING_TRADER_DRINK_POTION;
  }

  public static SoundEvent getToggleSparksSound(boolean sparks) {
    return sparks ? SoundEvents.AXE_WAX_OFF : SoundEvents.HONEYCOMB_WAX_ON;
  }

  public static SoundEvent getLidOpenSound() {
    return CobblemonSounds.GILDED_CHEST_OPEN;
  }

  public static SoundEvent getLidCloseSound() {
    return CobblemonSounds.GILDED_CHEST_CLOSE;
  }

  public static SoundEvent getPopItemSound() {
    return SoundEvents.TRIAL_SPAWNER_SPAWN_ITEM;
  }

}
