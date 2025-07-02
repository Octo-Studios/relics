package it.hurts.sskirillss.relics.items.relics.base.data.style;

import lombok.Builder;
import lombok.Data;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.function.BiFunction;

@Data
@Builder(toBuilder = true)
@Deprecated(forRemoval = true)
public class StyleTemplate {
    @Builder.Default
    private BiFunction<Player, ItemStack, TooltipData> tooltip;

    public static class StyleTemplateBuilder {
        private BiFunction<Player, ItemStack, TooltipData> tooltip = (player, stack) -> TooltipData.builder().build();

        public StyleTemplateBuilder tooltip(TooltipData tooltip) {
            return tooltip((player, stack) -> tooltip);
        }

        public StyleTemplateBuilder tooltip(BiFunction<Player, ItemStack, TooltipData> tooltip) {
            this.tooltip = tooltip;

            return this;
        }
    }
}