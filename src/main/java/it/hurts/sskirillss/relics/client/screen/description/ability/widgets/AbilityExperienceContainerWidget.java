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
import java.util.Locale;

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

        var dot = ".";
        var dotWidth = Math.max(1, font.width(dot));

        var ability = screen.getSelectedAbility();
        var relicData = relic.getRelicData(player, stack);
        var levelingData = relicData.getLevelingData();
        var abilityData = relicData.getAbilitiesData().getAbilityData(ability);
        var template = abilityData.getTemplate();

        if (template == null)
            return new ArrayList<>();

        var group = new LinkedHashMap<String, List<List<FormattedCharSequence>>>();
        var conditions = new HashMap<String, MutableComponent>();

        for (var source : template.getExperienceSources().getSources().values()) {
            var state = source.getVisibilityState().apply(player, stack, ability, source.getId());

            if (state == VisibilityState.HIDDEN)
                continue;

            var condition = source.getConditionComponent()
                    .apply(player, stack, ability, source.getId())
                    .withStyle(ChatFormatting.BOLD);

            var key = condition.getString().trim();

            var description = Component.literal("● ")
                    .append(source.getDescriptionComponent().apply(player, stack, ability, source.getId()));

            var descriptionLines = new ArrayList<FormattedCharSequence>();

            if (state == VisibilityState.OBFUSCATED) {
                descriptionLines.addAll(font.split(ScreenUtils.randomizeAllCharacters(description, this.hashCode())
                        .withStyle(Style.EMPTY
                                .withFont(ScreenUtils.ILLAGER_ALT_FONT)
                                .withColor(DescriptionUtils.NEGATIVE_COLOR(true))), maxWidth));
            } else {
                var totalSourceExperience = levelingData.getTotalSourceExperience();
                var sourceExperience = levelingData.getSourceExperience(ability, source.getId());
                var percentage = totalSourceExperience <= 0D ? 0D : sourceExperience / totalSourceExperience * 100D;
                var experience = String.format(Locale.ROOT, "%.1f", sourceExperience);
                var percentageText = String.format(Locale.ROOT, "%.1f", percentage);
                var suffix = Component.literal(" ")
                        .append(Component.literal((experience.endsWith(".0") ? experience.replace(".0", "") : experience)
                                        + " [" + (percentageText.endsWith(".0") ? percentageText.replace(".0", "") : percentageText) + "%]")
                                .withStyle(ChatFormatting.BOLD));

                var suffixWidth = font.width(suffix);
                var limit = Math.max(0, maxWidth - suffixWidth);
                var lines = font.split(description.append(Component.literal(" ")), limit);

                for (var i = 0; i < lines.size(); i++) {
                    var line = lines.get(i);

                    if (i < lines.size() - 1) {
                        descriptionLines.add(line);
                    } else {
                        var avail = Math.max(0, maxWidth - font.width(line) - suffixWidth);
                        var dotsCount = Math.max(0, avail / dotWidth);
                        var dots = dotsCount > 0
                                ? FormattedCharSequence.forward(dot.repeat(dotsCount), Style.EMPTY)
                                : FormattedCharSequence.EMPTY;

                        var seq = FormattedCharSequence.composite(line, dots, suffix.getVisualOrderText());

                        while (font.width(seq) > maxWidth && dotsCount > 0) {
                            dotsCount--;
                            dots = dotsCount > 0
                                    ? FormattedCharSequence.forward(dot.repeat(dotsCount), Style.EMPTY)
                                    : FormattedCharSequence.EMPTY;
                            seq = FormattedCharSequence.composite(line, dots, suffix.getVisualOrderText());
                        }

                        descriptionLines.add(seq);
                    }
                }
            }

            group.computeIfAbsent(key, k -> new ArrayList<>()).add(descriptionLines);

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

            for (var descriptionLines : entry.getValue())
                sequences.addAll(descriptionLines);
        }

        return sequences;
    }
}
