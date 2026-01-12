package it.hurts.sskirillss.relics.items.relics.belt;

import it.hurts.sskirillss.relics.api.relics.AbilityMetricTemplate;
import it.hurts.sskirillss.relics.api.relics.AbilityStatisticTemplate;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.VisibilityState;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourceTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourcesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.init.RelicsScalingModels;
import it.hurts.sskirillss.relics.init.RelicsDataComponents;
import it.hurts.sskirillss.relics.items.PetBoneItem;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.research.ResearchTemplate;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
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
import top.theillusivec4.curios.api.SlotContext;

public class HuntingBeltItem extends RelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("pack")
                                .rankModifier(1, "leader")
                                .rankModifier(3, "relentless")
                                .rankModifier(5, "revival")
                                .stat(StatTemplate.builder("damage_modifier")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.1429D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("pet_radius")
                                        .initialValue(3D, 5D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.0571D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("resistance_per_pet")
                                        .initialValue(0.01D, 0.05D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.0286D)
                                        .formatValue(value -> MathUtils.round(value * 100, 1))
                                        .build())
                                .stat(StatTemplate.builder("revival_cost")
                                        .initialValue(10D, 7.5D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), -0.02475D)
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

    @EventBusSubscriber
    public static class CommonEvents {
        private static final String BONE_DROP_TAG = Relics.MODID + ":hunting_belt_bone_drop";

        @SubscribeEvent
        public static void onLivingDamage(LivingDamageEvent.Pre event) {
            if (!(event.getSource().getEntity() instanceof OwnableEntity ownable))
                return;

            if (!(ownable.getOwner() instanceof LivingEntity owner))
                return;

            var totalModifier = 0D;
            var ignoreInvulnerability = false;

            for (var stack : EntityUtils.findEquippedCurios(owner, RelicsItems.HUNTING_BELT.get())) {
                var relic = (HuntingBeltItem) stack.getItem();

                if (!relic.getRelicData(owner, stack).getAbilitiesData().getAbilityData("pack").canPlayerUse(owner))
                    continue;

                totalModifier += relic.getRelicData(owner, stack).getAbilitiesData().getAbilityData("pack").getStatData("damage_modifier").getValue();
                ignoreInvulnerability |= relic.getRelicData(owner, stack).getAbilitiesData().getAbilityData("pack").isRankModifierUnlocked("relentless");
            }

            if (ignoreInvulnerability)
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

            for (var stack : EntityUtils.findEquippedCurios(player, RelicsItems.HUNTING_BELT.get())) {
                var relic = (HuntingBeltItem) stack.getItem();

                if (!relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("pack").canPlayerUse(player)
                        || !relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("pack").isRankModifierUnlocked("leader"))
                    continue;

                var radius = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("pack").getStatData("pet_radius").getValue();

                var pets = player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius),
                        target -> target instanceof OwnableEntity ownable && ownable.getOwner() != null
                                && ownable.getOwner().getUUID().equals(player.getUUID()));

                if (pets.isEmpty())
                    continue;

                var reduction = Math.min(pets.size(), 5) * relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("pack").getStatData("resistance_per_pet").getValue();
                var clamped = Math.clamp(reduction, 0D, 0.9D);
                var resisted = original * clamped;

                if (resisted <= 0F)
                    continue;

                event.setNewDamage((float) (original - resisted));

                relic.getRelicData(player, stack).getLevelingData().addExperience("pack", "pet_resistance", resisted);
                relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("pack").getStatisticData().getMetricData("damage_resisted").addValue(resisted);

                break;
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

            for (var stack : EntityUtils.findEquippedCurios(owner, RelicsItems.HUNTING_BELT.get())) {
                var relic = (HuntingBeltItem) stack.getItem();

                if (!relic.getRelicData(owner, stack).getAbilitiesData().getAbilityData("pack").canPlayerUse(owner))
                    continue;

                relic.getRelicData(owner, stack).getLevelingData().addExperience("pack", "pet_damage", 1);
                relic.getRelicData(owner, stack).getAbilitiesData().getAbilityData("pack").getStatisticData().getMetricData("pet_attacks").addValue(1);
                relic.getRelicData(owner, stack).getAbilitiesData().getAbilityData("pack").getStatisticData().getMetricData("pet_damage").addValue(damage);
            }
        }

        @SubscribeEvent
        public static void onLivingDeath(LivingDeathEvent event) {
            var entity = event.getEntity();

            if (entity.level().isClientSide())
                return;

            if (entity instanceof OwnableEntity ownable && ownable.getOwner() instanceof LivingEntity owner) {
                for (var stack : EntityUtils.findEquippedCurios(owner, RelicsItems.HUNTING_BELT.get())) {
                    var relic = (HuntingBeltItem) stack.getItem();

                    if (!relic.getRelicData(owner, stack).getAbilitiesData().getAbilityData("pack").canPlayerUse(owner)
                            || !relic.getRelicData(owner, stack).getAbilitiesData().getAbilityData("pack").isRankModifierUnlocked("revival"))
                        continue;

                    var requiredHealth = CommonEvents.getReviveHealthRequirement(relic, owner, stack, entity);
                    var boneStack = new ItemStack(RelicsItems.PET_BONE.get());

                    boneStack.set(RelicsDataComponents.PET_BONE_DATA.get(),
                            PetBoneItem.PetBoneData.fromEntity(entity, owner.getUUID(), requiredHealth));

                    entity.spawnAtLocation(boneStack);
                    entity.getPersistentData().putBoolean(BONE_DROP_TAG, true);

                    relic.getRelicData(owner, stack).getLevelingData().addExperience("pack", "bone_drop", 1);
                    relic.getRelicData(owner, stack).getAbilitiesData().getAbilityData("pack").getStatisticData().getMetricData("pet_bones").addValue(1);

                    break;
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

        private static double getReviveHealthRequirement(HuntingBeltItem relic, LivingEntity owner, ItemStack stack, LivingEntity pet) {
            var attribute = pet.getAttribute(Attributes.MAX_HEALTH);
            var baseHealth = attribute != null ? attribute.getBaseValue() : pet.getMaxHealth();
            var modifier = relic.getRelicData(owner, stack).getAbilitiesData().getAbilityData("pack").getStatData("revival_cost").getValue();

            return Math.max(1D, baseHealth * modifier);
        }
    }
}