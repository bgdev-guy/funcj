package io.github.jfunk;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import io.github.jfunk.data.IList;
import io.github.jfunk.data.Tuple;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.function.BinaryOperator;
import java.util.function.Function;


@RunWith(JUnitQuickcheck.class)
public class ParserTest {

    private static final Parser<Character, BinaryOperator<Integer>> subtr = Combinators.value('-', (x, y) -> x - y);
    private static final int Z = 1000;

    private static void assertEvaluate(Parser<Character, Integer> parser, String s, int expected) {
        Assert.assertEquals(s, expected, parser.parse(Input.of(s)).getOrThrow().intValue());
    }

    @Property
    public void pureConsumesNoInput(char c1) {
        final Input<Character> input = Input.of("");

        final Parser<Character, Character> parser = Parser.pure(c1);

        TestUtils.ParserCheck.parser(parser)
                .withInput(input)
                .succeedsWithResult(c1, input);
    }

    @Property
    public void mapTransformsValue(char c1) {
        final Input<Character> input = Input.of(String.valueOf(c1));

        final Parser<Character, Character> parser =
                Combinators.value(c1)
                        .map(c -> (char) (c + 1));

        TestUtils.ParserCheck.parser(parser)
                .withInput(input)
                .succeedsWithResult((char) (c1 + 1), input.next());
    }

    @Property
    public void apAppliesF(char c1) {
        final Input<Character> input = Input.of(String.valueOf(c1));

        final Function<Character, Character> f = c -> (char) (c + 1);

        final Parser<Character, Character> parser = Parser.ap(f, Combinators.any());

        TestUtils.ParserCheck.parser(parser)
                .withInput(input)
                .succeedsWithResult((char) (c1 + 1), input.next());
    }

    @Property
    public void apChainsParsers(char c1, char c2) {
        Assume.assumeThat(c1, CoreMatchers.not(c2));

        final Character cc1 = c1;
        final Character cc2 = c2;

        // String.toCharArray creates a new array each time, so ensure we call it only once.
        final char[] ca12 = ("" + c1 + c2).toCharArray();
        final char[] ca11 = ("" + c1 + c1).toCharArray();

        final Parser<Character, Tuple<Character, Character>> parser =
                Parser.ap(
                        Parser.ap(
                                a -> b -> Tuple.of(a, b),
                                Combinators.value(cc1)
                        ), Combinators.value(cc2)
                );

        TestUtils.ParserCheck.parser(parser)
                .withInput(Input.of(ca12))
                .succeedsWithResult(Tuple.of(cc1, cc2), Input.of(ca12).next().next());

        TestUtils.ParserCheck.parser(parser)
                .withInput(Input.of(ca11))
                .fails();
    }

    @Property
    public void orAppliesEitherParser(char c1, char c2, char c3) {
        Assume.assumeThat(c1, CoreMatchers.not(c2));
        Assume.assumeThat(c1, CoreMatchers.not(c3));
        Assume.assumeThat(c2, CoreMatchers.not(c3));

        final Input<Character> input1 = Input.of(String.valueOf(c1));
        final Input<Character> input2 = Input.of(String.valueOf(c2));
        final Input<Character> input3 = Input.of(String.valueOf(c3));

        final Character cc1 = c1;
        final Character cc2 = c2;

        final Parser<Character, Character> parser = Combinators.value(cc1).or(Combinators.value(cc2));

        TestUtils.ParserCheck.parser(parser)
                .withInput(input1)
                .succeedsWithResult(cc1, input1.next());

        TestUtils.ParserCheck.parser(parser)
                .withInput(input2)
                .succeedsWithResult(cc2, input2.next());

        TestUtils.ParserCheck.parser(parser)
                .withInput(input3)
                .fails();
    }

    @Property
    public void andWithMapAppliesF(char c1, char c2) {
        // String.toCharArray returns a new array each time, so ensure we call it only once.
        final char[] data = ("" + c1 + c2).toCharArray();
        final Input<Character> input = Input.of(data);

        final Parser<Character, Tuple<Character, Character>> parser =
                Combinators.any(Character.class)
                        .and(Combinators.any())
                        .map(Tuple::of);

        final Input<Character> expInp = Input.of(data).next().next();

        TestUtils.ParserCheck.parser(parser)
                .withInput(input)
                .succeedsWithResult(Tuple.of(c1, c2), expInp);
    }

