package io.github.jfunk.data;


import io.github.jfunk.functions.BiFunctionFlipper;
import io.github.jfunk.functions.BinaryOperatorFlipper;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Simple recursive, immutable linked list.
 * <p>
 * Each {@code IList} is either {@link } or it is {@link IList},
 * in which case it has a head element value and a tail.
 * The tail is itself an {@code IList}.
 * Null elements are not allowed.
 *
 * @param <T> the element type
 */
public class IList<T> implements Iterable<T> {

    private final T head;
    private final IList<T> tail;
    private boolean isEmpty= true;

    public IList(T head, IList<T> tail) {
        this.head = Objects.requireNonNull(head);
        this.tail = Objects.requireNonNull(tail);
        this.isEmpty = false;
    }

    public IList() {
        this.head = null;
        this.tail = null;
    }

    /**
     * Construct an empty list.
     *
     * @param <T> the element type
     * @return an empty list
     */
    public static <T> IList<T> empty() {
        return new IList<>();
    }

    /**
     * Construct an empty list.
     *
     * @param <T> the element type
     * @return an empty list
     */
    public static <T> IList<T> of() {
        return empty();
    }

    /**
     * Construct a list with one element.
     *
     * @param elem element
     * @param <T>  element type
     * @return the new list with one element
     */
    public static <T> IList<T> of(T elem) {
        return new IList<T>().add(elem);
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
        return ofArray(elems).add(elem);
    }

    /**
     * Construct a list from an {@link Iterable} collection of elements.
     *
     * @param elems the iterable collection of elements
     * @param <T>   the element type
     * @return the new list with multiple elements
     */
    public static <T> IList<T> ofIterable(Iterable<T> elems) {
        IList<T> r = empty();
        for (T elem : elems) {
            r = r.add(elem);
        }
        return r.reverse();
    }

    /**
     * Construct a list from an array.
     *
     * @param elems the array of elements
     * @param <T>   the element type
     * @return the new list with multiple elements
     */
    public static <T> IList<T> ofArray(T[] elems) {
        IList<T> r = empty();
        for (int i = elems.length - 1; i >= 0; --i) {
            r = r.add(elems[i]);
        }
        return r;
    }

    /**
     * Concatenate two lists to form a new list
     *
     * @param l1  the first list
     * @param l2  the second list
     * @param <T> the element type
     * @return the new concatenated list
     */
    public static <T> IList<T> concat(IList<? extends T> l1, IList<? extends T> l2) {
        @SuppressWarnings("unchecked")
        IList<T> r = (IList<T>) l2;
        for (T elem : l1.reverse()) {
            r = r.add(elem);
        }
        return r;
    }

    /**
     * Convert a list of {@link Character}s into a {@link String}.
     *
     * @param l the list of {@code Character}s
     * @return a {@code String}
     */
    public static String listToString(IList<Character> l) {
        final StringBuilder sb = new StringBuilder(l.size());
        for (; !l.isEmpty(); l = l.tail()) {
            sb.append(l.head());
        }
        return sb.toString();
    }

    /**
     * Convert a {@link String} into a list of {@link Character}s.
     *
     * @param s the {@code String}
     * @return a list of {@code Character}s
     */
    public static IList<Character> stringToList(String s) {
        IList<Character> r = empty();
        for (int i = s.length() - 1; i >= 0; --i) {
            r = r.add(s.charAt(i));
        }
        return r;
    }

    /**
     * Create a new list by adding an element to the head of this list.
     *
     * @param head the element to add onto head of this list
     * @return the new list
     */
    public IList<T> add(T head) {
        return new IList<T>(head, this);
    }

    /**
     * Create a new list by adding multiple elements to the head of this list.
     *
     * @param l   the list to be added to the head of this list
     * @param <S> the list element type
     * @return the new list
     */
    public <S extends T> IList<T> addAll(IList<S> l) {
        IList<T> r = this;
        for (IList<S> next = l.reverse(); !next.isEmpty(); next = next.tail()) {
            r = r.add(next.head());
        }
        return r;
    }

    public boolean isEmpty() {
        return isEmpty;
    }


    public T head() {
        if (isEmpty){
            throw new UnsupportedOperationException("Cannot take the head of an empty list");
        }
        return head;
    }

    public IList<T> tail() {
        if (isEmpty){
            throw new UnsupportedOperationException("Cannot take the tail of an empty list");
        }
        return tail;
    }

