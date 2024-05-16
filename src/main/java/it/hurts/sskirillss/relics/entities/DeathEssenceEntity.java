package it.hurts.sskirillss.relics.entities;

import it.hurts.sskirillss.relics.init.EntityRegistry;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class DeathEssenceEntity extends ThrowableProjectile {
    @Setter
    @Getter
    private float damage;

    private static LivingEntity target;

    public DeathEssenceEntity(EntityType<? extends DeathEssenceEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    public DeathEssenceEntity(LivingEntity throwerIn, LivingEntity targetIn, float damage) {
        super(EntityRegistry.DEATH_ESSENCE.get(), throwerIn.getCommandSenderWorld());

        this.setOwner(throwerIn);

        target = targetIn;

        this.damage = damage;
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide() || target == null)
            return;

        double size = 0.02D + damage * 0.001D;

        ((ServerLevel) level()).sendParticles(ParticleUtils.constructSimpleSpark(new Color(Color.DARK_GRAY.getRGB()), 0.1F + (damage * 0.01F), 20 + Math.round(damage * 0.025F), 0.9F),
                this.xo, this.yo, this.zo, 1, size, size, size, 0.01F + damage * 0.0001F);

        if (target.isDeadOrDying()) {
            this.remove(RemovalReason.KILLED);

            return;
        }

        for (DeathEssenceEntity essence : level().getEntitiesOfClass(DeathEssenceEntity.class, this.getBoundingBox().inflate(damage * 0.05F))) {
            if (essence.getStringUUID().equals(this.getStringUUID()) || (essence.getOwner() instanceof Player p1
                    && this.getOwner() instanceof Player p2 && !p1.getStringUUID().equals(p2.getStringUUID())))
                continue;

            setDamage(getDamage() + essence.getDamage());

            essence.remove(RemovalReason.KILLED);
        }

        double distance = this.position().distanceTo(target.position().add(0, target.getBbHeight() / 2, 0));

        if (distance > 0.5) {
            if (distance > 32) {
                this.remove(RemovalReason.KILLED);

                return;
            }

            EntityUtils.moveTowardsPosition(this, target.position().add(0, target.getBbHeight() / 2, 0), 0.25F);
        } else {
            Level level = target.getCommandSenderWorld();

            target.hurt(level.damageSources().generic(), damage);

            this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}