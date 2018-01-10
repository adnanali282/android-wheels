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

import java.io.File;
import java.io.InputStream;

import android.support.annotation.NonNull;

/**
 * Parameter of HTTP request (multipart/form-data)
 */
public final class HttpBodyParameter {
    final String key;
    final String value;
    final File file;
    final InputStream stream;
    final String fileName;
    final String contentType;

    HttpBodyParameter(@NonNull String key, @NonNull String value) {
        this.key = CommonUtils.requireNonNull(key);
        this.value = CommonUtils.requireNonNull(value);
        this.file = null;
        this.stream = null;
        this.fileName = null;
        this.contentType = null;
    }

    HttpBodyParameter(@NonNull String key, @NonNull File file) {
        this.key = CommonUtils.requireNonNull(key);
        this.value = null;
        this.file = CommonUtils.requireNonNull(file);
        this.stream = null;
        this.fileName = null;
        this.contentType = null;
    }

    HttpBodyParameter(@NonNull String key, @NonNull InputStream stream, @NonNull String fileName,
            @NonNull String contentType) {
        this.key = CommonUtils.requireNonNull(key);
        this.value = null;
        this.file = null;
        this.stream = CommonUtils.requireNonNull(stream);
        this.fileName = CommonUtils.requireNonNull(fileName);
        this.contentType = CommonUtils.requireNonNull(contentType);
    }

    @Override
    public int hashCode() {
        return CommonUtils.hash(key, value, file, stream, fileName, contentType);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof HttpBodyParameter) {
            HttpBodyParameter other = (HttpBodyParameter) obj;
            return CommonUtils.equals(key, other.key) && CommonUtils.equals(value, other.value) &&
                    CommonUtils.equals(file, other.file) && CommonUtils.equals(stream, other.stream) &&
                    CommonUtils.equals(fileName, other.fileName) && CommonUtils.equals(contentType, other.contentType);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("HttpBodyParameter [key: ").append(key);
        if (value != null) {
            stringBuilder.append("; value: ").append(value);
        }
        if (file != null) {
            stringBuilder.append("; file: ").append(file);
        }
        if (stream != null) {
            stringBuilder.append("; stream: ").append(stream);
        }
        if (fileName != null) {
            stringBuilder.append("; file name: ").append(fileName);
        }
        if (contentType != null) {
            stringBuilder.append("; content type: ").append(contentType);
        }
        return stringBuilder.append(']').toString();
    }
}
