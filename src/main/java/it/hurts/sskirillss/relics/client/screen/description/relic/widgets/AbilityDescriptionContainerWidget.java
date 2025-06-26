package it.hurts.sskirillss.relics.client.screen.description.relic.widgets;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.description.ability.AbilityDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.client.screen.utils.ScreenUtils;
import it.hurts.sskirillss.relics.utils.data.GUIScissors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class AbilityDescriptionContainerWidget extends DescriptionContainerWidget {
    private static final int VERTICAL_PADDING = 1;

    public AbilityDescriptionContainerWidget(int x, int y, AbilityDescriptionScreen screen) {
        super(x, y, screen);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var player = this.minecraft.player;
        var screen = ((AbilityDescriptionScreen) this.getScreen());
        var stack = screen.getStack();

        if (stack == null || !(stack.getItem() instanceof IRelicItem) || player == null)
            return;

        var poseStack = guiGraphics.pose();

        GUIScissors.begin(this.getX(), this.getY(), this.getWidth(), this.getHeight());

        poseStack.pushPose();

        poseStack.scale(0.5F, 0.5F, 0.5F);

        var data = this.constructDescriptionData();
        var layout = this.layoutJustifiedLines(this.minecraft.font, data.rawLines(), data.dynamicComponents(), 320);

        var scroll = this.getScrollbar();

        if (scroll != null) {
            var rawLineHeight = this.minecraft.font.lineHeight;
            var rawSpacing = 1;
            var localLineOffset = rawLineHeight + rawSpacing;

            var contentHeightLocal = layout.size() * localLineOffset + 2 * VERTICAL_PADDING;

            var windowHeightLocal = DescriptionContainerWidget.MAX_LINES * localLineOffset;

            var scrollRangeLocal = contentHeightLocal - windowHeightLocal;

            var offset = scroll.getScrollPosition(partialTick);
            var shiftY = offset * scrollRangeLocal;

            poseStack.translate(0, -(shiftY - VERTICAL_PADDING), 0);
        }

        this.renderJustifiedDescriptionWithStatBoxes(guiGraphics, (this.getX() + 7) * 2, (this.getY() * 2), 320, this.minecraft.font, layout);

        poseStack.popPose();

        GUIScissors.end();
    }

    private static class Word {
        final MutableComponent component;
        final boolean isDynamic;

        Word(MutableComponent component, boolean isDynamic) {
            this.component = component;
            this.isDynamic = isDynamic;
        }
    }

    private record LineEntry(Component rawLine, boolean justify) {
    }

    public record LayoutLine(List<Word> words, boolean justify) {
    }

    public record DescriptionData(List<LineEntry> rawLines, List<MutableComponent> dynamicComponents) {
    }

    public DescriptionData constructDescriptionData() {
        var player = minecraft.player;
        var screen = (AbilityDescriptionScreen) getScreen();
        var stack = screen.getStack();
        var abilityKey = screen.getSelectedAbility();

        if (player == null || stack == null || !(stack.getItem() instanceof IRelicItem relic))
            return new DescriptionData(List.of(), List.of());

        var template = relic.getAbilityTemplate(player, stack, abilityKey);
        var level = relic.getAbilityLevel(player, stack, abilityKey);

        var wantsUpgrade = screen.getUpgradeButton() != null && screen.getUpgradeButton().isHovered() && relic.mayUpgrade(player, stack, abilityKey);
        var wantsReroll = screen.getRerollButton() != null && screen.getRerollButton().isHovered() && relic.mayReroll(player, stack, abilityKey);
        var wantsReset = screen.getResetButton() != null && screen.getResetButton().isHovered() && relic.mayReset(player, stack, abilityKey);

        var dynamicComponents = template.getStats().values().stream().map(stat -> {
            var rawValue = relic.getStatValueForLevel(player, stack, abilityKey, stat.getId(), wantsUpgrade ? level + 1 : wantsReset ? 0 : level);
            var txt = stat.getFormatValue().apply(rawValue).toString();

            if (txt.endsWith(".0"))
                txt = txt.substring(0, txt.length() - 2);

            var result = Component.literal(txt)
                    .withStyle(ChatFormatting.BOLD)
                    .withColor(wantsUpgrade ? DescriptionUtils.POSITIVE_COLOR(true) : wantsReroll ? DescriptionUtils.NEUTRAL_COLOR(true) : wantsReset ? DescriptionUtils.NEGATIVE_COLOR(true) : DescriptionUtils.TEXT_COLOR);

            if (wantsReroll)
                result = result.withStyle(ChatFormatting.OBFUSCATED);

            return result;
        }).toList();

        var itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        var key = "tooltip.relics." + itemId + ".ability." + abilityKey + ".description";
        var tokens = IntStream.rangeClosed(1, dynamicComponents.size()).mapToObj(i -> "%" + i + "$s").toArray(String[]::new);
        var descriptionComponent = Component.translatable(key, (Object[]) tokens);

        var rawLines = new ArrayList<LineEntry>();

        rawLines.add(new LineEntry(descriptionComponent, true));

        for (var entry : template.getRankModifiers().entries()) {
            rawLines.add(new LineEntry(Component.literal(""), false));
            rawLines.add(new LineEntry(Component.translatable("tooltip.relics.description.ability.rank_modifier", entry.getKey())
                    .withStyle(ChatFormatting.BOLD), false));

            var description = Component.literal("● ").append(Component.translatable("tooltip.relics." + itemId + ".ability." + abilityKey + ".rank_modifier." + entry.getValue(), (Object[]) tokens));

            if (relic.getRelicRank(player, stack) < entry.getKey())
                description = ScreenUtils.randomizeAllCharacters(description, this.hashCode()).withStyle(Style.EMPTY.withFont(ScreenUtils.ILLAGER_ALT_FONT).withColor(DescriptionUtils.CUSTOM_COLOR(0x851b1b)));

            rawLines.add(new LineEntry(description, true));
        }

        return new DescriptionData(rawLines, dynamicComponents);
    }

    public List<LayoutLine> layoutJustifiedLines(Font font, List<LineEntry> rawLineEntries, List<MutableComponent> dynamicComponents, int maximumLineWidth) {
        var splitter = font.getSplitter();
        int spaceWidth = (int) Math.ceil(splitter.stringWidth(FormattedText.of(" ", Style.EMPTY)));
        var placeholderPattern = Pattern.compile("([()%]*%(\\d+)\\$s[()%]*)");
        var result = new ArrayList<LayoutLine>();

        for (var entry : rawLineEntries) {
            var rawText = entry.rawLine().getString();

            if (rawText.isEmpty()) {
                result.add(new LayoutLine(List.of(), false));

                continue;
            }

            var words = new ArrayList<Word>();
            var matcher = placeholderPattern.matcher(rawText);
            int lastEnd = 0;

            while (matcher.find()) {
                if (matcher.start() > lastEnd) {
                    var before = rawText.substring(lastEnd, matcher.start());

                    Arrays.stream(before.split(" "))
                            .filter(tok -> !tok.isEmpty())
                            .forEach(tok -> words.add(new Word(Component.literal(tok).withStyle(entry.rawLine().getStyle()), false)));
                }

                var segment = matcher.group(1);
                var index = Integer.parseInt(matcher.group(2)) - 1;
                var dynamicComponent = dynamicComponents.get(index).copy();

                var style = dynamicComponent.getStyle();

                var prefix = segment.substring(0, segment.indexOf('%'));
                var suffix = segment.substring(segment.lastIndexOf('s') + 1);

                if (!prefix.isEmpty())
                    dynamicComponent = Component.literal(prefix).withStyle(style).append(dynamicComponent);

                if (!suffix.isEmpty())
                    dynamicComponent.append(Component.literal(suffix).withStyle(style));

                words.add(new Word(dynamicComponent, true));

                lastEnd = matcher.end();
            }

            if (lastEnd < rawText.length()) {
                var after = rawText.substring(lastEnd);

                Arrays.stream(after.split(" "))
                        .filter(tok -> !tok.isEmpty())
                        .forEach(tok -> words.add(new Word(
                                Component.literal(tok).withStyle(entry.rawLine().getStyle()),
                                false
                        )));
            }

            var lines = new ArrayList<List<Word>>();
            var current = new ArrayList<Word>();
            int used = 0;

            for (var word : words) {
                int wordWidth = (int) Math.ceil(splitter.stringWidth(FormattedText.of(word.component.getString(), word.component.getStyle()))) + (word.isDynamic ? 4 : 0);

                if (!current.isEmpty() && used + wordWidth + spaceWidth > maximumLineWidth) {
                    lines.add(current);

                    current = new ArrayList<>();

                    used = 0;
                }

                current.add(word);

                used += wordWidth + spaceWidth;
            }

            if (!current.isEmpty())
                lines.add(current);

            for (int i = 0; i < lines.size(); i++) {
                boolean justifyLine = entry.justify() && i < lines.size() - 1;

                result.add(new LayoutLine(lines.get(i), justifyLine));
            }
        }

        return result;
    }

    public void renderJustifiedDescriptionWithStatBoxes(GuiGraphics graphics, int startX, int startY, int maximumLineWidth, Font font, List<LayoutLine> layoutLines) {
        var splitter = font.getSplitter();
        var spaceWidth = (int) Math.ceil(splitter.stringWidth(FormattedText.of(" ", Style.EMPTY)));

        int y = startY, lineHeight = font.lineHeight;

        for (var line : layoutLines) {
            var words = line.words();

            if (words.isEmpty()) {
                y += lineHeight + 1;

                continue;
            }

            boolean justify = line.justify();

            int totalW = words.stream()
                    .mapToInt(w -> (int) Math.ceil(splitter.stringWidth(w.component)) + (w.isDynamic ? 4 : 0))
                    .sum();

            int gaps = Math.max(1, words.size() - 1);
            int extra = justify ? maximumLineWidth - totalW : spaceWidth * gaps;
            int base = extra / gaps, rem = extra % gaps;

            int x = startX;

            for (int i = 0; i < words.size(); i++) {
                var word = words.get(i);
                var component = word.component;
                var textWidth = (int) Math.ceil(splitter.stringWidth(component));
                var style = component.getStyle();
                var color = style.getColor() != null ? style.getColor().getValue() : DescriptionUtils.TEXT_COLOR;

                if (word.isDynamic) {
                    graphics.fill(x - 1, y - 1, x + textWidth + 4, y, 0xFF000000 | color);
                    graphics.fill(x - 1, y + lineHeight, x + textWidth + 4, y + lineHeight + 1, 0xFF000000 | color);
                    graphics.fill(x - 1, y - 1, x, y + lineHeight + 1, 0xFF000000 | color);
                    graphics.fill(x + textWidth + 3, y - 1, x + textWidth + 4, y + lineHeight + 1, 0xFF000000 | color);

                    graphics.drawString(font, Language.getInstance().getVisualOrder(component), x + 2, y + 1, color, false);

                    x += textWidth + 5;
                } else {
                    graphics.drawString(font, Language.getInstance().getVisualOrder(component), x, y, color, false);

                    x += textWidth;
                }

                if (i < words.size() - 1)
                    x += base + (i < rem ? 1 : 0);
            }

            y += lineHeight + 1;
        }
    }

    @Override
    public int getContentHeight() {
        var data = constructDescriptionData();
        var lines = layoutJustifiedLines(minecraft.font, data.rawLines(), data.dynamicComponents(), 320);

        return (int) (lines.size() * minecraft.font.lineHeight / 2F + VERTICAL_PADDING);
    }
}