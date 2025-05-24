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

        PartDefinition body = partDefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(-0.0643F, 1.9463F, -0.475F));

        PartDefinition amulet = body.addOrReplaceChild("pendant", CubeListBuilder.create().texOffs(18, 19).addBox(-2.0F, -1.0F, -1.35F, 4.0F, 6.0F, 3.0F, new CubeDeformation(-0.8F))
                .texOffs(0, 28).addBox(-2.0F, -1.0F, -1.35F, 4.0F, 6.0F, 3.0F, new CubeDeformation(-1.0F)), PartPose.offset(0.0643F, -13.9463F, -2.675F));

        PartDefinition cube_r1 = amulet.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(14, 29).addBox(-3.0F, -3.0F, -1.5F, 5.0F, 5.0F, 3.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(0.0F, 2.75F, 0.25F, 0.0F, 0.0F, 0.7854F));

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