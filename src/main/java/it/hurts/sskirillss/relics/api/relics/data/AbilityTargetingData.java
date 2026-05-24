package it.hurts.sskirillss.relics.api.relics.data;

import it.hurts.sskirillss.relics.api.relics.AbilityTargetingComponent;
import it.hurts.sskirillss.relics.api.relics.abilities.targeting.AbilityTargetingOption;
import it.hurts.sskirillss.relics.api.relics.abilities.targeting.SelectorType;
import it.hurts.sskirillss.relics.handlers.PlayerAbilityTargetingHandler;
import it.hurts.sskirillss.relics.init.RelicsAttachments;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

public class AbilityTargetingData {
    private final AbilityData abilityData;

    public AbilityTargetingData(AbilityData abilityData) {
        this.abilityData = abilityData;
    }

    public AbilityData getAbilityData() {
        return this.abilityData;
    }

    public AbilityTargetingComponent getComponent() {
        var entity = this.getAbilityData().getAbilitiesData().getRelicData().getEntity();

        if (!(entity instanceof Player player))
            return AbilityTargetingComponent.EMPTY;

        return player.getData(RelicsAttachments.PLAYER_ABILITY_TARGETING)
                .getAbilities()
                .getOrDefault(this.getTargetingKey(), AbilityTargetingComponent.EMPTY);
    }

    public boolean getOption(AbilityTargetingOption option) {
        var template = this.getAbilityData().getTemplate();
        var targeting = template == null ? null : template.getTargeting();

        if (targeting == null || !targeting.hasOption(option))
            return true;

        return this.getOption(option, targeting.getSelector());
    }

    public boolean getOption(AbilityTargetingOption option, SelectorType selectorType) {
        var template = this.getAbilityData().getTemplate();
        var targeting = template == null ? null : template.getTargeting();

        if (targeting == null || !targeting.hasOption(selectorType, option))
            return true;

        return this.getComponent().getModes()
                .getOrDefault(selectorType.getSerializedName(), Map.of())
                .getOrDefault(getOptionKey(option), option.getDefaultValue(selectorType));
    }

    public void setOption(AbilityTargetingOption option, boolean value) {
        this.setOption(option, this.getAbilityData().getTemplate().getTargeting().getSelector(), value);
    }

    public void setOption(AbilityTargetingOption option, SelectorType selectorType, boolean value) {
        var entity = this.getAbilityData().getAbilitiesData().getRelicData().getEntity();

        if (!(entity instanceof ServerPlayer player))
            return;

        var key = this.getTargetingKey();
        var playerTargeting = player.getData(RelicsAttachments.PLAYER_ABILITY_TARGETING);
        var abilities = new HashMap<>(playerTargeting.getAbilities());
        var modes = new HashMap<>(abilities.getOrDefault(key, AbilityTargetingComponent.EMPTY).getModes());
        var options = new HashMap<>(modes.getOrDefault(selectorType.getSerializedName(), Map.of()));

        options.put(getOptionKey(option), value);
        modes.put(selectorType.getSerializedName(), options);
        abilities.put(key, AbilityTargetingComponent.builder().modes(modes).build());

        PlayerAbilityTargetingHandler.set(player, playerTargeting.toBuilder().abilities(abilities).build());
    }

    public String getTargetingKey() {
        var stack = this.getAbilityData().getAbilitiesData().getRelicData().getStack();
        var itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();

        return itemId + "#" + this.getAbilityData().getId();
    }

    public static String getOptionKey(AbilityTargetingOption option) {
        return option.name().toLowerCase();
    }
}
