package it.hurts.sskirillss.relics.client.screen.description.general.widgets;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractPlateWidget;
import it.hurts.sskirillss.relics.utils.RenderUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class LevelingPointsPlateWidget extends AbstractPlateWidget {
    private static final int HIDDEN_TIMEOUT = 120;
    private static final int MIN_DURATION = 6;
    private static final int MAX_DURATION = 40;
    private static final float DURATION_PER_POINT = 1.5F;

    private boolean initialized = false;
    private boolean animating = false;
    private int actual = 0;
    private int target = 0;
    private float from = 0F;
    private int ticks = 0;
    private int duration = 1;
    private int settle = 0;
    private int hidden = 0;
    private int hiddenTicks = 0;

    public LevelingPointsPlateWidget(int x, int y, DescriptionScreen screen) {
        super(x, y, screen, "leveling_point");
    }

    public void hide(int amount) {
        hidden += amount;
        hiddenTicks = HIDDEN_TIMEOUT;
    }

    public void reveal(int amount) {
        hidden = Math.max(0, hidden - amount);
    }

    @Override
    public void onTick() {
        super.onTick();

        var stack = getScreen().getStack();

        if (minecraft.player == null || !(stack.getItem() instanceof IRelicItem relic))
            return;

        var points = relic.getRelicData(minecraft.player, stack).getLevelingData().getPoints();

        if (!initialized) {
            initialized = true;
            actual = points;
            target = points;

            return;
        }

        if (points != actual) {
            if (points < actual)
                hidden = 0;

            actual = points;
            settle = 1;
        }

        if (hidden > 0 && --hiddenTicks <= 0)
            hidden = 0;

        if (settle > 0)
            settle--;
        else {
            var desired = Math.max(0, actual - hidden);

            if (desired != target) {
                from = displayedPoints(0F);
                target = desired;
                ticks = 0;
                duration = Mth.clamp(Math.round(Math.abs(desired - from) * DURATION_PER_POINT), MIN_DURATION, MAX_DURATION);
                animating = true;
            }
        }

        if (animating && ++ticks >= duration)
            animating = false;
    }

    private int displayedPoints(float partialTick) {
        if (!animating)
            return target;

        var t = Mth.clamp((ticks + partialTick) / duration, 0F, 1F);

        return Math.round(Mth.lerp(1F - (1F - t) * (1F - t), from, target));
    }

    @Override
    public List<MutableComponent> getHoverTooltip() {
        var entries = new ArrayList<MutableComponent>();

        var stack = this.getScreen().getStack();

        if (!(stack.getItem() instanceof IRelicItem relic))
            return entries;

        entries.add(Component.literal("").append(Component.translatable("relics.description.researching.general.leveling_point.title").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.UNDERLINE)).append(" " + relic.getRelicData(minecraft.player, stack).getLevelingData().getPoints()));

        entries.add(Component.literal(" "));

        if (Screen.hasShiftDown())
            entries.add(Component.translatable("relics.description.researching.general.leveling_point.extra_info").withStyle(ChatFormatting.ITALIC));
        else
            entries.add(Component.translatable("relics.description.researching.general.extra_info"));

        return entries;
    }

    @Override
    public String getValue(ItemStack stack) {
        if (!(stack.getItem() instanceof IRelicItem relic))
            return "";

        return String.valueOf(initialized ? displayedPoints(RenderUtils.getPartialTick(false)) : relic.getRelicData(minecraft.player, stack).getLevelingData().getPoints());
    }
}