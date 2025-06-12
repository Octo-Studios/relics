package it.hurts.sskirillss.relics.client.screen.description.relic.widgets;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.description.ability.AbilityDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.ability.widgets.base.AbstractAbilityActionWidget;
import it.hurts.sskirillss.relics.client.screen.description.relic.RelicDescriptionScreen;
import it.hurts.sskirillss.relics.init.SoundRegistry;
import it.hurts.sskirillss.relics.network.packets.leveling.PacketAbilityTweak;
import it.hurts.sskirillss.relics.network.packets.leveling.PacketRelicTweak;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;

public class RankupRelicActionWidget extends AbstractRelicActionWidget {
    public RankupRelicActionWidget(int x, int y, RelicDescriptionScreen screen) {
        super(x, y, PacketRelicTweak.Operation.RANKUP, screen);
    }

    @Override
    public boolean isLocked() {
        return !(getScreen().getStack().getItem() instanceof IRelicItem relic) || !relic.mayPlayerRankup(minecraft.player, getScreen().getStack());
    }

    @Override
    public void playDownSound(SoundManager handler) {
        if (!isLocked())
            handler.play(SimpleSoundInstance.forUI(SoundRegistry.TABLE_RESET.get(), 1F));
    }

    @Override
    public void onHovered(GuiGraphics guiGraphics, int mouseX, int mouseY) {

    }
}