package it.hurts.sskirillss.relics.dev.shake.dev;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@Data
@AllArgsConstructor
public class PositionShakeAnchor extends ShakeAnchor {
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
