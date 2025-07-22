//package it.hurts.sskirillss.relics.items.relics.feet;
//
//import com.google.common.collect.Lists;
//import com.mojang.blaze3d.vertex.PoseStack;
//import com.mojang.blaze3d.vertex.VertexConsumer;
//import it.hurts.sskirillss.relics.Relics;
//import it.hurts.sskirillss.relics.api.events.common.FluidCollisionEvent;
//import it.hurts.sskirillss.relics.api.relics.IRelicItem;
//import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
//import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
//import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
//import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
//import it.hurts.sskirillss.relics.client.models.items.base.CurioModel;
//import it.hurts.sskirillss.relics.client.models.items.base.SidedCurioModel;
//import it.hurts.sskirillss.relics.init.RelicsItems;
//import it.hurts.sskirillss.relics.init.ScalingModelRegistry;
//import it.hurts.sskirillss.relics.items.relics.base.IRenderableCurio;
//import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
//import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
//import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
//import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
//import it.hurts.sskirillss.relics.utils.EntityUtils;
//import it.hurts.sskirillss.relics.utils.MathUtils;
//import net.minecraft.client.model.EntityModel;
//import net.minecraft.client.model.HumanoidModel;
//import net.minecraft.client.model.geom.PartPose;
//import net.minecraft.client.model.geom.builders.*;
//import net.minecraft.client.renderer.MultiBufferSource;
//import net.minecraft.client.renderer.RenderType;
//import net.minecraft.client.renderer.entity.ItemRenderer;
//import net.minecraft.client.renderer.entity.RenderLayerParent;
//import net.minecraft.client.renderer.texture.OverlayTexture;
//import net.minecraft.tags.FluidTags;
//import net.minecraft.world.entity.Entity;
//import net.minecraft.world.entity.LivingEntity;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.level.Level;
//import net.neoforged.api.distmarker.Dist;
//import net.neoforged.api.distmarker.OnlyIn;
//import net.neoforged.bus.api.SubscribeEvent;
//import net.neoforged.fml.common.EventBusSubscriber;
//import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
//import top.theillusivec4.curios.api.SlotContext;
//import top.theillusivec4.curios.api.client.ICurioRenderer;
//
//import java.util.List;
//
//import static it.hurts.sskirillss.relics.init.DataComponentRegistry.CHARGE;
//
//@EventBusSubscriber(modid = Relics.MODID)
//public class MagmaWalkerItem extends RelicItem implements IRenderableCurio {
//    @Override
//    public RelicTemplate constructDefaultRelicTemplate() {
//        return RelicTemplate.builder()
//                .abilities(AbilitiesTemplate.builder()
//                        .ability(AbilityTemplate.builder("heat_resistance")
//                                .initialMaxLevel(0)
//                                .build())
//                        .ability(AbilityTemplate.builder("pace")
//                                .stat(StatTemplate.builder("time")
//                                        .initialValue(20D, 50D)
//                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.1D)
//                                        .formatValue(value -> (int) (MathUtils.round(value, 0)))
//                                        .build())
//                                .build())
//                        .build())
//                .leveling(LevelingTemplate.builder()
//                        .initialCost(100)
//                        .step(200)
//                        .build())
//                .loot(LootTemplate.builder()
//                        .entry(LootEntries.THE_NETHER, LootEntries.NETHER_LIKE)
//                        .build())
//                .build();
//    }
//
//    @Override
//    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
//        int heat = stack.getOrDefault(CHARGE, 0);
//
//        if (!(entity instanceof Player player) || player.tickCount % 20 != 0)
//            return;
//
//        if (heat > 0) {
//            if (heat > getStatValue(player, stack, "pace", "time"))
//                player.hurt(level.damageSources().hotFloor(), (float) (1F + ((heat - getStatValue(player, stack, "pace", "time")) / 10F)));
//
//            if (!level.getFluidState(player.blockPosition().below()).is(FluidTags.LAVA)
//                    && !level.getFluidState(player.blockPosition()).is(FluidTags.LAVA))
//                stack.set(CHARGE, --heat);
//        }
//    }
//
//    @Override
//    @OnlyIn(Dist.CLIENT)
//    public CurioModel getModel(ItemStack stack) {
//        return new SidedCurioModel(stack.getItem());
//    }
//
//    @Override
//    @OnlyIn(Dist.CLIENT)
//    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack matrixStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource renderTypeBuffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
//        CurioModel model = getModel(stack);
//
//        if (!(model instanceof SidedCurioModel sidedModel))
//            return;
//
//        sidedModel.setSlot(slotContext.index());
//
//        matrixStack.pushPose();
//
//        LivingEntity entity = slotContext.entity();
//
//        sidedModel.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
//        sidedModel.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
//
//        ICurioRenderer.followBodyRotations(entity, sidedModel);
//
//        VertexConsumer vertexconsumer = ItemRenderer.getArmorFoilBuffer(renderTypeBuffer, RenderType.armorCutoutNoCull(getTexture(stack)), stack.hasFoil());
//
//        matrixStack.translate(0, 0, -0.025F);
//
//        sidedModel.renderToBuffer(matrixStack, vertexconsumer, light, OverlayTexture.NO_OVERLAY);
//
//        matrixStack.popPose();
//    }
//
//    @Override
//    @OnlyIn(Dist.CLIENT)
//    public LayerDefinition constructLayerDefinition() {
//        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(0.4F), 0.0F);
//
//        PartDefinition rightLeg = mesh.getRoot().addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 9).addBox(-2.9F, 5.5F, -2.5F, 6.0F, 7.0F, 6.0F, new CubeDeformation(0.0F))
//                .texOffs(0, 1).addBox(-2.9F, 5.5F, -2.5F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.175F))
//                .texOffs(18, 9).addBox(-2.9F, 9.5F, -4.5F, 6.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.9F, 12.0F, 0.5F));
//
//        rightLeg.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, -1).addBox(1.5F, 5.5F, 4.4F, 0.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.3927F, 0.0F));
//        rightLeg.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, -1).mirror().addBox(-1.325F, 5.5F, 4.2F, 0.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));
//
//        PartDefinition leftLeg = mesh.getRoot().addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 9).addBox(-2.9F, 5.5F, -2.5F, 6.0F, 7.0F, 6.0F, new CubeDeformation(0.0F))
//                .texOffs(0, 1).addBox(-2.9F, 5.5F, -2.5F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.175F))
//                .texOffs(18, 9).addBox(-2.9F, 9.5F, -4.5F, 6.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.9F, 12.0F, 0.5F));
//
//        leftLeg.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, -1).addBox(1.5F, 5.5F, 4.4F, 0.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.3927F, 0.0F));
//        leftLeg.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, -1).mirror().addBox(-1.325F, 5.5F, 4.2F, 0.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));
//
//        return LayerDefinition.create(mesh, 64, 64);
//    }
//
//    @Override
//    public List<String> bodyParts() {
//        return Lists.newArrayList("right_leg", "left_leg");
//    }
//
//    @SubscribeEvent
//    public static void onLivingAttack(LivingIncomingDamageEvent event) {
//        ItemStack stack = EntityUtils.findEquippedCurio(event.getEntity(), RelicsItems.MAGMA_WALKER.get());
//
//        if (stack.getItem() instanceof IRelicItem relic && event.getSource() == event.getEntity().level().damageSources().hotFloor()
//                && stack.getOrDefault(CHARGE, 0) <= relic.getStatValue(event.getEntity(), stack, "pace", "time")) {
//            event.setCanceled(true);
//        }
//    }
//
//    @SubscribeEvent
//    public static void onFluidCollide(FluidCollisionEvent event) {
//        ItemStack stack = EntityUtils.findEquippedCurio(event.getEntity(), RelicsItems.MAGMA_WALKER.get());
//
//        if (!(event.getEntity() instanceof Player player) || !(stack.getItem() instanceof IRelicItem relic)
//                || !event.getFluid().is(FluidTags.LAVA) || player.isShiftKeyDown())
//            return;
//
//        if (player.tickCount % 20 == 0) {
//            int heat = stack.getOrDefault(CHARGE, 0);
//
//            stack.set(CHARGE, ++heat);
//
//            if (heat % 5 == 0)
//                relic.spreadRelicExperience(player, stack, 1);
//        }
//
//        event.setCanceled(true);
//    }
//}