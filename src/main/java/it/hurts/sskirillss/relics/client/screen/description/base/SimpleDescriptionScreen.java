package it.hurts.sskirillss.relics.client.screen.description.base;

import it.hurts.sskirillss.relics.client.screen.base.IAutoScaledScreen;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SimpleDescriptionScreen extends Screen implements IAutoScaledScreen {
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

        this.stack = DescriptionUtils.gatherRelicStack(player, slot);
    }

    @Override
    public void tick() {
        super.tick();

        var player = Minecraft.getInstance().player;

        if (player != null) {
            this.stack = DescriptionUtils.gatherRelicStack(player, this.slot);

            if (this.screen instanceof DescriptionScreen subScreen)
                subScreen.stack = DescriptionUtils.gatherRelicStack(player, this.slot);
        }
    }

    @Override
    public int getAutoScale() {
        return 4;
    }
}