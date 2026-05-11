package it.hurts.sskirillss.relics.api.relics.abilities.activation;

import it.hurts.sskirillss.relics.api.relics.data.AbilityData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record AbilityActivationContext(Player player, ItemStack stack, AbilityData abilityData, AbilityActivationType type, AbilityActivationStage stage) {
}
