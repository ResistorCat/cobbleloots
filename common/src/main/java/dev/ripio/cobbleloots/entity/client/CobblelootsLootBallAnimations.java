package dev.ripio.cobbleloots.entity.client;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

public class CobblelootsLootBallAnimations {
  public static final AnimationDefinition ANIM_LOOT_BALL_OPENING = AnimationDefinition.Builder.withLength(1f)
      .addAnimation("lid",
          new AnimationChannel(AnimationChannel.Targets.ROTATION,
              new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                  AnimationChannel.Interpolations.LINEAR),
              new Keyframe(1f, KeyframeAnimations.degreeVec(-75f, 0f, 0f),
                  AnimationChannel.Interpolations.LINEAR))).build();
}
