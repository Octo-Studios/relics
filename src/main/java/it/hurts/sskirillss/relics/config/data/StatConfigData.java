package it.hurts.sskirillss.relics.config.data;

import it.hurts.octostudios.octolib.module.config.annotation.Prop;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.StatData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.UpgradeOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

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

    @Prop(comment = """
            Type of mathematical operation used to calculate the stat's value based on the ability level. Supported operations include:
            MULTIPLY_BASE: x + ((x * y) * z),
            MULTIPLY_TOTAL: x * (y + 1)^z,
            ADD: x + (y * z).
            
            ...where x - Base stat value, y - Value of [upgradeModifier], z - Current relic level
            """)
    private UpgradeOperation upgradeOperation;
    @Prop(comment = "Modifier applied to the base value of the stat, depending on the [upgradeOperation] parameter.")
    private double upgradeModifier;

    public StatData toData(IRelicItem relic, String ability, String stat) {
        StatData data = relic.constructDefaultRelicData().getAbilities().getAbilities().get(ability).getStats().get(stat);

        data.setInitialValue(Pair.of(minInitialValue, maxInitialValue));
        data.setThresholdValue(Pair.of(minThresholdValue, maxThresholdValue));
        data.setUpgradeModifier(Pair.of(upgradeOperation, upgradeModifier));

        return data;
    }
}