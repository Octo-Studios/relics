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

public class SpringyBootModel extends HumanoidModel<LivingEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "springy_boot"), "springy_boot");

    public ModelPart feetPart;

    public SpringyBootModel(ModelPart root) {
        super(root);

        this.feetPart = root.getChild("feetPart");
    }

    public static LayerDefinition constructLayerDefinition() {
        MeshDefinition meshDefinition = HumanoidModel.createMesh(new CubeDeformation(0.4F), 0F);
        PartDefinition partDefinition = meshDefinition.getRoot();

        PartDefinition feetPart = partDefinition.addOrReplaceChild("feetPart", CubeListBuilder.create().texOffs(20, 5).addBox(-2.5F, 9.5F, -4.5F, 5.0F, 3.0F, 2.0F, new CubeDeformation(0.25F))
                .texOffs(0, 12).addBox(-2.5F, 6.5F, -2.5F, 5.0F, 6.0F, 5.0F, new CubeDeformation(0.25F))
                .texOffs(0, 1).addBox(-2.5F, 6.5F, -2.5F, 5.0F, 6.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(20, 0).addBox(-2.5F, 9.5F, -4.5F, 5.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 11.5F, 0.0F));

        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of(this.feetPart);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of();
    }
}