package it.hurts.sskirillss.relics.items.relics.feet;

import it.hurts.sskirillss.relics.client.particles.GhostlyFogParticle;
import it.hurts.sskirillss.relics.api.relics.AbilityMetricTemplate;
import it.hurts.sskirillss.relics.api.relics.AbilityStatisticTemplate;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.VisibilityState;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourceTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourcesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.AbilityStatTemplate;
import it.hurts.sskirillss.relics.init.*;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.WearableRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.research.ResearchTemplate;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.S2CSpawnParticle;
import it.hurts.sskirillss.relics.network.packets.item.springy_boot.S2CBounceFromSurface;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.WorldUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import org.joml.Vector3f;
import top.theillusivec4.curios.api.SlotContext;

public class SpringyBootItem extends WearableRelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("bounce")
                                .rankModifier(1, "disappearance")
                                .rankModifier(3, "strike")
                                .rankModifier(5, "shockwave")
                                .stat(AbilityStatTemplate.builder("power")
                                        .initialValue(0.5D, 0.75D)
                                        .targetValue(RelicsScalingModels.LOGARITHMIC.get(), 1.99993D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("damage_modifier")
                                        .initialValue(0.05D, 0.15D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.50017D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("radius")
                                        .initialValue(1D, 3D)
                                        .targetValue(RelicsScalingModels.LOGARITHMIC.get(), 10.00005D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("damage")
                                        .initialValue(2.5D, 5D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 30.0075D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("stun")
                                        .initialValue(0.25D, 0.5D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 4.99925D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("bounce")
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("strike")
                                                .rankModifierVisibilityState("strike", VisibilityState.OBFUSCATED)
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("create_shockwave")
                                                .rankModifierVisibilityState("shockwave", VisibilityState.OBFUSCATED)
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("shockwave_hit")
                                                .rankModifierVisibilityState("shockwave", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("bounce_duration")
                                                .formatValue((value) -> MathUtils.formatTime(value.intValue()))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("primary_bounces")
                                                .formatValue((value) -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("secondary_bounces")
                                                .formatValue((value) -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("additional_damage")
                                                .formatValue((value) -> String.valueOf(MathUtils.round(value, 1)))
                                                .rankModifierVisibilityState("strike", VisibilityState.OBFUSCATED)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("shockwaves_amount")
                                                .formatValue((value) -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .rankModifierVisibilityState("shockwave", VisibilityState.OBFUSCATED)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("shockwave_targets")
                                                .formatValue((value) -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .rankModifierVisibilityState("shockwave", VisibilityState.OBFUSCATED)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("shockwave_damage")
                                                .formatValue((value) -> String.valueOf(MathUtils.round(value, 1)))
                                                .rankModifierVisibilityState("shockwave", VisibilityState.OBFUSCATED)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("shockwave_stun")
                                                .formatValue((value) -> MathUtils.formatTime(value.intValue()))
                                                .rankModifierVisibilityState("shockwave", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 6, 11).star(1, 16, 13).star(2, 11, 22).star(3, 20, 23).star(4, 2, 24).star(5, 6, 29).star(6, 18, 29)
                                        .link(5, 4).link(4, 2).link(2, 3).link(3, 6).link(2, 0).link(2, 1)
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .step(100)
                        .build())
                .loot(LootTemplate.builder()
                        .entry(LootEntries.MOUNTAIN)
                        .build())
                .build();
    }

    public int getBounceCooldown(ItemStack stack) {
        return stack.getOrDefault(RelicsDataComponents.SPRINGY_BOOT_BOUNCE_COOLDOWN, 0);
    }

    public void setBounceCooldown(ItemStack stack, int cooldown) {
        stack.set(RelicsDataComponents.SPRINGY_BOOT_BOUNCE_COOLDOWN, Math.max(0, cooldown));
    }

    public void addBounceCooldown(ItemStack stack, int cooldown) {
        this.setBounceCooldown(stack, this.getBounceCooldown(stack) + cooldown);
    }

    public boolean isLeaped(ItemStack stack) {
        return stack.getOrDefault(RelicsDataComponents.SPRINGY_BOOT_LEAPED, false);
    }

    public void setLeaped(ItemStack stack, boolean leaped) {
        stack.set(RelicsDataComponents.SPRINGY_BOOT_LEAPED, leaped);
    }

    public int getLeaps(ItemStack stack) {
        return stack.getOrDefault(RelicsDataComponents.SPRINGY_BOOT_LEAPS, 0);
    }

    public void setLeaps(ItemStack stack, int leaps) {
        stack.set(RelicsDataComponents.SPRINGY_BOOT_LEAPS, Math.max(0, leaps));
    }

    public void addLeaps(ItemStack stack, int leaps) {
        this.setLeaps(stack, this.getLeaps(stack) + leaps);
    }

    public static void spawnBounceFog(Level level, LivingEntity entity, double radius, int particleCount) {
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel))
            return;

        var random = level.getRandom();
        var center = entity.blockPosition();
        var maxOffset = 4;

        for (var i = 0; i < particleCount; i++) {
            var angle = random.nextDouble() * Mth.TWO_PI;
            var distance = Math.sqrt(random.nextDouble()) * radius;
            var x = entity.getX() + Math.cos(angle) * distance;
            var z = entity.getZ() + Math.sin(angle) * distance;
            var groundY = WorldUtils.findSurfaceY(level, Mth.floor(x), Mth.floor(z), center.getY(), maxOffset);
            var minAllowedY = Math.max(level.getMinBuildHeight(), center.getY() - maxOffset);
            var maxAllowedY = Math.min(level.getMaxBuildHeight(), center.getY() + maxOffset);

            if (groundY < minAllowedY || groundY > maxAllowedY)
                continue;

            var direction = new Vec3(x - entity.getX(), 0D, z - entity.getZ());

            if (direction.lengthSqr() < 0.001D)
                direction = new Vec3(random.nextDouble() - 0.5D, 0D, random.nextDouble() - 0.5D);

            direction = direction.normalize();

            var speed = 0.008D + random.nextDouble() * 0.012D;
            var lifetime = 10 + random.nextInt(30);
            var fogPosition = new Vector3f((float) x, groundY + 0.04F, (float) z);
            var fogMotion = new Vector3f((float) (direction.x * speed), 0.08F + random.nextFloat() * 0.08F, (float) (direction.z * speed));

            NetworkHandler.sendToClientsTrackingChunk(new S2CSpawnParticle(new GhostlyFogParticle.Options(lifetime), fogPosition, fogMotion), serverLevel, new ChunkPos(Mth.floor(x) >> 4, Mth.floor(z) >> 4));
        }
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        super.curioTick(slotContext, stack);

        var entity = slotContext.entity();
        var level = entity.level();

        if (level.isClientSide())
            return;

        var cooldown = this.getBounceCooldown(stack);
        var leaped = this.isLeaped(stack);
        var leaps = this.getLeaps(stack);

        if (entity.tickCount % 20 == 0 && (leaped || leaps > 0))
            this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("bounce").getStatisticData().getMetricData("bounce_duration").addValue(1);

        if (cooldown > 0)
            this.addBounceCooldown(stack, -1);

        if (leaped) {
            if (entity.isInLiquid() || entity.isFallFlying() || (entity instanceof Player player && player.getAbilities().flying)) {
                this.setLeaped(stack, false);
                this.setLeaps(stack, 0);
            }

            if (this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("bounce").getRankModifierData("disappearance").isEnabled() && leaps <= 0)
                entity.addEffect(new MobEffectInstance(RelicsMobEffects.VANISHING, 5, 0, false, false));
        }
    }

    @EventBusSubscriber
    public static class CommonEvents {
        @SubscribeEvent
        public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
            var entity = event.getEntity();
            var level = entity.level();

            if (level.isClientSide())
                return;

            var power = 0D;

            for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.SPRINGY_BOOT.get())) {
                var relic = (SpringyBootItem) stack.getItem();

                if (!relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("bounce").canPlayerUse(entity) || relic.isLeaped(stack) || relic.getBounceCooldown(stack) > 0 || !entity.isShiftKeyDown())
                    continue;

                power += relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("bounce").getStatData("power").getValue();

                relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("bounce").getStatisticData().getMetricData("primary_bounces").addValue(1);

                relic.setLeaped(stack, true);
                relic.addBounceCooldown(stack, 5);
            }

            if (power > 0D) {
                var lookAngle = entity.getLookAngle();

                if (lookAngle.y() < 0F)
                    lookAngle = new Vec3(lookAngle.x(), 0F, lookAngle.z());

                var motion = lookAngle.multiply(-1F, 1F, -1F).add(0F, 0.5F, 0F).normalize().scale(power);

                NetworkHandler.sendToClientsTrackingEntityAndSelf(new S2CBounceFromSurface(entity.getId(), motion.toVector3f()), entity);

                SpringyBootItem.spawnBounceFog(level, entity, Math.max(1.1D, power * 0.85D), 12 + Mth.ceil(power * 5D));

                level.playSound(null, entity.blockPosition(), RelicsSounds.SPRING_BOING.get(), SoundSource.MASTER, 5F, 0.5F);
            }
        }

        @SubscribeEvent
        public static void onLivingDamage(LivingDamageEvent.Pre event) {
            if (!(event.getSource().getEntity() instanceof LivingEntity entity))
                return;

            var totalModifier = 0D;
            var totalLeaps = 0;

            for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.SPRINGY_BOOT.get())) {
                var relic = (SpringyBootItem) stack.getItem();

                if (!relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("bounce").canPlayerUse(entity) || !relic.isLeaped(stack) || !relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("bounce").getRankModifierData("strike").isEnabled())
                    continue;

                var leaps = relic.getLeaps(stack);

                if (totalLeaps < leaps)
                    totalLeaps = leaps;

                var modifier = relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("bounce").getStatData("damage_modifier").getValue();

                totalModifier += modifier;

                var damage = event.getNewDamage() * leaps * modifier;

                relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("bounce").getStatisticData().getMetricData("additional_damage").addValue(damage);

                relic.getRelicData(entity, stack).getLevelingData().addExperience("bounce", "strike", damage);
            }

            var damage = event.getNewDamage() * totalLeaps * totalModifier;

            event.setNewDamage((float) (event.getNewDamage() + damage));
        }
    }
}
