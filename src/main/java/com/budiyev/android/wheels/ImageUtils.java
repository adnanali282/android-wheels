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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;

/**
 * Tools for images
 */
public final class ImageUtils {
    private ImageUtils() {
    }

    /**
     * Crop image to specified size
     *
     * @param image  Source image
     * @param width  Result width
     * @param height Result height
     * @return Cropped (and resized) image
     */
    @NonNull
    public static Bitmap crop(@NonNull Bitmap image, int width, int height) {
        int w = width;
        int h = height;
        while (w > 0 && h > 0) {
            if (w > h) {
                w %= h;
            } else {
                h %= w;
            }
        }
        int d = w + h;
        int arW = width / d;
        int arH = height / d;
        final int CROP_MODE_NONE = 1;
        final int CROP_MODE_WIDTH = 2;
        final int CROP_MODE_HEIGHT = 3;
        int cropMode = CROP_MODE_HEIGHT;
        int resultH = image.getHeight();
        int resultW = arW * resultH / arH;
        if (resultW > image.getWidth()) {
            cropMode = CROP_MODE_WIDTH;
            resultW = image.getWidth();
            resultH = arH * resultW / arW;
        }
        if (resultH == image.getHeight() && resultW == image.getWidth()) {
            cropMode = CROP_MODE_NONE;
        }
        Bitmap cropped = null;
        switch (cropMode) {
            case CROP_MODE_NONE:
                cropped = image;
                break;
            case CROP_MODE_HEIGHT:
                cropped = Bitmap.createBitmap(image, (image.getWidth() - resultW) / 2, 0, resultW,
                        resultH);
                break;
            case CROP_MODE_WIDTH:
                cropped = Bitmap.createBitmap(image, 0, (image.getHeight() - resultH) / 2, resultW,
                        resultH);
                break;
        }
        return Bitmap.createScaledBitmap(cropped, width, height, true);
    }

    /**
     * Round image corners with specified radius
     *
     * @param image  Source image
     * @param radius Corner radius
     * @return Image with rounded corners
     */
    @NonNull
    public static Bitmap roundCorners(@NonNull Bitmap image, float radius) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(0xff424242);
        Rect rect = new Rect(0, 0, image.getWidth(), image.getHeight());
        RectF rectF = new RectF(rect);
        Bitmap bitmap =
                Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawRoundRect(rectF, radius, radius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(image, rect, rect, paint);
        return bitmap;
    }

    /**
     * Rotate image by specified amount of degrees
     *
     * @param image   Source image
     * @param degrees Amount of degrees
     * @return Rotated image
     */
    @NonNull
    public static Bitmap rotate(@NonNull Bitmap image, float degrees) {
        Matrix matrix = new Matrix();
        matrix.setRotate(degrees);
        return Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
    }

    @NonNull
    public static Bitmap mirrorHorizontally(@NonNull Bitmap image) {
        Matrix matrix = new Matrix();
        matrix.setScale(-1, 1);
        return Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
    }

    @NonNull
    public static Bitmap mirrorVertically(@NonNull Bitmap image) {
        Matrix matrix = new Matrix();
        matrix.setScale(1, -1);
        return Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
    }

    @NonNull
    private Bitmap invertColors(@NonNull Bitmap image) {
        Bitmap bitmap =
                Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(
                new float[]{-1, 0, 0, 0, 255, 0, -1, 0, 0, 255, 0, 0, -1, 0, 255, 0, 0, 0, 1, 0}));
        canvas.drawBitmap(image, 0, 0, paint);
        return bitmap;
    }
}
