package it.hurts.sskirillss.relics.items.relics.feet;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.events.common.EntityBlockSpeedFactorEvent;
import it.hurts.sskirillss.relics.api.events.common.LivingSlippingEvent;
import it.hurts.sskirillss.relics.api.relics.AbilityMetricTemplate;
import it.hurts.sskirillss.relics.api.relics.AbilityStatisticTemplate;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.VisibilityState;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourceTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourcesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.init.RelicsDataComponents;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.init.RelicsScalingModels;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.research.ResearchTemplate;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.item.roller_skate.C2SCreateSpark;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.joml.Vector3f;
import top.theillusivec4.curios.api.SlotContext;

public class RollerSkateItem extends RelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("skating")
                                .rankModifier(1, "step_height")
                                .rankModifier(3, "resistance")
                                .rankModifier(5, "sparkling")
                                .stat(StatTemplate.builder("speed")
                                        .initialValue(0.1D, 0.25D)
                                        .upgradeModifier(RelicsScalingModels.LOGARITHMIC.get(), 0.6279D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("step_height")
                                        .initialValue(0.6D, 1D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.0571D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("resistance")
                                        .initialValue(0.1D, 0.25D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.0857D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("damage")
                                        .initialValue(1D, 2.5D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.0857D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("ignite")
                                        .initialValue(1D, 2D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.1143D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("distance_traveled")
                                                .formatValue((value) -> String.valueOf(MathUtils.round(value, 1)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("damage_resisted")
                                                .formatValue((value) -> String.valueOf(MathUtils.round(value, 1)))
                                                .rankModifierVisibilityState("resistance", VisibilityState.OBFUSCATED)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("sparks_created")
                                                .formatValue((value) -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .rankModifierVisibilityState("sparkling", VisibilityState.OBFUSCATED)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("damage_dealt")
                                                .formatValue((value) -> String.valueOf(MathUtils.round(value, 1)))
                                                .rankModifierVisibilityState("sparkling", VisibilityState.OBFUSCATED)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("ignite_duration")
                                                .formatValue((value) -> MathUtils.formatTime(value.intValue()))
                                                .rankModifierVisibilityState("sparkling", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("skating")
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("resisting_damage")
                                                .rankModifierVisibilityState("resistance", VisibilityState.OBFUSCATED)
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("creating_sparks")
                                                .rankModifierVisibilityState("sparkling", VisibilityState.OBFUSCATED)
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("spark_hit")
                                                .rankModifierVisibilityState("sparkling", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 3, 7).star(1, 13, 9).star(2, 6, 16).star(3, 16, 18).star(4, 9, 25).star(5, 15, 29)
                                        .link(5, 4).link(4, 2).link(2, 0).link(2, 1).link(4, 3)
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .step(200)
                        .build())
                .loot(LootTemplate.builder()
                        .entry(LootEntries.OVERWORLD)
                        .build())
                .build();
    }

    public int getDuration(ItemStack stack) {
        return stack.getOrDefault(RelicsDataComponents.ROLLER_SKATE_DURATION, 0);
    }

    public void setDuration(ItemStack stack, int duration) {
        stack.set(RelicsDataComponents.ROLLER_SKATE_DURATION, Math.clamp(duration, 0, this.getMaxDuration()));
    }

    public void addDuration(ItemStack stack, int progress) {
        this.setDuration(stack, this.getDuration(stack) + progress);
    }

    public int getMaxDuration() {
        return 5 * 20;
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof LivingEntity entity))
            return;

        var level = entity.level();
        var random = level.getRandom();

        var duration = this.getDuration(stack);

        if (entity.isSprinting() && entity.onGround() && !entity.isInLiquid() && !entity.isFallFlying()) {
            if (duration < this.getMaxDuration())
                this.addDuration(stack, 1);

            var movement = entity.getKnownMovement().multiply(1, 0, 1).length();

            if (movement > 0F) {
                this.addRelicExperience(entity, stack, "skating", "skating", 1D / 20D);

                this.addAbilityMetricValue(entity, stack, "skating", "distance_traveled", movement);
            }
        } else if (duration > 0)
            this.addDuration(stack, -1);

        if (duration > 0) {
            EntityUtils.resetAttribute(entity, stack, Attributes.MOVEMENT_SPEED, (float) (this.getStatValue(entity, stack, "skating", "speed") / this.getMaxDuration() * this.getDuration(stack)), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

            if (this.isAbilityRankModifierUnlocked(entity, stack, "skating", "step_height"))
                EntityUtils.resetAttribute(entity, stack, Attributes.STEP_HEIGHT, (float) this.getStatValue(entity, stack, "skating", "step_height"), AttributeModifier.Operation.ADD_VALUE);
        }

        if (this.isAbilityRankModifierUnlocked(entity, stack, "skating", "sparkling")) {
            var motion = entity.getDeltaMovement();

            var xMotion = motion.x;
            var zMotion = motion.z;

            var speed = Mth.sqrt((float) (xMotion * xMotion + zMotion * zMotion));

            if (speed > 0.25F && entity.onGround()) {
                var yawRad = entity.getYRot() * (float) Math.PI / 180F;

                var inputX = entity.xxa;
                var inputZ = entity.zza;

                var inLen = Mth.sqrt(inputX * inputX + inputZ * inputZ);

                if (inLen > 0.01F) {
                    inputX /= inLen;
                    inputZ /= inLen;

                    var directionX = inputZ * -Mth.sin(yawRad) + inputX * Mth.cos(yawRad);
                    var directionZ = inputZ * Mth.cos(yawRad) + inputX * Mth.sin(yawRad);

                    var dot = directionX * (xMotion / speed) + directionZ * (zMotion / speed);

                    if (dot < 0.75F) {
                        var count = Mth.clamp((int) (speed * 10), 1, 20);

                        for (int i = 0; i < count; i++) {
                            var force = ((0.25F + random.nextFloat() * 0.25F) * speed) * 2F;

                            var offset = MathUtils.randomFloat(random) * 0.35F;

                            var deltaX = -directionX * force + (-directionZ) * offset;
                            var deltaY = 0.2F + random.nextFloat() * 0.2F * speed;
                            var deltaZ = -directionZ * force + directionX * offset;

                            NetworkHandler.sendToServer(new C2SCreateSpark(slotContext.identifier(), slotContext.index(), entity.position().toVector3f(), new Vector3f(deltaX, deltaY, deltaZ), entity.getStringUUID(),
                                    (float) (this.getStatValue(entity, stack, "skating", "damage") * speed), (float) (this.getStatValue(entity, stack, "skating", "ignite") * speed), this.isRelicFlawless(entity, stack)));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        if (stack.getItem() == newStack.getItem())
            return;

        LivingEntity entity = slotContext.entity();

        EntityUtils.removeAttribute(entity, stack, Attributes.MOVEMENT_SPEED, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        EntityUtils.removeAttribute(entity, stack, Attributes.STEP_HEIGHT, AttributeModifier.Operation.ADD_VALUE);
    }

    @EventBusSubscriber(modid = Relics.MODID)
    public static class Events {
        @SubscribeEvent
        public static void onLivingSlipping(LivingSlippingEvent event) {
            var entity = event.getEntity();

            if (entity.isInLiquid() || entity.isFallFlying() || !entity.onGround())
                return;

            var stack = EntityUtils.findEquippedCurio(entity, RelicsItems.ROLLER_SKATE.get());

            if (stack.isEmpty())
                return;

            var relic = (RollerSkateItem) stack.getItem();

            var base = 0.6F;
            var max = 1F;
            var diff = max - base;
            var modifier = diff / relic.getMaxDuration() * relic.getDuration(stack);

            event.setFriction(base + modifier);
        }

        @SubscribeEvent
        public static void onSpeedFactor(EntityBlockSpeedFactorEvent event) {
            if (!(event.getEntity() instanceof LivingEntity entity) || entity.isInLiquid() || entity.isFallFlying() || !entity.onGround())
                return;

            var stack = EntityUtils.findEquippedCurio(entity, RelicsItems.ROLLER_SKATE.get());

            if (stack.isEmpty())
                return;

            event.setSpeedFactor(1F);
        }

        @SubscribeEvent
        public static void onLivingDamage(LivingDamageEvent.Pre event) {
            var entity = event.getEntity();

            for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.ROLLER_SKATE.get())) {
                var original = event.getOriginalDamage();
                var relic = (RollerSkateItem) stack.getItem();

                var duration = relic.getDuration(stack);

                if (!relic.canPlayerUseAbility(entity, stack, "skating") || !relic.isAbilityRankModifierUnlocked(entity, stack, "skating", "resistance") || duration <= 0)
                    continue;

                var modifier = (float) (original * (relic.getStatValue(entity, stack, "skating", "resistance") * ((float) duration / relic.getMaxDuration())));

                event.setNewDamage(original - modifier);

                relic.addAbilityMetricValue(entity, stack, "skating", "damage_resisted", modifier);

                relic.addRelicExperience(entity, stack, "skating", "resisting_damage", modifier);
            }
        }
    }
}