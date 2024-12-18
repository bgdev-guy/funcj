package io.github.jfunk.functions;

/**
 * Function of arity 7.
 *
 * @param <A> the function's first argument type
 * @param <B> the function's second argument type
 * @param <C> the function's third argument type
 * @param <D> the function's fourth argument type
 * @param <E> the function's fifth argument type
 * @param <G> the function's sixth argument type
 * @param <H> the function's seventh argument type
 * @param <R> the function's return type
 */
@FunctionalInterface
public interface Function7<A, B, C, D, E, G, H, R> {

    /**
     * Apply this function.
     *
     * @param a the function's first argument
     * @param b the function's second argument
     * @param c the function's third argument
     * @param d the function's fourth argument
     * @param e the function's fifth argument
     * @param g the function's sixth argument
     * @param h the function's seventh argument
     * @return the result of applying this function
     */
    R apply(A a, B b, C c, D d, E e, G g, H h);

}
