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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentOptionalCompat<T> {
    private final Lock mLock = new ReentrantLock();
    private volatile T mValue;

    /**
     * Empty {@link ConcurrentOptionalCompat}
     */
    public ConcurrentOptionalCompat() {
    }

    /**
     * {@link ConcurrentOptionalCompat} with specified {@code value}
     */
    public ConcurrentOptionalCompat(@NonNull T value) {
        mValue = Objects.requireNonNull(value);
    }

    /**
     * Get value of this {@link ConcurrentOptionalCompat}
     *
     * @throws EmptyOptionalException if this {@link ConcurrentOptionalCompat} has no value
     */
    @NonNull
    public T get() {
        mLock.lock();
        try {
            T value = mValue;
            if (value == null) {
                throw new EmptyOptionalException();
            } else {
                return value;
            }
        } finally {
            mLock.unlock();
        }
    }

    /**
     * Set value of this {@link ConcurrentOptionalCompat}
     */
    public void set(@NonNull T value) {
        mLock.lock();
        try {
            mValue = Objects.requireNonNull(value);
        } finally {
            mLock.unlock();
        }
    }

    /**
     * Whether if this {@link ConcurrentOptionalCompat} has value
     */
    public boolean hasValue() {
        mLock.lock();
        try {
            return mValue != null;
        } finally {
            mLock.unlock();
        }
    }

    /**
     * Clear value of this {@link ConcurrentOptionalCompat}
     *
     * @return {@code true} if this {@link ConcurrentOptionalCompat} wasn't empty,
     * {@code false} otherwise
     */
    public boolean clear() {
        mLock.lock();
        try {
            if (mValue == null) {
                return false;
            } else {
                mValue = null;
                return true;
            }
        } finally {
            mLock.unlock();
        }
    }

    @Override
    public boolean equals(Object o) {
        mLock.lock();
        try {
            return o == this || o instanceof ConcurrentOptionalCompat<?> &&
                    Objects.equals(((ConcurrentOptionalCompat) o).mValue, mValue);
        } finally {
            mLock.unlock();
        }
    }

    @Override
    public int hashCode() {
        mLock.lock();
        try {
            T value = mValue;
            if (value == null) {
                return 0;
            } else {
                return value.hashCode();
            }
        } finally {
            mLock.unlock();
        }
    }

    @Override
    public String toString() {
        mLock.lock();
        try {
            T value = mValue;
            if (value == null) {
                return "ConcurrentOptionalCompat <>";
            } else {
                return "ConcurrentOptionalCompat [" + value + "]";
            }
        } finally {
            mLock.unlock();
        }
    }
}
