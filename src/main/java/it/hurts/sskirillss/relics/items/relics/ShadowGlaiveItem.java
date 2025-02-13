package it.hurts.sskirillss.relics.items.relics;

import it.hurts.sskirillss.relics.entities.ShadowGlaiveEntity;
import it.hurts.sskirillss.relics.init.EntityRegistry;
import it.hurts.sskirillss.relics.init.ItemRegistry;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.*;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemColor;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemShape;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.UpgradeOperation;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootData;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.research.ResearchData;
import it.hurts.sskirillss.relics.items.relics.base.data.style.BeamsData;
import it.hurts.sskirillss.relics.items.relics.base.data.style.StyleData;
import it.hurts.sskirillss.relics.items.relics.base.data.style.TooltipData;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public class ShadowGlaiveItem extends RelicItem {
    @Override
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("mayhem")
                                .stat(StatData.builder("chance")
                                        .initialValue(0.05D, 0.15D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.065D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatData.builder("bounces")
                                        .initialValue(2D, 4D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.15D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(StatData.builder("damage")
                                        .initialValue(0.1D, 0.2D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.15D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .research(ResearchData.builder()
                                        .star(0, 11, 2).star(1, 3, 19).star(2, 11, 19)
                                        .star(3, 19, 19).star(4, 11, 29)
                                        .link(0, 2).link(2, 1).link(2, 3).link(2, 4)
                                        .build())
                                .build())
                        .ability(AbilityData.builder("cloning")
                                .requiredLevel(5)
                                .stat(StatData.builder("chance")
                                        .initialValue(0.05D, 0.1D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.1D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .research(ResearchData.builder()
                                        .star(0, 12, 2).star(1, 7, 7).star(2, 17, 14)
                                        .star(3, 6, 22).star(4, 11, 29)
                                        .link(0, 1).link(1, 2).link(2, 3).link(3, 4)
                                        .build())
                                .build())
                        .build())
                .style(StyleData.builder()
                        .tooltip(TooltipData.builder()
                                .borderTop(0xff2c2430)
                                .borderBottom(0xff471e65)
                                .textured(true)
                                .build())
                        .beams(BeamsData.builder()
                                .startColor(0xFFFF00FF)
                                .endColor(0x000000FF)
                                .build())
                        .build())
                .leveling(LevelingData.builder()
                        .initialCost(100)
                        .maxLevel(15)
                        .step(100)
                        .sources(LevelingSourcesData.builder()
                                .source(LevelingSourceData.abilityBuilder("mayhem")
                                        .initialValue(1)
                                        .gem(GemShape.SQUARE, GemColor.PURPLE)
                                        .build())
                                .build())
                        .build())
                .loot(LootData.builder()
                        .entry(LootEntries.THE_END, LootEntries.END_LIKE)
                        .build())
                .build();
    }

    @EventBusSubscriber
    public static class ShadowGlaiveEvents {
        @SubscribeEvent
        public static void onLivingHurt(LivingDamageEvent.Post event) {
            var damage = event.getOriginalDamage();

            if (damage < 1F)
                return;

            var source = event.getSource().getDirectEntity();
            var target = event.getEntity();

            if (!(source instanceof Player player) || EntityUtils.isAlliedTo(source, target))
                return;

            for (var stack : EntityUtils.findEquippedCurios(player, ItemRegistry.SHADOW_GLAIVE.get())) {
                if (!(stack.getItem() instanceof IRelicItem relic) || !relic.canPlayerUseAbility(player, stack, "mayhem")
                        || source.getRandom().nextDouble() > relic.getStatValue(stack, "mayhem", "chance"))
                    continue;

                var level = target.getCommandSenderWorld();

                var entity = new ShadowGlaiveEntity(EntityRegistry.SHADOW_GLAIVE.get(), level);

                entity.setDamage((float) (damage * relic.getStatValue(stack, "mayhem", "damage")));
                entity.setMaxBounces((int) relic.getStatValue(stack, "mayhem", "bounces"));
                entity.getBouncedTargets().add(target.getStringUUID());
                entity.setPos(target.getEyePosition());
                entity.setOwner(source);

                if (relic.canPlayerUseAbility(player, stack, "cloning"))
                    entity.setChance((float) relic.getStatValue(stack, "cloning", "chance"));

                if (entity.locateNearestTargets().size() > 1) {
                    level.addFreshEntity(entity);

                    relic.spreadRelicExperience(player, stack, 1);
                }
            }
        }
    }
}