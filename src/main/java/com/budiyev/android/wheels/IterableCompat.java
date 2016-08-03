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
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Sequence of elements
 *
 * @see IterableCompat#wrap(Iterable)
 * @see IterableCompat#wrap(Object[])
 * @see IterableCompat#wrap(boolean[])
 * @see IterableCompat#wrap(byte[])
 * @see IterableCompat#wrap(short[])
 * @see IterableCompat#wrap(int[])
 * @see IterableCompat#wrap(long[])
 * @see IterableCompat#wrap(float[])
 * @see IterableCompat#wrap(double[])
 */
public final class IterableCompat<T> implements Iterable<T> {
    private final Queue<Runnable> mTasksQueue = new LinkedList<>();
    private final Lock mTasksLock = new ReentrantLock();
    private volatile Iterable<T> mIterable;

    private IterableCompat(@NonNull Iterable<T> iterable) {
        setIterable(Objects.requireNonNull(iterable));
    }

    //region Non-terminal methods

    /**
     * Add all elements of specified {@code iterable}
     * <br>
     * <b>Lazy evaluation</b>
     */
    @NonNull
    public IterableCompat<T> add(@NonNull final Iterable<T> iterable) {
        enqueueTask(new Runnable() {
            @Override
            public void run() {
                Iterable<T> sourceIterable = getIterable();
                if (sourceIterable instanceof Collection && iterable instanceof Collection) {
                    ((Collection<T>) sourceIterable).addAll((Collection<T>) iterable);
                } else {
                    Collection<T> sourceCollection;
                    if (sourceIterable instanceof Collection) {
                        sourceCollection = (Collection<T>) sourceIterable;
                    } else {
                        sourceCollection = new ArrayList<>();
                        for (T element : sourceIterable) {
                            sourceCollection.add(element);
                        }
                        setIterable(sourceCollection);
                    }
                    if (iterable instanceof Collection) {
                        sourceCollection.addAll((Collection<T>) iterable);
                    } else {
                        for (T element : iterable) {
                            sourceCollection.add(element);
                        }
                    }
                }
            }
        });
        return this;
    }

    /**
     * Remove all elements that doesn't match specified {@code predicate}
     * <br>
     * <b>Lazy evaluation</b>
     */
    @NonNull
    public IterableCompat<T> filter(@NonNull final PredicateCompat<T> predicate) {
        enqueueTask(new Runnable() {
            @Override
            public void run() {
                Iterable<T> iterable = getIterable();
                List<T> filtered = new ArrayList<>();
                for (T element : iterable) {
                    if (predicate.apply(element)) {
                        filtered.add(element);
                    }
                }
                setIterable(filtered);
            }
        });
        return this;
    }

    /**
     * Apply specified {@code function} to all elements
     * <br>
     * <b>Lazy evaluation</b>
     */
    @NonNull
    public IterableCompat<T> apply(@NonNull final FunctionCompat<T> function) {
        enqueueTask(new Runnable() {
            @Override
            public void run() {
                Iterable<T> iterable = getIterable();
                for (T element : iterable) {
                    function.apply(element);
                }
            }
        });
        return this;
    }

    /**
     * Retain only first {@code count} of elements
     * <br>
     * <b>Lazy evaluation</b>
     */
    @NonNull
    public IterableCompat<T> take(@IntRange(from = 0, to = Integer.MAX_VALUE) final int count) {
        if (count < 0) {
            throw new IllegalArgumentException();
        }
        enqueueTask(new Runnable() {
            @Override
            public void run() {
                Iterable<T> iterable = getIterable();
                List<T> taken;
                if (iterable instanceof List) {
                    List<T> list = (List<T>) iterable;
                    taken = list.subList(0, count);
                } else {
                    taken = new ArrayList<>();
                    int position = 0;
                    for (T element : iterable) {
                        if (position < count) {
                            taken.add(element);
                        } else {
                            break;
                        }
                        position++;
                    }
                }
                setIterable(taken);
            }
        });
        return this;
    }

    /**
     * Retain only first sequence of elements that matches specified {@code predicate}
     * <br>
     * <b>Lazy evaluation</b>
     */
    @NonNull
    public IterableCompat<T> takeWhile(@NonNull final PredicateCompat<T> predicate) {
        enqueueTask(new Runnable() {
            @Override
            public void run() {
                Iterable<T> iterable = getIterable();
                List<T> taken = new ArrayList<>();
                for (T element : iterable) {
                    if (predicate.apply(element)) {
                        taken.add(element);
                    } else {
                        break;
                    }
                }
                setIterable(taken);
            }
        });
        return this;
    }

