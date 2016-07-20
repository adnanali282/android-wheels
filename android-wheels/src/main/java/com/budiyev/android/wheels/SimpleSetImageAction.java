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
import android.support.annotation.Nullable;
import android.widget.ImageView;

/**
 * Simple set image action for {@link ImageLoader}
 */
class SimpleSetImageAction implements Runnable {
    private final ImageView mImageView;
    private final BitmapDrawable mBitmapDrawable;
    private final ImageLoadCallback mImageLoadCallback;

    public SimpleSetImageAction(@Nullable ImageView imageView,
            @Nullable BitmapDrawable bitmapDrawable,
            @Nullable ImageLoadCallback imageLoadCallback) {
        mImageView = imageView;
        mBitmapDrawable = bitmapDrawable;
        mImageLoadCallback = imageLoadCallback;
    }

    @Override
    public void run() {
        if (mBitmapDrawable == null || mImageView == null) {
            return;
        }
        mImageView.setImageDrawable(mBitmapDrawable);
        if (mImageLoadCallback != null) {
            mImageLoadCallback.onImageDisplayed(mImageView);
        }
    }
}
