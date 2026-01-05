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
                                        .initialValue(0.1D, 0.25D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.0667D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("pet_radius")
                                        .initialValue(6D, 10D)
                                        .upgradeModifier(RelicsScalingModels.LOGARITHMIC.get(), 3.0276D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("resistance_per_pet")
                                        .initialValue(0.02D, 0.05D)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 0.005D)
                                        .formatValue(value -> MathUtils.round(value * 100, 1))
                                        .build())
                                .stat(StatTemplate.builder("revival_cost")
                                        .initialValue(1D, 1D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0D)
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

                if (!relic.canPlayerUseAbility(owner, stack, "pack"))
                    continue;

                totalModifier += relic.getStatValue(owner, stack, "pack", "damage_modifier");
                ignoreInvulnerability |= relic.isAbilityRankModifierUnlocked(owner, stack, "pack", "relentless");
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

                if (!relic.canPlayerUseAbility(player, stack, "pack")
                        || !relic.isAbilityRankModifierUnlocked(player, stack, "pack", "leader"))
                    continue;

                var radius = relic.getStatValue(player, stack, "pack", "pet_radius");

                var pets = player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius),
                        target -> target instanceof OwnableEntity ownable && ownable.getOwner() != null
                                && ownable.getOwner().getUUID().equals(player.getUUID()));

                if (pets.isEmpty())
                    continue;

                var reduction = pets.size() * relic.getStatValue(player, stack, "pack", "resistance_per_pet");
                var clamped = Math.clamp(reduction, 0D, 0.9D);
                var resisted = original * clamped;

                if (resisted <= 0F)
                    continue;

                event.setNewDamage((float) (original - resisted));

                relic.addRelicExperience(player, stack, "pack", "pet_resistance", resisted);
                relic.addAbilityMetricValue(player, stack, "pack", "damage_resisted", resisted);

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

                if (!relic.canPlayerUseAbility(owner, stack, "pack"))
                    continue;

                relic.addRelicExperience(owner, stack, "pack", "pet_damage", 1);
                relic.addAbilityMetricValue(owner, stack, "pack", "pet_attacks", 1);
                relic.addAbilityMetricValue(owner, stack, "pack", "pet_damage", damage);
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

                    if (!relic.canPlayerUseAbility(owner, stack, "pack")
                            || !relic.isAbilityRankModifierUnlocked(owner, stack, "pack", "revival"))
                        continue;

                    var requiredHealth = CommonEvents.getReviveHealthRequirement(relic, owner, stack, entity);
                    var boneStack = new ItemStack(RelicsItems.PET_BONE.get());

                    boneStack.set(RelicsDataComponents.PET_BONE_DATA.get(),
                            PetBoneItem.PetBoneData.fromEntity(entity, owner.getUUID(), requiredHealth));

                    entity.spawnAtLocation(boneStack);
                    entity.getPersistentData().putBoolean(BONE_DROP_TAG, true);

                    relic.addRelicExperience(owner, stack, "pack", "bone_drop", 1);
                    relic.addAbilityMetricValue(owner, stack, "pack", "pet_bones", 1);

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
            var modifier = relic.getStatValue(owner, stack, "pack", "revival_cost");

            return Math.max(1D, baseHealth * modifier);
        }
    }
}