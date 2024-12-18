package io.github.jfunk;

import org.junit.Assert;
import org.junit.Test;

import java.io.CharArrayReader;

public class InputTest {
    private static final char[] charData = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'};

    @Test
    public void testStringInput() {
        testInput(Input.of(charData));
    }

    @Test
    public void testReaderInput() {
        testInput(Input.of(new CharArrayReader(charData)));
    }

    private void testInput(Input<Character> input) {
        Input<Character> curr = input;

        for (char c : charData) {
            Assert.assertFalse("", curr.isEof());
            Assert.assertEquals("", c, curr.get().charValue());


            Assert.assertFalse("", curr.isEof());
            Assert.assertEquals("", c, curr.get().charValue());

            final Input<Character> next = curr.next();

            curr.next();

            curr = next;
        }

        Assert.assertTrue("", curr.isEof());
    }
}
