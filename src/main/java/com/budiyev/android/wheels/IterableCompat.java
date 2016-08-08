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
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Object query API
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
    private final Lock mTasksLock = new ReentrantLock(true);
    private volatile List<T> mList;

    private IterableCompat() {
    }

    /**
     * Add specified {@code element} to end of sequence
     * <br>
     * <b>Lazy evaluation</b>
     */
    @NonNull
    public IterableCompat<T> add(@Nullable final T element) {
        enqueueTask(new Runnable() {
            @Override
            public void run() {
                getList().add(element);
            }
        });
        return this;
    }

    /**
     * Add all elements of specified {@code iterable} to end of sequence
     * <br>
     * <b>Lazy evaluation</b>
     */
    @NonNull
    public IterableCompat<T> add(@NonNull final Iterable<T> iterable) {
        enqueueTask(new Runnable() {
            @Override
            public void run() {
                Iterable<T> operand;
                if (iterable instanceof IterableCompat) {
                    operand = ((IterableCompat<T>) iterable).executeTasks();
                } else {
                    operand = iterable;
                }
                List<T> list = getList();
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
     * <br>
     * <b>Lazy evaluation</b>
     */
    @NonNull
    public IterableCompat<T> filter(@NonNull final PredicateCompat<T> predicate) {
        enqueueTask(new Runnable() {
            @Override
            public void run() {
                List<T> source = getList();
                List<T> filtered = new ArrayList<>();
                for (T element : source) {
                    if (predicate.apply(element)) {
                        filtered.add(element);
                    }
                }
                setList(filtered);
                source.clear();
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
                List<T> list = getList();
                for (T element : list) {
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
                List<T> list = getList();
                if (count > 0 && count < list.size()) {
                    setList(list.subList(0, count));
                }
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
                List<T> list = getList();
                int size = list.size();
                int count = 0;
                for (int i = 0; i < size; i++) {
                    if (predicate.apply(list.get(i))) {
                        count = i + 1;
                    } else {
                        break;
                    }
                }
                if (count > 0 && count < size) {
                    setList(list.subList(0, count));
                }
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
                List<T> list = getList();
                int size = list.size();
                if (count >= size) {
                    list.clear();
                } else {
                    setList(list.subList(count, size));
                }
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
                List<T> list = getList();
                int size = list.size();
                int start = 0;
                for (int i = 0; i < size; i++) {
                    if (!predicate.apply(list.get(i))) {
                        start = i;
                        break;
                    }
                }
                if (start > 0) {
                    setList(list.subList(start, size));
                }
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
                CommonUtils.sort(getList(), comparator);
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
                Collections.reverse(getList());
            }
        });
        return this;
    }

    /**
     * Convert all elements using specified {@code converter}
     * <br>
     * <b>Lazy evaluation</b>
     */
    @NonNull
    public <H> IterableCompat<H> convert(@NonNull final ConverterCompat<T, H> converter) {
        final IterableCompat<H> iterableCompat = new IterableCompat<>();
        iterableCompat.enqueueTask(new Runnable() {
            @Override
            public void run() {
                List<T> source = executeTasks();
                List<H> converted = new ArrayList<>();
                for (T element : source) {
                    converted.add(converter.apply(element));
                }
                iterableCompat.setList(converted);
                source.clear();
            }
        });
        return iterableCompat;
    }

    /**
     * Convert all elements to many elements using specified {@code converter}
     * <br>
     * <b>Lazy evaluation</b>
     */
    @NonNull
    public <H> IterableCompat<H> convertToMany(
            @NonNull final ConverterToManyCompat<T, H> converter) {
        final IterableCompat<H> iterableCompat = new IterableCompat<>();
        iterableCompat.enqueueTask(new Runnable() {
            @Override
            public void run() {
                List<T> source = executeTasks();
                List<H> converted = new ArrayList<>();
                for (T element : source) {
                    Iterable<H> convertResult = converter.apply(element);
                    if (convertResult instanceof IterableCompat) {
                        converted.addAll(((IterableCompat<H>) convertResult).executeTasks());
                    } else if (convertResult instanceof Collection) {
                        converted.addAll((Collection<H>) convertResult);
                    } else {
                        for (H convertedElement : convertResult) {
                            converted.add(convertedElement);
                        }
                    }
                }
                iterableCompat.setList(converted);
                source.clear();
            }
        });
        return iterableCompat;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<T> iterator() {
        Iterable<T> iterable = executeTasks();
        return iterable.iterator();
    }

    /**
     * First element, or {@code null} if there are no elements
     */
    @Nullable
    public T first() {
        List<T> list = executeTasks();
        if (list.size() > 0) {
            return list.get(1);
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
        List<T> list = executeTasks();
        for (T element : list) {
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
        List<T> list = executeTasks();
        T min = null;
        boolean first = true;
        for (T element : list) {
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
        List<T> list = executeTasks();
        T max = null;
        boolean first = true;
        for (T element : list) {
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
        List<T> list = executeTasks();
        boolean all = true;
        for (T element : list) {
            all &= predicate.apply(element);
        }
        return all;
    }

    /**
     * Whether no elements match specified predicate
     */
    public boolean none(@NonNull PredicateCompat<T> predicate) {
        List<T> list = executeTasks();
        boolean none = true;
        for (T element : list) {
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
    public int size() {
        List<T> list = executeTasks();
        return list.size();
    }

    /**
     * Convert to {@link List}
     */
    @NonNull
    public List<T> asList() {
        return executeTasks();
    }

    @NonNull
    private List<T> getList() {
        return mList;
    }

    private void setList(@NonNull List<T> list) {
        mList = list;
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
    private List<T> executeTasks() {
        mTasksLock.lock();
        try {
            for (Runnable task = mTasksQueue.poll(); task != null; task = mTasksQueue.poll()) {
                task.run();
            }
            return getList();
        } finally {
            mTasksLock.unlock();
        }
    }

    /**
     * Wrap specified {@code iterable} into {@link IterableCompat}
     * <br>
     * <b>Lazy evaluation</b>
     */
    @NonNull
    public static <T> IterableCompat<T> wrap(@NonNull final Iterable<T> iterable) {
        final IterableCompat<T> iterableCompat = new IterableCompat<>();
        iterableCompat.enqueueTask(new Runnable() {
            @Override
            public void run() {
                List<T> list;
                if (iterable instanceof Collection) {
                    Collection<T> collection = (Collection<T>) iterable;
                    list = new ArrayList<>(collection.size());
                    list.addAll(collection);
                } else {
                    list = new ArrayList<>();
                    for (T element : iterable) {
                        list.add(element);
                    }
                }
                iterableCompat.setList(list);
            }
        });
        return iterableCompat;
    }

    /**
     * Wrap specified {@code array} into {@link IterableCompat}
     * <br>
     * <b>Lazy evaluation</b>
     */
    @NonNull
    public static <T> IterableCompat<T> wrap(@NonNull final T[] array) {
        final IterableCompat<T> iterableCompat = new IterableCompat<>();
        iterableCompat.enqueueTask(new Runnable() {
            @Override
            public void run() {
                List<T> list = new ArrayList<>(array.length);
                Collections.addAll(list, array);
                iterableCompat.setList(list);
            }
        });
        return iterableCompat;
    }

    /**
     * Wrap specified {@code array} into {@link IterableCompat}
     * <br>
     * <b>Lazy evaluation</b>
     */
    @NonNull
    public static IterableCompat<Boolean> wrap(@NonNull final boolean[] array) {
        final IterableCompat<Boolean> iterableCompat = new IterableCompat<>();
        iterableCompat.enqueueTask(new Runnable() {
            @Override
            public void run() {
                List<Boolean> list = new ArrayList<>(array.length);
                for (boolean element : array) {
                    list.add(element);
                }
                iterableCompat.setList(list);
            }
        });
        return iterableCompat;
    }

    /**
     * Wrap specified {@code array} into {@link IterableCompat}
     * <br>
     * <b>Lazy evaluation</b>
     */
    @NonNull
    public static IterableCompat<Byte> wrap(@NonNull final byte[] array) {
        final IterableCompat<Byte> iterableCompat = new IterableCompat<>();
        iterableCompat.enqueueTask(new Runnable() {
            @Override
            public void run() {
                List<Byte> list = new ArrayList<>(array.length);
                for (byte element : array) {
                    list.add(element);
                }
                iterableCompat.setList(list);
            }
        });
        return iterableCompat;
    }

    /**
     * Wrap specified {@code array} into {@link IterableCompat}
     * <br>
     * <b>Lazy evaluation</b>
     */
    @NonNull
    public static IterableCompat<Short> wrap(@NonNull final short[] array) {
        final IterableCompat<Short> iterableCompat = new IterableCompat<>();
        iterableCompat.enqueueTask(new Runnable() {
            @Override
            public void run() {
                List<Short> list = new ArrayList<>(array.length);
                for (short element : array) {
                    list.add(element);
                }
                iterableCompat.setList(list);
            }
        });
        return iterableCompat;
    }

    /**
     * Wrap specified {@code array} into {@link IterableCompat}
     * <br>
     * <b>Lazy evaluation</b>
     */
    @NonNull
    public static IterableCompat<Integer> wrap(@NonNull final int[] array) {
        final IterableCompat<Integer> iterableCompat = new IterableCompat<>();
        iterableCompat.enqueueTask(new Runnable() {
            @Override
            public void run() {
                List<Integer> list = new ArrayList<>(array.length);
                for (int element : array) {
                    list.add(element);
                }
                iterableCompat.setList(list);
            }
        });
        return iterableCompat;
    }

    /**
     * Wrap specified {@code array} into {@link IterableCompat}
     * <br>
     * <b>Lazy evaluation</b>
     */
    @NonNull
    public static IterableCompat<Long> wrap(@NonNull final long[] array) {
        final IterableCompat<Long> iterableCompat = new IterableCompat<>();
        iterableCompat.enqueueTask(new Runnable() {
            @Override
            public void run() {
                List<Long> list = new ArrayList<>(array.length);
                for (long element : array) {
                    list.add(element);
                }
                iterableCompat.setList(list);
            }
        });
        return iterableCompat;
    }

    /**
     * Wrap specified {@code array} into {@link IterableCompat}
     * <br>
     * <b>Lazy evaluation</b>
     */
    @NonNull
    public static IterableCompat<Float> wrap(@NonNull final float[] array) {
        final IterableCompat<Float> iterableCompat = new IterableCompat<>();
        iterableCompat.enqueueTask(new Runnable() {
            @Override
            public void run() {
                List<Float> list = new ArrayList<>(array.length);
                for (float element : array) {
                    list.add(element);
                }
                iterableCompat.setList(list);
            }
        });
        return iterableCompat;
    }

    /**
     * Wrap specified {@code array} into {@link IterableCompat}
     * <br>
     * <b>Lazy evaluation</b>
     */
    @NonNull
    public static IterableCompat<Double> wrap(@NonNull final double[] array) {
        final IterableCompat<Double> iterableCompat = new IterableCompat<>();
        iterableCompat.enqueueTask(new Runnable() {
            @Override
            public void run() {
                List<Double> list = new ArrayList<>(array.length);
                for (double element : array) {
                    list.add(element);
                }
                iterableCompat.setList(list);
            }
        });
        return iterableCompat;
    }
}
