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

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

/**
 * Circular progress bar
 */
public class CircularProgressBar extends View {
    private static final float DEFAULT_MAXIMUM = 100F;
    private static final float DEFAULT_PROGRESS = 0F;
    private static final float DEFAULT_FOREGROUND_WIDTH_DP = 3F;
    private static final float DEFAULT_BACKGROUND_WIDTH_DP = 1F;
    private static final float DEFAULT_START_ANGLE = -90F;
    private static final float DEFAULT_INDETERMINATE_MINIMUM_ANGLE = 45F;
    private static final int DEFAULT_FOREGROUND_COLOR = Color.BLUE;
    private static final int DEFAULT_BACKGROUND_COLOR = Color.BLACK;
    private static final int DEFAULT_PROGRESS_ANIMATION_DURATION = 1000;
    private static final int DEFAULT_INDETERMINATE_START_ANIMATION_DURATION = 2000;
    private static final int DEFAULT_INDETERMINATE_SWEEP_ANIMATION_DURATION = 750;
    private static final boolean DEFAULT_ANIMATE_PROGRESS = true;
    private static final boolean DEFAULT_DRAW_BACKGROUND_STROKE = true;
    private static final boolean DEFAULT_INDETERMINATE = false;
    private float mMaximum;
    private float mProgress;
    private float mStartAngle;
    private float mIndeterminateStartAngle;
    private float mIndeterminateSweepAngle;
    private float mIndeterminateMinimumAngle;
    private float mIndeterminateStartAngleOffset;
    private long mProgressAnimationDuration;
    private boolean mIndeterminate;
    private boolean mAnimateProgress;
    private boolean mDrawBackgroundStroke;
    private boolean mIndeterminateAppearingMode;
    private Paint mForegroundStrokePaint;
    private Paint mBackgroundStrokePaint;
    private RectF mDrawRect;
    private ValueAnimator mProgressAnimator;
    private ValueAnimator mIndeterminateStartAngleAnimator;
    private ValueAnimator mIndeterminateSweepAngleAnimator;

    public CircularProgressBar(Context context) {
        super(context);
        initialize(context, null, 0, 0);
    }

