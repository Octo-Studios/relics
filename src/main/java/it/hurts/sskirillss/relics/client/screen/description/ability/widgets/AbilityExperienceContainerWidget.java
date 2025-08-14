package it.hurts.sskirillss.relics.client.screen.description.ability.widgets;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.VisibilityState;
import it.hurts.sskirillss.relics.client.screen.description.ability.AbilityDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.SimpleDescriptionContainerWidget;
import it.hurts.sskirillss.relics.client.screen.utils.ScreenUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class AbilityExperienceContainerWidget extends SimpleDescriptionContainerWidget {
    public AbilityExperienceContainerWidget(DescriptionScreen screen) {
        super(screen);
    }

    @Override
    public List<FormattedCharSequence> getContent() {
        if (!(this.getScreen() instanceof AbilityDescriptionScreen screen))
            return new ArrayList<>();

        var sequences = new ArrayList<FormattedCharSequence>();

        var stack = this.getScreen().getStack();
        var relic = (IRelicItem) stack.getItem();
        var player = this.minecraft.player;
        var font = this.minecraft.font;
        var maxWidth = 320;

        var ability = screen.getSelectedAbility();

        var group = new LinkedHashMap<String, List<MutableComponent>>();
        var conditions = new HashMap<String, MutableComponent>();

        for (var source : relic.getExperienceSourcesTemplate(player, stack, ability).getSources().values()) {
            var state = source.getVisibilityState().apply(player, stack, ability, source.getId());

            if (state == VisibilityState.HIDDEN)
                continue;

            var condition = source.getConditionComponent()
                    .apply(player, stack, ability, source.getId())
                    .withStyle(ChatFormatting.BOLD);

            var key = condition.getString().trim();

            var description = Component.literal("● ")
                    .append(source.getDescriptionComponent().apply(player, stack, ability, source.getId()));

            if (state == VisibilityState.OBFUSCATED)
                description = ScreenUtils.randomizeAllCharacters(description, this.hashCode())
                        .withStyle(Style.EMPTY
                                .withFont(ScreenUtils.ILLAGER_ALT_FONT)
                                .withColor(DescriptionUtils.NEGATIVE_COLOR(true)));

            group.computeIfAbsent(key, k -> new ArrayList<>()).add(description);

            if (!key.isBlank() && !conditions.containsKey(key))
                conditions.put(key, condition);
        }

        var firstGroup = true;

        for (var entry : group.entrySet()) {
            if (!firstGroup)
                sequences.addAll(font.split(Component.literal(" "), maxWidth));

            firstGroup = false;

            var key = entry.getKey();

            if (!key.isBlank() && conditions.containsKey(key) && !conditions.get(key).equals(Component.empty()))
                sequences.addAll(font.split(conditions.get(key), maxWidth));

            for (var desc : entry.getValue())
                sequences.addAll(font.split(desc, maxWidth));
        }

        return sequences;
    }
}