package it.hurts.sskirillss.relics.api.relics.abilities;

import com.mojang.datafixers.util.Function3;
import it.hurts.sskirillss.relics.config.data.AbilityConfigData;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.CastData;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.research.ResearchTemplate;
import lombok.Builder;
import lombok.Data;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Builder(toBuilder = true)
public class AbilityTemplate {
    private final String id;

    public static AbilityTemplateBuilder builder(String id) {
        AbilityTemplateBuilder builder = new AbilityTemplateBuilder();

        builder.id(id);

        return builder;
    }

    @Builder.Default
    private Function3<Player, ItemStack, String, String> icon = (player, stack, ability) -> ability;

    @Builder.Default
    private Map<String, StatTemplate> stats;

    @Builder.Default
    private int maxLevel = 10;

    @Builder.Default
    private int requiredLevel = 0;

    @Builder.Default
    private int requiredPoints = 1;

    @Builder.Default
    private CastData castData;

    @Builder.Default
    private ResearchTemplate researchTemplate;

    public AbilityConfigData toConfigData() {
        return new AbilityConfigData(requiredPoints, requiredLevel, maxLevel, stats.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toConfigData(), (o1, o2) -> o1, LinkedHashMap::new)));
    }

    public static class AbilityTemplateBuilder {
        private Map<String, StatTemplate> stats = new LinkedHashMap<>();
        private CastData castData = CastData.builder().build();
        private ResearchTemplate researchData = ResearchTemplate.builder().build();

        private AbilityTemplateBuilder castData(CastData data) {
            return this;
        }

        private AbilityTemplateBuilder researchData(ResearchTemplate data) {
            return this;
        }

        public AbilityTemplateBuilder research(ResearchTemplate data) {
            this.researchData = data;

            return this;
        }

        public AbilityTemplateBuilder active(CastData data) {
            this.castData = data;

            return this;
        }

        public AbilityTemplateBuilder stat(StatTemplate stat) {
            this.stats.put(stat.getId(), stat);

            return this;
        }

        private AbilityTemplateBuilder id(String id) {
            this.id = id;

            return this;
        }
    }
}