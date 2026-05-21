package it.hurts.sskirillss.relics.handlers;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.events.relic.GatherRelicTemplateCacheKeyEvent;
import it.hurts.sskirillss.relics.api.events.relic.ModifyRelicTemplateEvent;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber
public class RankHandler {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onGatherRelicTemplateCacheKey(GatherRelicTemplateCacheKeyEvent event) {
        var entity = event.getBearer();
        var stack = event.getStack();

        if (!(stack.getItem() instanceof IRelicItem relic))
            return;

        event.add(Relics.MODID + ":rank", relic.getRelicData(entity, stack).getLevelingData().getRank());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onModifyRelicTemplate(ModifyRelicTemplateEvent event) {
        var entity = event.getBearer();
        var stack = event.getStack();

        if (!(stack.getItem() instanceof IRelicItem relic))
            return;

        var data = relic.getRelicData(entity, stack);

        var base = event.getTemplate();
        var rank = data.getLevelingData().getRank();

        if (rank <= 0)
            return;

        var abilities = base.getAbilities();
        var abilitiesBuilder = abilities.toBuilder();

        for (var template : abilities.getAbilities().values()) {
            var updatedMax = template.getInitialMaxLevel();

            for (var i = 0; i < rank; i++)
                updatedMax += (int) Math.ceil(updatedMax * template.getMaxLevelRankModifier());

            abilitiesBuilder.ability(template.toBuilder()
                    .initialMaxLevel(updatedMax)
                    .build());
        }

        event.setTemplate(base.toBuilder()
                .abilities(abilitiesBuilder.build())
                .build());
    }
}
