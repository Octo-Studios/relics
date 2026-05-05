package it.hurts.sskirillss.relics.items.relics.belt;

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
import it.hurts.sskirillss.relics.api.relics.synergies.SynergyTemplate;
import it.hurts.sskirillss.relics.api.relics.synergies.conditions.AbilityConditionTemplate;
import it.hurts.sskirillss.relics.api.relics.synergies.conditions.RelicConditionTemplate;
import it.hurts.sskirillss.relics.api.relics.synergies.stats.SynergyStatTemplate;
import it.hurts.sskirillss.relics.entities.ChainedElectricityEntity;
import it.hurts.sskirillss.relics.init.RelicsDataComponents;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.init.RelicsRelicContainers;
import it.hurts.sskirillss.relics.init.RelicsScalingModels;
import it.hurts.sskirillss.relics.items.relics.base.WearableRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicSlotModifier;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.research.ResearchTemplate;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.item.jellyfish_necklace.C2SChainedElectricityPacket;
import it.hurts.sskirillss.relics.network.packets.item.kinetic_belt.C2SSetActive;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import top.theillusivec4.curios.api.SlotContext;

import java.awt.*;

public class KineticBeltItem extends WearableRelicItem {
    private static ResourceLocation getGravityAttributeId(ItemStack stack, SlotContext slotContext) {
        return ResourceLocation.fromNamespaceAndPath(Relics.MODID,
                BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath()
                        + "_" + BuiltInRegistries.ATTRIBUTE.getKey(Attributes.GRAVITY.value()).getPath()
                        + "_" + slotContext.identifier()
                        + "_" + slotContext.index()
                        + "_gliding");
    }

    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("slots")
                                .requiredPoints(2)
                                .initialMaxLevel(5)
                                .maxLevelRankModifier(0.1)
                                .stat(AbilityStatTemplate.builder("amount")
                                        .initialValue(1D, 2D)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 1D)
                                        .formatValue(value -> (int) (MathUtils.round(value, 0)))
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 11, 2).star(1, 5, 6).star(2, 17, 6).star(3, 2, 10).star(4, 20, 10).star(5, 11, 11).star(6, 2, 17).star(7, 7, 17).star(8, 15, 17).star(9, 20, 17).star(10, 2, 24).star(11, 20, 24).star(12, 11, 25)
                                        .link(0, 1).link(0, 2).link(2, 5).link(1, 5).link(5, 7).link(5, 8).link(7, 12).link(8, 12).link(7, 3).link(7, 6).link(7, 10).link(8, 4).link(8, 9).link(8, 11)
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("gliding")
                                .rankModifier(1, "momentum")
                                .rankModifier(3, "strike")
                                .rankModifier(5, "resistance")
                                .modes("enabled", "disabled")
                                .stat(AbilityStatTemplate.builder("efficiency")
                                        .initialValue(0.25D, 0.35D)
                                        .thresholdValue(0D, 1D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.0531D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("damage")
                                        .initialValue(0.1D, 0.25D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.2571D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("resistance")
                                        .initialValue(0.05D, 0.15D)
                                        .thresholdValue(0D, 0.75D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.1143D)
                                        .formatValue(value -> MathUtils.round(value * 100, 1))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("gliding")
                                                .modeVisibilityState("disabled", VisibilityState.HIDDEN)
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("strike")
                                                .modeVisibilityState("disabled", VisibilityState.HIDDEN)
                                                .rankModifierVisibilityState("strike", VisibilityState.OBFUSCATED)
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("resistance")
                                                .modeVisibilityState("disabled", VisibilityState.HIDDEN)
                                                .rankModifierVisibilityState("resistance", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("duration")
                                                .formatValue((value) -> MathUtils.formatTime(value.intValue()))
                                                .modeVisibilityState("disabled", VisibilityState.HIDDEN)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("damage")
                                                .formatValue((value) -> String.valueOf(MathUtils.round(value, 1)))
                                                .rankModifierVisibilityState("strike", VisibilityState.OBFUSCATED)
                                                .modeVisibilityState("disabled", VisibilityState.HIDDEN)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("resistance")
                                                .formatValue((value) -> String.valueOf(MathUtils.round(value, 1)))
                                                .rankModifierVisibilityState("resistance", VisibilityState.OBFUSCATED)
                                                .modeVisibilityState("disabled", VisibilityState.HIDDEN)
                                                .build())
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 2, 9).star(1, 20, 9).star(2, 5, 13).star(3, 17, 13).star(4, 11, 14).star(5, 2, 18).star(6, 20, 18).star(7, 5, 19).star(8, 11, 19).star(9, 17, 19).star(10, 7, 26).star(11, 15, 26)
                                        .link(8, 4).link(8, 2).link(8, 3).link(8, 10).link(8, 11).link(2, 7).link(2, 0).link(0, 5).link(3, 9).link(3, 1).link(1, 6)
                                        .build())
                                .build())
                        .synergy(SynergyTemplate.builder("electricity")
                                .modes("enabled", "disabled")
                                .stat(SynergyStatTemplate.builder("damage")
                                        .thresholdValue(1, 5)
                                        .formatValue(value -> value)
                                        .build())
                                .stat(SynergyStatTemplate.builder("lifetime")
                                        .thresholdValue(2, 10)
                                        .formatValue(value -> value)
                                        .build())
                                .condition(RelicConditionTemplate.builder(RelicsItems.KINETIC_BELT::get)
                                        .container(RelicsRelicContainers.CURIOS.get())
                                        .condition(AbilityConditionTemplate.builder("gliding")
                                                .build())
                                        .build())
                                .condition(RelicConditionTemplate.builder(RelicsItems.JELLYFISH_NECKLACE::get)
                                        .container(RelicsRelicContainers.CURIOS.get())
                                        .condition(AbilityConditionTemplate.builder("shock")
                                                .build())
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .step(100)
                        .build())
                .loot(LootTemplate.builder()
                        .entry(LootEntries.THE_END)
                        .entry(LootEntries.END_LIKE)
                        .build())
                .build();
    }

    public void setActive(ItemStack stack, boolean active) {
        stack.set(RelicsDataComponents.KINETIC_BELT_ACTIVE, active);
    }

    public boolean isActive(ItemStack stack) {
        return stack.getOrDefault(RelicsDataComponents.KINETIC_BELT_ACTIVE, false);
    }

    public void setLanded(ItemStack stack, boolean landed) {
        stack.set(RelicsDataComponents.KINETIC_BELT_LANDED, landed);
    }

    public boolean isLanded(ItemStack stack) {
        return stack.getOrDefault(RelicsDataComponents.KINETIC_BELT_LANDED, false);
    }

    private ChainedElectricityEntity getLastElectricityEntity(LivingEntity owner, ItemStack stack) {
        var entityId = stack.getOrDefault(RelicsDataComponents.KINETIC_BELT_LAST_ELECTRICITY_ID, -1);

        if (entityId < 0)
            return null;

        var raw = owner.level().getEntity(entityId);

        if (raw instanceof ChainedElectricityEntity electricity && electricity.isAlive())
            return electricity;

        this.setLastElectricityEntityId(stack, -1);

        return null;
    }

    private void setLastElectricityEntityId(ItemStack stack, int entityId) {
        stack.set(RelicsDataComponents.KINETIC_BELT_LAST_ELECTRICITY_ID, entityId);
    }

    @Override
    public RelicSlotModifier getSlotModifiers(LivingEntity entity, ItemStack stack) {
        return RelicSlotModifier.builder()
                .modifier("charm", (int) Math.round(getRelicData(entity, stack).getAbilitiesData().getAbilityData("slots").getStatData("amount").getValue()))
                .build();
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        super.curioTick(slotContext, stack);

        var entity = slotContext.entity();
        var level = entity.level();
        var gravityAttributeId = getGravityAttributeId(stack, slotContext);

        if (!this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("gliding").canPlayerUse(entity) || this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("gliding").getMode().equals("disabled")) {
            EntityUtils.removeAttribute(entity, Attributes.GRAVITY, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, gravityAttributeId);

            if (!level.isClientSide())
                this.setLastElectricityEntityId(stack, -1);

            return;
        }
        var random = level.getRandom();
        var isActive = this.isActive(stack);
        var isLanded = this.isLanded(stack);

        if (!level.isClientSide() && !isActive)
            this.setLastElectricityEntityId(stack, -1);

        var onGround = entity.onGround();

        var hasAttribute = EntityUtils.hasAttribute(entity, Attributes.GRAVITY, gravityAttributeId);

        if (level.isClientSide()) {
            if (entity instanceof LocalPlayer player) {
                if (player.input.jumping && !player.isFallFlying() && !player.getAbilities().flying && !player.isSwimming())
                    NetworkHandler.sendToServer(new C2SSetActive(slotContext.identifier(), slotContext.index(), true));
                else if (isActive)
                    NetworkHandler.sendToServer(new C2SSetActive(slotContext.identifier(), slotContext.index(), false));
            }
        }

        if (isLanded) {
            if (this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("gliding").getRankModifierData("momentum").isEnabled() && !onGround && isActive)
                this.setLanded(stack, false);
        } else {
            if (onGround && !isActive)
                this.setLanded(stack, true);
        }

        var motion = entity.getDeltaMovement();
        var verticalMotion = (float) -motion.y;

        var minVy = 0.25F;
        var maxVy = 7.5F;
        var baseSlowFactor = Math.clamp((maxVy - verticalMotion) / (maxVy - minVy), 0F, 1F);

        var slowFactor = (float) Math.sqrt(baseSlowFactor);

        var maxReductionPerTick = 1F;

        var reduction = maxReductionPerTick * slowFactor;

        entity.fallDistance = Math.max(entity.fallDistance - reduction, 0.1F);

        if (isActive) {
            if (entity.tickCount % 20 == 0) {
                this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("gliding").getStatisticData().getMetricData("duration").addValue(1);

                this.getRelicData(entity, stack).getLevelingData().addExperience("gliding", "gliding", 1);
            }

            if (!hasAttribute)
                EntityUtils.applyAttribute(entity, Attributes.GRAVITY, (float) -Math.min(this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("gliding").getStatData("efficiency").getValue(), 0.9F), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, gravityAttributeId);

            var prevPosition = new Vec3(entity.xOld, entity.yOld, entity.zOld);
            var position = entity.position();
            var movementDelta = position.subtract(prevPosition);
            var horizontalDelta = new Vec3(movementDelta.x, 0, movementDelta.z);
            var horizontalDistance = horizontalDelta.length();
            var trailLagFactor = Math.clamp(0.3D + horizontalDistance * 0.18D, 0.3D, 0.85D);
            var laggedMovementDelta = movementDelta.scale(1D - trailLagFactor);
            var distance = laggedMovementDelta.length();
            var spawnStep = 0.075D;
            int spawnCount = (int) (distance / spawnStep) + 1;

            var yawRadians = Math.toRadians(entity.yBodyRot);
            var facingForward = new Vec3(-Math.sin(yawRadians), 0, Math.cos(yawRadians)).normalize();
            var forward = horizontalDelta.lengthSqr() > 1.0E-6D ? horizontalDelta.normalize() : facingForward;
            var right = forward.cross(new Vec3(0, 1, 0)).normalize();
            var left = right.scale(-1);
            var sideOffset = 0.4D;
            var backOffset = 0.2D;
            var speedBackOffset = backOffset + Math.min(horizontalDistance * 0.5D, 1.4D);
            var electricitySpawnPosition = prevPosition.add(laggedMovementDelta).subtract(forward.scale(speedBackOffset));

            if (level.isClientSide() && this.getRelicData(entity, stack).getAbilitiesData().getSynergyData("electricity").isUnlocked()
                    && this.getRelicData(entity, stack).getAbilitiesData().getSynergyData("electricity").getMode().equals("enabled")) {
                var synergy = this.getRelicData(entity, stack).getAbilitiesData().getSynergyData("electricity");

                NetworkHandler.sendToServer(new C2SChainedElectricityPacket(
                        electricitySpawnPosition.x,
                        electricitySpawnPosition.y + entity.getBbHeight() / 2F - 0.15F,
                        electricitySpawnPosition.z,
                        (float) synergy.getStatData("damage").getValue(),
                        (int) synergy.getStatData("lifetime").getValue(),
                        this.getRelicData(entity, stack).isVisuallyFlawless(),
                        slotContext.identifier(),
                        slotContext.index()
                ));
            }

            for (var i = 0; i <= spawnCount; i++) {
                var t = spawnCount == 0 ? 0 : (double) i / spawnCount;
                var basePosition = prevPosition.add(laggedMovementDelta.scale(t)).subtract(forward.scale(speedBackOffset));
                var centerX = basePosition.x;
                var centerY = basePosition.y + entity.getBbHeight() / 2F - 0.15F;
                var centerZ = basePosition.z;

                for (var side : new Vec3[]{right, left}) {
                    var x = centerX + side.x * sideOffset + (random.nextDouble() * 2 - 1) * 0.02;
                    var y = centerY + (random.nextDouble() * 2 - 1) * 0.02;
                    var z = centerZ + side.z * sideOffset + (random.nextDouble() * 2 - 1) * 0.02;

                    var vx = (random.nextFloat() * 2 - 1) * 0.01F;
                    var vy = (random.nextFloat() * 2 - 1) * 0.01F;
                    var vz = (random.nextFloat() * 2 - 1) * 0.01F;

                    level.addParticle(ParticleUtils.constructSimpleSpark(this.getRelicData(entity, stack).isVisuallyFlawless() ? new Color(200 + random.nextInt(50), 150 + random.nextInt(50), 0) : new Color(random.nextInt(50), 75 + random.nextInt(100), 255), 0.1F + random.nextFloat() * 0.15F, 100 + random.nextInt(20), 0.995F), x, y, z, vx, vy, vz);
                }
            }
        } else if (hasAttribute)
            EntityUtils.removeAttribute(entity, Attributes.GRAVITY, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, gravityAttributeId);
    }

    @EventBusSubscriber
    public static class CommonEvents {
        @SubscribeEvent
        public static void onLivingFall(LivingFallEvent event) {
            var entity = event.getEntity();

            for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.KINETIC_BELT.get())) {
                var relic = (KineticBeltItem) stack.getItem();

                if (!relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("gliding").canPlayerUse(entity) || relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("gliding").getMode().equals("disabled")
                        || !relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("gliding").getRankModifierData("momentum").isEnabled() || !relic.isActive(stack))
                    continue;

                event.setDistance(0);

                break;
            }
        }

        @SubscribeEvent
        public static void onLivingDamage(LivingDamageEvent.Pre event) {
            var entity = event.getEntity();

            var original = event.getNewDamage();

            if (event.getSource().getDirectEntity() instanceof Projectile projectile && projectile.getOwner() instanceof LivingEntity source) {
                double strikeTotal = 0D;

                for (var stack : EntityUtils.findEquippedCurios(source, RelicsItems.KINETIC_BELT.get())) {
                    var relic = (KineticBeltItem) stack.getItem();

                    if (!relic.getRelicData(source, stack).getAbilitiesData().getAbilityData("gliding").canPlayerUse(source) || relic.getRelicData(source, stack).getAbilitiesData().getAbilityData("gliding").getMode().equals("disabled")
                            || !relic.getRelicData(source, stack).getAbilitiesData().getAbilityData("gliding").getRankModifierData("strike").isEnabled() || !relic.isActive(stack))
                        continue;

                    var additional = original * relic.getRelicData(source, stack).getAbilitiesData().getAbilityData("gliding").getStatData("damage").getValue();

                    relic.getRelicData(source, stack).getLevelingData().addExperience("gliding", "strike", additional);

                    relic.getRelicData(source, stack).getAbilitiesData().getAbilityData("gliding").getStatisticData().getMetricData("damage").addValue(additional);

                    strikeTotal += additional;
                }

                event.setNewDamage((float) (event.getNewDamage() + strikeTotal));
            }

            double resistanceTotal = 0D;

            for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.KINETIC_BELT.get())) {
                var relic = (KineticBeltItem) stack.getItem();

                if (!relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("gliding").canPlayerUse(entity) || relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("gliding").getMode().equals("disabled")
                        || !relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("gliding").getRankModifierData("resistance").isEnabled() || !relic.isActive(stack))
                    continue;

                var additional = original * relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("gliding").getStatData("resistance").getValue();

                relic.getRelicData(entity, stack).getLevelingData().addExperience("gliding", "resistance", additional);

                relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("gliding").getStatisticData().getMetricData("resistance").addValue(additional);

                resistanceTotal += additional;
            }

            event.setNewDamage((float) Math.max(0D, event.getNewDamage() - resistanceTotal));
        }
    }
}

