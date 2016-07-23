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

import java.io.File;
import java.io.InputStream;
import java.util.Objects;

/**
 * Parameter of HTTP request (multipart/form-data)
 */
public final class PostParameter {
    final String key;
    final String value;
    final File file;
    final InputStream stream;
    final String fileName;
    final String contentType;

    PostParameter(@NonNull String key, @NonNull String value) {
        this.key = Objects.requireNonNull(key);
        this.value = Objects.requireNonNull(value);
        this.file = null;
        this.stream = null;
        this.fileName = null;
        this.contentType = null;
    }

    PostParameter(@NonNull String key, @NonNull File file) {
        this.key = Objects.requireNonNull(key);
        this.value = null;
        this.file = Objects.requireNonNull(file);
        this.stream = null;
        this.fileName = null;
        this.contentType = null;
    }

    PostParameter(@NonNull String key, @NonNull InputStream stream, @NonNull String fileName,
            @NonNull String contentType) {
        this.key = Objects.requireNonNull(key);
        this.value = null;
        this.file = null;
        this.stream = Objects.requireNonNull(stream);
        this.fileName = Objects.requireNonNull(fileName);
        this.contentType = Objects.requireNonNull(contentType);
    }
}
