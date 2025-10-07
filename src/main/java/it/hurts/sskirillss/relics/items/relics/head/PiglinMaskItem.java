package it.hurts.sskirillss.relics.items.relics.head;

import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.init.RelicsScalingModels;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import top.theillusivec4.curios.api.SlotContext;

public class PiglinMaskItem extends RelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("neutrality")
                                .rankModifier(1, "legion")
                                .initialMaxLevel(0)
                                .build())
                        .ability(AbilityTemplate.builder("looting")
                                .modes("tooth", "buff")
                                .rankModifier(3, "frenzy")
                                .stat(StatTemplate.builder("chance")
                                        .initialValue(0.1D, 0.15D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.1212D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("amount")
                                        .initialValue(1D, 3D)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 0.25D)
                                        .formatValue(Double::intValue)
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("barter")
                                .stat(StatTemplate.builder("amount")
                                        .initialValue(1D, 3D)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 0.25D)
                                        .formatValue(Double::intValue)
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .step(100)
                        .build())
                .loot(LootTemplate.builder()
                        .entry(LootEntries.BASTION)
                        .build())
                .build();
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        super.curioTick(slotContext, stack);

    }

    @Override
    public boolean makesPiglinsNeutral(SlotContext slotContext, ItemStack stack) {
        return this.canPlayerUseAbility(slotContext.entity(), stack, "neutrality");
    }

    @EventBusSubscriber
    public static class CommonEvents {
        @SubscribeEvent
        public static void onLivingDamage1(LivingDamageEvent.Post event) {
            var target = event.getEntity();

            if (!(event.getSource().getEntity() instanceof LivingEntity source) || target instanceof AbstractPiglin)
                return;

            var level = source.level();

            for (var stack : EntityUtils.findEquippedCurios(source, RelicsItems.PIGLIN_MASK.get())) {
                var relic = (PiglinMaskItem) stack.getItem();

                if (!relic.isAbilityRankModifierUnlocked(source, stack, "neutrality", "legion"))
                    continue;

                for (var piglin : level.getEntitiesOfClass(AbstractPiglin.class, source.getBoundingBox().inflate(32))) {
                    if (piglin.getTarget() != null || !piglin.hasLineOfSight(target))
                        continue;

                    piglin.getBrain().setMemory(MemoryModuleType.ANGRY_AT, target.getUUID());
                }
            }
        }

        @SubscribeEvent
        public static void onLivingDamage2(LivingDamageEvent.Post event) {
            var source = event.getSource().getEntity();
            var target = event.getEntity();

            if (source == null)
                return;

            var level = source.level();

            for (var stack : EntityUtils.findEquippedCurios(target, RelicsItems.PIGLIN_MASK.get())) {
                var relic = (PiglinMaskItem) stack.getItem();

                if (!relic.isAbilityRankModifierUnlocked(target, stack, "neutrality", "legion"))
                    continue;

                for (var piglin : level.getEntitiesOfClass(AbstractPiglin.class, target.getBoundingBox().inflate(32))) {
                    if (piglin.getTarget() != null || !piglin.hasLineOfSight(source))
                        continue;

                    piglin.getBrain().setMemory(MemoryModuleType.ANGRY_AT, source.getUUID());
                }
            }
        }

        @SubscribeEvent
        public static void onLivingDeath(LivingDeathEvent event) {
            var source = event.getSource().getEntity();

            if (source == null)
                return;

            var entity = event.getEntity();

            var level = source.level();
            var random = level.getRandom();

            for (var stack : EntityUtils.findEquippedCurios(source, RelicsItems.PIGLIN_MASK.get())) {
                var relic = (PiglinMaskItem) stack.getItem();

                if (!relic.canPlayerUseAbility(entity, stack, "looting"))
                    continue;

                var amount = MathUtils.multicast(random, relic.getStatValue(entity, stack, "looting", "chance"), (int) relic.getStatValue(entity, stack, "looting", "amount"));

                for (int i = 0; i < amount; i++) {
                    var drop = new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), RelicsItems.GOLDEN_TOOTH.get().getDefaultInstance());

                    drop.setPickUpDelay(20);
                    drop.setDeltaMovement(MathUtils.randomFloat(random) * 0.35F, 0.25F + random.nextFloat() * 0.25F, MathUtils.randomFloat(random) * 0.35F);

                    level.addFreshEntity(drop);
                }
            }
        }
    }
}