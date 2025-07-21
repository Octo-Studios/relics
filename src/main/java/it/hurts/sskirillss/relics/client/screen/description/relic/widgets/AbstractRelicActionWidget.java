package it.hurts.sskirillss.relics.client.screen.description.relic.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.client.screen.base.IHoverableWidget;
import it.hurts.sskirillss.relics.client.screen.base.ITickingWidget;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractDescriptionWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
import it.hurts.sskirillss.relics.client.screen.description.relic.RelicDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.relic.particles.ExperienceParticleData;
import it.hurts.sskirillss.relics.client.screen.utils.ParticleStorage;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.leveling.PacketRelicTweak;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class AbstractRelicActionWidget extends AbstractDescriptionWidget implements IHoverableWidget, ITickingWidget {
    @Getter
    private final PacketRelicTweak.Operation operation;
    @Getter
    private final RelicDescriptionScreen screen;

    public AbstractRelicActionWidget(int x, int y, PacketRelicTweak.Operation operation, RelicDescriptionScreen screen) {
        super(x, y, 14, 14);

        this.operation = operation;
        this.screen = screen;
    }

    @Override
    public abstract boolean isLocked();

    @Override
    public void onPress() {
        if (!isLocked())
            NetworkHandler.sendToServer(new PacketRelicTweak(this.getScreen().getContainer(), getScreen().getSlot(), this.getOperation(), Screen.hasShiftDown()));
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        var poseStack = guiGraphics.pose();

        var actionId = this.getOperation().toString().toLowerCase(Locale.ROOT);

        GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/relic/" + actionId + "_button_" + (this.isLocked() ? "inactive" : "active") + ".png"), poseStack)
                .anchor(SpriteAnchor.TOP_LEFT)
                .pos(this.getX(), this.getY())
                .end();

        if (this.isHoveredOrFocused())
            GUIRenderer.begin(DescriptionTextures.ACTION_BUTTON_OUTLINE, poseStack)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .pos(this.getX() - 1, this.getY() - 1)
                    .end();
    }

    @Override
    public void onTick() {
        if (minecraft.player == null)
            return;

        RandomSource random = minecraft.player.getRandom();

        if (!isHovered() || minecraft.player.tickCount % 5 != 0)
            return;

        ParticleStorage.addParticle((Screen) screen, new ExperienceParticleData(
                new Color(200 + random.nextInt(50), 150 + random.nextInt(100), 0),
                getX() + random.nextInt(width), getY() + random.nextInt(height / 4),
                1F + (random.nextFloat() * 0.25F), 50 + random.nextInt(50)));
    }

    public List<MutableComponent> buildDescription() {
        return new ArrayList<>();
    }
}