package io.github.jfunk.impl.result;

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
public class Failure<I, A> implements Result<I, A> {

    protected final String error;
    protected final Input<I> input;

    public Failure(Input<I> input) {
        this(input, "Parsing failed");
    }

    public Failure(Input<I> input, String error) {
        this.input = input;
        this.error = error;
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
        return (Result<I, B>) this.cast();
    }

    @Override
    public void handle(Consumer<Success<I, A>> success, Consumer<Failure<I, A>> failure) {
        failure.accept(this);
    }

    @Override
    public int getPosition() {
        return input.position();
    }

    @Override
    public void addResult(Result<I, A> result) {

    }

    @Override
    public <B> B match(Function<Success<I, A>, B> success, Function<Failure<I, A>, B> failure) {
        return failure.apply(this);
    }

    public  Failure<I, A> cast() {
        return this;
    }

    @Override
    public Input<I> next() {
        return null;
    }

    public Input<I> input() {
        return input;
    }


    public String toString() {
        return "FailureMessage{" +
                "position=" + input.position() +
                "input=" + input +
                ", cause='" + error + '\'' +
                '}';
    }
}