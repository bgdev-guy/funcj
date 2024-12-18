package io.github.jfunk.inputs;

import io.github.jfunk.Input;

public record CharSequenceInput(int position, CharSequence data) implements Input<Character> {

    public CharSequenceInput(CharSequence data) {
        this(0, data);
    }

    @Override
    public boolean isEof() {
        return position >= data.length();
    }

    @Override
    public Character get() {
        return data.charAt(position);
    }

    @Override
    public Input<Character> next() {
        return new CharSequenceInput(position + 1, data);
    }

}
