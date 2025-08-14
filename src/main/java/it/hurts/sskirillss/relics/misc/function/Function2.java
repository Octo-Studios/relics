package it.hurts.sskirillss.relics.misc.function;

@FunctionalInterface
public interface Function2<T, U, R> {
    R apply(T t, U u);
}