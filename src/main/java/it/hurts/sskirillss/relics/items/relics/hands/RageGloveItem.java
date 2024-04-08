package it.hurts.sskirillss.relics.items.relics.hands;

import it.hurts.sskirillss.relics.items.relics.base.data.style.StyleData;
import it.hurts.sskirillss.relics.init.EffectRegistry;
import it.hurts.sskirillss.relics.init.ItemRegistry;
import it.hurts.sskirillss.relics.init.SoundRegistry;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
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
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;

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
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.SlotContext;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RageGloveItem extends RelicItem {
    public static final String TAG_STACKS = "stacks";
    public static final String TAG_TIME = "time";

    @Override
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("rage")
                                .maxLevel(10)
                                .stat(StatData.builder("incoming_damage")
                                        .initialValue(0.05D, 0.025D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.05D)
                                        .formatValue(value -> MathUtils.round(MathUtils.round(value, 3) * 100, 3))
                                        .build())
                                .stat(StatData.builder("dealt_damage")
                                        .initialValue(0.025D, 0.075D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.1D)
                                        .formatValue(value -> MathUtils.round(MathUtils.round(value, 3) * 100, 3))
                                        .build())
                                .stat(StatData.builder("duration")
                                        .initialValue(2D, 4D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.1D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .build())
                        .ability(AbilityData.builder("phlebotomy")
                                .requiredLevel(5)
                                .maxLevel(10)
                                .stat(StatData.builder("heal")
                                        .initialValue(0.0001D, 0.00025D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.1D)
                                        .formatValue(value -> MathUtils.round(MathUtils.round(value, 5) * 20, 5))
                                        .build())
                                .stat(StatData.builder("movement_speed")
                                        .initialValue(0.01D, 0.025D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.1D)
                                        .formatValue(value -> MathUtils.round(MathUtils.round(value, 3) * 100, 3))
                                        .build())
                                .stat(StatData.builder("attack_speed")
                                        .initialValue(0.005D, 0.01D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.05D)
                                        .formatValue(value -> MathUtils.round(MathUtils.round(value, 3) * 100, 3))
                                        .build())
                                .build())
                        .ability(AbilityData.builder("spurt")
                                .requiredLevel(10)
                                .maxLevel(10)
                                .active(CastType.INSTANTANEOUS)
                                .stat(StatData.builder("damage")
                                        .initialValue(0.1D, 0.25D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.1D)
                                        .formatValue(value -> MathUtils.round(value, 2))
                                        .build())
                                .stat(StatData.builder("distance")
                                        .initialValue(3D, 8D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.3D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatData.builder("cooldown")
                                        .initialValue(20, 15)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_TOTAL, -0.075)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .build())
                        .build())
                .leveling(new LevelingData(100, 20, 100))
                .style(StyleData.builder()
                        .build())
                .loot(LootData.builder()
                        .entry(LootCollections.NETHER)
                        .build())
                .build();
    }

    @Override
    public void castActiveAbility(ItemStack stack, Player player, String ability, CastType type, CastStage stage) {
        Level level = player.getCommandSenderWorld();
        Random random = level.getRandom();

        if (ability.equals("spurt")) {
            int stacks = NBTUtils.getInt(stack, TAG_STACKS, 0);

            double maxDistance = getAbilityValue(stack, "spurt", "distance");

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

                setAbilityCooldown(stack, "spurt", (int) Math.round(getAbilityValue(stack, "spurt", "cooldown") * 20));
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

                for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, new AABB(new BlockPos(start.x + dir.x * progress,
                        start.y + dir.y * progress, start.z + dir.z * progress)).inflate(0.5, 1, 0.5))) {
                    if (entity.getStringUUID().equals(player.getStringUUID())
                            || entity.isDeadOrDying())
                        continue;

                    targets.add(entity);
                }
            }

            if (!targets.isEmpty()) {
                EntityUtils.resetAttribute(player, stack, Attributes.ATTACK_SPEED, Integer.MAX_VALUE, AttributeModifier.Operation.MULTIPLY_BASE);
                EntityUtils.resetAttribute(player, stack, Attributes.ATTACK_DAMAGE, (float) (getAbilityValue(stack, "spurt", "damage") * stacks), AttributeModifier.Operation.ADDITION);

                for (LivingEntity entity : targets) {
                    if (entity.invulnerableTime > 0 || EntityUtils.isAlliedTo(player, entity))
                        continue;

                    player.attack(entity);

                    dropAllocableExperience(level, entity.getEyePosition(), stack, 1);

                    entity.addEffect(new MobEffectInstance(EffectRegistry.BLEEDING.get(), 100, 0));
                    entity.setSecondsOnFire(5);
                }

                EntityUtils.removeAttribute(player, stack, Attributes.ATTACK_DAMAGE, AttributeModifier.Operation.ADDITION);
                EntityUtils.removeAttribute(player, stack, Attributes.ATTACK_SPEED, AttributeModifier.Operation.MULTIPLY_BASE);
            }

            NBTUtils.clearTag(stack, TAG_STACKS);
        }
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player))
            return;

        if (canUseAbility(stack, "phlebotomy")) {
            float percentage = 100F - (player.getHealth() / player.getMaxHealth() * 100F);

            player.heal((float) getAbilityValue(stack, "phlebotomy", "heal") * percentage);

            EntityUtils.resetAttribute(player, stack, Attributes.ATTACK_SPEED, (float) (getAbilityValue(stack, "phlebotomy", "attack_speed") * percentage), AttributeModifier.Operation.MULTIPLY_TOTAL);
            EntityUtils.resetAttribute(player, stack, Attributes.MOVEMENT_SPEED, (float) (getAbilityValue(stack, "phlebotomy", "movement_speed") * percentage), AttributeModifier.Operation.MULTIPLY_TOTAL);
        }

        if (canUseAbility(stack, "rage")) {
            int stacks = NBTUtils.getInt(stack, TAG_STACKS, 0);

            if (stacks > 0) {
                int time = NBTUtils.getInt(stack, TAG_TIME, 0);

                if (time > 0)
                    NBTUtils.setInt(stack, TAG_TIME, --time);
                else {
                    NBTUtils.setInt(stack, TAG_STACKS, 0);
                }
            }
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player)
                || stack.getItem() == newStack.getItem())
            return;

        EntityUtils.removeAttribute(player, stack, Attributes.ATTACK_SPEED, AttributeModifier.Operation.MULTIPLY_TOTAL);
        EntityUtils.removeAttribute(player, stack, Attributes.MOVEMENT_SPEED, AttributeModifier.Operation.MULTIPLY_TOTAL);

        NBTUtils.clearTag(stack, TAG_STACKS);
        NBTUtils.clearTag(stack, TAG_TIME);
    }

    @Mod.EventBusSubscriber(modid = Reference.MODID)
    public static class Events {
        @SubscribeEvent
        public static void onLivingHurt(LivingHurtEvent event) {
            Entity source = event.getSource().getDirectEntity();

            if (source instanceof Player player) {
                if (!(event.getSource().getEntity() instanceof Player))
                    return;

                ItemStack stack = EntityUtils.findEquippedCurio(player, ItemRegistry.RAGE_GLOVE.get());

                if (!(stack.getItem() instanceof IRelicItem relic))
                    return;

                if (relic.canUseAbility(stack, "rage")) {
                    int stacks = NBTUtils.getInt(stack, TAG_STACKS, 0);

                    NBTUtils.setInt(stack, TAG_STACKS, ++stacks);
                    NBTUtils.setInt(stack, TAG_TIME, (int) Math.round(relic.getAbilityValue(stack, "rage", "duration") * 20));

                    relic.dropAllocableExperience(player.level, event.getEntity().getEyePosition(), stack, 1);

                    event.setAmount((float) (event.getAmount() + (event.getAmount() * (stacks * relic.getAbilityValue(stack, "rage", "dealt_damage")))));
                }
            } else if (event.getEntity() instanceof Player player){
                ItemStack stack = EntityUtils.findEquippedCurio(player, ItemRegistry.RAGE_GLOVE.get());

                if (!(stack.getItem() instanceof IRelicItem relic))
                    return;

                if (relic.canUseAbility(stack, "rage")) {
                    int stacks = NBTUtils.getInt(stack, TAG_STACKS, 0);

                    if (stacks <= 0)
                        return;

                    event.setAmount((float) (event.getAmount() + (event.getAmount() * (stacks * relic.getAbilityValue(stack, "rage", "incoming_damage")))));
                }
            }
        }
    }
}