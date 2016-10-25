/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Yuriy Budiyev [yuriy.budiyev@yandex.ru]
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.budiyev.android.wheels;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Object query API
 *
 * @see #from(Iterable)
 * @see #from(Object[])
 * @see #from(boolean[])
 * @see #from(byte[])
 * @see #from(short[])
 * @see #from(int[])
 * @see #from(long[])
 * @see #from(float[])
 * @see #from(double[])
 * @see #from(char[])
 * @see #from(CharSequence)
 */
public final class IterableQuery<T> extends AbstractIterableQuery<T> {
    private IterableQuery() {
    }

    /**
     * Add specified {@code element} to end of sequence
     */
    @NonNull
    public IterableQuery<T> add(@Nullable final T element) {
        enqueueTask(new Runnable() {
            @Override
            public void run() {
                getMutableIterable().add(element);
            }
        });
        return this;
    }

    /**
     * Add all elements of specified {@code iterable} to end of sequence
     */
    @NonNull
    public IterableQuery<T> add(@NonNull final Iterable<T> iterable) {
        enqueueTask(new Runnable() {
            @Override
            public void run() {
                List<T> list = getMutableIterable();
                if (iterable instanceof Collection<?>) {
                    list.addAll((Collection<T>) iterable);
                } else {
                    for (T element : iterable) {
                        list.add(element);
                    }
                }
            }
        });
        return this;
    }

    /**
     * Make union of this {@link IterableQuery} and specified {@link Iterable}
     */
    public IterableQuery<T> union(@NonNull final Iterable<T> iterable) {
        enqueueTask(new Runnable() {
            @Override
            public void run() {
                List<T> union = getMutableIterable();
                Collection<T> collection;
                if (iterable instanceof Collection<?>) {
                    collection = (Collection<T>) iterable;
                } else {
                    collection = CollectionUtils.copy(iterable);
                }
                union.removeAll(collection);
                union.addAll(collection);
            }
        });
        return this;
    }

    /**
     * Make intersection of this {@link IterableQuery} and specified {@link Iterable}
     */
    @NonNull
    public IterableQuery<T> intersect(@NonNull final Iterable<T> iterable) {
        enqueueTask(new Runnable() {
            @Override
            public void run() {
                List<T> intersection = getMutableIterable();
                Collection<T> collection;
                if (iterable instanceof Collection<?>) {
                    collection = (Collection<T>) iterable;
                } else {
                    collection = CollectionUtils.copy(iterable);
                }
                intersection.retainAll(collection);
            }
        });
        return this;
    }

    /**
     * Remove all elements that doesn't match specified {@code predicate}
     */
    @NonNull
    public IterableQuery<T> filter(@NonNull final Predicate<? super T> predicate) {
        enqueueTask(new Runnable() {
            @Override
            public void run() {
                Iterable<T> source = getIterable();
                List<T> filtered = new ArrayList<>();
                for (T element : source) {
                    if (predicate.apply(element)) {
                        filtered.add(element);
                    }
                }
                clearMutableIterable();
                setMutableIterable(filtered);
            }
        });
        return this;
    }

    /**
     * Apply specified {@code action} to all elements
     */
    @NonNull
    public IterableQuery<T> apply(@NonNull final Action<? super T> action) {
        enqueueTask(new Runnable() {
            @Override
            public void run() {
                Iterable<T> list = getIterable();
                for (T element : list) {
                    action.apply(element);
                }
            }
        });
        return this;
    }

    /**
     * Retain only first {@code count} of elements
     */
    @NonNull
    public IterableQuery<T> take(@IntRange(from = 0, to = Integer.MAX_VALUE) final int count) {
        if (count < 0) {
            throw new IllegalArgumentException();
        }
        enqueueTask(new Runnable() {
            @Override
            public void run() {
                int added = 0;
                List<T> taken = new ArrayList<>(count);
                for (T element : getIterable()) {
                    if (added == count) {
                        break;
                    }
                    taken.add(element);
                    added++;
                }
                clearMutableIterable();
                setMutableIterable(taken);
            }
        });
        return this;
    }

    /**
     * Retain only first sequence of elements that matches specified {@code predicate}
     */
    @NonNull
    public IterableQuery<T> takeWhile(@NonNull final Predicate<? super T> predicate) {
        enqueueTask(new Runnable() {
            @Override
            public void run() {
                List<T> taken = new ArrayList<>();
                for (T element : getIterable()) {
                    if (predicate.apply(element)) {
                        taken.add(element);
                    } else {
                        break;
                    }
                }
                clearMutableIterable();
                setMutableIterable(taken);
            }
        });
        return this;
    }

