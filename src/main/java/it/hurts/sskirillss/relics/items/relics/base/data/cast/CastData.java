package it.hurts.sskirillss.relics.items.relics.base.data.cast;

import it.hurts.sskirillss.relics.init.RelicsRelicContainer;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.containers.base.RelicContainer;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastType;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.PredicateType;
import lombok.Builder;
import lombok.Data;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

@Data
@Builder
public class CastData {
    @Builder.Default
    private List<RelicContainer> containers;

    @Builder.Default
    private CastType type = CastType.NONE;

    @Builder.Default
    private Map<String, Pair<PredicateType, BiFunction<Player, ItemStack, Boolean>>> predicates;

    public static class CastDataBuilder {
        private Map<String, Pair<PredicateType, BiFunction<Player, ItemStack, Boolean>>> predicates = new HashMap<>();

        private List<RelicContainer> containers = List.of(RelicsRelicContainer.CURIOS.get());

        public CastDataBuilder predicate(String id, PredicateType type, BiFunction<Player, ItemStack, Boolean> predicate) {
            predicates.put(id, Pair.of(type, predicate));

            return this;
        }

        public CastDataBuilder container(RelicContainer... containers) {
            this.containers = Arrays.asList(containers);

            return this;
        }
    }
}