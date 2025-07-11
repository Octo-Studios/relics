package it.hurts.sskirillss.relics.api.relics.description;

import it.hurts.sskirillss.relics.Relics;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

@Data
@AllArgsConstructor
public abstract class OperationCondition {
    private final String id;

    private final OperationType type;

    public abstract boolean isMet(LivingEntity entity, ItemStack stack);

    public Component getTitle(LivingEntity entity, ItemStack stack) {
        return Component.translatable(Relics.MODID + ".operation_condition." + this.getType().getCategory().getId() + "." + this.getType().getId() + "." + this.getId());
    }
}