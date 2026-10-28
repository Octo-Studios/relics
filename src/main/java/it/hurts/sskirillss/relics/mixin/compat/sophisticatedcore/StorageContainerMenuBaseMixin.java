package it.hurts.sskirillss.relics.mixin.compat.sophisticatedcore;

import it.hurts.sskirillss.relics.api.events.utility.ContainerSlotClickEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ResultSlot;
import net.neoforged.neoforge.common.NeoForge;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StorageContainerMenuBase.class)
public class StorageContainerMenuBaseMixin {
    @Inject(method = "doClick", at = @At("HEAD"), cancellable = true)
    private void relics$onClick(int index, int button, ClickType clickType, Player player, CallbackInfo ci) {
        if (index < 0)
            return;

        if (clickType != ClickType.PICKUP && clickType != ClickType.QUICK_MOVE && clickType != ClickType.SWAP && clickType != ClickType.THROW)
            return;

        var menu = (AbstractContainerMenu) (Object) this;

        if (index >= menu.slots.size())
            return;

        var slot = menu.slots.get(index);

        var shouldProcess = !(slot instanceof ResultSlot) && slot.isActive() && (
                clickType == ClickType.THROW
                        ? slot.mayPickup(player)
                        : slot.allowModification(player) && menu.canTakeItemForPickAll(menu.getCarried(), slot)
        );

        if (!shouldProcess)
            return;

        var event = new ContainerSlotClickEvent(player, menu, slot, clickType, button == 0 ? ClickAction.PRIMARY : ClickAction.SECONDARY, button, menu.getCarried(), slot.getItem());

        NeoForge.EVENT_BUS.post(event);

        if (event.isCanceled())
            ci.cancel();
    }
}
