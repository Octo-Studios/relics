package it.hurts.sskirillss.relics.items.relics.base.data.style;

import lombok.Builder;
import lombok.Data;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.function.BiFunction;

@Data
@Builder
public class StyleTemplate {
    @Builder.Default
    private BiFunction<Player, ItemStack, TooltipData> tooltip;

    @Builder.Default
    private BiFunction<Player, ItemStack, BeamsData> beams;

    public static class StyleTemplateBuilder {
        private BiFunction<Player, ItemStack, TooltipData> tooltip = (player, stack) -> TooltipData.builder().build();
        private BiFunction<Player, ItemStack, BeamsData> beams = (player, stack) -> BeamsData.builder().build();

        public StyleTemplateBuilder tooltip(TooltipData tooltip) {
            return tooltip((player, stack) -> tooltip);
        }

        public StyleTemplateBuilder tooltip(BiFunction<Player, ItemStack, TooltipData> tooltip) {
            this.tooltip = tooltip;

            return this;
        }

        public StyleTemplateBuilder beams(BeamsData beams) {
            return beams((player, stack) -> beams);
        }

        public StyleTemplateBuilder beams(BiFunction<Player, ItemStack, BeamsData> beams) {
            this.beams = beams;

            return this;
        }
    }
}