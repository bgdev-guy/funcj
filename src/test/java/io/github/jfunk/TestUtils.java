package io.github.jfunk;

import org.junit.Assert;

abstract class TestUtils {

    record ParserCheck<I, A>(Parser<I, A> parser) {
            static <I, A> ParserCheck<I, A> parser(Parser<I, A> parser) {
                return new ParserCheck<>(parser);
            }

        WithInput withInput(Input<I> input) {
                return new WithInput(input);
            }

            class WithInput {
                final Input<I> input;

                WithInput(Input<I> input) {
                    this.input = input;
                }

                void succeedsWithResult(A value, Input<I> next) {
                    checkSuccess(parser, input, value, next);
                }

                void fails() {
                    checkFailure(parser, input);
                }
            }
        }

    static <I, A> void checkSuccess(Parser<I, A> p, Input<I> input, A expVal, Input<I> expInput) {
        p.parse(input).handle(
                succ -> {
                    Assert.assertEquals("Parse result value", expVal, succ.value());
                    Assert.assertEquals("Parse result next input", expInput, succ.next());
                },
                fail -> {
                    throw new RuntimeException("Unexpected parse failure : " + fail);
                }
        );
    }

    static <I, A> void checkFailure(Parser<I, A> p, Input<I> input) {
        p.parse(input).handle(
                succ -> {
                    throw new RuntimeException("Unexpected parse success : " + succ);
                },
                fail -> {}
        );
    }
}
