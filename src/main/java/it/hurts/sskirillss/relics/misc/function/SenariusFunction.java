package it.hurts.sskirillss.relics.misc.function;

@FunctionalInterface
public interface SenariusFunction<T, U, V, W, X, Y, R> {
    R apply(T t, U u, V v, W w, X x, Y y);
}