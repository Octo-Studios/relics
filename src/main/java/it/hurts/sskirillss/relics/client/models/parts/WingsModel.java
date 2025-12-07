package it.hurts.sskirillss.relics.client.models.parts;

import com.google.common.collect.ImmutableList;
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

import javax.annotation.Nonnull;

public class WingsModel extends HumanoidModel<LivingEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/parts/wings.png"), "wings");

    public WingsModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(0.4F), 0.0F);
        PartDefinition partdefinition = mesh.getRoot().getChild("body");

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0024F, 11.5212F, 1.5295F));

        PartDefinition right_wing = body.addOrReplaceChild("right_wing", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r1 = right_wing.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 35).mirror().addBox(-14.0259F, -19.7807F, 0.3887F, 20.0F, 29.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-11.175F, -13.925F, 0.0F, 0.3565F, -0.45F, -0.4508F));

        PartDefinition cube_r2 = right_wing.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-14.0259F, -5.7807F, 0.3887F, 20.0F, 15.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-11.175F, -13.925F, 0.0F, 0.2448F, -0.1544F, -0.3698F));

        PartDefinition cube_r3 = right_wing.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(16, 23).mirror().addBox(-4.4159F, -9.026F, 0.8009F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-3.175F, -8.425F, 1.7F, 0.2094F, -0.0556F, -0.4166F));

        PartDefinition cube_r4 = right_wing.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(8, 15).mirror().addBox(-15.9065F, 5.1622F, 2.5781F, 2.0F, 14.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-16.0829F, -9.6683F, 2.7428F, -0.4743F, -0.341F, 2.3299F));

        PartDefinition cube_r5 = right_wing.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(24, 15).mirror().addBox(-11.7256F, 5.9237F, 2.5781F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-16.175F, -9.425F, 2.2F, -0.494F, -0.1328F, 2.7759F));

        PartDefinition cube_r6 = right_wing.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(0, 15).mirror().addBox(-9.1755F, -9.4016F, 2.5781F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-3.175F, -8.425F, 1.7F, -0.0172F, -0.5098F, 0.347F));

        PartDefinition cube_r7 = right_wing.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(24, 23).mirror().addBox(-3.0018F, 6.3573F, 6.3213F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-11.175F, -13.925F, 3.7F, -1.0269F, 0.0929F, -1.1949F));

        PartDefinition cube_r8 = right_wing.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(16, 15).mirror().addBox(-1.0177F, -5.3312F, -0.7445F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-3.175F, -8.425F, 1.7F, -0.1756F, 0.2549F, -1.1834F));

        PartDefinition left_wing = body.addOrReplaceChild("left_wing", CubeListBuilder.create(), PartPose.offset(-0.0048F, 0.0F, 0.0F));

        PartDefinition cube_r9 = left_wing.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(0, 35).addBox(-5.9741F, -19.7807F, 0.3887F, 20.0F, 29.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.175F, -13.925F, 0.0F, 0.3565F, 0.45F, 0.4508F));

        PartDefinition cube_r10 = left_wing.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(0, 0).addBox(-5.9741F, -5.7807F, 0.3887F, 20.0F, 15.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.175F, -13.925F, 0.0F, 0.2448F, 0.1544F, 0.3698F));

        PartDefinition cube_r11 = left_wing.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(16, 23).addBox(2.4159F, -9.026F, 0.8009F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.175F, -8.425F, 1.7F, 0.2094F, 0.0556F, 0.4166F));

        PartDefinition cube_r12 = left_wing.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(8, 15).addBox(13.9065F, 5.1622F, 2.5781F, 2.0F, 14.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(16.0829F, -9.6683F, 2.7428F, -0.4743F, 0.341F, -2.3299F));

        PartDefinition cube_r13 = left_wing.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(24, 15).addBox(9.7256F, 5.9237F, 2.5781F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(16.175F, -9.425F, 2.2F, -0.494F, 0.1328F, -2.7759F));

        PartDefinition cube_r14 = left_wing.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(0, 15).addBox(7.1755F, -9.4016F, 2.5781F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.175F, -8.425F, 1.7F, -0.0172F, 0.5098F, -0.347F));

        PartDefinition cube_r15 = left_wing.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(24, 23).addBox(1.0018F, 6.3573F, 6.3213F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.175F, -13.925F, 3.7F, -1.0269F, -0.0929F, 1.1949F));

        PartDefinition cube_r16 = left_wing.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(16, 15).addBox(-0.9823F, -5.3312F, -0.7445F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.175F, -8.425F, 1.7F, -0.1756F, -0.2549F, 1.1834F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void renderToBuffer(PoseStack p_102034_, VertexConsumer p_102035_, int p_102036_, int p_102037_, int p_350361_) {
        body.skipDraw = true;

        super.renderToBuffer(p_102034_, p_102035_, p_102036_, p_102037_, p_350361_);
    }

    @Override
    @Nonnull
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of();
    }

    @Override
    @Nonnull
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(this.body);
    }
}