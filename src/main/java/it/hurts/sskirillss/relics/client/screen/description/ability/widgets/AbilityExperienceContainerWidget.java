package it.hurts.sskirillss.relics.client.screen.description.ability.widgets;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.description.ability.AbilityDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.StatisticContainerWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.client.screen.utils.ScreenUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
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

        for (var source : relic.getExperienceSourcesTemplate(player, stack, screen.getSelectedAbility()).getSources().values()) {
            var description = Component.literal("● ").append(Component.translatable("tooltip.relics." + BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath() + ".ability." + screen.getSelectedAbility() + ".experience_source." + source.getId()));

            if (!source.getCondition().test(player, stack, screen.getSelectedAbility()))
                description = ScreenUtils.randomizeAllCharacters(description, this.hashCode()).withStyle(Style.EMPTY.withFont(ScreenUtils.ILLAGER_ALT_FONT).withColor(DescriptionUtils.CUSTOM_COLOR(0x851b1b)));

            sequences.addAll(font.split(description, maxWidth));

            sequences.addAll(font.split(Component.literal(" "), maxWidth));
        }

        return sequences;
    }
}