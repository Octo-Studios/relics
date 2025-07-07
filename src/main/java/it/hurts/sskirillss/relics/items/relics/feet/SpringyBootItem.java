package it.hurts.sskirillss.relics.items.relics.feet;

import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.init.*;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.style.StyleTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.style.TooltipData;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
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
                                        .upgradeModifier(ScalingModelRegistry.LOGARITHMIC.get(), 0.3488D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("damage_modifier")
                                        .initialValue(0.05D, 0.15D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.0667D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("radius")
                                        .initialValue(1D, 3D)
                                        .upgradeModifier(ScalingModelRegistry.LOGARITHMIC.get(), 1.9534D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("damage")
                                        .initialValue(2.5D, 5D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.0857D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("stun")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.2571D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .maxLevel(10)
                        .step(100)
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

    public boolean isLeaped(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.SPRINGY_BOOT_LEAPED, false);
    }

    public void setLeaped(ItemStack stack, boolean leaped) {
        stack.set(DataComponentRegistry.SPRINGY_BOOT_LEAPED, leaped);
    }

    public int getLeaps(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.SPRINGY_BOOT_LEAPS, 0);
    }

    public void setLeaps(ItemStack stack, int leaps) {
        stack.set(DataComponentRegistry.SPRINGY_BOOT_LEAPS, Math.max(0, leaps));
    }

    public void addLeaps(ItemStack stack, int leaps) {
        this.setLeaps(stack, this.getLeaps(stack) + leaps);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        super.curioTick(slotContext, stack);

        var entity = slotContext.entity();

        var leaped = this.isLeaped(stack);
        var leaps = this.getLeaps(stack);

        if (this.isAbilityRankModifierUnlocked(entity, stack, "bounce", "disappearance") && leaped && leaps <= 0)
            entity.addEffect(new MobEffectInstance(RelicsMobEffects.VANISHING, 5, 0, false, false));
    }

    @EventBusSubscriber
    public static class CommonEvents {
        @SubscribeEvent
        public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
            var entity = event.getEntity();
            var level = entity.level();
            var random = entity.getRandom();

            for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.SPRINGY_BOOT.get())) {
                var relic = (SpringyBootItem) stack.getItem();

                if (!relic.canPlayerUseAbility(entity, stack, "bounce") || relic.isLeaped(stack) || !entity.isShiftKeyDown())
                    continue;

                var lookAngle = entity.getLookAngle();

                if (lookAngle.y() < 0F)
                    lookAngle = new Vec3(lookAngle.x(), 0F, lookAngle.z());

                var motion = entity.getDeltaMovement().add(lookAngle.multiply(-1F, 1F, -1F).add(0F, 0.5F, 0F).normalize().scale(relic.getStatValue(entity, stack, "bounce", "power")));

                entity.setDeltaMovement(motion);

                relic.setLeaped(stack, true);

                level.playSound(null, entity.blockPosition(), SoundRegistry.SPRING_BOING.get(), SoundSource.MASTER, 5F, 0.5F);

                for (int i = 0; i < 100; i += 1) {
                    var angle = random.nextFloat() * Math.PI * 2;
                    var radius = Math.sqrt(random.nextFloat()) * 0.35F;

                    var dx = Math.cos(angle) * radius;
                    var dz = Math.sin(angle) * radius;

                    level.addParticle(ParticleTypes.CLOUD, entity.getX(), entity.getY(), entity.getZ(), dx, random.nextFloat() * 0.25F, dz);
                }

                for (int i = 0; i < 50; i += 1) {
                    var particleMotion = motion.normalize().scale(random.nextFloat());

                    level.addParticle(ParticleTypes.CLOUD, entity.getX() + MathUtils.randomFloat(random) * 0.5F, entity.getY(), entity.getZ() + MathUtils.randomFloat(random) * 0.5F, particleMotion.x(), particleMotion.y(), particleMotion.z());
                }
            }
        }

        @SubscribeEvent
        public static void onLivingDamage(LivingDamageEvent.Pre event) {
            if (!(event.getSource().getEntity() instanceof LivingEntity entity))
                return;

            for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.SPRINGY_BOOT.get())) {
                var relic = (SpringyBootItem) stack.getItem();

                if (!relic.canPlayerUseAbility(entity, stack, "bounce") || !relic.isLeaped(stack) || !relic.isAbilityRankModifierUnlocked(entity, stack, "bounce", "strike"))
                    continue;

                event.setNewDamage((float) (event.getNewDamage() + (event.getNewDamage() * relic.getLeaps(stack) * relic.getStatValue(entity, stack, "bounce", "damage_modifier"))));
            }
        }
    }
}