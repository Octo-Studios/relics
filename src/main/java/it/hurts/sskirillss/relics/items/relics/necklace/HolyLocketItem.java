package it.hurts.sskirillss.relics.items.relics.necklace;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.hurts.sskirillss.relics.client.models.items.CurioModel;
import it.hurts.sskirillss.relics.entities.DeathEssenceEntity;
import it.hurts.sskirillss.relics.entities.LifeEssenceEntity;
import it.hurts.sskirillss.relics.init.EntityRegistry;
import it.hurts.sskirillss.relics.init.ItemRegistry;
import it.hurts.sskirillss.relics.items.relics.base.IRenderableCurio;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.CastData;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastStage;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastType;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilitiesData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilityData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.StatData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.UpgradeOperation;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootData;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootCollections;
import it.hurts.sskirillss.relics.items.relics.base.data.style.StyleData;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.sync.SyncTargetPacket;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.NBTUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

import java.awt.*;
import java.util.Comparator;
import java.util.List;

public class HolyLocketItem extends RelicItem implements IRenderableCurio {
    public static final String TAG_TOGGLED = "toggled";
    public static final String TAG_CHARGE = "charge";

    @Override
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("belief")
                                .active(CastData.builder()
                                        .type(CastType.INSTANTANEOUS)
                                        .build())
                                .icon((player, stack, ability) -> ability + (NBTUtils.getBoolean(stack, TAG_TOGGLED, true) ? "_holy" : "_wicked"))
                                .stat(StatData.builder("radius")
                                        .initialValue(3D, 6D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.2D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatData.builder("amount")
                                        .initialValue(0.1D, 0.25D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.1D)
                                        .formatValue(value -> (int) (MathUtils.round(value, 3) * 100))
                                        .build())
                                .stat(StatData.builder("count")
                                        .initialValue(1D, 3D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.1D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(StatData.builder("capacity")
                                        .initialValue(8D, 12D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.2D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .build())
                        .ability(AbilityData.builder("repentance")
                                .requiredLevel(5)
                                .stat(StatData.builder("radius")
                                        .initialValue(2D, 6D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.15D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatData.builder("damage")
                                        .initialValue(0.1D, 0.2D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.1D)
                                        .formatValue(value -> MathUtils.round(value, 2))
                                        .build())
                                .build())
                        .ability(AbilityData.builder("blessing")
                                .maxLevel(5)
                                .requiredPoints(2)
                                .requiredLevel(10)
                                .active(CastData.builder()
                                        .type(CastType.TOGGLEABLE)
                                        .castPredicate("blessing", (player, stack) -> getCharges(stack) > 0)
                                        .build())
                                .stat(StatData.builder("consumption")
                                        .initialValue(8D, 6D)
                                        .upgradeModifier(UpgradeOperation.ADD, -1D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .build())
                        .build())
                .leveling(new LevelingData(100, 15, 100))
                .style(StyleData.builder()
                        .build())
                .loot(LootData.builder()
                        .entry(LootCollections.DESERT)
                        .build())
                .build();
    }

    @Override
    public void castActiveAbility(ItemStack stack, Player player, String ability, CastType type, CastStage stage) {
        if (ability.equals("belief"))
            NBTUtils.setBoolean(stack, TAG_TOGGLED, !NBTUtils.getBoolean(stack, TAG_TOGGLED, true));
    }

    public List<Monster> gatherMonsters(Player player, ItemStack stack) {
        return player.getCommandSenderWorld().getEntitiesOfClass(Monster.class, player.getBoundingBox().inflate(getAbilityValue(stack, "repentance", "radius"))).stream().filter(LivingEntity::isInvertedHealAndHarm).toList();
    }

    public int getMaxCharges(ItemStack stack) {
        return (int) getAbilityValue(stack, "belief", "capacity");
    }

    public void setCharges(ItemStack stack, int amount) {
        NBTUtils.setInt(stack, TAG_CHARGE, Mth.clamp(amount, 0, getMaxCharges(stack)));
    }

    public void addCharge(ItemStack stack, int amount) {
        setCharges(stack, getCharges(stack) + amount);
    }

    public int getCharges(ItemStack stack) {
        return NBTUtils.getInt(stack, TAG_CHARGE, 0);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player) || player.getCommandSenderWorld().isClientSide() || player.tickCount % 20 != 0)
            return;

        Level level = player.getCommandSenderWorld();

        int charges = getCharges(stack);

        if (isAbilityTicking(stack, "blessing")) {
            setCharges(stack, (int) (charges - getAbilityValue(stack, "blessing", "consumption")));

            if (charges <= 0)
                setAbilityTicking(stack, "blessing", false);
        }

        List<Monster> monsters = gatherMonsters(player, stack);

        if (monsters.isEmpty() || charges <= 0)
            return;

        for (Monster entity : monsters) {
            if (!EntityUtils.hurt(entity, player.level().damageSources().playerAttack(player), (float) (charges * getAbilityValue(stack, "repentance", "damage"))))
                continue;

            entity.setRemainingFireTicks(50);

            spreadExperience(player, stack, 1);

            RandomSource random = level.getRandom();

            ((ServerLevel) level).sendParticles(ParticleUtils.constructSimpleSpark(new Color(200, 150 + random.nextInt(50), random.nextInt(50)), 0.4F, 20, 0.95F),
                    entity.getX(), entity.getY() + entity.getBbHeight() / 2F, entity.getZ(), 10, entity.getBbWidth() / 2F, entity.getBbHeight() / 2F, entity.getBbWidth() / 2F, 0.025F);
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

        VertexConsumer vertexconsumer = ItemRenderer.getArmorFoilBuffer(renderTypeBuffer, RenderType.armorCutoutNoCull(getTexture(stack)), false, stack.hasFoil());

        matrixStack.scale(0.5F, 0.5F, 0.5F);

        model.renderToBuffer(matrixStack, vertexconsumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        matrixStack.scale(2F, 2F, 2F);

        matrixStack.popPose();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public LayerDefinition constructLayerDefinition() {
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(0.4F), 0.0F);
        PartDefinition bone = mesh.getRoot().addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 3).addBox(-8.0F, -1.15F, -4.15F, 16.0F, 7.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 1.15F, 0.0F));

        bone.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 18).addBox(-2.6096F, -0.8646F, -0.2F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0571F, 6.6447F, -4.9F, 0.0F, 0.0F, 0.2568F));
        bone.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(12, 18).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.15F)), PartPose.offsetAndRotation(0.0877F, 6.2393F, -5.2F, 0.0F, 0.0F, 0.7854F));
        bone.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0322F, -2.5947F, -0.225F, 2.0F, 6.0F, 1.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(0.0571F, 6.6447F, -4.9F, 0.0F, 0.0F, -0.004F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public List<String> headParts() {
        return Lists.newArrayList("body");
    }

    @Mod.EventBusSubscriber
    public static class Events {
        @SubscribeEvent
        public static void onLivingHurt(LivingHurtEvent event) {
            if (event.getEntity() instanceof Player player) {
                ItemStack stack = EntityUtils.findEquippedCurio(player, ItemRegistry.HOLY_LOCKET.get());

                if (stack.getItem() instanceof HolyLocketItem relic) {
                    if (relic.isAbilityTicking(stack, "blessing")) {
                        event.setCanceled(true);

                        return;
                    }

                    if (NBTUtils.getBoolean(stack, TAG_TOGGLED, false)) {
                        int charges = relic.getCharges(stack);

                        if (charges > 0)
                            event.setAmount(event.getAmount() * (1F - (charges * 0.01F)));
                    }
                }
            } else if (event.getSource().getEntity() instanceof Player player) {
                ItemStack stack = EntityUtils.findEquippedCurio(player, ItemRegistry.HOLY_LOCKET.get());

                if (stack.getItem() instanceof HolyLocketItem relic) {
                    if (!NBTUtils.getBoolean(stack, TAG_TOGGLED, false)) {
                        int charges = relic.getCharges(stack);

                        if (charges > 0)
                            event.setAmount(event.getAmount() + (event.getAmount() * charges * 0.01F));
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onLivingHeal(LivingHealEvent event) {
            if (event.getAmount() <= 0.5F)
                return;

            if (event.getEntity() instanceof Player player) {
                ItemStack stack = EntityUtils.findEquippedCurio(player, ItemRegistry.HOLY_LOCKET.get());
                Level level = player.getCommandSenderWorld();

                if (!(stack.getItem() instanceof HolyLocketItem relic) || NBTUtils.getBoolean(stack, TAG_TOGGLED, true))
                    return;

                for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(relic.getAbilityValue(stack, "belief", "radius"))).stream()
                        .filter(player::hasLineOfSight).sorted(Comparator.comparing(entity -> entity.position().distanceTo(player.position()))).limit((int) relic.getAbilityValue(stack, "belief", "count")).toList()) {
                    if (target.getStringUUID().equals(player.getStringUUID()))
                        continue;

                    int amount = (int) Math.max((event.getAmount() * relic.getAbilityValue(stack, "belief", "amount")), 1);

                    DeathEssenceEntity essence = new DeathEssenceEntity(EntityRegistry.DEATH_ESSENCE.get(), level);

                    essence.setPos(player.position().add(0, player.getBbHeight() / 2, 0));
                    essence.setDirectionChoice(MathUtils.randomFloat(player.getRandom()));
                    essence.setTarget(target);
                    essence.setDamage(amount);

                    level.addFreshEntity(essence);

                    if (!level.isClientSide())
                        NetworkHandler.sendToClients(PacketDistributor.TRACKING_ENTITY.with(() -> essence), new SyncTargetPacket(essence.getId(), target.getId()));

                    relic.spreadExperience(player, stack, amount);
                    relic.addCharge(stack, 1);
                }
            } else {
                LivingEntity entity = event.getEntity();
                Level level = entity.getCommandSenderWorld();

                for (ServerPlayer playerSearched : level.getEntitiesOfClass(ServerPlayer.class, event.getEntity().getBoundingBox().inflate(32))) {
                    ItemStack stack = EntityUtils.findEquippedCurio(playerSearched, ItemRegistry.HOLY_LOCKET.get());

                    if (!(stack.getItem() instanceof HolyLocketItem relic) || relic.getAbilityValue(stack, "belief", "radius") < playerSearched.position().distanceTo(event.getEntity().position())
                            || !NBTUtils.getBoolean(stack, TAG_TOGGLED, true))
                        continue;

                    int amount = (int) Math.max((event.getAmount() * relic.getAbilityValue(stack, "belief", "amount")), 0.5);

                    LifeEssenceEntity essence = new LifeEssenceEntity(EntityRegistry.LIFE_ESSENCE.get(), level);

                    essence.setPos(entity.position().add(0, entity.getBbHeight() / 2, 0));
                    essence.setDirectionChoice(MathUtils.randomFloat(playerSearched.getRandom()));
                    essence.setTarget(playerSearched);
                    essence.setHeal(amount);

                    playerSearched.level().addFreshEntity(essence);

                    if (!level.isClientSide())
                        NetworkHandler.sendToClients(PacketDistributor.TRACKING_ENTITY.with(() -> essence), new SyncTargetPacket(essence.getId(), playerSearched.getId()));

                    relic.spreadExperience(playerSearched, stack, amount);
                    relic.addCharge(stack, 1);
                }
            }
        }
    }
}