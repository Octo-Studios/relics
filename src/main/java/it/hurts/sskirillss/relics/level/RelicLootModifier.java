package it.hurts.sskirillss.relics.level;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.hurts.sskirillss.relics.init.ConfigRegistry;
import it.hurts.sskirillss.relics.init.LootCodecRegistry;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RelicLootModifier extends LootModifier {
    public static final Supplier<MapCodec<RelicLootModifier>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.mapCodec(inst -> codecStart(inst).apply(inst, RelicLootModifier::new)));

    public static final List<LootEntryCache> LOOT_ENTRIES = new ArrayList<>();

    public RelicLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Nonnull
    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        var entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        var vec = context.getParamOrNull(LootContextParams.ORIGIN);
        var table = context.getQueriedLootTableId().toString();

        if (vec == null || entity == null)
            return generatedLoot;

        var random = context.getRandom();

        if (random.nextDouble() > ConfigRegistry.LOOT_CONFIG.getRelicGenChance())
            return generatedLoot;

        var pos = new BlockPos((int) vec.x(), (int) vec.y(), (int) vec.z());
        var level = entity.level();

        List<LootEntryCache> entries = new ArrayList<>();

        for (var entry : LOOT_ENTRIES) {
            if (!(entry.getTables().stream().anyMatch(matcher -> matcher.matches(table))
                    && entry.getDimensions().stream().anyMatch(matcher -> matcher.matches(level.dimension().location().toString()))
                    && entry.getBiomes().stream().anyMatch(matcher -> matcher.matches(level.getBiome(pos).getRegisteredName()))))
                continue;

            entries.add(entry);
        }

        if (entries.isEmpty())
            return generatedLoot;

        var weight = entries.stream().mapToDouble(LootEntryCache::getWeight).sum();

        if (weight <= 0D)
            return generatedLoot;

        var range = random.nextDouble() * weight;

        for (var entry : entries) {
            range -= entry.getWeight();

            if (range <= 0D) {
                generatedLoot.add(entry.getItem().getDefaultInstance());

                break;
            }
        }

        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return LootCodecRegistry.RELIC_LOOT.get();
    }

    public static void processRelicCache(IRelicItem relic) {
        var server = ServerLifecycleHooks.getCurrentServer();

        if (server == null)
            return;

        LOOT_ENTRIES.removeIf(entry -> entry.getItem() == relic);

        // FIXME: Should not use default template
        for (var entry : relic.getDefaultLootTemplate().getEntries()) {
            var item = relic.getItem();

            if (item == null)
                continue;

            LOOT_ENTRIES.add(new LootEntryCache(compileRegex(entry.getDimensions()), compileRegex(entry.getBiomes()), compileRegex(entry.getTables()), entry.getWeight(), item));
        }
    }

    private static List<MatcherEntry> compileRegex(List<String> patterns) {
        List<MatcherEntry> entries = new ArrayList<>();

        for (var pattern : patterns) {
            try {
                entries.add(new RegexEntry(Pattern.compile(pattern)));
            } catch (PatternSyntaxException e) {
                entries.add(new StringEntry(pattern));
            }
        }

        return entries;
    }

    @EventBusSubscriber
    public static class Events {
        @SubscribeEvent
        public static void onServerStarted(ServerStartedEvent event) {
            if (!LOOT_ENTRIES.isEmpty())
                return;

            for (var entry : BuiltInRegistries.ITEM.entrySet()) {
                if (!(entry.getValue() instanceof IRelicItem relic))
                    continue;

                processRelicCache(relic);
            }
        }
    }

    @Data
    @AllArgsConstructor
    public static class LootEntryCache {
        private List<MatcherEntry> dimensions;
        private List<MatcherEntry> biomes;
        private List<MatcherEntry> tables;

        private int weight;

        private Item item;
    }

    private static sealed abstract class MatcherEntry permits RegexEntry, StringEntry {
        public abstract boolean matches(String input);
    }

    @Data
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    private static non-sealed class RegexEntry extends MatcherEntry {
        private Pattern entry;

        @Override
        public boolean matches(String input) {
            return entry.matcher(input).matches();
        }
    }

    @Data
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    private static non-sealed class StringEntry extends MatcherEntry {
        private String entry;

        @Override
        public boolean matches(String input) {
            return entry.equals(input);
        }
    }
}