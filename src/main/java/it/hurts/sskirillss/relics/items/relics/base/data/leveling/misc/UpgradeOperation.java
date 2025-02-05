package it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc;

import lombok.AllArgsConstructor;

/**
 * @deprecated Use BiFunctions instead
 */
@Deprecated
@AllArgsConstructor
public enum UpgradeOperation {
    ADD,
    MULTIPLY_BASE,
    MULTIPLY_TOTAL,
    CUSTOM;
}