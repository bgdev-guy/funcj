package io.github.jfunk;


import io.github.jfunk.data.IList;
import io.github.jfunk.data.Tuple;
import io.github.jfunk.data.Unit;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static io.github.jfunk.Utils.LTRUE;
import static io.github.jfunk.Utils.failureEof;

/**
 * A parser is essentially a function from an input stream to a parse {@link Result}.
 * The {@code Parser} type along with the {@code pure} and {@code ap} functions constitute an applicative functor.
 *
 * @param <I> the input stream symbol type
 * @param <A> the parser result type
 */
public class Parser<I, A> {

    Supplier<Boolean> acceptsEmpty;
    Function<Input<I>, Result<I, A>> applyHandler;

    public Parser(Supplier<Boolean> acceptsEmpty, Function<Input<I>, Result<I, A>> applyHandler) {
        this.acceptsEmpty = acceptsEmpty;
        this.applyHandler = applyHandler;
    }

    public Parser(Supplier<Boolean> acceptsEmpty) {
        this(acceptsEmpty, Utils::failure);
    }

    public Parser() {
    }


    /**
     * Construct an uninitialized parser reference object.
     *
     * @param <I> the input stream symbol type
     * @param <A> the parser result type
     * @return the uninitialized parser reference
     */
    public static <I, A> Ref<I, A> ref() {
        return new Ref<>();
    }

    /**
     * Applicative unit/pure function.
     * Constructs a parser that always returns the given value, without consuming any input.
     *
     * @param a   the value to be returned by the parser
     * @param <I> the input stream symbol type
     * @param <A> the parser result type
     * @return a parser that always returns the given value
     * @throws IllegalArgumentException if the provided value is null
     */
    public static <I, A> Parser<I, A> pure(A a) {
        if (a == null) {
            throw new IllegalArgumentException("The provided value cannot be null");
        }
        return new Parser<>(LTRUE, in -> Result.success(a, in));
    }

    /**
     * Constructs a parser that applies a function parser to a value parser.
     * If both parsers succeed, the result is the application of the function to the value.
     *
     * @param pf  the parser that returns a function result
     * @param pa  the parser that returns a value result
     * @param <I> the input stream symbol type
     * @param <A> the input type of the function
     * @param <B> the return type of the function
     * @return a parser that returns the result of applying the parsed function to the parsed value
     */
    public static <I, A, B> Parser<I, B> ap(Parser<I, Function<A, B>> pf, Parser<I, A> pa) {
        return new Parser<>(Utils.and(pf.acceptsEmpty(), pa.acceptsEmpty()),
                in -> {
                    final Result<I, Function<A, B>> r = pf.apply(in);

                    if (r.isSuccess()) {
                        final Result.Success<I, Function<A, B>> success = (Result.Success<I, Function<A, B>>) r;
                        final Input<I> next = success.next();
                        if (!pa.acceptsEmpty().get()) {
                            if (next.isEof()) {
                                return failureEof(pa, next);
                            }
                        }
                        final Result<I, A> r2 = pa.apply(next);
                        if (r2.isSuccess()) {
                            return r2.map(success.value());
                        }
                        return (Result.Failure<I, B>) r2;
                    }
                    return ((Result.Failure<I, Function<A, B>>) r).cast();
                });
    }

    /**
     * Constructs a parser that applies a function to the result of another parser.
     * If the parser succeeds, the result is the application of the function to the parsed value.
     *
     * @param f   the function to be applied to the parsed value
     * @param pa  the parser that returns a value result
     * @param <I> the input stream symbol type
     * @param <A> the input type of the function
     * @param <B> the return type of the function
     * @return a parser that returns the result of applying the function to the parsed value
     */
    public static <I, A, B> Parser<I, B> ap(Function<A, B> f, Parser<I, A> pa) {
        return ap(pure(f), pa);
    }

    /**
     * Standard applicative traversal.
     * Translates a list of values into a parser that returns a list of parsed values.
     *
     * @param lt  the list of values
     * @param f   the function to be applied to each value in the list
     * @param <I> the input stream symbol type
     * @param <T> the type of list elements
     * @param <U> the type of the parser result
     * @return a parser that returns a list of parsed values
     */
    public static <I, T, U> Parser<I, IList<U>> traverse(IList<T> lt, Function<T, Parser<I, U>> f) {
        return lt.foldRight(pure(IList.empty()),
                (t, plu) -> ap(plu.map(lu -> lu::add), f.apply(t))
        );
    }

