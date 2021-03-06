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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Tools for collections
 */
public final class CollectionUtils {
    private CollectionUtils() {
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
            copy = new ArrayList<>((Collection<T>) iterable);
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
            result = new ArrayList<>((Collection<T>) a);
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
     * Merge two arrays into the new one
     *
     * @param a First array
     * @param b Second array
     * @return New array, filled by elements of {@code a}, followed by elements of {@code b}
     */
    @NonNull
    @SuppressWarnings("unchecked")
    public static <T> T[] merge(@NonNull T[] a, @NonNull T[] b) {
        T[] result = (T[]) Array.newInstance(a.getClass().getComponentType(), a.length + b.length);
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
        if (CommonUtils.equals(item, list.get(position))) {
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
                    if (CommonUtils.equals(item, list.get(i))) {
                        return i;
                    }
                }
                int endOffset = position + previousOffset + 1;
                for (int i = endOffset; i < end; i++) {
                    if (CommonUtils.equals(item, list.get(i))) {
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
     * Whether if specified {@link Iterable} contains {@code element}
     *
     * @param iterable an iterable to be checked
     * @param element  element
     * @return {@code true} if {@code iterable} contains {@code element}, {@code false} otherwise
     */
    public static <T> boolean contains(@NonNull Iterable<T> iterable, @Nullable T element) {
        if (iterable instanceof Collection<?>) {
            return ((Collection<T>) iterable).contains(element);
        } else {
            for (T e : iterable) {
                if (CommonUtils.equals(e, element)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Whether if specified {@code array} contains {@code element}
     *
     * @param array   an array to be checked
     * @param element element
     * @return {@code true} if {@code array} contains {@code element}, {@code false} otherwise
     */
    public static <T> boolean contains(@NonNull T[] array, @Nullable T element) {
        for (T e : array) {
            if (CommonUtils.equals(e, element)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether if specified {@code array} contains {@code element}
     *
     * @param array   an array to be checked
     * @param element element
     * @return {@code true} if {@code array} contains {@code element}, {@code false} otherwise
     */
    public static boolean contains(@NonNull boolean[] array, boolean element) {
        for (boolean e : array) {
            if (e == element) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether if specified {@code array} contains {@code element}
     *
     * @param array   an array to be checked
     * @param element element
     * @return {@code true} if {@code array} contains {@code element}, {@code false} otherwise
     */
    public static boolean contains(@NonNull byte[] array, byte element) {
        for (byte e : array) {
            if (e == element) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether if specified {@code array} contains {@code element}
     *
     * @param array   an array to be checked
     * @param element element
     * @return {@code true} if {@code array} contains {@code element}, {@code false} otherwise
     */
    public static boolean contains(@NonNull short[] array, short element) {
        for (short e : array) {
            if (e == element) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether if specified {@code array} contains {@code element}
     *
     * @param array   an array to be checked
     * @param element element
     * @return {@code true} if {@code array} contains {@code element}, {@code false} otherwise
     */
    public static boolean contains(@NonNull int[] array, int element) {
        for (int e : array) {
            if (e == element) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether if specified {@code array} contains {@code element}
     *
     * @param array   an array to be checked
     * @param element element
     * @return {@code true} if {@code array} contains {@code element}, {@code false} otherwise
     */
    public static boolean contains(@NonNull long[] array, long element) {
        for (long e : array) {
            if (e == element) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether if specified {@code array} contains {@code element}
     *
     * @param array   an array to be checked
     * @param element element
     * @return {@code true} if {@code array} contains {@code element}, {@code false} otherwise
     */
    public static boolean contains(@NonNull float[] array, float element) {
        for (float e : array) {
            if (e == element) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether if specified {@code array} contains {@code element}
     *
     * @param array   an array to be checked
     * @param element element
     * @return {@code true} if {@code array} contains {@code element}, {@code false} otherwise
     */
    public static boolean contains(@NonNull double[] array, double element) {
        for (double e : array) {
            if (e == element) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether if specified {@code array} contains {@code element}
     *
     * @param array   an array to be checked
     * @param element element
     * @return {@code true} if {@code array} contains {@code element}, {@code false} otherwise
     */
    public static boolean contains(@NonNull char[] array, char element) {
        for (char e : array) {
            if (e == element) {
                return true;
            }
        }
        return false;
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
