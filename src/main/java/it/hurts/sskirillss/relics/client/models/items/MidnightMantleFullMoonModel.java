package it.hurts.sskirillss.relics.client.models.items;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Entity;

public class MidnightMantleFullMoonModel<T extends Entity> extends EntityModel<T> {
    public ModelPart part;

    public MidnightMantleFullMoonModel() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        PartDefinition bone = partDefinition.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -17.25F, 7.0F, 16.0F, 16.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 18).addBox(-8.0F, -17.25F, 7.0F, 16.0F, 16.0F, 2.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.5F, 0.0F));

        this.part = LayerDefinition.create(meshDefinition, 64, 64).bakeRoot();
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        part.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}