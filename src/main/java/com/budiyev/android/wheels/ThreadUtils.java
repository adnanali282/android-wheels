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

import android.os.AsyncTask;
import android.os.Looper;
import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Tools for asynchronous tasks in Android
 */
public final class ThreadUtils {
    private static volatile boolean sThrowExecutionExceptions = true;

    private ThreadUtils() {
    }

    /**
     * Get current name prefix of background threads (threads named like [prefix][number])
     * <br><br>
     * <b>Affects:</b>
     * <ul>
     * <li>{@link ThreadUtils}</li>
     * <li>{@link ImageLoader}</li>
     * <li>{@link HttpRequest}</li>
     * </ul>
     *
     * @return Thread name prefix
     */
    @NonNull
    @AnyThread
    public static String getBackgroundThreadNamePrefix() {
        return AsyncThreadFactory.getThreadNamePrefix();
    }

    /**
     * Set name prefix of background threads (threads named like [prefix][number])
     * <br>
     * Name prefix should be set before any background actions,
     * otherwise default prefix will be used
     * <br>
     * Name prefix of {@link ImageLoader} and {@link HttpRequest} threads can't be
     * changed after their usage
     * <br><br>
     * <b>Affects:</b>
     * <ul>
     * <li>{@link ThreadUtils}</li>
     * <li>{@link ImageLoader}</li>
     * <li>{@link HttpRequest}</li>
     * </ul>
     *
     * @param prefix Thread name prefix
     */
    @AnyThread
    public static void setBackgroundThreadNamePrefix(@NonNull String prefix) {
        AsyncThreadFactory.setThreadNamePrefix(prefix);
    }

    /**
     * Whether to rethrow exceptions that has been thrown in tasks
     * <br><br>
     * <b>Affects:</b>
     * <ul>
     * <li>{@link ThreadUtils}, excluding {@link ThreadUtils#runAsync(AsyncTask, Object[])},
     * {@link ThreadUtils#runAsync(AsyncTask, long, Object[])}</li>
     * <li>{@link ImageLoader}</li>
     * <li>{@link HttpRequest}</li>
     * </ul>
     */
    @AnyThread
    public static boolean isThrowExecutionExceptions() {
        return sThrowExecutionExceptions;
    }

    /**
     * Whether to rethrow exceptions that has been thrown in tasks
     * <br><br>
     * <b>Affects:</b>
     * <ul>
     * <li>{@link ThreadUtils}, excluding {@link ThreadUtils#runAsync(AsyncTask, Object[])},
     * {@link ThreadUtils#runAsync(AsyncTask, long, Object[])}</li>
     * <li>{@link ImageLoader}</li>
     * <li>{@link HttpRequest}</li>
     * </ul>
     */
    @AnyThread
    public static void setThrowExecutionExceptions(boolean throwExecutionExceptions) {
        sThrowExecutionExceptions = throwExecutionExceptions;
    }

    /**
     * Run task asynchronous
     *
     * @param task Task
     * @return a {@link Future} representing pending completion of the task
     */
    @NonNull
    @AnyThread
    public static Future<?> runAsync(@NonNull Runnable task) {
        return InternalExecutors.getThreadUtilsExecutor().submit(task);
    }

    /**
     * Run task asynchronous
     *
     * @param task Task
     * @return a {@link Future} representing pending completion of the task
     */
    @NonNull
    @AnyThread
    public static <T> Future<T> runAsync(@NonNull Callable<T> task) {
        return InternalExecutors.getThreadUtilsExecutor().submit(task);
    }

    /**
     * Run task asynchronous
     *
     * @param task       Task
     * @param parameters Parameters
     * @return Task
     */
    @NonNull
    @AnyThread
    @SafeVarargs
    public static <Parameters, Progress, Result> AsyncTask<Parameters, Progress, Result> runAsync(
            @NonNull AsyncTask<Parameters, Progress, Result> task, Parameters... parameters) {
        InternalExecutors.getMainThreadExecutor().execute(wrapAsyncTask(task, parameters));
        return task;
    }

    /**
     * Run task asynchronous with specified delay
     *
     * @param task  Task
     * @param delay Delay
     */
    @AnyThread
    public static void runAsync(@NonNull final Runnable task, long delay) {
        InternalExecutors.getMainThreadExecutor().execute(new Runnable() {
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
    @AnyThread
    public static void runAsync(@NonNull final Callable<?> task, long delay) {
        InternalExecutors.getMainThreadExecutor().execute(new Runnable() {
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
     * @return Task
     */
    @NonNull
    @AnyThread
    @SafeVarargs
    public static <Parameters, Progress, Result> AsyncTask<Parameters, Progress, Result> runAsync(
            @NonNull AsyncTask<Parameters, Progress, Result> task, long delay,
            Parameters... parameters) {
        InternalExecutors.getMainThreadExecutor().execute(wrapAsyncTask(task, parameters), delay);
        return task;
    }

    /**
     * Run task on the main (UI) thread
     *
     * @param task Task
     * @return a {@link Future} representing pending completion of the task
     */
    @NonNull
    @AnyThread
    public static Future<?> runOnMainThread(@NonNull Runnable task) {
        return InternalExecutors.getMainThreadExecutor().submit(task);
    }

    /**
     * Run task on the main (UI) thread
     *
     * @param task Task
     * @return a {@link Future} representing pending completion of the task
     */
    @NonNull
    @AnyThread
    public static <T> Future<T> runOnMainThread(@NonNull Callable<T> task) {
        return InternalExecutors.getMainThreadExecutor().submit(task);
    }

    /**
     * Run task on the main (UI) thread with specified delay
     *
     * @param task  Task
     * @param delay Delay
     * @return a {@link Future} representing pending completion of the task
     */
    @NonNull
    @AnyThread
    public static Future<?> runOnMainThread(@NonNull Runnable task, long delay) {
        return InternalExecutors.getMainThreadExecutor().submit(task, delay);
    }

    /**
     * Run task on the main (UI) thread with specified delay
     *
     * @param task  Task
     * @param delay Delay
     * @return a {@link Future} representing pending completion of the task
     */
    @NonNull
    @AnyThread
    public static <T> Future<T> runOnMainThread(@NonNull Callable<T> task, long delay) {
        return InternalExecutors.getMainThreadExecutor().submit(task, delay);
    }

    /**
     * Throws {@link NotMainThreadException} with specified message
     * if current thread is not the main (UI) thread
     *
     * @param message Message
     */
    @AnyThread
    public static void requireMainThread(@Nullable String message) {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            throw new NotMainThreadException(message);
        }
    }

    /**
     * Throws {@link NotMainThreadException} if current thread is not the main (UI) thread
     */
    @AnyThread
    public static void requireMainThread() {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            throw new NotMainThreadException();
        }
    }

    static void throwExecutionExceptionIfNeeded(@Nullable Runnable runnable,
            @Nullable Throwable throwable) {
        if (sThrowExecutionExceptions) {
            if (throwable == null) {
                if (runnable instanceof Future<?>) {
                    try {
                        ((Future<?>) runnable).get();
                    } catch (InterruptedException | CancellationException ignored) {
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e.getCause());
                    }
                }
            } else {
                throw new RuntimeException(throwable);
            }
        }
    }

    @SafeVarargs
    @NonNull
    private static <Parameters, Progress, Result> Runnable wrapAsyncTask(
            @NonNull final AsyncTask<Parameters, Progress, Result> asyncTask,
            final Parameters... parameters) {
        return new Runnable() {
            @Override
            public void run() {
                asyncTask.executeOnExecutor(InternalExecutors.getThreadUtilsExecutor(), parameters);
            }
        };
    }
}
