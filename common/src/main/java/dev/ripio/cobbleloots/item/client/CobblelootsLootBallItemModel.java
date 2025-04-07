package dev.ripio.cobbleloots.item.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;

public class CobblelootsLootBallItemModel extends Model {
	private final ModelPart main;
	private final ModelPart ball;
	private final ModelPart lid;

	public CobblelootsLootBallItemModel(ModelPart root) {
		super(RenderType::entityCutout);
		this.main = root.getChild("main");
		this.ball = this.main.getChild("ball");
		this.lid = this.ball.getChild("lid");
	}

	public static LayerDefinition createLayer() {
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

	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, int k) {
		this.main.render(poseStack, vertexConsumer, i, j, k);
	}
}