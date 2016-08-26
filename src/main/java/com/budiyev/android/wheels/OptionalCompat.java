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
 * A container object which may or may not contain a non-null value
 */
public class OptionalCompat<T> {
    private volatile T mValue;

    /**
     * Empty {@link OptionalCompat}
     */
    public OptionalCompat() {
    }

    /**
     * {@link OptionalCompat} with specified {@code value}
     */
    public OptionalCompat(@NonNull T value) {
        mValue = Objects.requireNonNull(value);
    }

    /**
     * Value of this {@link OptionalCompat}
     *
     * @throws EmptyOptionalException if this {@link OptionalCompat} has no value
     */
    @NonNull
    public T getValue() {
        T value = mValue;
        if (value == null) {
            throw new EmptyOptionalException();
        } else {
            return value;
        }
    }

    /**
     * Value of this {@link OptionalCompat}
     */
    public void setValue(@NonNull T value) {
        mValue = Objects.requireNonNull(value);
    }

    /**
     * Whether if this {@link OptionalCompat} has value
     */
    public boolean hasValue() {
        return mValue != null;
    }

    /**
     * Clear value of this {@link OptionalCompat}
     *
     * @return {@code true} if this {@link OptionalCompat} wasn't empty, {@code false} otherwise
     */
    public boolean clear() {
        T value = mValue;
        if (value == null) {
            return false;
        } else {
            mValue = null;
            return true;
        }
    }

    @Override
    public boolean equals(Object o) {
        return o == this || o instanceof OptionalCompat<?> &&
                Objects.equals(((OptionalCompat) o).mValue, mValue);
    }

    @Override
    public int hashCode() {
        T value = mValue;
        if (value == null) {
            return 0;
        } else {
            return value.hashCode();
        }
    }

    @Override
    public String toString() {
        T value = mValue;
        if (value == null) {
            return "OptionalCompat []";
        } else {
            return "OptionalCompat [" + value + "]";
        }
    }
}
