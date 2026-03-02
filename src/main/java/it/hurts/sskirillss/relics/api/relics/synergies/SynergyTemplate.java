package it.hurts.sskirillss.relics.api.relics.synergies;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Function3;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.synergies.conditions.AbilityConditionTemplate;
import it.hurts.sskirillss.relics.api.relics.synergies.conditions.RelicConditionTemplate;
import it.hurts.sskirillss.relics.api.relics.synergies.stats.SynergyStatTemplate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SynergyTemplate {
    private final String id;

    private final Function3<Player, ItemStack, String, String> icon;
    private final Map<String, SynergyStatTemplate> stats;
    private final List<String> modes;
    private final Multimap<Integer, String> rankModifiers;
    private final List<RelicConditionTemplate> relicConditions;

    public static SynergyTemplateBuilder builder(String id) {
        return new SynergyTemplateBuilder(id);
    }

    public SynergyTemplateBuilder toBuilder() {
        return new SynergyTemplateBuilder(this);
    }

    public static class SynergyTemplateBuilder {
        private final String id;

        private Function3<Player, ItemStack, String, String> icon = (player, stack, synergy) -> {
            var relic = (IRelicItem) stack.getItem();
            var synergyData = relic.getRelicData(player, stack).getAbilitiesData().getSynergyData(synergy);

            if (synergyData == null)
                return synergy;

            var modes = synergyData.getTemplate().getModes();

            return synergy + (modes.isEmpty() ? "" : "_" + synergyData.getMode());
        };
        private Map<String, SynergyStatTemplate> stats = new LinkedHashMap<>();
        private List<String> modes = new ArrayList<>();
        private Multimap<Integer, String> rankModifiers = LinkedHashMultimap.create();
        private List<RelicConditionTemplate> relicConditions = new ArrayList<>();

        public SynergyTemplateBuilder(String id) {
            this.id = id;
        }

        private SynergyTemplateBuilder(SynergyTemplate base) {
            this.id = base.getId();

            this.icon = base.getIcon();
            this.stats = new LinkedHashMap<>(base.getStats());
            this.modes = base.getModes();
            this.rankModifiers = base.getRankModifiers();
            this.relicConditions = new ArrayList<>(base.getRelicConditions());
        }

        public SynergyTemplateBuilder icon(Function3<Player, ItemStack, String, String> icon) {
            this.icon = icon;

            return this;
        }

        public SynergyTemplateBuilder stats(Map<String, SynergyStatTemplate> stats) {
            this.stats = stats;

            return this;
        }

        public SynergyTemplateBuilder stat(SynergyStatTemplate stat) {
            this.stats.put(stat.getId(), stat);

            return this;
        }

        public SynergyTemplateBuilder modes(String... mode) {
            this.modes.addAll(Lists.newArrayList(mode));

            return this;
        }

        public SynergyTemplateBuilder rankModifier(int rank, String modifier) {
            this.rankModifiers.put(rank, modifier);

            return this;
        }

        public SynergyTemplateBuilder conditions(List<RelicConditionTemplate> relicConditions) {
            this.relicConditions = relicConditions;

            return this;
        }

        public SynergyTemplateBuilder condition(RelicConditionTemplate condition) {
            this.relicConditions.add(condition);

            return this;
        }

        public SynergyTemplate build() {
            return new SynergyTemplate(id, icon, stats, modes, rankModifiers, relicConditions);
        }
    }
}
