package it.hurts.sskirillss.relics.client.screen.description.relic.widgets;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.description.ability.AbilityDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.ability.widgets.RerollAbilityActionWidget;
import it.hurts.sskirillss.relics.client.screen.description.ability.widgets.ResetAbilityActionWidget;
import it.hurts.sskirillss.relics.client.screen.description.ability.widgets.UpgradeAbilityActionWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.client.screen.description.relic.RelicDescriptionScreen;
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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AbilityDescriptionContainerWidget extends DescriptionContainerWidget {
    public AbilityDescriptionContainerWidget(int x, int y, AbilityDescriptionScreen screen) {
        super(x, y, screen);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var player = this.minecraft.player;
        var screen = ((AbilityDescriptionScreen) this.getScreen());
        var stack = screen.getStack();

        var ability = screen.getSelectedAbility();

        if (stack == null || !(stack.getItem() instanceof IRelicItem relic) || player == null)
            return;

        int abilityLevel = relic.getAbilityLevel(player, stack, ability);

        var poseStack = guiGraphics.pose();

        GUIScissors.begin(getX(), getY(), getWidth(), getHeight());

        poseStack.pushPose();

        poseStack.scale(0.5F, 0.5F, 0.5F);

        List<MutableComponent> components = new ArrayList<>();

        var hoveredUpgrade = screen.getUpgradeButton() != null && screen.getUpgradeButton().isHovered();
        var hoveredReroll = screen.getRerollButton() != null && screen.getRerollButton().isHovered();
        var hoveredReset = screen.getResetButton() != null && screen.getResetButton().isHovered();

        var wantsUpgrade = hoveredUpgrade && relic.mayUpgrade(player, stack, ability);
        var wantsReroll = hoveredReroll && relic.mayReroll(player, stack, ability);
        var wantsReset = hoveredReset && relic.mayReset(player, stack, ability);

        int color = DescriptionUtils.TEXT_COLOR;

        for (var stat : relic.getAbilityTemplate(player, stack, ability).getStats().values()) {
            if (wantsUpgrade)
                color = 0x228B22;

            if (wantsReroll)
                color = 0xFF8C00;

            if (wantsReset)
                color = 0xB22222;

            if (color != DescriptionUtils.TEXT_COLOR) {
                var brightness = (float) (0.75F + 0.1F * Math.sin(2 * Math.PI * 0.75F * player.tickCount / 20F));

                var hsb = Color.RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, null);

                hsb[2] = Mth.clamp(brightness, 0F, 1F);

                color = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
            }

            var resultComponent = Component.literal(String.valueOf(stat.getFormatValue().apply(relic.getStatValueForLevel(player, stack, ability, stat.getId(), wantsUpgrade ? abilityLevel + 1 : wantsReset ? 0 : abilityLevel))));

            components.add(resultComponent.withStyle(ChatFormatting.BOLD).withColor(color));
        }

        var lineOffset = 10;
        var lines = RelicDescriptionScreen.justifyStyledText(Component.translatable("tooltip.relics." + BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath() + ".ability." + ability + ".description", components.toArray()), 320, minecraft.font);

        var scroll = getScrollbar();

        if (scroll != null) {
            var offset = scroll.getScrollPosition(partialTick);

            poseStack.translate(0, -(offset * ((lines.size() - DescriptionContainerWidget.MAX_LINES) * (lineOffset))), 0);
        }

        renderJustifiedDescriptionWithStatBoxes(guiGraphics, (this.getX() + 7) * 2, (this.getY() * 2), 320, minecraft.font, player, stack, relic, ability, abilityLevel, screen.getUpgradeButton(), screen.getRerollButton(), screen.getResetButton());

        poseStack.popPose();

        GUIScissors.end();
    }

    private void renderJustifiedDescriptionWithStatBoxes(GuiGraphics guiGraphics, int x, int y, int maxWidth, Font font, Player player, ItemStack stack, IRelicItem relic, String ability, int level, UpgradeAbilityActionWidget upgradeButton, RerollAbilityActionWidget rerollButton, ResetAbilityActionWidget resetButton) {
        List<MutableComponent> dynamicComponents = new ArrayList<>();
        for (var stat : relic.getAbilityTemplate(player, stack, ability).getStats().values()) {
            boolean wantsUpgrade = upgradeButton != null && upgradeButton.isHovered() && relic.mayUpgrade(player, stack, ability);
            boolean wantsReroll = rerollButton != null && rerollButton.isHovered() && relic.mayReroll(player, stack, ability);
            boolean wantsReset = resetButton != null && resetButton.isHovered() && relic.mayReset(player, stack, ability);

            int color = DescriptionUtils.TEXT_COLOR;
            if (wantsUpgrade) color = 0x228B22;
            if (wantsReroll) color = 0xFF8C00;
            if (wantsReset) color = 0xB22222;
            if (color != DescriptionUtils.TEXT_COLOR) {
                float brightness = 0.75F + 0.1F * (float) Math.sin(2 * Math.PI * 0.75F * player.tickCount / 20F);
                var hsb = Color.RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, null);
                hsb[2] = Mth.clamp(brightness, 0F, 1F);
                color = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
            }
            double rawValue = relic.getStatValueForLevel(player, stack, ability, stat.getId(),
                    wantsUpgrade ? level + 1 : wantsReset ? 0 : level);
            String valStr = String.valueOf(stat.getFormatValue().apply(rawValue));
            if (valStr.endsWith(".0")) valStr = valStr.replace(".0", "");
            dynamicComponents.add(Component.literal(valStr).withStyle(ChatFormatting.BOLD).withColor(color));
        }

        List<String> placeholders = IntStream.rangeClosed(1, dynamicComponents.size())
                .mapToObj(i -> "%" + i + "$s")
                .collect(Collectors.toList());

        String rawDescription = Component.translatable(
                "tooltip.relics." + BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath() +
                        ".ability." + ability + ".description",
                placeholders.toArray()
        ).getString().replace("%%", "%");

        Pattern placeholderPattern = Pattern.compile("([^\\s]*%(\\d+)\\$s[^\\s]*)");
        List<Word> words = new ArrayList<>();
        Matcher m = placeholderPattern.matcher(rawDescription);
        int lastEnd = 0;
        while (m.find()) {
            if (m.start() > lastEnd) {
                for (String part : rawDescription.substring(lastEnd, m.start()).split(" ")) {
                    if (!part.isEmpty()) words.add(new Word(part, false, -1));
                }
            }
            String dynamicSegment = m.group(1);
            int idx = Integer.parseInt(m.group(2)) - 1;
            String prefix = dynamicSegment.substring(0, dynamicSegment.indexOf('%'));
            String suffix = dynamicSegment.substring(dynamicSegment.lastIndexOf('s') + 1);
            String replacementText = prefix + dynamicComponents.get(idx).getString() + suffix;
            words.add(new Word(replacementText, true, idx));
            lastEnd = m.end();
        }
        if (lastEnd < rawDescription.length()) {
            for (String part : rawDescription.substring(lastEnd).split(" ")) {
                if (!part.isEmpty()) words.add(new Word(part, false, -1));
            }
        }

        var splitter = font.getSplitter();
        int spaceWidth = (int) Math.ceil(splitter.stringWidth(FormattedText.of(" ", Style.EMPTY)));
        List<List<Word>> lines = new ArrayList<>();
        List<Word> currentLine = new ArrayList<>();
        int lineUsed = 0;

        for (Word w : words) {
            FormattedText ft = w.dynamic
                    ? FormattedText.of(w.text, dynamicComponents.get(w.index).getStyle())
                    : FormattedText.of(w.text, Style.EMPTY);
            int textWidth = (int) Math.ceil(splitter.stringWidth(ft));
            int wWidth = textWidth + (w.dynamic ? 4 : 0);
            if (!currentLine.isEmpty() && lineUsed + wWidth + spaceWidth > maxWidth) {
                lines.add(new ArrayList<>(currentLine));
                currentLine.clear();
                lineUsed = 0;
            }
            currentLine.add(w);
            lineUsed += wWidth + spaceWidth;
        }
        if (!currentLine.isEmpty()) lines.add(currentLine);

        int yOff = 0;
        for (List<Word> line : lines) {
            int wordsWidth = 0;
            for (Word w : line) {
                FormattedText ft = w.dynamic
                        ? FormattedText.of(w.text, dynamicComponents.get(w.index).getStyle())
                        : FormattedText.of(w.text, Style.EMPTY);
                int textWidth = (int) Math.ceil(splitter.stringWidth(ft));
                wordsWidth += textWidth + (w.dynamic ? 4 : 0);
            }
            int gaps = Math.max(1, line.size() - 1);
            int totalSpace = maxWidth - wordsWidth;
            int base = totalSpace / gaps;
            int extra = totalSpace % gaps;

            int currX = x;
            int currY = y + yOff;

            for (int i = 0; i < line.size(); i++) {
                Word w = line.get(i);
                FormattedText ft = w.dynamic
                        ? FormattedText.of(w.text, dynamicComponents.get(w.index).getStyle())
                        : FormattedText.of(w.text, Style.EMPTY);
                int textWidth = (int) Math.ceil(splitter.stringWidth(ft));
                if (w.dynamic) {
                    int tw = textWidth;
                    int fh = font.lineHeight;
                    int col = dynamicComponents.get(w.index).getStyle().getColor().getValue();
                    int frameStartX = currX - 1;
                    int frameStartY = currY - 1;
                    int frameEndX = currX + tw + 4;
                    int frameEndY = currY + fh + 1;
                    int borderColor = 0xFF000000 | col;
                    guiGraphics.fill(frameStartX, frameStartY, frameEndX, frameStartY + 1, borderColor);
                    guiGraphics.fill(frameStartX, frameEndY - 1, frameEndX, frameEndY, borderColor);
                    guiGraphics.fill(frameStartX, frameStartY, frameStartX + 1, frameEndY, borderColor);
                    guiGraphics.fill(frameEndX - 1, frameStartY, frameEndX, frameEndY, borderColor);
                    guiGraphics.drawString(font, Language.getInstance().getVisualOrder(ft), currX + 2, currY + 1, dynamicComponents.get(w.index).getStyle().getColor().getValue(), false);
                    currX += tw + 5;
                } else {
                    guiGraphics.drawString(font, Language.getInstance().getVisualOrder(ft), currX, currY, DescriptionUtils.TEXT_COLOR, false);
                    currX += textWidth;
                }
                if (i < line.size() - 1) {
                    currX += base + (i < extra ? 1 : 0);
                }
            }
            yOff += font.lineHeight + 1;
        }
    }

    private static class Word {
        final String text;
        final boolean dynamic;
        final int index;

        Word(String t, boolean dyn, int idx) {
            text = t;
            dynamic = dyn;
            index = idx;
        }
    }

    @Override
    public int getContentHeight() {
        return (int) (RelicDescriptionScreen.justifyStyledText(Component.translatable("tooltip.relics." + BuiltInRegistries.ITEM.getKey(this.getScreen().getStack().getItem()).getPath() + ".description"), 320, minecraft.font).size() * minecraft.font.lineHeight / 2F);
    }
}