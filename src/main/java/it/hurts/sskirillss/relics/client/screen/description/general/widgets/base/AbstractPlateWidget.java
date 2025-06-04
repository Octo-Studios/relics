package it.hurts.sskirillss.relics.client.screen.description.general.widgets.base;

import com.mojang.blaze3d.vertex.PoseStack;
import it.hurts.sskirillss.relics.client.screen.base.IHoverableWidget;
import it.hurts.sskirillss.relics.client.screen.base.IRelicScreenProvider;
import it.hurts.sskirillss.relics.client.screen.base.ITickingWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
import it.hurts.sskirillss.relics.utils.Reference;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public abstract class AbstractPlateWidget extends AbstractDescriptionWidget implements IHoverableWidget, ITickingWidget {
    @Getter
    private IRelicScreenProvider provider;
    @Getter
    private final String icon;

    public abstract String getValue(ItemStack stack);

    public AbstractPlateWidget(int x, int y, IRelicScreenProvider provider, String icon) {
        super(x, y, 55, 20);

        this.provider = provider;
        this.icon = icon;
    }

    @Override
    public final void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        PoseStack poseStack = guiGraphics.pose();

        poseStack.pushPose();

        poseStack.translate(getX() + Math.sin((minecraft.player.tickCount + pPartialTick + icon.length() * 10D) * 0.075D), getY() + Math.cos((minecraft.player.tickCount + pPartialTick + icon.length() * 10D) * 0.075D) * 0.5D, 0);

        GUIRenderer.begin(DescriptionTextures.PLATE_BACKGROUND, poseStack)
                .anchor(SpriteAnchor.TOP_LEFT)
                .end();

        GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/general/icons/" + icon + ".png"), poseStack)
                .anchor(SpriteAnchor.TOP_LEFT)
                .pos(5, 5)
                .end();

        MutableComponent value = Component.literal(getValue(provider.getStack())).withStyle(ChatFormatting.BOLD);

        guiGraphics.drawString(minecraft.font, value, 20, 7, 0xffe278, true);

        renderContent(guiGraphics, pMouseX, pMouseY, pPartialTick);

        if (isHovered())
            GUIRenderer.begin(DescriptionTextures.PLATE_OUTLINE, poseStack)
                    .pos(-1, -1)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .end();

        poseStack.popPose();
    }

    public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

    }

    @Override
    public void playDownSound(SoundManager handler) {

    }
}