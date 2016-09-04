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

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

/**
 * {@link Iterable} array wrapper
 */
final class BooleanArrayIterable implements Iterable<Boolean> {
    private final boolean[] mArray;

    public BooleanArrayIterable(@NonNull boolean[] array) {
        mArray = Objects.requireNonNull(array);
    }

    @Override
    public Iterator<Boolean> iterator() {
        return new Iterator<Boolean>() {
            private int position = -1;

            @Override
            public boolean hasNext() {
                return position + 1 < mArray.length;
            }

            @Override
            public Boolean next() {
                return mArray[++position];
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof BooleanArrayIterable) {
            BooleanArrayIterable other = (BooleanArrayIterable) o;
            if (other.mArray.length == mArray.length) {
                for (int i = 0; i < mArray.length; i++) {
                    if (other.mArray[i] != mArray[i]) {
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(mArray);
    }

    @NonNull
    @Override
    public String toString() {
        return Arrays.toString(mArray);
    }
}