    public T get(int index) {
        if (isEmpty) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for an empty list");
        }
        if (index < 0) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds");
        }
        if (index == 0) {
            return head;
        }
        IList<T> next = tail;
        for (int i = 1; i < index; ++i) {
            if (next.isEmpty()) {
                throw new IndexOutOfBoundsException("Index " + index + " out of bounds");
            } else {
                next = next.tail;
            }
        }
        if (next.isEmpty()) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds");
        } else {
            return next.head;
        }

    }

    @Override
    public String toString() {
        final StringBuilder r = new StringBuilder("[");
        append(r).setCharAt(r.length() - 1, ']');
        return r.toString();
    }

    public StringBuilder append(StringBuilder sb) {
        if (isEmpty) {
            return sb;
        }
        return tail.append(sb.append(head).append(','));
    }

    /**
     * List equality.
     *
     * @return true if this list and rhs are equal in terms of their size and elements.
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object rhs) {
        return this == rhs ||
                (rhs != null &&
                        getClass() == rhs.getClass() &&
                        equals((IList<T>) rhs));
    }

    @Override
    public int hashCode() {
        if (isEmpty) {
            return 0;
        }
        int hashCode = 1;
        for (T t : this)
            hashCode = 31 * hashCode + Objects.hashCode(t);
        return hashCode;
    }

    public boolean equals(IList<T> rhs) {
        if (this.isEmpty()) {
            return rhs.isEmpty();
        }
        if (rhs.isEmpty()) {
            return false;
        } else {
            for (T lhs : this) {
                if (rhs.isEmpty() || !lhs.equals(rhs.head())) {
                    return false;
                }

                rhs = rhs.tail();
            }

            return rhs.isEmpty();
        }
    }

    public <S> S match(Function<IList<T>, S> first, Function<IList<T>, S> second) {
        if (isEmpty) {
            return second.apply(this);
        }
        return first.apply(this);
    }

    public IList<T> appendAll(IList<T> l) {
        if (isEmpty) {
            return l;
        }
        return new IList<>(head, tail.appendAll(l));
    }

    public int size() {
        IList<T> pos = this;
        int length = 0;
        while (!pos.isEmpty()) {
            ++length;
            pos = pos.tail();
        }

        return length;
    }

    public IList<T> reverse() {
        if (isEmpty) {
            return this;
        }
        IList<T> r = IList.of();
        for (IList<T> n = this; !n.isEmpty(); n = n.tail()) {
            r = r.add(n.head());
        }
        return r;
    }

    public <U> IList<U> map(Function<? super T, ? extends U> f) {
        if(isEmpty){
            return empty();
        }
        IList<U> r = empty();
        for (IList<T> n = reverse(); !n.isEmpty(); n = n.tail()) {
            r = r.add(f.apply(n.head()));
        }
        return r;
    }

    public <U> IList<U> flatMap(Function<? super T, IList<? extends U>> f) {
        if (isEmpty) {
            return empty();
        }
        IList<U> r = empty();
        for (IList<T> n = reverse(); !n.isEmpty(); n = n.tail()) {
            r = r.addAll(f.apply(n.head()));
        }
        return r;
    }

    public <U> U foldRight(BiFunctionFlipper<T, U, U> f, U z) {
        if(isEmpty){
            return z;
        }
        return reverse().foldLeft(f.flip(), z);
    }

    public <U> U foldLeft(BiFunctionFlipper<U, T, U> f, U z) {
        if (isEmpty) {
            return z;
        }
        U r = z;
        for (IList<T> n = this; !n.isEmpty(); n = n.tail()) {
            r = f.apply(r, n.head());
        }
        return r;
    }

    /**
     * Right-fold a function over this non-empty list.
     *
     * @param f the function to be folded
     * @return the folded result
     */
    public T foldRight1(BinaryOperatorFlipper<T> f) {
        return reverse().foldLeft1(f.flip());
    }

    /**
     * Left-fold a function over this non-empty list.
     *
     * @param f the function to be folded
     * @return the folded result
     */
    public T foldLeft1(BinaryOperator<T> f) {
        T r = head;
        for (IList<T> n = tail; !n.isEmpty(); n = n.tail()) {
            r = f.apply(r, n.head());
        }
        return r;
    }

    @Override
    public Spliterator<T> spliterator() {
        return Spliterators.spliterator(
                this.iterator(),
                size(),
                Spliterator.IMMUTABLE + Spliterator.SIZED
        );
    }

    /**
     * Create a {@link Stream} onto this list.
     *
     * @return the new stream
     */
    public Stream<T> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * Create a parallel {@link Stream} onto this list.
     *
     * @return the new stream
     */
    public Stream<T> parallelStream() {
        return StreamSupport.stream(spliterator(), true);
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {

            IList<T> n = IList.this;

            @Override
            public boolean hasNext() {
                return !n.isEmpty();
            }

            @Override
            public T next() {
                final T head = n.head();
                n = n.tail();
                return head;
            }
        };
    }

    public List<T> toList() {
        return new ListAdaptor<>(this);
    }

    private static class ListAdaptor<T> extends AbstractSequentialList<T> {

        private final IList<T> impl;
        private final int size;

        ListAdaptor(IList<T> impl) {
            this.impl = impl;
            size = impl.size();
        }

        @Override
        public ListIterator<T> listIterator(int index) {

            return new ListIterator<>() {

                private int pos = index;
                private IList<T> node = move(impl, index);

                private IList<T> move(IList<T> node, int count) {
                    for (int i = 0; i < count; ++i) {
                        node = node.tail();
                    }
                    return node;
                }

                @Override
                public boolean hasNext() {
                    return pos < size;
                }

                @Override
                public T next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    } else {
                        final T ret = node.head();
                        node = node.tail();
                        ++pos;
                        return ret;
                    }
                }

                @Override
                public boolean hasPrevious() {
                    return pos >= 0;
                }

                @Override
                public T previous() {
                    if (!hasPrevious()) {
                        throw new NoSuchElementException();
                    } else {
                        --pos;
                        node = move(impl, pos);
                        ++pos;
                        return node.head();
                    }
                }

                @Override
                public int nextIndex() {
                    return pos;
                }

                @Override
                public int previousIndex() {
                    return pos - 1;
                }

                @Override
                public void remove() {
                    throw modError();
                }

                @Override
                public void set(T t) {
                    throw modError();
                }

                @Override
                public void add(T t) {
                    throw modError();
                }

                private UnsupportedOperationException modError() {
                    return new UnsupportedOperationException("IList can not be modified");
                }
            };
        }

        @Override
        public int size() {
            return size;
        }
    }
}
