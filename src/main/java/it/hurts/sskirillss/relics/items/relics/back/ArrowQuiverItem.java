package it.hurts.sskirillss.relics.items.relics.back;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.hurts.sskirillss.relics.api.events.common.ContainerSlotClickEvent;
import it.hurts.sskirillss.relics.client.models.items.CurioModel;
import it.hurts.sskirillss.relics.items.relics.base.data.style.StyleData;
import it.hurts.sskirillss.relics.entities.ArrowRainEntity;
import it.hurts.sskirillss.relics.init.EffectRegistry;
import it.hurts.sskirillss.relics.init.EntityRegistry;
import it.hurts.sskirillss.relics.init.ItemRegistry;
import it.hurts.sskirillss.relics.init.SoundRegistry;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.IRenderableCurio;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.CastPredicate;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastStage;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastType;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilitiesData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilityData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.StatData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.UpgradeOperation;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootData;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootCollections;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.PacketPlayerMotion;
import it.hurts.sskirillss.relics.utils.*;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingGetProjectileEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ArrowQuiverItem extends RelicItem implements IRenderableCurio {
    private static final String TAG_LEAP = "leap";
    private static final String TAG_ARROWS = "arrows";

    @Override
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("receptacle")
                                .maxLevel(10)
                                .stat(StatData.builder("slots")
                                        .initialValue(2, 5)
                                        .upgradeModifier(UpgradeOperation.ADD, 1)
                                        .formatValue(value -> (int) Math.round(value))
                                        .build())
                                .build())
                        .ability(AbilityData.builder("leap")
                                .requiredLevel(5)
                                .maxLevel(10)
                                .active(CastType.INSTANTANEOUS, CastPredicate.builder()
                                        .predicate("target", data -> {
                                            Player player = data.getPlayer();
                                            Level level = player.getLevel();

                                            double maxDistance = player.getReachDistance() + 1;

                                            Vec3 view = player.getViewVector(0);
                                            Vec3 eyeVec = player.getEyePosition(0);

                                            BlockHitResult ray = level.clip(new ClipContext(eyeVec, eyeVec.add(view.x * maxDistance, view.y * maxDistance,
                                                    view.z * maxDistance), ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, player));

                                            return ray.getType() != HitResult.Type.MISS && ray.getLocation().y() < player.getEyePosition().y();
                                        })
                                        .build()
                                )
                                .stat(StatData.builder("multiplier")
                                        .initialValue(0.1, 0.5)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_TOTAL, 0.15)
                                        .formatValue(value -> (int) (MathUtils.round(value, 2) * 100))
                                        .build())
                                .stat(StatData.builder("duration")
                                        .initialValue(4, 6)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.1)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatData.builder("cooldown")
                                        .initialValue(15, 12)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_TOTAL, -0.1)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .build())
                        .ability(AbilityData.builder("agility")
                                .requiredLevel(10)
                                .requiredPoints(2)
                                .maxLevel(5)
                                .stat(StatData.builder("modifier")
                                        .initialValue(1, 1)
                                        .upgradeModifier(UpgradeOperation.ADD, 1)
                                        .formatValue(value -> (int) ((1 + MathUtils.round(value, 0))) * 100)
                                        .build())
                                .build())
                        .ability(AbilityData.builder("rain")
                                .requiredLevel(15)
                                .maxLevel(10)
                                .active(CastType.INSTANTANEOUS, CastPredicate.builder()
                                        .predicate("arrow", data -> {
                                                    int count = 0;

                                                    for (ItemStack stack : getArrows(data.getStack()))
                                                        count += stack.getCount();

                                                    return count > 0;
                                                }
                                        )
                                        .build()
                                )
                                .stat(StatData.builder("radius")
                                        .initialValue(3, 5)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.15)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatData.builder("duration")
                                        .initialValue(10, 15)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.25)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatData.builder("delay")
                                        .initialValue(1, 0.75)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_TOTAL, -0.15)
                                        .formatValue(value -> MathUtils.round(value, 2))
                                        .build())
                                .build())
                        .build())
                .leveling(new LevelingData(100, 20, 100))
                .style(StyleData.builder()
                        .build())
                .loot(LootData.builder()
                        .entry(LootCollections.VILLAGE)
                        .build())
                .build();
    }

    @Override
    public void castActiveAbility(ItemStack stack, Player player, String ability, CastType type, CastStage stage) {
        Level level = player.getCommandSenderWorld();
        RandomSource random = level.getRandom();

        if (ability.equals("rain")) {
            if (getArrows(stack).isEmpty())
                return;

            double maxDistance = 32;

            Vec3 view = player.getViewVector(0);
            Vec3 eyeVec = player.getEyePosition(0);

            BlockHitResult ray = level.clip(new ClipContext(eyeVec, eyeVec.add(view.x * maxDistance, view.y * maxDistance,
                    view.z * maxDistance), ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, player));

            Vec3 target = ray.getLocation();

            int duration = (int) Math.round(getAbilityValue(stack, "rain", "duration") * 20);

            ArrowRainEntity rain = new ArrowRainEntity(EntityRegistry.ARROW_RAIN.get(), level);

            rain.setDelay((int) Math.round(getAbilityValue(stack, "rain", "delay") * 20));
            rain.setRadius((float) getAbilityValue(stack, "rain", "radius"));
            rain.setQuiver(stack.copy());
            rain.setDuration(duration);
            rain.setOwner(player);
            rain.setPos(target);

            level.addFreshEntity(rain);

            setAbilityCooldown(stack, "rain", duration);
        }

        if (ability.equals("leap")) {
            double maxDistance = player.getReachDistance() + 1;

            Vec3 view = player.getViewVector(0);
            Vec3 eyeVec = player.getEyePosition(0);

            BlockHitResult ray = level.clip(new ClipContext(eyeVec, eyeVec.add(view.x * maxDistance, view.y * maxDistance,
                    view.z * maxDistance), ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, player));

            if (ray.getType() != HitResult.Type.MISS && ray.getLocation().y() < player.getEyePosition().y()) {
                Vec3 motion = player.getLookAngle().scale(-1).normalize().scale(2F);

                for (int i = 0; i < 100; i++) {
                    level.addParticle(ParticleTypes.SPIT, player.getX(), player.getY(), player.getZ(),
                            motion.x() + MathUtils.randomFloat(random) * 0.1F,
                            motion.y() + MathUtils.randomFloat(random) * 0.25F,
                            motion.z() + MathUtils.randomFloat(random) * 0.1F);
                }

                if (!level.isClientSide()) {
                    NetworkHandler.sendToClient(new PacketPlayerMotion(motion.x, motion.y, motion.z), (ServerPlayer) player);

                    NBTUtils.setInt(stack, TAG_LEAP, 0);

                    setAbilityCooldown(stack, "leap", (int) Math.round(getAbilityValue(stack, "leap", "cooldown") * 20));

                    level.playSound(null, player.blockPosition(), SoundRegistry.LEAP.get(), SoundSource.MASTER, 1F, 1F + random.nextFloat() * 0.5F);
                }
            }
        }
    }

    @Override
    public void tickActiveAbilitySelection(ItemStack stack, Player player, String ability) {
        Level level = player.getCommandSenderWorld();

        if (ability.equals("rain")) {
            double maxDistance = 32;

            Vec3 view = player.getViewVector(0);
            Vec3 eyeVec = player.getEyePosition(0);

            BlockHitResult ray = level.clip(new ClipContext(eyeVec, eyeVec.add(view.x * maxDistance, view.y * maxDistance,
                    view.z * maxDistance), ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, player));

            Vec3 target = ray.getLocation();

            ParticleUtils.createCyl(ParticleUtils.constructSimpleSpark(new Color(255, 255, 255), 0.2F, 0, 1F),
                    target, level, getAbilityValue(stack, "rain", "radius"), 0.2F);
        }
    }

    @Override
    public void curioTick(String identifier, int index, LivingEntity livingEntity, ItemStack stack) {
        if (!(livingEntity instanceof Player player))
            return;

        Level level = player.getLevel();

        if (canUseAbility(stack, "leap")) {
            int leap = NBTUtils.getInt(stack, TAG_LEAP, -1);

            if (leap >= 0) {
                if (!level.isClientSide()) {
                    NBTUtils.setInt(stack, TAG_LEAP, ++leap);

                    player.addEffect(new MobEffectInstance(EffectRegistry.VANISHING.get(), 5, 0, false, false));

                    player.fallDistance = 0F;

                    if (leap >= 5 && (player.isOnGround() || player.getAbilities().flying
                            || player.isFallFlying() || player.isInWater() || player.isInLava()
                            || leap >= getAbilityValue(stack, "leap", "duration") * 20)) {
                        NBTUtils.clearTag(stack, TAG_LEAP);
                    }
                }

                if (player.isUsingItem() && player.getMainHandItem().getItem() instanceof BowItem) {
                    player.setDeltaMovement(player.getDeltaMovement().multiply(0.975F, player.getDeltaMovement().y() > 0 ? 0.9F : 0F, 0.975F));
                }
            }
        }

        if (canUseAbility(stack, "agility")) {
            if (player.isUsingItem() && player.getMainHandItem().getItem() instanceof BowItem) {
                for (int i = 0; i < 1; i++)
                    player.updatingUsingItem();
            }
        }
    }

    public int getSlotsAmount(ItemStack stack) {
        return (int) Math.round(getAbilityValue(stack, "receptacle", "slots"));
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return Optional.of(new ArrowQuiverTooltip(getArrows(stack), getSlotsAmount(stack)));
    }

    public static List<ItemStack> getArrows(ItemStack stack) {
        CompoundTag tag = stack.getTag();

        return tag == null ? new ArrayList<>() : tag.getList(TAG_ARROWS, 10).stream()
                .map(CompoundTag.class::cast)
                .map(ItemStack::of)
                .collect(Collectors.toList());
    }

    public int insertStack(ItemStack stack, ItemStack arrow) {
        if (!arrow.getItem().canFitInsideContainerItems())
            return 0;

        CompoundTag tag = stack.getOrCreateTag();

        if (!tag.contains(TAG_ARROWS))
            tag.put(TAG_ARROWS, new ListTag());

        ListTag list = tag.getList(TAG_ARROWS, 10);

        List<CompoundTag> entries = list.stream()
                .filter(CompoundTag.class::isInstance)
                .map(CompoundTag.class::cast)
                .filter(nbt -> ItemStack.isSameItemSameTags(ItemStack.of(nbt), arrow))
                .filter(nbt -> {
                    ItemStack item = ItemStack.of(nbt);

                    return item.getCount() < item.getMaxStackSize();
                })
                .toList();

        int amount = 0;

        if (!entries.isEmpty()) {
            for (CompoundTag entry : entries) {
                ItemStack s = ItemStack.of(entry);

                int count = s.getCount() + arrow.getCount();

                if (count <= s.getMaxStackSize()) {
                    amount += arrow.getCount();

                    arrow.setCount(0);

                    s.grow(amount);
                    s.save(entry);

                    list.remove(entry);
                    list.add(0, entry);

                    break;
                } else {
                    int step = s.getMaxStackSize() - s.getCount();

                    amount += step;

                    arrow.shrink(step);

                    s.grow(step);
                    s.save(entry);

                    list.remove(entry);
                    list.add(0, entry);
                }
            }
        }

        if (!arrow.isEmpty()) {
            if (getSlotsAmount(stack) <= getArrows(stack).size())
                return 0;

            amount += arrow.getCount();

            CompoundTag entry = new CompoundTag();

            arrow.copy().save(entry);

            list.add(0, entry);
        }

        return amount;
    }

    public static Optional<ItemStack> takeStack(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();

        if (!tag.contains(TAG_ARROWS))
            return Optional.empty();

        ListTag list = tag.getList(TAG_ARROWS, 10);

        if (list.isEmpty())
            return Optional.empty();

        ItemStack s = ItemStack.of(list.getCompound(0));

        list.remove(0);

        if (list.isEmpty())
            stack.removeTagKey(TAG_ARROWS);

        return Optional.of(s);
    }

    public static void takeArrow(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();

        if (!tag.contains(TAG_ARROWS))
            return;

        ListTag list = tag.getList(TAG_ARROWS, 10);

        if (list.isEmpty())
            return;

        CompoundTag entry = list.getCompound(0);

        ItemStack s = ItemStack.of(entry);

        s.shrink(1);
        s.save(entry);

        list.remove(0);

        if (!s.isEmpty())
            list.add(0, entry);

        if (list.isEmpty())
            stack.removeTagKey(TAG_ARROWS);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack matrixStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource renderTypeBuffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        CurioModel model = getModel(stack);

        matrixStack.pushPose();

        LivingEntity entity = slotContext.entity();

        model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
        model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        ICurioRenderer.followBodyRotations(entity, model);

        VertexConsumer vertexconsumer = ItemRenderer.getArmorFoilBuffer(renderTypeBuffer, RenderType.armorCutoutNoCull(getTexture(stack)), false, stack.hasFoil());

        model.renderToBuffer(matrixStack, vertexconsumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        matrixStack.popPose();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public LayerDefinition constructLayerDefinition() {
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(0.4F), 0.0F);

        PartDefinition body = mesh.getRoot().addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r1 = body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(16, 16).addBox(-1.5F, -3.0F, -1.75F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(16, 21).addBox(-1.0F, -2.0F, -1.25F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.25F))
                .texOffs(0, 16).addBox(-1.5F, 2.0F, -1.75F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, 2.5F, 3.5F, 0.0F, 0.0F, -0.7854F));

        PartDefinition cube_r2 = body.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(27, 10).addBox(-1.2F, -0.25F, 0.175F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.4534F, -1.2997F, 3.75F, 2.9686F, -0.8855F, 2.0959F));
        PartDefinition cube_r3 = body.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(27, 10).addBox(-0.825F, -0.25F, 0.175F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.4534F, -1.2997F, 3.75F, 3.0018F, 0.6732F, 1.874F));
        PartDefinition cube_r4 = body.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(29, 12).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.4534F, -0.4497F, 3.75F, 3.0018F, 0.6732F, 1.874F));
        PartDefinition cube_r5 = body.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(29, 12).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.4534F, -0.4497F, 3.75F, 2.9686F, -0.8855F, 2.0959F));
        PartDefinition cube_r6 = body.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(27, 5).addBox(-1.0F, -0.5F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.7784F, -2.1997F, 5.375F, -0.5251F, -0.8705F, -0.3867F));
        PartDefinition cube_r7 = body.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(27, 5).addBox(-1.0F, -0.5F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.7784F, -2.1997F, 5.375F, -2.7418F, -0.5916F, 2.1065F));
        PartDefinition cube_r8 = body.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(29, 7).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.4534F, -0.9497F, 4.75F, -2.7418F, -0.5916F, 2.1065F));
        PartDefinition cube_r9 = body.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(29, 7).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.4534F, -0.9497F, 4.75F, -0.5251F, -0.8705F, -0.3867F));
        PartDefinition cube_r10 = body.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(27, 0).addBox(-1.0F, -0.5F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.3784F, -4.1997F, 2.4F, 1.0472F, -1.3526F, -1.3963F));
        PartDefinition cube_r11 = body.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(27, 0).addBox(-1.0F, -0.5F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.3784F, -4.1997F, 2.4F, 0.1897F, 0.1084F, -0.3387F));
        PartDefinition cube_r12 = body.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(29, 2).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.7034F, -2.4497F, 2.75F, 1.0472F, -1.3526F, -1.3963F));
        PartDefinition cube_r13 = body.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(29, 2).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.7034F, -2.4497F, 2.75F, 0.1897F, 0.1084F, -0.3387F));

        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public List<String> headParts() {
        return Lists.newArrayList("body");
    }

    public record ArrowQuiverTooltip(@Getter List<ItemStack> items, @Getter int maxAmount) implements TooltipComponent {

    }

    @OnlyIn(Dist.CLIENT)
    public record ClientArrowQuiverTooltip(@Getter ArrowQuiverTooltip tooltip) implements ClientTooltipComponent {
        public static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MODID, "textures/gui/tooltip/arrow_quiver/empty_arrow.png");

        @Override
        public int getHeight() {
            return 26;
        }

        @Override
        public int getWidth(Font font) {
            return tooltip.getMaxAmount() * 11;
        }

        @Override
        public void renderImage(Font font, int mouseX, int mouseY, PoseStack poseStack, ItemRenderer itemRenderer, int blitOffset) {
            poseStack.pushPose();

            poseStack.translate(0, 0, 410);
            poseStack.scale(0.5F, 0.5F, 0.5F);

            int step = 0;

            for (ItemStack stack : tooltip.getItems()) {
                font.draw(poseStack, String.valueOf(stack.getCount()), ((mouseX + step) * 2) + ((16 - font.width(String.valueOf(stack.getCount()))) / 2F), (mouseY + 16) * 2, 0xFFFFFF);

                itemRenderer.renderAndDecorateFakeItem(stack, mouseX + step, mouseY);

                step += 10;
            }

            poseStack.scale(2F, 2F, 2F);

            for (int i = step / 10; i < tooltip.getMaxAmount(); i++) {
                RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
                RenderSystem.setShaderTexture(0, TEXTURE);

                Minecraft.getInstance().getTextureManager().getTexture(TEXTURE).bind();

                Gui.blit(poseStack, mouseX + step, mouseY, 16, 16, 0, 0, 16, 16, 16, 16);

                step += 10;
            }

            poseStack.translate(0, 0, -410);

            poseStack.popPose();
        }
    }

    @Mod.EventBusSubscriber
    public static class Events {
        @SubscribeEvent
        public static void onSlotClick(ContainerSlotClickEvent event) {
            Player player = event.getEntity();

            ItemStack heldStack = event.getHeldStack();
            ItemStack slotStack = event.getSlotStack();

            if (slotStack.getItem() != ItemRegistry.ARROW_QUIVER.get()
                    || !((IRelicItem) slotStack.getItem()).canUseAbility(slotStack, "receptacle"))
                return;

            if (event.getAction() == ClickAction.PRIMARY) {
                if (!(heldStack.getItem() instanceof ArrowItem))
                    return;

                int amount = ((ArrowQuiverItem) slotStack.getItem()).insertStack(slotStack, heldStack);

                if (amount <= 0)
                    return;

                heldStack.shrink(amount);

                player.playSound(SoundEvents.BUNDLE_INSERT, 1F, 1F);

                event.setCanceled(true);
            } else {
                if (!heldStack.isEmpty())
                    return;

                takeStack(slotStack).ifPresent((stack) -> {
                    event.getContainer().setCarried(stack);

                    player.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 1F, 1F);

                    event.setCanceled(true);
                });
            }
        }

        @SubscribeEvent
        public static void onArrowLoose(ArrowLooseEvent event) {
            Player player = event.getEntity();
            ItemStack stack = EntityUtils.findEquippedCurio(player, ItemRegistry.ARROW_QUIVER.get());

            if (stack.isEmpty() || !((IRelicItem) stack.getItem()).canUseAbility(stack, "receptacle")
                    || getArrows(stack).isEmpty() || player.isCreative())
                return;

            takeArrow(stack);
        }

        @SubscribeEvent
        public static void onGettingProjectile(LivingGetProjectileEvent event) {
            if (!(event.getEntity() instanceof Player player))
                return;

            ItemStack weapon = event.getProjectileWeaponItemStack();

            if (!(weapon.getItem() instanceof BowItem) || (weapon.getItem() instanceof CrossbowItem))
                return;

            ItemStack stack = EntityUtils.findEquippedCurio(player, ItemRegistry.ARROW_QUIVER.get());

            if (stack.isEmpty() || !((IRelicItem) stack.getItem()).canUseAbility(stack, "receptacle"))
                return;

            List<ItemStack> arrows = getArrows(stack);

            if (!arrows.isEmpty())
                event.setProjectileItemStack(arrows.get(0));
        }

        @SubscribeEvent
        public static void onLivingHurt(LivingHurtEvent event) {
            if (!(event.getSource().getDirectEntity() instanceof AbstractArrow arrow)
                    || !(arrow.getOwner() instanceof Player player))
                return;

            ItemStack stack = EntityUtils.findEquippedCurio(player, ItemRegistry.ARROW_QUIVER.get());

            if (stack.isEmpty() || !(stack.getItem() instanceof IRelicItem relic))
                return;

            if (relic.canUseAbility(stack, "receptacle")) {
                int amount = (int) Math.min(10, Math.round(player.position().distanceTo(new Vec3(arrow.getX(), player.getY(), arrow.getZ())) * 0.1));

                if (amount > 0)
                    relic.addExperience(player, stack, 1);
            }

            if (relic.canUseAbility(stack, "leap")) {
                if (arrow.getPersistentData().contains("arrow_quiver_multiplier"))
                    event.setAmount((float) (event.getAmount() + (event.getAmount() * relic.getAbilityValue(stack, "leap", "multiplier"))));
            }
        }

        @SubscribeEvent
        public static void onEntitySpawned(EntityJoinLevelEvent event) {
            if (event.getLevel().isClientSide()
                    || !(event.getEntity() instanceof AbstractArrow arrow)
                    || !(arrow.getOwner() instanceof Player player)
                    || arrow.position().distanceTo(player.position()) > 16
                    || arrow.life > 0)
                return;

            ItemStack stack = EntityUtils.findEquippedCurio(player, ItemRegistry.ARROW_QUIVER.get());

            if (stack.isEmpty() || !((IRelicItem) stack.getItem()).canUseAbility(stack, "leap")
                    || NBTUtils.getInt(stack, TAG_LEAP, -1) < 0)
                return;

            NBTUtils.clearTag(stack, TAG_LEAP);

            if (!arrow.isCritArrow())
                return;

            Level level = player.getCommandSenderWorld();

            level.playSound(null, player.blockPosition(), SoundRegistry.POWERED_ARROW.get(), SoundSource.MASTER, 1F, 1F + player.getRandom().nextFloat() * 0.5F);

            arrow.getPersistentData().putBoolean("arrow_quiver_multiplier", true);
            arrow.setNoGravity(true);

            Vec3 motion = player.getLookAngle().scale(-1).normalize();

            if (!level.isClientSide())
                NetworkHandler.sendToClient(new PacketPlayerMotion(motion.x, motion.y, motion.z), (ServerPlayer) player);
        }

        @SubscribeEvent
        public static void onProjectileImpact(ProjectileImpactEvent event) {
            if (!(event.getRayTraceResult() instanceof EntityHitResult result)
                    || !(result.getEntity() instanceof LivingEntity entity)
                    || !(event.getEntity() instanceof AbstractArrow arrow)
                    || !(arrow.getOwner() instanceof Player player))
                return;

            ItemStack stack = EntityUtils.findEquippedCurio(player, ItemRegistry.ARROW_QUIVER.get());

            if (stack.isEmpty() || !((IRelicItem) stack.getItem()).canUseAbility(stack, "leap"))
                return;

            CompoundTag data = arrow.getPersistentData();

            if (data.contains("arrow_quiver_multiplier")) {
                if (EntityUtils.isAlliedTo(player, entity))
                    event.setCanceled(true);
                else
                    data.remove("arrow_quiver_multiplier");
            }
        }
    }
}