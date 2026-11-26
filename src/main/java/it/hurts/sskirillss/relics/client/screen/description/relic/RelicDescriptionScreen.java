package it.hurts.sskirillss.relics.client.screen.description.relic;

import com.mojang.blaze3d.platform.InputConstants;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.description.DescriptionCategories;
import it.hurts.sskirillss.relics.api.relics.description.DescriptionCategory;
import it.hurts.sskirillss.relics.api.relics.description.DescriptionSubcategories;
import it.hurts.sskirillss.relics.api.relics.description.DescriptionSubcategory;
import it.hurts.sskirillss.relics.client.screen.base.IHoverableWidget;
import it.hurts.sskirillss.relics.client.screen.base.IPagedDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.base.ITabbedDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.RelicIntroScrollbarWidget;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.ScrollbarWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.BigRelicCardWidget;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.DismantleRelicActionWidget;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.RankupRelicActionWidget;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.RelicIntroContainerWidget;
import it.hurts.sskirillss.relics.client.screen.utils.misc.DismantleEffect;
import it.hurts.sskirillss.relics.client.screen.utils.misc.RankupEffect;
import it.hurts.sskirillss.relics.utils.RenderUtils;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RelicDescriptionScreen extends DescriptionScreen implements ITabbedDescriptionScreen, IPagedDescriptionScreen {
    @Getter
    @Setter
    private DescriptionSubcategory subcategory = DescriptionSubcategories.getSubcategory("relic_description");

    @Getter
    private final RankupEffect rankupEffect = new RankupEffect();
    @Getter
    private final DismantleEffect dismantleEffect = new DismantleEffect(this);
    private BigRelicCardWidget relicCard;

    public RelicDescriptionScreen(Player player, int container, int slot, Screen screen) {
        super(player, container, slot, screen);
    }

    @Override
    public void tick() {
        dismantleEffect.tick();

        if (dismantleEffect.isFinished()) {
            onClose();
            return;
        }

        super.tick();
        rankupEffect.tick();
    }

    @Override
    protected ItemStack getDisplayedStack(ItemStack live) {
        return dismantleEffect.displayedStack(live);
    }

    @Override
    public void removed() {
        dismantleEffect.reset();

        super.removed();
    }

    @Override
    protected void init() {
        super.init();

        if (!(stack.getItem() instanceof IRelicItem))
            return;

        relicCard = this.addRenderableWidget(new BigRelicCardWidget(x + 59, y + 43, this));

        var introContainer = new RelicIntroContainerWidget(this);

        this.addRenderableWidget(introContainer);
        this.addRenderableWidget(new RelicIntroScrollbarWidget(x + 279, y + 173, introContainer));

        var container = subcategory.getContainerWidget(this);

        this.addRenderableWidget(container);
        this.addRenderableWidget(new ScrollbarWidget(x + 279, y + 74, container));

        this.addRenderableWidget(new RankupRelicActionWidget(x + 289, y + 70, this));
        this.addRenderableWidget(new DismantleRelicActionWidget(x + 289, y + 98, this));
    }

    @Override
    protected float getSpaceBackgroundBrightness() {
        return 1F + dismantleEffect.backgroundFlash(RenderUtils.getPartialTick(false));
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderBackground(guiGraphics, pMouseX, pMouseY, pPartialTick);

        var poseStack = guiGraphics.pose();

        poseStack.pushPose();

        poseStack.scale(0.75F, 0.75F, 1F);

        guiGraphics.drawString(minecraft.font, Component.literal(stack.getDisplayName().getString().replace("[", "").replace("]", ""))
                .withStyle(ChatFormatting.BOLD), (int) ((x + 114) * 1.33F), (int) ((y + 62) * 1.33F), DescriptionUtils.TEXT_COLOR, false);

        poseStack.popPose();

        poseStack.pushPose();

        GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/top_background_delimiter.png"), poseStack)
                .anchor(SpriteAnchor.TOP_LEFT)
                .pos(x + 107, y + 70)
                .end();

        poseStack.popPose();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        var poseStack = guiGraphics.pose();

        poseStack.pushPose();
        poseStack.translate(rankupEffect.shakeX(pPartialTick), rankupEffect.shakeY(pPartialTick), 0F);

        super.render(guiGraphics, pMouseX, pMouseY, pPartialTick);

        if (dismantleEffect.isFinishing())
            relicCard.renderItem(guiGraphics, pPartialTick);
        else {
            for (var listener : this.children()) {
                if (listener instanceof AbstractButton button && button.isHovered()
                        && button instanceof IHoverableWidget widget) {
                    poseStack.translate(0, 0, 100);
                    widget.onHovered(guiGraphics, pMouseX, pMouseY);
                }
            }
        }
        poseStack.popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return dismantleEffect.isCommitted() || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (minecraft.options.keyInventory.isActiveAndMatches(InputConstants.getKey(pKeyCode, pScanCode))) {
            this.onClose();

            return true;
        }

        return dismantleEffect.isCommitted() && CommonInputs.selected(pKeyCode) || super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(dismantleEffect.isCommitted() ? getReturnScreen() : screen);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public DescriptionCategory getCategory() {
        return DescriptionCategories.getCategory("relic");
    }
}
