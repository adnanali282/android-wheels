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
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import java.util.LinkedList;

/**
 * Tools for Snackbar
 */
public final class SnackbarUtils {
    private SnackbarUtils() {
    }

    /**
     * Wrap {@link SnackbarAction} into {@link View.OnClickListener}
     *
     * @param action   Action
     * @param snackbar Snackbar
     * @return OnClickListener
     */
    @NonNull
    private static View.OnClickListener wrapAction(@NonNull final SnackbarAction action,
            @NonNull final Snackbar snackbar) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action.onClick(v, snackbar);
            }
        };
    }

    /**
     * Find suitable view for {@link Snackbar}
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
     * Build snackbar
     *
     * @param view            View
     * @param messageText     Message text
     * @param backgroundColor Background color
     * @param textColor       Text color
     * @param duration        Duration
     * @param action          Action
     * @param actionText      Action text
     * @return Snackbar
     */
    @NonNull
    public static Snackbar build(@NonNull View view, @NonNull CharSequence messageText,
            @ColorInt int backgroundColor, @ColorInt int textColor, @Snackbar.Duration int duration,
            @Nullable SnackbarAction action, @Nullable CharSequence actionText) {
        Snackbar snackbar = Snackbar.make(view, messageText, duration);
        if (backgroundColor != -1) {
            snackbar.getView().setBackgroundColor(backgroundColor);
        }
        if (textColor != -1) {
            ((TextView) view.findViewById(android.support.design.R.id.snackbar_text))
                    .setTextColor(textColor);
            ((Button) view.findViewById(android.support.design.R.id.snackbar_action))
                    .setTextColor(textColor);
        }
        if (action != null && actionText != null) {
            snackbar.setAction(actionText, wrapAction(action, snackbar));
        }
        return snackbar;
    }

    /**
     * Show snackbar
     *
     * @param view            View
     * @param messageText     Message text
     * @param backgroundColor Background color
     * @param textColor       Text color
     * @param duration        Duration
     * @param action          Action
     * @param actionText      Action text
     * @return Snackbar
     */
    @NonNull
    public static Snackbar show(@NonNull View view, @NonNull CharSequence messageText,
            @ColorInt int backgroundColor, @ColorInt int textColor, @Snackbar.Duration int duration,
            @Nullable SnackbarAction action, @Nullable CharSequence actionText) {
        Snackbar snackbar =
                build(view, messageText, backgroundColor, textColor, duration, action, actionText);
        snackbar.show();
        return snackbar;
    }

    /**
     * Show snackbar
     *
     * @param window          Window
     * @param messageText     Message text
     * @param backgroundColor Background color
     * @param textColor       Text color
     * @param duration        Duration
     * @param action          Action
     * @param actionText      Action text
     * @return Snackbar or null
     */
    @Nullable
    public static Snackbar show(@NonNull Window window, @NonNull CharSequence messageText,
            @ColorInt int backgroundColor, @ColorInt int textColor, @Snackbar.Duration int duration,
            @Nullable SnackbarAction action, @Nullable CharSequence actionText) {
        View view = window.getDecorView();
        if (view == null) {
            return null;
        }
        View contentView = view.findViewById(android.R.id.content);
        if (contentView != null) {
            view = findSuitableView(contentView);
        }
        return show(view, messageText, backgroundColor, textColor, duration, action, actionText);
    }

    /**
     * Show snackbar
     *
     * @param activity        Activity
     * @param messageText     Message text
     * @param backgroundColor Background color
     * @param textColor       Text color
     * @param duration        Duration
     * @param action          Action
     * @param actionText      Action text
     * @return Snackbar or null
     */
    @Nullable
    public static Snackbar show(@NonNull Activity activity, @NonNull CharSequence messageText,
            @ColorInt int backgroundColor, @ColorInt int textColor, @Snackbar.Duration int duration,
            @Nullable SnackbarAction action, @Nullable CharSequence actionText) {
        Window window = activity.getWindow();
        if (window == null) {
            return null;
        }
        return show(window, messageText, backgroundColor, textColor, duration, action, actionText);
    }

    /**
     * Show snackbar
     *
     * @param fragment        Fragment
     * @param messageText     Message text
     * @param backgroundColor Background color
     * @param textColor       Text color
     * @param duration        Duration
     * @param action          Action
     * @param actionText      Action text
     * @return Snackbar or null
     */
    @Nullable
    public static Snackbar show(@NonNull Fragment fragment, @NonNull CharSequence messageText,
            @ColorInt int backgroundColor, @ColorInt int textColor, @Snackbar.Duration int duration,
            @Nullable SnackbarAction action, @Nullable CharSequence actionText) {
        Activity activity = fragment.getActivity();
        if (activity == null) {
            return null;
        }
        return show(activity, messageText, backgroundColor, textColor, duration, action,
                actionText);
    }
}
