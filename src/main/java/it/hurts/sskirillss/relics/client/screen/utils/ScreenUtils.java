package it.hurts.sskirillss.relics.client.screen.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.RandomSource;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ScreenUtils {
    public static void drawTexturedTooltipBorder(GuiGraphics guiGraphics, ResourceLocation texture, int width, int height, int x, int y) {
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, texture);

        Minecraft.getInstance().getTextureManager().bindForSetup(texture);

        int texSize = 19;
        int cornerSize = 9;

        guiGraphics.blit(texture, x, y, cornerSize, cornerSize, 0, 0, cornerSize, cornerSize, texSize, texSize);
        guiGraphics.blit(texture, x + cornerSize, y, width, cornerSize, cornerSize, 0, 1, cornerSize, texSize, texSize);
        guiGraphics.blit(texture, x + cornerSize + width, y, cornerSize, cornerSize, cornerSize + 1, 0, cornerSize, cornerSize, texSize, texSize);
        guiGraphics.blit(texture, x, y + cornerSize, cornerSize, height, 0, cornerSize, cornerSize, 1, texSize, texSize);
        guiGraphics.blit(texture, x + width + cornerSize, y + cornerSize, cornerSize, height, cornerSize + 1, cornerSize, cornerSize, 1, texSize, texSize);
        guiGraphics.blit(texture, x + cornerSize, y + cornerSize, width, height, cornerSize + 1, cornerSize + 1, 1, 1, texSize, texSize);
        guiGraphics.blit(texture, x, y + height + cornerSize, cornerSize, cornerSize, 0, cornerSize + 1, cornerSize, cornerSize, texSize, texSize);
        guiGraphics.blit(texture, x + cornerSize, y + height + cornerSize, width, cornerSize, cornerSize, cornerSize + 1, 1, cornerSize, texSize, texSize);
        guiGraphics.blit(texture, x + cornerSize + width, y + cornerSize + height, cornerSize, cornerSize, cornerSize + 1, cornerSize + 1, cornerSize, cornerSize, texSize, texSize);
    }

    public static void drawCenteredString(GuiGraphics guiGraphics, Font font, Component text, int x, int y, int color, boolean dropShadow) {
        FormattedCharSequence sequence = text.getVisualOrderText();

        guiGraphics.drawString(font, sequence, x - font.width(sequence) / 2, y, color, dropShadow);
    }

    public static void drawCenteredString(GuiGraphics guiGraphics, Font font, Component text, float x, float y, int color, boolean dropShadow) {
        FormattedCharSequence sequence = text.getVisualOrderText();

        guiGraphics.drawString(font, sequence, x - font.width(sequence) / 2F, y, color, dropShadow);
    }

    public static void drawCenteredString(GuiGraphics guiGraphics, Font font, String text, int x, int y, int color, boolean dropShadow) {
        guiGraphics.drawString(font, text, x - font.width(text) / 2, y, color, dropShadow);
    }

    public static void drawCenteredString(GuiGraphics guiGraphics, Font font, FormattedCharSequence text, int x, int y, int color, boolean dropShadow) {
        guiGraphics.drawString(font, text, x - font.width(text) / 2, y, color, dropShadow);
    }

    public static boolean isHovered(int x, int y, int width, int height, int mouseX, int mouseY) {
        return (mouseX >= x && mouseX <= x + width) && (mouseY >= y && mouseY <= y + height);
    }

    public static final ResourceLocation ALT_FONT = ResourceLocation.withDefaultNamespace("alt");
    public static final ResourceLocation ILLAGER_ALT_FONT = ResourceLocation.withDefaultNamespace("illageralt");

    public static MutableComponent illageriate(MutableComponent input, double percentage, long seed) {
        return stylizeWidthReplacement(input, percentage, Style.EMPTY.withFont(ILLAGER_ALT_FONT), seed);
    }

    public static MutableComponent galactizate(MutableComponent input, double percentage, long seed) {
        return stylizeWidthReplacement(input, percentage, Style.EMPTY.withFont(ALT_FONT), seed);
    }

    public static MutableComponent obfuscate(MutableComponent input, double percentage, long seed) {
        return stylize(input, percentage, Style.EMPTY.withObfuscated(true), seed);
    }

    public static MutableComponent stylizeWidthReplacement(MutableComponent input, double percentage, Style style, long seed) {
        RandomSource random = RandomSource.create(seed);

        String text = input.getString();

        List<Integer> letterIndices = IntStream.range(0, text.length())
                .filter(i -> !Character.isSpaceChar(text.charAt(i)))
                .boxed().collect(Collectors.toList());

        Collections.shuffle(letterIndices, new Random(seed));

        Set<Integer> indicesToReplace = new HashSet<>(letterIndices.subList(0, Math.min((int) Math.round(letterIndices.size() * percentage), letterIndices.size())));

        List<Character> englishLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".chars()
                .mapToObj(c -> (char) c)
                .toList();

        return IntStream.range(0, text.length()).mapToObj(i -> {
            if (indicesToReplace.contains(i))
                return Component.literal(String.valueOf(englishLetters.get(random.nextInt(englishLetters.size())))).setStyle(style);

            return Component.literal(String.valueOf(text.charAt(i))).setStyle(input.getStyle());
        }).collect(Component::empty, MutableComponent::append, MutableComponent::append);
    }

    public static MutableComponent stylize(MutableComponent input, double percentage, Style style, long seed) {
        RandomSource random = RandomSource.create(seed);

        String text = input.getString();
        int length = text.length();

        var indices = IntStream.generate(() -> random.nextInt(length))
                .distinct()
                .limit((int) (length * percentage))
                .boxed()
                .collect(Collectors.toSet());

        return IntStream.range(0, length)
                .mapToObj(index -> {
                    MutableComponent component = Component.literal(String.valueOf(text.charAt(index))).setStyle(input.getStyle());

                    if (indices.contains(index))
                        component.setStyle(style.applyTo(component.getStyle()));

                    return component;
                })
                .collect(Component::empty, MutableComponent::append, MutableComponent::append);
    }
}