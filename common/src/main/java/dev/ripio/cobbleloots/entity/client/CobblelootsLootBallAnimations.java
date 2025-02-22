package dev.ripio.cobbleloots.entity.client;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

public class CobblelootsLootBallAnimations {
  public static final AnimationDefinition ANIM_LOOT_BALL_SHAKE = AnimationDefinition.Builder.withLength(2.5f)
      .addAnimation("ball",
          new AnimationChannel(AnimationChannel.Targets.ROTATION,
              new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                  AnimationChannel.Interpolations.LINEAR),
              new Keyframe(0.125f, KeyframeAnimations.degreeVec(-10f, 0f, 5f),
                  AnimationChannel.Interpolations.CATMULLROM),
              new Keyframe(0.25f, KeyframeAnimations.degreeVec(-2.17f, -2.58f, -9.78f),
                  AnimationChannel.Interpolations.CATMULLROM),
              new Keyframe(0.375f, KeyframeAnimations.degreeVec(-2.6f, -7.16f, 0.45f),
                  AnimationChannel.Interpolations.CATMULLROM),
              new Keyframe(0.5f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                  AnimationChannel.Interpolations.CATMULLROM))).build();
  public static final AnimationDefinition ANIM_LOOT_BALL_LID_OPEN = AnimationDefinition.Builder.withLength(2.5f)
      .addAnimation("lid",
          new AnimationChannel(AnimationChannel.Targets.ROTATION,
              new Keyframe(0.70833f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                  AnimationChannel.Interpolations.LINEAR),
              new Keyframe(0.91667f, KeyframeAnimations.degreeVec(-75f, 0f, 0f),
                  AnimationChannel.Interpolations.CATMULLROM),
              new Keyframe(2.25f, KeyframeAnimations.degreeVec(-75f, 0f, 0f),
                  AnimationChannel.Interpolations.CATMULLROM),
              new Keyframe(2.5f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                  AnimationChannel.Interpolations.CATMULLROM))).build();
}
