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

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Tools for asynchronous tasks in Android
 */
public final class ThreadUtils {
    private static final Thread MAIN_THREAD;
    private static final Handler MAIN_THREAD_HANDLER;
    private static final ExecutorService ASYNC_EXECUTOR = Executors.newCachedThreadPool();

    static {
        Looper mainLooper = Looper.getMainLooper();
        MAIN_THREAD = mainLooper.getThread();
        MAIN_THREAD_HANDLER = new Handler(mainLooper);
    }

    private ThreadUtils() {
    }

    /**
     * Wrap Callable into Runnable
     *
     * @param callable Callable
     * @return Runnable
     */
    @NonNull
    private static Runnable wrapCallable(@NonNull final Callable<?> callable) {
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

    /**
     * Run task asynchronous
     *
     * @param task Task
     * @return a Future representing pending completion of the task
     */
    @NonNull
    public static Future<?> runAsync(@NonNull Runnable task) {
        return ASYNC_EXECUTOR.submit(task);
    }

    /**
     * Run task asynchronous
     *
     * @param task Task
     * @return a Future representing pending completion of the task
     */
    @NonNull
    public static <T> Future<T> runAsync(@NonNull Callable<T> task) {
        return ASYNC_EXECUTOR.submit(task);
    }

    /**
     * Run task asynchronous
     *
     * @param task       Task
     * @param parameters Parameters
     */
    @SafeVarargs
    public static <Parameters, Progress, Result> void runAsync(
            @NonNull final AsyncTask<Parameters, Progress, Result> task,
            final Parameters... parameters) {
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                task.executeOnExecutor(ASYNC_EXECUTOR, parameters);
            }
        });
    }

    /**
     * Run task asynchronous with specified delay
     *
     * @param task  Task
     * @param delay Delay
     */
    public static void runAsync(@NonNull final Runnable task, long delay) {
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                runAsync(task);
            }
        }, delay);
    }

    /**
     * Run task asynchronous with specified delay
     *
     * @param task       Task
     * @param delay      Delay
     * @param parameters Parameters
     */
    @SafeVarargs
    public static <Parameters, Progress, Result> void runAsync(
            @NonNull final AsyncTask<Parameters, Progress, Result> task, long delay,
            final Parameters... parameters) {
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                task.executeOnExecutor(ASYNC_EXECUTOR, parameters);
            }
        }, delay);
    }

    /**
     * Run task asynchronous with specified delay
     *
     * @param task  Task
     * @param delay Delay
     */
    public static void runAsync(@NonNull Callable<?> task, long delay) {
        runAsync(wrapCallable(task), delay);
    }

    /**
     * Run task on main (UI) thread
     *
     * @param task Task
     */
    public static void runOnMainThread(@NonNull Runnable task) {
        if (Thread.currentThread() == MAIN_THREAD) {
            task.run();
        } else {
            runOnMainThread(task, 0);
        }
    }

    /**
     * Run task on main (UI) thread
     *
     * @param task Task
     */
    public static void runOnMainThread(@NonNull Callable<?> task) {
        runOnMainThread(wrapCallable(task));
    }

    /**
     * Run task on main (UI) thread with specified delay
     *
     * @param task  Task
     * @param delay Delay
     */
    public static void runOnMainThread(@NonNull Runnable task, long delay) {
        MAIN_THREAD_HANDLER.postDelayed(task, delay);
    }

    /**
     * Run task on main (UI) thread with specified delay
     *
     * @param task  Task
     * @param delay Delay
     */
    public static void runOnMainThread(@NonNull Callable<?> task, long delay) {
        runOnMainThread(wrapCallable(task), delay);
    }
}
