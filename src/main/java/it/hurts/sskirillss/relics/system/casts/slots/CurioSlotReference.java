package it.hurts.sskirillss.relics.system.casts.slots;

import it.hurts.sskirillss.relics.system.casts.slots.base.SlotReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurioSlotReference extends SlotReference {
    private int index;

    private String type;

    @Override
    public ItemStack gatherStack(Player player) {
        return CuriosApi.getCuriosInventory(player).map(itemHandler -> {
            IDynamicStackHandler stackHandler = itemHandler.getCurios().get(getType()).getStacks();

            int index = getIndex();

            return index > stackHandler.getSlots() - 1 ? ItemStack.EMPTY : stackHandler.getStackInSlot(index);
        }).orElse(ItemStack.EMPTY);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = super.serializeNBT();

        tag.putInt("index", index);
        tag.putString("type", type);

        return tag;
    }

    @Override
    public SlotReference deserializeNBT(CompoundTag tag) {
        this.index = tag.getInt("index");
        this.type = tag.getString("type");

        return this;
    }
}