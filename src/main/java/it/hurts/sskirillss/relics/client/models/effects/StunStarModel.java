package it.hurts.sskirillss.relics.client.models.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Entity;

public class StunStarModel<T extends Entity> extends EntityModel<T> {
    private final ModelPart part;

    public StunStarModel() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition star = partdefinition.addOrReplaceChild("part", CubeListBuilder.create().texOffs(8, 7).addBox(1.0F, -6.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(8, 15).addBox(-3.0F, -6.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 7).addBox(-1.0F, -9.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 4).addBox(-4.0F, -4.0F, -1.0F, 8.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -6.0F, -1.0F, 10.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(16, 7).addBox(-2.0F, -7.0F, -1.0F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(30, 7).addBox(-1.0F, -9.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.25F))
                .texOffs(16, 10).addBox(-3.0F, -3.0F, -1.0F, 6.0F, 3.0F, 2.0F, new CubeDeformation(0.25F))
                .texOffs(30, 0).addBox(-5.0F, -6.0F, -1.0F, 10.0F, 3.0F, 2.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        this.part = LayerDefinition.create(meshdefinition, 64, 64).bakeRoot();
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int seed) {
        part.render(poseStack, vertexConsumer, packedLight, packedOverlay, seed);
    }
}