package it.hurts.sskirillss.relics.client.screen.description.relic.widgets;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.client.screen.base.IHoverableWidget;
import it.hurts.sskirillss.relics.client.screen.base.ITickingWidget;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractDescriptionWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.client.screen.description.relic.RelicDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.relic.particles.ExperienceParticleData;
import it.hurts.sskirillss.relics.client.screen.utils.ParticleStorage;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;
import java.util.List;

public abstract class AbstractRelicActionWidget extends AbstractDescriptionWidget implements IHoverableWidget, ITickingWidget {
    private final String actionId;
    @Getter
    private final RelicDescriptionScreen screen;

    protected AbstractRelicActionWidget(int x, int y, String actionId, RelicDescriptionScreen screen) {
        super(x, y, 14, 14);

        this.actionId = actionId;
        this.screen = screen;
    }

    @Override
    public abstract boolean isLocked();

    @Override
    public abstract void onPress();

    protected boolean isQuickAction() {
        return false;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        var poseStack = guiGraphics.pose();
        var locked = isLocked();
        var quick = !locked && isQuickAction();

        var brightness = quick ? (float) (1.05F + Math.sin((minecraft.player.tickCount + pPartialTick) * 0.5F) * 0.1F) : 1F;

        GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/relic/" + actionId + "_button_" + (locked ? "inactive" : quick ? "quick" : "active") + ".png"), poseStack)
                .anchor(SpriteAnchor.TOP_LEFT)
                .color(brightness, brightness, brightness, 1F)
                .pos(this.getX(), this.getY())
                .end();

        if (this.isHovered())
            GUIRenderer.begin(DescriptionTextures.ACTION_BUTTON_OUTLINE, poseStack)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .pos(this.getX() - 1, this.getY() - 1)
                    .end();
    }

    @Override
    public void onTick() {
        if (minecraft.player == null)
            return;

        var random = minecraft.player.getRandom();

        if (!isHovered() || minecraft.player.tickCount % 5 != 0)
            return;

        ParticleStorage.addParticle(screen, new ExperienceParticleData(
                new Color(200 + random.nextInt(50), 150 + random.nextInt(100), 0),
                getX() + random.nextInt(width), getY() + random.nextInt(height / 4),
                1F + (random.nextFloat() * 0.25F), 50 + random.nextInt(50)));
    }

    public abstract List<MutableComponent> buildDescription();

    @Override
    public void onHovered(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        DescriptionUtils.drawActionTooltip(guiGraphics, minecraft.font, buildDescription(), getX() + width + 1, mouseY);
    }
}
