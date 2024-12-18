package io.github.jfunk;

import io.github.jfunk.data.IList;
import io.github.jfunk.data.Unit;

import java.util.function.Predicate;

import static io.github.jfunk.Utils.*;

/**
 * Combinators provides functions for combining parsers to form new parsers.
 */
public abstract class Combinators {

    /**
     * A parser that always fails.
     *
     * @param <I> the input stream symbol type
     * @param <A> the parser result type
     * @return a parser that always fails.
     */
    public static <I, A> Parser<I, A> fail() {
        return new ParserImpl<>(LTRUE) {
            @Override
            public Result<I, A> apply(Input<I> in) {
                return failure(in);
            }
        };
    }

    /**
     * A parser that always fails.
     *
     * @param msg the failure message
     * @param <I> the input stream symbol type
     * @param <A> the parser result type
     * @return a parser that always fails.
     */
    public static <I, A> Parser<I, A> fail(String msg) {
        return new ParserImpl<>(LTRUE) {
            @Override
            public Result<I, A> apply(Input<I> in) {
                return failure(msg, in);
            }
        };
    }

    /**
     * A parser that succeeds if the end of the input has been reached.
     *
     * @param <I> the input stream symbol type
     * @return a parser that succeeds iff we are at the end of the input.
     */
    public static <I> Parser<I, Unit> eof() {
        return new ParserImpl<>(LTRUE) {
            @Override
            public Result<I, Unit> apply(Input<I> in) {
                return in.isEof() ?
                        Result.success(Unit.UNIT, in) :
                        failure(in);
            }
        };
    }

    /**
     * A parser that succeeds if the next input symbol equals the given {@code value},
     * and returns the value.
     *
     * @param val the value expected by the parser
     * @param <I> the input stream symbol type
     * @return as parser that succeeds if the next input symbol equals the given {@code value}
     */
    public static <I> Parser<I, I> value(I val) {
        return value(val, val);
    }

    /**
     * A parser that succeeds if the next input symbol equals the given {@code value},
     * and returns the given {@code res} value.
     *
     * @param val the value expected by the parser
     * @param res the value returned by the parser
     * @param <I> the input stream symbol type
     * @param <A> the parser result type
     * @return a parser that succeeds if the next input symbol equals the given {@code value}
     */
    public static <I, A> Parser<I, A> value(I val, A res) {
        return new ParserImpl<>(LFALSE) {
            @Override
            public Result<I, A> apply(Input<I> in) {
                if (in.get().equals(val)) {
                    return Result.success(res, in.next());
                }
                return new Result.FailureMessage<>(in, "expected %s saw %s".formatted(val, in.get()));
            }
        };
    }

    /**
     * A parser that succeeds if the next input symbol satisfies the given predicate.
     *
     * @param name a name for the parser (used for error messages)
     * @param pred the predicate to be applied to the next input
     * @param <I>  the input stream symbol type
     * @return a parser that succeeds if the next input symbol satisfies the given predicate.
     */
    public static <I> Parser<I, I> satisfy(String name, Predicate<I> pred) {
        return new ParserImpl<>(LFALSE) {
            @Override
            public Result<I, I> apply(Input<I> in) {
                if (pred.test(in.get())) {
                    return Result.success(in.get(), in.next());
                }
                return Result.failure(in, name);
            }
        };
    }

    /**
     * A parser that succeeds on any input symbol, and returns that symbol.
     *
     * @param <I> the input stream symbol type
     * @return a parser that succeeds on any input symbol
     */
    public static <I> Parser<I, I> any() {
        return new ParserImpl<>(LFALSE) {
            @Override
            public Result<I, I> apply(Input<I> in) {
                return in.isEof() ?
                        failureEof(this, in) :
                        Result.success(in.get(), in.next());
            }
        };
    }

    /**
     * A parser that succeeds on any input symbol, and returns that symbol.
     *
     * @param <I>   the input stream symbol type
     * @param clazz a dummy param for type inference of generic type {@code I}
     * @return a parser that succeeds on any input symbol
     */
    public static <I> Parser<I, I> any(Class<I> clazz) {
        return any();
    }


    /**
     * A parser that attempts one or more parsers in turn and returns the result
     * of the first that succeeds, or else fails.
     *
     * @param p1  the first parser
     * @param p2  the second parser
     * @param <I> the input stream symbol type
     * @param <A> the parser result type
     * @return a parser that attempts one or more parsers in turn
     */
    public static <I, A>
    Parser<I, A> choice(Parser<I, ? extends A> p1, Parser<I, ? extends A> p2) {
        return choice(IList.of(p1.cast(), p2.cast()));
    }

