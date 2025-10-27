package it.hurts.sskirillss.relics.dev.shake.dev;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

@Data
@AllArgsConstructor
public class EntityShakeAnchor extends ShakeAnchor {
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