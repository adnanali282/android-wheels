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

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Load image action for {@link ImageLoader}
 *
 * @param <T>
 */
class LoadImageAction<T> {
    private final ImageSource<T> mImageSource;
    private final WeakReference<ImageView> mImageViewReference;
    private final ImageLoader<T> mImageLoader;
    private final ImageLoadCallback mImageLoadCallback;
    private final AtomicBoolean mSubmitted = new AtomicBoolean();
    private volatile boolean mFinished;
    private volatile boolean mCancelled;
    private volatile Future<Void> mFuture;

    public LoadImageAction(@NonNull ImageSource<T> imageSource, @NonNull ImageView imageView,
            @Nullable ImageLoadCallback imageLoadCallback, @NonNull ImageLoader<T> imageLoader) {
        mImageSource = imageSource;
        mImageViewReference = new WeakReference<>(imageView);
        mImageLoadCallback = imageLoadCallback;
        mImageLoader = imageLoader;
    }

    public void loadImage() {
        Bitmap image = null;
        synchronized (mImageLoader.getPauseWorkLock()) {
            while (mImageLoader.isPauseWork() && !isCancelled()) {
                try {
                    mImageLoader.getPauseWorkLock().wait();
                } catch (InterruptedException ignored) {
                }
            }
        }
        if (!isCancelled() && getAttachedImageView() != null &&
                !mImageLoader.isExitTasksEarly()) {
            StorageImageCache storageImageCache = mImageLoader.getStorageImageCache();
            if (storageImageCache != null) {
                image = storageImageCache.get(mImageSource.getKey());
                if (image != null && mImageLoadCallback != null) {
                    mImageLoadCallback.onImageLoaded(image, false, true);
                }
            }
            if (image == null) {
                BitmapLoader<T> bitmapLoader = mImageLoader.getBitmapLoader();
                if (bitmapLoader != null) {
                    image = bitmapLoader.load(mImageLoader.getContext(), mImageSource.getData());
                }
                if (image != null) {
                    if (mImageLoadCallback != null) {
                        mImageLoadCallback.onImageLoaded(image, false, false);
                    }
                    if (storageImageCache != null) {
                        storageImageCache.put(mImageSource.getKey(), image);
                    }
                }
            }
        }
        RecyclingBitmapDrawable drawable = null;
        if (image != null) {
            drawable = new RecyclingBitmapDrawable(mImageLoader.getContext().getResources(), image);
            MemoryImageCache memoryImageCache = mImageLoader.getMemoryImageCache();
            if (memoryImageCache != null) {
                memoryImageCache.put(mImageSource.getKey(), drawable);
            }
        }
        ThreadUtils.runOnMainThread(
                new SetImageAction(drawable, mImageLoader, mImageLoadCallback, this));
    }

    @NonNull
    public ImageSource<T> getImageSource() {
        return mImageSource;
    }

    @Nullable
    public ImageView getAttachedImageView() {
        ImageView imageView = mImageViewReference.get();
        LoadImageAction<?> loadImageAction = ImageLoader.getLoadImageAction(imageView);
        if (this == loadImageAction) {
            return imageView;
        }
        return null;
    }

    public boolean execute(@NonNull ExecutorService executor) {
        if (!isCancelled() && !isFinished() && mSubmitted.compareAndSet(false, true)) {
            mFuture = executor.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    loadImage();
                    mFinished = true;
                    mSubmitted.set(false);
                    return null;
                }
            });
            return true;
        } else {
            return false;
        }
    }

    public void cancel() {
        if (mFuture != null) {
            mFuture.cancel(true);
        }
        mCancelled = true;
        synchronized (mImageLoader.getPauseWorkLock()) {
            mImageLoader.getPauseWorkLock().notifyAll();
        }
    }

    public boolean reset() {
        if (isFinished() || isCancelled()) {
            mFuture = null;
            mFinished = false;
            mCancelled = false;
            mSubmitted.set(false);
            return true;
        } else {
            return false;
        }
    }

    public boolean isFinished() {
        return mFinished;
    }

    public boolean isCancelled() {
        return mCancelled;
    }
}
