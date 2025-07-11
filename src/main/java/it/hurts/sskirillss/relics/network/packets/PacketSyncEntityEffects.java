package it.hurts.sskirillss.relics.network.packets;

import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.Relics;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.function.IntFunction;

@Data
@AllArgsConstructor
public class PacketSyncEntityEffects implements CustomPacketPayload {
    private final CompoundTag data;
    private final Action action;
    private final int entity;

    public static final CustomPacketPayload.Type<PacketSyncEntityEffects> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "sync_entity_effect"));

    public static final StreamCodec<ByteBuf, PacketSyncEntityEffects> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, PacketSyncEntityEffects::getData,
            ByteBufCodecs.idMapper(Action.BY_ID, Action::getId), PacketSyncEntityEffects::getAction,
            ByteBufCodecs.INT, PacketSyncEntityEffects::getEntity,
            PacketSyncEntityEffects::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft MC = Minecraft.getInstance();
            ClientLevel level = MC.level;

            if (level == null || !(level.getEntity(this.entity) instanceof LivingEntity entity))
                return;

            MobEffectInstance effect = MobEffectInstance.load(data);

            if (effect != null) {
                switch (action) {
                    case ADD, UPDATE -> entity.addEffect(effect);
                    case REMOVE -> entity.removeEffect(effect.getEffect());
                }
            }
        });
    }

    @Getter
    @AllArgsConstructor
    public enum Action {
        ADD(0),
        REMOVE(1),
        UPDATE(2);

        public static final IntFunction<Action> BY_ID = ByIdMap.continuous(Action::getId, Action.values(), ByIdMap.OutOfBoundsStrategy.ZERO);

        private final int id;
    }
}