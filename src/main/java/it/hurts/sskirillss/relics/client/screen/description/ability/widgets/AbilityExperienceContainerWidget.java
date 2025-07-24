package it.hurts.sskirillss.relics.client.screen.description.ability.widgets;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.description.ability.AbilityDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.StatisticContainerWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.client.screen.utils.ScreenUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

public class AbilityExperienceContainerWidget extends StatisticContainerWidget {
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

        for (var source : relic.getExperienceSourcesTemplate(player, stack, ability).getSources().values()) {
            var condition = source.getConditionComponent().apply(player, stack, ability, source.getId()).withStyle(ChatFormatting.BOLD);
            var description = Component.literal("● ").append(source.getDescriptionComponent().apply(player, stack, ability, source.getId()));

            if (!source.getCondition().test(player, stack, screen.getSelectedAbility()))
                description = ScreenUtils.randomizeAllCharacters(description, this.hashCode()).withStyle(Style.EMPTY.withFont(ScreenUtils.ILLAGER_ALT_FONT).withColor(DescriptionUtils.CUSTOM_COLOR(0x851b1b)));

            var content = new ArrayList<MutableComponent>();

            if (!sequences.isEmpty())
                content.add(Component.literal(" "));

            if (!condition.equals(Component.empty()))
                content.add(condition);

            content.add(description);

            for (var entry : content)
                sequences.addAll(font.split(entry, maxWidth));
        }

        return sequences;
    }
}