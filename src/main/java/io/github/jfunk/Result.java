package io.github.jfunk;


import io.github.jfunk.impl.result.Failure;
import io.github.jfunk.impl.result.Success;

import java.util.function.Consumer;
import java.util.function.Function;

public interface Result<I, A> {

    boolean isSuccess();

    A getOrThrow();

    <B> Result<I, B> map(Function<A, B> f);

    void handle(Consumer<Success<I, A>> success, Consumer<Failure<I, A>> failure);

    int getPosition();

    void addResult(Result<I, A> result);

    Result<I,A> cast();

    Input<I> next();

    <B> B match(Function<Success<I, A>, B> success, Function<Failure<I, A>, B> failure);

    static <I, A> Result<I, A> success(A result, Input<I> next) {
        return new Success<>(result, next);
    }

    static <I, A> Result<I, A> failure(Input<I> in, String expected) {
        return new Failure<I, A>(in, expected) {
        };
    }

    static <I, A> Result<I, A> failureEof(Input<I> in, String expected) {
        return new Failure<>(in, expected);
    }

    static <I, A> Result<I, A> failureMessage(Input<I> in, String error) {
        return new Failure<>(in, error);
    }
}
