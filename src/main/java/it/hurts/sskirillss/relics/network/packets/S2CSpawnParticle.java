package it.hurts.sskirillss.relics.network.packets;

import it.hurts.sskirillss.relics.utils.Reference;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.joml.Vector3f;

@Data
@AllArgsConstructor
public class S2CSpawnParticle implements CustomPacketPayload {
    private final ParticleOptions particle;
    private final Vector3f pos;
    private final Vector3f motion;

    public static final Type<S2CSpawnParticle> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MODID, "spawn_particle"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CSpawnParticle> STREAM_CODEC = StreamCodec.composite(
            ParticleTypes.STREAM_CODEC, S2CSpawnParticle::getParticle,
            ByteBufCodecs.VECTOR3F, S2CSpawnParticle::getPos,
            ByteBufCodecs.VECTOR3F, S2CSpawnParticle::getMotion,
            S2CSpawnParticle::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> ctx.player().level().addParticle(this.getParticle(), this.getPos().x(), this.getPos().y(), this.getPos().z(), this.getMotion().x(), this.getMotion().y(), this.getMotion().z()));
    }
}