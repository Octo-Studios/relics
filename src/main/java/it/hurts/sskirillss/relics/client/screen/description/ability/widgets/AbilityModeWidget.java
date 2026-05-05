package it.hurts.sskirillss.relics.client.screen.description.ability.widgets;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.description.ability.AbilityDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractDescriptionWidget;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.description.ability.C2SChangeAbilityMode;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class AbilityModeWidget extends AbstractDescriptionWidget {
    @Getter
    private AbilityDescriptionScreen screen;

    @Getter
    private int step;

    public AbilityModeWidget(int x, int y, AbilityDescriptionScreen screen, int step) {
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

        var abilityData = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData(screen.getSelectedAbility());
        var template = abilityData.getTemplate();

        if (template == null)
            return;

        var modes = template.getModes();
        var currentMode = abilityData.getMode();

        int step = this.getStep();

        var currentIndex = modes.indexOf(currentMode);

        if (currentIndex == -1 || modes.isEmpty())
            return;

        int newIndex = Math.floorMod(currentIndex + step, modes.size());

        var newMode = modes.get(newIndex);

        NetworkHandler.sendToServer(new C2SChangeAbilityMode(screen.getContainer(), screen.getSlot(), screen.getSelectedAbility(), newMode));

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
