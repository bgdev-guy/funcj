package io.github.jfunk;

import java.util.function.Supplier;

/**
 * Base class for {@code Parser} implementations.
 *
 * @param <I> the input stream symbol type
 * @param <A> the parser result type
 */
public abstract class ParserImpl<I, A> implements Parser<I, A> {

    private final Supplier<Boolean> acceptsEmpty;

    ParserImpl(Supplier<Boolean> acceptsEmpty) {
        this.acceptsEmpty = acceptsEmpty;
    }

    public Supplier<Boolean> acceptsEmpty() {
        return acceptsEmpty;
    }

    @Override
    public String toString() {
        return "parser{" +
                "empty=" + acceptsEmpty.get() +
                '}';
    }
}
