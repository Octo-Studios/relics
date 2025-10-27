package it.hurts.sskirillss.relics.network.packets.item.springy_boot;

import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.dev.shake.Shake;
import it.hurts.sskirillss.relics.dev.shake.ShakeManager;
import it.hurts.sskirillss.relics.utils.MathUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.joml.Vector3f;

@Data
@AllArgsConstructor
public class S2CBounceFromSurface implements CustomPacketPayload {
    private final int id;
    private final Vector3f motion;

    public static final Type<S2CBounceFromSurface> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "springy_boot/bounce_from_surface"));

    public static final StreamCodec<ByteBuf, S2CBounceFromSurface> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, S2CBounceFromSurface::getId,
            ByteBufCodecs.VECTOR3F, S2CBounceFromSurface::getMotion,
            S2CBounceFromSurface::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var player = ctx.player();
            var level = player.level();
            var random = level.getRandom();
            var entity = level.getEntity(this.getId());

            if (entity == null)
                return;

            var motion = new Vec3(this.getMotion());
            var speed = Math.abs(motion.y());

            entity.setDeltaMovement(motion);

            for (float i = 0; i < speed * 5F; i += 0.1F) {
                var angle = random.nextFloat() * Math.PI * 2;
                var radius = Math.sqrt(random.nextFloat()) * speed * 0.15F;

                var dx = Math.cos(angle) * radius;
                var dz = Math.sin(angle) * radius;

                level.addParticle(ParticleTypes.CLOUD, entity.getX(), entity.getY(), entity.getZ(), dx, random.nextFloat() * 0.15F, dz);
            }

            for (int i = 0; i < 50 * speed; i += 1) {
                var particleMotion = motion.normalize().scale(random.nextFloat());

                level.addParticle(ParticleTypes.CLOUD, entity.getX() + MathUtils.randomFloat(random) * 0.5F, entity.getY(), entity.getZ() + MathUtils.randomFloat(random) * 0.5F, particleMotion.x(), particleMotion.y(), particleMotion.z());
            }

            ShakeManager.add(level, Shake.builder(player)
                    .amplitude(0, 0, (float) (0.075F * speed))
                    .duration((int) (15 * speed))
                    .radius(Integer.MAX_VALUE)
                    .build());
        });
    }

}