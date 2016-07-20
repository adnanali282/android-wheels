/**
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2016 Yuriy Budiyev [yuriy.budiyev@yandex.ru]
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p/>
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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.NonNull;
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
    private static final long DEFAULT_ANIMATION_DURATION = 1000L;
    private static final long DEFAULT_INDETERMINATE_START_ANGLE_ANIMATION_DURATION = 2000L;
    private static final long DEFAULT_INDETERMINATE_SWEEP_ANGLE_ANIMATION_DURATION = 750L;
    private static final boolean DEFAULT_ANIMATE_PROGRESS = true;
    private static final boolean DEFAULT_DRAW_BACKGROUND_STROKE = true;
    private static final boolean DEFAULT_INDETERMINATE = false;
    private float mMaximum;
    private float mProgress;
    private float mForegroundStrokeWidth;
    private float mBackgroundStrokeWidth;
    private float mStartAngle;
    private float mIndeterminateStartAngle;
    private float mIndeterminateSweepAngle;
    private float mIndeterminateMinimumAngle;
    private float mIndeterminateStartAngleOffset;
    private int mForegroundStrokeColor;
    private int mBackgroundStrokeColor;
    private long mAnimationDuration;
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
        initialize(context);
    }

    public CircularProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public CircularProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CircularProgressBar(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize(context);
    }

    private void initialize(Context context) {
        mMaximum = DEFAULT_MAXIMUM;
        mProgress = DEFAULT_PROGRESS;
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        mForegroundStrokeWidth = DEFAULT_FOREGROUND_WIDTH_DP * displayMetrics.density;
        mBackgroundStrokeWidth = DEFAULT_BACKGROUND_WIDTH_DP * displayMetrics.density;
        mStartAngle = DEFAULT_START_ANGLE;
        mIndeterminateStartAngle = 0F;
        mIndeterminateSweepAngle = 0F;
        mIndeterminateMinimumAngle = DEFAULT_INDETERMINATE_MINIMUM_ANGLE;
        mForegroundStrokeColor = DEFAULT_FOREGROUND_COLOR;
        mBackgroundStrokeColor = DEFAULT_BACKGROUND_COLOR;
        mAnimationDuration = DEFAULT_ANIMATION_DURATION;
        mIndeterminate = DEFAULT_INDETERMINATE;
        mAnimateProgress = DEFAULT_ANIMATE_PROGRESS;
        mDrawBackgroundStroke = DEFAULT_DRAW_BACKGROUND_STROKE;
        mIndeterminateAppearingMode = false;
        mForegroundStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mForegroundStrokePaint.setColor(mForegroundStrokeColor);
        mForegroundStrokePaint.setStyle(Paint.Style.STROKE);
        mForegroundStrokePaint.setStrokeWidth(mForegroundStrokeWidth);
        mBackgroundStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundStrokePaint.setColor(mBackgroundStrokeColor);
        mBackgroundStrokePaint.setStyle(Paint.Style.STROKE);
        mBackgroundStrokePaint.setStrokeWidth(mBackgroundStrokeWidth);
        mDrawRect = new RectF();
        mProgressAnimator = new ValueAnimator();
        mProgressAnimator.setInterpolator(new DecelerateInterpolator());
        mProgressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setProgressInternal((float) animation.getAnimatedValue());
            }
        });
        mIndeterminateStartAngleAnimator = new ValueAnimator();
        mIndeterminateStartAngleAnimator.setFloatValues(360F);
        mIndeterminateStartAngleAnimator
                .setDuration(DEFAULT_INDETERMINATE_START_ANGLE_ANIMATION_DURATION);
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
        mIndeterminateSweepAngleAnimator = new ValueAnimator();
        mIndeterminateSweepAngleAnimator.setFloatValues(360F - mIndeterminateMinimumAngle * 2F);
        mIndeterminateSweepAngleAnimator
                .setDuration(DEFAULT_INDETERMINATE_SWEEP_ANGLE_ANIMATION_DURATION);
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
            size = Math.max(mForegroundStrokeWidth, mBackgroundStrokeWidth);
        } else {
            size = mForegroundStrokeWidth;
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

    public float getMaximum() {
        return mMaximum;
    }

    public void setMaximum(float maximum) {
        stopProgressAnimation();
        mMaximum = maximum;
        invalidate();
    }

    public float getProgress() {
        return mProgress;
    }

    public void setProgress(float progress) {
        stopProgressAnimation();
        if (mIndeterminate) {
            mProgress = progress;
        } else {
            if (mAnimateProgress && isLaidOutCompat()) {
                mProgressAnimator.setFloatValues(mProgress, progress);
                mProgressAnimator.setDuration(mAnimationDuration);
                mProgressAnimator.start();
            } else {
                setProgressInternal(progress);
            }
        }
    }

    public float getForegroundStrokeWidth() {
        return mForegroundStrokeWidth;
    }

    public void setForegroundStrokeWidth(float foregroundStrokeWidth) {
        mForegroundStrokeWidth = foregroundStrokeWidth;
        mForegroundStrokePaint.setStrokeWidth(mForegroundStrokeWidth);
        invalidateDrawRect();
        invalidate();
    }

    public float getBackgroundStrokeWidth() {
        return mBackgroundStrokeWidth;
    }

    public void setBackgroundStrokeWidth(float backgroundStrokeWidth) {
        mBackgroundStrokeWidth = backgroundStrokeWidth;
        mBackgroundStrokePaint.setStrokeWidth(mBackgroundStrokeWidth);
        invalidateDrawRect();
        invalidate();
    }

    public float getStartAngle() {
        return mStartAngle;
    }

    public void setStartAngle(float startAngle) {
        stopProgressAnimation();
        mStartAngle = startAngle;
        if (!mIndeterminate) {
            invalidate();
        }
    }

    public int getForegroundStrokeColor() {
        return mForegroundStrokeColor;
    }

    public void setForegroundStrokeColor(int foregroundStrokeColor) {
        mForegroundStrokeColor = foregroundStrokeColor;
        mForegroundStrokePaint.setColor(mForegroundStrokeColor);
        invalidate();
    }

    public int getBackgroundStrokeColor() {
        return mBackgroundStrokeColor;
    }

    public void setBackgroundStrokeColor(int backgroundStrokeColor) {
        mBackgroundStrokeColor = backgroundStrokeColor;
        mBackgroundStrokePaint.setColor(mBackgroundStrokeColor);
        invalidate();
    }

    public long getAnimationDuration() {
        return mAnimationDuration;
    }

    public void setAnimationDuration(long animationDuration) {
        mAnimationDuration = animationDuration;
    }

    public boolean isAnimating() {
        return mProgressAnimator.isRunning();
    }

    public boolean isAnimateProgress() {
        return mAnimateProgress;
    }

    public void setAnimateProgress(boolean animateProgress) {
        if (!animateProgress) {
            stopProgressAnimation();
        }
        mAnimateProgress = animateProgress;
    }

    public boolean isDrawBackgroundStroke() {
        return mDrawBackgroundStroke;
    }

    public void setDrawBackgroundStroke(boolean drawBackgroundStroke) {
        mDrawBackgroundStroke = drawBackgroundStroke;
        invalidateDrawRect();
        invalidate();
    }

    public float getIndeterminateMinimumAngle() {
        return mIndeterminateMinimumAngle;
    }

    public void setIndeterminateMinimumAngle(float indeterminateMinimumAngle) {
        stopIndeterminateAnimations();
        mIndeterminateMinimumAngle = indeterminateMinimumAngle;
        mIndeterminateSweepAngleAnimator.setFloatValues(360F - mIndeterminateMinimumAngle * 2F);
        if (mIndeterminate) {
            invalidate();
            startIndeterminateAnimations();
        }
    }

    public long getIndeterminateStartAngleAnimationDuration() {
        return mIndeterminateStartAngleAnimator.getDuration();
    }

    public void setIndeterminateStartAngleAnimationDuration(
            long indeterminateStartAngleAnimationDuration) {
        stopIndeterminateAnimations();
        mIndeterminateStartAngleAnimator.setDuration(indeterminateStartAngleAnimationDuration);
        if (mIndeterminate) {
            invalidate();
            startIndeterminateAnimations();
        }
    }

    public long getIndeterminateSweepAngleAnimationDuration() {
        return mIndeterminateSweepAngleAnimator.getDuration();
    }

    public void setIndeterminateSweepAngleAnimationDuration(
            long indeterminateSweepAngleAnimationDuration) {
        stopIndeterminateAnimations();
        mIndeterminateSweepAngleAnimator.setDuration(indeterminateSweepAngleAnimationDuration);
        if (mIndeterminate) {
            invalidate();
            startIndeterminateAnimations();
        }
    }

    public boolean isIndeterminate() {
        return mIndeterminate;
    }

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
