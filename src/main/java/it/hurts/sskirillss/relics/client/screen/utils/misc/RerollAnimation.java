package it.hurts.sskirillss.relics.client.screen.utils.misc;

import it.hurts.sskirillss.relics.api.relics.data.AbilityData;
import lombok.Getter;
import net.minecraft.util.Mth;

public class RerollAnimation {
    public static final int DURATION = 12;
    public static final int TINT_DOWN = 0xFF5A4A;

    private static final int STAR_FLASH_DURATION = 4;
    private static final float STAR_FLASH_STRENGTH = 0.9F;

    private static final int TINT_MAX = UIParticles.GOLD;
    private static final int TINT_UP = 0x7CFF6B;
    private static final int TINT_SAME = 0xCFE3FF;

    private String ability = null;
    private Integer signature = null;
    private int quality = 0;

    @Getter
    private int age = DURATION;
    @Getter
    private int outcome = 0;
    @Getter
    private int oldQuality = 0;
    @Getter
    private int newQuality = 0;
    @Getter
    private boolean maxed = false;

    public boolean isActive() {
        return age < DURATION;
    }

    public void tick() {
        if (age < DURATION)
            age++;
    }

    public float starFlash(float partialTick) {
        if (age >= STAR_FLASH_DURATION)
            return 0F;

        return Mth.clamp(1F - (age + partialTick) / STAR_FLASH_DURATION, 0F, 1F) * STAR_FLASH_STRENGTH;
    }

    public int tint() {
        if (maxed)
            return TINT_MAX;

        return outcome > 0 ? TINT_UP : outcome < 0 ? TINT_DOWN : TINT_SAME;
    }

    public boolean observe(AbilityData data, String ability) {
        var template = data.getTemplate();

        if (template == null)
            return false;

        var newSignature = 1;

        for (var stat : template.getStats().keySet())
            newSignature = newSignature * 31 + data.getStatData(stat).getInitialQuality();

        var newQuality = data.calculateQuality();

        var changed = this.signature != null && ability.equals(this.ability) && newSignature != this.signature;

        if (changed) {
            this.outcome = Integer.compare(newQuality, this.quality);
            this.oldQuality = this.quality;
            this.newQuality = newQuality;
            this.maxed = newQuality >= data.getMaxQuality();
            this.age = 0;
        } else if (!ability.equals(this.ability))
            this.age = DURATION;

        this.ability = ability;
        this.signature = newSignature;
        this.quality = newQuality;

        return changed;
    }
}