    /**
     * Standard applicative sequencing.
     * Translates a list of parsers into a parser that returns a list of parsed values.
     *
     * @param lpt the list of parsers
     * @param <I> the input stream symbol type
     * @param <T> the type of the parser result
     * @return a parser that returns a list of parsed values
     */
    public static <I, T> Parser<I, IList<T>> sequence(IList<Parser<I, T>> lpt) {
        return lpt.foldRight(
                pure(IList.empty()),
                (pt, plt) -> ap(plt.map(lt -> lt::add), pt)
        );
    }

    /**
     * Variation of {@link Parser#sequence(IList)} for streams.
     * Translates a stream of parsers into a parser that returns a stream of parsed values.
     *
     * @param spt the stream of parsers
     * @param <E> the input stream symbol type
     * @param <T> the type of the parser result
     * @return a parser that returns a stream of parsed values
     */
    public static <E, T> Parser<E, Stream<T>> sequence(Stream<Parser<E, T>> spt) {
        final Iterator<Parser<E, T>> iter = spt.iterator();
        Parser<E, IList<T>> plt = pure(IList.empty());
        while (iter.hasNext()) {
            final Parser<E, T> pt = iter.next();
            plt = ap(plt.map(lt -> lt::add), pt);
        }
        return plt.map(IList::stream);
    }

    /**
     * Apply this parser to the input stream. Fail if EOF isn't reached.
     *
     * @param in the input stream
     * @return the parser result
     */
    public Result<I, A> parse(Input<I> in) {
        final Parser<I, A> parserAndEof = this.andL(Combinators.eof());
        if (acceptsEmpty().get()) {
            return parserAndEof.apply(in);
        } else if (in.isEof()) {
            return failureEof(this, in);
        }
        return parserAndEof.apply(in);
    }

    /**
     * Indicates whether this parser accepts empty input.
     *
     * @return a supplier that returns true if the parser accepts empty input
     */
    public Supplier<Boolean> acceptsEmpty() {
        if (applyHandler == null) {
            throw new RuntimeException("Uninitialised Parser");
        }
        return acceptsEmpty;
    }

    /**
     * Apply this parser to the input stream.
     * Note: If this parser is being used as a standalone parser,
     * then call {@link Parser#parse(Input)} to parse an input.
     *
     * @param in the input stream
     * @return the parser result
     */
    public Result<I, A> apply(Input<I> in) {
        if (applyHandler == null) {
            throw new RuntimeException("Uninitialised Parser");
        }
        return applyHandler.apply(in);
    }

    /**
     * Cast this parser to another type.
     *
     * @param <B> the target type
     * @return the casted parser
     */
    @SuppressWarnings("unchecked")
    public <B> Parser<I, B> cast() {
        return (Parser<I, B>) this;
    }

    /**
     * Constructs a parser that applies a function to the result of this parser.
     * If this parser succeeds, the result is the application of the function to the parsed value.
     *
     * @param f   the function to be mapped over this parser
     * @param <B> the function return type
     * @return a parser that returns the function applied to this parser's result
     */
    public <B> Parser<I, B> map(Function<A, B> f) {
        return new Parser<>(
                Parser.this.acceptsEmpty()
        ) {
            @Override
            public Result<I, B> apply(Input<I> in) {
                return Parser.this.apply(in).map(f);
            }
        };
    }

    /**
     * Constructs a parser that returns the result of either this parser or,
     * if it fails, the result of the {@code rhs} parser.
     *
     * @param rhs the second parser to attempt
     * @param <B> the rhs parser result type
     * @return a parser that returns the result of either this parser or the {@code rhs} parser
     */
    @SuppressWarnings("unchecked")
    public <B extends A> Parser<I, A> or(Parser<I, B> rhs) {
        return new Parser<>(
                Utils.or(Parser.this.acceptsEmpty(), rhs.acceptsEmpty())
        ) {
            @Override
            public Result<I, A> apply(Input<I> in) {
                if (in.isEof()) {
                    if (Parser.this.acceptsEmpty().get()) {
                        return Parser.this.apply(in);
                    } else if (rhs.acceptsEmpty().get()) {
                        return (Result<I, A>) rhs.apply(in);
                    }
                    return failureEof(this, in);
                }
                Result<I, A> result = Parser.this.apply(in);
                if (result.isSuccess()) {
                    return result;
                }
                return (Result<I, A>) rhs.apply(in);
            }
        };
    }

