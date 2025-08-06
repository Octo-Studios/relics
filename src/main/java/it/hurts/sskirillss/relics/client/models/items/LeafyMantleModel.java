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

public class LeafyMantleModel extends HumanoidModel<LivingEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "leafy_mantle"), "leafy_mantle");

    public ModelPart bodyPart;
    public ModelPart leftArmPart;
    public ModelPart rightArmPart;

    public LeafyMantleModel(ModelPart root) {
        super(root);

        this.bodyPart = root.getChild("body");
        this.leftArmPart = root.getChild("left_arm");
        this.rightArmPart = root.getChild("right_arm");
    }

    public static LayerDefinition constructLayerDefinition() {
        MeshDefinition meshDefinition = HumanoidModel.createMesh(new CubeDeformation(0.4F), 0F);
        PartDefinition partDefinition = meshDefinition.getRoot();

        PartDefinition body = partDefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(-0.0003F, 0.3556F, 0.0307F));

        PartDefinition cube_r1 = body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(44, 24).addBox(-5.501F, 0.3112F, -2.8444F, 11.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0003F, 0.2131F, 0.3331F, -0.1745F, 0.0F, 0.0F));

        PartDefinition cube_r2 = body.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(44, 49).addBox(-5.5F, -3.0F, 0.0F, 11.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0003F, 20.1503F, 9.4187F, 1.0036F, 0.0F, 0.0F));

        PartDefinition cube_r3 = body.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(44, 43).addBox(-5.5F, -3.0F, 0.0F, 11.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0003F, 16.9003F, 8.5187F, 0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r4 = body.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(44, 37).addBox(-5.5F, -3.0F, 0.0F, 11.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0003F, 12.9003F, 7.5687F, 0.6109F, 0.0F, 0.0F));

        PartDefinition cube_r5 = body.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(44, 31).addBox(-5.5F, -3.0F, 0.0F, 11.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0003F, 9.4003F, 6.8687F, 0.6109F, 0.0F, 0.0F));

        PartDefinition cube_r6 = body.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(22, 15).addBox(-5.5F, -1.0585F, 3.4436F, 11.0F, 24.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 15).addBox(-5.5F, -0.5585F, 3.1936F, 11.0F, 24.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(12, 39).addBox(-5.5F, -0.5585F, -2.8064F, 0.0F, 24.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 39).addBox(5.5F, -0.5585F, -2.8064F, 0.0F, 24.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(32, 6).addBox(-5.5F, -0.5585F, -2.8064F, 11.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0003F, -0.0365F, 0.3386F, 0.2182F, 0.0F, 0.0F));

        PartDefinition cube_r7 = body.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -2.2416F, 2.3602F, 9.0F, 8.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0003F, -0.0365F, 0.3386F, -0.4363F, 0.0F, 0.0F));

        PartDefinition cube_r8 = body.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(32, 0).addBox(-5.501F, 20.9365F, 11.0185F, 11.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0003F, -0.0365F, 0.3386F, -0.1309F, 0.0F, 0.0F));

        PartDefinition left_arm = partDefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(24, 39).addBox(-2.0F, -2.5F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(5.751F, 2.2082F, 0.0538F));

        PartDefinition right_arm = partDefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(44, 12).addBox(-3.0F, -2.5F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.751F, 2.2082F, 0.0538F));

        return LayerDefinition.create(meshDefinition, 128, 128);
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of(this.bodyPart, this.leftArmPart, this.rightArmPart);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of();
    }
}