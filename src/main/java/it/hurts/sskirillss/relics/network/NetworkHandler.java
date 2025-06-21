package it.hurts.sskirillss.relics.network;

import it.hurts.sskirillss.relics.network.packets.PacketItemActivation;
import it.hurts.sskirillss.relics.network.packets.PacketPlayerMotion;
import it.hurts.sskirillss.relics.network.packets.PacketSyncEntityEffects;
import it.hurts.sskirillss.relics.network.packets.leveling.PacketRelicTweak;
import it.hurts.sskirillss.relics.network.packets.sync.S2CEntityMotionPacket;
import it.hurts.sskirillss.relics.network.packets.abilities.SpellCastPacket;
import it.hurts.sskirillss.relics.network.packets.capability.CapabilitySyncPacket;
import it.hurts.sskirillss.relics.network.packets.leveling.FixLevelingPoints;
import it.hurts.sskirillss.relics.network.packets.leveling.PacketAbilityTweak;
import it.hurts.sskirillss.relics.network.packets.lock.PacketAbilityUnlock;
import it.hurts.sskirillss.relics.network.packets.research.PacketManageLink;
import it.hurts.sskirillss.relics.network.packets.research.PacketResearchHint;
import it.hurts.sskirillss.relics.network.packets.sync.S2CSyncEntityTargetPacket;
import it.hurts.sskirillss.relics.utils.Reference;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class NetworkHandler {
    @SubscribeEvent
    public static void onRegisterPayloadHandler(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(Reference.MODID)
                .versioned("1.0")
                .optional();

        registrar.playToClient(PacketPlayerMotion.TYPE, PacketPlayerMotion.STREAM_CODEC, PacketPlayerMotion::handle);
        registrar.playToClient(PacketItemActivation.TYPE, PacketItemActivation.STREAM_CODEC, PacketItemActivation::handle);
        registrar.playToServer(PacketAbilityTweak.TYPE, PacketAbilityTweak.STREAM_CODEC, PacketAbilityTweak::handle);
        registrar.playToClient(PacketSyncEntityEffects.TYPE, PacketSyncEntityEffects.STREAM_CODEC, PacketSyncEntityEffects::handle);
        registrar.playToClient(CapabilitySyncPacket.TYPE, CapabilitySyncPacket.STREAM_CODEC, CapabilitySyncPacket::handle);
        registrar.playToServer(SpellCastPacket.TYPE, SpellCastPacket.STREAM_CODEC, SpellCastPacket::handle);
        registrar.playToClient(S2CSyncEntityTargetPacket.TYPE, S2CSyncEntityTargetPacket.STREAM_CODEC, S2CSyncEntityTargetPacket::handle);
        registrar.playToServer(PacketAbilityUnlock.TYPE, PacketAbilityUnlock.STREAM_CODEC, PacketAbilityUnlock::handle);
        registrar.playToServer(PacketManageLink.TYPE, PacketManageLink.STREAM_CODEC, PacketManageLink::handle);
        registrar.playToServer(PacketResearchHint.TYPE, PacketResearchHint.STREAM_CODEC, PacketResearchHint::handle);
        registrar.playToServer(FixLevelingPoints.TYPE, FixLevelingPoints.STREAM_CODEC, FixLevelingPoints::handle);
        registrar.playToServer(PacketRelicTweak.TYPE, PacketRelicTweak.STREAM_CODEC, PacketRelicTweak::handle);

        registrar.playToClient(S2CEntityMotionPacket.TYPE, S2CEntityMotionPacket.STREAM_CODEC, S2CEntityMotionPacket::handle);
    }

    public static <MSG extends CustomPacketPayload> void sendToServer(MSG message) {
        PacketDistributor.sendToServer(message);
    }

    public static <MSG extends CustomPacketPayload> void sendToClient(MSG message, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, message);
    }

    public static <MSG extends CustomPacketPayload> void sendToClientsTrackingEntity(MSG message, Entity entity) {
        PacketDistributor.sendToPlayersTrackingEntity(entity, message);
    }

    public static <MSG extends CustomPacketPayload> void sendToClientsTrackingEntityAndSelf(MSG message, Entity entity) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, message);
    }
}