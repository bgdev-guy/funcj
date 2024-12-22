package io.github.jfunk.impl;

import io.github.jfunk.Input;

/**
 * The result of a failed parse, where the failure is descriobed by an error message.
 *
 * @param <I> input stream symbol type
 * @param <A> parser result type
 */
public class FailureMessage<I, A> extends Failure<I, A> {
    private final String error;

    public FailureMessage(Input<I> input, String error) {
        super(input);
        this.error = error;
    }

    @Override
    public String toString() {
        return "FailureMessage{" +
                "input=" + input +
                ", error='" + error + '\'' +
                '}';
    }
}