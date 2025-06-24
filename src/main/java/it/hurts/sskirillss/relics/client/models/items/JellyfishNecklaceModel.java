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

public class JellyfishNecklaceModel extends HumanoidModel<LivingEntity> implements INecklaceModel<LivingEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Reference.MODID, "jellyfish_necklace"), "jellyfish_necklace");

    public ModelPart bodyPart;

    public JellyfishNecklaceModel(ModelPart root) {
        super(root);

        this.bodyPart = root.getChild("body");
    }

    public static LayerDefinition constructLayerDefinition() {
        MeshDefinition meshDefinition = HumanoidModel.createMesh(new CubeDeformation(0.4F), 0F);
        PartDefinition partDefinition = meshDefinition.getRoot();

        PartDefinition body = partDefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(-0.0643F, 15.9463F, -0.475F));

        PartDefinition amulet = body.addOrReplaceChild("pendant", CubeListBuilder.create().texOffs(15, 30).addBox(-2.0F, -1.0F, -1.85F, 4.0F, 4.0F, 3.0F, new CubeDeformation(-1.0F))
                .texOffs(31, 34).addBox(-1.5F, 1.35F, -1.35F, 3.0F, 1.0F, 2.0F, new CubeDeformation(-0.3F)), PartPose.offset(0.0643F, -13.9463F, -2.675F));

        PartDefinition cube_r1 = amulet.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(2, 31).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(0.0F, 2.4F, -1.1F, 0.0F, 0.0F, 0.1745F));

        PartDefinition cube_r2 = amulet.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(17, 23).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(0.1F, 2.4F, -1.6F, 0.0F, 0.0F, -0.1745F));

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

    @Override
    public ModelPart getBodyPart() {
        return bodyPart;
    }
}