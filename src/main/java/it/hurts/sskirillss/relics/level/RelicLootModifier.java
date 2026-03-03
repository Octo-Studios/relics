package it.hurts.sskirillss.relics.level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.hurts.sskirillss.relics.init.LootCodecRegistry;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicStorage;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RelicLootModifier extends LootModifier {
    private static final Set<String> INVALID_PATTERN_CACHE = new ObjectOpenHashSet<>();
    private static final Map<String, Pattern> COMPILED_PATTERN_CACHE = new Object2ObjectOpenHashMap<>();

    public static final Codec<RelicLootModifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, RelicLootModifier::new));

    public RelicLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Nonnull
    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        String lootId = context.getQueriedLootTableId().toString();

        boolean isValid;

        for (IRelicItem relic : RelicStorage.RELICS.keySet()) {
            for (Map.Entry<String, Float> entry : relic.getLootData().getCollection().getEntries().entrySet()) {
                String pattern = entry.getKey();
                float chance = entry.getValue();

                if (!INVALID_PATTERN_CACHE.contains(pattern)) {
                    Pattern compiledPattern = COMPILED_PATTERN_CACHE.get(pattern);
                    if (compiledPattern == null) {
                        try {
                            compiledPattern = Pattern.compile(pattern);
                        } catch (PatternSyntaxException ignored) {
                        }
                    }
                    if (compiledPattern != null) {
                        COMPILED_PATTERN_CACHE.put(pattern, compiledPattern);
                        var matcher = compiledPattern.matcher(lootId);
                        isValid = matcher.matches();
                    } else {
                        INVALID_PATTERN_CACHE.add(pattern);
                        isValid = lootId.equals(pattern);
                    }
                } else {
                    isValid = lootId.equals(pattern);
                }

                if (isValid) {
                    if (context.getRandom().nextFloat() <= chance)
                        generatedLoot.add(relic.getItem().getDefaultInstance());

                    break;
                }
            }
        }

        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return LootCodecRegistry.RELIC_LOOT.get();
    }
}