    /**
     * Remove first {@code count} of elements
     * <br>
     * <b>Lazy evaluation</b>
     */
    @NonNull
    public IterableCompat<T> skip(@IntRange(from = 0, to = Integer.MAX_VALUE) final int count) {
        if (count < 0) {
            throw new IllegalArgumentException();
        }
        enqueueTask(new Runnable() {
            @Override
            public void run() {
                Iterable<T> iterable = getIterable();
                List<T> rest;
                if (iterable instanceof List) {
                    List<T> list = (List<T>) iterable;
                    rest = list.subList(count, list.size());
                } else {
                    rest = new ArrayList<>();
                    int position = 0;
                    for (T element : iterable) {
                        if (position >= count) {
                            rest.add(element);
                        }
                        position++;
                    }
                }
                setIterable(rest);
            }
        });
        return this;
    }

    /**
     * Remove first sequence of elements that matches specified {@code predicate}
     * <br>
     * <b>Lazy evaluation</b>
     */
    @NonNull
    public IterableCompat<T> skipWhile(@NonNull final PredicateCompat<T> predicate) {
        enqueueTask(new Runnable() {
            @Override
            public void run() {
                Iterable<T> iterable = getIterable();
                List<T> rest = new ArrayList<>();
                boolean skip = true;
                for (T element : iterable) {
                    if (skip) {
                        skip = predicate.apply(element);
                    }
                    if (!skip) {
                        rest.add(element);
                    }
                }
                setIterable(rest);
            }
        });
        return this;
    }

    /**
     * Sort using specified {@code comparator}
     * <br>
     * Sorting algorithm is <b>unstable</b> (Heapsort)
     * <br>
     * <b>Lazy evaluation</b>
     */
    @NonNull
    public IterableCompat<T> sort(@NonNull final Comparator<T> comparator) {
        enqueueTask(new Runnable() {
            @Override
            public void run() {
                Iterable<T> iterable = getIterable();
                if (iterable instanceof List) {
                    CommonUtils.sort((List<T>) iterable, comparator);
                } else {
                    List<T> sorted = new ArrayList<>();
                    for (T element : iterable) {
                        sorted.add(element);
                    }
                    CommonUtils.sort(sorted, comparator);
                    setIterable(sorted);
                }
            }
        });
        return this;
    }

    /**
     * Reverse elements order
     * <br>
     * <b>Lazy evaluation</b>
     */
    @NonNull
    public IterableCompat<T> reverse() {
        enqueueTask(new Runnable() {
            @Override
            public void run() {
                Iterable<T> iterable = getIterable();
                if (iterable instanceof List) {
                    Collections.reverse((List<T>) iterable);
                } else {
                    List<T> reversed = new ArrayList<>();
                    for (T element : iterable) {
                        reversed.add(element);
                    }
                    Collections.reverse(reversed);
                    setIterable(reversed);
                }
            }
        });
        return this;
    }

    //endregion

    //region Terminal methods

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<T> iterator() {
        Iterable<T> iterable = executeTasks();
        return iterable.iterator();
    }

    /**
     * Convert all elements using specified {@code converter}
     */
    @NonNull
    public <H> IterableCompat<H> convert(@NonNull ConverterCompat<T, H> converter) {
        Iterable<T> iterable = executeTasks();
        List<H> converted = new ArrayList<>();
        for (T element : iterable) {
            converted.add(converter.apply(element));
        }
        return new IterableCompat<>(converted);
    }

