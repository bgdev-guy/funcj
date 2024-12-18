package io.github.jfunk.functions;

/**
 * Function of arity 3.
 *
 * @param <A> the function's first argument type
 * @param <B> the function's second argument type
 * @param <C> the function's third argument type
 * @param <R> the function's return type
 */
@FunctionalInterface
public interface Function3<A, B, C, R> {
    /**
     * Apply this function.
     *
     * @param a the function's first argument
     * @param b the function's second argument
     * @param c the function's third argument
     * @return the result of applying this function
     */
    R apply(A a, B b, C c);
}
