package io.github.jfunk;

import org.junit.Assert;
import org.junit.Test;

import java.util.function.BinaryOperator;


public class ExprTest {

    private static final Ref<Character, Double> expr = Parser.ref();

    static {
        final Parser<Character, BinaryOperator<Double>> add = Text.chr('+').map(c -> Double::sum);
        final Parser<Character, BinaryOperator<Double>> sub = Text.chr('-').map(c -> (x, y) -> x - y);
        final Parser<Character, BinaryOperator<Double>> mult = Text.chr('*').map(c -> (x, y) -> x * y);
        final Parser<Character, BinaryOperator<Double>> div = Text.chr('/').map(c -> (x, y) -> x / y);
        final Parser<Character, BinaryOperator<Double>> binOp = add.or(sub).or(mult).or(div);

        final Parser<Character, Double> binOpExpr =
                Text.chr('(')
                        .andR(expr)
                        .and(binOp)
                        .and(expr)
                        .andL(Text.chr(')'))
                        .map((l, op, r) -> op.apply(l, r));

        expr.set(Combinators.choice(Text.dble, binOpExpr));
    }

    private static final double EPSILON = 1e-8;

    private static void assertEvaluate(String s, double expected) {
        Assert.assertEquals(s, expected, expr.parse(Input.of(s)).getOrThrow(), EPSILON);
    }

    @Test
    public void testDble() {
        assertEvaluate("1", 1);
        assertEvaluate("1.2", 1.2);
    }

    @Test
    public void testAdd() {
        assertEvaluate("(1+2)", 3);
        assertEvaluate("(1.234+8.765)", 9.999);
    }

    @Test
    public void testAddMult() {
        assertEvaluate("(1+(2*3))", (1+(2*3)));
        assertEvaluate("(1.23+(4.56*7.89))", (1.23+(4.56*7.89)));
    }
}
