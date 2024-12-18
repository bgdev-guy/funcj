package io.github.jfunk;

import io.github.jfunk.data.IList;
import io.github.jfunk.data.Tuple;

import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;

abstract class Utils {

    static final Supplier<Boolean> LTRUE = () -> true;
    static final Supplier<Boolean> LFALSE = () -> false;

    static Supplier<Boolean> and(Supplier<Boolean> l, Supplier<Boolean> r) {
        return () -> l.get() && r.get();
    }

    static Supplier<Boolean> or(Supplier<Boolean> l, Supplier<Boolean> r) {
        return () -> l.get() || r.get();
    }

    static <I, A> Result<I, A> failure( Input<I> in) {
        return Result.failure(in, "did not Expect");
    }

    static <I, A> Result<I, A> failure(String msg, Input<I> in) {
        return Result.failureMessage(in, msg);
    }

    static <I, A> Result<I, A> failureEof(Parser<I, ?> parser, Input<I> in) {
        return Result.failureEof(in, "unexpected eof");
    }

    static <A> A reduce(A a, IList<Tuple<BinaryOperator<A>, A>> lopA) {
        return lopA.match(
                nel -> nel.head().first().apply(a, reduce(nel.head().second(), nel.tail())),
                nil -> a
        );
    }

    static <T> Optional<T> ifClass(Class<T> clazz, Object value) {
        if (clazz.isInstance(value)) {
            return Optional.of(clazz.cast(value));
        } else {
            return Optional.empty();
        }
    }

    static Optional<Ref<?,?>> ifRefClass(Object value) {
        if (value instanceof Ref) {
            return Optional.of((Ref<?,?>) value);
        } else {
            return Optional.empty();
        }
    }
}
