package it.hurts.sskirillss.relics.client.screen.description.research.particles;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.client.screen.description.general.particles.base.ParticleData;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.awt.*;

public class SmokeParticleData extends ParticleData {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/relic/particles/smoke.png");

    private final float fadeInPercentage;

    public SmokeParticleData(float xStart, float yStart, float scale, int lifeTime, float fadeInPercentage) {
        super(TEXTURE, new Color(1F, 1F, 1F), xStart, yStart, scale, lifeTime);

        this.fadeInPercentage = fadeInPercentage;
    }

    @Override
    public void tick(Screen screen) {
        super.tick(screen);

        LocalPlayer player = screen.getMinecraft().player;

        if (player == null)
            return;

        var lifePercentage = (float) getLifeTime() / getMaxLifeTime();

        setX(getX() + getDeltaX() * lifePercentage);
        setY(getY() + getDeltaY() * lifePercentage);
    }

    @Override
    public void render(Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        var lifePercentage = (float) getLifeTime() / getMaxLifeTime();

        float maxScale = getScale();
        float ratio = 1F - lifePercentage;
        float scale = ratio <= fadeInPercentage ? maxScale * (ratio / fadeInPercentage) : maxScale;

        poseStack.pushPose();

        RenderSystem.setShaderColor(getColor().getRed() / 255F, getColor().getGreen() / 255F, getColor().getBlue() / 255F, lifePercentage * 0.75F);

        RenderSystem.enableBlend();

        poseStack.translate(Mth.lerp(partialTick, getXO(), getX()), Mth.lerp(partialTick, getYO(), getY()), 0);

        poseStack.mulPose(Axis.ZP.rotationDegrees((getLifeTime() + partialTick) * lifePercentage));

        GUIRenderer.begin(TEXTURE, guiGraphics.pose())
                .scale(scale)
                .anchor(SpriteAnchor.CENTER)
                .end();

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        RenderSystem.disableBlend();

        poseStack.popPose();
    }
}