    /**
     * First element, or {@code null} if there are no elements
     */
    @Nullable
    public T first() {
        Iterable<T> iterable = executeTasks();
        Iterator<T> iterator = iterable.iterator();
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
        Iterable<T> iterable = executeTasks();
        for (T element : iterable) {
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
        Iterable<T> iterable = executeTasks();
        T min = null;
        boolean first = true;
        for (T element : iterable) {
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
        Iterable<T> iterable = executeTasks();
        T max = null;
        boolean first = true;
        for (T element : iterable) {
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
        Iterable<T> iterable = executeTasks();
        boolean all = true;
        for (T element : iterable) {
            all &= predicate.apply(element);
        }
        return all;
    }

    /**
     * Whether no elements match specified predicate
     */
    public boolean none(@NonNull PredicateCompat<T> predicate) {
        Iterable<T> iterable = executeTasks();
        boolean none = true;
        for (T element : iterable) {
            none &= !predicate.apply(element);
        }
        return none;
    }

    /**
     * Whether any elements match specified predicate
     */
    public boolean has(@NonNull PredicateCompat<T> predicate) {
        Iterable<T> iterable = executeTasks();
        for (T element : iterable) {
            if (predicate.apply(element)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Count elements
     */
    public int count() {
        Iterable<T> iterable = executeTasks();
        if (iterable instanceof Collection) {
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
     * Convert to {@link List}
     */
    @NonNull
    public List<T> toList() {
        Iterable<T> iterable = executeTasks();
        if (iterable instanceof List) {
            return (List<T>) iterable;
        } else {
            List<T> list = new ArrayList<>();
            for (T element : iterable) {
                list.add(element);
            }
            return list;
        }
    }

    //endregion

    @NonNull
    private Iterable<T> getIterable() {
        return mIterable;
    }

    private void setIterable(@NonNull Iterable<T> iterable) {
        mIterable = iterable;
    }

    private void enqueueTask(@NonNull Runnable task) {
        mTasksLock.lock();
        try {
            mTasksQueue.offer(task);
        } finally {
            mTasksLock.unlock();
        }
    }

    @NonNull
    private Iterable<T> executeTasks() {
        mTasksLock.lock();
        try {
            for (Runnable operation = mTasksQueue.poll(); operation != null;
                    operation = mTasksQueue.poll()) {
                operation.run();
            }
            return getIterable();
        } finally {
            mTasksLock.unlock();
        }
    }

    /**
     * Wrap specified {@code iterable} into {@link IterableCompat}
     */
    @NonNull
    public static <T> IterableCompat<T> wrap(@NonNull Iterable<T> iterable) {
        return new IterableCompat<>(iterable);
    }

    /**
     * Wrap specified {@code array} into {@link IterableCompat}
     */
    @NonNull
    public static <T> IterableCompat<T> wrap(@NonNull T[] array) {
        List<T> list = new ArrayList<>(array.length);
        Collections.addAll(list, array);
        return new IterableCompat<>(list);
    }

    /**
     * Wrap specified {@code array} into {@link IterableCompat}
     */
    @NonNull
    public static IterableCompat<Boolean> wrap(@NonNull boolean[] array) {
        List<Boolean> list = new ArrayList<>(array.length);
        for (boolean element : array) {
            list.add(element);
        }
        return new IterableCompat<>(list);
    }

    /**
     * Wrap specified {@code array} into {@link IterableCompat}
     */
    @NonNull
    public static IterableCompat<Byte> wrap(@NonNull byte[] array) {
        List<Byte> list = new ArrayList<>(array.length);
        for (byte element : array) {
            list.add(element);
        }
        return new IterableCompat<>(list);
    }

    /**
     * Wrap specified {@code array} into {@link IterableCompat}
     */
    @NonNull
    public static IterableCompat<Short> wrap(@NonNull short[] array) {
        List<Short> list = new ArrayList<>(array.length);
        for (short element : array) {
            list.add(element);
        }
        return new IterableCompat<>(list);
    }

    /**
     * Wrap specified {@code array} into {@link IterableCompat}
     */
    @NonNull
    public static IterableCompat<Integer> wrap(@NonNull int[] array) {
        List<Integer> list = new ArrayList<>(array.length);
        for (int element : array) {
            list.add(element);
        }
        return new IterableCompat<>(list);
    }

    /**
     * Wrap specified {@code array} into {@link IterableCompat}
     */
    @NonNull
    public static IterableCompat<Long> wrap(@NonNull long[] array) {
        List<Long> list = new ArrayList<>(array.length);
        for (long element : array) {
            list.add(element);
        }
        return new IterableCompat<>(list);
    }

    /**
     * Wrap specified {@code array} into {@link IterableCompat}
     */
    @NonNull
    public static IterableCompat<Float> wrap(@NonNull float[] array) {
        List<Float> list = new ArrayList<>(array.length);
        for (float element : array) {
            list.add(element);
        }
        return new IterableCompat<>(list);
    }

    /**
     * Wrap specified {@code array} into {@link IterableCompat}
     */
    @NonNull
    public static IterableCompat<Double> wrap(@NonNull double[] array) {
        List<Double> list = new ArrayList<>(array.length);
        for (double element : array) {
            list.add(element);
        }
        return new IterableCompat<>(list);
    }
}
