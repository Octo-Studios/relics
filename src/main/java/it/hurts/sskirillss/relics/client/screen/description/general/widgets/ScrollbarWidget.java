package it.hurts.sskirillss.relics.client.screen.description.general.widgets;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.client.screen.base.IScrollableWidget;
import it.hurts.sskirillss.relics.client.screen.base.ITickingWidget;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractDescriptionWidget;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.DescriptionContainerWidget;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

public class ScrollbarWidget extends AbstractDescriptionWidget implements ITickingWidget {
    private final IScrollableWidget container;

    private double scrollPosition = 0.0;
    private double prevScrollPosition = 0.0;
    private double scrollVelocity = 0.0;
    private static final double FRICTION = 0.5;

    private boolean dragging = false;
    private double dragOffsetY = 0.0;

    private static final int TRACK_WIDTH = 10;
    private static final int TRACK_HEIGHT = 57;
    private static final int SLIDER_WIDTH = 10;
    private static final int SLIDER_HEIGHT = 12;
    private static final int SLIDER_OFFSET_X = -1;
    private static final int SLIDER_OFFSET_Y = 6;

    public ScrollbarWidget(int x, int y, IScrollableWidget container) {
        super(x, y, TRACK_WIDTH, TRACK_HEIGHT);

        this.container = container;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var poseStack = guiGraphics.pose();

        poseStack.pushPose();

        GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/top_scroll_bar.png"), poseStack)
                .pos(this.getX(), this.getY() - 20)
                .anchor(SpriteAnchor.TOP_LEFT)
                .end();

        if (!this.isLocked()) {
            int usableTrackHeight = TRACK_HEIGHT - SLIDER_HEIGHT - 4;
            int sliderX = this.getX() + SLIDER_OFFSET_X;
            int sliderY = this.getY() + SLIDER_OFFSET_Y + (int) Math.round(this.getScrollPosition(partialTick) * usableTrackHeight);

            var color = (float) (((AbstractWidget) container).isHovered() ? 1D + Math.sin(System.currentTimeMillis() * 0.01D) * 0.1D : 1D);

            poseStack.translate(sliderX + SLIDER_WIDTH / 2F, sliderY, 0);

            var distance = Math.min(Math.abs(prevScrollPosition - scrollPosition) * 1.1F, 0.35F);

            poseStack.scale((float) (1 - distance), (float) (1 + (distance * 2.5F)), 1);

            GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/scroll_bar_slider.png"), poseStack)
                    .color(color, color, color, 1F)
                    .anchor(SpriteAnchor.CENTER)
                    .end();

            if (isHoveringSlider(mouseX, mouseY)) {
                GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/scroll_bar_slider_selection.png"), poseStack)
                        .anchor(SpriteAnchor.CENTER)
                        .end();
            }
        }

        poseStack.popPose();
    }

    @Override
    public void onTick() {
        this.prevScrollPosition = this.scrollPosition;
        this.scrollPosition += this.scrollVelocity;
        this.scrollVelocity *= FRICTION;

        if (Math.abs(this.scrollVelocity) < 0.0005) {
            this.scrollVelocity = 0.0;
        }

        if (this.scrollPosition < 0.0) {
            this.scrollPosition = 0.0;
            this.scrollVelocity = 0.0;
        } else if (this.scrollPosition > 1.0) {
            this.scrollPosition = 1.0;
            this.scrollVelocity = 0.0;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isLocked() || button != GLFW.GLFW_MOUSE_BUTTON_LEFT || !isMouseOver(mouseX, mouseY))
            return false;

        this.dragOffsetY = this.isHoveringSlider(mouseX, mouseY) ? mouseY - this.getSliderTopY() : SLIDER_HEIGHT / 2D;
        this.dragging = true;

        this.updateScrollPositionFromMouse(mouseX, mouseY);

        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.isLocked() || !dragging || button != GLFW.GLFW_MOUSE_BUTTON_LEFT)
            return false;

        this.scrollVelocity = 0D;

        this.updateScrollPositionFromMouse(mouseX, mouseY);

        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.isLocked() || button != GLFW.GLFW_MOUSE_BUTTON_LEFT)
            return false;

        if (dragging) {
            dragging = false;

            return true;
        }

        return false;
    }

    private void updateScrollPositionFromMouse(double mouseX, double mouseY) {
        var trackStartY = this.getY() + SLIDER_OFFSET_Y;
        var usableTrackHeight = TRACK_HEIGHT - SLIDER_HEIGHT - 4;
        var relativeY = (mouseY - dragOffsetY) - trackStartY;

        this.scrollPosition = Math.clamp(relativeY / usableTrackHeight, 0, 1);
    }

    public boolean isHoveringSlider(double mouseX, double mouseY) {
        var sliderTop = getSliderTopY();

        return mouseX >= this.getX() + SLIDER_OFFSET_X
                && mouseX <= this.getX() + SLIDER_OFFSET_X + SLIDER_WIDTH
                && mouseY + SLIDER_OFFSET_Y >= sliderTop
                && mouseY + SLIDER_OFFSET_Y <= sliderTop + SLIDER_HEIGHT;
    }

    private double getSliderTopY() {
        var usableTrackHeight = TRACK_HEIGHT - SLIDER_HEIGHT - 4;

        return this.getY() + SLIDER_OFFSET_Y + (this.scrollPosition * usableTrackHeight);
    }

    public double getScrollPosition(float partialTicks) {
        return Mth.lerp(partialTicks, prevScrollPosition, scrollPosition);
    }

    @Override
    public boolean isLocked() {
        var unit = (this.minecraft.font.lineHeight + 2) / 2F;
        var threshold = DescriptionContainerWidget.MAX_LINES * unit;

        return container.getContentHeight() <= threshold;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.isLocked() || (!isMouseOver(mouseX, mouseY) && !((AbstractWidget) container).isHovered()))
            return false;

        var unit = (this.minecraft.font.lineHeight + 2) / 2F;
        var extraHeight = container.getContentHeight() - DescriptionContainerWidget.MAX_LINES * unit;
        var maxScrollPixels = Math.max(1F, extraHeight);

        var scrollDelta = (unit / (double) maxScrollPixels) * scrollY;

        this.scrollVelocity -= scrollDelta;

        return true;
    }
}