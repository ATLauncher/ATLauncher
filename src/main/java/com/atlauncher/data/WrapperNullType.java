package com.atlauncher.data;

/**
 * This class is used for RXJava Observables, as they do not accept nulls.
 *
 * @param <T> Type of item contained
 */
public abstract class WrapperNullType<T> {
    public static class NULL<T> extends WrapperNullType<T> {
    }

    public static <T> WrapperNullType<T> no() {
        return new NULL<>();
    }

    public static class Value<T> extends WrapperNullType<T> {
        public final T value;

        Value(T value) {
            this.value = value;
        }
    }

    public static <T> WrapperNullType<T> value(T value) {
        return new Value<>(value);
    }
}