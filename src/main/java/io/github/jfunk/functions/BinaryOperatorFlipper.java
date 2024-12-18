package io.github.jfunk.functions;

import java.util.function.BinaryOperator;

public interface BinaryOperatorFlipper<T> extends BinaryOperator<T> {
    /**
     * Flip this operator by reversing the order of its arguments.
     *
     * @return the flipped function
     */
    default BinaryOperatorFlipper<T> flip() {
        return (b, a) -> apply(a, b);
    }

}
