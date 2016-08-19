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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * {@link ImageView} that notifies {@link RecyclingBitmapDrawable}
 * when image drawable is changed.
 */
public class RecyclingImageView extends ImageView {
    private boolean mClearDrawableOnDetach;

    public RecyclingImageView(Context context) {
        super(context);
        initialize(context, null, 0, 0);
    }

    public RecyclingImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0, 0);
    }

    public RecyclingImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RecyclingImageView(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initialize(@NonNull Context context, @Nullable AttributeSet attributeSet,
            @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        if (attributeSet != null) {
            TypedArray attributes = null;
            try {
                attributes = context.getTheme()
                        .obtainStyledAttributes(attributeSet, R.styleable.RecyclingImageView,
                                defStyleAttr, defStyleRes);
                mClearDrawableOnDetach = attributes
                        .getBoolean(R.styleable.RecyclingImageView_clearDrawableOnDetach, false);
            } finally {
                if (attributes != null) {
                    attributes.recycle();
                }
            }
        }
    }

    private void notifyDrawable(Drawable drawable, boolean displayed) {
        if (drawable instanceof RecyclingBitmapDrawable) {
            ((RecyclingBitmapDrawable) drawable).setDisplayed(displayed);
        } else if (drawable instanceof LayerDrawable) {
            LayerDrawable layerDrawable = (LayerDrawable) drawable;
            for (int i = 0, z = layerDrawable.getNumberOfLayers(); i < z; i++) {
                notifyDrawable(layerDrawable.getDrawable(i), displayed);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mClearDrawableOnDetach) {
            setImageDrawable(null);
        }
        super.onDetachedFromWindow();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        Drawable previousDrawable = getDrawable();
        super.setImageDrawable(drawable);
        notifyDrawable(drawable, true);
        notifyDrawable(previousDrawable, false);
    }

    /**
     * Clear image drawable when detached from window. Set true if {@link RecyclingImageView}
     * is used with component that doesn't reuse views.
     *
     * @param clearDrawableOnDetach Clear drawable on detach
     */
    public void setClearDrawableOnDetach(boolean clearDrawableOnDetach) {
        mClearDrawableOnDetach = clearDrawableOnDetach;
    }
}
