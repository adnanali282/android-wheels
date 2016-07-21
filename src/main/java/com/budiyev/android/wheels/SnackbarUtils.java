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

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import java.util.LinkedList;

/**
 * Tools for Snackbar
 */
public final class SnackbarUtils {
    private SnackbarUtils() {
    }

    /**
     * Find suitable view for Snackbar
     *
     * @param view View
     * @return Best suitable view
     */
    @NonNull
    private static View findSuitableView(@NonNull View view) {
        if (view instanceof CoordinatorLayout) {
            return view;
        }
        ViewGroup viewGroup;
        if (view instanceof ViewGroup) {
            viewGroup = (ViewGroup) view;
        } else {
            return view;
        }
        LinkedList<View> queue = new LinkedList<>();
        queue.add(viewGroup);
        while (!queue.isEmpty()) {
            View currentView = queue.remove();
            if (currentView instanceof CoordinatorLayout) {
                return currentView;
            }
            if (currentView instanceof ViewGroup) {
                ViewGroup currentViewGroup = (ViewGroup) currentView;
                for (int i = 0; i < currentViewGroup.getChildCount(); i++) {
                    queue.add(currentViewGroup.getChildAt(i));
                }
            }
        }
        return view;
    }

    /**
     * Make Snackbar for specified {@link View}
     *
     * @param view     View
     * @param text     Message text
     * @param duration Duration
     * @return Snackbar
     */
    @NonNull
    public static Snackbar makeSnackbar(@NonNull View view, @NonNull CharSequence text,
            @Snackbar.Duration int duration) {
        return Snackbar.make(view, text, duration);
    }

    /**
     * Make Snackbar for specified {@link View}
     *
     * @param view     View
     * @param textId   Message text resource identifier
     * @param duration Duration
     * @return Snackbar
     */
    @NonNull
    public static Snackbar makeSnackbar(@NonNull View view, @StringRes int textId,
            @Snackbar.Duration int duration) {
        return Snackbar.make(view, textId, duration);
    }

    /**
     * Make Snackbar for specified {@link Window}
     *
     * @param window   Window
     * @param text     Message text
     * @param duration Duration
     * @return Snackbar or null
     */
    @Nullable
    public static Snackbar makeSnackbar(@NonNull Window window, @NonNull CharSequence text,
            @Snackbar.Duration int duration) {
        View view = window.getDecorView();
        if (view == null) {
            return null;
        }
        View contentView = view.findViewById(android.R.id.content);
        if (contentView != null) {
            view = findSuitableView(contentView);
        }
        return makeSnackbar(view, text, duration);
    }

    /**
     * Make Snackbar for specified {@link Window}
     *
     * @param window   Window
     * @param textId   Message text resource identifier
     * @param duration Duration
     * @return Snackbar or null
     */
    @Nullable
    public static Snackbar makeSnackbar(@NonNull Window window, @StringRes int textId,
            @Snackbar.Duration int duration) {
        View view = window.getDecorView();
        if (view == null) {
            return null;
        }
        View contentView = view.findViewById(android.R.id.content);
        if (contentView != null) {
            view = findSuitableView(contentView);
        }
        return makeSnackbar(view, textId, duration);
    }

    /**
     * Make Snackbar for specified {@link Activity}
     *
     * @param activity Activity
     * @param text     Message text
     * @param duration Duration
     * @return Snackbar or null
     */
    @Nullable
    public static Snackbar makeSnackbar(@NonNull Activity activity, @NonNull CharSequence text,
            @Snackbar.Duration int duration) {
        Window window = activity.getWindow();
        if (window == null) {
            return null;
        }
        return makeSnackbar(window, text, duration);
    }

    /**
     * Make Snackbar for specified {@link Activity}
     *
     * @param activity Activity
     * @param textId   Message text resource identifier
     * @param duration Duration
     * @return Snackbar or null
     */
    @Nullable
    public static Snackbar makeSnackbar(@NonNull Activity activity, @StringRes int textId,
            @Snackbar.Duration int duration) {
        Window window = activity.getWindow();
        if (window == null) {
            return null;
        }
        return makeSnackbar(window, textId, duration);
    }

    /**
     * Make Snackbar for specified {@link Fragment}
     *
     * @param fragment Fragment
     * @param text     Message text
     * @param duration Duration
     * @return Snackbar
     */
    @Nullable
    public static Snackbar makeSnackbar(@NonNull Fragment fragment, @NonNull CharSequence text,
            @Snackbar.Duration int duration) {
        Activity activity = fragment.getActivity();
        if (activity == null) {
            return null;
        }
        return makeSnackbar(activity, text, duration);
    }

    /**
     * Make Snackbar for specified {@link Fragment}
     *
     * @param fragment Fragment
     * @param textId   Message text resource identifier
     * @param duration Duration
     * @return Snackbar
     */
    @Nullable
    public static Snackbar makeSnackbar(@NonNull Fragment fragment, @StringRes int textId,
            @Snackbar.Duration int duration) {
        Activity activity = fragment.getActivity();
        if (activity == null) {
            return null;
        }
        return makeSnackbar(activity, textId, duration);
    }
}
