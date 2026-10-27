package it.hurts.sskirillss.relics.client.screen.utils.misc;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public class FloatingTexts {
    public static class Entry {
        private String text;
        private int color;
        private float x;
        private float y;
        private float direction;
        private int age = 0;
        private int life = 0;
        private float pop = 0F;
        private float popO = 0F;
        private boolean hold = false;
    }

    private static final int DURATION = 22;
    private static final int MAX_ENTRIES = 8;
    private static final int HOLD_TIMEOUT = 200;
    private static final int MIN_ALPHA = 5;

    private static final float RISE = 18F;
    private static final float SCALE = 0.75F;
    private static final float POP_SCALE = 0.3F;
    private static final float POP_DECAY = 0.7F;
    private static final float Z_LEVEL = 300F;

    private final List<Entry> entries = new ArrayList<>();

    public Entry add(String text, int color, float x, float y, float direction) {
        var entry = new Entry();

        entry.text = text;
        entry.color = color;
        entry.x = x;
        entry.y = y;
        entry.direction = direction;

        entries.add(entry);

        if (entries.size() > MAX_ENTRIES)
            entries.remove(0);

        return entry;
    }

    public Entry counter(Entry counter, String text, int color, float x, float y, float direction) {
        if (counter == null || !entries.contains(counter)) {
            counter = add(text, color, x, y, direction);

            counter.hold = true;
        }

        counter.text = text;
        counter.color = color;
        counter.pop = 1F;
        counter.age = 0;

        return counter;
    }

    public void release(Entry counter) {
        if (counter != null)
            counter.hold = false;
    }

    public void tick() {
        for (var entry : entries) {
            entry.life++;
            entry.popO = entry.pop;
            entry.pop *= POP_DECAY;

            if (entry.hold && entry.life > HOLD_TIMEOUT)
                entry.hold = false;

            if (!entry.hold)
                entry.age++;
        }

        entries.removeIf(entry -> entry.age >= DURATION);
    }

    public void render(GuiGraphics guiGraphics, Font font, float partialTick) {
        var poseStack = guiGraphics.pose();

        for (var entry : entries) {
            var progress = entry.hold ? 0F : Math.min((entry.age + partialTick) / DURATION, 1F);
            var alpha = Math.max((int) (255 * (1F - progress * progress)), MIN_ALPHA);
            var scale = SCALE * (1F + Mth.lerp(partialTick, entry.popO, entry.pop) * POP_SCALE);

            var component = Component.literal(entry.text).withStyle(ChatFormatting.BOLD);

            poseStack.pushPose();

            poseStack.translate(entry.x, entry.y + font.lineHeight * SCALE / 2F + entry.direction * progress * RISE, Z_LEVEL);
            poseStack.scale(scale, scale, 1F);

            guiGraphics.drawString(font, component.getVisualOrderText(), -font.width(component) / 2F, -font.lineHeight / 2F, (alpha << 24) | (entry.color & 0xFFFFFF), true);

            poseStack.popPose();
        }
    }
}