    /**
     * Remove first {@code count} of elements
     */
    @NonNull
    public IterableQuery<T> skip(@IntRange(from = 0, to = Integer.MAX_VALUE) final int count) {
        if (count < 0) {
            throw new IllegalArgumentException();
        }
        enqueueTask(new Runnable() {
            @Override
            public void run() {
                int position = 0;
                List<T> rest = new ArrayList<>();
                for (T element : getIterable()) {
                    if (position >= count) {
                        rest.add(element);
                    }
                    position++;
                }
                clearMutableIterable();
                setMutableIterable(rest);
            }
        });
        return this;
    }

    /**
     * Remove first sequence of elements that matches specified {@code predicate}
     */
    @NonNull
    public IterableQuery<T> skipWhile(@NonNull final Predicate<? super T> predicate) {
        enqueueTask(new Runnable() {
            @Override
            public void run() {
                boolean skip = true;
                List<T> rest = new ArrayList<>();
                for (T element : getIterable()) {
                    if (skip) {
                        skip = predicate.apply(element);
                    }
                    if (!skip) {
                        rest.add(element);
                    }
                }
                clearMutableIterable();
                setMutableIterable(rest);
            }
        });
        return this;
    }

    /**
     * Sort using specified {@code comparator}
     * <br>
     * Sorting algorithm is <b>unstable</b> (Heapsort)
     */
    @NonNull
    public IterableQuery<T> sort(@NonNull final Comparator<? super T> comparator) {
        enqueueTask(new Runnable() {
            @Override
            public void run() {
                CollectionUtils.sort(getMutableIterable(), comparator);
            }
        });
        return this;
    }

    /**
     * Reverse elements order
     */
    @NonNull
    public IterableQuery<T> reverse() {
        enqueueTask(new Runnable() {
            @Override
            public void run() {
                Collections.reverse(getMutableIterable());
            }
        });
        return this;
    }

    /**
     * Convert all elements using specified {@code converter}
     */
    @NonNull
    public <H> IterableQuery<H> convert(@NonNull final Converter<? super T, H> converter) {
        final IterableQuery<H> query = new IterableQuery<>();
        query.enqueueTask(new Runnable() {
            @Override
            public void run() {
                List<H> converted = new ArrayList<>();
                for (T element : executeTasks()) {
                    converted.add(converter.apply(element));
                }
                clearMutableIterable();
                query.setMutableIterable(converted);
            }
        });
        return query;
    }

    /**
     * Convert all elements to many elements using specified {@code converter}
     */
    @NonNull
    public <H> IterableQuery<H> convertToMany(
            @NonNull final ConverterToMany<? super T, H> converter) {
        final IterableQuery<H> query = new IterableQuery<>();
        query.enqueueTask(new Runnable() {
            @Override
            public void run() {
                List<H> converted = new ArrayList<>();
                for (T element : executeTasks()) {
                    Iterable<H> convertResult = converter.apply(element);
                    if (convertResult instanceof Collection<?>) {
                        converted.addAll((Collection<H>) convertResult);
                    } else if (convertResult != null) {
                        for (H convertedElement : convertResult) {
                            converted.add(convertedElement);
                        }
                    }
                }
                clearMutableIterable();
                query.setMutableIterable(converted);
            }
        });
        return query;
    }

    /**
     * Apply specified accumulator function over a sequence
     *
     * @param aggregator Accumulator function
     * @return Accumulated value
     */
    @Nullable
    public T aggregate(@NonNull Aggregator<T, ? super T> aggregator) {
        boolean first = true;
        T accumulator = null;
        for (T element : executeTasks()) {
            if (first) {
                accumulator = element;
                first = false;
                continue;
            }
            accumulator = aggregator.apply(accumulator, element);
        }
        return accumulator;
    }

    /**
     * Apply specified accumulator function over a sequence
     *
     * @param seed       Initial accumulator value
     * @param aggregator Accumulator function
     * @return Accumulated value
     */
    @Nullable
    public <A> A aggregate(@Nullable A seed, @NonNull Aggregator<A, ? super T> aggregator) {
        A accumulator = seed;
        for (T element : executeTasks()) {
            accumulator = aggregator.apply(accumulator, element);
        }
        return accumulator;
    }

    /**
     * Apply specified accumulator function over a sequence
     *
     * @param seed       Initial accumulator value
     * @param aggregator Accumulator function
     * @return Accumulated value
     */
    public boolean aggregate(boolean seed, @NonNull Aggregator<Boolean, ? super T> aggregator) {
        Boolean accumulated = aggregate(Boolean.valueOf(seed), aggregator);
        return accumulated == null ? seed : accumulated;
    }

