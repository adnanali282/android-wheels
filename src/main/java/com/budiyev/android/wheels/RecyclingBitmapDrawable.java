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

/**
 * {@link BitmapDrawable} that recycles it's bitmap.
 */
public class RecyclingBitmapDrawable extends BitmapDrawable {
    private int mDisplayReferencesCount;
    private int mCacheReferencesCount;
    private boolean mHasBeenDisplayed;

    public RecyclingBitmapDrawable(Resources res, Bitmap bitmap) {
        super(res, bitmap);
    }

    public RecyclingBitmapDrawable(Resources res, String filepath) {
        super(res, filepath);
    }

    public RecyclingBitmapDrawable(Resources res, InputStream is) {
        super(res, is);
    }

    private void checkStateAndRecycleIfNeeded() {
        Bitmap bitmap = getBitmap();
        if (bitmap != null && !bitmap.isRecycled() && mCacheReferencesCount <= 0 &&
                mDisplayReferencesCount <= 0 && mHasBeenDisplayed) {
            bitmap.recycle();
        }
    }

    public synchronized void setDisplayed(boolean displayed) {
        if (displayed) {
            mDisplayReferencesCount++;
            mHasBeenDisplayed = true;
        } else {
            mCacheReferencesCount--;
        }
        checkStateAndRecycleIfNeeded();
    }

    public synchronized void setCached(boolean cached) {
        if (cached) {
            mCacheReferencesCount++;
        } else {
            mCacheReferencesCount--;
        }
        checkStateAndRecycleIfNeeded();
    }
}
