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

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import java.io.InputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link BitmapDrawable} that recycles it's bitmap.
 */
public class RecyclingBitmapDrawable extends BitmapDrawable {
    private final Lock mRecycleLock = new ReentrantLock();
    private volatile int mDisplayReferencesCount;
    private volatile int mCacheReferencesCount;
    private volatile boolean mHasBeenDisplayed;

    public RecyclingBitmapDrawable(Resources resources, Bitmap bitmap) {
        super(resources, bitmap);
    }

    public RecyclingBitmapDrawable(Resources resources, String filePath) {
        super(resources, filePath);
    }

    public RecyclingBitmapDrawable(Resources resources, InputStream inputStream) {
        super(resources, inputStream);
    }

    public void setDisplayed(boolean displayed) {
        mRecycleLock.lock();
        try {
            if (displayed) {
                mDisplayReferencesCount++;
                mHasBeenDisplayed = true;
            } else {
                mCacheReferencesCount--;
            }
            checkStateAndRecycleIfNeeded();
        } finally {
            mRecycleLock.unlock();
        }
    }

    public void setCached(boolean cached) {
        mRecycleLock.lock();
        try {
            if (cached) {
                mCacheReferencesCount++;
            } else {
                mCacheReferencesCount--;
            }
            checkStateAndRecycleIfNeeded();
        } finally {
            mRecycleLock.unlock();
        }
    }

    private void checkStateAndRecycleIfNeeded() {
        Bitmap bitmap = getBitmap();
        if (bitmap != null && !bitmap.isRecycled() && mCacheReferencesCount <= 0 &&
                mDisplayReferencesCount <= 0 && mHasBeenDisplayed) {
            bitmap.recycle();
        }
    }
}
