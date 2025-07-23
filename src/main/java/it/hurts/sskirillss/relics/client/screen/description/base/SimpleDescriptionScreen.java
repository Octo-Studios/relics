package it.hurts.sskirillss.relics.client.screen.description.base;

import it.hurts.sskirillss.relics.client.screen.base.IAutoScaledScreen;
import it.hurts.sskirillss.relics.client.screen.base.IRelicScreenProvider;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import lombok.Getter;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

// TODO: Get rid of IRelicScreenProvider, use DescriptionScreen instead
@OnlyIn(Dist.CLIENT)
public class SimpleDescriptionScreen extends Screen implements IRelicScreenProvider, IAutoScaledScreen {
    public final Screen screen;

    @Getter
    public final int container;
    @Getter
    public final int slot;
    @Getter
    public ItemStack stack;

    protected SimpleDescriptionScreen(Player player, int container, int slot, Screen screen) {
        super(Component.empty());

        this.container = container;
        this.slot = slot;
        this.screen = screen;

        stack = DescriptionUtils.gatherRelicStack(player, slot);
    }

    @Override
    public int getAutoScale() {
        return 4;
    }
}