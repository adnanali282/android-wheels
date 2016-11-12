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

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;

/**
 * Drawable with fade effect
 */
public class FadeDrawable extends LayerDrawable implements Drawable.Callback {
    private static final int FADE_NONE = 0;
    private static final int FADE_RUNNING = 1;
    private static final int FADE_DONE = 2;
    private static final int START_DRAWABLE = 0;
    private static final int END_DRAWABLE = 1;
    private static final int MAX_ALPHA = 255;
    private int mFadeState = FADE_NONE;
    private long mStartTime;
    private long mDuration;
    private FadeCallback mFadeCallback;

    /**
     * Fade drawable
     *
     * @param startDrawable start drawable
     * @param endDrawable   end drawable
     */
    public FadeDrawable(@Nullable Drawable startDrawable, @Nullable Drawable endDrawable) {
        super(new Drawable[]{startDrawable, endDrawable});
        setId(START_DRAWABLE, START_DRAWABLE);
        setId(END_DRAWABLE, END_DRAWABLE);
    }

    private void draw(Canvas canvas, Drawable drawable, int alpha) {
        int originalAlpha = drawable.getAlpha();
        drawable.setAlpha(alpha);
        drawable.draw(canvas);
        drawable.setAlpha(originalAlpha);
    }

    @Override
    public void draw(Canvas canvas) {
        if (mFadeState == FADE_RUNNING) {
            int alpha = Math.min(Math.round(
                    MAX_ALPHA * (float) (System.currentTimeMillis() - mStartTime) /
                            (float) mDuration), MAX_ALPHA);
            Drawable startDrawable = getStartDrawable();
            if (startDrawable != null) {
                draw(canvas, startDrawable, MAX_ALPHA - alpha);
            }
            Drawable endDrawable = getEndDrawable();
            if (endDrawable != null) {
                draw(canvas, endDrawable, alpha);
            }
            if (alpha == MAX_ALPHA) {
                mFadeState = FADE_DONE;
                ThreadUtils.runOnMainThread(new Runnable() {
                    @Override
                    @MainThread
                    public void run() {
                        FadeCallback fadeCallback = mFadeCallback;
                        if (fadeCallback != null) {
                            fadeCallback.onEnd(FadeDrawable.this);
                        }
                    }
                }, 0);
            } else {
                invalidateSelf();
            }
        } else if (mFadeState == FADE_NONE) {
            Drawable startDrawable = getStartDrawable();
            if (startDrawable != null) {
                draw(canvas, startDrawable, MAX_ALPHA);
            }
        } else if (mFadeState == FADE_DONE) {
            Drawable endDrawable = getEndDrawable();
            if (endDrawable != null) {
                draw(canvas, endDrawable, MAX_ALPHA);
            }
        }
    }

    /**
     * Start fade
     *
     * @param duration Fade duration
     */
    public void startFade(int duration) {
        mDuration = duration;
        mStartTime = System.currentTimeMillis();
        mFadeState = FADE_RUNNING;
        invalidateSelf();
        ThreadUtils.runOnMainThread(new Runnable() {
            @Override
            @MainThread
            public void run() {
                FadeCallback fadeCallback = mFadeCallback;
                if (fadeCallback != null) {
                    fadeCallback.onStart(FadeDrawable.this);
                }
            }
        }, 0);
    }

    /**
     * Reset fade to initial state
     */
    public void resetFade() {
        mFadeState = FADE_NONE;
        invalidateSelf();
    }

    @Nullable
    public Drawable getStartDrawable() {
        return getDrawable(START_DRAWABLE);
    }

    public void setStartDrawable(@Nullable Drawable startDrawable) {
        setDrawableByLayerId(START_DRAWABLE, startDrawable);
    }

    @Nullable
    public Drawable getEndDrawable() {
        return getDrawable(END_DRAWABLE);
    }

    public void setEndDrawable(@Nullable Drawable endDrawable) {
        setDrawableByLayerId(END_DRAWABLE, endDrawable);
    }

    @Nullable
    public FadeCallback getFadeCallback() {
        return mFadeCallback;
    }

    public void setFadeCallback(@Nullable FadeCallback fadeCallback) {
        mFadeCallback = fadeCallback;
    }
}
