package io.github.jfunk;

import java.util.function.Supplier;

public abstract class Trampoline<T> {

    public abstract T get();

    public static <T> Trampoline<T> done(T result) {
        return new Done<>(result);
    }

    public static <T> Trampoline<T> more(Supplier<Trampoline<T>> next) {
        return new More<>(next);
    }

    private static class Done<T> extends Trampoline<T> {
        private final T result;

        private Done(T result) {
            this.result = result;
        }

        @Override
        public T get() {
            return result;
        }
    }

    private static class More<T> extends Trampoline<T> {
        private final Supplier<Trampoline<T>> next;

        private More(Supplier<Trampoline<T>> next) {
            this.next = next;
        }

        @Override
        public T get() {
            Trampoline<T> trampoline = this;
            while (trampoline instanceof More) {
                trampoline = ((More<T>) trampoline).next.get();
            }
            return trampoline.get();
        }
    }
}