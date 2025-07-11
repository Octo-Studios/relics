package it.hurts.sskirillss.relics.client.models.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.hurts.sskirillss.relics.Relics;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class StunStarModel extends HumanoidModel<LivingEntity> {
    public static final ModelLayerLocation TEXTURE = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/mob_effect/effects/stun_star.png"), "star");

    private final ModelPart star;

    public StunStarModel(ModelPart root) {
        super(root);

        this.star = root.getChild("star");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition star = partdefinition.addOrReplaceChild("star", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -8.0F, -0.75F, 16.0F, 16.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 32).addBox(-8.0F, -8.0F, 0.0F, 16.0F, 16.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 16).addBox(-8.0F, -8.0F, 0.75F, 16.0F, 16.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 8.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int seed) {
        star.render(poseStack, vertexConsumer, packedLight, packedOverlay, seed);
    }
}