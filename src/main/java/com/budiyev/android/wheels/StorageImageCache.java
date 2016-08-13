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

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;

/**
 * Storage image cache for {@link ImageLoader}
 *
 * @see ImageLoader#getMemoryImageCache()
 * @see ImageLoader#setMemoryImageCache(MemoryImageCache)
 * @see ImageLoader#newStorageImageCache(Context)
 * @see ImageLoader#newStorageImageCache(File)
 * @see ImageLoader#newStorageImageCache(File, long, Bitmap.CompressFormat, int)
 */
public interface StorageImageCache {
    /**
     * Put {@link Bitmap} into cache
     *
     * @param key   Unique key
     * @param value Image bitmap
     */
    void put(@NonNull String key, @NonNull Bitmap value);

    /**
     * Get {@link Bitmap} for the specified key
     *
     * @param key Unique key
     * @return Image bitmap or null, id there are no entry for the specified key
     */
    @Nullable
    Bitmap get(@NonNull String key);

    /**
     * Remove entry with specified {@code key} from cache
     *
     * @param key Unique key
     */
    void remove(@NonNull String key);

    /**
     * Clear cache
     */
    void clear();
}
