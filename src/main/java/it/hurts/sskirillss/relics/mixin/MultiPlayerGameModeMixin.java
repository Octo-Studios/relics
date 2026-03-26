package it.hurts.sskirillss.relics.mixin;

import it.hurts.sskirillss.relics.api.events.utility.ContainerSlotClickEvent;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ResultSlot;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
    @Inject(method = "handleInventoryMouseClick", at = @At("HEAD"), cancellable = true)
    private void relics$onInventoryClick(int containerId, int index, int button, ClickType clickType, Player player, CallbackInfo ci) {
        if (index < 0)
            return;

        if (clickType != ClickType.PICKUP && clickType != ClickType.QUICK_MOVE && clickType != ClickType.SWAP)
            return;

        var menu = player.containerMenu;

        if (containerId != menu.containerId)
            return;

        if (index >= menu.slots.size())
            return;

        var slot = menu.slots.get(index);

        if (!(slot instanceof ResultSlot) && slot.allowModification(player) && menu.canTakeItemForPickAll(menu.getCarried(), slot) && slot.isActive()) {
            var event = new ContainerSlotClickEvent(player, menu, slot, clickType, button == 0 ? ClickAction.PRIMARY : ClickAction.SECONDARY, menu.getCarried(), slot.getItem());

            NeoForge.EVENT_BUS.post(event);

            if (event.isCanceled())
                ci.cancel();
        }
    }
}