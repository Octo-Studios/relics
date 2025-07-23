package it.hurts.sskirillss.relics.client.screen.description.ability.widgets.base;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.client.screen.base.IHoverableWidget;
import it.hurts.sskirillss.relics.client.screen.base.ITickingWidget;
import it.hurts.sskirillss.relics.client.screen.description.ability.AbilityDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractDescriptionWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
import it.hurts.sskirillss.relics.client.screen.description.relic.particles.ExperienceParticleData;
import it.hurts.sskirillss.relics.client.screen.utils.ParticleStorage;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.leveling.PacketAbilityTweak;
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

public abstract class AbstractAbilityActionWidget extends AbstractDescriptionWidget implements IHoverableWidget {
    @Getter
    private final PacketAbilityTweak.Operation operation;
    @Getter
    private final AbilityDescriptionScreen screen;

    public AbstractAbilityActionWidget(int x, int y, PacketAbilityTweak.Operation operation, AbilityDescriptionScreen screen) {
        super(x, y, 14, 14);

        this.operation = operation;
        this.screen = screen;
    }

    @Override
    public abstract boolean isLocked();

    public String getAbility() {
        return this.getScreen().getSelectedAbility();
    }

    @Override
    public void onPress() {
        if (!this.isLocked())
            NetworkHandler.sendToServer(new PacketAbilityTweak(this.getScreen().getContainer(), getScreen().getSlot(), getAbility(), this.getOperation(), Screen.hasShiftDown()));
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        var poseStack = guiGraphics.pose();

        var actionId = this.getOperation().toString().toLowerCase(Locale.ROOT);

        GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/ability/" + actionId + "_button_" + (this.isLocked() ? "inactive" : "active") + ".png"), poseStack)
                .anchor(SpriteAnchor.TOP_LEFT)
                .pos(this.getX(), this.getY())
                .end();

        if (this.isHovered())
            GUIRenderer.begin(DescriptionTextures.ACTION_BUTTON_OUTLINE, poseStack)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .pos(this.getX() - 1, this.getY() - 1)
                    .end();
    }

    public List<MutableComponent> buildDescription() {
        return new ArrayList<>();
    }
}