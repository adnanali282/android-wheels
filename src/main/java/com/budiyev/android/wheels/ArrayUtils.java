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

import java.util.Iterator;

/**
 * Tools for arrays
 */
public class ArrayUtils {
    private ArrayUtils() {
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
}
