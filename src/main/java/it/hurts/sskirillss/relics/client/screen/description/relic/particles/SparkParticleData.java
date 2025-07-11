package it.hurts.sskirillss.relics.client.screen.description.relic.particles;

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
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class SparkParticleData extends ParticleData {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/particles/pixel.png");

    public SparkParticleData(Color color, float xStart, float yStart, float scale, int lifeTime) {
        super(TEXTURE, color, xStart, yStart, scale, lifeTime);
    }

    @Override
    public void tick(Screen screen) {
        super.tick(screen);

        LocalPlayer player = screen.getMinecraft().player;

        if (player == null)
            return;

        var lifePercentage = (float) getLifeTime() / getMaxLifeTime();

        setX(getX() + getDeltaX() * lifePercentage);
        setY(getY() + getDeltaY() * lifePercentage + (getMaxLifeTime() - getLifeTime()));
    }

    @Override
    public void render(Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        var lifePercentage = (float) getLifeTime() / getMaxLifeTime();

        float blinkOffset = 0.25F + (float) (Math.sin(getLifeTime() + partialTick) * 0.5F);

        poseStack.pushPose();

        RenderSystem.setShaderColor(getColor().getRed() / 255F + blinkOffset, getColor().getGreen() / 255F + blinkOffset, getColor().getBlue() / 255F + blinkOffset, lifePercentage);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        poseStack.translate(Mth.lerp(partialTick, getXO(), getX()), Mth.lerp(partialTick, getYO(), getY()), 0);

        poseStack.mulPose(Axis.ZP.rotationDegrees((getLifeTime() + partialTick) * 5));

        GUIRenderer.begin(TEXTURE, guiGraphics.pose())
                .scale(getScale() * lifePercentage)
                .anchor(SpriteAnchor.CENTER)
                .end();

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();

        poseStack.popPose();
    }
}