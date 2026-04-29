package it.hurts.sskirillss.relics.client.screen.description.synergy.widgets;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.RankModifierToggleWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.client.screen.description.misc.TextJustificator;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.DescriptionContainerWidget;
import it.hurts.sskirillss.relics.client.screen.description.synergy.SynergyDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.utils.ScreenUtils;
import it.hurts.sskirillss.relics.utils.RenderUtils;
import it.hurts.sskirillss.relics.utils.data.GUIScissors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class SynergyDescriptionContainerWidget extends DescriptionContainerWidget {
    private static final int VERTICAL_PADDING = 1;
    private static final String RANK_MODIFIER_CONDITION_INDENT = "    ";

    public SynergyDescriptionContainerWidget(DescriptionScreen screen) {
        super(screen);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var player = this.minecraft.player;
        var screen = ((SynergyDescriptionScreen) this.getScreen());
        var stack = screen.getStack();

        if (stack == null || !(stack.getItem() instanceof IRelicItem) || player == null)
            return;

        var poseStack = guiGraphics.pose();

        GUIScissors.begin(this.getX(), this.getY() - VERTICAL_PADDING, this.getWidth(), this.getHeight() + VERTICAL_PADDING);

        poseStack.pushPose();

        poseStack.scale(0.5F, 0.5F, 0.5F);

        var data = this.constructDescriptionData();
        var layout = TextJustificator.layoutJustifiedLines(this.minecraft.font, data.rawLines(), data.dynamicComponents(), 320);

        var scroll = this.getScrollbar();

        var shiftY = 0D;

        if (scroll != null) {
            var lineStep = this.minecraft.font.lineHeight + 1;
            var totalLines = layout.size();
            var overflowLines = Math.max(0, totalLines - MAX_LINES);

            var maxScrollPx = overflowLines * lineStep + VERTICAL_PADDING * 3;
            var offset = scroll.getScrollPosition(RenderUtils.getPartialTick(false));
            shiftY = offset * maxScrollPx;

            poseStack.translate(0, -shiftY, 0);
        }

        TextJustificator.renderJustifiedDescriptionWithStatBoxes(guiGraphics, (this.getX() + 7) * 2, (this.getY() * 2), 320, this.minecraft.font, layout);

        poseStack.popPose();

        GUIScissors.end();
    }

    public record DescriptionData(List<TextJustificator.LineEntry> rawLines, List<MutableComponent> dynamicComponents) {
    }

    public DescriptionData constructDescriptionData() {
        var player = minecraft.player;
        var screen = (SynergyDescriptionScreen) getScreen();
        var stack = screen.getStack();
        var ability = screen.getSelectedSynergy();

        if (player == null || stack == null || !(stack.getItem() instanceof IRelicItem relic))
            return new DescriptionData(List.of(), List.of());

        var synergyData = relic.getRelicData(player, stack).getAbilitiesData().getSynergyData(ability);
        var template = synergyData.getTemplate();

        if (template == null)
            return new DescriptionData(List.of(), List.of());

        var progress = synergyData.getProgress();

        var dynamicComponents = template.getStats().values().stream().map(stat -> {
            var rawValue = synergyData.getStatData(stat.getId()).getValueForProgress(progress);
            var txt = stat.getFormatValue().apply(rawValue).toString();

            if (txt.endsWith(".0"))
                txt = txt.substring(0, txt.length() - 2);

            return Component.literal(txt)
                    .withStyle(ChatFormatting.BOLD);
        }).toList();

        var itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        var key = "relics.description." + itemId + ".synergy." + ability + "." + (template.getModes().isEmpty() ? "description" : synergyData.getMode() + ".description");
        var tokens = IntStream.rangeClosed(1, dynamicComponents.size()).mapToObj(i -> "%" + i + "$s").toArray(String[]::new);
        var descriptionComponent = Component.translatable(key, (Object[]) tokens);

        var rawLines = new ArrayList<TextJustificator.LineEntry>();

        rawLines.add(new TextJustificator.LineEntry(descriptionComponent, true));

        for (var entry : template.getRankModifiers().entries()) {
            var unlocked = relic.getRelicData(player, stack).getLevelingData().getRank() >= entry.getKey();
            var condition = Component.literal(unlocked ? RANK_MODIFIER_CONDITION_INDENT : "")
                    .append(Component.translatable("relics.description.ability.rank_modifier.condition.rank", entry.getKey()))
                    .withStyle(ChatFormatting.BOLD);

            rawLines.add(new TextJustificator.LineEntry(Component.literal(""), false));
            rawLines.add(new TextJustificator.LineEntry(condition, false));

            var description = Component.literal("● ").append(Component.translatable("relics.description." + itemId + ".ability." + ability + ".rank_modifier." + entry.getValue(), (Object[]) tokens));

            if (!unlocked)
                description = ScreenUtils.randomizeAllCharacters(description, this.hashCode()).withStyle(Style.EMPTY.withFont(ScreenUtils.ILLAGER_ALT_FONT).withColor(DescriptionUtils.NEGATIVE_COLOR(true)));
            else if (!synergyData.getRankModifierData(entry.getValue()).isEnabled())
                description = description.withStyle(ChatFormatting.STRIKETHROUGH);

            rawLines.add(new TextJustificator.LineEntry(description, true));
        }

        if (!synergyData.isUnlocked())
            for (var line : rawLines)
                line.setRawLine(ScreenUtils.randomizeAllCharacters(line.getRawLine(), this.hashCode()).withStyle(Style.EMPTY.withFont(ScreenUtils.ILLAGER_ALT_FONT).withColor(DescriptionUtils.NEGATIVE_COLOR(true))));

        return new DescriptionData(rawLines, dynamicComponents);
    }

    @Override
    public int getContentHeight() {
        var data = constructDescriptionData();
        var lines = TextJustificator.layoutJustifiedLines(minecraft.font, data.rawLines(), data.dynamicComponents(), 320);

        var step = minecraft.font.lineHeight + 1;

        return (lines.size() * step / 2 + VERTICAL_PADDING * 6);
    }

    public List<RankModifierToggleWidget.Entry> getRankModifierToggleEntries() {
        var player = minecraft.player;
        var screen = (SynergyDescriptionScreen) getScreen();
        var stack = screen.getStack();
        var synergy = screen.getSelectedSynergy();

        if (player == null || stack == null || !(stack.getItem() instanceof IRelicItem relic))
            return List.of();

        var synergyData = relic.getRelicData(player, stack).getAbilitiesData().getSynergyData(synergy);
        var template = synergyData.getTemplate();

        if (template == null)
            return List.of();

        var data = this.constructDescriptionData();
        var rankModifiers = template.getRankModifiers().entries().stream().toList();
        var result = new ArrayList<RankModifierToggleWidget.Entry>();
        var rank = synergyData.getAbilitiesData().getRelicData().getLevelingData().getRank();
        var lineIndex = TextJustificator.layoutJustifiedLines(this.minecraft.font, List.of(data.rawLines().getFirst()), data.dynamicComponents(), 320).size();
        var rawLineIndex = 1;

        for (var modifier : rankModifiers) {
            lineIndex += TextJustificator.layoutJustifiedLines(this.minecraft.font, List.of(data.rawLines().get(rawLineIndex++)), data.dynamicComponents(), 320).size();

            if (rank >= modifier.getKey())
                result.add(new RankModifierToggleWidget.Entry(modifier.getValue(), lineIndex));

            lineIndex += TextJustificator.layoutJustifiedLines(this.minecraft.font, List.of(data.rawLines().get(rawLineIndex++)), data.dynamicComponents(), 320).size();
            lineIndex += TextJustificator.layoutJustifiedLines(this.minecraft.font, List.of(data.rawLines().get(rawLineIndex++)), data.dynamicComponents(), 320).size();
        }

        return result;
    }
}
