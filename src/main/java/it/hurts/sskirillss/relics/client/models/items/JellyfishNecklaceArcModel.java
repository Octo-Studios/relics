package it.hurts.sskirillss.relics.client.models.items;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Entity;

public class JellyfishNecklaceArcModel<T extends Entity> extends EntityModel<T> {
    private final ModelPart part;

    public JellyfishNecklaceArcModel() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        PartDefinition model = partDefinition.addOrReplaceChild("arc", CubeListBuilder.create().texOffs(0, 3).addBox(-16.0F, -12.0F, 0.0F, 16.0F, 27.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 9.0F, 0.0F));

        this.part = LayerDefinition.create(meshDefinition, 32, 32).bakeRoot();
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        part.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}