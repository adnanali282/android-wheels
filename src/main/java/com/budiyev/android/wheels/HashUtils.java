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

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Hashing tools
 */
public final class HashUtils {
    private static final int BUFFER_SIZE = 8192;
    public static final String ALGORITHM_MD5 = "MD5";
    public static final String ALGORITHM_SHA256 = "SHA-256";
    public static final String ALGORITHM_SHA512 = "SHA-512";
    public static final int DEFAULT_RADIX = 16;

    private HashUtils() {
    }

    /**
     * Generate MD5 hash string for specified {@link String}
     *
     * @param string Source string
     * @return MD5 hash string
     */
    @NonNull
    public static String generateMD5(@NonNull String string) {
        return generateHash(string, ALGORITHM_MD5);
    }

    /**
     * Generate MD5 hash string for specified data
     *
     * @param data Data
     * @return MD5 hash string
     */
    @NonNull
    public static String generateMD5(@NonNull byte[] data) {
        return generateHash(data, ALGORITHM_MD5);
    }

    /**
     * Generate SHA-256 hash string for specified {@link String}
     *
     * @param string Source string
     * @return SHA-256 hash string
     */
    @NonNull
    public static String generateSHA256(@NonNull String string) {
        return generateHash(string, ALGORITHM_SHA256);
    }

    /**
     * Generate SHA-256 hash string for specified data
     *
     * @param data Data
     * @return SHA-256 hash string
     */
    @NonNull
    public static String generateSHA256(@NonNull byte[] data) {
        return generateHash(data, ALGORITHM_SHA256);
    }

    /**
     * Generate SHA-512 hash string for specified {@link String}
     *
     * @param string Source string
     * @return SHA-512 hash string
     */
    @NonNull
    public static String generateSHA512(@NonNull String string) {
        return generateHash(string, ALGORITHM_SHA512);
    }

    /**
     * Generate SHA-512 hash string for specified data
     *
     * @param data Data
     * @return SHA-512 hash string
     */
    @NonNull
    public static String generateSHA512(@NonNull byte[] data) {
        return generateHash(data, ALGORITHM_SHA512);
    }

    /**
     * Generate hash string for the specified string, using specified algorithm
     *
     * @param string    Source string
     * @param algorithm Hashing algorithm
     * @return Hash string
     */
    @NonNull
    public static String generateHash(@NonNull String string, @NonNull String algorithm) {
        return generateHash(string.getBytes(), algorithm);
    }

    /**
     * Generate hash string for the specified data, using specified algorithm
     *
     * @param data      Data
     * @param algorithm Hashing algorithm
     * @return Hash string
     */
    @NonNull
    public static String generateHash(@NonNull byte[] data, @NonNull String algorithm) {
        return generateHash(data, algorithm, DEFAULT_RADIX);
    }

    /**
     * Generate hash string for the specified stream, using specified algorithm
     *
     * @param inputStream Data stream
     * @param algorithm   Hashing algorithm
     * @return Hash string
     */
    @NonNull
    public static String generateHash(@NonNull InputStream inputStream, @NonNull String algorithm) {
        return generateHash(inputStream, algorithm, DEFAULT_RADIX);
    }

    /**
     * Generate hash string for the specified string, using specified algorithm and radix
     *
     * @param string    Source string
     * @param algorithm Hashing algorithm
     * @param radix     Base to be used for the string representation of hash value
     * @return Hash string
     */
    @NonNull
    public static String generateHash(@NonNull String string, @NonNull String algorithm,
            @IntRange(from = Character.MIN_RADIX, to = Character.MAX_RADIX) int radix) {
        return generateHash(string.getBytes(), algorithm, radix);
    }

    /**
     * Generate hash string for the specified data, using specified algorithm and radix
     *
     * @param data      Data
     * @param algorithm Hashing algorithm
     * @param radix     Base to be used for the string representation of hash value
     * @return Hash string
     */
    @NonNull
    public static String generateHash(@NonNull byte[] data, @NonNull String algorithm,
            @IntRange(from = Character.MIN_RADIX, to = Character.MAX_RADIX) int radix) {
        return convertBytesToString(generateHashBytes(data, algorithm), radix);
    }

    /**
     * Generate hash string for the specified stream, using specified algorithm and radix
     *
     * @param inputStream Data stream
     * @param algorithm   Hashing algorithm
     * @param radix       Base to be used for the string representation of hash value
     * @return Hash string
     */
    @NonNull
    public static String generateHash(@NonNull InputStream inputStream, @NonNull String algorithm,
            @IntRange(from = Character.MIN_RADIX, to = Character.MAX_RADIX) int radix) {
        return convertBytesToString(generateHashBytes(inputStream, algorithm), radix);
    }

    /**
     * Generate hash bytes for the specified data, using specified algorithm
     *
     * @param data      Data
     * @param algorithm Hashing algorithm
     * @return Hash bytes
     */
    @NonNull
    public static byte[] generateHashBytes(@NonNull byte[] data, @NonNull String algorithm) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
            messageDigest.update(data);
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generate hash bytes for the specified stream, using specified algorithm
     *
     * @param inputStream Data stream
     * @param algorithm   Hashing algorithm
     * @return Hash bytes
     */
    @NonNull
    public static byte[] generateHashBytes(@NonNull InputStream inputStream,
            @NonNull String algorithm) {
        try (DigestInputStream digestStream = new DigestInputStream(inputStream,
                MessageDigest.getInstance(algorithm))) {
            byte[] buffer = new byte[BUFFER_SIZE];
            for (; ; ) {
                if (digestStream.read(buffer) < 0) {
                    break;
                }
            }
            return digestStream.getMessageDigest().digest();
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create string representation of byte array as single integer number
     *
     * @param data  Input data
     * @param radix Base to be used for the string representation
     * @return String representation of input data
     */
    @NonNull
    public static String convertBytesToString(@NonNull byte[] data,
            @IntRange(from = Character.MIN_RADIX, to = Character.MAX_RADIX) int radix) {
        return new BigInteger(1, data).toString(radix);
    }
}
