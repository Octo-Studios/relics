package it.hurts.sskirillss.relics.client.screen.description.general.widgets;

import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractPlateWidget;
import it.hurts.sskirillss.relics.client.screen.description.relic.RelicDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.utils.misc.FloatingTexts;
import it.hurts.sskirillss.relics.client.screen.utils.misc.RerollAnimation;
import it.hurts.sskirillss.relics.client.screen.utils.misc.UIParticles;
import it.hurts.sskirillss.relics.handlers.RankHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class RankPointsPlateWidget extends AbstractPlateWidget {
    private final FloatingTexts floatingTexts = new FloatingTexts();
    private long lastPoints;

    public RankPointsPlateWidget(int x, int y, DescriptionScreen screen) {
        super(x, y, screen, "rank_point");

        lastPoints = getPoints(screen.getStack());
    }

    @Override
    public void onTick() {
        super.onTick();

        floatingTexts.tick();

        var points = getPoints(getScreen().getStack());
        var delta = points - lastPoints;

        if (delta != 0) {
            pulse(0.6F);

            floatingTexts.add((delta > 0 ? "+" : "") + delta, delta > 0 ? UIParticles.GOLD : RerollAnimation.TINT_DOWN, 10F, 8F, -1F);
            lastPoints = points;
        }
    }

    @Override
    public void renderContent(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        floatingTexts.render(graphics, minecraft.font, partialTick);
    }

    @Override
    public List<MutableComponent> getHoverTooltip() {
        return List.of(
                Component.translatable("relics.description.researching.general.rank_point.title").withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE)
                        .append(" " + getValue(getScreen().getStack())),
                Component.literal(" "),
                Screen.hasShiftDown() ? Component.translatable("relics.description.researching.general.rank_point.extra_info").withStyle(ChatFormatting.ITALIC)
                        : Component.translatable("relics.description.researching.general.extra_info")
        );
    }

    @Override
    public String getValue(ItemStack stack) {
        return String.valueOf(getPoints(stack));
    }

    private long getPoints(ItemStack stack) {
        var points = RankHandler.getRankPoints(minecraft.player, stack.getItem());

        return getScreen() instanceof RelicDescriptionScreen screen ? screen.getDismantleEffect().displayedPoints(points) : points;
    }
}
