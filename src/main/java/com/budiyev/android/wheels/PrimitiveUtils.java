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

import android.support.annotation.Nullable;

/**
 * Convenience methods to unpack nullable packed primitive types,
 * {@code null} value will be unpacked to the default value of concrete primitive type
 * according to the Java Language Specification.
 */
public final class PrimitiveUtils {
    private PrimitiveUtils() {
    }

    /**
     * Convenience method to unpack nullable packed primitive value
     *
     * @param value Packed value
     * @return Unpacked value, default if {@code value} is {@code null}
     */
    public static byte unpack(@Nullable Byte value) {
        return value == null ? (byte) 0 : value;
    }

    /**
     * Convenience method to unpack nullable packed primitive value
     *
     * @param value Packed value
     * @return Unpacked value, default if {@code value} is {@code null}
     */
    public static short unpack(@Nullable Short value) {
        return value == null ? (short) 0 : value;
    }

    /**
     * Convenience method to unpack nullable packed primitive value
     *
     * @param value Packed value
     * @return Unpacked value, default if {@code value} is {@code null}
     */
    public static int unpack(@Nullable Integer value) {
        return value == null ? 0 : value;
    }

    /**
     * Convenience method to unpack nullable packed primitive value
     *
     * @param value Packed value
     * @return Unpacked value, default if {@code value} is {@code null}
     */
    public static long unpack(@Nullable Long value) {
        return value == null ? 0L : value;
    }

    /**
     * Convenience method to unpack nullable packed primitive value
     *
     * @param value Packed value
     * @return Unpacked value, default if {@code value} is {@code null}
     */
    public static float unpack(@Nullable Float value) {
        return value == null ? 0.0f : value;
    }

    /**
     * Convenience method to unpack nullable packed primitive value
     *
     * @param value Packed value
     * @return Unpacked value, default if {@code value} is {@code null}
     */
    public static double unpack(@Nullable Double value) {
        return value == null ? 0.0d : value;
    }

    /**
     * Convenience method to unpack nullable packed primitive value
     *
     * @param value Packed value
     * @return Unpacked value, default if {@code value} is {@code null}
     */
    public static char unpack(@Nullable Character value) {
        return value == null ? '\u0000' : value;
    }

    /**
     * Convenience method to unpack nullable packed primitive value
     *
     * @param value Packed value
     * @return Unpacked value, default if {@code value} is {@code null}
     */
    public static boolean unpack(@Nullable Boolean value) {
        return value == null ? false : value;
    }
}