    /**
     * Apply specified accumulator function over a sequence
     *
     * @param seed       Initial accumulator value
     * @param aggregator Accumulator function
     * @return Accumulated value
     */
    public byte aggregate(byte seed, @NonNull Aggregator<Byte, ? super T> aggregator) {
        Byte accumulated = aggregate(Byte.valueOf(seed), aggregator);
        return accumulated == null ? seed : accumulated;
    }

    /**
     * Apply specified accumulator function over a sequence
     *
     * @param seed       Initial accumulator value
     * @param aggregator Accumulator function
     * @return Accumulated value
     */
    public short aggregate(short seed, @NonNull Aggregator<Short, ? super T> aggregator) {
        Short accumulated = aggregate(Short.valueOf(seed), aggregator);
        return accumulated == null ? seed : accumulated;
    }

    /**
     * Apply specified accumulator function over a sequence
     *
     * @param seed       Initial accumulator value
     * @param aggregator Accumulator function
     * @return Accumulated value
     */
    public int aggregate(int seed, @NonNull Aggregator<Integer, ? super T> aggregator) {
        Integer accumulated = aggregate(Integer.valueOf(seed), aggregator);
        return accumulated == null ? seed : accumulated;
    }

    /**
     * Apply specified accumulator function over a sequence
     *
     * @param seed       Initial accumulator value
     * @param aggregator Accumulator function
     * @return Accumulated value
     */
    public long aggregate(long seed, @NonNull Aggregator<Long, ? super T> aggregator) {
        Long accumulated = aggregate(Long.valueOf(seed), aggregator);
        return accumulated == null ? seed : accumulated;
    }

    /**
     * Apply specified accumulator function over a sequence
     *
     * @param seed       Initial accumulator value
     * @param aggregator Accumulator function
     * @return Accumulated value
     */
    public float aggregate(float seed, @NonNull Aggregator<Float, ? super T> aggregator) {
        Float accumulated = aggregate(Float.valueOf(seed), aggregator);
        return accumulated == null ? seed : accumulated;
    }

    /**
     * Apply specified accumulator function over a sequence
     *
     * @param seed       Initial accumulator value
     * @param aggregator Accumulator function
     * @return Accumulated value
     */
    public double aggregate(double seed, @NonNull Aggregator<Double, ? super T> aggregator) {
        Double accumulated = aggregate(Double.valueOf(seed), aggregator);
        return accumulated == null ? seed : accumulated;
    }

    /**
     * Apply specified accumulator function over a sequence
     *
     * @param seed       Initial accumulator value
     * @param aggregator Accumulator function
     * @return Accumulated value
     */
    public char aggregate(char seed, @NonNull Aggregator<Character, ? super T> aggregator) {
        Character accumulated = aggregate(Character.valueOf(seed), aggregator);
        return accumulated == null ? seed : accumulated;
    }

    /**
     * Apply specified accumulator function over a sequence
     *
     * @param seed       Initial accumulator value
     * @param aggregator Accumulator function
     * @param converter  Converter form accumulated value to result value
     * @return Converted accumulated value (returned by {@code converter})
     */
    @Nullable
    public <A, H> H aggregate(@Nullable A seed, @NonNull Aggregator<A, ? super T> aggregator,
            @NonNull Converter<? super A, H> converter) {
        A accumulator = seed;
        for (T element : executeTasks()) {
            accumulator = aggregator.apply(accumulator, element);
        }
        return converter.apply(accumulator);
    }

