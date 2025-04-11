package it.hurts.sskirillss.relics.client.screen.description.general.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.hurts.sskirillss.relics.client.screen.base.IHoverableWidget;
import it.hurts.sskirillss.relics.client.screen.base.IRelicScreenProvider;
import it.hurts.sskirillss.relics.client.screen.base.ITickingWidget;
import it.hurts.sskirillss.relics.client.screen.description.relic.particles.ExperienceParticleData;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractDescriptionWidget;
import it.hurts.sskirillss.relics.client.screen.utils.ParticleStorage;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.utils.data.AnimationData;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.util.RandomSource;

import java.awt.*;

public class LogoWidget extends AbstractDescriptionWidget implements IHoverableWidget, ITickingWidget {
    @Getter
    private IRelicScreenProvider provider;

    public LogoWidget(int x, int y, IRelicScreenProvider provider) {
        super(x, y, 54, 14);

        this.provider = provider;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null || !(provider.getStack().getItem() instanceof IRelicItem relic))
            return;

        PoseStack poseStack = guiGraphics.pose();

        poseStack.pushPose();

        float color = (float) (1.05F + (Math.sin(player.tickCount * 0.25F) * 0.1F));

        RenderSystem.setShaderColor(color, color, color, 1F);

        RenderSystem.setShaderTexture(0, DescriptionTextures.LOGO);

        poseStack.translate(getX() + Math.sin((minecraft.player.tickCount + pPartialTick) * 0.075F), getY() + Math.cos((minecraft.player.tickCount + pPartialTick) * 0.075F) * 0.5F, 0);

        GUIRenderer.begin(DescriptionTextures.LOGO, poseStack)
                .animation(AnimationData.builder()
                        .frame(0, 2).frame(1, 2).frame(2, 2)
                        .frame(3, 2).frame(4, 2).frame(5, 2)
                        .frame(6, 2).frame(7, 2).frame(8, 2)
                        .frame(9, 2).frame(10, 2).frame(11, 2)
                        .frame(12, 2).frame(13, 2).frame(14, 2)
                        .frame(15, 20))
                .anchor(SpriteAnchor.TOP_LEFT)
                .texSize(width, height * 16)
                .patternSize(width, height)
                .end();

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        poseStack.popPose();
    }

    @Override
    public void onTick() {
        if (!(provider.getStack().getItem() instanceof IRelicItem relic) || minecraft.player == null)
            return;

        RandomSource random = minecraft.player.getRandom();

        if (minecraft.player.tickCount % 5 == 0) {
            ParticleStorage.addParticle((Screen) provider, new ExperienceParticleData(new Color(200 + random.nextInt(50), 150 + random.nextInt(100), 0),
                    getX() + random.nextInt(width), getY() + random.nextInt(3), 1F + (random.nextFloat() * 0.25F), 50 + random.nextInt(50)));
        }
    }

    @Override
    public void onHovered(GuiGraphics guiGraphics, int mouseX, int mouseY) {

    }

    @Override
    public void playDownSound(SoundManager handler) {

    }
}