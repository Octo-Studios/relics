package it.hurts.sskirillss.relics.client.screen.description.relic.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.data.RelicData;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.description.relic.C2SChangeRelicOptionFlawlessVisual;
import it.hurts.sskirillss.relics.utils.data.GUIScissors;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class RelicOptionsContainerWidget extends DescriptionContainerWidget {
    private static final ResourceLocation SWITCH_ON = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/rank_modifier_switch_on.png");
    private static final ResourceLocation SWITCH_OFF = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/rank_modifier_switch_off.png");
    private static final ResourceLocation SWITCH_OUTLINE = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/rank_modifier_switch_outline.png");

    private static final int TOGGLE_X = 5;
    private static final int TOGGLE_Y = 0;
    private static final int TOGGLE_WIDTH = 10;
    private static final int TOGGLE_HEIGHT = 7;
    private static final int ROW_STEP = 10;
    private static final int LABEL_X_OFFSET = 12;
    private static final int LABEL_Y_OFFSET = 2;

    private static final List<OptionEntry> OPTIONS = List.of(
            new OptionEntry(
                    Component.translatable("relics.description.researching.relic.options.flawless_visual"),
                    data -> data.getOptionsData().isVisuallyFlawless(),
                    (data, value) -> data.getOptionsData().setVisuallyFlawless(value),
                    (screen, value) -> NetworkHandler.sendToServer(new C2SChangeRelicOptionFlawlessVisual(screen.getContainer(), screen.getSlot(), value))
            )
    );

    public RelicOptionsContainerWidget(DescriptionScreen screen) {
        super(screen);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var player = this.minecraft.player;
        var stack = this.getScreen().getStack();

        if (player == null || stack == null || !(stack.getItem() instanceof IRelicItem))
            return;

        var data = this.resolveRelicData();

        if (data == null)
            return;

        var poseStack = guiGraphics.pose();

        GUIScissors.begin(this.getX(), this.getY() - 1, this.getWidth(), this.getHeight() + 1);

        for (int index = 0; index < OPTIONS.size(); index++) {
            var option = OPTIONS.get(index);
            var x = this.getX() + TOGGLE_X;
            var y = this.getY() + TOGGLE_Y + index * ROW_STEP;

            poseStack.pushPose();
            poseStack.translate(x, y, 100);

            GUIRenderer.begin(option.getEnabled().apply(data) ? SWITCH_ON : SWITCH_OFF, poseStack)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .pos(1, 1)
                    .end();

            if (this.isToggleHovered(mouseX, mouseY, index))
                GUIRenderer.begin(SWITCH_OUTLINE, poseStack)
                        .anchor(SpriteAnchor.TOP_LEFT)
                        .end();

            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

            poseStack.popPose();

            poseStack.pushPose();
            poseStack.translate(0, 0, 100);
            poseStack.scale(0.5F, 0.5F, 0.5F);

            guiGraphics.drawString(this.minecraft.font, option.label(), (x + LABEL_X_OFFSET) * 2, (y + LABEL_Y_OFFSET) * 2, DescriptionUtils.TEXT_COLOR, false);

            poseStack.popPose();
        }

        GUIScissors.end();
    }

    @Override
    public int getContentHeight() {
        return 0;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        var data = this.resolveRelicData();

        if (data == null)
            return false;

        for (int index = 0; index < OPTIONS.size(); index++) {
            if (!this.isToggleHovered(mouseX, mouseY, index))
                continue;

            this.toggleOption(data, index);
            this.playDownSound(this.minecraft.getSoundManager());

            return true;
        }

        return false;
    }

    @Override
    public void playDownSound(net.minecraft.client.sounds.SoundManager handler) {
        handler.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
    }

    private void toggleOption(RelicData data, int index) {
        var option = OPTIONS.get(index);
        var enabled = !option.getEnabled().apply(data);

        option.setEnabled().accept(data, enabled);
        option.sync().accept(this.getScreen(), enabled);
    }

    private RelicData resolveRelicData() {
        var player = this.minecraft.player;
        var stack = this.getScreen().getStack();

        if (player == null || stack == null || !(stack.getItem() instanceof IRelicItem relic))
            return null;

        return relic.getRelicData(player, stack);
    }

    private boolean isToggleHovered(double mouseX, double mouseY, int index) {
        var x = this.getX() + TOGGLE_X;
        var y = this.getY() + TOGGLE_Y + index * ROW_STEP;

        return mouseX >= x
                && mouseX < x + TOGGLE_WIDTH
                && mouseY >= y
                && mouseY < y + TOGGLE_HEIGHT;
    }

    private record OptionEntry(Component label, Function<RelicData, Boolean> getEnabled, BiConsumer<RelicData, Boolean> setEnabled, BiConsumer<DescriptionScreen, Boolean> sync) {

    }
}
