package it.hurts.sskirillss.relics.client.screen.description.synergy.widgets;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.base.ITickingWidget;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractDescriptionWidget;
import it.hurts.sskirillss.relics.client.screen.description.synergy.SynergyDescriptionScreen;
import it.hurts.sskirillss.relics.utils.RenderUtils;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

public class SynergyPageScrollbarWidget extends AbstractDescriptionWidget implements ITickingWidget {
    private final SynergyDescriptionScreen screen;

    private double scrollPosition;
    private double prevScrollPosition;
    private double targetScrollPosition;

    private boolean dragging = false;
    private double dragOffsetY = 0.0;

    private static final int TRACK_WIDTH = 10;
    private static final int TRACK_HEIGHT = 68;
    private static final int SLIDER_WIDTH = 10;
    private static final int SLIDER_HEIGHT = 12;
    private static final int SLIDER_OFFSET_X = -1;
    private static final int SLIDER_OFFSET_Y = -12;

    public SynergyPageScrollbarWidget(int x, int y, SynergyDescriptionScreen screen) {
        super(x, y, TRACK_WIDTH, TRACK_HEIGHT);

        this.screen = screen;

        this.scrollPosition = this.prevScrollPosition = this.targetScrollPosition = this.pageToScrollPosition(screen.getPage());
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var poseStack = guiGraphics.pose();

        poseStack.pushPose();

        GUIRenderer.begin(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/bottom_scroll_bar.png"), poseStack)
                .pos(this.getX(), this.getY() - 20)
                .anchor(SpriteAnchor.TOP_LEFT)
                .end();

        if (!this.isLocked()) {
            int usableTrackHeight = TRACK_HEIGHT - SLIDER_HEIGHT - 4;
            int sliderX = this.getX() + SLIDER_OFFSET_X;
            int sliderY = this.getY() + SLIDER_OFFSET_Y + (int) Math.round(this.getScrollPosition(RenderUtils.getPartialTick(false)) * usableTrackHeight);

            poseStack.translate(sliderX + SLIDER_WIDTH / 2F, sliderY, 0);

            var distance = Math.min(Math.abs(prevScrollPosition - scrollPosition) * 1.1F, 0.35F);

            poseStack.scale((float) (1 - distance), (float) (1 + (distance * 2.5F)), 1);

            GUIRenderer.begin(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/scroll_bar_slider.png"), poseStack)
                    .anchor(SpriteAnchor.CENTER)
                    .end();

            if (isHoveringSlider(mouseX, mouseY)) {
                GUIRenderer.begin(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/scroll_bar_slider_selection.png"), poseStack)
                        .anchor(SpriteAnchor.CENTER)
                        .end();
            }
        }

        poseStack.popPose();
    }

    @Override
    public void onTick() {
        this.prevScrollPosition = this.scrollPosition;
        this.scrollPosition = this.targetScrollPosition;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isLocked() || button != GLFW.GLFW_MOUSE_BUTTON_LEFT || (!isMouseOver(mouseX, mouseY) && !isHoveringSlider(mouseX, mouseY)))
            return false;

        this.dragOffsetY = this.isHoveringSlider(mouseX, mouseY) ? mouseY - this.getSliderCenterY() : 0D;
        this.dragging = true;

        this.updateScrollPositionFromMouse(mouseX, mouseY);
        this.targetScrollPosition = this.scrollPosition;

        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.isLocked() || !dragging || button != GLFW.GLFW_MOUSE_BUTTON_LEFT)
            return false;

        this.updateScrollPositionFromMouse(mouseX, mouseY);
        this.targetScrollPosition = this.scrollPosition;

        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.isLocked() || button != GLFW.GLFW_MOUSE_BUTTON_LEFT)
            return false;

        if (dragging) {
            dragging = false;

            this.applyPageFromScroll();

            return true;
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.isLocked() || !isMouseOver(mouseX, mouseY))
            return false;

        return this.handleScroll(scrollY, false);
    }

    public boolean handleExternalScroll(double scrollY) {
        if (this.isLocked())
            return false;

        return this.handleScroll(scrollY, true);
    }

    private void updateScrollPositionFromMouse(double mouseX, double mouseY) {
        var trackStartY = this.getY() + SLIDER_OFFSET_Y;
        var usableTrackHeight = TRACK_HEIGHT - SLIDER_HEIGHT - 4;
        var relativeCenterY = (mouseY - dragOffsetY) - trackStartY;

        this.scrollPosition = Math.clamp(relativeCenterY / usableTrackHeight, 0, 1);
    }

    private void applyPageFromScroll() {
        var totalPages = this.getTotalPages();

        if (totalPages <= 0)
            return;

        var newPage = totalPages <= 1 ? 0 : Mth.clamp((int) Math.round(this.scrollPosition * (totalPages - 1)), 0, totalPages - 1);

        this.setTargetPage(newPage);

        if (newPage != this.screen.getPage()) {
            this.screen.setPage(newPage);
            this.screen.rebuildWidgets();
        }
    }

    private int getTotalPages() {
        var player = minecraft.player;
        var stack = screen.getStack();

        if (!(stack.getItem() instanceof IRelicItem relic))
            return 0;

        var relicData = relic.getRelicData(player, stack);
        var synergies = relicData.getTemplate().getAbilities().getSynergies().keySet().stream()
                .filter(entry -> relicData.getAbilitiesData().getSynergyData(entry).isEnabled())
                .toList();

        var maxEntries = 4;
        var size = synergies.size();

        return (int) Math.ceil(size / (double) maxEntries);
    }

    private double pageToScrollPosition(int page) {
        var totalPages = this.getTotalPages();

        return totalPages <= 1 ? 0D : (double) Mth.clamp(page, 0, totalPages - 1) / (double) (totalPages - 1);
    }

    private void setTargetPage(int page) {
        this.targetScrollPosition = this.pageToScrollPosition(page);
        this.scrollPosition = this.targetScrollPosition;
        this.prevScrollPosition = this.scrollPosition;
    }

    public boolean isHoveringSlider(double mouseX, double mouseY) {
        var sliderTop = getSliderTopY();

        return mouseX >= this.getX() + SLIDER_OFFSET_X
                && mouseX <= this.getX() + SLIDER_OFFSET_X + SLIDER_WIDTH
                && mouseY >= sliderTop
                && mouseY <= sliderTop + SLIDER_HEIGHT;
    }

    private double getSliderCenterY() {
        var usableTrackHeight = TRACK_HEIGHT - SLIDER_HEIGHT - 4;

        return this.getY() + SLIDER_OFFSET_Y + (this.scrollPosition * usableTrackHeight);
    }

    private double getSliderTopY() {
        return this.getSliderCenterY() - (SLIDER_HEIGHT / 2D);
    }

    public double getScrollPosition(float partialTicks) {
        return this.scrollPosition;
    }

    @Override
    public boolean isLocked() {
        return this.getTotalPages() <= 1;
    }

    private boolean handleScroll(double scrollY, boolean fromExternal) {
        var totalPages = this.getTotalPages();

        if (totalPages <= 0)
            return false;

        var newPage = Mth.clamp(this.screen.getPage() - (int) Math.signum(scrollY), 0, totalPages - 1);

        if (newPage == this.screen.getPage())
            return false;

        this.setTargetPage(newPage);

        this.screen.setPage(newPage);
        this.screen.rebuildWidgets();

        return true;
    }
}
