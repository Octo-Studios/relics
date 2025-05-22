package it.hurts.sskirillss.relics.entities;

import it.hurts.sskirillss.relics.init.RelicsEntities;
import it.hurts.sskirillss.relics.init.RelicsItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ThrownRelicExperienceBottle extends ThrowableItemProjectile {
    public ThrownRelicExperienceBottle(EntityType<? extends ThrownRelicExperienceBottle> type, Level level) {
        super(type, level);
    }

    @Override
    protected Item getDefaultItem() {
        return RelicsItems.RELIC_EXPERIENCE_BOTTLE.get();
    }

    @Override
    protected double getDefaultGravity() {
        return 0.07D;
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        if (this.level() instanceof ServerLevel) {
            this.level().levelEvent(2002, this.blockPosition(), PotionContents.getColor(Potions.LUCK));

            int steps = 10 + random.nextInt(10);

            for (int i = 0; i < steps; i++) {
                RelicExperienceOrbEntity orb = new RelicExperienceOrbEntity(RelicsEntities.RELIC_EXPERIENCE_ORB.get(), this.level());

                orb.setExperience(1 + random.nextInt(3));
                orb.setPos(Vec3.atCenterOf(this.blockPosition()));
                orb.setDeltaMovement(
                        (-1 + 2 * random.nextFloat()) * 0.15F,
                        0.1F + random.nextFloat() * 0.2F,
                        (-1 + 2 * random.nextFloat()) * 0.15F
                );

                this.level().addFreshEntity(orb);
            }

            this.discard();
        }
    }

//    @Override
//    public Packet<ClientGamePacketListener> getAddEntityPacket() {
//        return NetworkHooks.getEntitySpawningPacket(this);
//    }
}