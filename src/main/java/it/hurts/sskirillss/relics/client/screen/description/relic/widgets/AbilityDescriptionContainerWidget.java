package it.hurts.sskirillss.relics.client.screen.description.relic.widgets;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.AbilityStatTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatStackingType;
import it.hurts.sskirillss.relics.client.screen.base.IHoverableWidget;
import it.hurts.sskirillss.relics.client.screen.description.ability.AbilityDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.RankModifierToggleWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.client.screen.description.misc.TextJustificator;
import it.hurts.sskirillss.relics.client.screen.utils.ScreenUtils;
import it.hurts.sskirillss.relics.utils.RenderUtils;
import it.hurts.sskirillss.relics.utils.data.GUIScissors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

public class AbilityDescriptionContainerWidget extends DescriptionContainerWidget implements IHoverableWidget {
    private static final int VERTICAL_PADDING = 1;
    private static final String RANK_MODIFIER_CONDITION_INDENT = "    ";
    private static final Style DISABLED_STYLE = Style.EMPTY.withStrikethrough(true);

    private TextJustificator.StatBox hoveredStat;

    public AbilityDescriptionContainerWidget(DescriptionScreen screen) {
        super(screen);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var player = this.minecraft.player;
        var screen = ((AbilityDescriptionScreen) this.getScreen());
        var stack = screen.getStack();

        hoveredStat = null;

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

        hoveredStat = TextJustificator.renderJustifiedDescriptionWithStatBoxes(guiGraphics, (getX() + 7) * 2, getY() * 2, 320, minecraft.font, layout, mouseX * 2, mouseY * 2 + shiftY);

        poseStack.popPose();

        GUIScissors.end();
    }

    @Override
    public void onHovered(GuiGraphics graphics, int mouseX, int mouseY) {
        if (hoveredStat == null)
            return;

        var screen = (AbilityDescriptionScreen) getScreen();
        var stack = screen.getStack();
        var ability = ((IRelicItem) stack.getItem()).getRelicData(minecraft.player, stack).getAbilitiesData().getAbilityData(screen.getSelectedAbility());
        var stat = ability.getStatData(ability.getTemplate().getStats().keySet().stream().toList().get(hoveredStat.index()));
        var template = stat.getTemplate();
        var type = template.getStacking().getType();
        var key = "relics.description.ability.stat.stacking." + type.name().toLowerCase(Locale.ROOT);
        var stacking = Component.translatable(key);
        var shiftDown = Screen.hasShiftDown();

        if (shiftDown)
            stacking = Component.translatable("relics.description.ability.stat.stacking.detail", stacking, Component.translatable(key + ".description"));

        var lines = new ArrayList<MutableComponent>();

        lines.add(tooltipLabel("relics.description.ability.stat.stacking").append(stacking));

        if (type.ranksCopiesByStat()) {
            var result = stat.getStacking();
            var efficiency = result.efficiency(stack);

            if (type == StatStackingType.BEST) {
                if (efficiency.isPresent())
                    lines.add(Component.translatable(key + (result.entries().getFirst().stack() == stack ? ".selected" : ".other")));
            } else {
                efficiency.ifPresent(value -> lines.add(tooltipLabel("relics.description.ability.stat.efficiency")
                        .append(BigDecimal.valueOf(value * 100D).stripTrailingZeros().toPlainString() + "%")));
            }

            if (result.entries().size() > 1)
                result.total().ifPresent(value -> {
                    var total = formatValue(template, value);

                    if (!total.equals(formatValue(template, stat.getValue())))
                        lines.add(tooltipLabel("relics.description.ability.stat.total").append(hoveredStat.format(total)));
                });
        }

        if (!shiftDown || (type.ranksCopiesByStat() && type != StatStackingType.BEST)) {
            lines.add(Component.literal(" "));
            lines.add(shiftDown
                    ? Component.translatable("relics.description.ability.stat.efficiency.description").withStyle(ChatFormatting.ITALIC)
                    : Component.translatable("relics.description.researching.general.extra_info"));
        }

        DescriptionUtils.drawActionTooltip(graphics, minecraft.font, lines, mouseX + 2, mouseY);
    }

    private static MutableComponent tooltipLabel(String key) {
        return Component.empty().append(Component.translatable(key).withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE)).append(" ");
    }

    private static String formatValue(AbilityStatTemplate stat, double value) {
        var text = stat.getFormatValue().apply(value).toString();

        return text.endsWith(".0") ? text.substring(0, text.length() - 2) : text;
    }

    public record DescriptionData(List<TextJustificator.LineEntry> rawLines, List<MutableComponent> dynamicComponents) {
    }

    public DescriptionData constructDescriptionData() {
        var player = minecraft.player;
        var screen = (AbilityDescriptionScreen) getScreen();
        var stack = screen.getStack();
        var ability = screen.getSelectedAbility();

        if (player == null || stack == null || !(stack.getItem() instanceof IRelicItem relic))
            return new DescriptionData(List.of(), List.of());

        var abilityData = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData(ability);
        var template = abilityData.getTemplate();

        if (template == null)
            return new DescriptionData(List.of(), List.of());

        var wantsReroll = screen.getRerollButton() != null && screen.getRerollButton().isHovered() && abilityData.mayReroll();

        var dynamicComponents = template.getStats().values().stream().map(stat -> {
            var rawValue = abilityData.getStatData(stat.getId()).getValue();
            var result = Component.literal(formatValue(stat, rawValue))
                    .withStyle(ChatFormatting.BOLD)
                    .withColor(wantsReroll ? DescriptionUtils.NEUTRAL_COLOR(true) : DescriptionUtils.TEXT_COLOR);

            if (wantsReroll)
                result = result.withStyle(ChatFormatting.OBFUSCATED);

            if (abilityData.isUnlocked() && !abilityData.isEnabled())
                result = result.withStyle(DISABLED_STYLE);

            return result;
        }).toList();

        var itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        var key = "relics.description." + itemId + ".ability." + ability + "." + (template.getModes().isEmpty() ? "description" : abilityData.getMode() + ".description");
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
            else if (!abilityData.getRankModifierData(entry.getValue()).isEnabled())
                description = description.withStyle(ChatFormatting.STRIKETHROUGH);

            rawLines.add(new TextJustificator.LineEntry(description, true));
        }

        if (!abilityData.isUnlocked())
            for (var line : rawLines)
                line.setRawLine(ScreenUtils.randomizeAllCharacters(line.getRawLine(), this.hashCode()).withStyle(Style.EMPTY.withFont(ScreenUtils.ILLAGER_ALT_FONT).withColor(DescriptionUtils.NEGATIVE_COLOR(true))));
        else if (!abilityData.isEnabled())
            for (var line : rawLines)
                if (line.isJustify())
                    line.setRawLine(line.getRawLine().withStyle(DISABLED_STYLE));

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
        var screen = (AbilityDescriptionScreen) getScreen();
        var stack = screen.getStack();
        var ability = screen.getSelectedAbility();

        if (player == null || stack == null || !(stack.getItem() instanceof IRelicItem relic))
            return List.of();

        var abilityData = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData(ability);
        var template = abilityData.getTemplate();

        if (template == null)
            return List.of();

        var data = this.constructDescriptionData();
        var rankModifiers = template.getRankModifiers().entries().stream().toList();
        var result = new ArrayList<RankModifierToggleWidget.Entry>();
        var rank = abilityData.getAbilitiesData().getRelicData().getLevelingData().getRank();
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
