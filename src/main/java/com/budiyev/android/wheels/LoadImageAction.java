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
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * Load image action for {@link ImageLoader}
 */
final class LoadImageAction<T> {
    @NonNull
    private final ImageSource<T> mImageSource;
    @NonNull
    private final WeakReference<ImageView> mImageViewReference;
    @NonNull
    private final ImageLoader<T> mImageLoader;
    @NonNull
    private final Lock mPauseLoadingLock;
    @NonNull
    private final Condition mPauseLoadingCondition;
    @Nullable
    private final ImageLoadCallback<T> mImageLoadCallback;
    @NonNull
    private final AtomicBoolean mExecuting = new AtomicBoolean();
    @Nullable
    private volatile Future<?> mFuture;
    private volatile boolean mCancelled;

    @AnyThread
    public LoadImageAction(@NonNull ImageSource<T> source, @NonNull ImageView view,
            @NonNull ImageLoader<T> loader, @NonNull Lock lock, @NonNull Condition condition,
            @Nullable ImageLoadCallback<T> callback) {
        mImageSource = source;
        mImageViewReference = new WeakReference<>(view);
        mImageLoader = loader;
        mPauseLoadingLock = lock;
        mPauseLoadingCondition = condition;
        mImageLoadCallback = callback;
    }

    @AnyThread
    public boolean execute() {
        if (!mCancelled && mExecuting.compareAndSet(false, true)) {
            mFuture = InternalExecutors.getImageLoaderExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    loadImage();
                    mFuture = null;
                    mExecuting.set(false);
                }
            });
            return true;
        } else {
            return false;
        }
    }

    @AnyThread
    public void cancel() {
        mCancelled = true;
        Future<?> future = mFuture;
        if (future != null) {
            future.cancel(false);
        }
    }

    @NonNull
    @AnyThread
    public ImageSource<T> getImageSource() {
        return mImageSource;
    }

    @Nullable
    @MainThread
    public ImageView getAttachedImageView() {
        ImageView imageView = mImageViewReference.get();
        LoadImageAction<?> loadImageAction = ImageLoader.getLoadImageAction(imageView);
        if (loadImageAction == this) {
            return imageView;
        } else {
            return null;
        }
    }

    @AnyThread
    public boolean isCancelled() {
        return mCancelled;
    }

    @WorkerThread
    private void loadImage() {
        mPauseLoadingLock.lock();
        try {
            while (!mCancelled && mImageLoader.isLoadingPaused() &&
                    !mImageLoader.isExitTasksEarly()) {
                mPauseLoadingCondition.awaitUninterruptibly();
            }
        } finally {
            mPauseLoadingLock.unlock();
        }
        if (mCancelled || mImageLoader.isExitTasksEarly()) {
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
        if (mCancelled || mImageLoader.isExitTasksEarly()) {
            return;
        }
        ThreadUtils.runOnMainThread(
                new SetImageAction<>(drawable, mImageLoader, mImageLoadCallback, this));
    }

    @AnyThread
    private void reportImageLoaded(@NonNull final T data, @NonNull final Bitmap image,
            final boolean fromMemoryCache, final boolean fromStorageCache) {
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
    private void reportError(@NonNull final T data, @NonNull final Exception exception) {
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
