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

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewManager;
import android.view.ViewParent;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

public final class CommonUtils {
    private static final String URI_SCHEME_HTTP = "http";
    private static final String URI_SCHEME_HTTPS = "https";
    private static final String URI_SCHEME_FTP = "ftp";

    private CommonUtils() {
    }

    /**
     * Remove parent of specified {@link View}
     */
    public static void removeViewParent(@NonNull View view) {
        ViewParent parent = view.getParent();
        if (parent instanceof ViewManager) {
            ((ViewManager) parent).removeView(view);
        }
    }

    /**
     * Get size of specified {@link View}
     *
     * @return Real size if view has already been displayed, measured size otherwise
     */
    @NonNull
    public static SizeCompat getViewSize(@NonNull View view) {
        int width = view.getWidth();
        int height = view.getHeight();
        if (width == 0 || height == 0) {
            DisplayMetrics metrics = view.getResources().getDisplayMetrics();
            int widthSpec =
                    View.MeasureSpec.makeMeasureSpec(metrics.widthPixels, View.MeasureSpec.EXACTLY);
            int heightSpec = View.MeasureSpec
                    .makeMeasureSpec(metrics.heightPixels, View.MeasureSpec.EXACTLY);
            view.measure(widthSpec, heightSpec);
            width = view.getMeasuredWidth();
            height = view.getMeasuredHeight();
        }
        return new SizeCompat(width, height);
    }

    /**
     * Get {@link Bitmap} representation of specified {@link View}
     *
     * @param view View
     * @return Bitmap
     */
    @NonNull
    private Bitmap getViewBitmap(@NonNull View view) {
        int width = view.getWidth();
        int height = view.getHeight();
        if (width == 0 || height == 0) {
            SizeCompat viewSize = getViewSize(view);
            width = viewSize.getWidth();
            height = viewSize.getHeight();
            view.layout(0, 0, width, height);
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    /**
     * Convert DP to PX
     *
     * @param displayMetrics Display metrics
     * @param dp             Value in DP
     * @return Value in PX
     */
    public static int dpToPx(@NonNull DisplayMetrics displayMetrics, float dp) {
        return Math.round(dp * displayMetrics.density);
    }

    /**
     * Convert DP to PX
     *
     * @param context Context
     * @param dp      Value in DP
     * @return Value in PX
     */
    public static int dpToPx(@NonNull Context context, float dp) {
        return dpToPx(context.getResources().getDisplayMetrics(), dp);
    }

    /**
     * Check if specified text is {@code null} or empty
     */
    public static boolean isNullOrEmpty(@Nullable CharSequence text) {
        return text == null || text.length() == 0;
    }

    /**
     * Check if specified {@link String} is {@code null} or empty or white space
     */
    public static boolean isNullOrWhiteSpace(@Nullable String string) {
        return string == null || string.trim().length() == 0;
    }

    /**
     * Get text resource value by name of identifier
     *
     * @param context      Context
     * @param resourceName Name of resource identifier
     * @return Text
     */
    @Nullable
    public static CharSequence getTextByResourceName(@NonNull Context context,
            @NonNull String resourceName) {
        Resources resources = context.getResources();
        int resId = resources.getIdentifier(resourceName, "string", context.getPackageName());
        try {
            return resources.getText(resId);
        } catch (Resources.NotFoundException e) {
            return null;
        }
    }

    /**
     * Hide software input in window associated with specified {@link View}
     *
     * @param context Context
     * @param view    View
     */
    public static void hideSoftwareInput(@NonNull Context context, @NonNull View view) {
        InputMethodManager manager =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(view.getWindowToken(), 0);

    }

    /**
     * Hide software input in window associated with specified {@link View}
     *
     * @param view View
     */
    public static void hideSoftwareInput(@NonNull View view) {
        hideSoftwareInput(view.getContext(), view);
    }

    /**
     * Hide software input in window associated with specified {@link Activity}
     *
     * @param activity Activity
     */
    public static void hideSoftwareInput(@NonNull Context context, @NonNull Activity activity) {
        Window window = activity.getWindow();
        if (window == null) {
            return;
        }
        View view = window.getDecorView();
        if (view == null) {
            return;
        }
        hideSoftwareInput(context, view);
    }

    /**
     * Hide software input in window associated with specified {@link Activity}
     *
     * @param activity Activity
     */
    public static void hideSoftwareInput(@NonNull Activity activity) {
        hideSoftwareInput(activity, activity);
    }

    /**
     * Get input stream from uri
     *
     * @param context Context
     * @param uri     Uri
     * @return Data stream
     * @throws IOException
     */
    @Nullable
    public static InputStream getDataStreamFromUri(@NonNull Context context,
            @NonNull Uri uri) throws IOException {
        String scheme = uri.getScheme();
        if (URI_SCHEME_HTTP.equalsIgnoreCase(scheme) ||
                URI_SCHEME_HTTPS.equalsIgnoreCase(scheme) ||
                URI_SCHEME_FTP.equalsIgnoreCase(scheme)) {
            return new URL(uri.toString()).openConnection().getInputStream();
        } else {
            return context.getContentResolver().openInputStream(uri);
        }
    }

    /**
     * Get number of available bytes by specified path
     *
     * @param path Path
     * @return Number of free bytes
     */
    public static long getAvailableBytes(@NonNull File path) {
        return new StatFs(path.getAbsolutePath()).getAvailableBytes();
    }

    /**
     * Get total number of bytes by specified path
     *
     * @param path Path
     * @return Total number of bytes
     */
    public static long getTotalBytes(@NonNull File path) {
        return new StatFs(path.getAbsolutePath()).getTotalBytes();
    }

    /**
     * Delete all files and directories at specified path
     *
     * @param path Path
     * @return {@code true} if all files deleted successfully, {@code false} otherwise
     */
    public static boolean deletePath(@NonNull File path) {
        if (!path.exists()) {
            return false;
        }
        boolean result = true;
        Queue<File> queue = new LinkedList<>();
        queue.add(path);
        while (!queue.isEmpty()) {
            File current = queue.remove();
            if (!current.exists()) {
                continue;
            }
            if (current.isDirectory()) {
                File[] content = current.listFiles();
                if (CollectionUtils.isNullOrEmpty(content)) {
                    result &= current.delete();
                } else {
                    Collections.addAll(queue, content);
                    queue.add(current);
                }
            } else {
                result &= current.delete();
            }
        }
        return result;
    }

    //region Deprecated

    /**
     * Generate MD5 hash string for specified {@link String}
     *
     * @param string Source string
     * @return MD5 hash string
     * @see HashUtils#generateMD5(String)
     * @deprecated
     */
    @NonNull
    @Deprecated
    public static String generateMD5(@NonNull String string) {
        return HashUtils
                .generateHash(string.getBytes(), HashUtils.ALGORITHM_MD5, Character.MAX_RADIX);
    }

    /**
     * Generate SHA-512 hash string for specified {@link String}
     *
     * @param string Source string
     * @return SHA-512 hash string
     * @see HashUtils#generateSHA512(String)
     * @deprecated
     */
    @NonNull
    @Deprecated
    public static String generateSHA512(@NonNull String string) {
        return HashUtils
                .generateHash(string.getBytes(), HashUtils.ALGORITHM_SHA512, Character.MAX_RADIX);
    }

    //endregion
}
