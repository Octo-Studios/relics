package it.hurts.sskirillss.relics.config.data;

import it.hurts.octostudios.octolib.modules.config.annotations.Prop;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.init.RegistryRegistry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.resources.ResourceLocation;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatConfigData {
    @Prop(comment = "Minimum base value of the stat. A random value within this range is assigned when the relic is first created")
    private double minInitialValue;
    @Prop(comment = "Maximum base value of the stat. A random value within this range is assigned when the relic is first created")
    private double maxInitialValue;

    @Prop(comment = "Minimum threshold value for the stat, representing hard limits that cannot be surpassed through ability level upgrades or other methods")
    private double minThresholdValue;
    @Prop(comment = "Maximum threshold value for the stat, representing hard limits that cannot be surpassed through ability level upgrades or other methods")
    private double maxThresholdValue;

    @Prop(comment = "Type of mathematical operation used to calculate the stat's value based on the ability level.")
    private String upgradeOperation;
    @Prop(comment = "Modifier applied to the base value of the stat, depending on the [upgradeOperation] parameter.")
    private double upgradeModifier;

    public StatTemplate toData(IRelicItem relic, String ability, String stat) {
        return relic.getDefaultStatTemplate(ability, stat).toBuilder()
                .initialValue(minInitialValue, maxInitialValue)
                .thresholdValue(minThresholdValue, maxThresholdValue)
                .upgradeModifier(RegistryRegistry.SCALING_MODEL_REGISTRY.get(ResourceLocation.parse(upgradeOperation)), upgradeModifier)
                .build();
    }
}