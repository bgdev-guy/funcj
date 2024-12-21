package io.github.jfunk;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A reference to a {@link Parser}.
 * <p>
 * At creation, the reference is typically uninitialised.
 * Any of the {@code Parser} methods will throw if invoked on an uninitialised {@code Ref}.
 * It is subsequently initialised (via the {@link Ref#set(Parser)} method) with a {@code Parser}.
 * {@code Ref} is typically used to allow parsers for grammars with circular
 * dependencies to be constructed.
 *
 * @param <I> input stream symbol type
 * @param <A> parser result type
 */
public class Ref<I, A> extends Parser<I, A> {

    private volatile Parser<I, A> impl = new Uninitialised<>();
    private volatile boolean initialised = false;

    /**
     * Constructs an uninitialised Ref.
     * Any of the {@code Parser} methods will throw if invoked on an uninitialised {@code Ref}.
     * The reference can be subsequently initialised via the {@link Ref#set(Parser)} method.
     */
    public Ref() {
        super(() -> {
            throw error();
        }, in -> {
            throw error();
        });
    }

    private static RuntimeException error() {
        return new RuntimeException("Uninitialised Parser reference");
    }

    /**
     * Indicates if this reference is initialised.
     *
     * @return true if this reference is initialised
     */
    public boolean isInitialised() {
        return initialised;
    }

    /**
     * Initialises this reference with the given parser implementation.
     *
     * @param impl the parser implementation
     * @return this parser
     * @throws IllegalStateException if the reference is already initialised
     */
    public synchronized Parser<I, A> set(Parser<I, A> impl) {
        if (initialised) {
            throw new IllegalStateException("Ref is already initialised");
        }
        this.impl = Objects.requireNonNull(impl);
        this.initialised = true;
        return this;
    }

    @Override
    public Supplier<Boolean> acceptsEmpty() {
        return () -> impl.acceptsEmpty().get();
    }

    @Override
    public Result<I, A> apply(Input<I> in) {
        return impl.apply(in);
    }

    protected static class Uninitialised<I, A> extends Parser<I, A> {

        private Uninitialised() {
            super(() -> {
                throw error();
            }, in -> {
                throw error();
            });
        }

        private static RuntimeException error() {
            return new RuntimeException("Uninitialised Parser reference");
        }
    }
}