    /**
     * Combines this parser with another to form a builder that accumulates the parse results.
     *
     * @param pb  the second parser
     * @param <B> the result type of the second parser
     * @return an {@link ApplyBuilder} that accumulates the parse results
     */
    public <B> ApplyBuilder.ApplyBuilder2<I, A, B> and(Parser<I, B> pb) {
        return new ApplyBuilder.ApplyBuilder2<>(this, pb);
    }

    /**
     * Combines this parser with another to form a parser that applies two parsers,
     * and if both are successful, returns the result of the left-hand parser.
     *
     * @param pb  the second parser
     * @param <B> the result type of the second parser
     * @return a parser that applies two parsers consecutively and returns the result of the first
     */
    public <B> Parser<I, A> andL(Parser<I, B> pb) {
        return this.and(pb).map((a, b) -> a);
    }

    /**
     * Combines this parser with another to form a parser that applies two parsers,
     * and if both are successful, returns the result of the right-hand parser.
     *
     * @param pb  the second parser
     * @param <B> the result type of the second parser
     * @return a parser that applies two parsers consecutively and returns the result of the second
     */
    public <B> Parser<I, B> andR(Parser<I, B> pb) {
        return this.and(pb).map((a, b) -> b);
    }

    /**
     * A parser that repeatedly applies this parser until it fails,
     * and then returns a list of the results.
     * If this parser fails on the first attempt, the parser succeeds with an empty list.
     *
     * @return a parser that applies this parser zero or more times until it fails
     */
    public Parser<I, IList<A>> many() {
        if (Utils.ifRefClass(this).map(Ref::isInitialised).orElse(true)
                && acceptsEmpty().get()) {
            throw new RuntimeException("Cannot construct a many parser from one that accepts empty");
        }

        return new Parser<>(LTRUE) {
            @Override
            public Result<I, IList<A>> apply(Input<I> in) {
                IList<A> accumulator = IList.empty();
                while (true) {
                    if (!in.isEof()) {
                        final Result<I, A> r = Parser.this.apply(in);
                        if (r.isSuccess()) {
                            final Result.Success<I, A> success = (Result.Success<I, A>) r;
                            accumulator = accumulator.add(success.value());
                            in = success.next();
                            continue;
                        }
                        return Result.success(accumulator.reverse(), in);
                    }
                    return Result.success(accumulator.reverse(), in);
                }
            }
        };
    }

    /**
     * A parser that repeatedly applies this parser until the end parser succeeds,
     * and then returns a list of the results.
     *
     * @param end the end parser
     * @param <B> the result type of the end parser
     * @return a parser that applies this parser zero or more times until the end parser succeeds
     */
    public <B> Parser<I, IList<A>> manyTill(Parser<I, B> end) {
        return new Parser<>(end.acceptsEmpty()) {

            public Result<I, IList<A>> apply(Input<I> in) {
                IList<A> acc = IList.empty();
                while (true) {
                    if (!in.isEof()) {
                        Result<I, B> r = end.apply(in);
                        if (r.isSuccess()) {
                            final Result.Success<I, B> succ = (Result.Success<I, B>) r;
                            in = succ.next();
                            break;
                        }
                        Result<I, A> r2 = Parser.this.apply(in);
                        if (r2.isSuccess()) {
                            final Result.Success<I, A> succ = (Result.Success<I, A>) r2;
                            acc = acc.add(succ.value());
                            in = succ.next();
                        } else {
                            return ((Result.Failure<I, A>) r2).cast();
                        }
                    }
                }
                return Result.success(acc.reverse(), in);
            }
        };
    }

