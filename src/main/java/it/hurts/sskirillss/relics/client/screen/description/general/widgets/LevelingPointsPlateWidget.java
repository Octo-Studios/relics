package it.hurts.sskirillss.relics.client.screen.description.general.widgets;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractPlateWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class LevelingPointsPlateWidget extends AbstractPlateWidget {
    public LevelingPointsPlateWidget(int x, int y, DescriptionScreen screen) {
        super(x, y, screen, "leveling_point");
    }

    @Override
    public List<MutableComponent> getHoverTooltip() {
        var entries = new ArrayList<MutableComponent>();

        var stack = this.getScreen().getStack();

        if (!(stack.getItem() instanceof IRelicItem relic))
            return entries;

        entries.add(Component.literal("").append(Component.translatable("relics.description.researching.general.leveling_point.title").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.UNDERLINE)).append(" " + relic.getRelicLevelingPoints(minecraft.player, stack)));

        entries.add(Component.literal(" "));

        if (Screen.hasShiftDown())
            entries.add(Component.translatable("relics.description.researching.general.leveling_point.extra_info").withStyle(ChatFormatting.ITALIC));
        else
            entries.add(Component.translatable("relics.description.researching.general.extra_info"));

        return entries;
    }

    @Override
    public String getValue(ItemStack stack) {
        return stack.getItem() instanceof IRelicItem relic ? String.valueOf(relic.getRelicLevelingPoints(minecraft.player, stack)) : "";
    }
}