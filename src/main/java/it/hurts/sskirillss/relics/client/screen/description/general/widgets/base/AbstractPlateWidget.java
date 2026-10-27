package it.hurts.sskirillss.relics.client.screen.description.general.widgets.base;

import com.mojang.blaze3d.systems.RenderSystem;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.client.screen.base.IHoverableWidget;
import it.hurts.sskirillss.relics.client.screen.base.ITickingWidget;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPlateWidget extends AbstractDescriptionWidget implements IHoverableWidget, ITickingWidget {
    private static final double WOBBLE_SPEED = 0.075D;
    private static final double WOBBLE_PHASE = 10D;
    private static final double WOBBLE_Y_SCALE = 0.5D;
    private static final float PULSE_DECAY = 0.75F;
    private static final float PULSE_THRESHOLD = 0.01F;
    private static final float PULSE_SCALE = 0.08F;
    private static final float PULSE_BRIGHTNESS = 0.9F;
    private static final float PULSE_BLUE_BRIGHTNESS = 0.6F;

    @Getter
    private DescriptionScreen screen;
    @Getter
    private final String icon;

    private float pulse = 0F;
    private float pulseO = 0F;

    public abstract String getValue(ItemStack stack);

    public AbstractPlateWidget(int x, int y, DescriptionScreen screen, String icon) {
        super(x, y, 55, 20);

        this.screen = screen;
        this.icon = icon;
    }

    @Override
    public final void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        var poseStack = guiGraphics.pose();

        var player = minecraft.player;

        poseStack.pushPose();

        poseStack.translate(getX() + getWobbleX(pPartialTick), getY() + getWobbleY(pPartialTick), 0);

        var glow = Mth.lerp(pPartialTick, pulseO, pulse);
        var scale = getPulseScale(pPartialTick);

        poseStack.translate(width / 2F, height / 2F, 0F);
        poseStack.scale(scale, scale, 1F);
        poseStack.translate(-width / 2F, -height / 2F, 0F);

        RenderSystem.setShaderColor(1F + glow * PULSE_BRIGHTNESS, 1F + glow * PULSE_BRIGHTNESS, 1F + glow * PULSE_BLUE_BRIGHTNESS, 1F);

        GUIRenderer.begin(DescriptionTextures.PLATE_BACKGROUND, poseStack)
                .anchor(SpriteAnchor.TOP_LEFT)
                .end();

        poseStack.translate(0F, 0F, 10F);

        this.renderIcon(guiGraphics, 3, 3, pMouseX, pMouseY, pPartialTick);

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        var value = Component.literal(this.getValue(screen.getStack())).withStyle(ChatFormatting.BOLD);

        guiGraphics.drawString(minecraft.font, value, 20, 7, 0xffe278, true);

        this.renderContent(guiGraphics, pMouseX, pMouseY, pPartialTick);

        if (isHovered())
            GUIRenderer.begin(DescriptionTextures.PLATE_OUTLINE, poseStack)
                    .pos(-1, -1)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .end();

        poseStack.popPose();
    }

    public float getPulseScale(float partialTick) {
        return 1F + Mth.lerp(partialTick, pulseO, pulse) * PULSE_SCALE;
    }

    public double getWobbleX(float partialTick) {
        return Math.sin((minecraft.player.tickCount + partialTick + icon.length() * WOBBLE_PHASE) * WOBBLE_SPEED);
    }

    public double getWobbleY(float partialTick) {
        return Math.cos((minecraft.player.tickCount + partialTick + icon.length() * WOBBLE_PHASE) * WOBBLE_SPEED) * WOBBLE_Y_SCALE;
    }

    public void pulse(float strength) {
        pulse = Math.min(1F, pulse + strength);
    }

    @Override
    public void onTick() {
        pulseO = pulse;
        pulse = pulse < PULSE_THRESHOLD ? 0F : pulse * PULSE_DECAY;
    }

    public void renderIcon(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY, float partialTick) {
        var poseStack = guiGraphics.pose();

        GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/icons/" + icon + ".png"), poseStack)
                .anchor(SpriteAnchor.TOP_LEFT)
                .pos(x, y)
                .end();
    }

    public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

    }

    @Override
    public void onHovered(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        var poseStack = guiGraphics.pose();

        var tooltip = new ArrayList<FormattedCharSequence>();

        var maxWidth = 150;
        var renderWidth = 0;

        var entries = this.getHoverTooltip();

        if (entries.isEmpty())
            return;

        for (var entry : entries) {
            int entryWidth = (minecraft.font.width(entry) / 2);

            if (entryWidth > renderWidth)
                renderWidth = Math.min(entryWidth + 2, maxWidth);

            tooltip.addAll(minecraft.font.split(entry, maxWidth * 2));
        }

        poseStack.pushPose();

        poseStack.translate(0F, 0F, 100);

        DescriptionUtils.drawTooltipBackground(guiGraphics, renderWidth, tooltip.size() * 5, mouseX - 9 - (renderWidth / 2), mouseY);

        poseStack.scale(0.5F, 0.5F, 0.5F);

        var yOff = 0;

        for (FormattedCharSequence entry : tooltip) {
            guiGraphics.drawString(minecraft.font, entry, ((mouseX - renderWidth / 2) + 1) * 2, ((mouseY + yOff + 9) * 2), DescriptionUtils.TEXT_COLOR, false);

            yOff += 5;
        }

        poseStack.popPose();
    }

    public List<MutableComponent> getHoverTooltip() {
        return new ArrayList<>();
    }

    @Override
    public void playDownSound(SoundManager handler) {

    }
}