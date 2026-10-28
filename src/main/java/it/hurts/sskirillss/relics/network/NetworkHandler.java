package it.hurts.sskirillss.relics.network;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.dev.chromatic_aberration.misc.ChromaticAberrationPacket;
import it.hurts.sskirillss.relics.dev.shake.misc.ShakePacket;
import it.hurts.sskirillss.relics.network.packets.PacketItemActivation;
import it.hurts.sskirillss.relics.network.packets.PacketSyncEntityEffects;
import it.hurts.sskirillss.relics.network.packets.S2CSetEntityMotion;
import it.hurts.sskirillss.relics.network.packets.S2CSpawnParticle;
import it.hurts.sskirillss.relics.network.packets.capability.CapabilitySyncPacket;
import it.hurts.sskirillss.relics.network.packets.description.ability.C2SChangeAbilityMode;
import it.hurts.sskirillss.relics.network.packets.description.ability.C2SChangeAbilityEnabled;
import it.hurts.sskirillss.relics.network.packets.description.ability.C2SChangeAbilityRankModifier;
import it.hurts.sskirillss.relics.network.packets.description.ability.C2SPacketAbilityUnlock;
import it.hurts.sskirillss.relics.network.packets.abilities.C2SActivateAbility;
import it.hurts.sskirillss.relics.network.packets.abilities.C2SSwitchSynergyMode;
import it.hurts.sskirillss.relics.network.packets.description.relic.C2SChangeRelicOptionFlawlessVisual;
import it.hurts.sskirillss.relics.network.packets.description.synergy.C2SChangeSynergyMode;
import it.hurts.sskirillss.relics.network.packets.description.synergy.C2SChangeSynergyRankModifier;
import it.hurts.sskirillss.relics.network.packets.item.astral_satchel.C2SMergeAstralSatchelItems;
import it.hurts.sskirillss.relics.network.packets.item.astral_satchel.C2SPushAstralSatchelItem;
import it.hurts.sskirillss.relics.network.packets.item.astral_satchel.C2SQuickStoreAstralSatchelDrop;
import it.hurts.sskirillss.relics.network.packets.item.astral_satchel.C2SQuickStoreAstralSatchelSlot;
import it.hurts.sskirillss.relics.network.packets.item.astral_satchel.C2SSetAstralSatchelAim;
import it.hurts.sskirillss.relics.network.packets.item.astral_satchel.S2CAstralSatchelMotion;
import it.hurts.sskirillss.relics.network.packets.item.astral_satchel.C2SSetAstralSatchelActive;
import it.hurts.sskirillss.relics.network.packets.item.astral_satchel.C2SSyncAstralSatchelCamera;
import it.hurts.sskirillss.relics.network.packets.item.astral_satchel.C2SWithdrawAstralSatchelItem;
import it.hurts.sskirillss.relics.network.packets.item.cut_glass_boot.C2SCycleFluid;
import it.hurts.sskirillss.relics.network.packets.item.jellyfish_necklace.C2SChainedElectricityPacket;
import it.hurts.sskirillss.relics.network.packets.item.rider_flute.C2SCycleRiderFluteSlot;
import it.hurts.sskirillss.relics.network.packets.item.kinetic_belt.C2SSetActive;
import it.hurts.sskirillss.relics.network.packets.item.ring_of_the_seven_deadly_sins.C2SHurtPlayer;
import it.hurts.sskirillss.relics.network.packets.item.roller_skate.C2SCreateSpark;
import it.hurts.sskirillss.relics.network.packets.item.shield_of_retaliation.C2SShieldOfRetaliationRelease;
import it.hurts.sskirillss.relics.network.packets.item.springy_boot.S2CBounceFromSurface;
import it.hurts.sskirillss.relics.network.packets.leveling.FixLevelingPoints;
import it.hurts.sskirillss.relics.network.packets.leveling.PacketAbilityTweak;
import it.hurts.sskirillss.relics.network.packets.leveling.PacketRelicTweak;
import it.hurts.sskirillss.relics.network.packets.research.PacketManageLink;
import it.hurts.sskirillss.relics.network.packets.research.PacketResearchHint;
import it.hurts.sskirillss.relics.network.packets.research.S2CSyncPlayerResearch;
import it.hurts.sskirillss.relics.network.packets.sync.S2CSyncEntityTargetPacket;
import it.hurts.sskirillss.relics.network.packets.targeting.C2SChangeAbilityTargetingOption;
import it.hurts.sskirillss.relics.network.packets.targeting.S2CSyncPlayerAbilityTargeting;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber
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
        registrar.playToClient(S2CSyncEntityTargetPacket.TYPE, S2CSyncEntityTargetPacket.STREAM_CODEC, S2CSyncEntityTargetPacket::handle);
        registrar.playToServer(C2SPacketAbilityUnlock.TYPE, C2SPacketAbilityUnlock.STREAM_CODEC, C2SPacketAbilityUnlock::handle);
        registrar.playToServer(PacketManageLink.TYPE, PacketManageLink.STREAM_CODEC, PacketManageLink::handle);
        registrar.playToServer(PacketResearchHint.TYPE, PacketResearchHint.STREAM_CODEC, PacketResearchHint::handle);
        registrar.playToServer(FixLevelingPoints.TYPE, FixLevelingPoints.STREAM_CODEC, FixLevelingPoints::handle);
        registrar.playToServer(PacketRelicTweak.TYPE, PacketRelicTweak.STREAM_CODEC, PacketRelicTweak::handle);
        registrar.playToServer(C2SChangeAbilityMode.TYPE, C2SChangeAbilityMode.STREAM_CODEC, C2SChangeAbilityMode::handle);
        registrar.playToServer(C2SChangeSynergyMode.TYPE, C2SChangeSynergyMode.STREAM_CODEC, C2SChangeSynergyMode::handle);
        registrar.playToServer(C2SChangeAbilityRankModifier.TYPE, C2SChangeAbilityRankModifier.STREAM_CODEC, C2SChangeAbilityRankModifier::handle);
        registrar.playToServer(C2SChangeAbilityEnabled.TYPE, C2SChangeAbilityEnabled.STREAM_CODEC, C2SChangeAbilityEnabled::handle);
        registrar.playToServer(C2SChangeSynergyRankModifier.TYPE, C2SChangeSynergyRankModifier.STREAM_CODEC, C2SChangeSynergyRankModifier::handle);
        registrar.playToServer(C2SChangeRelicOptionFlawlessVisual.TYPE, C2SChangeRelicOptionFlawlessVisual.STREAM_CODEC, C2SChangeRelicOptionFlawlessVisual::handle);
        registrar.playToClient(S2CSyncPlayerResearch.TYPE, S2CSyncPlayerResearch.STREAM_CODEC, S2CSyncPlayerResearch::handle);
        registrar.playToClient(S2CSyncPlayerAbilityTargeting.TYPE, S2CSyncPlayerAbilityTargeting.STREAM_CODEC, S2CSyncPlayerAbilityTargeting::handle);
        registrar.playToServer(C2SChangeAbilityTargetingOption.TYPE, C2SChangeAbilityTargetingOption.STREAM_CODEC, C2SChangeAbilityTargetingOption::handle);
        registrar.playToServer(C2SActivateAbility.TYPE, C2SActivateAbility.STREAM_CODEC, C2SActivateAbility::handle);
        registrar.playToServer(C2SSwitchSynergyMode.TYPE, C2SSwitchSynergyMode.STREAM_CODEC, C2SSwitchSynergyMode::handle);

        // === KINETIC BELT ===
        registrar.playToServer(C2SSetActive.TYPE, C2SSetActive.STREAM_CODEC, C2SSetActive::handle);

        // === SPRINGY BOOT ===
        registrar.playToClient(S2CBounceFromSurface.TYPE, S2CBounceFromSurface.STREAM_CODEC, S2CBounceFromSurface::handle);

        // === ROLLER SKATE ===
        registrar.playToServer(C2SCreateSpark.TYPE, C2SCreateSpark.STREAM_CODEC, C2SCreateSpark::handle);

        // === CUT-GLASS BOOT ===
        registrar.playToServer(C2SCycleFluid.TYPE, C2SCycleFluid.STREAM_CODEC, C2SCycleFluid::handle);

        // === RIDER FLUTE ===
        registrar.playToServer(C2SCycleRiderFluteSlot.TYPE, C2SCycleRiderFluteSlot.STREAM_CODEC, C2SCycleRiderFluteSlot::handle);

        // === RING OF THE SEVEN DEADLY SINS ===
        registrar.playToServer(C2SHurtPlayer.TYPE, C2SHurtPlayer.STREAM_CODEC, C2SHurtPlayer::handle);

        // === JELLYFISH NECKLACE ===
        registrar.playToServer(C2SChainedElectricityPacket.TYPE, C2SChainedElectricityPacket.STREAM_CODEC, C2SChainedElectricityPacket::handle);

        // === SHIELD OF RETALIATION ===
        registrar.playToServer(C2SShieldOfRetaliationRelease.TYPE, C2SShieldOfRetaliationRelease.STREAM_CODEC, C2SShieldOfRetaliationRelease::handle);

        // === ASTRAL SATCHEL ===
        registrar.playToServer(C2SSetAstralSatchelActive.TYPE, C2SSetAstralSatchelActive.STREAM_CODEC, C2SSetAstralSatchelActive::handle);
        registrar.playToServer(C2SWithdrawAstralSatchelItem.TYPE, C2SWithdrawAstralSatchelItem.STREAM_CODEC, C2SWithdrawAstralSatchelItem::handle);
        registrar.playToServer(C2SSyncAstralSatchelCamera.TYPE, C2SSyncAstralSatchelCamera.STREAM_CODEC, C2SSyncAstralSatchelCamera::handle);
        registrar.playToServer(C2SPushAstralSatchelItem.TYPE, C2SPushAstralSatchelItem.STREAM_CODEC, C2SPushAstralSatchelItem::handle);
        registrar.playToServer(C2SMergeAstralSatchelItems.TYPE, C2SMergeAstralSatchelItems.STREAM_CODEC, C2SMergeAstralSatchelItems::handle);
        registrar.playToServer(C2SSetAstralSatchelAim.TYPE, C2SSetAstralSatchelAim.STREAM_CODEC, C2SSetAstralSatchelAim::handle);
        registrar.playToServer(C2SQuickStoreAstralSatchelSlot.TYPE, C2SQuickStoreAstralSatchelSlot.STREAM_CODEC, C2SQuickStoreAstralSatchelSlot::handle);
        registrar.playToServer(C2SQuickStoreAstralSatchelDrop.TYPE, C2SQuickStoreAstralSatchelDrop.STREAM_CODEC, C2SQuickStoreAstralSatchelDrop::handle);
        registrar.playToClient(S2CAstralSatchelMotion.TYPE, S2CAstralSatchelMotion.STREAM_CODEC, S2CAstralSatchelMotion::handle);

        registrar.playToClient(ChromaticAberrationPacket.TYPE, ChromaticAberrationPacket.STREAM_CODEC, ChromaticAberrationPacket::handle);
        registrar.playToClient(ShakePacket.TYPE, ShakePacket.STREAM_CODEC, ShakePacket::handle);
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

    public static <MSG extends CustomPacketPayload> void sendToClientsTrackingChunk(MSG message, ServerLevel level, ChunkPos chunkPos) {
        PacketDistributor.sendToPlayersTrackingChunk(level, chunkPos, message);
    }
}
