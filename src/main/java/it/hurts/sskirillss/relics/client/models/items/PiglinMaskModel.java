package it.hurts.sskirillss.relics.client.models.items;

import com.google.common.collect.ImmutableList;
import it.hurts.sskirillss.relics.Relics;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class PiglinMaskModel extends HumanoidModel<LivingEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "piglin_mask"), "piglin_mask");

    public ModelPart headPart;

    public PiglinMaskModel(ModelPart root) {
        super(root);

        this.headPart = root.getChild("head");
    }

    public static LayerDefinition constructLayerDefinition() {
        MeshDefinition meshDefinition = HumanoidModel.createMesh(new CubeDeformation(0.4F), 0F);
        PartDefinition partDefinition = meshDefinition.getRoot();

        PartDefinition head = partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(24, 28).addBox(-2.0F, -2.6603F, -6.0049F, 4.0F, 3.0F, 1.0F, new CubeDeformation(0.25F))
                .texOffs(0, 10).addBox(-5.0F, -8.0103F, -4.5049F, 10.0F, 8.0F, 2.0F, new CubeDeformation(0.25F))
                .texOffs(0, 20).addBox(-5.0F, -8.0103F, -4.5049F, 10.0F, 8.0F, 2.0F, new CubeDeformation(0.5F))
                .texOffs(0, 0).addBox(-4.0F, -5.0103F, -4.0049F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.5103F, 0.0049F));

        PartDefinition head_r1 = head.addOrReplaceChild("head_r1", CubeListBuilder.create().texOffs(24, 19).addBox(0.0F, -2.5F, -2.0F, 1.0F, 5.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-5.45F, -7.4603F, -5.0049F, -1.3491F, -0.2421F, 0.6391F));

        PartDefinition head_r2 = head.addOrReplaceChild("head_r2", CubeListBuilder.create().texOffs(24, 10).addBox(-1.0F, -2.5F, -2.0F, 1.0F, 5.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(5.45F, -7.4603F, -5.0049F, -1.3491F, 0.2421F, -0.6391F));

        PartDefinition head_r3 = head.addOrReplaceChild("head_r3", CubeListBuilder.create().texOffs(4, 30).addBox(-2.25F, -2.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-3.1699F, -1.1904F, -5.2367F, 0.733F, -0.7627F, -0.9163F));

        PartDefinition head_r4 = head.addOrReplaceChild("head_r4", CubeListBuilder.create().texOffs(12, 30).addBox(0.0F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-3.5F, -0.7603F, -5.7549F, 0.733F, -0.7627F, -0.9163F));

        PartDefinition head_r5 = head.addOrReplaceChild("head_r5", CubeListBuilder.create().texOffs(0, 30).addBox(1.25F, -2.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(3.1699F, -1.1904F, -5.2367F, 0.733F, 0.7627F, 0.9163F));

        PartDefinition head_r6 = head.addOrReplaceChild("head_r6", CubeListBuilder.create().texOffs(8, 30).addBox(-1.0F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(3.5F, -0.7603F, -5.7549F, 0.733F, 0.7627F, 0.9163F));

        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of(this.headPart);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of();
    }
}