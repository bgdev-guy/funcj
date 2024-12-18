package io.github.jfunk.functions;

import java.util.function.BiFunction;

public interface BiFunctionFlipper<A, B, R> extends BiFunction<A, B, R> {


    /**
     * Flip this function by reversing the order of its arguments.
     *
     * @return the flipped function
     */
    default BiFunctionFlipper<B, A, R> flip() {
        return (b, a) -> apply(a, b);
    }


}