    @Property
    public void manyMatches(char c1, char c2) {
        Assume.assumeThat(c1, CoreMatchers.not(c2));

        final String s = "" + c1 + c1 + c1 + c1;
        final char[] ca = (s + c2).toCharArray();

        final Parser<Character, String> parser =
                Text.chr(c1).many()
                        .andL(Text.chr(c2))
                        .map(IList::listToString);

        TestUtils.ParserCheck.parser(parser)
                .withInput(Input.of(ca))
                .succeedsWithResult(s, Input.of(ca).next().next().next().next().next());
    }

    @Property
    public void manySucceedsOnNonEmptyInput() {
        final Input<Character> input = Input.of("");

        final Parser<Character, String> parser = Combinators.any(Character.class).many().map(IList::listToString);

        TestUtils.ParserCheck.parser(parser)
                .withInput(input)
                .succeedsWithResult("", input);
    }

    @Property
    public void manyTillMatches(char c1, char c2) {
        Assume.assumeThat(c1, CoreMatchers.not(c2));

        final String s = "" + c1 + c1 + c1 + c1;
        final char[] ca = (s + c2).toCharArray();

        final Parser<Character, String> parser =
                Text.chr(c1).manyTill(Text.chr(c2))
                        .map(IList::listToString);

        TestUtils.ParserCheck.parser(parser)
                .withInput(Input.of(ca))
                .succeedsWithResult(s, Input.of(ca).next().next().next().next().next());
    }

    @Property
    public void many1MatchesMany(char c1, char c2) {
        final String s = "" + c1 + c2;
        final char[] ca = s.toCharArray();

        final Parser<Character, String> parser = Combinators.any(Character.class).many1().map(IList::listToString);

        TestUtils.ParserCheck.parser(parser)
                .withInput(Input.of(ca))
                .succeedsWithResult(s, Input.of(ca).next().next());
    }

    @Property
    public void many1FailsOnNonEmptyInput() {
        final Input<Character> input = Input.of("");

        final Parser<Character, String> parser = Combinators.any(Character.class).many1().map(IList::listToString);

        TestUtils.ParserCheck.parser(parser)
                .withInput(input)
                .fails();
    }

    @Test
    public void testUninitialisedRefManyDoesNotThrow() {
        final Ref<Character, Character> r = Parser.ref();
        Parser<Character, IList<Character>> rs = r.many();
    }

    @Test
    public void testInitialisedRefManyDoesNotThrow() {
        final Ref<Character, Character> ref = Parser.ref();
        ref.set(Text.chr('x'));
        Parser<Character, IList<Character>> rs = ref.many();
    }

    @Test(expected = Exception.class)
    public void testInitialisedRefManyDoesThrow() {
        final Ref<Character, Character> ref = Parser.ref();
        ref.set(Combinators.fail());
        Parser<Character, IList<Character>> rs = ref.many();
    }

    @Test
    public void testChainl() {
        final Parser<Character, Integer> parser = Text.intr.chainl(subtr, Z);

        assertEvaluate(parser, "", Z);
        assertEvaluate(parser, "1", 1);
        assertEvaluate(parser, "1-2", -1);
        assertEvaluate(parser, "1-2-3", (1 - 2) - 3);
    }

    @Test
    public void testChainr() {
        final Parser<Character, Integer> parser = Text.intr.chainr(subtr, Z);

        assertEvaluate(parser, "", Z);
        assertEvaluate(parser, "1", 1);
        assertEvaluate(parser, "1-2", -1);
        assertEvaluate(parser, "1-2-3", 1 - (2 - 3));
    }

    @Test
    public void testChainl1() {
        final Parser<Character, Integer> parser = Text.intr.chainl1(subtr);

        assertEvaluate(parser, "1", 1);
        assertEvaluate(parser, "1-2", -1);
        assertEvaluate(parser, "1-2-3", (1 - 2) - 3);
    }

    @Test
    public void testChainr1() {
        final Parser<Character, Integer> parser = Text.intr.chainr1(subtr);

        assertEvaluate(parser, "1", 1);
        assertEvaluate(parser, "1-2", -1);
        assertEvaluate(parser, "1-2-3", 1 - (2 - 3));
    }
}
