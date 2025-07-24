package it.hurts.sskirillss.relics.misc.function;

@FunctionalInterface
public interface UnusFunction<T, R> {
    R apply(T t);
}