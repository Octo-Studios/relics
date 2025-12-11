package it.hurts.sskirillss.relics.dev.anchor;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@Data
@AllArgsConstructor
public class EntityAnchor extends Anchor {
    private int id;

    @Override
    public Vec3 getPosition(Level level) {
        var entity = level.getEntity(this.getId());

        if (entity == null)
            return Vec3.ZERO;

        return entity.position();
    }

    @Override
    public AnchorType getType() {
        return AnchorType.ENTITY;
    }
}