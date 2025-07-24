package it.hurts.sskirillss.relics.misc.function;

@FunctionalInterface
public interface BiFunction<T, U, R> {
    R apply(T t, U u);
}