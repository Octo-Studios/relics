package it.hurts.sskirillss.relics.mixin;

import it.hurts.sskirillss.relics.api.events.utility.ContainerSlotClickEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ResultSlot;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin {
    @Inject(at = @At(value = "HEAD"), method = "doClick", cancellable = true)
    protected void onClick(int index, int action, ClickType clickType, Player player, CallbackInfo ci) {
        if (index < 0)
            return;

        if (clickType != ClickType.PICKUP && clickType != ClickType.QUICK_MOVE && clickType != ClickType.SWAP)
            return;

        var menu = (AbstractContainerMenu) (Object) this;

        if (index >= menu.slots.size())
            return;

        var slot = menu.slots.get(index);

        if (!(slot instanceof ResultSlot) && slot.allowModification(player) && menu.canTakeItemForPickAll(menu.getCarried(), slot) && slot.isActive()) {
            var event = new ContainerSlotClickEvent(player, menu, slot, clickType, action == 0 ? ClickAction.PRIMARY : ClickAction.SECONDARY, menu.getCarried(), slot.getItem());

            NeoForge.EVENT_BUS.post(event);

            if (event.isCanceled())
                ci.cancel();
        }
    }
}