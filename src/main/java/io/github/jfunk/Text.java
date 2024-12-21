package io.github.jfunk;

import static io.github.jfunk.Combinators.*;

/**
 * Parser combinators for working with {@link Character} streams.
 */
public abstract class Text {
    /**
     * A parser that succeeds if the next input symbol is an alphabetic letter.
     */
    public static final Parser<Character, Character> alpha = satisfy("letter", c -> Character.isAlphabetic((int) c));
    /**
     * A parser that succeeds if the next input symbol is a numeric digit.
     */
    public static final Parser<Character, Character> digit = satisfy("digit", Character::isDigit);
    /**
     * A parser that succeeds if the next input symbol is a numeric digit.
     */
    public static final Parser<Character, Character> nonZeroDigit = satisfy(
            "nonZeroDigit",
            c -> c != '0' && Character.isDigit(c));
    /**
     * A parser that succeeds if the next input symbol is a letter or a digit.
     */
    public static final Parser<Character, Character> alphaNum = satisfy("letterOrDigit", Character::isLetterOrDigit);
    /**
     * A parser that succeeds if the next input symbol is whitespace.
     */
    public static final Parser<Character, Character> ws = satisfy("ws", Character::isWhitespace);
    public static final Parser<Character, Boolean> sign =
            choice(
                    chr('+').andR(Parser.pure(true)),
                    chr('-').andR(Parser.pure(false)),
                    Parser.pure(true)
            );
    private static final Parser<Character, Integer> uintrZero =
            chr('0').map(zs -> 0);
    private static final Parser<Character, Integer> uintrNotZero =
            nonZeroDigit.and(digit.many())
                    .map(d -> ds -> ds.add(d))
                    .map(ds -> ds.map(Text::digitToInt))
                    .map(is -> is.foldLeft1((acc, x) -> acc * 10 + x));
    /**
     * A parser for an unsigned integer.
     */
    public static final Parser<Character, Integer> uintr = uintrZero.or(uintrNotZero);
    /**
     * A parser for a signed integer.
     */
    public static final Parser<Character, Integer> intr =
            sign.and(uintr)
                    .map((sign, i) -> sign ? i : -i);
    private static final Parser<Character, Integer> expnt =
            (chr('e').or(chr('E')))
                    .andR(intr);
    private static final Parser<Character, Long> ulngZero =
            chr('0').map(zs -> 0L);
    private static final Parser<Character, Long> ulngNotZero =
            nonZeroDigit.and(digit.many())
                    .map(d -> ds -> ds.add(d))
                    .map(ds -> ds.map(Text::digitToInt))
                    .map(ds -> ds.foldLeft(0L, (acc, x) -> acc * 10L + x));
    /**
     * A parser for an unsigned long.
     */
    public static final Parser<Character, Long> ulng = ulngZero.or(ulngNotZero);
    /**
     * A parser for an unsigned long.
     */
    public static final Parser<Character, Long> lng =
            sign.and(ulng)
                    .map((sign, i) -> sign ? i : -i);
    private static final Parser<Character, Double> floating =
            digit.many()
                    .map(ds -> ds.map(Text::digitToInt))
                    .map(l -> l.foldRight(0.0, (d, acc) -> d + acc / 10.0) / 10.0);
    /**
     * A parser for a floating point number.
     */
    public static final Parser<Character, Double> dble =
            sign.and(ulng)
                    .and((chr('.').andR(floating)).optional())
                    .and(expnt.optional())
                    .map((sn, i, f, exp) -> {
                        double r = i.doubleValue();
                        if (f.isPresent()) {
                            r += f.get();
                        }
                        if (exp.isPresent()) {
                            r = r * Math.pow(10.0, exp.get());
                        }
                        return sn ? r : -r;
                    });

    /**
     * Specialisation of {@link Parser#pure(Object)} for {@code Chr}.
     * Construct a parser that always returns the given value, without consuming any input.
     *
     * @param c the char value
     * @return a parser that always returns the given char
     */
    public static Parser<Character, Character> pure(char c) {
        return Parser.pure(c);
    }

    /**
     * A parser that succeeds if the next input symbol equals the given char {@code c},
     * and returns the value.
     *
     * @param c the value expected by the parser
     * @return as parser that succeeds if the next input symbol equals the given char {@code c}
     */
    public static Parser<Character, Character> chr(char c) {
        return value(c);
    }

    public static int digitToInt(Character c) {
        return Character.getNumericValue(c);
    }

    /**
     * A parser that succeeds if it can extract the given string from the input.
     *
     * @param s the expected string
     * @return a parser for the given string value
     */
    public static Parser<Character, String> string(String s) {
        return switch (s.length()) {
            case 0 -> Combinators.fail();
            case 1 -> chr(s.charAt(0)).map(Object::toString);
            default -> new Parser<>(() -> false) {
                @Override
                public Result<Character, String> apply(Input<Character> in) {
                    for (int i = 0; i < s.length(); ++i) {
                        if (in.isEof()) {
                            return Utils.failureEof(this, in);
                        } else if (!in.get().equals(s.charAt(i))) {
                            return Utils.failure(in);
                        } else {
                            in = in.next();
                        }
                    }

                    return Result.success(s, in);
                }
            };
        };
    }
}
