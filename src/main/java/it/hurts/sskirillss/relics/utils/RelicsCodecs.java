package it.hurts.sskirillss.relics.utils;

import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.BaseMapCodec;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class RelicsCodecs {
    /**
     * A copy of {@link com.mojang.serialization.codecs.UnboundedMapCodec} that wraps the result with
     * {@link Collections#unmodifiableMap(Map)} instead of copying into a {@link com.google.common.collect.ImmutableMap}
     * to boost performance for keys with expensive {@link Object#equals(Object)} implementations like {@link String}.
     *
     * @param keyCodec The codec to use for de/serializing the keys
     * @param elementCodec The codec to use for de/serializing the elements
     * @param <K> The type of the keys
     * @param <V> The type of the elements
     */
    public record RelicsUnboundedMapCodec<K, V>(
            Codec<K> keyCodec,
            Codec<V> elementCodec
    ) implements BaseMapCodec<K, V>, Codec<Map<K, V>> {
        @Override
        public <T> DataResult<Pair<Map<K, V>, T>> decode(final DynamicOps<T> ops, final T input) {
            return ops.getMap(input).setLifecycle(Lifecycle.stable()).flatMap(map -> decode(ops, map)).map(r -> Pair.of(r, input));
        }

        @Override
        public <T> DataResult<T> encode(final Map<K, V> input, final DynamicOps<T> ops, final T prefix) {
            return encode(input, ops, ops.mapBuilder()).build(prefix);
        }

        @Override
        public @NotNull String toString() {
            return "RelicsUnboundedMapCodec[" + keyCodec + " -> " + elementCodec + ']';
        }

        @Override
        public <T> DataResult<Map<K, V>> decode(final DynamicOps<T> ops, final MapLike<T> input) {
            final Object2ObjectMap<K, V> read = new Object2ObjectArrayMap<>();
            final Stream.Builder<Pair<T, T>> failed = Stream.builder();

            final DataResult<Unit> result = input.entries().reduce(
                    DataResult.success(Unit.INSTANCE, Lifecycle.stable()),
                    (r, pair) -> {
                        final DataResult<K> key = keyCodec().parse(ops, pair.getFirst());
                        final DataResult<V> value = elementCodec().parse(ops, pair.getSecond());

                        final DataResult<Pair<K, V>> entryResult = key.apply2stable(Pair::of, value);
                        final Optional<Pair<K, V>> entry = entryResult.resultOrPartial();
                        if (entry.isPresent()) {
                            final V existingValue = read.putIfAbsent(entry.get().getFirst(), entry.get().getSecond());
                            if (existingValue != null) {
                                failed.add(pair);
                                return r.apply2stable((u, p) -> u, DataResult.error(() -> "Duplicate entry for key: '" + entry.get().getFirst() + "'"));
                            }
                        }
                        if (entryResult.isError()) {
                            failed.add(pair);
                        }

                        return r.apply2stable((u, p) -> u, entryResult);
                    },
                    (r1, r2) -> r1.apply2stable((u1, u2) -> u1, r2)
            );

            final Map<K, V> elements = Collections.unmodifiableMap(read);
            final T errors = ops.createMap(failed.build());

            return result.map(unit -> elements).setPartial(elements).mapError(e -> e + " missed input: " + errors);
        }
    }

    public static <K, V> RelicsUnboundedMapCodec<K, V> unboundedMap(final Codec<K> keyCodec, final Codec<V> elementCodec) {
        return new RelicsUnboundedMapCodec<>(keyCodec, elementCodec);
    }
}
