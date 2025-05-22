package it.hurts.sskirillss.relics.client.models.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Entity;

public class ReflectiveOrbModel<T extends Entity> extends EntityModel<T> {
    private final ModelPart needle;

    public ReflectiveOrbModel() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, 19.9571F, -6.0F));

        bone.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(69, 65).addBox(0.5303F, 0.5F, -4.0F, -1.0F, -1.0F, -6.0F, new CubeDeformation(1.5F))
                .texOffs(3, 16).addBox(-1.5303F, -1.5F, -5.5F, 3.0F, 3.0F, 5.0F, new CubeDeformation(-0.5F))
                .texOffs(22, 13).addBox(-0.4697F, -0.5F, -8.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0215F, 0.0214F, 10.25F, 0.0F, 0.0F, 0.7854F));

        bone.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(50, 68).addBox(1.5F, 1.5F, 2.5F, -3.0F, -3.0F, -5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 7.25F, 0.0F, 0.0F, 0.7854F));

        this.needle = LayerDefinition.create(meshdefinition, 64, 64).bakeRoot();
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int seed) {
        needle.render(poseStack, buffer, packedLight, packedOverlay, seed);
    }
}