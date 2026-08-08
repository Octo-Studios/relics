package it.hurts.sskirillss.relics.api.relics.abilities.stats.misc;

import it.hurts.sskirillss.relics.api.scaling_models.ScalingModel;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TargetValue {
    private ScalingModel scalingModel;
    private double targetValue;
}
