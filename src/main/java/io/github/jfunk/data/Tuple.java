package io.github.jfunk.data;

import java.util.Objects;

/**
 * A 2-tuple of values.
 * <p>
 * Null values are not allowed.
 *
 * @param <A> the first value type
 * @param <B> the second value type
 */
public record Tuple<A, B>(A first, B second) {
    /**
     * Create a new {@code Tuple} from the given values.
     *
     * @param first  the first value
     * @param second the second value
     * @param <A>    the first value type
     * @param <B>    the second value type
     * @return the new {@code Tuple}
     * @throws NullPointerException if any of the tuple values are null
     */
    public static <A, B> Tuple<A, B> of(A first, B second) {
        return new Tuple<>(Objects.requireNonNull(first), Objects.requireNonNull(second));
    }

}
