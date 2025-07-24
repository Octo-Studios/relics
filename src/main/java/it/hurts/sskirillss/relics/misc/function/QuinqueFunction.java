package it.hurts.sskirillss.relics.misc.function;

@FunctionalInterface
public interface QuinqueFunction<T, U, V, W, X, R> {
    R apply(T t, U u, V v, W w, X x);
}