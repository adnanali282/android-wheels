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

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreadUtils {
    private static final Thread MAIN_THREAD;
    private static final Handler MAIN_THREAD_HANDLER;
    private static final ExecutorService ASYNC_EXECUTOR = Executors.newCachedThreadPool();

    static {
        Looper mainLooper = Looper.getMainLooper();
        MAIN_THREAD = mainLooper.getThread();
        MAIN_THREAD_HANDLER = new Handler(mainLooper);
    }

    @NonNull
    private static Runnable wrapCallable(final Callable<?> callable) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    callable.call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @NonNull
    public static Future<?> runAsync(@NonNull Runnable runnable) {
        return ASYNC_EXECUTOR.submit(runnable);
    }

    public static void runAsync(@NonNull final Runnable runnable, long delay) {
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                runAsync(runnable);
            }
        }, delay);
    }

    @NonNull
    public static <T> Future<T> callAsync(@NonNull Callable<T> callable) {
        return ASYNC_EXECUTOR.submit(callable);
    }

    public static void callAsync(@NonNull Callable<?> callable, long delay) {
        runAsync(wrapCallable(callable), delay);
    }

    public static void runOnMainThread(@NonNull Runnable runnable, long delay) {
        MAIN_THREAD_HANDLER.postDelayed(runnable, delay);
    }

    public static void runOnMainThread(@NonNull Runnable runnable) {
        if (Thread.currentThread() == MAIN_THREAD) {
            runnable.run();
        } else {
            runOnMainThread(runnable, 0);
        }
    }

    public static void callOnMainThread(@NonNull Callable<?> callable) {
        runOnMainThread(wrapCallable(callable));
    }

    public static void callOnMainThread(@NonNull Callable<?> callable, long delay) {
        runOnMainThread(wrapCallable(callable), delay);
    }
}
