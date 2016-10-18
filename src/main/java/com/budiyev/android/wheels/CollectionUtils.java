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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Tools for collections
 */
public final class CollectionUtils {
    private CollectionUtils() {
    }

    /**
     * Wrap specified {@code array} into immutable {@link Iterable}
     */
    @NonNull
    public static <T> Iterable<T> asIterable(@NonNull T[] array) {
        return new GenericArrayIterable<>(array);
    }

    /**
     * Wrap specified {@code array} into immutable {@link Iterable}
     */
    @NonNull
    public static Iterable<Boolean> asIterable(@NonNull boolean[] array) {
        return new BooleanArrayIterable(array);
    }

    /**
     * Wrap specified {@code array} into immutable {@link Iterable}
     */
    @NonNull
    public static Iterable<Byte> asIterable(@NonNull byte[] array) {
        return new ByteArrayIterable(array);
    }

    /**
     * Wrap specified {@code array} into immutable {@link Iterable}
     */
    @NonNull
    public static Iterable<Short> asIterable(@NonNull short[] array) {
        return new ShortArrayIterable(array);
    }

    /**
     * Wrap specified {@code array} into immutable {@link Iterable}
     */
    @NonNull
    public static Iterable<Integer> asIterable(@NonNull int[] array) {
        return new IntegerArrayIterable(array);
    }

    /**
     * Wrap specified {@code array} into immutable {@link Iterable}
     */
    @NonNull
    public static Iterable<Long> asIterable(@NonNull long[] array) {
        return new LongArrayIterable(array);
    }

    /**
     * Wrap specified {@code array} into immutable {@link Iterable}
     */
    @NonNull
    public static Iterable<Float> asIterable(@NonNull float[] array) {
        return new FloatArrayIterable(array);
    }

    /**
     * Wrap specified {@code array} into immutable {@link Iterable}
     */
    @NonNull
    public static Iterable<Double> asIterable(@NonNull double[] array) {
        return new DoubleArrayIterable(array);
    }

    /**
     * Wrap specified {@code array} into immutable {@link Iterable}
     */
    @NonNull
    public static Iterable<Character> asIterable(@NonNull char[] array) {
        return new CharacterArrayIterable(array);
    }

    /**
     * Wrap specified {@link CharSequence} into immutable {@link Iterable}
     */
    @NonNull
    public static Iterable<Character> asIterable(@NonNull CharSequence charSequence) {
        return new CharSequenceIterable(charSequence);
    }

    /**
     * Wrap specified {@code array} into {@link CharSequence}
     */
    @NonNull
    public static CharSequence asCharSequence(@NonNull char[] array) {
        return new ArrayCharSequence(array, 0, array.length);
    }

    /**
     * Wrap specified {@code array} into {@link CharSequence}
     *
     * @param array Array
     * @param start Start index (including)
     * @param end   End index (excluding)
     * @return New {@link CharSequence}
     */
    @NonNull
    public static CharSequence asCharSequence(@NonNull char[] array, int start, int end) {
        return new ArrayCharSequence(array, start, end);
    }

    /**
     * Copy all elements of the specified iterable to the new list
     *
     * @param iterable Source {@link Iterable}
     * @return New {@link List}, containing all elements of {@code iterable}
     */
    @NonNull
    public static <T> List<T> copy(@NonNull Iterable<T> iterable) {
        List<T> copy;
        if (iterable instanceof Collection<?>) {
            Collection<T> collection = (Collection<T>) iterable;
            copy = new ArrayList<>(collection.size());
            copy.addAll(collection);
        } else {
            copy = new ArrayList<>();
            for (T element : iterable) {
                copy.add(element);
            }
        }
        return copy;
    }

    /**
     * Merge two {@link Iterable}s into single {@link List}
     *
     * @param a First {@link Iterable}
     * @param b Second {@link Iterable}
     * @return List, consisting of elements of {@code a} followed by elements of {@code b}
     */
    @NonNull
    public static <T> List<T> merge(@NonNull Iterable<T> a, @NonNull Iterable<T> b) {
        List<T> result;
        if (a instanceof Collection<?> && b instanceof Collection<?>) {
            Collection<T> collectionA = (Collection<T>) a;
            Collection<T> collectionB = (Collection<T>) b;
            result = new ArrayList<>(collectionA.size() + collectionB.size());
            result.addAll(collectionA);
            result.addAll(collectionB);
        } else if (a instanceof Collection<?>) {
            Collection<T> collectionA = (Collection<T>) a;
            result = new ArrayList<>(collectionA.size());
            result.addAll(collectionA);
            for (T element : b) {
                result.add(element);
            }
        } else {
            result = new ArrayList<>();
            for (T element : a) {
                result.add(element);
            }
            if (b instanceof Collection<?>) {
                result.addAll((Collection<T>) b);
            } else {
                for (T element : b) {
                    result.add(element);
                }
            }
        }
        return result;
    }

