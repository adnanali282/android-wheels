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
import java.util.Objects;

// TODO
public class IterableCompat<T> implements Iterable<T> {
    private Iterable<T> mIterable;

    public IterableCompat(@NonNull Iterable<T> iterable) {
        mIterable = Objects.requireNonNull(iterable);
    }

    @Override
    public Iterator<T> iterator() {
        return mIterable.iterator();
    }

    @NonNull
    public IterableCompat<T> filter(@NonNull PredicateCompat<T> predicate) {
        List<T> filtered = new ArrayList<>();
        for (T element : mIterable) {
            if (predicate.apply(element)) {
                filtered.add(element);
            }
        }
        mIterable = filtered;
        return this;
    }

    @NonNull
    public <H> IterableCompat<H> convert(@NonNull ConverterCompat<T, H> converter) {
        List<H> converted = new ArrayList<>();
        for (T element : mIterable) {
            converted.add(converter.apply(element));
        }
        return new IterableCompat<>(converted);
    }

    @NonNull
    public IterableCompat<T> apply(@NonNull FunctionCompat<T> function) {
        for (T element : mIterable) {
            function.apply(element);
        }
        return this;
    }

    @NonNull
    public IterableCompat<T> skip(@IntRange(from = 0, to = Integer.MAX_VALUE) int count) {
        if (count < 0) {
            throw new IllegalArgumentException();
        }
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
        return this;
    }

    @NonNull
    public IterableCompat<T> take(@IntRange(from = 0, to = Integer.MAX_VALUE) int count) {
        if (count < 0) {
            throw new IllegalArgumentException();
        }
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
        return this;
    }

    @NonNull
    public IterableCompat<T> sort(@NonNull Comparator<T> comparator) {
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
        return this;
    }

    @NonNull
    public IterableCompat<T> reverse() {
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
        return this;
    }

    @Nullable
    public T first(@NonNull PredicateCompat<T> predicate) {
        for (T element : mIterable) {
            if (predicate.apply(element)) {
                return element;
            }
        }
        return null;
    }

    public int count() {
        if (mIterable instanceof Collection) {
            return ((Collection<T>) mIterable).size();
        } else {
            int count = 0;
            for (T element : mIterable) {
                count++;
            }
            return count;
        }
    }

    @NonNull
    public List<T> toList() {
        if (mIterable instanceof List) {
            return (List<T>) mIterable;
        } else {
            List<T> list = new ArrayList<>();
            for (T element : mIterable) {
                list.add(element);
            }
            return list;
        }
    }
}
