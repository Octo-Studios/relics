package it.hurts.sskirillss.relics.client.screen.description.general.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.description.ability.AbilityDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractDescriptionWidget;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.DescriptionContainerWidget;
import it.hurts.sskirillss.relics.client.screen.description.synergy.SynergyDescriptionScreen;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.description.ability.C2SChangeAbilityRankModifier;
import it.hurts.sskirillss.relics.network.packets.description.synergy.C2SChangeSynergyRankModifier;
import it.hurts.sskirillss.relics.utils.RenderUtils;
import it.hurts.sskirillss.relics.utils.data.GUIScissors;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class RankModifierToggleWidget extends AbstractDescriptionWidget {
    public static final int WIDTH = 10;
    public static final int HEIGHT = 7;

    private static final double LINE_STEP = 5D;
    private static final int X_OFFSET = 5;
    private static final int Y_OFFSET = -2;
    private static final ResourceLocation SWITCH_ON = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/rank_modifier_switch_on.png");
    private static final ResourceLocation SWITCH_OFF = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/rank_modifier_switch_off.png");
    private static final ResourceLocation SWITCH_OUTLINE = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/rank_modifier_switch_outline.png");

    private final DescriptionContainerWidget container;
    private final String rankModifier;
    private final int lineIndex;
    private final boolean synergy;
    private double renderX;
    private double renderY;

    public RankModifierToggleWidget(DescriptionContainerWidget container, String rankModifier, int lineIndex, boolean synergy) {
        super(container.getX() + X_OFFSET, container.getY() + Y_OFFSET, WIDTH, HEIGHT);

        this.container = container;
        this.rankModifier = rankModifier;
        this.lineIndex = lineIndex;
        this.synergy = synergy;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.updatePosition();

        if (!this.isVisible())
            return;

        var poseStack = guiGraphics.pose();

        GUIScissors.begin(this.container.getX(), this.container.getY() - 1, this.container.getWidth(), this.container.getHeight() + 1);

        poseStack.pushPose();
        poseStack.translate(this.renderX, this.renderY, 100);

        GUIRenderer.begin(this.isRankModifierEnabled() ? SWITCH_ON : SWITCH_OFF, poseStack)
                .anchor(SpriteAnchor.TOP_LEFT)
                .pos(1, 1)
                .end();

        if (this.isHovered())
            GUIRenderer.begin(SWITCH_OUTLINE, poseStack)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .end();

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        poseStack.popPose();

        GUIScissors.end();
    }

    @Override
    public void onPress() {
        if (!this.isVisible())
            return;

        var enabled = !this.isRankModifierEnabled();

        if (this.synergy) {
            var screen = (SynergyDescriptionScreen) this.container.getScreen();
            var player = minecraft.player;
            var stack = screen.getStack();

            if (player == null || stack == null || !(stack.getItem() instanceof IRelicItem relic))
                return;

            var synergyData = relic.getRelicData(player, stack).getAbilitiesData().getSynergyData(screen.getSelectedSynergy());

            synergyData.getRankModifierData(this.rankModifier).setEnabled(enabled);
            NetworkHandler.sendToServer(new C2SChangeSynergyRankModifier(screen.getContainer(), screen.getSlot(), screen.getSelectedSynergy(), this.rankModifier, enabled));

            return;
        }

        var screen = (AbilityDescriptionScreen) this.container.getScreen();
        var player = minecraft.player;
        var stack = screen.getStack();

        if (player == null || stack == null || !(stack.getItem() instanceof IRelicItem relic))
            return;

        var abilityData = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData(screen.getSelectedAbility());

        abilityData.getRankModifierData(this.rankModifier).setEnabled(enabled);
        NetworkHandler.sendToServer(new C2SChangeAbilityRankModifier(screen.getContainer(), screen.getSlot(), screen.getSelectedAbility(), this.rankModifier, enabled));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.updatePosition();

        return this.isVisible() && super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        this.updatePosition();

        return this.isVisible()
                && mouseX >= this.renderX
                && mouseX < this.renderX + this.width
                && mouseY >= this.renderY
                && mouseY < this.renderY + this.height;
    }

    private boolean isRankModifierEnabled() {
        var screen = this.container.getScreen();
        var player = minecraft.player;
        var stack = screen.getStack();

        if (player == null || stack == null || !(stack.getItem() instanceof IRelicItem relic))
            return true;

        var abilitiesData = relic.getRelicData(player, stack).getAbilitiesData();

        if (this.synergy)
            return abilitiesData.getSynergyData(((SynergyDescriptionScreen) screen).getSelectedSynergy()).getRankModifierData(this.rankModifier).isEnabled();

        return abilitiesData.getAbilityData(((AbilityDescriptionScreen) screen).getSelectedAbility()).getRankModifierData(this.rankModifier).isEnabled();
    }

    private void updatePosition() {
        var scrollbar = this.container.getScrollbar();
        var scroll = 0D;

        if (scrollbar != null && !scrollbar.isLocked()) {
            var lineCount = Math.max(0D, (this.container.getContentHeight() - 6D) / LINE_STEP);
            var overflowLines = Math.max(0D, lineCount - DescriptionContainerWidget.MAX_LINES);
            var maxScrollPx = overflowLines <= 0D ? 0D : overflowLines * LINE_STEP + 1.5D;

            scroll = scrollbar.getScrollPosition(RenderUtils.getPartialTick(false)) * maxScrollPx;
        }

        this.renderX = this.container.getX() + X_OFFSET;
        this.renderY = this.container.getY() + Y_OFFSET + this.lineIndex * LINE_STEP - scroll;

        this.setX((int) Math.floor(this.renderX));
        this.setY((int) Math.floor(this.renderY));
    }

    private boolean isVisible() {
        return this.renderY + this.getHeight() >= this.container.getY()
                && this.renderY <= this.container.getY() + this.container.getHeight();
    }

    public record Entry(String rankModifier, int lineIndex) {

    }
}
