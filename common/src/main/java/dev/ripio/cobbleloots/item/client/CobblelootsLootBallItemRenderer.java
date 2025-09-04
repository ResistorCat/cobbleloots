package dev.ripio.cobbleloots.item.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ripio.cobbleloots.data.CobblelootsDataProvider;
import dev.ripio.cobbleloots.data.custom.CobblelootsLootBallData;
import dev.ripio.cobbleloots.data.custom.CobblelootsLootBallVariantData;
import dev.ripio.cobbleloots.util.CobblelootsDefinitions;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.joml.Quaternionf;

public class CobblelootsLootBallItemRenderer {
  public static ResourceLocation DEFAULT_TEXTURE = ResourceLocation.fromNamespaceAndPath("cobblemon","textures/poke_balls/strange_ball.png");

  public static CobblelootsLootBallItemModel MODEL = new CobblelootsLootBallItemModel(CobblelootsLootBallItemModel.createLayer().bakeRoot());

  public static ResourceLocation getTexture(ItemStack stack) {
    CompoundTag compoundTag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    if (compoundTag.contains("Texture")) {
      ResourceLocation textureLocation = ResourceLocation.tryParse(compoundTag.getString("Texture"));
      if (textureLocation != null) {
        return textureLocation;
      }
    }
    return DEFAULT_TEXTURE;
  }

  public static void renderLootBallItem(ItemStack stack, ItemDisplayContext mode, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
    ResourceLocation texture = getTexture(stack);
    if (texture != null) {
      matrices.pushPose();
      if (mode == ItemDisplayContext.FIXED) {
        matrices.translate(0.5F, 1.75F, 0.5F);
        matrices.scale(1F, 1F, 1F);
        matrices.mulPose(new Quaternionf(0, 0, 1, 0 ));
      }
      if (mode == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
        matrices.translate(0.5F, 1.5F, 0.5F);
        matrices.scale(0.7F, 0.7F, 0.7F);
        matrices.mulPose(new Quaternionf(1, 0, 0, 0));
      }
      if (mode == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
        matrices.translate(0.5F, 1.5F, 0.5F);
        matrices.scale(0.7F, 0.7F, 0.7F);
        matrices.mulPose(new Quaternionf(1, 0, 0, 0));
      }
      if (mode == ItemDisplayContext.GROUND) {
        matrices.translate(0.5F, 1.25F, 0.5F);
        matrices.scale(0.6F, 0.6F, 0.6F);
        matrices.mulPose(new Quaternionf(1, 0, 0, 0));
      }
      if (mode == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
        matrices.translate(0.5F, 1.75F, 0.1F);
        matrices.mulPose(new Quaternionf(0.9029221, -0.0705408, -0.4232447, -0.0248899));
      }
      if (mode == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
        matrices.translate(0.5F, 1.75F, 0.1F);
        matrices.mulPose(new Quaternionf( 0.9029221, 0.0705408, 0.4232447, 0.0248899));
      }
      if (mode == ItemDisplayContext.GUI) {
        matrices.translate(0.5F, 1.75F, 0F);
        matrices.mulPose(new Quaternionf(0.9128798, 0.0633944, -0.3803666, -0.1339882));
      }
      MODEL.renderToBuffer(matrices, vertexConsumers.getBuffer(MODEL.renderType(texture)), light, overlay, -1);
      matrices.popPose();
    }
  }
}
