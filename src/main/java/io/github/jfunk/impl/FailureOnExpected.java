package io.github.jfunk.impl;

import io.github.jfunk.Input;
import io.github.jfunk.ParseError;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The result of a failed parse, where the failure was due an expected symbol not being found.
 *
 * @param <I> input stream symbol type
 * @param <A> parser result type
 */
public class FailureOnExpected<I, A> extends Failure<I, A> {
    private final String expected;

    public FailureOnExpected(Input<I> input, String expected) {
        super(input);
        this.expected = expected;
    }

    @Override
    public String toString() {
        return "FailureOnExpected{" +
                "input=" + input +
                ", expected='" + expected + '\'' +
                '}';
    }
}