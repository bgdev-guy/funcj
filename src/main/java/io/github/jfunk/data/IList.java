package io.github.jfunk.data;

import io.github.jfunk.functions.BiFunctionFlipper;
import io.github.jfunk.functions.BinaryOperatorFlipper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Simple recursive, immutable linked list.
 * <p>
 * Each {@code IList} is either empty or it has a head element value and a tail.
 * The tail is itself an {@code IList}.
 * Null elements are not allowed.
 *
 * @param <T> the element type
 */
public class IList<T> implements Iterable<T> {

    private final T head;
    private final IList<T> tail;
    private final boolean isEmpty;

    // Static Empty instance
    private static final IList<?> EMPTY = new IList<>();

    private IList() {
        this.head = null;
        this.tail = null;
        this.isEmpty = true;
    }

    private IList(T head, IList<T> tail) {
        this.head = Objects.requireNonNull(head);
        this.tail = Objects.requireNonNull(tail);
        this.isEmpty = false;
    }

    /**
     * Construct an empty list.
     *
     * @param <T> the element type
     * @return an empty list
     */
    @SuppressWarnings("unchecked")
    public static <T> IList<T> empty() {
        return (IList<T>) EMPTY;
    }

    /**
     * Construct a list with one element.
     *
     * @param elem element
     * @param <T>  element type
     * @return the new list with one element
     */
    public static <T> IList<T> of(T elem) {
        return new IList<>(elem, empty());
    }

    /**
     * Construct a list with one or more elements.
     *
     * @param elem  the first element
     * @param elems the remaining elements
     * @param <T>   the element type
     * @return the new list with one or more element
     */
    @SafeVarargs
    public static <T> IList<T> of(T elem, T... elems) {
        IList<T> list = ofArray(elems);
        return list.add(elem);
    }

    /**
     * Construct a list from an array.
     *
     * @param elems the array of elements
     * @param <T>   the element type
     * @return the new list with multiple elements
     */
    public static <T> IList<T> ofArray(T[] elems) {
        IList<T> list = empty();
        for (int i = elems.length - 1; i >= 0; --i) {
            list = list.add(elems[i]);
        }
        return list;
    }

    /**
     * Create a new list by adding an element to the head of this list.
     *
     * @param head the element to add onto head of this list
     * @return the new list
     */
    public IList<T> add(T head) {
        return new IList<>(head, this);
    }

    /**
     * Return true if this list is empty otherwise false
     *
     * @return true if this list is empty otherwise false
     */
    public boolean isEmpty() {
        return isEmpty;
    }

    /**
     * Return the head element of this list.
     *
     * @return the head of this list.
     * @throws UnsupportedOperationException if the list is empty.
     */
    public T head() {
        if (isEmpty) {
            throw new UnsupportedOperationException("Cannot take the head of an empty list");
        }
        return head;
    }

    /**
     * Return the tail of this list.
     *
     * @return the tail of this list.
     * @throws UnsupportedOperationException if the list is empty.
     */
    public IList<T> tail() {
        if (isEmpty) {
            throw new UnsupportedOperationException("Cannot take the tail of an empty list");
        }
        return tail;
    }

    /**
     * Returns the element at the specified position in this list.
     *
     * @param index the position of the element to return
     * @return the element of this list at the specified position.
     * @throws IndexOutOfBoundsException if the index is out of bounds.
     */
    public T get(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds");
        } else if (index == 0) {
            return head();
        } else {
            return tail().get(index - 1);
        }
    }

    /**
     * Reverse the list.
     *
     * @return a new list with the elements in reverse order
     */
    public IList<T> reverse() {
        IList<T> reversed = empty();
        IList<T> current = this;
        while (!current.isEmpty()) {
            reversed = reversed.add(current.head());
            current = current.tail();
        }
        return reversed;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private IList<T> current = IList.this;

            @Override
            public boolean hasNext() {
                return !current.isEmpty();
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                T value = current.head();
                current = current.tail();
                return value;
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IList<?> iList = (IList<?>) o;
        return isEmpty == iList.isEmpty &&
                Objects.equals(head, iList.head) &&
                Objects.equals(tail, iList.tail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(head, tail, isEmpty);
    }

    /**
     * Convert a list of {@link Character}s into a {@link String}.
     *
     * @param l the list of {@code Character}s
     * @return a {@code String}
     */
    public static String listToString(IList<Character> l) {
        return l.foldLeft(new StringBuilder(), StringBuilder::append).toString();
    }

    @Override
    public String toString() {
        if (isEmpty) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        IList<T> current = this;
        while (!current.isEmpty()) {
            sb.append(current.head()).append(", ");
            current = current.tail();
        }
        sb.setLength(sb.length() - 2); // Remove the last ", "
        sb.append("]");
        return sb.toString();
    }

    // Additional methods from previous context

    /**
     * Map function over the list.
     *
     * @param mapper the function to apply to each element
     * @param <R>    the result type
     * @return a new list with the mapped elements
     */
    public <R> IList<R> map(Function<T, R> mapper) {
        if (isEmpty) {
            return empty();
        } else {
            return tail().map(mapper).add(mapper.apply(head()));
        }
    }

    /**
     * Match function to handle both non-empty and empty cases.
     *
     * @param nonEmptyCase the function to apply if the list is non-empty
     * @param emptyCase    the supplier to provide a result if the list is empty
     * @param <R>          the result type
     * @return the result of applying the appropriate function
     */
    public <R> R match(Function<IList<T>, R> nonEmptyCase, Supplier<R> emptyCase) {
        if (isEmpty) {
            return emptyCase.get();
        } else {
            return nonEmptyCase.apply(this);
        }
    }

    /**
     * Fold left function over the list.
     *
     * @param identity the initial value
     * @param operator the binary operator to apply
     * @param <R>      the result type
     * @return the result of folding the list
     */
    public <R> R foldLeft(R identity, BiFunctionFlipper<R,T,R> operator) {
        R result = identity;
        for (T element : this) {
            result = operator.apply(result, element);
        }
        return result;
    }

    /**
     * Fold left function over the list with at least one element.
     *
     * @param operator the binary operator to apply
     * @return the result of folding the list
     */
    public T foldLeft1(BinaryOperatorFlipper<T> operator) {
        if (isEmpty) {
            throw new UnsupportedOperationException("Cannot fold an empty list");
        }
        T result = head();
        IList<T> current = tail();
        while (!current.isEmpty()) {
            result = operator.apply(result, current.head());
            current = current.tail();
        }
        return result;
    }

    /**
     * Fold right function over the list.
     *
     * @param identity the initial value
     * @param operator the binary operator to apply
     * @param <R>      the result type
     * @return the result of folding the list
     */
    public <R> R foldRight(R identity, BiFunctionFlipper<T,R,R> operator) {
        if (isEmpty) {
            return identity;
        } else {
            return operator.apply(head(), tail().foldRight(identity, operator));
        }
    }

    /**
     * Convert the list to a stream.
     *
     * @return a stream of the list elements
     */
    public Stream<T> stream() {
        return StreamSupport.stream(spliterator(), false);
    }
}