    /**
     * A parser that attempts one or more parsers in turn and returns the result
     * of the first that succeeds, or else fails.
     *
     * @param p1  the first parser
     * @param p2  the second parser
     * @param p3  the third parser
     * @param <I> the input stream symbol type
     * @param <A> the parser result type
     * @return a parser that attempts one or more parsers in turn
     */
    public static <I, A>
    Parser<I, A> choice(Parser<I, ? extends A> p1, Parser<I, ? extends A> p2, Parser<I, ? extends A> p3) {
        return choice(IList.of(p1.cast(), p2.cast(), p3.cast()));
    }

    /**
     * A parser that attempts one or more parsers in turn and returns the result
     * of the first that succeeds, or else fails.
     *
     * @param p1  the first parser
     * @param p2  the second parser
     * @param p3  the third parser
     * @param p4  the fourth parser
     * @param <I> the input stream symbol type
     * @param <A> the parser result type
     * @return a parser that attempts one or more parsers in turn
     */
    public static <I, A>
    Parser<I, A> choice(
            Parser<I, ? extends A> p1,
            Parser<I, ? extends A> p2,
            Parser<I, ? extends A> p3,
            Parser<I, ? extends A> p4) {
        return choice(IList.of(p1.cast(), p2.cast(), p3.cast(), p4.cast()));
    }

    /**
     * A parser that attempts one or more parsers in turn and returns the result
     * of the first that succeeds, or else fails.
     *
     * @param p1  the first parser
     * @param p2  the second parser
     * @param p3  the third parser
     * @param p4  the fourth parser
     * @param p5  the fifth parser
     * @param <I> the input stream symbol type
     * @param <A> the parser result type
     * @return a parser that attempts one or more parsers in turn
     */
    public static <I, A>
    Parser<I, A> choice(
            Parser<I, ? extends A> p1,
            Parser<I, ? extends A> p2,
            Parser<I, ? extends A> p3,
            Parser<I, ? extends A> p4,
            Parser<I, ? extends A> p5) {
        return choice(IList.of(p1.cast(), p2.cast(), p3.cast(), p4.cast(), p5.cast()));
    }

    /**
     * A parser that attempts one or more parsers in turn and returns the result
     * of the first that succeeds, or else fails.
     *
     * @param p1  the first parser
     * @param p2  the second parser
     * @param p3  the third parser
     * @param p4  the fourth parser
     * @param p5  the fifth parser
     * @param p6  the sixth parser
     * @param <I> the input stream symbol type
     * @param <A> the parser result type
     * @return a parser that attempts one or more parsers in turn
     */
    public static <I, A>
    Parser<I, A> choice(
            Parser<I, ? extends A> p1,
            Parser<I, ? extends A> p2,
            Parser<I, ? extends A> p3,
            Parser<I, ? extends A> p4,
            Parser<I, ? extends A> p5,
            Parser<I, ? extends A> p6) {
        return choice(IList.of(p1.cast(), p2.cast(), p3.cast(), p4.cast(), p5.cast(), p6.cast()));
    }

    /**
     * A parser that attempts one or more parsers in turn and returns the result
     * of the first that succeeds, or else fails.
     *
     * @param ps  the list of parsers
     * @param <I> the input stream symbol type
     * @param <A> the parser result type
     * @return a parser that attempts one or more parsers in turn
     */
    public static <I, A> Parser<I, A> choice(IList.NonEmpty<Parser<I, A>> ps) {
        // We use an iterative implementation for performance, and to avoid StackOverflowExceptions.
        // The more concise recursive equivalent is ps.foldLeft1(Parser::or)
        return new ParserImpl<>(
                ps.map(Parser::acceptsEmpty).foldLeft1(Utils::or)
        ) {
            @Override
            public Result<I, A> apply(Input<I> in) {
                if (in.isEof()) {
                    for (Parser<I, A> p : ps) {
                        if (p.acceptsEmpty().get()) {
                            return p.apply(in);
                        }
                    }
                    return failureEof(this, in);
                }
                for (Parser<I, A> p : ps) {
                    Result<I, A> result = p.apply(in);
                    if (result.isSuccess()) {
                        return result;
                    }
                }
                return failure(in);
            }
        };
    }

    /**
     * A parser that attempts one or more parsers in turn and returns the result
     * of the first that succeeds, or else fails.
     *
     * @param ps  the list of parsers
     * @param <I> the input stream symbol type
     * @param <A> the parser result type
     * @return a parser that attempts one or more parsers in turn
     */
    @SafeVarargs
    public static <I, A>
    Parser<I, A> choice(Parser<I, A>... ps) {
        if (ps.length == 0) {
            throw new RuntimeException("Cannot construct a choice from an empty list of parsers");
        } else {
            return choice((IList.NonEmpty<Parser<I, A>>) IList.ofArray(ps));
        }
    }
}
