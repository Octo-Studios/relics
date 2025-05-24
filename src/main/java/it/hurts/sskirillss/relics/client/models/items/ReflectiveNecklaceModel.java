package it.hurts.sskirillss.relics.client.models.items;

import com.google.common.collect.ImmutableList;
import it.hurts.sskirillss.relics.utils.Reference;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class ReflectiveNecklaceModel extends HumanoidModel<LivingEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Reference.MODID, "reflective_necklace"), "reflective_necklace");

    public ModelPart bodyPart;

    public ReflectiveNecklaceModel(ModelPart root) {
        super(root);

        this.bodyPart = root.getChild("body");
    }

    public static LayerDefinition constructLayerDefinition() {
        MeshDefinition meshDefinition = HumanoidModel.createMesh(new CubeDeformation(0.4F), 0F);
        PartDefinition partDefinition = meshDefinition.getRoot();

        PartDefinition body = partDefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(-0.0643F, 15.9463F, -0.475F));

        PartDefinition amulet = body.addOrReplaceChild("amulet", CubeListBuilder.create().texOffs(18, 19).addBox(-2.0F, -3.0F, -1.35F, 4.0F, 6.0F, 3.0F, new CubeDeformation(-0.8F))
                .texOffs(0, 28).addBox(-2.0F, -3.0F, -1.35F, 4.0F, 6.0F, 3.0F, new CubeDeformation(-1.0F)), PartPose.offset(0.0643F, -11.9463F, -2.675F));

        PartDefinition cube_r1 = amulet.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 19).addBox(-1.0F, -1.0F, -3.5F, 2.0F, 2.0F, 7.0F, new CubeDeformation(-0.7F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.6109F, -1.5708F, 0.0F));

        PartDefinition cube_r2 = amulet.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 19).addBox(-1.0F, -1.0F, -3.5F, 2.0F, 2.0F, 7.0F, new CubeDeformation(-0.7F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.6109F, -1.5708F, 0.0F));

        PartDefinition cube_r3 = amulet.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 19).addBox(-1.0F, -1.0F, -3.5F, 2.0F, 2.0F, 7.0F, new CubeDeformation(-0.7F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(0, 0).addBox(-5.9357F, -13.4463F, -4.525F, 12.0F, 10.0F, 9.0F, new CubeDeformation(-2.0F)), PartPose.offset(0.0F, -4.75F, 0.0F));

        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of(this.bodyPart);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of();
    }
}