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
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

/**
 * Tools for asynchronous tasks in Android
 */
public final class ThreadUtils {
    private static volatile boolean sThrowExecutionExceptions = true;
    private static volatile boolean sAsyncExecutorShutDown = false;

    private ThreadUtils() {
    }

    /**
     * Create new {@link ThreadFactory} same as used in {@link ThreadUtils}, {@link HttpRequest}
     * and {@link ImageLoader}; this factory will use the same thread name prefix
     * and naming scheme as rest threads used by mentioned components.
     *
     * @return New thread factory
     */
    @NonNull
    @AnyThread
    public static ThreadFactory newThreadFactory() {
        return new AsyncThreadFactory(Thread.NORM_PRIORITY);
    }

    /**
     * Create new {@link ThreadFactory} same as used in {@link ThreadUtils}, {@link HttpRequest}
     * and {@link ImageLoader}; this factory will use the same thread name prefix
     * and naming scheme as rest threads used by mentioned components.
     *
     * @param threadsPriority Priority of threads created by the factory
     * @return New thread factory
     */
    @NonNull
    @AnyThread
    public static ThreadFactory newThreadFactory(
            @IntRange(from = Thread.MIN_PRIORITY, to = Thread.MAX_PRIORITY) int threadsPriority) {
        return new AsyncThreadFactory(threadsPriority);
    }

    /**
     * Create new {@link ThreadFactory} same as used in {@link ThreadUtils}, {@link HttpRequest}
     * and {@link ImageLoader}; this factory will use the same thread name prefix
     * and naming scheme as rest threads used by mentioned components.
     *
     * @param threadsPriority Priority of threads created by the factory
     * @param daemonThreads   Whether if created threads will be daemon threads
     * @return New thread factory
     */
    @NonNull
    @AnyThread
    public static ThreadFactory newThreadFactory(
            @IntRange(from = Thread.MIN_PRIORITY, to = Thread.MAX_PRIORITY) int threadsPriority,
            boolean daemonThreads) {
        return new AsyncThreadFactory(threadsPriority, daemonThreads);
    }

    /**
     * Get current name prefix of background threads (threads named like [prefix][number])
     * <br><br>
     * <b>Affects:</b>
     * <ul>
     * <li>{@link ThreadUtils}</li>
     * <li>{@link ImageLoader}</li>
     * <li>{@link HttpRequest}</li>
     * <li>Threads, created thread factories, returned by {@link #newThreadFactory()},
     * {@link #newThreadFactory(int)} and {@link #newThreadFactory(int, boolean)}</li>
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
     * otherwise default prefix will be used.
     * <br><br>
     * <b>Affects:</b>
     * <ul>
     * <li>{@link ThreadUtils}</li>
     * <li>{@link ImageLoader}</li>
     * <li>{@link HttpRequest}</li>
     * <li>Threads, created thread factories, returned by {@link #newThreadFactory()},
     * {@link #newThreadFactory(int)} and {@link #newThreadFactory(int, boolean)}</li>
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
     * Shutdown asynchronous executor, attempt to stop all actively executing tasks, halt
     * processing of waiting tasks.
     * <br>
     * All {@code runAsync*} methods calls will be ignored and will return {@code null} after
     * this method returns. This operation can't be undone.
     */
    public static void shutdownAsyncExecutor() {
        sAsyncExecutorShutDown = true;
        InternalExecutors.getThreadUtilsExecutor().shutdownNow();
    }

    /**
     * Whether if asynchronous executor has been shut down, all {@code runAsync*} methods calls
     * will be ignored and will return {@code null} if this method returns {@code true}.
     */
    public static boolean isAsyncExecutorShutDown() {
        return sAsyncExecutorShutDown;
    }

    /**
     * Run task asynchronous
     *
     * @param task Task
     * @return a {@link Future} representing pending completion of the task, or {@code null}
     * if asynchronous executor has been shut down
     */
    @Nullable
    @AnyThread
    public static Future<?> runAsync(@NonNull Runnable task) {
        if (sAsyncExecutorShutDown) {
            return null;
        }
        return InternalExecutors.getThreadUtilsExecutor().submit(task);
    }

    /**
     * Run task asynchronous
     *
     * @param task Task
     * @return a {@link Future} representing pending completion of the task, or {@code null}
     * if asynchronous executor has been shut down
     */
    @Nullable
    @AnyThread
    public static <T> Future<T> runAsync(@NonNull Callable<T> task) {
        if (sAsyncExecutorShutDown) {
            return null;
        }
        return InternalExecutors.getThreadUtilsExecutor().submit(task);
    }

    /**
     * Run task asynchronous
     *
     * @param task       Task
     * @param parameters Parameters
     * @return Task or {@code null} if asynchronous executor has been shut down
     */
    @Nullable
    @AnyThread
    @SafeVarargs
    public static <Parameters, Progress, Result> AsyncTask<Parameters, Progress, Result> runAsync(
            @NonNull AsyncTask<Parameters, Progress, Result> task, Parameters... parameters) {
        if (sAsyncExecutorShutDown) {
            return null;
        }
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
        if (sAsyncExecutorShutDown) {
            return;
        }
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
        if (sAsyncExecutorShutDown) {
            return;
        }
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
     * @return Task or {@code null} if asynchronous executor has been shut down
     */
    @Nullable
    @AnyThread
    @SafeVarargs
    public static <Parameters, Progress, Result> AsyncTask<Parameters, Progress, Result> runAsync(
            @NonNull AsyncTask<Parameters, Progress, Result> task, long delay,
            Parameters... parameters) {
        if (sAsyncExecutorShutDown) {
            return null;
        }
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
        if (Thread.currentThread() != MainThreadHolder.MAIN_THREAD) {
            throw new NotMainThreadException(message);
        }
    }

    /**
     * Throws {@link NotMainThreadException} if current thread is not the main (UI) thread
     */
    @AnyThread
    public static void requireMainThread() {
        if (Thread.currentThread() != MainThreadHolder.MAIN_THREAD) {
            throw new NotMainThreadException();
        }
    }

    /**
     * Check if current thread is the main (UI) thread or not
     *
     * @return {@code true} if current thread is the main (UI) thread, {@code false} otherwise
     */
    @AnyThread
    public static boolean isMainThread() {
        return Thread.currentThread() == MainThreadHolder.MAIN_THREAD;
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
                if (sAsyncExecutorShutDown) {
                    return;
                }
                asyncTask.executeOnExecutor(InternalExecutors.getThreadUtilsExecutor(), parameters);
            }
        };
    }

    private static final class MainThreadHolder {
        public static final Thread MAIN_THREAD = Looper.getMainLooper().getThread();
    }
}