    /**
     * Merge two arrays
     *
     * @param a      First array
     * @param b      Second array
     * @param result Result array (length must be equal or greater than
     *               sum of {@code a} and {@code b} lengths)
     * @return Result array, filled by elements of {@code a}, followed by elements of {@code b}
     */
    @NonNull
    public static <T> T[] merge(@NonNull T[] a, @NonNull T[] b, @NonNull T[] result) {
        if (result.length < a.length + b.length) {
            throw new IllegalArgumentException();
        }
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    /**
     * Merge two arrays into the new one
     *
     * @param a First array
     * @param b Second array
     * @return New array, filled by elements of {@code a}, followed by elements of {@code b}
     */
    @NonNull
    public static boolean[] merge(@NonNull boolean[] a, @NonNull boolean[] b) {
        boolean[] result = new boolean[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    /**
     * Merge two arrays into the new one
     *
     * @param a First array
     * @param b Second array
     * @return New array, filled by elements of {@code a}, followed by elements of {@code b}
     */
    @NonNull
    public static byte[] merge(@NonNull byte[] a, @NonNull byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    /**
     * Merge two arrays into the new one
     *
     * @param a First array
     * @param b Second array
     * @return New array, filled by elements of {@code a}, followed by elements of {@code b}
     */
    @NonNull
    public static short[] merge(@NonNull short[] a, @NonNull short[] b) {
        short[] result = new short[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    /**
     * Merge two arrays into the new one
     *
     * @param a First array
     * @param b Second array
     * @return New array, filled by elements of {@code a}, followed by elements of {@code b}
     */
    @NonNull
    public static int[] merge(@NonNull int[] a, @NonNull int[] b) {
        int[] result = new int[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    /**
     * Merge two arrays into the new one
     *
     * @param a First array
     * @param b Second array
     * @return New array, filled by elements of {@code a}, followed by elements of {@code b}
     */
    @NonNull
    public static long[] merge(@NonNull long[] a, @NonNull long[] b) {
        long[] result = new long[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    /**
     * Merge two arrays into the new one
     *
     * @param a First array
     * @param b Second array
     * @return New array, filled by elements of {@code a}, followed by elements of {@code b}
     */
    @NonNull
    public static float[] merge(@NonNull float[] a, @NonNull float[] b) {
        float[] result = new float[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    /**
     * Merge two arrays into the new one
     *
     * @param a First array
     * @param b Second array
     * @return New array, filled by elements of {@code a}, followed by elements of {@code b}
     */
    @NonNull
    public static double[] merge(@NonNull double[] a, @NonNull double[] b) {
        double[] result = new double[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    /**
     * Merge two arrays into the new one
     *
     * @param a First array
     * @param b Second array
     * @return New array, filled by elements of {@code a}, followed by elements of {@code b}
     */
    @NonNull
    public static char[] merge(@NonNull char[] a, @NonNull char[] b) {
        char[] result = new char[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
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
     * Sorting algorithm is <b>unstable</b> (Heapsort)
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
    public static <T> void sort(@NonNull List<T> list, @NonNull Comparator<? super T> comparator) {
        int size = list.size();
        for (int i = size / 2 - 1; i >= 0; i--) {
            shift(list, comparator, i, size);
        }
        for (int i = size - 1; i >= 1; i--) {
            swap(list, 0, i);
            shift(list, comparator, 0, i);
        }
    }

    private static <T> void shift(@NonNull List<T> list, @NonNull Comparator<? super T> comparator,
            int i, int j) {
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
     * Search {@code item} in {@code list}, starting from specified {@code position}
     * in both directions
     * <br>
     * Such approach is effective if approximate position of an element in the list is known
     *
     * @param list     List of items
     * @param item     Item to search for
     * @param position Search start position
     * @return Position of {@code item} in {@code list} or {@code -1} if {@code item} is not found
     */
    public static <T> int search(@NonNull List<T> list, @Nullable T item, int position) {
        int step = Math.round(list.size() * 0.05F);
        if (step < 4) {
            step = 4;
        } else if (step > 16) {
            step = 16;
        }
        return search(list, item, position, step);
    }

    /**
     * Search {@code item} in {@code list}, starting from specified {@code position}
     * in both directions
     * <br>
     * Such approach is effective if approximate position of an element in the list is known
     * <br>
     * Starting from {@code position} it checks {@code step} of elements on the left
     * and at the right, if {@code item} is not found among them, another {@code step} of
     * elements on the left and at the right, and so on while {@code item} found or list ended
     *
     * @param list     List of items
     * @param item     Item to search for
     * @param position Search start position
     * @param step     Search step
     * @return Position of {@code item} in {@code list} or {@code -1} if {@code item} is not found
     */
    public static <T> int search(@NonNull List<T> list, @Nullable T item, int position, int step) {
        if (position < 0 || step < 1) {
            throw new IllegalArgumentException();
        }
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
                int startOffset = position - previousOffset - 1;
                for (int i = startOffset; i >= start; i--) {
                    if (Objects.equals(item, list.get(i))) {
                        return i;
                    }
                }
                int endOffset = position + previousOffset + 1;
                for (int i = endOffset; i < end; i++) {
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
        if (iterable == null) {
            return true;
        } else {
            if (iterable instanceof Collection<?>) {
                return ((Collection<?>) iterable).isEmpty();
            } else {
                return !iterable.iterator().hasNext();
            }
        }
    }

    /**
     * Check if specified array is {@code null} or empty
     */
    public static <T> boolean isNullOrEmpty(@Nullable T[] array) {
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
