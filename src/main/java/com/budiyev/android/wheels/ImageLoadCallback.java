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
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.widget.ImageView;

/**
 * Callback of image loading
 *
 * @see ImageLoader#loadImage(ImageSource, ImageView, ImageLoadCallback)
 */
public interface ImageLoadCallback<T> {
    /**
     * Called when image is loaded and ready to be displayed
     *
     * @param data             Source data
     * @param image            Image, loaded from {@code data}
     * @param fromMemoryCache  Whether if image is loaded from memory cache
     * @param fromStorageCache Whether if image is loaded from storage cache
     */
    @MainThread
    void onLoaded(@NonNull T data, @NonNull Bitmap image, boolean fromMemoryCache,
            boolean fromStorageCache);

    /**
     * Called when image displayed; if fade effect is enabled, this method will be called
     * when fade will done
     *
     * @param data      Source data
     * @param image     Loaded image
     * @param imageView {@link ImageView}, on which {@code image} loaded form {@code data}
     *                  is displayed
     */
    @MainThread
    void onDisplayed(@NonNull T data, @NonNull Bitmap image, @NonNull ImageView imageView);

    /**
     * Called when {@link BitmapLoader} was unable to load {@link Bitmap}
     *
     * @param data      Source data
     * @param exception Exception, that has been thrown by
     *                  {@link BitmapLoader#load(Context, Object)} method or
     *                  {@link NullPointerException} if {@link BitmapLoader#load(Context, Object)}
     *                  method returned null
     */
    @MainThread
    void onError(@NonNull T data, @NonNull Exception exception);
}
