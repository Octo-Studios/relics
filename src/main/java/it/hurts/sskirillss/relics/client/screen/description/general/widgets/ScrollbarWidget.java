package it.hurts.sskirillss.relics.client.screen.description.general.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import it.hurts.sskirillss.relics.client.screen.base.IRelicScreenProvider;
import it.hurts.sskirillss.relics.client.screen.base.ITickingWidget;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractDescriptionWidget;
import it.hurts.sskirillss.relics.utils.Reference;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

public class ScrollbarWidget extends AbstractDescriptionWidget implements ITickingWidget {
    private final IRelicScreenProvider provider;
    private double scrollPosition = 0.0;
    private double prevScrollPosition = 0.0;
    private double scrollVelocity = 0.0;
    private static final double FRICTION = 0.35;
    private static final double SCROLL_SPEED = 0.045;

    private boolean dragging = false;
    private double dragOffsetY = 0.0;

    private static final int TRACK_WIDTH = 8;
    private static final int TRACK_HEIGHT = 57;
    private static final int SLIDER_WIDTH = 10;
    private static final int SLIDER_HEIGHT = 12;
    private static final int SLIDER_OFFSET_X = -1;
    private static final int SLIDER_OFFSET_Y = 0;

    public ScrollbarWidget(int x, int y, IRelicScreenProvider provider) {
        super(x, y, TRACK_WIDTH, TRACK_HEIGHT);
        this.provider = provider;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        double interpScroll = this.getScrollPosition(partialTick);

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();

        GUIRenderer.begin(
                        ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/general/top_scroll_bar.png"),
                        poseStack
                )
                .pos(this.getX(), this.getY() - 20)
                .anchor(SpriteAnchor.TOP_LEFT)
                .end();

        int usableTrackHeight = TRACK_HEIGHT - SLIDER_HEIGHT - 4;
        int sliderX = this.getX() + SLIDER_OFFSET_X;
        int sliderY = this.getY() + SLIDER_OFFSET_Y + (int) Math.round(interpScroll * usableTrackHeight);

        GUIRenderer.begin(
                        ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/general/scroll_bar_slider.png"),
                        poseStack
                )
                .pos(sliderX, sliderY)
                .anchor(SpriteAnchor.TOP_LEFT)
                .end();

        poseStack.popPose();
    }

    @Override
    public void onTick() {
        prevScrollPosition = scrollPosition;
        scrollPosition += scrollVelocity;
        scrollVelocity *= FRICTION;

        if (Math.abs(scrollVelocity) < 0.0005) {
            scrollVelocity = 0.0;
        }

        if (scrollPosition < 0.0) {
            scrollPosition = 0.0;
            scrollVelocity = 0.0;
        } else if (scrollPosition > 1.0) {
            scrollPosition = 1.0;
            scrollVelocity = 0.0;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        if (!isMouseOver(mouseX, mouseY)) {
            return false;
        }
        double currentSliderTopY = getSliderTopY();
        if (mouseX >= this.getX() + SLIDER_OFFSET_X
                && mouseX <= this.getX() + SLIDER_OFFSET_X + SLIDER_WIDTH
                && mouseY >= currentSliderTopY
                && mouseY <= currentSliderTopY + SLIDER_HEIGHT) {
            this.dragOffsetY = mouseY - currentSliderTopY;
            this.dragging = true;
            updateScrollPositionFromMouse(mouseX, mouseY);
            this.scrollVelocity = 0.0;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!dragging || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        updateScrollPositionFromMouse(mouseX, mouseY);
        this.scrollVelocity = 0.0;
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        if (dragging) {
            dragging = false;
            return true;
        }
        return false;
    }

    private void updateScrollPositionFromMouse(double mouseX, double mouseY) {
        double trackStartY = this.getY() + SLIDER_OFFSET_Y;
        double usableTrackHeight = TRACK_HEIGHT - SLIDER_HEIGHT - 4;
        double relativeY = (mouseY - dragOffsetY) - trackStartY;
        double newPos = relativeY / usableTrackHeight;
        newPos = Math.max(0.0, Math.min(1.0, newPos));
        this.scrollPosition = newPos;
    }

    private double getSliderTopY() {
        int usableTrackHeight = TRACK_HEIGHT - SLIDER_HEIGHT;
        return this.getY() + SLIDER_OFFSET_Y + (this.scrollPosition * usableTrackHeight);
    }

    public double getScrollPosition(float partialTicks) {
        return Mth.lerp(partialTicks, prevScrollPosition, scrollPosition);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!isMouseOver(mouseX, mouseY)) {
            return false;
        }
        this.scrollVelocity -= scrollY * SCROLL_SPEED;
        return true;
    }
}