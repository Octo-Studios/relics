package it.hurts.sskirillss.relics.network.packets.item.roller_skate;

import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.entities.RollerSparkEntity;
import it.hurts.sskirillss.relics.init.RelicsEntities;
import it.hurts.sskirillss.relics.items.relics.feet.RollerSkateItem;
import it.hurts.sskirillss.relics.misc.stream_codec.ExtendedStreamCodec;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.joml.Vector3f;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.UUID;

@Data
@AllArgsConstructor
public class C2SCreateSpark implements CustomPacketPayload {
    private final String identifier;
    private final int index;

    private final Vector3f pos;
    private final Vector3f motion;
    private final String owner;
    private final float damage;
    private final float ignite;
    private final boolean flawless;

    public static final Type<C2SCreateSpark> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "roller_skate/create_spark"));

    public static final StreamCodec<ByteBuf, C2SCreateSpark> STREAM_CODEC = ExtendedStreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, C2SCreateSpark::getIdentifier,
            ByteBufCodecs.INT, C2SCreateSpark::getIndex,
            ByteBufCodecs.VECTOR3F, C2SCreateSpark::getPos,
            ByteBufCodecs.VECTOR3F, C2SCreateSpark::getMotion,
            ByteBufCodecs.STRING_UTF8, C2SCreateSpark::getOwner,
            ByteBufCodecs.FLOAT, C2SCreateSpark::getDamage,
            ByteBufCodecs.FLOAT, C2SCreateSpark::getIgnite,
            ByteBufCodecs.BOOL, C2SCreateSpark::isFlawless,
            C2SCreateSpark::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var player = ctx.player();
            var level = player.level();

            CuriosApi.getCuriosInventory(player).flatMap(inventory -> inventory.findCurio(this.getIdentifier(), this.getIndex())).ifPresent(slot -> {
                var stack = slot.stack();
                var relic = (RollerSkateItem) stack.getItem();

                var spark = new RollerSparkEntity(RelicsEntities.ROLLER_SPARK.get(), level);

                spark.setDeltaMovement(this.motion.x(), this.motion.y(), this.motion.z());
                spark.setPos(this.pos.x(), this.pos.y(), this.pos.z());
                spark.setFlawless(this.flawless);
                spark.setDamage(this.damage);
                spark.setIgnite(this.ignite);
                spark.setStack(stack);

                if (level instanceof ServerLevel serverLevel)
                    spark.setOwner(serverLevel.getEntity(UUID.fromString(this.owner)));

                level.addFreshEntity(spark);

                relic.addAbilityMetricValue(player, stack, "skating", "sparks_created", 1);

                if (relic.canAddRelicExperience(player, stack, "skating", "creating_sparks"))
                    relic.addRelicExperience(player, stack, "skating", "creating_sparks", 1);
            });
        });
    }
}