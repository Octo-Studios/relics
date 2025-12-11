package it.hurts.sskirillss.relics.dev.anchor;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@Data
@AllArgsConstructor
public class PositionAnchor extends Anchor {
    private Vec3 position;

    @Override
    public AnchorType getType() {
        return AnchorType.POSITION;
    }

    @Override
    public Vec3 getPosition(Level level) {
        return this.getPosition();
    }
}
