//package it.hurts.sskirillss.relics.items.relics;
//
//import it.hurts.sskirillss.relics.api.relics.IRelicItem;
//import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
//import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
//import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
//import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
//import it.hurts.sskirillss.relics.entities.ShadowGlaiveEntity;
//import it.hurts.sskirillss.relics.init.RelicsEntities;
//import it.hurts.sskirillss.relics.init.RelicsItems;
//import it.hurts.sskirillss.relics.init.ScalingModelRegistry;
//import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
//import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
//import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemColor;
//import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemShape;
//import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
//import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
//import it.hurts.sskirillss.relics.items.relics.base.data.style.StyleTemplate;
//import it.hurts.sskirillss.relics.items.relics.base.data.style.TooltipData;
//import it.hurts.sskirillss.relics.utils.EntityUtils;
//import it.hurts.sskirillss.relics.utils.MathUtils;
//import net.minecraft.world.entity.player.Player;
//import net.neoforged.bus.api.SubscribeEvent;
//import net.neoforged.fml.common.EventBusSubscriber;
//import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
//
//public class ShadowGlaiveItem extends RelicItem {
//    @Override
//    public RelicTemplate constructDefaultRelicTemplate() {
//        return super.constructDefaultRelicTemplate().toBuilder()
//                .style(StyleTemplate.builder()
//                        .tooltip(TooltipData.builder()
//                                .borderTop(0xff2c2430)
//                                .borderBottom(0xff471e65)
//                                .textured(true)
//                                .build())
//                        .build())
//                .build();
//    }
//
//    @Override
//    public AbilitiesTemplate constructDefaultAbilitiesTemplate() {
//        return AbilitiesTemplate.builder()
//                .ability(AbilityTemplate.builder("mayhem")
//                        .stat(StatTemplate.builder("chance")
//                                .initialValue(0.05D, 0.15D)
//                                .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.065D)
//                                .formatValue(value -> (int) MathUtils.round(value * 100, 0))
//                                .build())
//                        .stat(StatTemplate.builder("bounces")
//                                .initialValue(2D, 4D)
//                                .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.15D)
//                                .formatValue(value -> (int) MathUtils.round(value, 0))
//                                .build())
//                        .stat(StatTemplate.builder("damage")
//                                .initialValue(0.1D, 0.2D)
//                                .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.15D)
//                                .formatValue(value -> (int) MathUtils.round(value * 100, 0))
//                                .build())
//                        .build())
//                .ability(AbilityTemplate.builder("cloning")
//                        .requiredLevel(5)
//                        .stat(StatTemplate.builder("chance")
//                                .initialValue(0.05D, 0.1D)
//                                .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.1D)
//                                .formatValue(value -> (int) MathUtils.round(value * 100, 0))
//                                .build())
//                        .build())
//                .build();
//    }
//
//    @Override
//    public LevelingTemplate constructDefaultLevelingTemplate() {
//        return LevelingTemplate.builder()
//                .initialCost(100)
//                .step(100)
//                .build();
//    }
//
//    @Override
//    public LootTemplate constructDefaultLootTemplate() {
//        return LootTemplate.builder()
//                .entry(LootEntries.THE_END, LootEntries.END_LIKE)
//                .build();
//    }
//
//    @EventBusSubscriber
//    public static class ShadowGlaiveEvents {
//        @SubscribeEvent
//        public static void onLivingHurt(LivingDamageEvent.Post event) {
//            var damage = event.getOriginalDamage();
//
//            if (damage < 1F)
//                return;
//
//            var source = event.getSource().getDirectEntity();
//            var target = event.getEntity();
//
//            if (!(source instanceof Player player) || EntityUtils.isAlliedTo(source, target))
//                return;
//
//            for (var stack : EntityUtils.findEquippedCurios(player, RelicsItems.SHADOW_GLAIVE.get())) {
//                if (!(stack.getItem() instanceof IRelicItem relic) || !relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("mayhem").canPlayerUse(player)
//                        || source.getRandom().nextDouble() > relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("mayhem").getStatData("chance").getValue())
//                    continue;
//
//                var level = target.getCommandSenderWorld();
//
//                var entity = new ShadowGlaiveEntity(RelicsEntities.SHADOW_GLAIVE.get(), level);
//
//                entity.setDamage((float) (damage * relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("mayhem").getStatData("damage").getValue()));
//                entity.setMaxBounces((int) relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("mayhem").getStatData("bounces").getValue());
//                entity.getBouncedTargets().add(target.getStringUUID());
//                entity.setPos(target.getEyePosition());
//                entity.setOwner(source);
//
//                if (relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("cloning").canPlayerUse(player))
//                    entity.setChance((float) relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("cloning").getStatData("chance").getValue());
//
//                if (entity.locateNearestTargets().size() > 1) {
//                    level.addFreshEntity(entity);
//
//                    relic.spreadRelicExperience(player, stack, 1);
//                }
//            }
//        }
//    }
//}