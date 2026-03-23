package it.hurts.sskirillss.relics.handlers;

import it.hurts.sskirillss.relics.api.events.relic.GatherRelicTemplateEvent;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@EventBusSubscriber
public class RankHandler {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onGatherRelicData(GatherRelicTemplateEvent event) {
        var entity = event.getBearer();
        var stack = event.getStack();

        if (!(stack.getItem() instanceof IRelicItem relic))
            return;

        var data = relic.getRelicData(entity, stack);

        var base = event.getTemplate();
        var rank = data.getLevelingData().getRank();

        var abilities = base.getAbilities();

        var updatedAbilitiesMap = abilities.getAbilities().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                            var template = entry.getValue();

                            var updatedMax = IntStream.range(0, rank)
                                    .reduce(template.getInitialMaxLevel(), (level, i) -> level + (int) Math.ceil(level * template.getMaxLevelRankModifier()));

                            return template.toBuilder()
                                    .initialMaxLevel(updatedMax)
                                    .build();
                        }
                ));

        var abilitiesBuilder = abilities.toBuilder();

        updatedAbilitiesMap.forEach((key, template) -> abilitiesBuilder.ability(template.toBuilder()
                .initialMaxLevel(template.getInitialMaxLevel())
                .build()
        ));

        event.setTemplate(base.toBuilder()
                .abilities(abilitiesBuilder.build())
                .build());
    }
}