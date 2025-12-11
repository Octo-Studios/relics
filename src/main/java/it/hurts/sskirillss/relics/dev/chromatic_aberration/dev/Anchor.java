package it.hurts.sskirillss.relics.dev.chromatic_aberration.dev;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public abstract class Anchor {
    public abstract AnchorType getType();

    public abstract Vec3 getPosition(Level level);

    public enum AnchorType {
        ENTITY,
        POSITION
    }
}