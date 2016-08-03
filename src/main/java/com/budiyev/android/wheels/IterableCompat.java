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
 * @param <T> Element type
 * @see IterableCompat#wrap(Iterable)
 */
// TODO
public class IterableCompat<T> implements Iterable<T> {
    private final Queue<Runnable> mTasksQueue = new LinkedList<>();
    private final Lock mTasksLock = new ReentrantLock();
    private volatile Iterable<T> mIterable;

    private IterableCompat(@NonNull Iterable<T> iterable) {
        mIterable = Objects.requireNonNull(iterable);
    }

    //region Non-terminal methods

    @NonNull
    public IterableCompat<T> filter(@NonNull final PredicateCompat<T> predicate) {
        enqueueTask(new Runnable() {
            @Override
            public void run() {
                List<T> filtered = new ArrayList<>();
                for (T element : mIterable) {
                    if (predicate.apply(element)) {
                        filtered.add(element);
                    }
                }
                mIterable = filtered;
            }
        });
        return this;
    }

    @NonNull
    public IterableCompat<T> apply(@NonNull final FunctionCompat<T> function) {
        enqueueTask(new Runnable() {
            @Override
            public void run() {
                for (T element : mIterable) {
                    function.apply(element);
                }
            }
        });
        return this;
    }

    @NonNull
    public IterableCompat<T> take(@IntRange(from = 0, to = Integer.MAX_VALUE) final int count) {
        if (count < 0) {
            throw new IllegalArgumentException();
        }
        enqueueTask(new Runnable() {
            @Override
            public void run() {
                List<T> taken;
                if (mIterable instanceof List) {
                    List<T> list = (List<T>) mIterable;
                    taken = list.subList(0, count);
                } else {
                    taken = new ArrayList<>();
                    int position = 0;
                    for (T element : mIterable) {
                        if (position < count) {
                            taken.add(element);
                        } else {
                            break;
                        }
                        position++;
                    }
                }
                mIterable = taken;
            }
        });
        return this;
    }

    @NonNull
    public IterableCompat<T> takeWhile(@NonNull final PredicateCompat<T> predicate) {
        enqueueTask(new Runnable() {
            @Override
            public void run() {
                List<T> taken = new ArrayList<>();
                for (T element : mIterable) {
                    if (predicate.apply(element)) {
                        taken.add(element);
                    } else {
                        break;
                    }
                }
                mIterable = taken;
            }
        });
        return this;
    }

    @NonNull
    public IterableCompat<T> skip(@IntRange(from = 0, to = Integer.MAX_VALUE) final int count) {
        if (count < 0) {
            throw new IllegalArgumentException();
        }
        enqueueTask(new Runnable() {
            @Override
            public void run() {
                List<T> rest;
                if (mIterable instanceof List) {
                    List<T> list = (List<T>) mIterable;
                    rest = list.subList(count, list.size());
                } else {
                    rest = new ArrayList<>();
                    int position = 0;
                    for (T element : mIterable) {
                        if (position >= count) {
                            rest.add(element);
                        }
                        position++;
                    }
                }
                mIterable = rest;
            }
        });
        return this;
    }

    @NonNull
    public IterableCompat<T> skipWhile(@NonNull final PredicateCompat<T> predicate) {
        enqueueTask(new Runnable() {
            @Override
            public void run() {
                List<T> rest = new ArrayList<>();
                boolean skip = true;
                for (T element : mIterable) {
                    if (skip) {
                        skip = predicate.apply(element);
                    }
                    if (!skip) {
                        rest.add(element);
                    }
                }
                mIterable = rest;
            }
        });
        return this;
    }

    @NonNull
    public IterableCompat<T> sort(@NonNull final Comparator<T> comparator) {
        enqueueTask(new Runnable() {
            @Override
            public void run() {
                if (mIterable instanceof List) {
                    CommonUtils.sort((List<T>) mIterable, comparator);
                } else {
                    List<T> sorted = new ArrayList<>();
                    for (T element : mIterable) {
                        sorted.add(element);
                    }
                    CommonUtils.sort(sorted, comparator);
                    mIterable = sorted;
                }
            }
        });
        return this;
    }

    @NonNull
    public IterableCompat<T> reverse() {
        enqueueTask(new Runnable() {
            @Override
            public void run() {
                if (mIterable instanceof List) {
                    Collections.reverse((List<T>) mIterable);
                } else {
                    List<T> reversed = new ArrayList<>();
                    for (T element : mIterable) {
                        reversed.add(element);
                    }
                    Collections.reverse(reversed);
                    mIterable = reversed;
                }
            }
        });
        return this;
    }

    //endregion

    //region Terminal methods

    @Override
    public Iterator<T> iterator() {
        Iterable<T> iterable = executeTasks();
        return iterable.iterator();
    }

    @NonNull
    public <H> IterableCompat<H> convert(@NonNull ConverterCompat<T, H> converter) {
        Iterable<T> iterable = executeTasks();
        List<H> converted = new ArrayList<>();
        for (T element : iterable) {
            converted.add(converter.apply(element));
        }
        return new IterableCompat<>(converted);
    }

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

    public boolean all(@NonNull PredicateCompat<T> predicate) {
        Iterable<T> iterable = executeTasks();
        boolean all = true;
        for (T element : iterable) {
            all &= predicate.apply(element);
        }
        return all;
    }

    public boolean none(@NonNull PredicateCompat<T> predicate) {
        Iterable<T> iterable = executeTasks();
        boolean none = true;
        for (T element : iterable) {
            none &= !predicate.apply(element);
        }
        return none;
    }

    public boolean has(@NonNull PredicateCompat<T> predicate) {
        Iterable<T> iterable = executeTasks();
        for (T element : iterable) {
            if (predicate.apply(element)) {
                return true;
            }
        }
        return false;
    }

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
            return mIterable;
        } finally {
            mTasksLock.unlock();
        }
    }

    @NonNull
    public static <T> IterableCompat<T> wrap(@NonNull Iterable<T> iterable) {
        return new IterableCompat<>(iterable);
    }
}
