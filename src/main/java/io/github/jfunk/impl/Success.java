package io.github.jfunk.impl;

import io.github.jfunk.Input;
import io.github.jfunk.Result;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The result of a successful parse.
 * Consists of the parsed value, and a reference to the point in the input symbol stream
 * immediately after the parsed input.
 *
 * @param <I> the input stream symbol type
 * @param <A> the parser result type
 */
public class Success<I, A> implements Result<I, A> {
    private final A value;
    private final Input<I> next;

    public Success(A value, Input<I> next) {
        this.value = value;
        this.next = next;
    }

    @Override
    public boolean isSuccess() {
        return true;
    }

    @Override
    public A getOrThrow() {
        return value;
    }

    @Override
    public <B> Result<I, B> map(Function<A, B> f) {
        return new Success<>(f.apply(value), next);
    }

    @Override
    public void handle(Consumer<Success<I, A>> success, Consumer<Failure<I, A>> failure) {
        success.accept(this);
    }

    @Override
    public <B> B match(Function<Success<I, A>, B> success, Function<Failure<I, A>, B> failure) {
        return success.apply(this);
    }

    public Input<I> next() {
        return next;
    }
}