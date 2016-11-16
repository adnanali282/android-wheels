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
