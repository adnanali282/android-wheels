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

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;

public final class Size {
    private final int mWidth;
    private final int mHeight;

    public Size(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    /**
     * Copy values from {@link android.util.Size}
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Size(@NonNull android.util.Size size) {
        mWidth = size.getWidth();
        mHeight = size.getHeight();
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    /**
     * Convert to {@link android.util.Size}
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @NonNull
    public android.util.Size toAndroidSize() {
        return new android.util.Size(mWidth, mHeight);
    }
}
