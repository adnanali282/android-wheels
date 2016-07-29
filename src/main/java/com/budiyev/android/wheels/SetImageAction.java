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

import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ImageView;

/**
 * Set image action for {@link ImageLoader}
 */
final class SetImageAction implements Runnable {
    private final BitmapDrawable mBitmapDrawable;
    private final ImageLoader<?> mImageLoader;
    private final LoadImageAction<?> mLoadImageAction;
    private final ImageLoadCallback mImageLoadCallback;

    public SetImageAction(@Nullable BitmapDrawable bitmapDrawable,
            @NonNull ImageLoader<?> imageLoader, @Nullable ImageLoadCallback imageLoadCallback,
            @NonNull LoadImageAction<?> loadImageAction) {
        mBitmapDrawable = bitmapDrawable;
        mImageLoader = imageLoader;
        mImageLoadCallback = imageLoadCallback;
        mLoadImageAction = loadImageAction;
    }

    @Override
    public void run() {
        if (!mLoadImageAction.isCancelled() && !mImageLoader.isExitTasksEarly()) {
            final ImageView imageView = mLoadImageAction.getAttachedImageView();
            if (mBitmapDrawable == null || imageView == null) {
                return;
            }
            if (mImageLoader.isImageFadeIn()) {
                FadeDrawable fadeDrawable =
                        new FadeDrawable(new ColorDrawable(Color.TRANSPARENT), mBitmapDrawable);
                imageView.setBackground(new BitmapDrawable(mImageLoader.getContext().getResources(),
                        mImageLoader.getPlaceholderImage()));
                imageView.setImageDrawable(fadeDrawable);
                fadeDrawable.setFadeCallback(new FadeCallback() {
                    @Override
                    public void onStart(@NonNull FadeDrawable drawable) {
                    }

                    @Override
                    public void onEnd(@NonNull FadeDrawable drawable) {
                        if (mImageLoadCallback != null) {
                            mImageLoadCallback.onImageDisplayed(imageView);
                        }
                    }
                });
                fadeDrawable.startFade(mImageLoader.getImageFadeInTime());
            } else {
                imageView.setImageDrawable(mBitmapDrawable);
                if (mImageLoadCallback != null) {
                    mImageLoadCallback.onImageDisplayed(imageView);
                }
            }
        }
    }
}
