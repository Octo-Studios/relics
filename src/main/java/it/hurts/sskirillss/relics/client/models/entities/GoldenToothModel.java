package it.hurts.sskirillss.relics.client.models.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Entity;

public class GoldenToothModel<T extends Entity> extends EntityModel<T> {
    private final ModelPart part;

    public GoldenToothModel() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -4.9286F, -1.5F, 6.0F, 5.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 8).addBox(-3.0F, -4.9286F, -1.5F, 6.0F, 5.0F, 3.0F, new CubeDeformation(0.25F))
                .texOffs(32, 33).addBox(3.0F, 0.0714F, 1.5F, -6.0F, -5.0F, -3.0F, new CubeDeformation(-0.5F))
                .texOffs(0, 16).addBox(-2.5F, -0.1786F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.25F))
                .texOffs(31, 14).addBox(-0.5F, 3.8214F, 1.0F, -2.0F, -4.0F, -2.0F, new CubeDeformation(-0.25F))
                .texOffs(29, 7).addBox(2.5F, 3.8214F, 1.0F, -2.0F, -4.0F, -2.0F, new CubeDeformation(-0.25F))
                .texOffs(8, 16).addBox(0.5F, -0.1786F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.25F)), PartPose.offset(0.0F, 19.9286F, 0.5F));

        this.part = LayerDefinition.create(meshdefinition, 32, 32).bakeRoot();
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int seed) {
        part.render(poseStack, buffer, packedLight, packedOverlay, seed);
    }
}