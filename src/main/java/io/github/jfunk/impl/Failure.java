package io.github.jfunk.impl;

import io.github.jfunk.Input;
import io.github.jfunk.Result;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Base class for the results of a failed parse.
 *
 * @param <I> input stream symbol type
 * @param <A> parser result type
 */
public abstract class Failure<I, A> implements Result<I, A> {
    protected final Input<I> input;

    public Failure(Input<I> input) {
        this.input = input;
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public A getOrThrow() {
        throw new RuntimeException("Parsing failed");
    }

    @Override
    public <B> Result<I, B> map(Function<A, B> f) {
        return this.cast();
    }

    @Override
    public void handle(Consumer<Success<I, A>> success, Consumer<Failure<I, A>> failure) {
        failure.accept(this);
    }

    @Override
    public <B> B match(Function<Success<I, A>, B> success, Function<Failure<I, A>, B> failure) {
        return failure.apply(this);
    }

    @SuppressWarnings("unchecked")
    public <T> Failure<I, T> cast() {
        return (Failure<I, T>) this;
    }

    public Input<I> input() {
        return input;
    }
}