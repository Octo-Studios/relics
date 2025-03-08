package it.hurts.sskirillss.relics.network.packets.research;

import com.google.common.collect.Multimap;
import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.init.SoundRegistry;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.Reference;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Map;

@Data
@AllArgsConstructor
public class PacketResearchHint implements CustomPacketPayload {
    private final int container;
    private final int slot;
    private final String ability;
    private final int amount;

    public static final Type<PacketResearchHint> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MODID, "research_hint"));

    public static final StreamCodec<ByteBuf, PacketResearchHint> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, PacketResearchHint::getContainer,
            ByteBufCodecs.INT, PacketResearchHint::getSlot,
            ByteBufCodecs.STRING_UTF8, PacketResearchHint::getAbility,
            ByteBufCodecs.INT, PacketResearchHint::getAmount,
            PacketResearchHint::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player().level().isClientSide())
                return;

            ServerPlayer player = (ServerPlayer) ctx.player();

            if (player.containerMenu.containerId != container) {
                causeError(player);

                return;
            }

            ItemStack stack = DescriptionUtils.gatherRelicStack(player, slot);

            if (!(stack.getItem() instanceof IRelicItem relic)) {
                causeError(player);

                return;
            }

            RandomSource random = player.getRandom();

            int cost = relic.getResearchHintPlayerExperienceCost(ability) * amount;

            if (EntityUtils.getPlayerTotalExperience(player) < cost)
                return;

            player.giveExperiencePoints(-cost);

            research(stack, amount);

            if (relic.testAbilityResearch(stack, ability)) {
                relic.setAbilityResearched(stack, ability, true);

                player.connection.send(new ClientboundSoundPacket(Holder.direct(SoundRegistry.FINISH_RESEARCH.get()), SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 1F, 1F, random.nextLong()));
            } else
                player.connection.send(new ClientboundSoundPacket(Holder.direct(SoundRegistry.CONNECT_STARS.get()), SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.75F, 0.75F + random.nextFloat() * 0.5F, random.nextLong()));

            try {
                player.containerMenu.getSlot(slot).set(stack);
            } catch (Exception e) {
                e.printStackTrace();

                causeError(player);
            }
        });
    }

    public void research(ItemStack stack, int amount) {
        if (!(stack.getItem() instanceof IRelicItem relic))
            return;

        Multimap<Integer, Integer> pattern = relic.getResearchData(ability).getLinks();
        Multimap<Integer, Integer> links = relic.getResearchLinks(stack, ability);

        int iteration = 0;

        for (Map.Entry<Integer, Integer> entry : links.entries()) {
            Integer start = entry.getKey();
            Integer end = entry.getValue();

            if (!(pattern.containsEntry(start, end) || pattern.containsEntry(end, start))) {
                relic.removeResearchLink(stack, ability, start, end);

                if (++iteration >= amount)
                    break;
            }
        }

        iteration = 0;

        for (Map.Entry<Integer, Integer> entry : pattern.entries()) {
            Integer start = entry.getKey();
            Integer end = entry.getValue();

            if (!(links.containsEntry(start, end) || links.containsEntry(end, start))) {
                relic.addResearchLink(stack, ability, start, end);

                if (++iteration >= amount)
                    break;
            }
        }
    }

    private static void causeError(Player player) {
        player.displayClientMessage(Component.translatable("info.relics.researching.wrong_container").withStyle(ChatFormatting.RED), false);

        player.closeContainer();
    }
}