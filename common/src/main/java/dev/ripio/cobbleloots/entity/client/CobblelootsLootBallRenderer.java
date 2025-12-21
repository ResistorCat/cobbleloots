package dev.ripio.cobbleloots.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ripio.cobbleloots.entity.custom.CobblelootsLootBall;
import dev.ripio.cobbleloots.util.CobblelootsDefinitions;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class CobblelootsLootBallRenderer
    extends LivingEntityRenderer<CobblelootsLootBall, CobblelootsLootBallModel<CobblelootsLootBall>> {
  ResourceLocation DEFAULT_TEXTURE = ResourceLocation.fromNamespaceAndPath("cobblemon",
      "textures/poke_balls/strange_ball.png");

  public CobblelootsLootBallRenderer(EntityRendererProvider.Context context) {
    super(context, new CobblelootsLootBallModel<>(context.bakeLayer(CobblelootsLootBallModel.LAYER_LOCATION)), 0.5f);
  }

  @Override
  public @NotNull ResourceLocation getTextureLocation(CobblelootsLootBall entity) {
    ResourceLocation texture = entity.getTexture();
    return texture.equals(CobblelootsDefinitions.EMPTY_LOCATION) ? DEFAULT_TEXTURE : texture;
  }

  @Override
  public void render(CobblelootsLootBall livingEntity, float f, float g, PoseStack poseStack,
      MultiBufferSource multiBufferSource, int i) {
    if (!livingEntity.getDisplayItem().isEmpty()) {
      poseStack.pushPose();

      // Calculate rising animation
      float openingProgress = (float) (CobblelootsLootBall.LOOT_BALL_OPENING_TICKS - livingEntity.getOpeningTicks())
          / CobblelootsLootBall.LOOT_BALL_OPENING_TICKS;
      // Clamp between 0 and 1
      openingProgress = Math.max(0f, Math.min(1f, openingProgress));

      // Translate up
      // Start slightly above center and rise slowly
      double yOffset = 0.1D + (openingProgress * 0.5D);
      poseStack.translate(0.0D, yOffset, 0.0D);

      // Scale down a bit to not look huge
      poseStack.scale(0.75F, 0.75F, 0.75F);

      // Rotate
      long time = livingEntity.level().getGameTime();
      poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees((time + g) * 4));

      // Render the item
      net.minecraft.client.Minecraft.getInstance().getItemRenderer().renderStatic(
          livingEntity.getDisplayItem(),
          net.minecraft.world.item.ItemDisplayContext.GROUND,
          i,
          net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
          poseStack,
          multiBufferSource,
          livingEntity.level(),
          0);

      poseStack.popPose();
    }

    super.render(livingEntity, f, g, poseStack, multiBufferSource, i);
  }

  @Override
  protected boolean shouldShowName(CobblelootsLootBall livingEntity) {
    return super.shouldShowName(livingEntity) && livingEntity.hasCustomName();
  }
}
