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

        PartDefinition bone = partdefinition.addOrReplaceChild("wings", CubeListBuilder.create(), PartPose.offset(-13.9643F, 4.2569F, -4.4757F));

        PartDefinition cube_r1 = bone.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(36, 0).addBox(-9.6586F, -5.283F, -0.2953F, 7.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 7.0F, -0.0657F, -0.1133F, 0.8328F));

        PartDefinition cube_r2 = bone.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(36, 4).addBox(-1.1998F, -6.0505F, -0.2962F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 7.0F, -0.0172F, -0.1298F, 0.4374F));

        PartDefinition cube_r3 = bone.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(34, 9).addBox(-4.9428F, -2.4742F, -1.0783F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(35, 35).addBox(-2.755F, -1.5004F, -0.2879F, 5.0F, 10.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(36, 25).addBox(-1.69F, -5.4947F, 0.2078F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.8905F, -2.0449F, 8.3029F, 0.0114F, 0.0441F, 0.2194F));

        PartDefinition cube_r4 = bone.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 0).addBox(-25.2504F, -5.453F, 0.4201F, 18.0F, 11.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 15).addBox(-7.2504F, 3.547F, 0.4201F, 9.0F, 14.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 11).addBox(-16.6577F, -9.1841F, 0.896F, 17.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(18, 15).addBox(-7.2504F, -5.453F, 0.4201F, 9.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 7.0F, -0.0916F, -0.1687F, 0.7675F));

        PartDefinition cube_r5 = bone.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(16, 36).addBox(-2.5F, 7.0F, -1.0F, 5.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.3486F, -0.6377F, 9.0425F, 0.0114F, 0.0441F, 0.2194F));

        PartDefinition cube_r6 = bone.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(0, 29).addBox(0.4267F, 4.2147F, 0.9002F, 8.0F, 12.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(18, 25).addBox(0.4267F, -5.7853F, 0.9002F, 8.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 7.0F, 0.0F, -0.053F, 0.4175F));

        PartDefinition cube_r7 = bone.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(34, 13).addBox(-0.5076F, -7.4805F, 0.9935F, 9.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 6.7F, 0.0F, -0.1402F, 0.4175F));

        PartDefinition bone2 = partdefinition.addOrReplaceChild("bone2", CubeListBuilder.create(), PartPose.offset(13.9643F, 4.2569F, -4.4757F));

        PartDefinition cube_r8 = bone2.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(36, 25).mirror().addBox(-2.31F, -5.4947F, 0.2078F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-10.8905F, -2.0449F, 8.3029F, 0.0114F, -0.0441F, -0.2194F));

        PartDefinition bone3 = partdefinition.addOrReplaceChild("bone3", CubeListBuilder.create(), PartPose.offset(13.9643F, 4.2569F, -4.4757F));

        PartDefinition cube_r9 = bone3.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(18, 15).mirror().addBox(-1.7496F, -5.453F, 0.4201F, 9.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -2.0F, 7.0F, -0.0916F, 0.1687F, -0.7675F));

        PartDefinition bone4 = partdefinition.addOrReplaceChild("bone4", CubeListBuilder.create(), PartPose.offset(13.9643F, 4.2569F, -4.4757F));

        PartDefinition cube_r10 = bone4.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(18, 25).mirror().addBox(-8.4267F, -5.7853F, 0.9002F, 8.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -2.0F, 7.0F, 0.0F, 0.053F, -0.4175F));

        PartDefinition bone5 = partdefinition.addOrReplaceChild("bone5", CubeListBuilder.create(), PartPose.offset(13.9643F, 4.2569F, -4.4757F));

        PartDefinition cube_r11 = bone5.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(35, 35).mirror().addBox(-2.245F, -1.5004F, -0.2879F, 5.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-10.8905F, -2.0449F, 8.3029F, 0.0114F, -0.0441F, -0.2194F));

        PartDefinition bone6 = partdefinition.addOrReplaceChild("bone6", CubeListBuilder.create(), PartPose.offset(13.9643F, 4.2569F, -4.4757F));

        PartDefinition cube_r12 = bone6.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(34, 13).mirror().addBox(-8.4924F, -7.4805F, 0.9935F, 9.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -2.0F, 6.7F, 0.0F, 0.1402F, -0.4175F));

        PartDefinition bone7 = partdefinition.addOrReplaceChild("bone7", CubeListBuilder.create(), PartPose.offset(13.9643F, 4.2569F, -4.4757F));

        PartDefinition cube_r13 = bone7.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(0, 11).mirror().addBox(-0.3423F, -9.1841F, 0.896F, 17.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -2.0F, 7.0F, -0.0916F, 0.1687F, -0.7675F));

        PartDefinition bone8 = partdefinition.addOrReplaceChild("bone8", CubeListBuilder.create(), PartPose.offset(13.9643F, 4.2569F, -4.4757F));

        PartDefinition cube_r14 = bone8.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(0, 15).mirror().addBox(-1.7496F, 3.547F, 0.4201F, 9.0F, 14.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -2.0F, 7.0F, -0.0916F, 0.1687F, -0.7675F));

        PartDefinition bone9 = partdefinition.addOrReplaceChild("bone9", CubeListBuilder.create(), PartPose.offset(13.9643F, 4.2569F, -4.4757F));

        PartDefinition cube_r15 = bone9.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(0, 29).mirror().addBox(-8.4267F, 4.2147F, 0.9002F, 8.0F, 12.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -2.0F, 7.0F, 0.0F, 0.053F, -0.4175F));

        PartDefinition bone10 = partdefinition.addOrReplaceChild("bone10", CubeListBuilder.create(), PartPose.offset(13.9643F, 4.2569F, -4.4757F));

        PartDefinition cube_r16 = bone10.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(16, 36).mirror().addBox(-2.5F, 7.0F, -1.0F, 5.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-10.3486F, -0.6377F, 9.0425F, 0.0114F, -0.0441F, -0.2194F));

        PartDefinition bone11 = partdefinition.addOrReplaceChild("bone11", CubeListBuilder.create(), PartPose.offset(13.9643F, 4.2569F, -4.4757F));

        PartDefinition cube_r17 = bone11.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(7.2504F, -5.453F, 0.4201F, 18.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -2.0F, 7.0F, -0.0916F, 0.1687F, -0.7675F));

        PartDefinition bone12 = partdefinition.addOrReplaceChild("bone12", CubeListBuilder.create(), PartPose.offset(13.9643F, 4.2569F, -4.4757F));

        PartDefinition cube_r18 = bone12.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(34, 9).mirror().addBox(-3.0572F, -2.4742F, -1.0783F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-10.8905F, -2.0449F, 8.3029F, 0.0114F, -0.0441F, -0.2194F));

        PartDefinition bone13 = partdefinition.addOrReplaceChild("bone13", CubeListBuilder.create(), PartPose.offset(13.9643F, 4.2569F, -4.4757F));

        PartDefinition cube_r19 = bone13.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(36, 4).mirror().addBox(-4.8002F, -6.0505F, -0.2962F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -2.0F, 7.0F, -0.0172F, 0.1298F, -0.4374F));

        PartDefinition bone14 = partdefinition.addOrReplaceChild("bone14", CubeListBuilder.create(), PartPose.offset(13.9643F, 4.2569F, -4.4757F));

        PartDefinition cube_r20 = bone14.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(36, 0).mirror().addBox(2.6586F, -5.283F, -0.2953F, 7.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -2.0F, 7.0F, -0.0657F, 0.1133F, -0.8328F));

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