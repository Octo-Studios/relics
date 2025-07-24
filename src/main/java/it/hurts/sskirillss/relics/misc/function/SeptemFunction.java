package it.hurts.sskirillss.relics.misc.function;

@FunctionalInterface
public interface SeptemFunction<T, U, V, W, X, Y, Z, R> {
    R apply(T t, U u, V v, W w, X x, Y y, Z z);
}