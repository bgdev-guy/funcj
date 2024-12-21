package io.github.jfunk.enumexpr;

import io.github.jfunk.Input;

import java.util.*;

public record ListInput<T>(int position,List<T> data) implements Input<T> {

    ListInput(List<T> data) {
        this(0,data);
    }

    public Input<T> setPosition(int position) {
        return new ListInput<>(position,data);
    }

    @Override
    public String toString() {
        final String dataStr = isEof() ? "EOF" : "...";
        return "ListInput{" + position + ",data=\"" + dataStr + "\"";
    }

    @Override
    public boolean isEof() {
        return position >= data.size();
    }

    @Override
    public T get() {
        return data.get(position);
    }

    @Override
    public Input<T> next() {
        return new ListInput<>(position+1,data);
    }

    @Override
    public int position() {
        return position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ListInput<T> that = (ListInput<T>) o;
        return position == that.position &&
                data == that.data;
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, position);
    }
}