    public CircularProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0, 0);
    }

    public CircularProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CircularProgressBar(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initialize(@NonNull Context context, @Nullable AttributeSet attributeSet,
            @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        mDrawRect = new RectF();
        mForegroundStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mForegroundStrokePaint.setStyle(Paint.Style.STROKE);
        mBackgroundStrokePaint.setStyle(Paint.Style.STROKE);
        mProgressAnimator = new ValueAnimator();
        mIndeterminateStartAngleAnimator = new ValueAnimator();
        mIndeterminateSweepAngleAnimator = new ValueAnimator();
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        if (attributeSet == null) {
            mMaximum = DEFAULT_MAXIMUM;
            mProgress = DEFAULT_PROGRESS;
            mStartAngle = DEFAULT_START_ANGLE;
            mIndeterminateStartAngle = 0F;
            mIndeterminateSweepAngle = 0F;
            mIndeterminateMinimumAngle = DEFAULT_INDETERMINATE_MINIMUM_ANGLE;
            mProgressAnimationDuration = DEFAULT_PROGRESS_ANIMATION_DURATION;
            mIndeterminate = DEFAULT_INDETERMINATE;
            mAnimateProgress = DEFAULT_ANIMATE_PROGRESS;
            mDrawBackgroundStroke = DEFAULT_DRAW_BACKGROUND_STROKE;
            mIndeterminateAppearingMode = false;
            mForegroundStrokePaint.setColor(DEFAULT_FOREGROUND_COLOR);
            mForegroundStrokePaint
                    .setStrokeWidth(DEFAULT_FOREGROUND_WIDTH_DP * displayMetrics.density);
            mBackgroundStrokePaint.setColor(DEFAULT_BACKGROUND_COLOR);
            mBackgroundStrokePaint
                    .setStrokeWidth(DEFAULT_BACKGROUND_WIDTH_DP * displayMetrics.density);
            mIndeterminateStartAngleAnimator
                    .setDuration(DEFAULT_INDETERMINATE_START_ANIMATION_DURATION);
            mIndeterminateSweepAngleAnimator
                    .setDuration(DEFAULT_INDETERMINATE_SWEEP_ANIMATION_DURATION);
        } else {
            TypedArray attributes = null;
            try {
                attributes = context.getTheme()
                        .obtainStyledAttributes(attributeSet, R.styleable.CircularProgressBar,
                                defStyleAttr, defStyleRes);
                mMaximum = attributes
                        .getFloat(R.styleable.CircularProgressBar_maximum, DEFAULT_MAXIMUM);
                mProgress = attributes
                        .getFloat(R.styleable.CircularProgressBar_progress, DEFAULT_PROGRESS);
                mStartAngle = attributes
                        .getFloat(R.styleable.CircularProgressBar_startAngle, DEFAULT_START_ANGLE);
                mIndeterminateMinimumAngle = attributes
                        .getFloat(R.styleable.CircularProgressBar_indeterminateMinimumAngle,
                                DEFAULT_INDETERMINATE_MINIMUM_ANGLE);
                mProgressAnimationDuration = attributes
                        .getInteger(R.styleable.CircularProgressBar_progressAnimationDuration,
                                DEFAULT_PROGRESS_ANIMATION_DURATION);
                mIndeterminateStartAngleAnimator.setDuration(attributes.getInteger(
                        R.styleable.CircularProgressBar_indeterminateStartAnimationDuration,
                        DEFAULT_INDETERMINATE_START_ANIMATION_DURATION));
                mIndeterminateSweepAngleAnimator.setDuration(attributes.getInteger(
                        R.styleable.CircularProgressBar_indeterminateSweepAnimationDuration,
                        DEFAULT_INDETERMINATE_SWEEP_ANIMATION_DURATION));
                mForegroundStrokePaint.setColor(attributes
                        .getColor(R.styleable.CircularProgressBar_foregroundStrokeColor,
                                DEFAULT_FOREGROUND_COLOR));
                mBackgroundStrokePaint.setColor(attributes
                        .getColor(R.styleable.CircularProgressBar_backgroundStrokeColor,
                                DEFAULT_BACKGROUND_COLOR));
                mForegroundStrokePaint.setStrokeWidth(attributes.getDimensionPixelSize(
                        R.styleable.CircularProgressBar_foregroundStrokeWidth,
                        Math.round(DEFAULT_FOREGROUND_WIDTH_DP * displayMetrics.density)));
                mBackgroundStrokePaint.setStrokeWidth(attributes.getDimensionPixelSize(
                        R.styleable.CircularProgressBar_backgroundStrokeWidth,
                        Math.round(DEFAULT_BACKGROUND_WIDTH_DP * displayMetrics.density)));
                mAnimateProgress = attributes
                        .getBoolean(R.styleable.CircularProgressBar_animateProgress,
                                DEFAULT_ANIMATE_PROGRESS);
                mDrawBackgroundStroke = attributes
                        .getBoolean(R.styleable.CircularProgressBar_drawBackgroundStroke,
                                DEFAULT_DRAW_BACKGROUND_STROKE);
                mIndeterminate = attributes
                        .getBoolean(R.styleable.CircularProgressBar_indeterminate,
                                DEFAULT_INDETERMINATE);
            } finally {
                if (attributes != null) {
                    attributes.recycle();
                }
            }
        }

        mProgressAnimator.setInterpolator(new DecelerateInterpolator());
        mProgressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setProgressInternal((float) animation.getAnimatedValue());
            }
        });
        mIndeterminateStartAngleAnimator.setFloatValues(360F);
        mIndeterminateStartAngleAnimator.setRepeatMode(ValueAnimator.RESTART);
        mIndeterminateStartAngleAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mIndeterminateStartAngleAnimator.setInterpolator(new LinearInterpolator());
        mIndeterminateStartAngleAnimator
                .addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        mIndeterminateStartAngle = (float) animation.getAnimatedValue();
                        invalidate();
                    }
                });
        mIndeterminateSweepAngleAnimator.setFloatValues(360F - mIndeterminateMinimumAngle * 2F);
        mIndeterminateSweepAngleAnimator.setRepeatMode(ValueAnimator.RESTART);
        mIndeterminateSweepAngleAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mIndeterminateSweepAngleAnimator.setInterpolator(new DecelerateInterpolator());
        mIndeterminateSweepAngleAnimator
                .addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        mIndeterminateSweepAngle = (float) animation.getAnimatedValue();
                        invalidate();
                    }
                });
        mIndeterminateSweepAngleAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                mIndeterminateAppearingMode = !mIndeterminateAppearingMode;
                if (mIndeterminateAppearingMode) {
                    mIndeterminateStartAngleOffset =
                            (mIndeterminateStartAngleOffset + mIndeterminateMinimumAngle * 2F) %
                                    360F;
                }
            }
        });
    }

    private void invalidateDrawRect(int width, int height) {
        float size;
        if (mDrawBackgroundStroke) {
            size = Math.max(mForegroundStrokePaint.getStrokeWidth(),
                    mBackgroundStrokePaint.getStrokeWidth());
        } else {
            size = mForegroundStrokePaint.getStrokeWidth();
        }
        if (width > height) {
            float space = (width - height) / 2F;
            mDrawRect.set(space + size / 2F, size / 2F, width - space - size / 2F,
                    height - size / 2F);
        } else if (width < height) {
            float space = (height - width) / 2F;
            mDrawRect.set(size / 2F, space + size / 2F, width - size / 2F,
                    height - space - size / 2F);
        } else {
            mDrawRect.set(size / 2F, size / 2F, width - size / 2F, height - size / 2F);
        }
    }

    private void invalidateDrawRect() {
        int width = getWidth();
        int height = getHeight();
        if (width > 0 && height > 0) {
            invalidateDrawRect(width, height);
        }
    }

    private boolean isLaidOutCompat() {
        return getWidth() > 0 && getHeight() > 0;
    }

    private void stopProgressAnimation() {
        if (mProgressAnimator.isRunning()) {
            mProgressAnimator.cancel();
        }
    }

    private void setProgressInternal(float progress) {
        mProgress = progress;
        invalidate();
    }

    private void startIndeterminateAnimations() {
        if (isLaidOutCompat()) {
            ValueAnimator startAngleAnimator = mIndeterminateStartAngleAnimator;
            if (startAngleAnimator != null && !startAngleAnimator.isRunning()) {
                startAngleAnimator.start();
            }
            ValueAnimator sweepAngleAnimator = mIndeterminateSweepAngleAnimator;
            if (sweepAngleAnimator != null && !sweepAngleAnimator.isRunning()) {
                sweepAngleAnimator.start();
            }
        }
    }

    private void stopIndeterminateAnimations() {
        ValueAnimator startAngleAnimator = mIndeterminateStartAngleAnimator;
        if (startAngleAnimator != null && startAngleAnimator.isRunning()) {
            startAngleAnimator.cancel();
        }
        ValueAnimator sweepAngleAnimator = mIndeterminateSweepAngleAnimator;
        if (sweepAngleAnimator != null && sweepAngleAnimator.isRunning()) {
            sweepAngleAnimator.cancel();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int measuredHeight = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(measuredWidth, measuredHeight);
        invalidateDrawRect(measuredWidth, measuredHeight);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        invalidateDrawRect();
        if (mIndeterminate) {
            startIndeterminateAnimations();
        }
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        invalidateDrawRect(width, height);
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE) {
            if (mIndeterminate) {
                startIndeterminateAnimations();
            }
        } else {
            stopIndeterminateAnimations();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mIndeterminate && isLaidOutCompat()) {
            startIndeterminateAnimations();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopIndeterminateAnimations();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDrawBackgroundStroke) {
            canvas.drawOval(mDrawRect, mBackgroundStrokePaint);
        }
        if (mIndeterminate) {
            float startAngle;
            float sweepAngle;
            if (mIndeterminateAppearingMode) {
                startAngle = mIndeterminateStartAngle - mIndeterminateStartAngleOffset;
                sweepAngle = mIndeterminateSweepAngle + mIndeterminateMinimumAngle;
            } else {
                startAngle = mIndeterminateStartAngle + mIndeterminateSweepAngle -
                        mIndeterminateStartAngleOffset;
                sweepAngle = 360F - mIndeterminateSweepAngle - mIndeterminateMinimumAngle;
            }
            canvas.drawArc(mDrawRect, startAngle, sweepAngle, false, mForegroundStrokePaint);
        } else {
            float progress;
            if (mProgress > mMaximum) {
                progress = mMaximum;
            } else if (mProgress < -mMaximum) {
                progress = -mMaximum;
            } else {
                progress = mProgress;
            }
            float sweepAngle = 360F * progress / mMaximum;
            canvas.drawArc(mDrawRect, mStartAngle, sweepAngle, false, mForegroundStrokePaint);
        }
    }

    /**
     * Maximum progress value
     */
    public float getMaximum() {
        return mMaximum;
    }

    /**
     * Maximum progress value
     */
    public void setMaximum(float maximum) {
        stopProgressAnimation();
        mMaximum = maximum;
        invalidate();
    }

    /**
     * Current progress value
     */
    public float getProgress() {
        return mProgress;
    }

    /**
     * Current progress value
     */
    public void setProgress(float progress) {
        stopProgressAnimation();
        if (mIndeterminate) {
            mProgress = progress;
        } else {
            if (mAnimateProgress && isLaidOutCompat()) {
                mProgressAnimator.setFloatValues(mProgress, progress);
                mProgressAnimator.setDuration(mProgressAnimationDuration);
                mProgressAnimator.start();
            } else {
                setProgressInternal(progress);
            }
        }
    }

    /**
     * Foreground stroke width
     * Foreground stroke represents current progress value
     */
    public float getForegroundStrokeWidth() {
        return mForegroundStrokePaint.getStrokeWidth();
    }

    /**
     * Foreground stroke width
     * Foreground stroke represents current progress value
     */
    public void setForegroundStrokeWidth(float foregroundStrokeWidth) {
        mForegroundStrokePaint.setStrokeWidth(foregroundStrokeWidth);
        invalidateDrawRect();
        invalidate();
    }

    /**
     * Background stroke width
     */
    public float getBackgroundStrokeWidth() {
        return mBackgroundStrokePaint.getStrokeWidth();
    }

    /**
     * Background stroke width
     */
    public void setBackgroundStrokeWidth(float backgroundStrokeWidth) {
        mBackgroundStrokePaint.setStrokeWidth(backgroundStrokeWidth);
        invalidateDrawRect();
        invalidate();
    }

    /**
     * Progress bar start angle
     */
    public float getStartAngle() {
        return mStartAngle;
    }

    /**
     * Progress bar start angle
     */
    public void setStartAngle(float startAngle) {
        stopProgressAnimation();
        mStartAngle = startAngle;
        if (!mIndeterminate) {
            invalidate();
        }
    }

    /**
     * Foreground stroke color
     * Foreground stroke represents current progress value
     */
    @ColorInt
    public int getForegroundStrokeColor() {
        return mForegroundStrokePaint.getColor();
    }

    /**
     * Foreground stroke color
     * Foreground stroke represents current progress value
     */
    public void setForegroundStrokeColor(@ColorInt int foregroundStrokeColor) {
        mForegroundStrokePaint.setColor(foregroundStrokeColor);
        invalidate();
    }

    /**
     * Background stroke color
     */
    @ColorInt
    public int getBackgroundStrokeColor() {
        return mBackgroundStrokePaint.getColor();
    }

    /**
     * Background stroke color
     */
    public void setBackgroundStrokeColor(@ColorInt int backgroundStrokeColor) {
        mBackgroundStrokePaint.setColor(backgroundStrokeColor);
        invalidate();
    }

    /**
     * Progress change animation duration
     */
    public long getProgressAnimationDuration() {
        return mProgressAnimationDuration;
    }

    /**
     * Progress change animation duration
     */
    public void setProgressAnimationDuration(long progressAnimationDuration) {
        mProgressAnimationDuration = progressAnimationDuration;
    }

    /**
     * Progress animation state
     */
    public boolean isAnimating() {
        return mProgressAnimator.isRunning();
    }

    /**
     * Animate progress changes
     */
    public boolean isAnimateProgress() {
        return mAnimateProgress;
    }

    /**
     * Animate progress changes
     */
    public void setAnimateProgress(boolean animateProgress) {
        if (!animateProgress) {
            stopProgressAnimation();
        }
        mAnimateProgress = animateProgress;
    }

    /**
     * Draw background stroke
     */
    public boolean isDrawBackgroundStroke() {
        return mDrawBackgroundStroke;
    }

    /**
     * Draw background stroke
     */
    public void setDrawBackgroundStroke(boolean drawBackgroundStroke) {
        mDrawBackgroundStroke = drawBackgroundStroke;
        invalidateDrawRect();
        invalidate();
    }

    /**
     * Minimum angle of indeterminate animation
     */
    public float getIndeterminateMinimumAngle() {
        return mIndeterminateMinimumAngle;
    }

    /**
     * Minimum angle of indeterminate animation
     */
    public void setIndeterminateMinimumAngle(float indeterminateMinimumAngle) {
        stopIndeterminateAnimations();
        mIndeterminateMinimumAngle = indeterminateMinimumAngle;
        mIndeterminateSweepAngleAnimator.setFloatValues(360F - mIndeterminateMinimumAngle * 2F);
        if (mIndeterminate) {
            invalidate();
            startIndeterminateAnimations();
        }
    }

    /**
     * Duration of start angle animation in indeterminate mode
     */
    public long getIndeterminateStartAnimationDuration() {
        return mIndeterminateStartAngleAnimator.getDuration();
    }

    /**
     * Duration of start angle animation in indeterminate mode
     */
    public void setIndeterminateStartAnimationDuration(long duration) {
        stopIndeterminateAnimations();
        mIndeterminateStartAngleAnimator.setDuration(duration);
        if (mIndeterminate) {
            invalidate();
            startIndeterminateAnimations();
        }
    }

    /**
     * Duration of sweep angle animation in indeterminate mode
     */
    public long getIndeterminateSweepAnimationDuration() {
        return mIndeterminateSweepAngleAnimator.getDuration();
    }

    /**
     * Duration of sweep angle animation in indeterminate mode
     */
    public void setIndeterminateSweepAnimationDuration(long duration) {
        stopIndeterminateAnimations();
        mIndeterminateSweepAngleAnimator.setDuration(duration);
        if (mIndeterminate) {
            invalidate();
            startIndeterminateAnimations();
        }
    }

    /**
     * Indeterminate mode
     */
    public boolean isIndeterminate() {
        return mIndeterminate;
    }

    /**
     * Indeterminate mode
     */
    public void setIndeterminate(boolean indeterminate) {
        if (indeterminate != mIndeterminate) {
            mIndeterminate = indeterminate;
            if (mIndeterminate) {
                startIndeterminateAnimations();
            } else {
                stopIndeterminateAnimations();
            }
        }
    }
}
