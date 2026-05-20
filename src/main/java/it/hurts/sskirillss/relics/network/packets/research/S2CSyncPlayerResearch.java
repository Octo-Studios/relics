package it.hurts.sskirillss.relics.network.packets.research;

import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.PlayerResearchComponent;
import it.hurts.sskirillss.relics.api.relics.ResearchComponent;
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
public class S2CSyncPlayerResearch implements CustomPacketPayload {
    private final int entityId;
    private final Map<String, ResearchComponent> research;

    public static final Type<S2CSyncPlayerResearch> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "sync_player_research"));

    private static final StreamCodec<ByteBuf, ResearchComponent> RESEARCH_COMPONENT_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.collection(java.util.ArrayList::new, ByteBufCodecs.INT)), ResearchComponent::getLinks,
            ByteBufCodecs.BOOL, ResearchComponent::isResearched,
            ResearchComponent::new
    );

    public static final StreamCodec<ByteBuf, S2CSyncPlayerResearch> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, S2CSyncPlayerResearch::getEntityId,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, RESEARCH_COMPONENT_CODEC), S2CSyncPlayerResearch::getResearch,
            S2CSyncPlayerResearch::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> this.doSync(this.entityId, this.research));
    }

    @OnlyIn(Dist.CLIENT)
    private void doSync(int entityId, Map<String, ResearchComponent> research) {
        var minecraft = Minecraft.getInstance();
        var level = minecraft.level;
        var entity = level == null ? null : level.getEntity(entityId);
        var player = entity instanceof Player foundPlayer ? foundPlayer : null;

        if (player == null && minecraft.player != null && minecraft.player.getId() == entityId)
            player = minecraft.player;

        if (player == null)
            return;

        player.setData(RelicsAttachments.PLAYER_RESEARCH, PlayerResearchComponent.builder().research(research).build());
    }
}
