package dev.ripio.cobbleloots.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ripio.cobbleloots.entity.custom.CobblelootsLootBall;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class CobblelootsLootBallRenderer extends LivingEntityRenderer<CobblelootsLootBall, CobblelootsLootBallModel<CobblelootsLootBall>> {
  ResourceLocation DEFAULT_TEXTURE = ResourceLocation.fromNamespaceAndPath("cobblemon","textures/poke_balls/poke_ball.png");

  public CobblelootsLootBallRenderer(EntityRendererProvider.Context context) {
    super(context, new CobblelootsLootBallModel<>(context.bakeLayer(CobblelootsLootBallModel.LAYER_LOCATION)), 0.5f);
  }

  @Override
  public @NotNull ResourceLocation getTextureLocation(CobblelootsLootBall entity) {
    ResourceLocation texture = entity.getTexture();
    return texture == null || texture.getPath().isEmpty() ? DEFAULT_TEXTURE : texture;
  }

  @Override
  public void render(CobblelootsLootBall livingEntity, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
    super.render(livingEntity, f, g, poseStack, multiBufferSource, i);
  }

  @Override
  protected boolean shouldShowName(CobblelootsLootBall livingEntity) {
    return super.shouldShowName(livingEntity) && livingEntity.hasCustomName();
  }
}
