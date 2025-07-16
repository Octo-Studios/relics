package it.hurts.sskirillss.relics.network.packets.description;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public interface IRelicValidator {
    default void causeError(Player player) {
        player.displayClientMessage(Component.translatable("info.relics.researching.wrong_container").withStyle(ChatFormatting.RED), false);

        player.closeContainer();
    }
}