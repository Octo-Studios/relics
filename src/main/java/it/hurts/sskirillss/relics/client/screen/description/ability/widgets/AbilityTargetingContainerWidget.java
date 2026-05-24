package it.hurts.sskirillss.relics.client.screen.description.ability.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.abilities.targeting.AbilityTargetingOption;
import it.hurts.sskirillss.relics.api.relics.abilities.targeting.SelectorType;
import it.hurts.sskirillss.relics.api.relics.data.AbilityData;
import it.hurts.sskirillss.relics.client.screen.description.ability.AbilityDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.DescriptionContainerWidget;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.targeting.C2SChangeAbilityTargetingOption;
import it.hurts.sskirillss.relics.utils.RenderUtils;
import it.hurts.sskirillss.relics.utils.data.GUIScissors;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class AbilityTargetingContainerWidget extends DescriptionContainerWidget {
    private static final ResourceLocation SWITCH_ON = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/rank_modifier_switch_on.png");
    private static final ResourceLocation SWITCH_OFF = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/rank_modifier_switch_off.png");
    private static final ResourceLocation SWITCH_OUTLINE = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/rank_modifier_switch_outline.png");

    private static final int TOGGLE_X = 5;
    private static final int TOGGLE_Y = 0;
    private static final int TOGGLE_WIDTH = 10;
    private static final int TOGGLE_HEIGHT = 7;
    private static final int ROW_STEP = 10;
    private static final int HEADER_STEP = 9;
    private static final int LABEL_X_OFFSET = 12;
    private static final int LABEL_Y_OFFSET = 2;

    public AbilityTargetingContainerWidget(DescriptionScreen screen) {
        super(screen);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var data = this.resolveAbilityData();

        if (data == null)
            return;

        var rows = this.getRows(data);
        var poseStack = guiGraphics.pose();
        var scrollOffset = this.getScrollOffset();

        GUIScissors.begin(this.getX(), this.getY() - 1, this.getWidth(), this.getHeight() + 1);

        var yOffset = 0;

        for (int index = 0; index < rows.size(); index++) {
            var row = rows.get(index);

            if (row.header()) {
                var y = this.getY() + yOffset - scrollOffset;

                poseStack.pushPose();
                poseStack.translate(0, 0, 100);
                poseStack.scale(0.5F, 0.5F, 0.5F);

                guiGraphics.drawString(this.minecraft.font, getModeLabel(row.mode()), (this.getX() + TOGGLE_X) * 2, (int) Math.round((y + LABEL_Y_OFFSET) * 2), DescriptionUtils.TEXT_COLOR, false);

                poseStack.popPose();
                yOffset += HEADER_STEP;

                continue;
            }

            var option = row.option();
            var x = this.getX() + TOGGLE_X;
            var y = this.getY() + TOGGLE_Y + yOffset - scrollOffset;

            poseStack.pushPose();
            poseStack.translate(x, y, 100);

            GUIRenderer.begin(data.getTargetingData().getOption(option, row.mode()) ? SWITCH_ON : SWITCH_OFF, poseStack)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .pos(1, 1)
                    .end();

            if (this.isToggleHovered(mouseX, mouseY, row, yOffset))
                GUIRenderer.begin(SWITCH_OUTLINE, poseStack)
                        .anchor(SpriteAnchor.TOP_LEFT)
                        .end();

            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

            poseStack.popPose();

            poseStack.pushPose();
            poseStack.translate(0, 0, 100);
            poseStack.scale(0.5F, 0.5F, 0.5F);

            guiGraphics.drawString(this.minecraft.font, getLabel(option), (x + LABEL_X_OFFSET) * 2, (int) Math.round((y + LABEL_Y_OFFSET) * 2), DescriptionUtils.TEXT_COLOR, false);

            poseStack.popPose();
            yOffset += ROW_STEP;
        }

        GUIScissors.end();
    }

    @Override
    public int getContentHeight() {
        var data = this.resolveAbilityData();

        return data == null ? 0 : this.getRows(data).stream().mapToInt(row -> row.header() ? HEADER_STEP : ROW_STEP).sum();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        var data = this.resolveAbilityData();

        if (data == null)
            return false;

        var rows = this.getRows(data);
        var yOffset = 0;

        for (var row : rows) {
            if (row.header()) {
                yOffset += HEADER_STEP;
                continue;
            }

            if (!this.isToggleHovered(mouseX, mouseY, row, yOffset)) {
                yOffset += ROW_STEP;

                continue;
            }

            var option = row.option();
            var enabled = !data.getTargetingData().getOption(option, row.mode());

            data.getTargetingData().setOption(option, row.mode(), enabled);
            NetworkHandler.sendToServer(new C2SChangeAbilityTargetingOption(this.getScreen().getContainer(), this.getScreen().getSlot(), data.getId(), false, row.mode().getSerializedName(), option.name().toLowerCase(Locale.ROOT), enabled));
            this.playDownSound(this.minecraft.getSoundManager());

            return true;
        }

        return false;
    }

    @Override
    public void playDownSound(net.minecraft.client.sounds.SoundManager handler) {
        handler.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
    }

    private AbilityData resolveAbilityData() {
        var player = this.minecraft.player;
        var stack = this.getScreen().getStack();

        if (player == null || stack == null || !(stack.getItem() instanceof IRelicItem relic) || !(this.getScreen() instanceof AbilityDescriptionScreen screen))
            return null;

        return relic.getRelicData(player, stack).getAbilitiesData().getAbilityData(screen.getSelectedAbility());
    }

    private List<TargetingRow> getRows(AbilityData data) {
        var targeting = data.getTemplate().getTargeting();
        var mixed = targeting.getSelectors().size() > 1;

        return targeting.getSelectors().stream()
                .sorted(Comparator.comparingInt(Enum::ordinal))
                .flatMap(mode -> {
                    var options = targeting.optionsFor(mode).stream()
                            .filter(targeting::hasOption)
                            .sorted(Comparator.comparingInt(Enum::ordinal))
                            .map(option -> TargetingRow.option(mode, option));

                    return mixed ? java.util.stream.Stream.concat(java.util.stream.Stream.of(TargetingRow.header(mode)), options) : options;
                })
                .toList();
    }

    private boolean isToggleHovered(double mouseX, double mouseY, TargetingRow row, int yOffset) {
        if (row.header())
            return false;

        var x = this.getX() + TOGGLE_X;
        var y = this.getY() + TOGGLE_Y + yOffset - this.getScrollOffset();

        return mouseX >= x
                && mouseX < x + TOGGLE_WIDTH
                && mouseY >= y
                && mouseY < y + TOGGLE_HEIGHT;
    }

    private double getScrollOffset() {
        var scrollbar = this.getScrollbar();

        if (scrollbar == null)
            return 0D;

        var extraHeight = Math.max(0D, this.getContentHeight() - this.getHeight());

        return scrollbar.getScrollPosition(RenderUtils.getPartialTick(false)) * extraHeight;
    }

    private Component getLabel(AbilityTargetingOption option) {
        var key = "relics.description.researching.ability.targeting." + option.name().toLowerCase(Locale.ROOT);

        return Component.translatableWithFallback(key, option.name().toLowerCase(Locale.ROOT).replace('_', ' '));
    }

    private MutableComponent getModeLabel(SelectorType selectorType) {
        var key = "relics.description.researching.ability.targeting.mode." + selectorType.getSerializedName();

        return Component.translatableWithFallback(key, selectorType.getSerializedName()).append(":").withStyle(style -> style.withBold(true));
    }

    private record TargetingRow(SelectorType mode, AbilityTargetingOption option, boolean header) {
        private static TargetingRow header(SelectorType selectorType) {
            return new TargetingRow(selectorType, null, true);
        }

        private static TargetingRow option(SelectorType selectorType, AbilityTargetingOption option) {
            return new TargetingRow(selectorType, option, false);
        }
    }
}
