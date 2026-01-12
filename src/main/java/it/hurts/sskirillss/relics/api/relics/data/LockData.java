package it.hurts.sskirillss.relics.api.relics.data;

import it.hurts.sskirillss.relics.api.relics.LockComponent;
import net.minecraft.util.Mth;

public class LockData {
    private final AbilityData abilityData;

    public LockData(AbilityData abilityData) {
        this.abilityData = abilityData;
    }

    public LockComponent getComponent() {
        return abilityData.getComponent().getLock();
    }

    public void setComponent(LockComponent component) {
        abilityData.setComponent(abilityData.getComponent().toBuilder()
                .lock(component)
                .build());
    }

    public int getMaxUnlocks() {
        return 5;
    }

    public int getUnlocks() {
        return getComponent().getUnlocks();
    }

    public void setUnlocks(int unlocks) {
        setComponent(getComponent().toBuilder()
                .unlocks(Mth.clamp(unlocks, 0, getMaxUnlocks()))
                .build());
    }

    public void addUnlocks(int unlocks) {
        setUnlocks(getUnlocks() + unlocks);
    }

    public boolean isUnlocked() {
        return getUnlocks() >= getMaxUnlocks();
    }
}
