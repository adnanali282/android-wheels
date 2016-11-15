package com.budiyev.android.wheels;

import android.support.annotation.Nullable;

/**
 * Convenience methods to unpack nullable packed numbers
 */
public final class NumberUtils {
    private NumberUtils() {
    }

    /**
     * Convenience method to unpack nullable number value, {@code null} = {@code 0}
     *
     * @param value Packed value
     * @return Unpacked value
     */
    public static long unpack(@Nullable Long value) {
        return value == null ? 0 : value;
    }

    /**
     * Convenience method to unpack nullable number value, {@code null} = {@code 0}
     *
     * @param value Packed value
     * @return Unpacked value
     */
    public static int unpack(@Nullable Integer value) {
        return value == null ? 0 : value;
    }

    /**
     * Convenience method to unpack nullable number value, {@code null} = {@code 0}
     *
     * @param value Packed value
     * @return Unpacked value
     */
    public static short unpack(@Nullable Short value) {
        return value == null ? 0 : value;
    }

    /**
     * Convenience method to unpack nullable number value, {@code null} = {@code 0}
     *
     * @param value Packed value
     * @return Unpacked value
     */
    public static byte unpack(@Nullable Byte value) {
        return value == null ? 0 : value;
    }

    /**
     * Convenience method to unpack nullable number value, {@code null} = {@code 0}
     *
     * @param value Packed value
     * @return Unpacked value
     */
    public static double unpack(@Nullable Double value) {
        return value == null ? 0 : value;
    }

    /**
     * Convenience method to unpack nullable number value, {@code null} = {@code 0}
     *
     * @param value Packed value
     * @return Unpacked value
     */
    public static float unpack(@Nullable Float value) {
        return value == null ? 0 : value;
    }
}
