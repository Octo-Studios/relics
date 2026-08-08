package it.hurts.sskirillss.relics.client.screen.description.synergy.widgets;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractDescriptionWidget;
import it.hurts.sskirillss.relics.client.screen.description.synergy.SynergyDescriptionScreen;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.description.ability.C2SChangeAbilityMode;
import it.hurts.sskirillss.relics.network.packets.description.synergy.C2SChangeSynergyMode;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class SynergyModeWidget extends AbstractDescriptionWidget {
    @Getter
    private SynergyDescriptionScreen screen;

    @Getter
    private int step;

    public SynergyModeWidget(int x, int y, SynergyDescriptionScreen screen, int step) {
        super(x, y, 12, 12);

        this.screen = screen;
        this.step = step;
    }

    @Override
    public void onPress() {
        var player = minecraft.player;
        var stack = screen.getStack();

        if (!(stack.getItem() instanceof IRelicItem relic))
            return;

        var synergyData = relic.getRelicData(player, stack).getAbilitiesData().getSynergyData(screen.getSelectedSynergy());
        var template = synergyData.getTemplate();

        if (template == null)
            return;

        var modes = template.getModes();
        var currentMode = synergyData.getMode();

        int step = this.getStep();

        var currentIndex = modes.indexOf(currentMode);

        if (currentIndex == -1 || modes.isEmpty())
            return;

        int newIndex = Math.floorMod(currentIndex + step, modes.size());

        var newMode = modes.get(newIndex);

        NetworkHandler.sendToServer(new C2SChangeSynergyMode(screen.getContainer(), screen.getSlot(), screen.getSelectedSynergy(), newMode));

        screen.rebuildWidgets();
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        var poseStack = guiGraphics.pose();

        poseStack.pushPose();

        GUIRenderer.begin(step > 0 ? ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/ability/ability_mode_arrow_right.png") : ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/ability/ability_mode_arrow_left.png"), poseStack)
                .anchor(SpriteAnchor.TOP_LEFT)
                .pos(this.getX(), this.getY())
                .end();

        if (this.isHovered())
            GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/ability/ability_mode_arrow_selection.png"), poseStack)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .pos(this.getX() - 1, this.getY() - 1)
                    .end();

        poseStack.popPose();
    }
}
