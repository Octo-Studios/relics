package it.hurts.sskirillss.relics.items.relics.hands;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.hurts.sskirillss.relics.client.models.items.CurioModel;
import it.hurts.sskirillss.relics.client.models.items.SidedCurioModel;
import it.hurts.sskirillss.relics.client.models.items.SidedFPRCurioModel;
import it.hurts.sskirillss.relics.init.EffectRegistry;
import it.hurts.sskirillss.relics.init.ItemRegistry;
import it.hurts.sskirillss.relics.init.SoundRegistry;
import it.hurts.sskirillss.relics.init.ScalingModelRegistry;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.IRenderableCurio;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.CastData;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastStage;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastType;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.PacketPlayerMotion;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import it.hurts.sskirillss.relics.utils.Reference;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static it.hurts.sskirillss.relics.init.DataComponentRegistry.CHARGE;
import static it.hurts.sskirillss.relics.init.DataComponentRegistry.TIME;

public class RageGloveItem extends RelicItem implements IRenderableCurio {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("rage")
                                .maxLevel(10)
                                .stat(StatTemplate.builder("incoming_damage")
                                        .initialValue(0.05D, 0.025D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.05D)
                                        .formatValue(value -> MathUtils.round(MathUtils.round(value, 3) * 100, 3))
                                        .build())
                                .stat(StatTemplate.builder("dealt_damage")
                                        .initialValue(0.025D, 0.075D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.1D)
                                        .formatValue(value -> MathUtils.round(MathUtils.round(value, 3) * 100, 3))
                                        .build())
                                .stat(StatTemplate.builder("duration")
                                        .initialValue(2D, 4D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.1D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("phlebotomy")
                                .requiredLevel(5)
                                .maxLevel(10)
                                .stat(StatTemplate.builder("heal")
                                        .initialValue(0.0001D, 0.00025D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.1D)
                                        .formatValue(value -> MathUtils.round(MathUtils.round(value, 5) * 20, 5))
                                        .build())
                                .stat(StatTemplate.builder("movement_speed")
                                        .initialValue(0.01D, 0.025D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.1D)
                                        .formatValue(value -> MathUtils.round(MathUtils.round(value, 3) * 100, 3))
                                        .build())
                                .stat(StatTemplate.builder("attack_speed")
                                        .initialValue(0.005D, 0.01D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.05D)
                                        .formatValue(value -> MathUtils.round(MathUtils.round(value, 3) * 100, 3))
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("spurt")
                                .requiredLevel(10)
                                .maxLevel(10)
                                .active(CastData.builder()
                                        .type(CastType.INSTANTANEOUS)
                                        .build())
                                .stat(StatTemplate.builder("damage")
                                        .initialValue(0.1D, 0.25D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.1D)
                                        .formatValue(value -> MathUtils.round(value, 2))
                                        .build())
                                .stat(StatTemplate.builder("distance")
                                        .initialValue(3D, 8D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.3D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("cooldown")
                                        .initialValue(20, 15)
                                        .upgradeModifier(ScalingModelRegistry.EXPONENTIAL.get(), -0.075)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .build())
                        .build())
                .leveling(new LevelingTemplate(100, 20, 100))
                .loot(LootTemplate.builder()
                        .entry(LootEntries.NETHER_LIKE, LootEntries.THE_NETHER)
                        .build())
                .build();
    }

    @Override
    public void castActiveAbility(ItemStack stack, Player player, String ability, CastType type, CastStage stage) {
        Level level = player.getCommandSenderWorld();
        RandomSource random = level.getRandom();

        if (ability.equals("spurt")) {
            int stacks = stack.getOrDefault(CHARGE, 0);

            double maxDistance = getStatValue(entity, stack, "spurt", "distance");

            Vec3 view = player.getViewVector(0);
            Vec3 eyeVec = player.getEyePosition(0);

            BlockHitResult ray = level.clip(new ClipContext(eyeVec, eyeVec.add(view.x * maxDistance, view.y * maxDistance,
                    view.z * maxDistance), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

            Vec3 current = player.position();
            Vec3 target = ray.getLocation();

            int distance = (int) Math.ceil(current.distanceTo(target));

            if (distance <= 0)
                return;

            Vec3 motion = player.getDeltaMovement().add(target.subtract(current).normalize());

            player.teleportTo(target.x, target.y, target.z);

            if (!level.isClientSide()) {
                NetworkHandler.sendToClient(new PacketPlayerMotion(motion.x, motion.y, motion.z), (ServerPlayer) player);

                setAbilityCooldown(stack, "spurt", (int) Math.round(getStatValue(entity, stack, "spurt", "cooldown") * 20));
            }

            player.fallDistance = 0F;

            level.playSound(null, player.blockPosition(), SoundRegistry.SPURT.get(), SoundSource.MASTER, 1F, 0.75F + random.nextFloat() * 0.5F);

            Vec3 start = current.add(0, 1, 0);
            Vec3 end = target.add(0, 1, 0);

            Vec3 delta = end.subtract(start);
            Vec3 dir = delta.normalize();

            for (int i = 0; i < distance * 20; ++i) {
                double progress = i * delta.length() / (distance * 20);

                level.addParticle(ParticleUtils.constructSimpleSpark(new Color(255, 60 + random.nextInt(60), 0), 0.2F + random.nextFloat() * 0.5F,
                                60 + random.nextInt(60), 0.95F),
                        start.x + dir.x * progress, start.y + dir.y * progress,
                        start.z + dir.z * progress, 0, MathUtils.randomFloat(random) * 0.075F, 0);
            }

            List<LivingEntity> targets = new ArrayList<>();

            for (int i = 0; i < distance; ++i) {
                double progress = i * delta.length() / distance;

                for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, new AABB(new BlockPos((int) (start.x + dir.x * progress),
                        (int) (start.y + dir.y * progress), (int) (start.z + dir.z * progress))).inflate(0.5, 1, 0.5))) {
                    if (entity.getStringUUID().equals(player.getStringUUID())
                            || entity.isDeadOrDying())
                        continue;

                    targets.add(entity);
                }
            }

            if (!targets.isEmpty()) {
                EntityUtils.resetAttribute(player, stack, Attributes.ATTACK_SPEED, Integer.MAX_VALUE, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
                EntityUtils.resetAttribute(player, stack, Attributes.ATTACK_DAMAGE, (float) (getStatValue(entity, stack, "spurt", "damage") * stacks), AttributeModifier.Operation.ADD_VALUE);

                for (LivingEntity entity : targets) {
                    if (entity.invulnerableTime > 0 || EntityUtils.isAlliedTo(player, entity))
                        continue;

                    player.attack(entity);

                    spreadRelicExperience(player, stack, 1);

                    entity.addEffect(new MobEffectInstance(EffectRegistry.BLEEDING, 100, 0));
                    entity.setRemainingFireTicks(5 * 20);
                }

                EntityUtils.removeAttribute(player, stack, Attributes.ATTACK_DAMAGE, AttributeModifier.Operation.ADD_VALUE);
                EntityUtils.removeAttribute(player, stack, Attributes.ATTACK_SPEED, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
            }

            stack.set(CHARGE, 0);
        }
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player))
            return;

        if (isAbilityUnlocked(stack, "phlebotomy")) {
            float percentage = 100F - (player.getHealth() / player.getMaxHealth() * 100F);

            player.heal((float) getStatValue(entity, stack, "phlebotomy", "heal") * percentage);

            EntityUtils.resetAttribute(player, stack, Attributes.ATTACK_SPEED, (float) (getStatValue(entity, stack, "phlebotomy", "attack_speed") * percentage), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            EntityUtils.resetAttribute(player, stack, Attributes.MOVEMENT_SPEED, (float) (getStatValue(entity, stack, "phlebotomy", "movement_speed") * percentage), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        }

        if (isAbilityUnlocked(stack, "rage")) {
            int stacks = stack.getOrDefault(CHARGE, 0);

            if (stacks > 0) {
                int time = stack.getOrDefault(TIME, 0);

                if (time > 0)
                    stack.set(TIME, --time);
                else {
                    stack.set(CHARGE, 0);
                }
            }
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player)
                || stack.getItem() == newStack.getItem())
            return;

        EntityUtils.removeAttribute(player, stack, Attributes.ATTACK_SPEED, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        EntityUtils.removeAttribute(player, stack, Attributes.MOVEMENT_SPEED, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        stack.set(CHARGE, 0);
        stack.set(TIME, 0);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public CurioModel getModel(ItemStack stack) {
        return new SidedFPRCurioModel(stack.getItem());
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

        matrixStack.translate(0, 0, -0.025F);

        sidedModel.renderToBuffer(matrixStack, vertexconsumer, light, OverlayTexture.NO_OVERLAY);

        matrixStack.popPose();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public LayerDefinition constructLayerDefinition() {
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(0.4F), 0.0F);

        mesh.getRoot().addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-4.0F, 6.0F, -2.5F, 3.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 13).mirror().addBox(-1.0F, 6.0F, -2.5F, 3.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 0).mirror().addBox(-4.5F, 8.0F, -0.5F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-3.0F, 2.0F, -0.5F));

        mesh.getRoot().addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 0).addBox(1.0F, 6.0F, -2.5F, 3.0F, 7.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 13).addBox(-2.0F, 6.0F, -2.5F, 3.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(3.5F, 8.0F, -0.5F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, 2.0F, -0.5F));

        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public List<String> bodyParts() {
        return Lists.newArrayList("right_arm", "left_arm");
    }

    @EventBusSubscriber(modid = Reference.MODID)
    public static class Events {
        @SubscribeEvent
        public static void onLivingHurt(LivingIncomingDamageEvent event) {
            Entity source = event.getSource().getDirectEntity();

            if (source instanceof Player player) {
                if (!(event.getSource().getEntity() instanceof Player))
                    return;

                ItemStack stack = EntityUtils.findEquippedCurio(player, ItemRegistry.RAGE_GLOVE.get());

                if (!(stack.getItem() instanceof IRelicItem relic))
                    return;

                if (relic.isAbilityUnlocked(stack, "rage")) {
                    int stacks = stack.getOrDefault(CHARGE, 0);

                    stack.set(CHARGE, ++stacks);
                    stack.set(TIME, (int) Math.round(relic.getStatValue(entity, stack, "rage", "duration") * 20));

                    relic.spreadRelicExperience(player, stack, 1);

                    event.setAmount((float) (event.getAmount() + (event.getAmount() * (stacks * relic.getStatValue(entity, stack, "rage", "dealt_damage")))));
                }
            } else if (event.getEntity() instanceof Player player) {
                ItemStack stack = EntityUtils.findEquippedCurio(player, ItemRegistry.RAGE_GLOVE.get());

                if (!(stack.getItem() instanceof IRelicItem relic))
                    return;

                if (relic.isAbilityUnlocked(stack, "rage")) {
                    int stacks = stack.getOrDefault(CHARGE, 0);

                    if (stacks <= 0)
                        return;

                    event.setAmount((float) (event.getAmount() + (event.getAmount() * (stacks * relic.getStatValue(entity, stack, "rage", "incoming_damage")))));
                }
            }
        }
    }
}