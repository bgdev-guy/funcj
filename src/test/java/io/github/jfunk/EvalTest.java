package io.github.jfunk;

import org.junit.Test;

import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

public class EvalTest {

    enum BinOp {
        ADD {
            BinaryOperator<Integer> op() {return Integer::sum;}},
        SUB {BinaryOperator<Integer> op() {return (l, r) -> l - r;}},
        MUL {BinaryOperator<Integer> op() {return (l, r) -> l * r;}},
        DIV {BinaryOperator<Integer> op() {return (l, r) -> l / r;}};

        abstract BinaryOperator<Integer> op();
    }

    @Test
    public void test() {

        final Ref<Character, UnaryOperator<Integer>> expr = Parser.ref();

        final Parser<Character, UnaryOperator<Integer>> var = Text.chr('x').map(u -> x -> x);

        final Parser<Character, UnaryOperator<Integer>> num = Text.intr.map(i -> x -> i);

        final Parser<Character, BinOp> binOp =
                Combinators.choice(
                        Text.chr('+').map(c -> BinOp.ADD),
                        Text.chr('-').map(c -> BinOp.SUB),
                        Text.chr('*').map(c -> BinOp.MUL),
                        Text.chr('/').map(c -> BinOp.DIV)
                );

        final Parser<Character, UnaryOperator<Integer>> add =
                Text.chr('(')
                        .andR(expr)
                        .and(binOp)
                        .and(expr)
                        .andL(Text.chr(')'))
                        .map(lhs -> bo -> rhs -> x -> bo.op().apply(lhs.apply(x), rhs.apply(x)));

        expr.set(Combinators.choice(var, num, add));

        final int i = expr.parse(Input.of("(x*((x/2)+x))")).getOrThrow().apply(4);
        assert(i == 24);
    }
}
