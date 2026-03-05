package it.hurts.sskirillss.relics.items.relics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.AbilityMetricTemplate;
import it.hurts.sskirillss.relics.api.relics.AbilityStatisticTemplate;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.VisibilityState;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourceTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourcesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.AbilityStatTemplate;
import it.hurts.sskirillss.relics.init.RelicsDataComponents;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ClotOfTimeItem extends RelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("rewind")
                                .rankModifier(1, "invulnerability")
                                .rankModifier(3, "health_rewind")
                                .rankModifier(5, "oblivion")
                                .stat(AbilityStatTemplate.builder("time")
                                        .initialValue(10D, 10D)
                                        .formatValue(value -> MathUtils.round(value, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("cooldown")
                                        .initialValue(5D, 5D)
                                        .formatValue(value -> MathUtils.round(value, 0))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source("activation")
                                        .source("rewind")
                                        .source(ExperienceSourceTemplate.builder("health_rewind")
                                                .rankModifierVisibilityState("health_rewind", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("activations")
                                                .formatValue(value -> String.valueOf(value.intValue()))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("rewind_duration")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 1)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("health_restored")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 1)))
                                                .rankModifierVisibilityState("health_rewind", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .step(100)
                        .build())
                .loot(LootTemplate.builder()
                        .build())
                .build();
    }

    private int getRememberedTicks(Player player, ItemStack stack) {
        var ability = this.getRelicData(player, stack).getAbilitiesData().getAbilityData("rewind");

        if (!ability.canPlayerUse(player))
            return 0;

        return Math.max(1, (int) Math.round(ability.getStatData("time").getValue() * 20D));
    }

    private int getCooldown(ItemStack stack) {
        return Math.max(0, stack.getOrDefault(RelicsDataComponents.CLOT_OF_TIME_COOLDOWN, 0));
    }

    private void setCooldown(ItemStack stack, int cooldown) {
        stack.set(RelicsDataComponents.CLOT_OF_TIME_COOLDOWN, Math.max(0, cooldown));
    }

    private int getCooldownFromStat(Player player, ItemStack stack) {
        var ability = this.getRelicData(player, stack).getAbilitiesData().getAbilityData("rewind");

        return Math.max(0, (int) Math.round(ability.getStatData("cooldown").getValue() * 20D));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);

        if (this.getRememberedTicks(player, stack) <= 0 || stack.getOrDefault(RelicsDataComponents.CLOT_OF_TIME_PATH, List.<PathPointData>of()).size() < 2 || this.getCooldown(stack) > 0)
            return InteractionResultHolder.pass(stack);

        if (!level.isClientSide()) {
            stack.set(RelicsDataComponents.CLOT_OF_TIME_REWIND_CURSOR, -1D);

            var ability = this.getRelicData(player, stack).getAbilitiesData().getAbilityData("rewind");

            if (ability.canPlayerUse(player)) {
                this.getRelicData(player, stack).getLevelingData().addExperience("rewind", "activation", 1D);

                ability.getStatisticData().getMetricData("activations").addValue(1D);
            }
        }

        player.startUsingItem(hand);

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player))
            return;

        if (player.isPassenger())
            player.stopRiding();

        if (level.isClientSide())
            return;

        var path = new ArrayList<>(stack.getOrDefault(RelicsDataComponents.CLOT_OF_TIME_PATH, List.<PathPointData>of()));

        if (path.size() < 2) {
            stack.set(RelicsDataComponents.CLOT_OF_TIME_REWIND_CURSOR, -1D);

            if (this.getCooldown(stack) <= 0)
                this.setCooldown(stack, this.getCooldownFromStat(player, stack));

            player.stopUsingItem();

            return;
        }

        var lastIndex = path.size() - 1;
        var cursor = (double) stack.getOrDefault(RelicsDataComponents.CLOT_OF_TIME_REWIND_CURSOR, -1D);

        if (cursor < 0D || cursor > lastIndex)
            cursor = lastIndex;

        var speed = 1.05D + Math.min(3D, Math.max(0, player.getTicksUsingItem() - 1) * 0.01D);

        cursor = Math.max(0D, cursor - speed);

        var ability = this.getRelicData(player, stack).getAbilitiesData().getAbilityData("rewind");

        if (ability.canPlayerUse(player)) {
            this.getRelicData(player, stack).getLevelingData().addExperience("rewind", "rewind", 1D / 20D);

            ability.getStatisticData().getMetricData("rewind_duration").addValue(1D / 20D);
        }

        if (ability.canPlayerUse(player) && ability.isRankModifierUnlocked("health_rewind")) {
            var segment = Mth.floor(cursor);

            var t = (float) (cursor - segment);

            var p1 = path.get(segment);
            var p2 = path.get(Math.min(lastIndex, segment + 1));

            var rewindHealth = Mth.lerp(t, p1.health(), p2.health());
            var currentHealth = player.getHealth();
            var targetHealth = Math.min(player.getMaxHealth(), Math.max(currentHealth, rewindHealth));

            if (targetHealth > currentHealth) {
                player.setHealth(targetHealth);

                if (ability.canPlayerUse(player) && ability.isRankModifierUnlocked("health_rewind")) {
                    this.getRelicData(player, stack).getLevelingData().addExperience("rewind", "health_rewind", targetHealth - currentHealth);
                    ability.getStatisticData().getMetricData("health_restored").addValue(targetHealth - currentHealth);
                }
            }
        }

        while (path.size() > 2 && cursor <= path.size() - 2)
            path.removeLast();

        if (cursor <= 0D) {
            stack.set(RelicsDataComponents.CLOT_OF_TIME_REWIND_CURSOR, -1D);
            stack.set(RelicsDataComponents.CLOT_OF_TIME_PATH, path);

            if (this.getCooldown(stack) <= 0)
                this.setCooldown(stack, this.getCooldownFromStat(player, stack));

            player.stopUsingItem();

            return;
        }

        stack.set(RelicsDataComponents.CLOT_OF_TIME_REWIND_CURSOR, cursor);
        stack.set(RelicsDataComponents.CLOT_OF_TIME_PATH, path);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BLOCK;
    }

    public record PathPointData(double x, double y, double z, float yRot, float xRot, float health) {
        public static final Codec<PathPointData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("x").forGetter(PathPointData::x),
                Codec.DOUBLE.fieldOf("y").forGetter(PathPointData::y),
                Codec.DOUBLE.fieldOf("z").forGetter(PathPointData::z),
                Codec.FLOAT.fieldOf("y_rot").forGetter(PathPointData::yRot),
                Codec.FLOAT.fieldOf("x_rot").forGetter(PathPointData::xRot),
                Codec.FLOAT.optionalFieldOf("health", 0F).forGetter(PathPointData::health)
        ).apply(instance, PathPointData::new));

        public static PathPointData fromPlayer(Player player) {
            return new PathPointData(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot(), player.getHealth());
        }
    }

    private float interpolateAngleCatmullrom(float t, float a0, float a1, float a2, float a3) {
        var p0 = a1 + Mth.wrapDegrees(a0 - a1);
        var p1 = a1;
        var p2 = p1 + Mth.wrapDegrees(a2 - p1);
        var p3 = p2 + Mth.wrapDegrees(a3 - p2);

        return Mth.wrapDegrees(Mth.catmullrom(t, p0, p1, p2, p3));
    }

    @EventBusSubscriber
    public static class CommonEvents {
        @SubscribeEvent
        public static void onLivingDamage(LivingDamageEvent.Pre event) {
            if (!(event.getEntity() instanceof Player player) || player.level().isClientSide())
                return;

            if (!(player.isUsingItem() && player.getUseItem().getItem() instanceof ClotOfTimeItem relic))
                return;

            var ability = relic.getRelicData(player, player.getUseItem()).getAbilitiesData().getAbilityData("rewind");

            if (!ability.canPlayerUse(player) || !ability.isRankModifierUnlocked("invulnerability"))
                return;

            event.setNewDamage(0F);
        }

        @SubscribeEvent
        public static void onEntityTick(EntityTickEvent.Post event) {
            if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide())
                return;

            var usingSlot = -1;

            if (player.isUsingItem() && player.getUseItem().getItem() instanceof ClotOfTimeItem)
                usingSlot = player.getUsedItemHand() == InteractionHand.MAIN_HAND ? player.getInventory().selected : Inventory.SLOT_OFFHAND;

            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                var stack = player.getInventory().getItem(i);

                if (!(stack.getItem() instanceof ClotOfTimeItem relic))
                    continue;

                var rememberedTicks = relic.getRememberedTicks(player, stack);

                if (rememberedTicks <= 0) {
                    stack.set(RelicsDataComponents.CLOT_OF_TIME_PATH, List.of());
                    stack.set(RelicsDataComponents.CLOT_OF_TIME_REWIND_CURSOR, -1D);

                    relic.setCooldown(stack, 0);

                    continue;
                }

                var cooldown = relic.getCooldown(stack);

                if (cooldown > 0)
                    relic.setCooldown(stack, cooldown - 1);

                var cursor = stack.getOrDefault(RelicsDataComponents.CLOT_OF_TIME_REWIND_CURSOR, -1D);
                var usingThisStack = i == usingSlot;

                if (usingThisStack) {
                    var ability = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("rewind");

                    if (ability.canPlayerUse(player) && ability.isRankModifierUnlocked("oblivion")) {
                        for (var mob : player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(16D))) {
                            if (mob.getTarget() == player)
                                mob.setTarget(null);
                        }
                    }
                }

                if (!usingThisStack && cursor >= 0D) {
                    stack.set(RelicsDataComponents.CLOT_OF_TIME_REWIND_CURSOR, -1D);

                    if (relic.getCooldown(stack) <= 0)
                        relic.setCooldown(stack, relic.getCooldownFromStat(player, stack));
                }

                if (usingThisStack)
                    continue;

                var path = new ArrayList<>(stack.getOrDefault(RelicsDataComponents.CLOT_OF_TIME_PATH, List.<PathPointData>of()));

                path.add(PathPointData.fromPlayer(player));

                if (path.size() > rememberedTicks)
                    path = new ArrayList<>(path.subList(path.size() - rememberedTicks, path.size()));

                stack.set(RelicsDataComponents.CLOT_OF_TIME_PATH, path);
            }
        }
    }

    @EventBusSubscriber(modid = Relics.MODID, value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onRenderFrame(RenderFrameEvent.Pre event) {
            var minecraft = Minecraft.getInstance();
            var player = minecraft.player;

            if (player == null || minecraft.isPaused())
                return;

            if (!(player.isUsingItem() && player.getUseItem().getItem() instanceof ClotOfTimeItem item))
                return;

            if (player.getTicksUsingItem() <= 1)
                return;

            var stack = player.getUseItem();
            var path = stack.getOrDefault(RelicsDataComponents.CLOT_OF_TIME_PATH, List.<PathPointData>of());

            if (path.size() < 2)
                return;

            var lastIndex = path.size() - 1;
            double cursor = stack.getOrDefault(RelicsDataComponents.CLOT_OF_TIME_REWIND_CURSOR, -1D);

            if (cursor < 0D || cursor > lastIndex)
                cursor = lastIndex;

            var partial = Mth.clamp(event.getPartialTick().getGameTimeDeltaPartialTick(false), 0F, 1F);
            var speed = 1.05D + Math.min(3D, Math.max(0, player.getTicksUsingItem() - 1) * 0.03D);

            cursor = Math.max(0D, cursor - speed * partial);

            var segment = Mth.floor(cursor);
            var t = (float) (cursor - segment);

            var p0 = path.get(Math.max(0, segment - 1));
            var p1 = path.get(segment);
            var p2 = path.get(Math.min(lastIndex, segment + 1));
            var p3 = path.get(Math.min(lastIndex, segment + 2));

            var x = Mth.catmullrom(t, (float) p0.x(), (float) p1.x(), (float) p2.x(), (float) p3.x());
            var y = Mth.catmullrom(t, (float) p0.y(), (float) p1.y(), (float) p2.y(), (float) p3.y());
            var z = Mth.catmullrom(t, (float) p0.z(), (float) p1.z(), (float) p2.z(), (float) p3.z());

            var targetYRot = item.interpolateAngleCatmullrom(t, p0.yRot(), p1.yRot(), p2.yRot(), p3.yRot());
            var targetXRot = Mth.catmullrom(t, p0.xRot(), p1.xRot(), p2.xRot(), p3.xRot());

            var yRot = Mth.rotLerp(0.45F, player.getYRot(), targetYRot);
            var xRot = Mth.lerp(0.45F, player.getXRot(), Mth.clamp(targetXRot, -90F, 90F));

            var level = player.level();
            var random = level.random;
            var center = new Vec3(x, y + player.getBbHeight() * 0.55D, z);
            var phase = (player.tickCount + partial) * 0.45D;

            for (int i = 0; i < 6; i++) {
                var angle = phase + i * (Math.PI * 2D / 6D);
                var radius = 0.45D + 0.12D * Math.sin(phase * 1.6D + i);

                var px = center.x + Math.cos(angle) * radius;
                var pz = center.z + Math.sin(angle) * radius;
                var py = center.y + Math.sin(angle * 1.7D) * 0.2D;

                var hue = (float) ((0.58D + phase * 0.025D + i * 0.06D) % 1D);
                var color = Color.getHSBColor(hue, 0.6F + random.nextFloat() * 0.25F, 1F);

                var inward = new Vec3(center.x - px, center.y - py, center.z - pz).scale(0.1D);
                var swirl = new Vec3(-Math.sin(angle), 0D, Math.cos(angle)).scale(0.03D);

                var velocity = inward.add(swirl).add(0D, 0.01D + random.nextDouble() * 0.01D, 0D);

                level.addParticle(ParticleUtils.constructSimpleSpark(color, 0.25F + random.nextFloat() * 0.15F, 20 + random.nextInt(10), 0.975F),
                        px, py, pz, velocity.x, velocity.y, velocity.z);
            }

            player.setYRot(yRot);
            player.setXRot(xRot);
            player.setPos(x, y, z);
            player.setYHeadRot(yRot);
            player.setDeltaMovement(Vec3.ZERO);

            player.yRotO = yRot;
            player.xRotO = xRot;
            player.yBodyRot = yRot;
            player.yHeadRotO = yRot;
            player.yBodyRotO = yRot;
            player.fallDistance = 0F;
        }
    }
}