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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Tools for collections
 */
public class CollectionUtils {
    private CollectionUtils() {
    }

    /**
     * Wrap specified {@code array} into {@link Iterable}
     */
    @NonNull
    public static <T> Iterable<T> asIterable(@NonNull final T[] array) {
        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return new Iterator<T>() {
                    private int position = -1;

                    @Override
                    public boolean hasNext() {
                        return position + 1 < array.length;
                    }

                    @Override
                    public T next() {
                        return array[++position];
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    /**
     * Wrap specified {@code array} into {@link Iterable}
     */
    @NonNull
    public static Iterable<Boolean> asIterable(@NonNull final boolean[] array) {
        return new Iterable<Boolean>() {
            @Override
            public Iterator<Boolean> iterator() {
                return new Iterator<Boolean>() {
                    private int position = -1;

                    @Override
                    public boolean hasNext() {
                        return position + 1 < array.length;
                    }

                    @Override
                    public Boolean next() {
                        return array[++position];
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    /**
     * Wrap specified {@code array} into {@link Iterable}
     */
    @NonNull
    public static Iterable<Byte> asIterable(@NonNull final byte[] array) {
        return new Iterable<Byte>() {
            @Override
            public Iterator<Byte> iterator() {
                return new Iterator<Byte>() {
                    private int position = -1;

                    @Override
                    public boolean hasNext() {
                        return position + 1 < array.length;
                    }

                    @Override
                    public Byte next() {
                        return array[++position];
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    /**
     * Wrap specified {@code array} into {@link Iterable}
     */
    @NonNull
    public static Iterable<Short> asIterable(@NonNull final short[] array) {
        return new Iterable<Short>() {
            @Override
            public Iterator<Short> iterator() {
                return new Iterator<Short>() {
                    private int position = -1;

                    @Override
                    public boolean hasNext() {
                        return position + 1 < array.length;
                    }

                    @Override
                    public Short next() {
                        return array[++position];
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    /**
     * Wrap specified {@code array} into {@link Iterable}
     */
    @NonNull
    public static Iterable<Integer> asIterable(@NonNull final int[] array) {
        return new Iterable<Integer>() {
            @Override
            public Iterator<Integer> iterator() {
                return new Iterator<Integer>() {
                    private int position = -1;

                    @Override
                    public boolean hasNext() {
                        return position + 1 < array.length;
                    }

                    @Override
                    public Integer next() {
                        return array[++position];
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    /**
     * Wrap specified {@code array} into {@link Iterable}
     */
    @NonNull
    public static Iterable<Long> asIterable(@NonNull final long[] array) {
        return new Iterable<Long>() {
            @Override
            public Iterator<Long> iterator() {
                return new Iterator<Long>() {
                    private int position = -1;

                    @Override
                    public boolean hasNext() {
                        return position + 1 < array.length;
                    }

                    @Override
                    public Long next() {
                        return array[++position];
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    /**
     * Wrap specified {@code array} into {@link Iterable}
     */
    @NonNull
    public static Iterable<Float> asIterable(@NonNull final float[] array) {
        return new Iterable<Float>() {
            @Override
            public Iterator<Float> iterator() {
                return new Iterator<Float>() {
                    private int position = -1;

                    @Override
                    public boolean hasNext() {
                        return position + 1 < array.length;
                    }

                    @Override
                    public Float next() {
                        return array[++position];
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    /**
     * Wrap specified {@code array} into {@link Iterable}
     */
    @NonNull
    public static Iterable<Double> asIterable(@NonNull final double[] array) {
        return new Iterable<Double>() {
            @Override
            public Iterator<Double> iterator() {
                return new Iterator<Double>() {
                    private int position = -1;

                    @Override
                    public boolean hasNext() {
                        return position + 1 < array.length;
                    }

                    @Override
                    public Double next() {
                        return array[++position];
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    /**
     * Wrap specified {@code array} into {@link Iterable}
     */
    @NonNull
    public static Iterable<Character> asIterable(@NonNull final char[] array) {
        return new Iterable<Character>() {
            @Override
            public Iterator<Character> iterator() {
                return new Iterator<Character>() {
                    private int position = -1;

                    @Override
                    public boolean hasNext() {
                        return position + 1 < array.length;
                    }

                    @Override
                    public Character next() {
                        return array[++position];
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    /**
     * Swap the elements of {@code list} at positions {@code a} and {@code b}
     */
    public static <T> void swap(@NonNull List<T> list, int a, int b) {
        list.set(b, list.set(a, list.get(b)));
    }

    /**
     * Sorts the {@code list} in ascending natural order
     * <br>
     * Sorting algorithm is unstable (Heapsort)
     */
    public static <T extends Comparable<T>> void sort(@NonNull List<T> list) {
        sort(list, new Comparator<T>() {
            @Override
            public int compare(T lhs, T rhs) {
                return lhs.compareTo(rhs);
            }
        });
    }

    /**
     * Sorts the {@code list} using the {@code comparator}
     * <br>
     * Sorting algorithm is <b>unstable</b> (Heapsort)
     */
    public static <T> void sort(@NonNull List<T> list, @NonNull Comparator<T> comparator) {
        int size = list.size();
        for (int i = size / 2 - 1; i >= 0; i--) {
            shift(list, comparator, i, size);
        }
        for (int i = size - 1; i >= 1; i--) {
            swap(list, 0, i);
            shift(list, comparator, 0, i);
        }
    }

    private static <T> void shift(@NonNull List<T> list, @NonNull Comparator<T> comparator, int i,
            int j) {
        int max;
        int di = i * 2;
        while (di + 1 < j) {
            if (di + 1 == j - 1 || comparator.compare(list.get(di + 1), list.get(di + 2)) > 0) {
                max = di + 1;
            } else {
                max = di + 2;
            }
            if (comparator.compare(list.get(i), list.get(max)) < 0) {
                swap(list, i, max);
                i = max;
                di = i * 2;
            } else {
                break;
            }
        }
    }

    /**
     * Search {@code item} in {@code list}
     * <br>
     * The algorithm searches for an element in the list in both directions, starting from
     * {@code position}. Such approach is very effective if approximate position
     * of an element in the list is known.
     * <br>
     * Starting from {@code position} it checks {@code step} of elements on the left
     * and at the right, if {@code item} is not found among them, another {@code step} of
     * elements on the left and at the right, and so on while {@code item} found or list ended.
     *
     * @param list     List of items
     * @param item     Item to search for
     * @param position Search start position
     * @param step     Search step
     * @return Position of item in list or -1
     */
    public static <T> int search(@NonNull List<T> list, @Nullable T item, int position, int step) {
        if (Objects.equals(item, list.get(position))) {
            return position;
        } else {
            int listSize = list.size();
            int currentOffset = step;
            int previousOffset = 0;
            for (; ; ) {
                int start = position - currentOffset;
                if (start < 0) {
                    start = 0;
                }
                int end = position + currentOffset + 1;
                if (end > listSize) {
                    end = listSize;
                }
                for (int i = start; i < end; i++) {
                    if (i >= position - previousOffset && i <= position + previousOffset) {
                        continue;
                    }
                    if (Objects.equals(item, list.get(i))) {
                        return i;
                    }
                }
                previousOffset = currentOffset;
                currentOffset += step;
                if (start == 0 && end == listSize) {
                    return -1;
                }
            }
        }
    }

    /**
     * Check if specified {@link Iterable} is {@code null} or empty
     */
    public static boolean isNullOrEmpty(@Nullable Iterable<?> iterable) {
        return iterable == null || !iterable.iterator().hasNext();
    }

    /**
     * Check if specified array is {@code null} or empty
     */
    public static boolean isNullOrEmpty(@Nullable Object[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Check if specified array is {@code null} or empty
     */
    public static boolean isNullOrEmpty(@Nullable boolean[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Check if specified array is {@code null} or empty
     */
    public static boolean isNullOrEmpty(@Nullable byte[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Check if specified array is {@code null} or empty
     */
    public static boolean isNullOrEmpty(@Nullable short[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Check if specified array is {@code null} or empty
     */
    public static boolean isNullOrEmpty(@Nullable int[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Check if specified array is {@code null} or empty
     */
    public static boolean isNullOrEmpty(@Nullable long[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Check if specified array is {@code null} or empty
     */
    public static boolean isNullOrEmpty(@Nullable float[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Check if specified array is {@code null} or empty
     */
    public static boolean isNullOrEmpty(@Nullable double[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Check if specified array is {@code null} or empty
     */
    public static boolean isNullOrEmpty(@Nullable char[] array) {
        return array == null || array.length == 0;
    }
}
