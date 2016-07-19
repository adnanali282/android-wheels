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
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tools for asynchronous tasks in Android
 */
public final class ThreadUtils {
    private static final BackgroundExecutor BACKGROUND_EXECUTOR =
            new BackgroundExecutor("ThreadUtils-background-thread-");
    private static final MainThreadExecutor MAIN_THREAD_EXECUTOR = new MainThreadExecutor();

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
                asyncTask.executeOnExecutor(BACKGROUND_EXECUTOR, parameters);
            }
        };
    }

    /**
     * Set name prefix of background threads (threads named like [prefix][number])
     *
     * @param prefix Thread name prefix
     */
    public static void setBackgroundThreadNamePrefix(@NonNull String prefix) {
        BACKGROUND_EXECUTOR.getBackgroundThreadFactory().setThreadNamePrefix(prefix);
    }

    /**
     * Get current name prefix of background threads (threads named like [prefix][number])
     *
     * @return Thread name prefix
     */
    @NonNull
    public static String getBackgroundThreadNamePrefix() {
        return BACKGROUND_EXECUTOR.getBackgroundThreadFactory().getThreadNamePrefix();
    }

    /**
     * Run task asynchronous
     *
     * @param task Task
     * @return a {@link Future} representing pending completion of the task
     */
    @NonNull
    public static Future<?> runAsync(@NonNull Runnable task) {
        return BACKGROUND_EXECUTOR.submit(task);
    }

    /**
     * Run task asynchronous
     *
     * @param task Task
     * @return a {@link Future} representing pending completion of the task
     */
    @NonNull
    public static <T> Future<T> runAsync(@NonNull Callable<T> task) {
        return BACKGROUND_EXECUTOR.submit(task);
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
        MAIN_THREAD_EXECUTOR.execute(wrapAsyncTask(task, parameters));
    }

    /**
     * Run task asynchronous with specified delay
     *
     * @param task  Task
     * @param delay Delay
     */
    public static void runAsync(@NonNull final Runnable task, long delay) {
        MAIN_THREAD_EXECUTOR.execute(new Runnable() {
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
        MAIN_THREAD_EXECUTOR.execute(wrapAsyncTask(task, parameters), delay);
    }

    /**
     * Run task on the main (UI) thread
     *
     * @param task Task
     * @return a {@link Future} representing pending completion of the task
     */
    @NonNull
    public static Future<?> runOnMainThread(@NonNull Runnable task) {
        return MAIN_THREAD_EXECUTOR.submit(task);
    }

    /**
     * Run task on the main (UI) thread
     *
     * @param task Task
     * @return a {@link Future} representing pending completion of the task
     */
    @NonNull
    public static <T> Future<T> runOnMainThread(@NonNull Callable<T> task) {
        return MAIN_THREAD_EXECUTOR.submit(task);
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
        return MAIN_THREAD_EXECUTOR.submit(task, delay);
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
        return MAIN_THREAD_EXECUTOR.submit(task, delay);
    }

    /**
     * Throws {@link NotMainThreadException} with specified message
     * if current thread is not the main (UI) thread
     *
     * @param message Message
     */
    public static void requireMainThread(@Nullable String message) {
        if (Thread.currentThread() != MAIN_THREAD_EXECUTOR.getThread()) {
            throw new NotMainThreadException(message);
        }
    }

    /**
     * Throws {@link NotMainThreadException} if current thread is not the main (UI) thread
     */
    public static void requireMainThread() {
        if (Thread.currentThread() != MAIN_THREAD_EXECUTOR.getThread()) {
            throw new NotMainThreadException();
        }
    }

    private static class BackgroundExecutor extends ThreadPoolExecutor {
        public BackgroundExecutor(@NonNull String threadNamePrefix) {
            super(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(),
                    new BackgroundThreadFactory(threadNamePrefix, Thread.NORM_PRIORITY, false));
        }

        @NonNull
        public BackgroundThreadFactory getBackgroundThreadFactory() {
            ThreadFactory threadFactory = getThreadFactory();
            if (threadFactory instanceof BackgroundThreadFactory) {
                return (BackgroundThreadFactory) threadFactory;
            } else {
                throw new IllegalStateException();
            }
        }
    }

    private static class BackgroundThreadFactory implements ThreadFactory {
        private final AtomicInteger mThreadCounter = new AtomicInteger(1);
        private volatile String mThreadNamePrefix;
        private final int mThreadPriority;
        private final boolean mDaemonThread;

        public BackgroundThreadFactory(@NonNull String threadNamePrefix,
                @IntRange(from = Thread.MIN_PRIORITY, to = Thread.MAX_PRIORITY) int threadPriority,
                boolean daemonThread) {
            mThreadNamePrefix = Objects.requireNonNull(threadNamePrefix);
            mDaemonThread = daemonThread;
            mThreadPriority = threadPriority;
        }

        @Override
        public Thread newThread(@NonNull Runnable runnable) {
            mThreadCounter.compareAndSet(Integer.MAX_VALUE, 1);
            Thread thread =
                    new Thread(runnable, mThreadNamePrefix + mThreadCounter.getAndIncrement());
            if (thread.getPriority() != mThreadPriority) {
                thread.setPriority(mThreadPriority);
            }
            if (thread.isDaemon() != mDaemonThread) {
                thread.setDaemon(mDaemonThread);
            }
            return thread;
        }

        public void setThreadNamePrefix(@NonNull String threadNamePrefix) {
            mThreadNamePrefix = Objects.requireNonNull(threadNamePrefix);
        }

        @NonNull
        public String getThreadNamePrefix() {
            return mThreadNamePrefix;
        }
    }

    private static class MainThreadExecutor extends AbstractExecutorService {
        private final Handler mHandler;
        private final Thread mThread;

        public MainThreadExecutor() {
            Looper mainLooper = Looper.getMainLooper();
            mHandler = new Handler(mainLooper);
            mThread = mainLooper.getThread();
        }

        @NonNull
        public Handler getHandler() {
            return mHandler;
        }

        @NonNull
        public Thread getThread() {
            return mThread;
        }

        @Override
        public void execute(@NonNull Runnable command) {
            if (Thread.currentThread() == mThread) {
                command.run();
            } else {
                execute(command, 0);
            }
        }

        public void execute(@NonNull Runnable command, long delay) {
            mHandler.postDelayed(command, delay);
        }

        @NonNull
        public Future<?> submit(@NonNull Runnable task, long delay) {
            RunnableFuture<Void> future = newTaskFor(Objects.requireNonNull(task), null);
            execute(future, delay);
            return future;
        }

        @NonNull
        public <T> Future<T> submit(@NonNull Callable<T> task, long delay) {
            RunnableFuture<T> future = newTaskFor(Objects.requireNonNull(task));
            execute(future, delay);
            return future;
        }

        @Override
        public void shutdown() {
            throw new UnsupportedOperationException();
        }

        @NonNull
        @Override
        public List<Runnable> shutdownNow() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isShutdown() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isTerminated() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean awaitTermination(long timeout, @NonNull TimeUnit unit) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Thrown when current thread is not the main (UI) thread
     */
    public static class NotMainThreadException extends RuntimeException {
        public NotMainThreadException() {
            super();
        }

        public NotMainThreadException(String detailMessage) {
            super(detailMessage);
        }

        public NotMainThreadException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        public NotMainThreadException(Throwable throwable) {
            super(throwable);
        }
    }
}