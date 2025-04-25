package it.hurts.sskirillss.relics.items.relics.feet;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.client.models.items.CurioModel;
import it.hurts.sskirillss.relics.client.models.items.SidedCurioModel;
import it.hurts.sskirillss.relics.init.SoundRegistry;
import it.hurts.sskirillss.relics.init.ScalingModelRegistry;
import it.hurts.sskirillss.relics.items.relics.base.IRenderableCurio;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.CastData;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastStage;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastType;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.*;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemColor;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemShape;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.style.StyleTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.style.TooltipData;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

import java.util.List;

public class SpringyBootItem extends RelicItem implements IRenderableCurio {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("bounce")
                                .active(CastData.builder()
                                        .type(CastType.TOGGLEABLE)
                                        .build())
                                .stat(StatTemplate.builder("power")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.1D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 1))
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .maxLevel(10)
                        .step(100)
                        .sources(LevelingSourcesTemplate.builder()
                                .source(LevelingSourceTemplate.abilityBuilder("bounce")
                                        .initialValue(1)
                                        .gem(GemShape.SQUARE, GemColor.YELLOW)
                                        .build())
                                .build())
                        .build())
                .style(StyleTemplate.builder()
                        .tooltip(TooltipData.builder()
                                .borderTop(0xff8a5610)
                                .borderBottom(0xff275504)
                                .textured(true)
                                .build())
                        .build())
                .loot(LootTemplate.builder()
                        .entry(LootEntries.MOUNTAIN)
                        .build())
                .build();
    }

    @Override
    public void castActiveAbility(ItemStack stack, Player player, String ability, CastType type, CastStage stage) {
        var level = player.level();

        if (ability.equals("bounce") && stage == CastStage.START && player.onGround()) {
            var motion = player.getKnownMovement();

            var speed = getStatValue(entity, stack, "bounce", "power") * 1.25F;

            player.setDeltaMovement(motion.x, speed, motion.z);

            var random = level.getRandom();

            level.playSound(player, player.blockPosition(), SoundRegistry.SPRING_BOING.get(), SoundSource.PLAYERS, (float) Math.min(2F, 0.25F + speed * 0.5F), (float) Math.max(0.1F, 2F - speed * 0.75F));

            for (float i = 0; i < speed * 3F; i += 0.1F)
                level.addParticle(ParticleTypes.CLOUD, player.getX(), player.getY(), player.getZ(), MathUtils.randomFloat(random) * speed * 0.15F, random.nextFloat() * 0.1F, MathUtils.randomFloat(random) * speed * 0.15F);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public CurioModel getModel(ItemStack stack) {
        return new SidedCurioModel(stack.getItem());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack matrixStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource renderTypeBuffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        CurioModel model = getModel(stack);

        if (!(model instanceof SidedCurioModel sidedModel))
            return;

        sidedModel.setSlot(slotContext.index());

        matrixStack.pushPose();

        LivingEntity entity = slotContext.entity();

        sidedModel.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
        sidedModel.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        ICurioRenderer.followBodyRotations(entity, sidedModel);

        VertexConsumer vertexconsumer = ItemRenderer.getArmorFoilBuffer(renderTypeBuffer, RenderType.armorCutoutNoCull(getTexture(stack)), stack.hasFoil());

        sidedModel.renderToBuffer(matrixStack, vertexconsumer, light, OverlayTexture.NO_OVERLAY);

        matrixStack.popPose();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public LayerDefinition constructLayerDefinition() {
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(0.4F), 0.0F);

        PartDefinition right_leg = mesh.getRoot().addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(40, 4).addBox(-2.5F, 5.25F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(0.305F))
                .texOffs(0, 10).addBox(-2.5F, 5.25F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 22).addBox(-2.5F, 12.25F, -5.5F, 5.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(20, 16).addBox(-2.5F, 8.25F, -5.5F, 5.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(20, 0).addBox(-2.5F, 8.25F, -5.5F, 5.0F, 4.0F, 3.0F, new CubeDeformation(0.3F)), PartPose.offset(-1.0F, 11.75F, 2.5F));

        right_leg.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(2, 22).addBox(3.5F, -1.0F, 1.52F, 0.0F, 3.0F, 1.0F, new CubeDeformation(0.001F))
                .texOffs(0, 22).addBox(10.5F, -1.0F, 1.52F, 0.0F, 3.0F, 1.0F, new CubeDeformation(0.001F))
                .texOffs(20, 10).addBox(3.5F, -1.0F, -1.48F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.0F, 8.25F, 1.4F, 0.3491F, 0.0F, 0.0F));

        PartDefinition left_leg = mesh.getRoot().addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(40, 4).addBox(-2.5F, 5.25F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(0.305F))
                .texOffs(0, 10).addBox(-2.5F, 5.25F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 22).addBox(-2.5F, 12.25F, -5.5F, 5.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(20, 16).addBox(-2.5F, 8.25F, -5.5F, 5.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(20, 0).addBox(-2.5F, 8.25F, -5.5F, 5.0F, 4.0F, 3.0F, new CubeDeformation(0.3F)), PartPose.offset(-1.0F, 11.75F, 2.5F));

        left_leg.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(2, 22).addBox(3.5F, -1.0F, 1.52F, 0.0F, 3.0F, 1.0F, new CubeDeformation(0.001F))
                .texOffs(0, 22).addBox(10.5F, -1.0F, 1.52F, 0.0F, 3.0F, 1.0F, new CubeDeformation(0.001F))
                .texOffs(20, 10).addBox(3.5F, -1.0F, -1.48F, 7.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.0F, 8.25F, 1.4F, 0.3491F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public List<String> bodyParts() {
        return Lists.newArrayList("right_leg", "left_leg");
    }
}