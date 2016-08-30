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

/**
 * Default {@link ImageSource} implementation
 *
 * @see ImageLoader#newImageSource(Object)
 */
final class ImageSourceImplementation<T> implements ImageSource<T> {
    private final Lock mKeyLock = new ReentrantLock();
    private final T mData;
    private volatile String mKey;

    public ImageSourceImplementation(@NonNull T data) {
        mData = Objects.requireNonNull(data);
    }

    @NonNull
    @Override
    public T getData() {
        return mData;
    }

    @NonNull
    @Override
    public String getKey() {
        String key = mKey;
        if (key == null) {
            mKeyLock.lock();
            try {
                key = mKey;
                if (key == null) {
                    key = HashUtils.generateSHA256(String.valueOf(mData));
                    mKey = key;
                }
            } finally {
                mKeyLock.unlock();
            }
        }
        return key;
    }
}
