package dev.ripio.cobbleloots.entity.client;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.ripio.cobbleloots.entity.custom.CobblelootsLootBall;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import org.jetbrains.annotations.NotNull;

import static dev.ripio.cobbleloots.util.CobblelootsUtils.cobblelootsResource;

public class CobblelootsLootBallModel<T extends CobblelootsLootBall> extends HierarchicalModel<T> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(cobblelootsResource("loot_ball"), "main");
	private final ModelPart main;
	private final ModelPart ball;
	private final ModelPart lid;

	public CobblelootsLootBallModel(ModelPart root) {
		this.main = root.getChild("main");
		this.ball = this.main.getChild("ball");
		this.lid = this.ball.getChild("lid");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition main = partdefinition.addOrReplaceChild("main", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition ball = main.addOrReplaceChild("ball", CubeListBuilder.create().texOffs(32, 20).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 4.0F, 8.0F, new CubeDeformation(0.25F))
				.texOffs(32, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition lid = ball.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -8.0F, 8.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(-1, -1).addBox(-1.0F, -1.0F, -8.25F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 20).addBox(-4.0F, -4.0F, -8.0F, 8.0F, 4.0F, 8.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, -4.0F, 4.0F));

		return LayerDefinition.create(meshdefinition, 64, 32);
	}

	@Override
	public void setupAnim(CobblelootsLootBall entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		this.animate(entity.openingAnimationState, CobblelootsLootBallAnimations.ANIM_LOOT_BALL_SHAKE, ageInTicks, 1f);
		this.animate(entity.openingAnimationState, CobblelootsLootBallAnimations.ANIM_LOOT_BALL_LID_OPEN, ageInTicks, 1f);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		main.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}

	@Override
	public @NotNull ModelPart root() {
		return main;
	}


}