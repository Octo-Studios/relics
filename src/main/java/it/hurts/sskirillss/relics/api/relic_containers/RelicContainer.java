package it.hurts.sskirillss.relics.api.relic_containers;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class RelicContainer {
    public abstract Function<LivingEntity, List<ItemStack>> gatherRelics();

    public Function<Player, List<RelicStackReference>> gatherRelicReferences() {
        return player -> List.of();
    }

    public Function<Player, List<AbilityReference>> gatherAbilityReferences() {
        return player -> {
            List<AbilityReference> references = new ArrayList<>();

            for (var reference : this.gatherRelicReferences().apply(player)) {
                var stack = reference.getStack(player);

                if (!(stack.getItem() instanceof IRelicItem relic))
                    continue;

                var abilitiesData = relic.getRelicData(player, stack).getAbilitiesData();

                for (var abilityData : abilitiesData.getAbilities().values()) {
                    if (abilityData == null || !abilityData.isActive())
                        continue;

                    var containers = abilityData.getTemplate().getActivation().getContainers();

                    if (!containers.isEmpty() && !containers.contains(this))
                        continue;

                    references.add(new AbilityReference(abilityData.getId(), reference));
                }

                for (var synergyData : abilitiesData.getSynergies().values()) {
                    if (synergyData == null || !synergyData.isUnlocked() || synergyData.getTemplate().getModes().isEmpty())
                        continue;

                    references.add(new AbilityReference(synergyData.getId(), reference, AbilityReference.Type.SYNERGY));
                }
            }

            return references;
        };
    }
}
