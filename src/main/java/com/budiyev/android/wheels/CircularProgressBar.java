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
import android.support.annotation.Px;
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
    private static final float DEFAULT_MAXIMUM = 100f;
    private static final float DEFAULT_PROGRESS = 0f;
    private static final float DEFAULT_FOREGROUND_STROKE_WIDTH_DP = 3f;
    private static final float DEFAULT_BACKGROUND_STROKE_WIDTH_DP = 1f;
    private static final float DEFAULT_START_ANGLE = -90f;
    private static final float DEFAULT_INDETERMINATE_MINIMUM_ANGLE = 45f;
    private static final int DEFAULT_FOREGROUND_STROKE_COLOR = Color.BLUE;
    private static final int DEFAULT_BACKGROUND_STROKE_COLOR = Color.BLACK;
    private static final int DEFAULT_PROGRESS_ANIMATION_DURATION = 500;
    private static final int DEFAULT_INDETERMINATE_GROW_ANIMATION_DURATION = 2000;
    private static final int DEFAULT_INDETERMINATE_SWEEP_ANIMATION_DURATION = 1000;
    private static final boolean DEFAULT_ANIMATE_PROGRESS = true;
    private static final boolean DEFAULT_DRAW_BACKGROUND_STROKE = true;
    private static final boolean DEFAULT_INDETERMINATE = false;
    private float mMaximum;
    private float mProgress;
    private float mStartAngle;
    private float mIndeterminateGrowAngle;
    private float mIndeterminateSweepAngle;
    private float mIndeterminateMinimumAngle;
    private float mIndeterminateGrowAngleOffset;
    private boolean mIndeterminate;
    private boolean mAnimateProgress;
    private boolean mDrawBackgroundStroke;
    private boolean mIndeterminateGrowMode;
    private Paint mForegroundStrokePaint;
    private Paint mBackgroundStrokePaint;
    private RectF mDrawRect;
    private ValueAnimator mProgressAnimator;
    private ValueAnimator mIndeterminateGrowAnimator;
    private ValueAnimator mIndeterminateSweepAnimator;

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
        mIndeterminateGrowAnimator = new ValueAnimator();
        mIndeterminateSweepAnimator = new ValueAnimator();
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        if (attributeSet == null) {
            mMaximum = DEFAULT_MAXIMUM;
            mProgress = DEFAULT_PROGRESS;
            mStartAngle = DEFAULT_START_ANGLE;
            mIndeterminateGrowAngle = 0F;
            mIndeterminateSweepAngle = 0F;
            mIndeterminateMinimumAngle = DEFAULT_INDETERMINATE_MINIMUM_ANGLE;
            mProgressAnimator.setDuration(DEFAULT_PROGRESS_ANIMATION_DURATION);
            mIndeterminate = DEFAULT_INDETERMINATE;
            mAnimateProgress = DEFAULT_ANIMATE_PROGRESS;
            mDrawBackgroundStroke = DEFAULT_DRAW_BACKGROUND_STROKE;
            mForegroundStrokePaint.setColor(DEFAULT_FOREGROUND_STROKE_COLOR);
            mForegroundStrokePaint.setStrokeWidth(
                    Math.round(DEFAULT_FOREGROUND_STROKE_WIDTH_DP * displayMetrics.density));
            mBackgroundStrokePaint.setColor(DEFAULT_BACKGROUND_STROKE_COLOR);
            mBackgroundStrokePaint.setStrokeWidth(
                    Math.round(DEFAULT_BACKGROUND_STROKE_WIDTH_DP * displayMetrics.density));
            mIndeterminateGrowAnimator.setDuration(DEFAULT_INDETERMINATE_GROW_ANIMATION_DURATION);
            mIndeterminateSweepAnimator.setDuration(DEFAULT_INDETERMINATE_SWEEP_ANIMATION_DURATION);
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
                mProgressAnimator.setDuration(attributes
                        .getInteger(R.styleable.CircularProgressBar_progressAnimationDuration,
                                DEFAULT_PROGRESS_ANIMATION_DURATION));
                mIndeterminateGrowAnimator.setDuration(attributes.getInteger(
                        R.styleable.CircularProgressBar_indeterminateGrowAnimationDuration,
                        DEFAULT_INDETERMINATE_GROW_ANIMATION_DURATION));
                mIndeterminateSweepAnimator.setDuration(attributes.getInteger(
                        R.styleable.CircularProgressBar_indeterminateSweepAnimationDuration,
                        DEFAULT_INDETERMINATE_SWEEP_ANIMATION_DURATION));
                mForegroundStrokePaint.setColor(attributes
                        .getColor(R.styleable.CircularProgressBar_foregroundStrokeColor,
                                DEFAULT_FOREGROUND_STROKE_COLOR));
                mBackgroundStrokePaint.setColor(attributes
                        .getColor(R.styleable.CircularProgressBar_backgroundStrokeColor,
                                DEFAULT_BACKGROUND_STROKE_COLOR));
                mForegroundStrokePaint.setStrokeWidth(attributes.getDimensionPixelSize(
                        R.styleable.CircularProgressBar_foregroundStrokeWidth,
                        Math.round(DEFAULT_FOREGROUND_STROKE_WIDTH_DP * displayMetrics.density)));
                mBackgroundStrokePaint.setStrokeWidth(attributes.getDimensionPixelSize(
                        R.styleable.CircularProgressBar_backgroundStrokeWidth,
                        Math.round(DEFAULT_BACKGROUND_STROKE_WIDTH_DP * displayMetrics.density)));
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
        mIndeterminateGrowAnimator.setFloatValues(360f);
        mIndeterminateGrowAnimator.setRepeatMode(ValueAnimator.RESTART);
        mIndeterminateGrowAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mIndeterminateGrowAnimator.setInterpolator(new LinearInterpolator());
        mIndeterminateGrowAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mIndeterminateGrowAngle = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        mIndeterminateSweepAnimator.setFloatValues(360f - mIndeterminateMinimumAngle * 2f);
        mIndeterminateSweepAnimator.setRepeatMode(ValueAnimator.RESTART);
        mIndeterminateSweepAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mIndeterminateSweepAnimator.setInterpolator(new DecelerateInterpolator());
        mIndeterminateSweepAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mIndeterminateSweepAngle = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        mIndeterminateSweepAnimator.addListener(new Animator.AnimatorListener() {
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
                mIndeterminateGrowMode = !mIndeterminateGrowMode;
                if (mIndeterminateGrowMode) {
                    mIndeterminateGrowAngleOffset =
                            (mIndeterminateGrowAngleOffset + mIndeterminateMinimumAngle * 2f) %
                                    360f;
                }
            }
        });
    }

    private void invalidateDrawRect(int width, int height) {
        int size;
        if (mDrawBackgroundStroke) {
            size = Math.round(Math.max(mForegroundStrokePaint.getStrokeWidth(),
                    mBackgroundStrokePaint.getStrokeWidth()));
        } else {
            size = Math.round(mForegroundStrokePaint.getStrokeWidth());
        }
        if (width > height) {
            int space = (width - height) / 2;
            mDrawRect.set(space + size / 2 + 1, size / 2 + 1, width - space - size / 2 - 1,
                    height - size / 2 - 1);
        } else if (width < height) {
            int space = (height - width) / 2;
            mDrawRect.set(size / 2 + 1, space + size / 2 + 1, width - size / 2 - 1,
                    height - space - size / 2 - 1);
        } else {
            mDrawRect.set(size / 2 + 1, size / 2 + 1, width - size / 2 - 1, height - size / 2 - 1);
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

    private void setProgressInternal(float progress) {
        mProgress = progress;
        invalidate();
    }

    private void setProgressAnimated(float progress) {
        ValueAnimator progressAnimator = mProgressAnimator;
        if (progressAnimator == null) {
            setProgressInternal(progress);
        } else {
            progressAnimator.setFloatValues(mProgress, progress);
            progressAnimator.start();
        }
    }

    private void stopProgressAnimation() {
        ValueAnimator progressAnimator = mProgressAnimator;
        if (progressAnimator != null && progressAnimator.isRunning()) {
            progressAnimator.cancel();
        }
    }

    private void stopIndeterminateAnimations() {
        ValueAnimator growAnimator = mIndeterminateGrowAnimator;
        if (growAnimator != null && growAnimator.isRunning()) {
            growAnimator.cancel();
        }
        ValueAnimator sweepAnimator = mIndeterminateSweepAnimator;
        if (sweepAnimator != null && sweepAnimator.isRunning()) {
            sweepAnimator.cancel();
        }
    }

    private void startIndeterminateAnimations() {
        if (isLaidOutCompat()) {
            ValueAnimator growAnimator = mIndeterminateGrowAnimator;
            if (growAnimator != null && !growAnimator.isRunning()) {
                growAnimator.start();
            }
            ValueAnimator sweepAnimator = mIndeterminateSweepAnimator;
            if (sweepAnimator != null && !sweepAnimator.isRunning()) {
                sweepAnimator.start();
            }
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
            if (mIndeterminateGrowMode) {
                startAngle = mIndeterminateGrowAngle - mIndeterminateGrowAngleOffset;
                sweepAngle = mIndeterminateSweepAngle + mIndeterminateMinimumAngle;
            } else {
                startAngle = mIndeterminateGrowAngle + mIndeterminateSweepAngle -
                        mIndeterminateGrowAngleOffset;
                sweepAngle = 360f - mIndeterminateSweepAngle - mIndeterminateMinimumAngle;
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
            float sweepAngle = 360f * progress / mMaximum;
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
                setProgressAnimated(progress);
            } else {
                setProgressInternal(progress);
            }
        }
    }

    /**
     * Foreground stroke width
     * <br>
     * Foreground stroke represents current progress value
     */
    @Px
    public int getForegroundStrokeWidth() {
        return (int) mForegroundStrokePaint.getStrokeWidth();
    }

    /**
     * Foreground stroke width
     * <br>
     * Foreground stroke represents current progress value
     */
    public void setForegroundStrokeWidth(@Px int width) {
        mForegroundStrokePaint.setStrokeWidth(width);
        invalidateDrawRect();
        invalidate();
    }

    /**
     * Background stroke width
     */
    @Px
    public int getBackgroundStrokeWidth() {
        return (int) mBackgroundStrokePaint.getStrokeWidth();
    }

    /**
     * Background stroke width
     */
    public void setBackgroundStrokeWidth(@Px int width) {
        mBackgroundStrokePaint.setStrokeWidth(width);
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
    public void setStartAngle(float angle) {
        stopProgressAnimation();
        mStartAngle = angle;
        if (!mIndeterminate) {
            invalidate();
        }
    }

    /**
     * Foreground stroke color
     * <br>
     * Foreground stroke represents current progress value
     */
    @ColorInt
    public int getForegroundStrokeColor() {
        return mForegroundStrokePaint.getColor();
    }

    /**
     * Foreground stroke color
     * <br>
     * Foreground stroke represents current progress value
     */
    public void setForegroundStrokeColor(@ColorInt int color) {
        mForegroundStrokePaint.setColor(color);
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
    public void setBackgroundStrokeColor(@ColorInt int color) {
        mBackgroundStrokePaint.setColor(color);
        invalidate();
    }

    /**
     * Progress change animation duration
     */
    public long getProgressAnimationDuration() {
        return mProgressAnimator.getDuration();
    }

    /**
     * Progress change animation duration
     */
    public void setProgressAnimationDuration(long duration) {
        mProgressAnimator.setDuration(duration);
    }

    /**
     * Animation state
     */
    public boolean isAnimating() {
        return mProgressAnimator.isRunning() || mIndeterminateGrowAnimator.isRunning() ||
                mIndeterminateSweepAnimator.isRunning();
    }

    /**
     * Stop all animations
     */
    public void stopAnimations() {
        stopProgressAnimation();
        stopIndeterminateAnimations();
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
    public void setAnimateProgress(boolean animate) {
        if (!animate) {
            stopProgressAnimation();
        }
        mAnimateProgress = animate;
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
    public void setDrawBackgroundStroke(boolean draw) {
        mDrawBackgroundStroke = draw;
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
    public void setIndeterminateMinimumAngle(float angle) {
        stopIndeterminateAnimations();
        mIndeterminateMinimumAngle = angle;
        mIndeterminateSweepAnimator.setFloatValues(360F - mIndeterminateMinimumAngle * 2F);
        if (mIndeterminate) {
            invalidate();
            startIndeterminateAnimations();
        }
    }

    /**
     * Duration of grow animation in indeterminate mode
     */
    public long getIndeterminateGrowAnimationDuration() {
        return mIndeterminateGrowAnimator.getDuration();
    }

    /**
     * Duration of grow animation in indeterminate mode
     */
    public void setIndeterminateGrowAnimationDuration(long duration) {
        stopIndeterminateAnimations();
        mIndeterminateGrowAnimator.setDuration(duration);
        if (mIndeterminate) {
            invalidate();
            startIndeterminateAnimations();
        }
    }

    /**
     * Duration of sweep animation in indeterminate mode
     */
    public long getIndeterminateSweepAnimationDuration() {
        return mIndeterminateSweepAnimator.getDuration();
    }

    /**
     * Duration of sweep animation in indeterminate mode
     */
    public void setIndeterminateSweepAnimationDuration(long duration) {
        stopIndeterminateAnimations();
        mIndeterminateSweepAnimator.setDuration(duration);
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
