package it.hurts.sskirillss.relics.badges.ability.base;

import it.hurts.sskirillss.relics.badges.base.AbilityBadge;
import it.hurts.sskirillss.relics.init.HotkeyRegistry;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastType;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CastTypeBadge extends AbilityBadge {
    @Getter
    private final CastType type;

    public CastTypeBadge(CastType type) {
        super(type.name().toLowerCase(Locale.ROOT));

        this.type = type;
    }

    @Override
    public List<MutableComponent> getHint(ItemStack stack, String ability) {
        return Arrays.asList(Component.translatable("tooltip.relics.researching.badge.ability.cast_type.hint", HotkeyRegistry.ACTIVE_ABILITIES_LIST.getKey().getDisplayName()));
    }

    @Override
    public boolean isVisible(ItemStack stack, String ability) {
        if (!(stack.getItem() instanceof IRelicItem relic))
            return false;

        return relic.getAbilityCastData(ability).getType() == type;
    }
}