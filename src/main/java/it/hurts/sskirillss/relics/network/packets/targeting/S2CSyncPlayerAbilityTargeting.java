package it.hurts.sskirillss.relics.network.packets.targeting;

import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.AbilityTargetingComponent;
import it.hurts.sskirillss.relics.api.relics.PlayerAbilityTargetingComponent;
import it.hurts.sskirillss.relics.init.RelicsAttachments;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
public class S2CSyncPlayerAbilityTargeting implements CustomPacketPayload {
    private final int entityId;
    private final Map<String, AbilityTargetingComponent> abilities;

    public static final Type<S2CSyncPlayerAbilityTargeting> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "sync_player_ability_targeting"));

    private static final StreamCodec<ByteBuf, Map<String, Boolean>> TARGETING_OPTIONS_CODEC =
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.BOOL);

    private static final StreamCodec<ByteBuf, AbilityTargetingComponent> TARGETING_COMPONENT_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, TARGETING_OPTIONS_CODEC), AbilityTargetingComponent::getModes,
            AbilityTargetingComponent::new
    );

    public static final StreamCodec<ByteBuf, S2CSyncPlayerAbilityTargeting> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, S2CSyncPlayerAbilityTargeting::getEntityId,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, TARGETING_COMPONENT_CODEC), S2CSyncPlayerAbilityTargeting::getAbilities,
            S2CSyncPlayerAbilityTargeting::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> this.doSync(this.entityId, this.abilities));
    }

    @OnlyIn(Dist.CLIENT)
    private void doSync(int entityId, Map<String, AbilityTargetingComponent> abilities) {
        var minecraft = Minecraft.getInstance();
        var level = minecraft.level;
        var entity = level == null ? null : level.getEntity(entityId);
        var player = entity instanceof Player foundPlayer ? foundPlayer : null;

        if (player == null && minecraft.player != null && minecraft.player.getId() == entityId)
            player = minecraft.player;

        if (player == null)
            return;

        player.setData(RelicsAttachments.PLAYER_ABILITY_TARGETING, PlayerAbilityTargetingComponent.builder().abilities(abilities).build());
    }
}
