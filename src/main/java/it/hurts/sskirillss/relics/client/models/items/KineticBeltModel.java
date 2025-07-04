package it.hurts.sskirillss.relics.client.models.items;

import com.google.common.collect.ImmutableList;
import it.hurts.sskirillss.relics.client.renderer.items.base.INecklaceModel;
import it.hurts.sskirillss.relics.utils.Reference;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class KineticBeltModel extends HumanoidModel<LivingEntity> implements INecklaceModel<LivingEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Reference.MODID, "kinetic_belt"), "kinetic_belt");

    public ModelPart bodyPart;

    public KineticBeltModel(ModelPart root) {
        super(root);

        this.bodyPart = root.getChild("body");
    }

    public static LayerDefinition constructLayerDefinition() {
        MeshDefinition meshDefinition = HumanoidModel.createMesh(new CubeDeformation(0.4F), 0F);
        PartDefinition partDefinition = meshDefinition.getRoot();

        PartDefinition body = partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, 8.5F, -2.5F, 9.0F, 3.0F, 5.0F, new CubeDeformation(-0.25F))
                .texOffs(4, 24).addBox(-4.5F, 8.5F, -2.5F, 9.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 12.5F, 0.0F));

        PartDefinition cube_r1 = body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(10, 18).addBox(-1.5F, -1.5F, -2.375F, 3.0F, 3.0F, 3.0F, new CubeDeformation(-0.25F))
                .texOffs(0, 8).addBox(-1.5F, -1.5F, -2.375F, 3.0F, 3.0F, 3.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(0.0F, 10.0F, -0.875F, 0.0F, 0.0F, 0.7854F));

        return LayerDefinition.create(meshDefinition, 32, 32);
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of(this.bodyPart);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of();
    }

    @Override
    public ModelPart getBodyPart() {
        return bodyPart;
    }
}