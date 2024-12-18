package io.github.jfunk;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import io.github.jfunk.data.Unit;
import org.junit.runner.RunWith;


@RunWith(JUnitQuickcheck.class)
public class CombinatorsTest {

    @Property
    public void failAlwaysFails(char c1) {
        final Input<Character> input = Input.of(String.valueOf(c1));

        final Parser<Character, Character> parser = Combinators.fail();

        TestUtils.ParserCheck.parser(parser)
                .withInput(input)
                .fails();
    }

    @Property
    public void eofSucceedsOnEmptyInput(char c1) {
        final Input<Character> input = Input.of("");

        final Parser<Character, Unit> parser = Combinators.eof();

        TestUtils.ParserCheck.parser(parser)
                .withInput(input)
                .succeedsWithResult(Unit.UNIT, input);
    }

    @Property
    public void eofFailsOnNonEmptyInput(char c1) {

        final Parser<Character, Unit> parser = Combinators.eof();

        TestUtils.ParserCheck.parser(parser)
                .withInput(Input.of(Character.toString(c1)))
                .fails();
    }

    @Property
    public void valueMatchesValue(char c1, char c2) {
        final Input<Character> input1 = Input.of(String.valueOf(c1));
        final Input<Character> input2 = Input.of(String.valueOf(c2));

        final Parser<Character, Character> parser = Combinators.value(c1);

        TestUtils.ParserCheck.parser(parser)
                .withInput(input1)
                .succeedsWithResult(c1, input1.next());

        TestUtils.ParserCheck.parser(parser)
                .withInput(input2)
                .fails();
    }

    @Property
    public void valueFailsOnNonEmptyInput(char c1) {

        final Parser<Character, Character> parser = Combinators.value(c1);

        TestUtils.ParserCheck.parser(parser)
                .withInput(Input.of(""))
                .fails();
    }

    @Property
    public void satisfyAppliesPredicate(char c0) {
        final char even;
        final char odd;
        if (c0 % 2 == 0) {
            even = c0;
            odd = (char)((even + 1) % 0xffff);
        } else {
            odd = c0;
            even = (char)((odd + 1) % 0xffff);
        }

        final Input<Character> input1 = Input.of(String.valueOf(even));
        final Input<Character> input2 = Input.of(String.valueOf(odd));

        final Parser<Character, Character> parser =
                Combinators.satisfy("even", c -> c % 2 == 0);

        TestUtils.ParserCheck.parser(parser)
                .withInput(input1)
                .succeedsWithResult(even, input1.next());

        TestUtils.ParserCheck.parser(parser)
                .withInput(input2)
                .fails();
    }

    @Property
    public void satisfyFailsOnNonEmptyInput() {

        final Parser<Character, Character> parser =
                Combinators.satisfy("even", c -> c % 2 == 0);

        TestUtils.ParserCheck.parser(parser)
                .withInput(Input.of(""))
                .fails();
    }

    @Property
    public void anyMatchesAnything(char c1) {
        final Input<Character> input = Input.of(String.valueOf(c1));

        final Parser<Character, Character> parser = Combinators.any();

        TestUtils.ParserCheck.parser(parser)
                .withInput(input)
                .succeedsWithResult(c1, input.next());
    }

    @Property
    public void anyFailsOnNonEmptyInput() {

        final Parser<Character, Character> parser = Combinators.any();

        TestUtils.ParserCheck.parser(parser)
                .withInput(Input.of(""))
                .fails();
    }
}
