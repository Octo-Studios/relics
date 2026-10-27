package it.hurts.sskirillss.relics.client.screen.description.general.widgets;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractPlateWidget;
import it.hurts.sskirillss.relics.client.screen.utils.misc.AnimatedValue;
import it.hurts.sskirillss.relics.client.screen.utils.misc.DeltaCounter;
import it.hurts.sskirillss.relics.client.screen.utils.misc.FloatingTexts;
import it.hurts.sskirillss.relics.client.screen.utils.misc.RerollAnimation;
import it.hurts.sskirillss.relics.client.screen.utils.misc.UIParticles;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
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
    private static final float COUNTER_X = 10F;
    private static final float COUNTER_Y = 8F;
    private static final float SPEND_PULSE = 0.6F;

    private final FloatingTexts floatingTexts = new FloatingTexts();
    private final DeltaCounter counter = new DeltaCounter(floatingTexts);
    private final AnimatedValue points = new AnimatedValue();

    private int actual = 0;
    private int target = 0;
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
    public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var poseStack = guiGraphics.pose();

        var scale = getPulseScale(partialTick);

        poseStack.pushPose();

        poseStack.translate(width / 2F, height / 2F, 0F);
        poseStack.scale(1F / scale, 1F / scale, 1F);
        poseStack.translate(-width / 2F - getWobbleX(partialTick), -height / 2F - getWobbleY(partialTick), 0D);

        floatingTexts.render(guiGraphics, minecraft.font, partialTick);

        poseStack.popPose();
    }

    @Override
    public void onTick() {
        super.onTick();

        floatingTexts.tick();

        var stack = getScreen().getStack();

        if (minecraft.player == null || !(stack.getItem() instanceof IRelicItem relic))
            return;

        var current = relic.getRelicData(minecraft.player, stack).getLevelingData().getPoints();

        if (!points.isInitialized()) {
            actual = current;
            target = current;

            points.snap(current);

            return;
        }

        if (current != actual) {
            if (current < actual)
                hidden = 0;

            actual = current;
            settle = 1;
        }

        if (hidden > 0 && --hiddenTicks <= 0)
            hidden = 0;

        if (settle > 0)
            settle--;
        else {
            var desired = Math.max(0, actual - hidden);

            if (desired != target) {
                if (desired < target)
                    pulse(SPEND_PULSE);

                target = desired;

                points.set(desired, Mth.clamp(Math.round(Math.abs(desired - points.getValue()) * DURATION_PER_POINT), MIN_DURATION, MAX_DURATION));
            }
        }

        counter.update(points.getInt(target), points.isMoving(), UIParticles.GOLD, RerollAnimation.TINT_DOWN, COUNTER_X, COUNTER_Y);
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

        return String.valueOf(points.getInt(relic.getRelicData(minecraft.player, stack).getLevelingData().getPoints()));
    }
}
