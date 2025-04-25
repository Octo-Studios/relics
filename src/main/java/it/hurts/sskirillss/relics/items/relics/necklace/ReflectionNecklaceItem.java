package it.hurts.sskirillss.relics.items.relics.necklace;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.hurts.sskirillss.relics.client.models.items.CurioModel;
import it.hurts.sskirillss.relics.entities.StalactiteEntity;
import it.hurts.sskirillss.relics.init.ItemRegistry;
import it.hurts.sskirillss.relics.init.ScalingModelRegistry;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.IRenderableCurio;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.style.StyleTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.style.TooltipData;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.Reference;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

import java.util.List;

import static it.hurts.sskirillss.relics.init.DataComponentRegistry.CHARGE;
import static it.hurts.sskirillss.relics.init.DataComponentRegistry.TIME;

public class ReflectionNecklaceItem extends RelicItem implements IRenderableCurio {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("explode")
                                .stat(StatTemplate.builder("capacity")
                                        .initialValue(20D, 60D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.35D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(StatTemplate.builder("damage")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.5D)
                                        .formatValue(value -> MathUtils.round(value, 2))
                                        .build())
                                .stat(StatTemplate.builder("stun")
                                        .initialValue(0.05D, 0.25D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.25D)
                                        .formatValue(value -> MathUtils.round(value, 2))
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .maxLevel(10)
                        .step(200)
                        .build())
                .style(StyleTemplate.builder()
                        .tooltip(TooltipData.builder()
                                .borderTop(0xff00baff)
                                .borderBottom(0xff0090a9)
                                .textured(true)
                                .build())
                        .build())
                .loot(LootTemplate.builder()
                        .entry(LootEntries.NETHER_LIKE, LootEntries.THE_NETHER)
                        .build())
                .build();
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player)
                || player.tickCount % 20 != 0)
            return;

        int time = stack.getOrDefault(TIME, 0);
        double charge = stack.getOrDefault(CHARGE, 0);

        if (time > 0 && charge < getStatValue(player, stack, "explode", "capacity")) {
            stack.set(TIME, --time);
        } else if (charge > 0) {
            Level level = player.level();
            RandomSource random = player.getRandom();

            float size = (float) (Math.log(charge) * 0.6F);
            float speed = (float) (0.35F + (charge * 0.001F));

            for (float i = -size; i <= size; i += 1) {
                for (float j = -size; j <= size; j += 1) {
                    for (float k = -size; k <= size; k += 1) {
                        double d3 = (double) j + (random.nextDouble() - random.nextDouble());
                        double d4 = (double) i + (random.nextDouble() - random.nextDouble());
                        double d5 = (double) k + (random.nextDouble() - random.nextDouble());

                        double d6 = (double) Mth.sqrt((float) (d3 * d3 + d4 * d4 + d5 * d5)) / speed + random.nextGaussian();

                        Vec3 motion = new Vec3(d3 / d6, d4 / d6, d5 / d6);

                        float mul = player.getBbHeight() / 1.5F;

                        Vec3 pos = player.position().add(0, mul, 0).add(motion.normalize().multiply(mul, mul, mul));

                        if (level.getBlockState(new BlockPos((int) pos.x, (int) pos.y, (int) pos.z)).blocksMotion())
                            continue;

                        StalactiteEntity stalactite = new StalactiteEntity(level,
                                (float) (charge * getStatValue(player, stack, "explode", "damage")),
                                (float) (charge * getStatValue(player, stack, "explode", "stun")));

                        stalactite.setOwner(player);
                        stalactite.setPos(pos);
                        stalactite.setDeltaMovement(motion);

                        level.addFreshEntity(stalactite);
                    }
                }
            }

            spreadRelicExperience(player, stack, (int) Math.floor(charge / 10F));

            stack.set(CHARGE, 0);
            stack.set(TIME, 0);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack matrixStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource renderTypeBuffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        CurioModel model = getModel(stack);

        matrixStack.pushPose();

        LivingEntity entity = slotContext.entity();

        model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
        model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        ICurioRenderer.translateIfSneaking(matrixStack, entity);
        ICurioRenderer.rotateIfSneaking(matrixStack, entity);

        ICurioRenderer.followBodyRotations(entity, model);

        VertexConsumer vertexconsumer = ItemRenderer.getArmorFoilBuffer(renderTypeBuffer, RenderType.armorCutoutNoCull(getTexture(stack)), stack.hasFoil());

        matrixStack.scale(0.5F, 0.5F, 0.5F);

        model.renderToBuffer(matrixStack, vertexconsumer, light, OverlayTexture.NO_OVERLAY);

        matrixStack.scale(2F, 2F, 2F);

        matrixStack.popPose();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public LayerDefinition constructLayerDefinition() {
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(0.4F), 0.0F);

        PartDefinition bone = mesh.getRoot().addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, 0.0F, -4.15F, 16.0F, 7.0F, 8.0F, new CubeDeformation(0.5F))
                .texOffs(0, 0).addBox(-1.5F, 5.125F, -5.15F, 3.0F, 5.0F, 1.0F, new CubeDeformation(-0.075F))
                .texOffs(0, 15).addBox(-1.0F, 6.375F, -5.775F, 2.0F, 3.0F, 1.0F, new CubeDeformation(-0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        bone.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(6, 15).addBox(-0.675F, -1.325F, -0.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 10.475F, -4.65F, 0.0F, 0.0F, -0.7854F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public List<String> headParts() {
        return Lists.newArrayList("body");
    }

    @EventBusSubscriber(modid = Reference.MODID)
    public static class ReflectionNecklaceServerEvents {
        @SubscribeEvent
        public static void onEntityHurt(LivingIncomingDamageEvent event) {
            if (!(event.getEntity() instanceof Player player))
                return;

            ItemStack stack = EntityUtils.findEquippedCurio(player, ItemRegistry.REFLECTION_NECKLACE.get());

            if (!(stack.getItem() instanceof IRelicItem relic))
                return;

            double charge = stack.getOrDefault(CHARGE, 0);
            double capacity = relic.getStatValue(player, stack, "explode", "capacity");

            if (charge < capacity) {
                stack.set(CHARGE, (int) Math.min(capacity, charge + (event.getAmount())));

                stack.set(TIME, 5);
            }
        }
    }
}