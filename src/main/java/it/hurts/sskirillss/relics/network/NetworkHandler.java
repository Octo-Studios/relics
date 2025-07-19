package it.hurts.sskirillss.relics.network;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.network.packets.PacketItemActivation;
import it.hurts.sskirillss.relics.network.packets.PacketSyncEntityEffects;
import it.hurts.sskirillss.relics.network.packets.S2CSetEntityMotion;
import it.hurts.sskirillss.relics.network.packets.S2CSpawnParticle;
import it.hurts.sskirillss.relics.network.packets.abilities.SpellCastPacket;
import it.hurts.sskirillss.relics.network.packets.capability.CapabilitySyncPacket;
import it.hurts.sskirillss.relics.network.packets.description.ability.C2SChangeMode;
import it.hurts.sskirillss.relics.network.packets.item.kinetic_belt.C2SSetActive;
import it.hurts.sskirillss.relics.network.packets.item.midnight_mantle.S2CSyncConstellation;
import it.hurts.sskirillss.relics.network.packets.item.roller_skate.C2SCreateSpark;
import it.hurts.sskirillss.relics.network.packets.item.springy_boot.S2CBounceFromSurface;
import it.hurts.sskirillss.relics.network.packets.leveling.FixLevelingPoints;
import it.hurts.sskirillss.relics.network.packets.leveling.PacketAbilityTweak;
import it.hurts.sskirillss.relics.network.packets.leveling.PacketRelicTweak;
import it.hurts.sskirillss.relics.network.packets.lock.PacketAbilityUnlock;
import it.hurts.sskirillss.relics.network.packets.research.PacketManageLink;
import it.hurts.sskirillss.relics.network.packets.research.PacketResearchHint;
import it.hurts.sskirillss.relics.network.packets.sync.S2CSyncEntityTargetPacket;
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
        final PayloadRegistrar registrar = event.registrar(Relics.MODID)
                .versioned("1.0")
                .optional();

        registrar.playToClient(S2CSetEntityMotion.TYPE, S2CSetEntityMotion.STREAM_CODEC, S2CSetEntityMotion::handle);
        registrar.playToClient(S2CSpawnParticle.TYPE, S2CSpawnParticle.STREAM_CODEC, S2CSpawnParticle::handle);
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
        registrar.playToServer(C2SChangeMode.TYPE, C2SChangeMode.STREAM_CODEC, C2SChangeMode::handle);

        // === KINETIC BELT ===
        registrar.playToServer(C2SSetActive.TYPE, C2SSetActive.STREAM_CODEC, C2SSetActive::handle);

        // === SPRINGY BOOT ===
        registrar.playToClient(S2CBounceFromSurface.TYPE, S2CBounceFromSurface.STREAM_CODEC, S2CBounceFromSurface::handle);

        // === ROLLER SKATE ===
        registrar.playToServer(C2SCreateSpark.TYPE, C2SCreateSpark.STREAM_CODEC, C2SCreateSpark::handle);

        // === MIDNIGHT MANTLE ===
        registrar.playToClient(S2CSyncConstellation.TYPE, S2CSyncConstellation.STREAM_CODEC, S2CSyncConstellation::handle);
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