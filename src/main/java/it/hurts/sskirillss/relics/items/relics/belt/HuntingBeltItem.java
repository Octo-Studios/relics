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
import it.hurts.sskirillss.relics.api.relics.abilities.targeting.AbilityTargetingTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.targeting.SelectorType;
import it.hurts.sskirillss.relics.init.RelicsConfigs;
import it.hurts.sskirillss.relics.init.RelicsDataComponents;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.init.RelicsScalingModels;
import it.hurts.sskirillss.relics.items.PetBoneItem;
import it.hurts.sskirillss.relics.items.relics.base.WearableRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicSlotModifier;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.research.ResearchTemplate;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.RelicStackingUtils;
import it.hurts.sskirillss.relics.utils.RelicStacks;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.TargetingUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

import java.util.ArrayList;

public class HuntingBeltItem extends WearableRelicItem {
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
                                        .targetValue(RelicsScalingModels.ADDITIVE.get(), 12D)
                                        .formatValue(value -> (int) (MathUtils.round(value, 0)))
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 6, 12).star(1, 8, 17).star(2, 6, 23).star(3, 15, 23).star(4, 13, 17).star(5, 15, 12)
                                        .link(0, 5).link(5, 4).link(4, 3).link(3, 2).link(2, 1).link(1, 0)
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("pack")
                                .rankModifier(1, "leader")
                                .rankModifier(3, "relentless")
                                .rankModifier(5, "revival")
                                .targeting(AbilityTargetingTemplate.builder()
                                        .selector(SelectorType.HARMFUL)
                                        .build())
                                .stat(AbilityStatTemplate.builder("damage_modifier")
                                        .initialValue(0.25D, 0.5D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 3.00075D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("pet_radius")
                                        .initialValue(3D, 5D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 14.9925D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("resistance_per_pet")
                                        .initialValue(0.01D, 0.05D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.10005D)
                                        .formatValue(value -> MathUtils.round(value * 100, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("revival_cost")
                                        .initialValue(10D, 7.5D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 1.00312D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("pet_damage")
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("pet_resistance")
                                                .rankModifierVisibilityState("leader", VisibilityState.OBFUSCATED)
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("bone_drop")
                                                .rankModifierVisibilityState("revival", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("pet_attacks")
                                                .formatValue((value) -> String.valueOf(value.intValue()))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("pet_damage")
                                                .formatValue((value) -> String.valueOf(MathUtils.round(value, 1)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("damage_resisted")
                                                .formatValue((value) -> String.valueOf(MathUtils.round(value, 1)))
                                                .rankModifierVisibilityState("leader", VisibilityState.OBFUSCATED)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("pet_bones")
                                                .formatValue((value) -> String.valueOf(value.intValue()))
                                                .rankModifierVisibilityState("revival", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 7, 11).star(1, 13, 17).star(2, 16, 9).star(3, 5, 19).star(4, 15, 3).star(5, 6, 4).star(6, 18, 16).star(7, 16, 24).star(8, 5, 28)
                                        .link(0, 2).link(2, 1).link(1, 3).link(3, 0).link(1, 0).link(5, 4).link(4, 2).link(3, 8).link(8, 7).link(7, 6)
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .step(100)
                        .build())
                .loot(LootTemplate.builder()
                        .entry(LootEntries.VILLAGE)
                        .build())
                .build();
    }

    @Override
    public RelicSlotModifier getSlotModifiers(LivingEntity entity, ItemStack stack) {
        return RelicSlotModifier.builder()
                .modifier("charm", (int) Math.round(getRelicData(entity, stack).getAbilitiesData().getAbilityData("slots").getStatData("amount").getValue()))
                .build();
    }

    @EventBusSubscriber
    public static class CommonEvents {
        private static final String BONE_DROP_TAG = Relics.MODID + ":hunting_belt_bone_drop";

        @SubscribeEvent
        public static void onLivingDamage(LivingDamageEvent.Pre event) {
            if (!(event.getSource().getEntity() instanceof OwnableEntity ownable))
                return;

            if (!(ownable.getOwner() instanceof LivingEntity owner))
                return;

            var stacks = RelicStackingUtils.stacks(owner, RelicsItems.HUNTING_BELT.get(), "pack")
                    .filter(entry -> TargetingUtils.canHarm(owner, event.getEntity(), entry.stack(), "pack"));

            var totalModifier = stacks.sum("damage_modifier");

            if (stacks.any("relentless"))
                event.getEntity().invulnerableTime = 0;

            if (totalModifier <= 0D)
                return;

            event.setNewDamage((float) (event.getNewDamage() * (1D + totalModifier)));
        }

        @SubscribeEvent
        public static void onLivingDamageOwner(LivingDamageEvent.Pre event) {
            var entity = event.getEntity();

            if (!(entity instanceof Player player))
                return;

            var original = event.getNewDamage();

            if (original <= 0F)
                return;

            var leaderBelts = RelicStackingUtils.stacks(player, RelicsItems.HUNTING_BELT.get(), "pack").withRank("leader");

            if (leaderBelts.isEmpty())
                return;

            var maxRadius = Math.max(0D, leaderBelts.max("pet_radius"));

            var nearbyPets = player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(maxRadius),
                    target -> target instanceof OwnableEntity ownable && ownable.getOwner() != null
                            && ownable.getOwner().getUUID().equals(player.getUUID()));

            if (nearbyPets.isEmpty())
                return;

            var beltResisted = new ArrayList<Double>(leaderBelts.size());
            var totalResisted = 0D;

            for (var entry : leaderBelts) {
                var radius = entry.stat("pet_radius");
                var resistancePerPet = entry.stat("resistance_per_pet");
                var petsInRange = nearbyPets.stream().filter(pet -> pet.distanceToSqr(player) <= radius * radius).count();

                var reduction = Math.min(petsInRange, 5L) * resistancePerPet;
                var clamped = Math.clamp(reduction, 0D, 0.9D);
                var resisted = original * clamped;

                beltResisted.add(resisted);
                totalResisted += resisted;
            }

            if (totalResisted <= 0D)
                return;

            var maxResisted = original * 0.9D;
            var scale = totalResisted > maxResisted ? maxResisted / totalResisted : 1D;
            var appliedResisted = totalResisted * scale;

            event.setNewDamage((float) (original - appliedResisted));

            for (var i = 0; i < leaderBelts.size(); i++) {
                var resisted = beltResisted.get(i) * scale;

                if (resisted <= 0D)
                    continue;

                var entry = leaderBelts.getEntries().get(i);

                entry.addExperience("pet_resistance", resisted);
                entry.addMetric("damage_resisted", resisted);
            }
        }

        @SubscribeEvent
        public static void onLivingDamage(LivingDamageEvent.Post event) {
            if (!(event.getSource().getEntity() instanceof OwnableEntity ownable))
                return;

            if (!(ownable.getOwner() instanceof LivingEntity owner))
                return;

            var damage = event.getNewDamage();

            if (damage <= 0F)
                return;

            for (var entry : RelicStackingUtils.stacks(owner, RelicsItems.HUNTING_BELT.get(), "pack")) {
                if (!TargetingUtils.canHarm(owner, event.getEntity(), entry.stack(), "pack"))
                    continue;

                entry.addExperience("pet_damage", 1);
                entry.addMetric("pet_attacks", 1);
                entry.addMetric("pet_damage", damage);
            }
        }

        @SubscribeEvent
        public static void onLivingDeath(LivingDeathEvent event) {
            var entity = event.getEntity();

            if (entity.level().isClientSide())
                return;

            if (entity instanceof OwnableEntity ownable && ownable.getOwner() instanceof LivingEntity owner
                    && !RelicsConfigs.RELICS_CONFIG.getHuntingBeltRevivalEntitiesBlacklist().contains(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString())) {
                var revivalBelts = RelicStackingUtils.stacks(owner, RelicsItems.HUNTING_BELT.get(), "pack").withRank("revival");

                if (!revivalBelts.isEmpty()) {
                    var requiredHealth = Double.MAX_VALUE;

                    for (var entry : revivalBelts)
                        requiredHealth = Math.min(requiredHealth, CommonEvents.getReviveHealthRequirement(entry, entity));

                    var boneStack = new ItemStack(RelicsItems.PET_BONE.get());

                    boneStack.set(RelicsDataComponents.PET_BONE_DATA.get(),
                            PetBoneItem.PetBoneData.fromEntity(entity, owner.getUUID(), requiredHealth));

                    entity.spawnAtLocation(boneStack);
                    entity.getPersistentData().putBoolean(BONE_DROP_TAG, true);

                    for (var entry : revivalBelts) {
                        entry.addExperience("bone_drop", 1);
                        entry.addMetric("pet_bones", 1);
                    }
                }
            }

            if (!(event.getSource().getEntity() instanceof Player player))
                return;

            if (entity instanceof Player)
                return;

            for (var stack : EntityUtils.findItemsInInventory(player, RelicsItems.PET_BONE.get())) {
                var data = stack.getOrDefault(RelicsDataComponents.PET_BONE_DATA.get(), PetBoneItem.PetBoneData.EMPTY);

                if (!data.isValid() || !player.getUUID().equals(data.owner()) || data.collectedHealth() >= data.requiredHealth())
                    continue;

                var gained = entity.getMaxHealth();
                var updated = Math.min(data.requiredHealth(), data.collectedHealth() + gained);

                stack.set(RelicsDataComponents.PET_BONE_DATA.get(), data.withCollectedHealth(updated));
            }
        }

        @SubscribeEvent
        public static void onLivingDrops(LivingDropsEvent event) {
            var entity = event.getEntity();

            if (entity.getPersistentData().getBoolean(BONE_DROP_TAG)) {
                event.getDrops().clear();

                entity.getPersistentData().remove(BONE_DROP_TAG);
            }
        }

        private static double getReviveHealthRequirement(RelicStacks.Entry entry, LivingEntity pet) {
            var attribute = pet.getAttribute(Attributes.MAX_HEALTH);
            var baseHealth = attribute != null ? attribute.getBaseValue() : pet.getMaxHealth();
            var modifier = entry.stat("revival_cost");

            return Math.max(1D, baseHealth * modifier);
        }
    }
}
