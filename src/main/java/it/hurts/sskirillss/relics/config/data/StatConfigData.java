package it.hurts.sskirillss.relics.config.data;

import it.hurts.octostudios.octolib.module.config.annotation.Prop;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.AbilityStatTemplate;
import it.hurts.sskirillss.relics.init.RelicsRegistries;
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

    @Prop(comment = "Lower limit of the stat after scaling")
    private double minThresholdValue;
    @Prop(comment = "Upper limit of the stat after scaling")
    private double maxThresholdValue;

    @Prop(comment = "Scaling model for the stat as the relic gains levels.")
    private String scalingModel;
    @Prop(comment = "Target value at maximum stat quality and the last level of the final rank.")
    private double targetValue;

    public AbilityStatTemplate toData(IRelicItem relic, String ability, String stat) {
        return relic.getDefaultStatTemplate(ability, stat).toBuilder()
                .initialValue(minInitialValue, maxInitialValue)
                .thresholdValue(minThresholdValue, maxThresholdValue)
                .targetValue(RelicsRegistries.SCALING_MODEL_REGISTRY.get(ResourceLocation.parse(scalingModel)), targetValue)
                .build();
    }
}
