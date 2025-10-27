package it.hurts.sskirillss.relics.dev.shake.dev;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public abstract class ShakeAnchor {
    public abstract AnchorType getType();

    public abstract Vec3 getPosition(Level level);

    public enum AnchorType {
        ENTITY,
        POSITION
    }
}