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

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Tools for asynchronous tasks in Android
 */
public final class ThreadUtils {
    private ThreadUtils() {
    }

    /**
     * Wrap {@link Callable} into {@link Runnable}
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
     * Wrap {@link AsyncTask} into {@link Runnable}
     *
     * @param asyncTask  AsyncTask
     * @param parameters AsyncTask parameters
     * @return Runnable
     */
    @SafeVarargs
    @NonNull
    private static <Parameters, Progress, Result> Runnable wrapAsyncTask(
            @NonNull final AsyncTask<Parameters, Progress, Result> asyncTask,
            final Parameters... parameters) {
        return new Runnable() {
            @Override
            public void run() {
                asyncTask.executeOnExecutor(ExecutorUtils.getThreadUtilsExecutor(), parameters);
            }
        };
    }

    /**
     * Set name prefix of background threads (threads named like [prefix][number])
     *
     * @param prefix Thread name prefix
     */
    public static void setBackgroundThreadNamePrefix(@NonNull String prefix) {
        ExecutorUtils.setBackgroundThreadNamePrefix(prefix);
    }

    /**
     * Get current name prefix of background threads (threads named like [prefix][number])
     *
     * @return Thread name prefix
     */
    @NonNull
    public static String getBackgroundThreadNamePrefix() {
        return ExecutorUtils.getBackgroundThreadNamePrefix();
    }

    /**
     * Run task asynchronous
     *
     * @param task Task
     * @return a {@link Future} representing pending completion of the task
     */
    @NonNull
    public static Future<?> runAsync(@NonNull Runnable task) {
        return ExecutorUtils.getThreadUtilsExecutor().submit(task);
    }

    /**
     * Run task asynchronous
     *
     * @param task Task
     * @return a {@link Future} representing pending completion of the task
     */
    @NonNull
    public static <T> Future<T> runAsync(@NonNull Callable<T> task) {
        return ExecutorUtils.getThreadUtilsExecutor().submit(task);
    }

    /**
     * Run task asynchronous
     *
     * @param task       Task
     * @param parameters Parameters
     */
    @SafeVarargs
    public static <Parameters, Progress, Result> void runAsync(
            @NonNull AsyncTask<Parameters, Progress, Result> task, Parameters... parameters) {
        ExecutorUtils.getMainThreadExecutor().execute(wrapAsyncTask(task, parameters));
    }

    /**
     * Run task asynchronous with specified delay
     *
     * @param task  Task
     * @param delay Delay
     */
    public static void runAsync(@NonNull final Runnable task, long delay) {
        ExecutorUtils.getMainThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                runAsync(task);
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
     * Run task asynchronous with specified delay
     *
     * @param task       Task
     * @param delay      Delay
     * @param parameters Parameters
     */
    @SafeVarargs
    public static <Parameters, Progress, Result> void runAsync(
            @NonNull AsyncTask<Parameters, Progress, Result> task, long delay,
            Parameters... parameters) {
        ExecutorUtils.getMainThreadExecutor().execute(wrapAsyncTask(task, parameters), delay);
    }

    /**
     * Run task on the main (UI) thread
     *
     * @param task Task
     * @return a {@link Future} representing pending completion of the task
     */
    @NonNull
    public static Future<?> runOnMainThread(@NonNull Runnable task) {
        return ExecutorUtils.getMainThreadExecutor().submit(task);
    }

    /**
     * Run task on the main (UI) thread
     *
     * @param task Task
     * @return a {@link Future} representing pending completion of the task
     */
    @NonNull
    public static <T> Future<T> runOnMainThread(@NonNull Callable<T> task) {
        return ExecutorUtils.getMainThreadExecutor().submit(task);
    }

    /**
     * Run task on the main (UI) thread with specified delay
     *
     * @param task  Task
     * @param delay Delay
     * @return a {@link Future} representing pending completion of the task
     */
    @NonNull
    public static Future<?> runOnMainThread(@NonNull Runnable task, long delay) {
        return ExecutorUtils.getMainThreadExecutor().submit(task, delay);
    }

    /**
     * Run task on the main (UI) thread with specified delay
     *
     * @param task  Task
     * @param delay Delay
     * @return a {@link Future} representing pending completion of the task
     */
    @NonNull
    public static <T> Future<T> runOnMainThread(@NonNull Callable<T> task, long delay) {
        return ExecutorUtils.getMainThreadExecutor().submit(task, delay);
    }

    /**
     * Throws {@link NotMainThreadException} with specified message
     * if current thread is not the main (UI) thread
     *
     * @param message Message
     */
    public static void requireMainThread(@Nullable String message) {
        if (Thread.currentThread() != ExecutorUtils.getMainThreadExecutor().getThread()) {
            throw new NotMainThreadException(message);
        }
    }

    /**
     * Throws {@link NotMainThreadException} if current thread is not the main (UI) thread
     */
    public static void requireMainThread() {
        if (Thread.currentThread() != ExecutorUtils.getMainThreadExecutor().getThread()) {
            throw new NotMainThreadException();
        }
    }
}
