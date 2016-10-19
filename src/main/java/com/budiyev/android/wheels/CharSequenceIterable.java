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
import java.util.Objects;

/**
 * {@link Iterable} {@link CharSequence} wrapper
 */
final class CharSequenceIterable implements Iterable<Character> {
    private final CharSequence mCharSequence;
    private final int mStart;
    private final int mEnd;

    public CharSequenceIterable(@NonNull CharSequence charSequence) {
        mCharSequence = Objects.requireNonNull(charSequence);
        mStart = 0;
        mEnd = charSequence.length();
    }

    public CharSequenceIterable(@NonNull CharSequence charSequence, int start, int length) {
        mCharSequence = Objects.requireNonNull(charSequence);
        int end = start + length;
        if (start < 0 || length < 0 || end > charSequence.length()) {
            throw new IllegalArgumentException();
        }
        mStart = start;
        mEnd = end;
    }

    @NonNull
    @Override
    public Iterator<Character> iterator() {
        return new CharSequenceIterator();
    }

    @Override
    public int hashCode() {
        return mCharSequence.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof CharSequenceIterable &&
                Objects.equals(mCharSequence, ((CharSequenceIterable) obj).mCharSequence);
    }

    @NonNull
    @Override
    public String toString() {
        return mCharSequence.toString();
    }

    private class CharSequenceIterator implements Iterator<Character> {
        private int mPosition = mStart - 1;

        @Override
        public boolean hasNext() {
            return mPosition + 1 < mEnd;
        }

        @Override
        public Character next() {
            return mCharSequence.charAt(++mPosition);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
