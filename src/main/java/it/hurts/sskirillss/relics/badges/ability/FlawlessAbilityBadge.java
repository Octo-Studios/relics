package it.hurts.sskirillss.relics.badges.ability;

import it.hurts.sskirillss.relics.badges.base.AbilityBadge;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class FlawlessAbilityBadge extends AbilityBadge {
    public FlawlessAbilityBadge() {
        super("flawless_ability");
    }

    @Override
    public boolean isVisible(LivingEntity entity, ItemStack stack, String ability) {
        if (!(stack.getItem() instanceof IRelicItem relic))
            return false;

        return relic.isAbilityFlawless(entity, stack, ability);
    }
}