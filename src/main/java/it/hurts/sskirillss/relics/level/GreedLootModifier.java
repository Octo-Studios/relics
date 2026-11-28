package it.hurts.sskirillss.relics.level;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatStacking;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.init.RelicsLootCodecs;
import it.hurts.sskirillss.relics.utils.RelicStacks;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;
import javax.annotation.Nonnull;

public class GreedLootModifier extends LootModifier {
    public static final Supplier<MapCodec<GreedLootModifier>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.mapCodec(inst -> codecStart(inst).apply(inst, GreedLootModifier::new)));

    public GreedLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Nonnull
    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        var entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        var vec = context.getParamOrNull(LootContextParams.ORIGIN);
        var tableId = context.getQueriedLootTableId();
        var random = context.getRandom();

        if (vec == null)
            return generatedLoot;

        var path = tableId.getPath();
        var shouldApply = false;

        if (path.startsWith("blocks/")) {
            var blockState = context.getParamOrNull(LootContextParams.BLOCK_STATE);

            if (blockState == null)
                return generatedLoot;

            if (blockState.is(Tags.Blocks.ORES))
                shouldApply = true;
        } else
            shouldApply = true;

        if (!shouldApply)
            return generatedLoot;

        var player = entity instanceof Player p ? p : null;

        if (player == null) {
            var killer = context.getParamOrNull(LootContextParams.ATTACKING_ENTITY);

            if (killer instanceof Player p)
                player = p;
        }

        if (player != null) {
            var rings = RelicStacks.forStat(player, RelicsItems.RING_OF_THE_SEVEN_DEADLY_SINS.get(), "greed", "chance");
            var chance = StatStacking.fromParticipants(rings, "chance");

            for (var entry : rings) {
                if (random.nextDouble() < chance.contribution(entry.stack()).orElse(0D)) {
                    entry.addExperience("nullification", 1);
                    entry.addMetric("nullified_tables", 1);

                    return new ObjectArrayList<>();
                }
            }
        }

        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return RelicsLootCodecs.GREED_LOOT.get();
    }
}
