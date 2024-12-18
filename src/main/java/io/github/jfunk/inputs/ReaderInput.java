package io.github.jfunk.inputs;

import io.github.jfunk.Input;
import io.github.jfunk.util.Exceptions;

import java.io.Reader;
import java.util.Objects;

public class ReaderInput implements Input<Character> {

    protected final Reader reader;
    protected final int position;
    protected final ReaderInput next;
    protected int current;
    protected boolean isEof = false;

    public ReaderInput(Reader reader) {
        this.position = 0;
        this.reader = reader;
        this.current = -1;
        this.next = null;
    }

    ReaderInput(char c, Reader reader) {
        this.position = 0;
        this.reader = reader;
        this.current = c;
        this.next = null;
    }

    @Override
    public String toString() {
        return "ReaderInput{current=\"" + current + "\",isEof=" + isEof + "}";
    }

    @Override
    public boolean isEof() {
        if (!isEof && current == -1) {
            Exceptions.wrap(() -> {
                current = reader.read();
                isEof = current == -1;
            });
        }
        return isEof;
    }

    @Override
    public Character get() {
        if (isEof()) {
            throw new RuntimeException("End of input");
        } else {
            return (char) current;
        }
    }

    @Override
    public Input<Character> next() {
        return new ReaderInput(this.reader);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReaderInput that = (ReaderInput) o;
        return position == that.position &&
                reader == that.reader;
    }

    public int position() {
        return position;
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, reader);
    }
}
