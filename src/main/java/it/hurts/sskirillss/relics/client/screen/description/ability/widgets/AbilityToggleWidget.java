package it.hurts.sskirillss.relics.client.screen.description.ability.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.description.ability.AbilityDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractDescriptionWidget;
import it.hurts.sskirillss.relics.client.screen.utils.misc.ToggleSwitch;
import it.hurts.sskirillss.relics.client.screen.utils.misc.ToggleSwitchAnimator;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.description.ability.C2SChangeAbilityEnabled;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class AbilityToggleWidget extends AbstractDescriptionWidget {
    public static final int WIDTH = 10;
    public static final int HEIGHT = 7;

    public static final int TOGGLE_X = 107 + 5;
    public static final int TOGGLE_Y = 61;
    public static final int TITLE_X = 114;
    public static final int TITLE_OFFSET = WIDTH + 1;

    private static final ResourceLocation SWITCH_OUTLINE = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/rank_modifier_switch_outline.png");

    private final AbilityDescriptionScreen screen;
    private final ToggleSwitchAnimator switches = new ToggleSwitchAnimator();

    public AbilityToggleWidget(int x, int y, AbilityDescriptionScreen screen) {
        super(x, y, WIDTH, HEIGHT);

        this.screen = screen;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var poseStack = guiGraphics.pose();

        poseStack.pushPose();
        poseStack.translate(this.getX(), this.getY(), 100);

        ToggleSwitch.render(poseStack, this.switches.amount(this.screen.getSelectedAbility(), this.isAbilityEnabled()));

        if (this.isHovered())
            GUIRenderer.begin(SWITCH_OUTLINE, poseStack)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .end();

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        poseStack.popPose();
    }

    @Override
    public void onPress() {
        var player = minecraft.player;
        var stack = this.screen.getStack();

        if (player == null || stack == null || !(stack.getItem() instanceof IRelicItem relic))
            return;

        var enabled = !this.isAbilityEnabled();

        relic.getRelicData(player, stack).getAbilitiesData().getAbilityData(this.screen.getSelectedAbility()).setEnabled(enabled);

        NetworkHandler.sendToServer(new C2SChangeAbilityEnabled(this.screen.getContainer(), this.screen.getSlot(), this.screen.getSelectedAbility(), enabled));
    }

    private boolean isAbilityEnabled() {
        var player = minecraft.player;
        var stack = this.screen.getStack();

        if (player == null || stack == null || !(stack.getItem() instanceof IRelicItem relic))
            return true;

        return relic.getRelicData(player, stack).getAbilitiesData().getAbilityData(this.screen.getSelectedAbility()).isEnabled();
    }
}
