package io.github.jfunk.data;


import io.github.jfunk.functions.BiFunctionFlipper;
import io.github.jfunk.functions.BinaryOperatorFlipper;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Simple recursive, immutable linked list.
 * <p>
 * Each {@code IList} is either {@link Empty} or it is {@link NonEmpty},
 * in which case it has a head element value and a tail.
 * The tail is itself an {@code IList}.
 * Null elements are not allowed.
 *
 * @param <T> the element type
 */
public abstract class IList<T> implements Iterable<T> {

    /**
     * Construct an empty list.
     *
     * @param <T> the element type
     * @return an empty list
     */
    @SuppressWarnings("unchecked")
    public static <T> IList<T> empty() {
        return (IList<T>) Empty.EMPTY;
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
    public static <T> NonEmpty<T> of(T elem) {
        return IList.<T>empty().add(elem);
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
    public static <T> NonEmpty<T> of(T elem, T... elems) {
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
    public NonEmpty<T> add(T head) {
        return new NonEmpty<>(head, this);
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

    /**
     * Return true if this list is empty otherwise false
     *
     * @return true if this list is empty otherwise false
     */
    public abstract boolean isEmpty();

    /**
     * Returns Optional.empty() if this list is empty,
     * otherwise it returns an {@link Optional} which wraps the non-empty list.
     *
     * @return the Optional.empty() if this list is empty, otherwise an {@code Optional} which wraps the
     * non-empty list.
     */
    public abstract Optional<NonEmpty<T>> nonEmptyOpt();

    /**
     * Return the head element of this list.
     *
     * @return the head of this list.
     * @throws UnsupportedOperationException if the list is empty.
     */
    public abstract T head();

    /**
     * Return the tail of this list.
     *
     * @return the tail of this list.
     * @throws UnsupportedOperationException if the list is empty.
     */
    public abstract IList<T> tail();

    /**
     * Returns the element at the specified position in this list.
     *
     * @param index the position of the element to return
     * @return the element of this list at the specified position.
     * @throws IndexOutOfBoundsException if the index is out of bounds.
     */
    public abstract T get(int index);

    /**
     * Append the contents of this list to a {@link StringBuilder}.
     *
     * @param sb the StringBuilder to be appended to
     * @return the StringBuilder
     */
    public abstract StringBuilder append(StringBuilder sb);

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
        int hashCode = 1;
        for (T t : this)
            hashCode = 31 * hashCode + Objects.hashCode(t);
        return hashCode;
    }

    /**
     * Type-safe list equality.
     *
     * @param rhs the list to be compared
     * @return true if this list and rhs are equal in terms of their elements.
     */
    public abstract boolean equals(IList<T> rhs);

    /**
     * Apply one of two functions depending on whether this list is empty or not.
     *
     * @param nonEmptyF the function to be applied if the list is non-empty
     * @param emptyF    the function to be applied if the list is empty
     * @param <S>       return type of both functions
     * @return the result of applying the appropriate function.
     */
    public abstract <S> S match(Function<NonEmpty<T>, S> nonEmptyF, Function<Empty<T>, S> emptyF);

    /**
     * Create a new list by appending an element to the end of this list.
     *
     * @param l the list to be appended to the end of this list
     * @return the new list
     */
    public abstract IList<T> appendAll(IList<? extends T> l);

    /**
     * @return the length of this list.
     */
    public abstract int size();

    /**
     * @return this list in reverse.
     */
    public abstract IList<T> reverse();

    /**
     * Apply the function {@code f} to each element in this list,
     * and store the results in a new list.
     *
     * @param f   the function to be applied to each element
     * @param <U> the function return type
     * @return the new list
     */
    public abstract <U> IList<U> map(Function<? super T, ? extends U> f);

    /**
     * Apply a function that returns an {@code IList} to each element
     * in this list and concatenate the results into a single list.
     *
     * @param f   the function to be applied
     * @param <U> the element type for the list returned by the function
     * @return the new list
     */
    public abstract <U> IList<U> flatMap(Function<? super T, IList<? extends U>> f);

    /**
     * Right-fold a function over this list.
     *
     * @param f   the function to be folded
     * @param z   the initial value for the fold (typically the identity value of {@code f})
     * @param <U> the fold result type
     * @return the folded result
     */
    public abstract <U> U foldRight(BiFunctionFlipper<T, U, U> f, U z);

    /**
     * Left-fold a function over this list.
     *
     * @param f   the function to be folded
     * @param z   the initial value for the fold (typically the identity value of {@code f})
     * @param <U> the fold result type
     * @return the folded result
     */
    public abstract <U> U foldLeft(BiFunctionFlipper<U, T, U> f, U z);

    /**
     * Create a {@link Spliterator}.
     *
     * @return the spliterator
     */
    @Override
    public abstract Spliterator<T> spliterator();

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

    /**
     * Create an {@link Iterator} over this list.
     *
     * @return the iterator
     */
    @Override
    public abstract Iterator<T> iterator();

    /**
     * Convert to a Java List implementation, albeit an immutable one.
     *
     * @return the Java List.
     */
    public abstract List<T> toList();


    /**
     * An empty list node.
     *
     * @param <T> the element type
     */
    public static final class Empty<T> extends IList<T> {
        static final Empty<?> EMPTY = new Empty<Void>();

        private Empty() {
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public Optional<NonEmpty<T>> nonEmptyOpt() {
            return Optional.empty();
        }

        @Override
        public T head() {
            throw new UnsupportedOperationException("Cannot take the head of an empty list");
        }

        @Override
        public IList<T> tail() {
            throw new UnsupportedOperationException("Cannot take the tail of an empty list");
        }

        @Override
        public T get(int index) {
            throw new IndexOutOfBoundsException(
                    "Index " + index + " out of bounds for an " + size() + " element list");
        }

        @Override
        public String toString() {
            return "[]";
        }

        @Override
        public boolean equals(IList<T> rhs) {
            return rhs.isEmpty();
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public StringBuilder append(StringBuilder sb) {
            return sb;
        }

        @Override
        public <S> S match(Function<NonEmpty<T>, S> nonEmptyF, Function<Empty<T>, S> emptyF) {
            return emptyF.apply(this);
        }

        @Override
        @SuppressWarnings("unchecked")
        public IList<T> appendAll(IList<? extends T> l) {
            return (IList<T>) l;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public IList<T> reverse() {
            return empty();
        }

        @Override
        public <U> IList<U> map(Function<? super T, ? extends U> f) {
            return empty();
        }

        @Override
        public <U> IList<U> flatMap(Function<? super T, IList<? extends U>> f) {
            return empty();
        }

        @Override
        public <U> U foldRight(BiFunctionFlipper<T, U, U> f, U z) {
            return z;
        }

        @Override
        public <U> U foldLeft(BiFunctionFlipper<U, T, U> f, U z) {
            return z;
        }

        @Override
        public Spliterator<T> spliterator() {
            return new Spliterator<>() {
                @Override
                public boolean tryAdvance(Consumer<? super T> action) {
                    return false;
                }

                @Override
                public Spliterator<T> trySplit() {
                    return null;
                }

                @Override
                public long estimateSize() {
                    return size();
                }

                @Override
                public int characteristics() {
                    return Spliterator.IMMUTABLE + Spliterator.SIZED;
                }
            };
        }

        @Override
        public Iterator<T> iterator() {

            return new Iterator<>() {
                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public T next() {
                    throw new NoSuchElementException();
                }
            };
        }

        @Override
        public List<T> toList() {
            return Collections.emptyList();
        }
    }

    /**
     * A non-empty list node.
     *
     * @param <T> the element type
     */
    public static final class NonEmpty<T> extends IList<T> {

        private final T head;
        private final IList<T> tail;

        NonEmpty(T head, IList<T> tail) {
            this.head = Objects.requireNonNull(head);
            this.tail = Objects.requireNonNull(tail);
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public Optional<NonEmpty<T>> nonEmptyOpt() {
            return Optional.of(this);
        }

        @Override
        public T head() {
            return head;
        }

        @Override
        public IList<T> tail() {
            return tail;
        }

        @Override
        public T get(int index) {
            if (index < 0) {
                throw new IndexOutOfBoundsException("Index " + index + " out of bounds");
            } else if (index == 0) {
                return head;
            } else {
                IList<T> next = tail;
                for (int i = 1; i < index; ++i) {
                    if (next.isEmpty()) {
                        throw new IndexOutOfBoundsException("Index " + index + " out of bounds");
                    } else {
                        next = ((NonEmpty<T>) next).tail;
                    }
                }
                if (next.isEmpty()) {
                    throw new IndexOutOfBoundsException("Index " + index + " out of bounds");
                } else {
                    return ((NonEmpty<T>) next).head;
                }
            }
        }

        @Override
        public String toString() {
            final StringBuilder r = new StringBuilder("[");
            append(r).setCharAt(r.length() - 1, ']');
            return r.toString();
        }

        @Override
        public StringBuilder append(StringBuilder sb) {
            return tail.append(sb.append(head).append(','));
        }

        @Override
        public boolean equals(IList<T> rhs) {
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

        @Override
        public int hashCode() {
            int hashCode = 1;
            for (T elem : this) {
                hashCode = 31 * hashCode + elem.hashCode();
            }
            return hashCode;
        }

        @Override
        public <S> S match(Function<NonEmpty<T>, S> nonEmptyF, Function<Empty<T>, S> emptyF) {
            return nonEmptyF.apply(this);
        }

        @Override
        public IList<T> appendAll(IList<? extends T> l) {
            return new NonEmpty<>(head, tail.appendAll(l));
        }

        @Override
        public int size() {
            IList<T> pos = this;
            int length = 0;
            while (!pos.isEmpty()) {
                ++length;
                pos = pos.tail();
            }

            return length;
        }

        @Override
        public NonEmpty<T> reverse() {
            IList<T> r = IList.of();
            for (IList<T> n = this; !n.isEmpty(); n = n.tail()) {
                r = r.add(n.head());
            }
            return (NonEmpty<T>) r;
        }

        @Override
        public <U> NonEmpty<U> map(Function<? super T, ? extends U> f) {
            IList<U> r = empty();
            for (IList<T> n = reverse(); !n.isEmpty(); n = n.tail()) {
                r = r.add(f.apply(n.head()));
            }
            return (NonEmpty<U>) r;
        }

        @Override
        public <U> IList<U> flatMap(Function<? super T, IList<? extends U>> f) {
            IList<U> r = empty();
            for (IList<T> n = reverse(); !n.isEmpty(); n = n.tail()) {
                r = r.addAll(f.apply(n.head()));
            }
            return r;
        }

        @Override
        public <U> U foldRight(BiFunctionFlipper<T, U, U> f, U z) {
            return reverse().foldLeft(f.flip(), z);
        }

        @Override
        public <U> U foldLeft(BiFunctionFlipper<U, T, U> f, U z) {
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

        @Override
        public Iterator<T> iterator() {
            return new Iterator<>() {

                IList<T> n = NonEmpty.this;

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

        @Override
        public List<T> toList() {
            return new ListAdaptor<>(this);
        }
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
