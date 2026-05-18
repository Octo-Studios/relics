package it.hurts.sskirillss.relics.client.screen.description.relic;

import com.mojang.blaze3d.platform.InputConstants;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.description.DescriptionCategories;
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
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.RankupRelicActionWidget;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.RelicIntroContainerWidget;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RelicDescriptionScreen extends DescriptionScreen implements ITabbedDescriptionScreen, IPagedDescriptionScreen {
    @Getter
    @Setter
    private DescriptionSubcategory subcategory = DescriptionSubcategories.getSubcategory("relic_description");

    public RelicDescriptionScreen(Player player, int container, int slot, Screen screen) {
        super(player, container, slot, screen);
    }

    @Override
    protected void init() {
        super.init();

        if (this.stack == null || !(this.stack.getItem() instanceof IRelicItem relic))
            return;

        this.addRenderableWidget(new BigRelicCardWidget(x + 59, y + 43, this));

        var introContainer = new RelicIntroContainerWidget(this);

        this.addRenderableWidget(introContainer);
        this.addRenderableWidget(new RelicIntroScrollbarWidget(x + 279, y + 173, introContainer));

        var container = subcategory.getContainerWidget(this);

        this.addRenderableWidget(container);
        this.addRenderableWidget(new ScrollbarWidget(x + 279, y + 74, container));

        this.addRenderableWidget(new RankupRelicActionWidget(x + 289, y + 84, this));
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
        super.render(guiGraphics, pMouseX, pMouseY, pPartialTick);

        for (GuiEventListener listener : this.children()) {
            if (listener instanceof AbstractButton button && button.isHovered()
                    && button instanceof IHoverableWidget widget) {
                guiGraphics.pose().translate(0, 0, 100);

                widget.onHovered(guiGraphics, pMouseX, pMouseY);
            }
        }
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (minecraft.options.keyInventory.isActiveAndMatches(InputConstants.getKey(pKeyCode, pScanCode))) {
            this.onClose();

            return true;
        }

        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public void onClose() {
        screen.rebuildWidgets();

        Minecraft.getInstance().setScreen(screen);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public it.hurts.sskirillss.relics.api.relics.description.DescriptionCategory getCategory() {
        return DescriptionCategories.getCategory("relic");
    }
}
