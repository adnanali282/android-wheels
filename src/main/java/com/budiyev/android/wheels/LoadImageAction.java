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

import android.graphics.Bitmap;
import android.support.annotation.AnyThread;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Load image action for {@link ImageLoader}
 */
final class LoadImageAction<T> {
    private final ImageSource<T> mImageSource;
    private final WeakReference<ImageView> mImageViewReference;
    private final ImageLoader<T> mImageLoader;
    private final Object mPauseWorkLock;
    private final ImageLoadCallback<T> mImageLoadCallback;
    private final AtomicBoolean mSubmitted = new AtomicBoolean();
    private final AtomicBoolean mFinished = new AtomicBoolean();
    private final AtomicBoolean mCancelled = new AtomicBoolean();
    private final AtomicReference<Future<?>> mFuture = new AtomicReference<>();

    public LoadImageAction(@NonNull ImageSource<T> imageSource, @NonNull ImageView imageView,
            @NonNull ImageLoader<T> imageLoader, @NonNull Object pauseWorkLock,
            @Nullable ImageLoadCallback<T> imageLoadCallback) {
        mImageSource = imageSource;
        mImageViewReference = new WeakReference<>(imageView);
        mImageLoader = imageLoader;
        mPauseWorkLock = pauseWorkLock;
        mImageLoadCallback = imageLoadCallback;
    }

    @AnyThread
    public boolean execute() {
        if (!isCancelled() && !isFinished() && mSubmitted.compareAndSet(false, true)) {
            mFuture.set(InternalExecutors.getImageLoaderExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    loadImage();
                    mFinished.set(true);
                    mSubmitted.set(false);
                }
            }));
            return true;
        } else {
            return false;
        }
    }

    @NonNull
    public ImageSource<T> getImageSource() {
        return mImageSource;
    }

    @Nullable
    @MainThread
    public ImageView getAttachedImageView() {
        ImageView imageView = mImageViewReference.get();
        LoadImageAction<?> loadImageAction = ImageLoader.getLoadImageAction(imageView);
        if (this == loadImageAction) {
            return imageView;
        }
        return null;
    }

    public void cancel() {
        mCancelled.set(true);
        Future<?> future = mFuture.get();
        if (future != null) {
            future.cancel(false);
        }
    }

    public boolean reset() {
        if (isFinished() || isCancelled()) {
            mFuture.set(null);
            mFinished.set(false);
            mCancelled.set(false);
            mSubmitted.set(false);
            return true;
        } else {
            return false;
        }
    }

    public boolean isFinished() {
        return mFinished.get();
    }

    public boolean isCancelled() {
        return mCancelled.get();
    }

    @WorkerThread
    private void loadImage() {
        synchronized (mPauseWorkLock) {
            while (mImageLoader.isPauseWork() && !mImageLoader.isExitTasksEarly() &&
                    !isCancelled()) {
                try {
                    mPauseWorkLock.wait();
                } catch (InterruptedException ignored) {
                }
            }
        }
        if (isCancelled() || mImageLoader.isExitTasksEarly()) {
            return;
        }
        Bitmap image = null;
        StorageImageCache storageImageCache = mImageLoader.getStorageImageCache();
        String key = mImageSource.getKey();
        T data = mImageSource.getData();
        if (storageImageCache != null) {
            image = storageImageCache.get(key);
            if (image != null && mImageLoadCallback != null) {
                reportImageLoaded(data, image, false, true);
            }
        }
        if (image == null) {
            BitmapLoader<T> bitmapLoader = mImageLoader.getBitmapLoader();
            if (bitmapLoader != null) {
                try {
                    image = bitmapLoader.load(mImageLoader.getContext(), data);
                } catch (Exception exception) {
                    reportError(data, exception);
                    return;
                }
                if (image == null) {
                    reportError(data, new NullPointerException("BitmapLoader returned null"));
                    return;
                }
            }
            if (image != null) {
                reportImageLoaded(data, image, false, false);
                if (storageImageCache != null) {
                    storageImageCache.put(key, image);
                }
            }
        }
        RecyclingBitmapDrawable drawable = null;
        if (image != null) {
            drawable = new RecyclingBitmapDrawable(mImageLoader.getContext().getResources(), image);
            MemoryImageCache memoryImageCache = mImageLoader.getMemoryImageCache();
            if (memoryImageCache != null) {
                memoryImageCache.put(key, drawable);
            }
        }
        if (isCancelled() || mImageLoader.isExitTasksEarly()) {
            return;
        }
        ThreadUtils.runOnMainThread(
                new SetImageAction<>(drawable, mImageLoader, mImageLoadCallback, this));
    }

    @AnyThread
    private void reportImageLoaded(final T data, final Bitmap image, final boolean fromMemoryCache,
            final boolean fromStorageCache) {
        if (mImageLoadCallback != null) {
            ThreadUtils.runOnMainThread(new Runnable() {
                @Override
                @MainThread
                public void run() {
                    mImageLoadCallback.onLoaded(data, image, fromMemoryCache, fromStorageCache);
                }
            });
        }
    }

    @AnyThread
    private void reportError(final T data, final Exception exception) {
        if (mImageLoadCallback != null) {
            ThreadUtils.runOnMainThread(new Runnable() {
                @Override
                @MainThread
                public void run() {
                    mImageLoadCallback.onError(data, exception);
                }
            });
        }
    }
}
