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
 * @see IterableQuery#from(Iterable)
 * @see IterableQuery#from(Object[])
 * @see IterableQuery#from(boolean[])
 * @see IterableQuery#from(byte[])
 * @see IterableQuery#from(short[])
 * @see IterableQuery#from(int[])
 * @see IterableQuery#from(long[])
 * @see IterableQuery#from(float[])
 * @see IterableQuery#from(double[])
 * @see IterableQuery#from(char[])
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
                Iterable<T> operand;
                if (iterable instanceof IterableQuery) {
                    operand = ((IterableQuery<T>) iterable).executeTasks();
                } else {
                    operand = iterable;
                }
                List<T> list = getMutableIterable();
                if (operand instanceof Collection) {
                    list.addAll((Collection<T>) operand);
                } else {
                    for (T element : operand) {
                        list.add(element);
                    }
                }
            }
        });
        return this;
    }

    /**
     * Remove all elements that doesn't match specified {@code predicate}
     */
    @NonNull
    public IterableQuery<T> filter(@NonNull final PredicateCompat<T> predicate) {
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
                if (isIterableMutable()) {
                    ((List<T>) source).clear();
                }
                setMutableIterable(filtered);
            }
        });
        return this;
    }

    /**
     * Apply specified {@code function} to all elements
     */
    @NonNull
    public IterableQuery<T> apply(@NonNull final FunctionCompat<T> function) {
        enqueueTask(new Runnable() {
            @Override
            public void run() {
                Iterable<T> list = getIterable();
                for (T element : list) {
                    function.apply(element);
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
                setMutableIterable(taken);
            }
        });
        return this;
    }

    /**
     * Retain only first sequence of elements that matches specified {@code predicate}
     */
    @NonNull
    public IterableQuery<T> takeWhile(@NonNull final PredicateCompat<T> predicate) {
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
                setMutableIterable(rest);
            }
        });
        return this;
    }

    /**
     * Remove first sequence of elements that matches specified {@code predicate}
     */
    @NonNull
    public IterableQuery<T> skipWhile(@NonNull final PredicateCompat<T> predicate) {
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
    public IterableQuery<T> sort(@NonNull final Comparator<T> comparator) {
        enqueueTask(new Runnable() {
            @Override
            public void run() {
                CommonUtils.sort(getMutableIterable(), comparator);
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
    public <H> IterableQuery<H> convert(@NonNull final ConverterCompat<T, H> converter) {
        final IterableQuery<H> query = new IterableQuery<>();
        query.enqueueTask(new Runnable() {
            @Override
            public void run() {
                Iterable<T> source = executeTasks();
                List<H> converted = new ArrayList<>();
                for (T element : source) {
                    converted.add(converter.apply(element));
                }
                if (isIterableMutable()) {
                    ((List<T>) source).clear();
                }
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
            @NonNull final ConverterToManyCompat<T, H> converter) {
        final IterableQuery<H> query = new IterableQuery<>();
        query.enqueueTask(new Runnable() {
            @Override
            public void run() {
                Iterable<T> source = executeTasks();
                List<H> converted = new ArrayList<>();
                for (T element : source) {
                    Iterable<H> convertResult = converter.apply(element);
                    if (convertResult instanceof Collection) {
                        converted.addAll((Collection<H>) convertResult);
                    } else {
                        for (H convertedElement : convertResult) {
                            converted.add(convertedElement);
                        }
                    }
                }
                if (isIterableMutable()) {
                    ((List<T>) source).clear();
                }
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
    public T aggregate(@NonNull AggregatorCompat<T, T> aggregator) {
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
    public <A> A aggregate(@Nullable A seed, @NonNull AggregatorCompat<A, T> aggregator) {
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
    public boolean aggregate(boolean seed, @NonNull AggregatorCompat<Boolean, T> aggregator) {
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
    public byte aggregate(byte seed, @NonNull AggregatorCompat<Byte, T> aggregator) {
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
    public short aggregate(short seed, @NonNull AggregatorCompat<Short, T> aggregator) {
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
    public int aggregate(int seed, @NonNull AggregatorCompat<Integer, T> aggregator) {
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
    public long aggregate(long seed, @NonNull AggregatorCompat<Long, T> aggregator) {
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
    public float aggregate(float seed, @NonNull AggregatorCompat<Float, T> aggregator) {
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
    public double aggregate(double seed, @NonNull AggregatorCompat<Double, T> aggregator) {
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
    public char aggregate(char seed, @NonNull AggregatorCompat<Character, T> aggregator) {
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
    public <A, H> H aggregate(@Nullable A seed, @NonNull AggregatorCompat<A, T> aggregator,
            @NonNull ConverterCompat<A, H> converter) {
        A accumulator = seed;
        for (T element : executeTasks()) {
            accumulator = aggregator.apply(accumulator, element);
        }
        return converter.apply(accumulator);
    }

    /**
     * First element, or {@code null} if there are no elements
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
     * there are no matching elements
     */
    @Nullable
    public T first(@NonNull PredicateCompat<T> predicate) {
        for (T element : executeTasks()) {
            if (predicate.apply(element)) {
                return element;
            }
        }
        return null;
    }

    /**
     * Minimum element according to specified {@code comparator}, or {@code null}
     * if there are no elements
     */
    @Nullable
    public T min(@NonNull Comparator<T> comparator) {
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
     * if there are no elements
     */
    @Nullable
    public T max(@NonNull Comparator<T> comparator) {
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
    public boolean all(@NonNull PredicateCompat<T> predicate) {
        boolean all = true;
        for (T element : executeTasks()) {
            all &= predicate.apply(element);
        }
        return all;
    }

    /**
     * Whether no elements match specified predicate
     */
    public boolean none(@NonNull PredicateCompat<T> predicate) {
        boolean none = true;
        for (T element : executeTasks()) {
            none &= !predicate.apply(element);
        }
        return none;
    }

    /**
     * Whether any elements match specified predicate
     */
    public boolean has(@NonNull PredicateCompat<T> predicate) {
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
        if (iterable instanceof Collection) {
            return ((Collection) iterable).size();
        } else {
            int count = 0;
            for (T element : iterable) {
                count++;
            }
            return count;
        }
    }

    /**
     * Convert to {@link List}
     */
    @NonNull
    public List<T> asList() {
        Iterable<T> iterable = executeTasks();
        if (iterable instanceof List) {
            return (List<T>) iterable;
        } else {
            List<T> list = new ArrayList<>();
            for (T element : iterable) {
                list.add(element);
            }
            setMutableIterable(list);
            return list;
        }
    }

    /**
     * Query from specified {@code iterable}
     */
    @NonNull
    public static <T> IterableQuery<T> from(@NonNull Iterable<T> iterable) {
        IterableQuery<T> query = new IterableQuery<>();
        query.setIterable(iterable);
        return query;
    }

    /**
     * Query from specified {@code array}
     */
    @NonNull
    public static <T> IterableQuery<T> from(@NonNull T[] array) {
        IterableQuery<T> query = new IterableQuery<>();
        query.setIterable(ArrayUtils.asIterable(array));
        return query;
    }

    /**
     * Query from specified {@code array}
     */
    @NonNull
    public static IterableQuery<Boolean> from(@NonNull boolean[] array) {
        IterableQuery<Boolean> query = new IterableQuery<>();
        query.setIterable(ArrayUtils.asIterable(array));
        return query;
    }

    /**
     * Query from specified {@code array}
     */
    @NonNull
    public static IterableQuery<Byte> from(@NonNull byte[] array) {
        IterableQuery<Byte> query = new IterableQuery<>();
        query.setIterable(ArrayUtils.asIterable(array));
        return query;
    }

    /**
     * Query from specified {@code array}
     */
    @NonNull
    public static IterableQuery<Short> from(@NonNull short[] array) {
        IterableQuery<Short> query = new IterableQuery<>();
        query.setIterable(ArrayUtils.asIterable(array));
        return query;
    }

    /**
     * Query from specified {@code array}
     */
    @NonNull
    public static IterableQuery<Integer> from(@NonNull int[] array) {
        IterableQuery<Integer> query = new IterableQuery<>();
        query.setIterable(ArrayUtils.asIterable(array));
        return query;
    }

    /**
     * Query from specified {@code array}
     */
    @NonNull
    public static IterableQuery<Long> from(@NonNull long[] array) {
        IterableQuery<Long> query = new IterableQuery<>();
        query.setIterable(ArrayUtils.asIterable(array));
        return query;
    }

    /**
     * Query from specified {@code array}
     */
    @NonNull
    public static IterableQuery<Float> from(@NonNull float[] array) {
        IterableQuery<Float> query = new IterableQuery<>();
        query.setIterable(ArrayUtils.asIterable(array));
        return query;
    }

    /**
     * Query from specified {@code array}
     */
    @NonNull
    public static IterableQuery<Double> from(@NonNull double[] array) {
        IterableQuery<Double> query = new IterableQuery<>();
        query.setIterable(ArrayUtils.asIterable(array));
        return query;
    }

    /**
     * Query from specified {@code array}
     */
    @NonNull
    public static IterableQuery<Character> from(@NonNull char[] array) {
        IterableQuery<Character> query = new IterableQuery<>();
        query.setIterable(ArrayUtils.asIterable(array));
        return query;
    }
}