    /**
     * A parser that applies this parser one or more times until it fails,
     * and then returns a non-empty list of the results.
     * If this parser fails on the first attempt, the parser fails.
     *
     * @return a parser that applies this parser repeatedly until it fails
     */
    public Parser<I, IList<A>> many1() {
        return this.and(this.many())
                .map(a -> l -> l.add(a));
    }

    /**
     * A parser that applies this parser zero or more times until it fails,
     * and discards the results.
     * If this parser fails on the first attempt, the parser succeeds.
     *
     * @return a parser that applies this parser repeatedly until it fails
     */
    public Parser<I, Unit> skipMany() {
        return this.many()
                .map(u -> Unit.UNIT);
    }

    /**
     * A parser that applies this parser zero or more times until it fails,
     * alternating with calls to the separator parser.
     * The results of this parser are collected in a list and returned by the parser.
     *
     * @param sep   the separator parser
     * @param <SEP> the separator type
     * @return a parser that applies this parser zero or more times alternated with the separator parser
     */
    public <SEP> Parser<I, IList<A>> sepBy(Parser<I, SEP> sep) {
        return this.sepBy1(sep).map(l -> l)
                .or(pure(IList.empty()));
    }

    /**
     * A parser that applies this parser one or more times until it fails,
     * alternating with calls to the separator parser.
     * The results of this parser are collected in a non-empty list and returned by the parser.
     *
     * @param sep   the separator parser
     * @param <SEP> the separator type
     * @return a parser that applies this parser one or more times alternated with the separator parser
     */
    public <SEP> Parser<I, IList<A>> sepBy1(Parser<I, SEP> sep) {
        return this.and(sep.andR(this).many())
                .map(a -> l -> l.add(a));
    }

    /**
     * A parser that applies this parser, and if it succeeds,
     * returns the result wrapped in an {@link Optional},
     * otherwise returns an empty {@code Optional}.
     *
     * @return a parser that returns an optional result
     */
    public Parser<I, Optional<A>> optional() {
        return this.map(Optional::of)
                .or(pure(Optional.empty()));
    }

    /**
     * A parser for expressions with enclosing symbols.
     * Applies the open parser, then this parser, and then the close parser.
     * If all three succeed, the result of this parser is returned.
     *
     * @param open    the open symbol parser
     * @param close   the close symbol parser
     * @param <OPEN>  the open parser result type
     * @param <CLOSE> the close parser result type
     * @return a parser for expressions with enclosing symbols
     */
    public <OPEN, CLOSE> Parser<I, A> between(Parser<I, OPEN> open, Parser<I, CLOSE> close) {
        return open.andR(this).andL(close);
    }

    /**
     * A parser for an operand, followed by zero or more operands that are separated by operators.
     * The operators are right-associative.
     *
     * @param op the parser for the operator
     * @param a  the value to return if there are no operands
     * @return a parser for right-associative operator expressions
     */
    public Parser<I, A> chainr(Parser<I, BinaryOperator<A>> op, A a) {
        return this.chainr1(op).or(pure(a));
    }

    /**
     * Parse right-associative operator expressions.
     *
     * @param op the parser for the binary operator
     * @return a parser that parses right-associative operator expressions
     */
    public Parser<I, A> chainr1(Parser<I, BinaryOperator<A>> op) {
        return this.and(
                op.and(this)
                        .map(Tuple::of)
                        .many()
        ).map(Utils::reduce);
    }

    /**
     * Parse right-associative operator expressions with an initial value.
     *
     * @param op the parser for the binary operator
     * @param a  the initial value
     * @return a parser that parses right-associative operator expressions with an initial value
     */
    public Parser<I, A> chainl(Parser<I, BinaryOperator<A>> op, A a) {
        return this.chainl1(op).or(pure(a));
    }

    /**
     * Parse left-associative operator expressions.
     *
     * @param op the parser for the binary operator
     * @return a parser that parses left-associative operator expressions
     */
    public Parser<I, A> chainl1(Parser<I, BinaryOperator<A>> op) {
        final Parser<I, UnaryOperator<A>> plo =
                op.and(this)
                        .map((f, y) -> x -> f.apply(x, y));
        return this.and(plo.many())
                .map((a, lf) -> lf.foldLeft(a, (acc, f) -> f.apply(acc)));
    }
}
