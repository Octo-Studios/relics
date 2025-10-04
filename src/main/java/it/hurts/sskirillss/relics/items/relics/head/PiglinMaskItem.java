package it.hurts.sskirillss.relics.items.relics.head;

import it.hurts.sskirillss.relics.api.relics.AbilityMetricTemplate;
import it.hurts.sskirillss.relics.api.relics.AbilityStatisticTemplate;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.VisibilityState;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourceTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourcesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.init.RelicsScalingModels;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.research.ResearchTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.style.StyleTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.style.TooltipData;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

public class PiglinMaskItem extends RelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("neutrality")
                                .initialMaxLevel(0)
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
                                .stat(StatTemplate.builder("efficiency")
                                        .initialValue(0.25D, 0.35D)
                                        .thresholdValue(0D, 1D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.0531D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("damage")
                                        .initialValue(0.1D, 0.25D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.2571D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("resistance")
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
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .step(100)
                        .build())
                .loot(LootTemplate.builder()
                        .entry(LootEntries.BASTION)
                        .build())
                .style(StyleTemplate.builder()
                        .tooltip(TooltipData.builder()
                                .borderTop(0xFFbaa8a8)
                                .borderBottom(0xFFbaa8a8)
                                .textured(true)
                                .build())
                        .build())
                .build();
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        super.curioTick(slotContext, stack);

    }

    @Override
    public boolean makesPiglinsNeutral(SlotContext slotContext, ItemStack stack) {
        return this.isAbilityUnlocked(slotContext.entity(), stack, "neutrality");
    }

    //    @EventBusSubscriber
//    public static class CommonEvents {
//
//    }
}