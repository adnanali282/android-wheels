/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2016 Yuriy Budiyev [yuriy.budiyev@yandex.ru]
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.budiyev.android.wheels;

import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;

/**
 * Default implementation of {@link MemoryImageCache} for {@link ImageLoader}
 */
class MemoryImageCacheImplementation implements MemoryImageCache {
    private final LruCache<String, BitmapDrawable> mCache;

    /**
     * Memory image cache
     *
     * @param size Size in bytes
     */
    public MemoryImageCacheImplementation(int size) {
        mCache = new LruCache<String, BitmapDrawable>(size) {
            @Override
            protected void entryRemoved(boolean evicted, String key, BitmapDrawable oldValue,
                    BitmapDrawable newValue) {
                if (oldValue instanceof RecyclingBitmapDrawable) {
                    ((RecyclingBitmapDrawable) oldValue).setCached(false);
                }
            }

            @Override
            protected int sizeOf(String key, BitmapDrawable value) {
                return value.getBitmap().getAllocationByteCount();
            }
        };
    }

    @Override
    public void put(@NonNull String key, @NonNull BitmapDrawable value) {
        if (value instanceof RecyclingBitmapDrawable) {
            ((RecyclingBitmapDrawable) value).setCached(true);
        }
        mCache.put(key, value);
    }

    @Nullable
    @Override
    public BitmapDrawable get(@NonNull String key) {
        return mCache.get(key);
    }

    @Override
    public void remove(@NonNull String key) {
        mCache.remove(key);
    }

    @Override
    public void clear() {
        mCache.evictAll();
    }
}
