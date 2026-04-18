package it.hurts.sskirillss.relics.network.packets.item.jellyfish_necklace;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.entities.ChainedElectricityEntity;
import it.hurts.sskirillss.relics.init.RelicsDataComponents;
import it.hurts.sskirillss.relics.init.RelicsEntities;
import it.hurts.sskirillss.relics.items.relics.belt.KineticBeltItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import top.theillusivec4.curios.api.CuriosApi;

public record C2SChainedElectricityPacket(double x, double y, double z, float damage, int lifetime, boolean flawless, String slotId, int slotIndex) implements CustomPacketPayload {
    public static final Type<C2SChainedElectricityPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "jellyfish_necklace/chained_electricity"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SChainedElectricityPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buffer, packet) -> {
                        buffer.writeDouble(packet.x);
                        buffer.writeDouble(packet.y);
                        buffer.writeDouble(packet.z);
                        buffer.writeFloat(packet.damage);
                        buffer.writeInt(packet.lifetime);
                        buffer.writeBoolean(packet.flawless);
                        buffer.writeUtf(packet.slotId);
                        buffer.writeInt(packet.slotIndex);
                    },
                    buffer -> new C2SChainedElectricityPacket(
                            buffer.readDouble(),
                            buffer.readDouble(),
                            buffer.readDouble(),
                            buffer.readFloat(),
                            buffer.readInt(),
                            buffer.readBoolean(),
                            buffer.readUtf(),
                            buffer.readInt()
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(C2SChainedElectricityPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player) || player.level().isClientSide())
                return;

            var level = player.level();
            var position = new Vec3(packet.x, packet.y, packet.z);
            var slot = CuriosApi.getCuriosInventory(player).flatMap(inventory -> inventory.findCurio(packet.slotId, packet.slotIndex));

            if (slot.isEmpty())
                return;

            var stack = slot.get().stack();

            if (!(stack.getItem() instanceof KineticBeltItem))
                return;

            ChainedElectricityEntity previous = null;
            var previousId = stack.getOrDefault(RelicsDataComponents.KINETIC_BELT_LAST_ELECTRICITY_ID, -1);
            var bestDistance = Double.MAX_VALUE;

            if (previousId >= 0) {
                var raw = level.getEntity(previousId);

                if (raw instanceof ChainedElectricityEntity candidate && candidate.isAlive() && candidate.getOwner() == player) {
                    bestDistance = candidate.position().distanceToSqr(position);

                    if (bestDistance < 1D)
                        return;

                    if (bestDistance <= 100D)
                        previous = candidate;
                }
            }

            var electricity = new ChainedElectricityEntity(RelicsEntities.KINETIC_ELECTRICITY.get(), player.level());

            electricity.setLifetime(Math.max(1, packet.lifetime));
            electricity.setDamage(Math.max(0F, packet.damage));
            electricity.setFlawless(packet.flawless);
            electricity.setPos(position);
            electricity.setOwner(player);

            if (previous != null && bestDistance <= 100D)
                electricity.setPreviousEntity(previous);

            level.addFreshEntity(electricity);

            stack.set(RelicsDataComponents.KINETIC_BELT_LAST_ELECTRICITY_ID, electricity.getId());
        });
    }
}
