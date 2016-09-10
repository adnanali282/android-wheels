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

/**
 * Accumulator function
 *
 * @see IterableQuery#aggregate(Aggregator)
 * @see IterableQuery#aggregate(Object, Aggregator)
 * @see IterableQuery#aggregate(boolean, Aggregator)
 * @see IterableQuery#aggregate(byte, Aggregator)
 * @see IterableQuery#aggregate(short, Aggregator)
 * @see IterableQuery#aggregate(int, Aggregator)
 * @see IterableQuery#aggregate(long, Aggregator)
 * @see IterableQuery#aggregate(float, Aggregator)
 * @see IterableQuery#aggregate(double, Aggregator)
 * @see IterableQuery#aggregate(char, Aggregator)
 * @see IterableQuery#aggregate(Object, Aggregator, Converter)
 */
public interface Aggregator<A, T> {
    /**
     * Apply function
     *
     * @param accumulator Accumulated value
     * @param value       Next value from sequence
     * @return Accumulated value
     */
    A apply(A accumulator, T value);
}
