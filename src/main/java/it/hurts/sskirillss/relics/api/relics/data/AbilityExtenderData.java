package it.hurts.sskirillss.relics.api.relics.data;

import it.hurts.sskirillss.relics.api.relics.abilities.AbilityExtenderComponent;

public class AbilityExtenderData {
    private final AbilityData abilityData;

    public AbilityExtenderData(AbilityData abilityData) {
        this.abilityData = abilityData;
    }

    public AbilityExtenderComponent getComponent() {
        return abilityData.getComponent().getExtender();
    }

    public void setComponent(AbilityExtenderComponent component) {
        abilityData.setComponent(abilityData.getComponent().toBuilder()
                .extender(component)
                .build());
    }

    public int getCooldownCap() {
        return getComponent().getCooldownCap();
    }

    public void setCooldownCap(int amount) {
        setComponent(getComponent().toBuilder()
                .cooldownCap(amount)
                .build());
    }

    public int getCooldown() {
        return getComponent().getCooldown();
    }

    public void setCooldown(int amount) {
        setComponent(getComponent().toBuilder()
                .cooldownCap(amount)
                .cooldown(amount)
                .build());
    }

    public void addCooldown(int amount) {
        setComponent(getComponent().toBuilder()
                .cooldown(getCooldown() + amount)
                .build());
    }

    public void setTicking(boolean ticking) {
        setComponent(getComponent().toBuilder()
                .ticking(ticking)
                .build());
    }

    public boolean isTicking() {
        return getComponent().isTicking();
    }
}
