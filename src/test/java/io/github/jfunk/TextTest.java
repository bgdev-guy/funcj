package io.github.jfunk;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.runner.RunWith;

import java.io.StringReader;


@RunWith(JUnitQuickcheck.class)
public class TextTest {
    private static <T> void parseSuccess(Parser<Character, T> parser, String s, T exp) {
        Assert.assertEquals(exp, parseSuccess(parser, Input.of(s)));
        Assert.assertEquals(exp, parseSuccess(parser, Input.of(new StringReader(s))));
    }

    private static <T> T parseSuccess(Parser<Character, T> parser, Input<Character> in) {
        return parser.apply(in).getOrThrow();
    }

    private static <T> void parseFailure(Parser<Character, T> parser, String s) {
        Assert.assertFalse(parse(parser, Input.of(s)).isSuccess());
        Assert.assertFalse(parse(parser, Input.of(new StringReader(s))).isSuccess());
    }

    private static <T> Result<Character, T> parse(Parser<Character, T> parser, Input<Character> in) {
        return parser.apply(in);
    }

    @Property
    public void testAlpha(char c) {
        final Result<Character, Character> res = Text.alpha.parse(Input.of("" + c));
        Assert.assertEquals("alpha parser applied to " + c, Character.isAlphabetic(c), res.isSuccess());
    }

    @Property
    public void testDigit(char c) {
        final Result<Character, Character> res = Text.digit.parse(Input.of("" + c));
        Assert.assertEquals("digit parser applied to " + c, Character.isDigit(c), res.isSuccess());
    }

    @Property
    public void testAlphaNum(char c) {
        final Result<Character, Character> res = Text.alphaNum.parse(Input.of("" + c));
        Assert.assertEquals("alphaNum parser applied to " + c, Character.isLetterOrDigit(c), res.isSuccess());
    }

    @Property
    public void testWs(char c) {
        final Result<Character, Character> res = Text.ws.parse(Input.of("" + c));
        Assert.assertEquals("ws parser applied to " + c, Character.isWhitespace(c), res.isSuccess());
    }

    @Property
    public void testIntr(int i) {
        {
            final Result<Character, Integer> res = Text.intr.parse(Input.of("" + i));
            Assert.assertEquals("alpha parser applied to " + i, i, res.getOrThrow().intValue());
        }

        if (i > 0) {
            final Result<Character, Integer> res = Text.uintr.parse(Input.of("" + i));
            Assert.assertEquals("alpha parser applied to " + i, i, res.getOrThrow().intValue());
        }
    }

    @Property
    public void testLng(long l) {
        {
            final Result<Character, Long> res = Text.lng.parse(Input.of("" + l));
            Assert.assertEquals("alpha parser applied to " + l, l, res.getOrThrow().longValue());
        }

        if (l > 0) {
            final Result<Character, Long> res = Text.ulng.parse(Input.of("" + l));
            Assert.assertEquals("alpha parser applied to " + l, l, res.getOrThrow().longValue());
        }
    }

    @Property
    public void testDbl(double d) {
        testDblImpl(d);
        testDblImpl(1.0/d);
    }

    @Property
    public void testDbl(long mi, long mf, boolean signB, byte exp) {
        final double sign = signB ? 1.0 : -1.0;
        final int mfd = 1 + (int) Math.log10(mf);
        final double d = ((double) mi + (double) mf / Math.pow(10.0, mfd)) * Math.pow(10.0, sign * exp);

        testDblImpl(d);

        if (Math.abs(d) > 1e-20) {
            testDblImpl(1.0 / d);
        }

        if (mf >= 0) {
            final String s = mi + "." + mf + "E" + exp;
            testDblImpl(s);
        }

    }

    private static void testDblImpl(double d) {
        if (Double.isInfinite(d) || Double.isNaN(d)) {
            return;
        }

        final double eps = Math.abs(d * 1e-12);

        final String s = Double.toString(d);
        final Result<Character, Double> res = Text.dble.apply(Input.of(s));

        Assert.assertTrue("Parsing double : " + s, res.isSuccess());

        Assert.assertEquals("Round-tripped double : " + d, d, res.getOrThrow(), eps);
    }

    private static void testDblImpl(String s) {
        final Result<Character, Double> res = Text.dble.apply(Input.of(s));

        try {
            final double d = Double.parseDouble(s);
            Assert.assertTrue("Parsing double expected to succeed: " + s, res.isSuccess());

            final double eps = Math.abs(d * 1e-12);
            Assert.assertEquals("", d, res.getOrThrow(), eps);
        } catch (NumberFormatException ex) {
            Assert.assertFalse("Parsing double expected to fail: " + s, res.isSuccess());
        }
    }

    @Property
    public void testString(String s) {
        Assume.assumeFalse(s.isEmpty());

        final Parser<Character, String> p = Text.string(s);
        final Result<Character, String> r = p.parse(Input.of(s));
        Assert.assertEquals("string(" + s + ").parse(" + s + ")", s, r.getOrThrow());
    }
//
//    private static <T> Parser<Character, T> manyTill(Parser<Character, T> end) {
//
//    }
//
//    @Test
//    public void testBlockComment() {
//        final Parser<Character, String> start = Text.string("/*");
//        final Parser<Character, String> end = Text.string("*/");
//        final Result<Character, Unit> p =
//                start
//                        .andR()
//
//
//    }
}
