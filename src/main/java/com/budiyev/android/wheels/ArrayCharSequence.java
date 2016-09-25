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

import java.util.Objects;

/**
 * {@link CharSequence} array wrapper
 */
final class ArrayCharSequence implements CharSequence {
    private final char[] mArray;
    private final int mStart;
    private final int mEnd;
    private final int mLength;

    public ArrayCharSequence(@NonNull char[] array, int start, int end) {
        if (start > end) {
            throw new IllegalArgumentException();
        }
        if (start < 0 || end > array.length) {
            throw new IndexOutOfBoundsException();
        }
        mArray = Objects.requireNonNull(array);
        mStart = start;
        mEnd = end;
        mLength = end - start;
    }

    @Override
    public int length() {
        return mLength;
    }

    @Override
    public char charAt(int index) {
        if (index < 0 || index >= mLength) {
            throw new IndexOutOfBoundsException();
        }
        return mArray[mStart + index];
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        if (start > end) {
            throw new IllegalArgumentException();
        }
        int subStart = mStart + start;
        int subEnd = mStart + end;
        if (start < 0 || subStart < 0 || subEnd > mLength) {
            throw new IndexOutOfBoundsException();
        }
        return new ArrayCharSequence(mArray, subStart, subEnd);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof CharSequence) {
            CharSequence other = (CharSequence) o;
            if (other.length() == mLength) {
                for (int i = 0; i < mLength; i++) {
                    if (other.charAt(i) != mArray[mStart + i]) {
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
        int hashCode = 1;
        for (int i = mStart; i < mEnd; i++) {
            hashCode = 31 * hashCode + mArray[i];
        }
        return hashCode;
    }

    @NonNull
    @Override
    public String toString() {
        return new String(mArray, mStart, mLength);
    }
}
