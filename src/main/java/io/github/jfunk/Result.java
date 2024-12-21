package io.github.jfunk;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A parse result is either a {@code Success} or a {@code Failure}.
 *
 * @param <I> the input stream symbol type
 * @param <A> the parser result type
 */
public abstract class Result<I, A> {

    final List<ParseError> parseErrors = new ArrayList<>();
    Input<I> input;
    String error;
    A value;
    Input<I> next;
    boolean success;

    static <I, A> Result<I, A> success(A result, Input<I> next) {
        return new Success<>(result, next);
    }

    static <I, A> Result<I, A> failure(Input<I> in, String expected) {
        return new FailureOnExpected<>(in, expected);
    }

    static <I, A> Result<I, A> failureEof(Input<I> in, String expected) {
        return new FailureOnExpected<>(in, expected);
    }

    static <I, A> Result<I, A> failureMessage(Input<I> in, String error) {
        return new FailureMessage<>(in, error);
    }

    /**
     * Indicates if this result is successful.
     *
     * @return true if this is a {@code Success}, otherwise false
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns the parse value if the parse was successful, otherwise throws an exception
     *
     * @return the parse value if the parse was successful
     * @throws RuntimeException if the parse was unsuccessful
     */
    public abstract A getOrThrow();

    public abstract void addParseError(ParseError pe);

    /**
     * Map a function over this {@code Result} value
     *
     * @param f   the function
     * @param <B> the function return type
     * @return if this is a {@code Success} then return a {@code Success} which wraps the result
     * of applying the function to the success value, otherwise return this value
     */
    public abstract <B> Result<I, B> map(Function<A, B> f);

    /**
     * Apply one of two side effects functions to this value.
     *
     * @param success the function to be applied to a successful value
     * @param failure the function to be applied to a failure value
     */
    public abstract void handle(Consumer<Success<I, A>> success, Consumer<Failure<I, A>> failure);

    /**
     * Apply one of two functions to this value.
     *
     * @param success the function to be applied to a successful value
     * @param failure the function to be applied to a failure value
     * @param <B>     the function return type
     * @return the result of applying either function
     */
    public abstract <B> B match(Function<Success<I, A>, B> success, Function<Failure<I, A>, B> failure);

    /**
     * The result of a successful parse.
     * Consists of the parsed value, and a reference to the point in the input symbol stream
     * immediately after the parsed input.
     *
     * @param <I> the input stream symbol type
     * @param <A> the parser result type
     */
    public static class Success<I, A> extends Result<I, A> {

        public Success(A value, Input<I> next) {
            this.success = true;
            this.value = value;
            this.next = next;
        }

        public Input<I> next() {
            return this.next;
        }

        public A value() {
            return value;
        }


        public String toString() {
            return "Success{" +
                    "value=" + value +
                    ", next=" + next +
                    '}';
        }


        public A getOrThrow() {
            return value;
        }

        public void addParseError(ParseError pe) {
            //this.parseErrors.add(pe)
        }


        public <B> Result<I, B> map(Function<A, B> f) {
            return success(f.apply(value), next);
        }


        public void handle(Consumer<Success<I, A>> success, Consumer<Failure<I, A>> failure) {
            success.accept(this);
        }

        public <B> B match(Function<Success<I, A>, B> success, Function<Failure<I, A>, B> failure) {
            return success.apply(this);
        }
    }

    /**
     * Base class for the results of a failed parse.
     *
     * @param <I> input stream symbol type
     * @param <A> parser result type
     */
    public abstract static class Failure<I, A> extends Result<I, A> {


        public Failure(Input<I> input) {
            this.success = false;
            this.input = input;
        }

        public Input<I> input() {
            return input;
        }

        @Override
        public String toString() {
            return "input=" + input;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Failure<?, ?> failure = (Failure<?, ?>) o;
            return Objects.equals(input, failure.input);
        }

        @Override
        public int hashCode() {
            return Objects.hash(input);
        }

        @SuppressWarnings("unchecked")
        public <T> Failure<I, T> cast() {
            return (Failure<I, T>) this;
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
    }

    /**
     * The result of a failed parse, where the failure was due an expected symbol not being found.
     *
     * @param <I> input stream symbol type
     * @param <A> parser result type
     */
    public static final class FailureOnExpected<I, A> extends Failure<I, A> {
        private final String expected;
        private final List<ParseError> parseErrors = new ArrayList<>();

        public FailureOnExpected(Input<I> input, String expected) {
            super(input);
            this.expected = expected;
        }

        public String expected() {
            return expected;
        }

        @Override
        public String toString() {
            return "FailureOnExpected{" +
                    super.toString() +
                    ", expected=" + expected +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            FailureOnExpected<?, ?> that = (FailureOnExpected<?, ?>) o;
            if (!Objects.equals(expected, that.expected)) return false;
            return super.equals(o);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), expected);
        }

        @Override
        public A getOrThrow() {
            throw new RuntimeException(
                    "Failure at position " + input.position() + ", expected=" + expected
            );
        }

        @Override
        public void addParseError(ParseError pe) {
            this.parseErrors.add(pe);
        }

    }

    /**
     * The result of a failed parse, where the failure is descriobed by an error message.
     *
     * @param <I> input stream symbol type
     * @param <A> parser result type
     */
    public static class FailureMessage<I, A> extends Failure<I, A> {


        public FailureMessage(Input<I> input, String error) {
            super(input);
            this.error = error;
        }

        public String expected() {
            return error;
        }

        @Override
        public String toString() {
            return "Failure{" +
                    super.toString() +
                    ", error=" + error +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            FailureMessage<?, ?> that = (FailureMessage<?, ?>) o;
            if (!Objects.equals(error, that.error)) return false;
            return super.equals(o);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), error);
        }

        @Override
        public A getOrThrow() {
            throw new RuntimeException(
                    "Failure at position " + input.position() + ", error=" + error
            );
        }

        @Override
        public void addParseError(ParseError pe) {
            this.parseErrors.add(pe);
        }

    }
}
