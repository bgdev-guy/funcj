package io.github.jfunk.functions;

/**
 * Function of arity 4.
 *
 * @param <A> the function's first argument type
 * @param <B> the function's second argument type
 * @param <C> the function's third argument type
 * @param <D> the function's fourth argument type
 * @param <R> the function's return type
 */
@FunctionalInterface
public interface Function4<A, B, C, D, R> {

    /**
     * Apply this function.
     *
     * @param a the function's first argument
     * @param b the function's second argument
     * @param c the function's third argument
     * @param d the function's fourth argument
     * @return the result of applying this function
     */
    R apply(A a, B b, C c, D d);

}
