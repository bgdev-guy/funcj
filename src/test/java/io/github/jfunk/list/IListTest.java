package io.github.jfunk.list;

import io.github.jfunk.data.IList;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class IListTest {

    @Test
    void testMapEmptyList() {
        IList<Integer> emptyList = IList.empty();
        Function<Integer, Integer> mapper = x -> x * 2;
        IList<Integer> result = emptyList.map(mapper);
        assertTrue(result.isEmpty(), "Mapping an empty list should return an empty list");
    }

    @Test
    void testMapSingleElementList() {
        IList<Integer> singleElementList = IList.of(1);
        Function<Integer, Integer> mapper = x -> x * 2;
        IList<Integer> result = singleElementList.map(mapper);
        assertFalse(result.isEmpty(), "Result list should not be empty");
        assertEquals(2, result.head(), "Mapped value should be 2");
        assertTrue(result.tail().isEmpty(), "Tail of the result list should be empty");
    }

    @Test
    void testMapMultipleElementsList() {
        IList<Integer> list = IList.of(1, 2, 3);
        Function<Integer, Integer> mapper = x -> x * 2;
        IList<Integer> result = list.map(mapper);
        assertFalse(result.isEmpty(), "Result list should not be empty");
        assertEquals(2, result.head(), "First mapped value should be 2");
        assertEquals(4, result.tail().head(), "Second mapped value should be 4");
        assertEquals(6, result.tail().tail().head(), "Third mapped value should be 6");
        assertTrue(result.tail().tail().tail().isEmpty(), "Tail of the result list should be empty");
    }
}