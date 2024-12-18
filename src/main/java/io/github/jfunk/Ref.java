package io.github.jfunk;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A reference to a {@link Parser}.
 * <p>
 * At creation the reference is typically uninitialised.
 * Any of the {@code Parser} methods will throw if invoked on an uninitialised {@code Ref}.
 * It is subsequently initialised (via the {@link Ref#set(Parser)} method) with a {@code Parser}
 * {@code Ref} is typically used to allow parsers for grammars with circular
 * dependencies to be constructed.
 *
 * @param <I> input stream symbol type
 * @param <A> parser result type
 */
public class Ref<I, A> implements Parser<I, A> {

    private Parser<I, A> impl;

    Ref(Parser<I, A> impl) {
        this.impl = Objects.requireNonNull(impl);
    }

    Ref() {
        this.impl = Uninitialised.instance();
    }

    /**
     * Indicate if this reference is initialised.
     *
     * @return true if this reference is initialised
     */
    public boolean initialised() {
        return impl != Uninitialised.instance();
    }

    /**
     * Initialise this reference
     *
     * @param impl the parser
     * @return this parser
     */
    public Parser<I, A> set(Parser<I, A> impl) {
        if (this.impl != Uninitialised.INSTANCE) {
            throw new IllegalStateException("Ref is already initialised");
        } else {
            this.impl = Objects.requireNonNull(impl);
            return this;
        }
    }

    @Override
    public Supplier<Boolean> acceptsEmpty() {
        return () -> impl.acceptsEmpty().get();
    }

    @Override
    public Result<I, A> apply(Input<I> in) {
        return impl.apply(in);
    }

    protected enum Uninitialised implements Parser<Object, Object> {
        INSTANCE {
            @Override
            public Supplier<Boolean> acceptsEmpty() {
                throw error();
            }

            public Result<Object, Object> apply(Input<Object> in) {
                throw error();
            }
        };

        private static RuntimeException error() {
            return new RuntimeException("Uninitialised Supplier Parser reference");
        }

        @SuppressWarnings("unchecked")
        static <I, A> Parser<I, A> instance() {
            return (Parser<I, A>) INSTANCE;
        }
    }
}
