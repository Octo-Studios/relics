package it.hurts.sskirillss.relics.items.relics.feet;

import it.hurts.sskirillss.relics.api.relics.*;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourceTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourcesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.init.*;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.research.ResearchTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.style.StyleTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.style.TooltipData;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.item.springy_boot.S2CBounceFromSurface;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import top.theillusivec4.curios.api.SlotContext;

public class SpringyBootItem extends RelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("bounce")
                                .rankModifier(1, "disappearance")
                                .rankModifier(3, "strike")
                                .rankModifier(5, "shockwave")
                                .stat(StatTemplate.builder("power")
                                        .initialValue(0.5D, 0.75D)
                                        .upgradeModifier(RelicsScalingModels.LOGARITHMIC.get(), 0.3488D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("damage_modifier")
                                        .initialValue(0.05D, 0.15D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.0667D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("radius")
                                        .initialValue(1D, 3D)
                                        .upgradeModifier(RelicsScalingModels.LOGARITHMIC.get(), 1.9534D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("damage")
                                        .initialValue(2.5D, 5D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.1429D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("stun")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.2571D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("bounce")
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("strike")
                                                .rankModifierCondition("strike")
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("create_shockwave")
                                                .rankModifierCondition("shockwave")
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("shockwave_hit")
                                                .rankModifierCondition("shockwave")
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
                .style(StyleTemplate.builder()
                        .tooltip(TooltipData.builder()
                                .borderTop(0xff94b920)
                                .borderBottom(0xff517910)
                                .textured(true)
                                .build())
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
            this.addAbilityMetricValue(entity, stack, "bounce", "bounce_duration", 1);

        if (cooldown > 0)
            this.addBounceCooldown(stack, -1);

        if (leaped) {
            if (entity.isInLiquid() || entity.isFallFlying() || (entity instanceof Player player && player.getAbilities().flying)) {
                this.setLeaped(stack, false);
                this.setLeaps(stack, 0);
            }

            if (this.isAbilityRankModifierUnlocked(entity, stack, "bounce", "disappearance") && leaps <= 0)
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

                if (!relic.canPlayerUseAbility(entity, stack, "bounce") || relic.isLeaped(stack) || relic.getBounceCooldown(stack) > 0 || !entity.isShiftKeyDown())
                    continue;

                power += relic.getStatValue(entity, stack, "bounce", "power");

                relic.addAbilityMetricValue(entity, stack, "bounce", "primary_bounces", 1);

                relic.setLeaped(stack, true);
                relic.addBounceCooldown(stack, 5);
            }

            if (power > 0D) {
                var lookAngle = entity.getLookAngle();

                if (lookAngle.y() < 0F)
                    lookAngle = new Vec3(lookAngle.x(), 0F, lookAngle.z());

                var motion = lookAngle.multiply(-1F, 1F, -1F).add(0F, 0.5F, 0F).normalize().scale(power);

                NetworkHandler.sendToClientsTrackingEntityAndSelf(new S2CBounceFromSurface(entity.getId(), motion.toVector3f()), entity);

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

                if (!relic.canPlayerUseAbility(entity, stack, "bounce") || !relic.isLeaped(stack) || !relic.isAbilityRankModifierUnlocked(entity, stack, "bounce", "strike"))
                    continue;

                var leaps = relic.getLeaps(stack);

                if (totalLeaps < leaps)
                    totalLeaps = leaps;

                var modifier = relic.getStatValue(entity, stack, "bounce", "damage_modifier");

                totalModifier += modifier;

                var damage = event.getNewDamage() * leaps * modifier;

                relic.addAbilityMetricValue(entity, stack, "bounce", "additional_damage", damage);

                if (relic.canAddRelicExperience(entity, stack, "bounce", "strike"))
                    relic.addRelicExperience(entity, stack, "bounce", "strike", damage);
            }

            var damage = event.getNewDamage() * totalLeaps * totalModifier;

            event.setNewDamage((float) (event.getNewDamage() + damage));
        }
    }
}