    /**
     * First element, or {@code null} if there are no elements or if first element is {@code null}
     */
    @Nullable
    public T first() {
        Iterator<T> iterator = executeTasks().iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return null;
        }
    }

    /**
     * First element that matches specified {@code predicate}, or {@code null} if
     * there are no matching elements or if matched element is {@code null}
     */
    @Nullable
    public T first(@NonNull Predicate<? super T> predicate) {
        for (T element : executeTasks()) {
            if (predicate.apply(element)) {
                return element;
            }
        }
        return null;
    }

    /**
     * Minimum element according to specified {@code comparator}, or {@code null}
     * if there are no elements or minimal element is {@code null}
     */
    @Nullable
    public T min(@NonNull Comparator<? super T> comparator) {
        T min = null;
        boolean first = true;
        for (T element : executeTasks()) {
            if (first) {
                min = element;
                first = false;
                continue;
            }
            if (comparator.compare(min, element) > 0) {
                min = element;
            }
        }
        return min;
    }

    /**
     * Maximum element according to specified {@code comparator}, or {@code null}
     * if there are no elements or maximal element is {@code null}
     */
    @Nullable
    public T max(@NonNull Comparator<? super T> comparator) {
        T max = null;
        boolean first = true;
        for (T element : executeTasks()) {
            if (first) {
                max = element;
                first = false;
                continue;
            }
            if (comparator.compare(max, element) < 0) {
                max = element;
            }
        }
        return max;
    }

    /**
     * Whether all elements match specified predicate
     */
    public boolean all(@NonNull Predicate<? super T> predicate) {
        boolean hasElements = false;
        for (T element : executeTasks()) {
            if (!predicate.apply(element)) {
                return false;
            }
            hasElements = true;
        }
        return hasElements;
    }

    /**
     * Whether no elements match specified predicate
     */
    public boolean none(@NonNull Predicate<? super T> predicate) {
        for (T element : executeTasks()) {
            if (predicate.apply(element)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Whether any elements match specified predicate
     */
    public boolean has(@NonNull Predicate<? super T> predicate) {
        for (T element : executeTasks()) {
            if (predicate.apply(element)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Count elements
     */
    public int size() {
        Iterable<T> iterable = executeTasks();
        if (iterable instanceof Collection<?>) {
            return ((Collection<T>) iterable).size();
        } else {
            int count = 0;
            for (T element : iterable) {
                count++;
            }
            return count;
        }
    }

    /**
     * Query from specified {@code iterable}
     */
    @NonNull
    public static <T> IterableQuery<T> from(@NonNull Iterable<T> iterable) {
        IterableQuery<T> query = new IterableQuery<>();
        query.setImmutableIterable(iterable);
        return query;
    }

    /**
     * Query from specified {@code array}
     */
    @NonNull
    public static <T> IterableQuery<T> from(@NonNull T[] array) {
        IterableQuery<T> query = new IterableQuery<>();
        query.setImmutableIterable(CollectionUtils.asIterable(array));
        return query;
    }

    /**
     * Query from specified {@code array}
     */
    @NonNull
    public static IterableQuery<Boolean> from(@NonNull boolean[] array) {
        IterableQuery<Boolean> query = new IterableQuery<>();
        query.setImmutableIterable(CollectionUtils.asIterable(array));
        return query;
    }

    /**
     * Query from specified {@code array}
     */
    @NonNull
    public static IterableQuery<Byte> from(@NonNull byte[] array) {
        IterableQuery<Byte> query = new IterableQuery<>();
        query.setImmutableIterable(CollectionUtils.asIterable(array));
        return query;
    }

    /**
     * Query from specified {@code array}
     */
    @NonNull
    public static IterableQuery<Short> from(@NonNull short[] array) {
        IterableQuery<Short> query = new IterableQuery<>();
        query.setImmutableIterable(CollectionUtils.asIterable(array));
        return query;
    }

    /**
     * Query from specified {@code array}
     */
    @NonNull
    public static IterableQuery<Integer> from(@NonNull int[] array) {
        IterableQuery<Integer> query = new IterableQuery<>();
        query.setImmutableIterable(CollectionUtils.asIterable(array));
        return query;
    }

    /**
     * Query from specified {@code array}
     */
    @NonNull
    public static IterableQuery<Long> from(@NonNull long[] array) {
        IterableQuery<Long> query = new IterableQuery<>();
        query.setImmutableIterable(CollectionUtils.asIterable(array));
        return query;
    }

    /**
     * Query from specified {@code array}
     */
    @NonNull
    public static IterableQuery<Float> from(@NonNull float[] array) {
        IterableQuery<Float> query = new IterableQuery<>();
        query.setImmutableIterable(CollectionUtils.asIterable(array));
        return query;
    }

    /**
     * Query from specified {@code array}
     */
    @NonNull
    public static IterableQuery<Double> from(@NonNull double[] array) {
        IterableQuery<Double> query = new IterableQuery<>();
        query.setImmutableIterable(CollectionUtils.asIterable(array));
        return query;
    }

    /**
     * Query from specified {@code array}
     */
    @NonNull
    public static IterableQuery<Character> from(@NonNull char[] array) {
        IterableQuery<Character> query = new IterableQuery<>();
        query.setImmutableIterable(CollectionUtils.asIterable(array));
        return query;
    }

    /**
     * Query from specified {@link CharSequence}
     */
    @NonNull
    public static IterableQuery<Character> from(@NonNull CharSequence charSequence) {
        IterableQuery<Character> query = new IterableQuery<>();
        query.setImmutableIterable(CollectionUtils.asIterable(charSequence));
        return query;
    }
}
