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

import android.support.annotation.NonNull;

import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Executors for internal usage in AndroidWheels
 */
final class ExecutorUtils {
    private static final Lock THREAD_UTILS_EXECUTOR_LOCK = new ReentrantLock();
    private static final Lock IMAGE_LOADER_EXECUTOR_LOCK = new ReentrantLock();
    private static final Lock MAIN_THREAD_EXECUTOR_LOCK = new ReentrantLock();
    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(1);
    private static volatile String sBackgroundThreadNamePrefix = "AndroidWheels-background-thread-";
    private static volatile ThreadPoolExecutor sThreadUtilsExecutor;
    private static volatile ThreadPoolExecutor sImageLoaderExecutor;
    private static volatile MainThreadExecutor sMainThreadExecutor;

    private ExecutorUtils() {
    }

    /**
     * Can be accessed via {@link ThreadUtils#getBackgroundThreadNamePrefix()}
     */
    @NonNull
    public static String getBackgroundThreadNamePrefix() {
        return sBackgroundThreadNamePrefix;
    }

    /**
     * Can be accessed via {@link ThreadUtils#setBackgroundThreadNamePrefix(String)}
     */
    public static void setBackgroundThreadNamePrefix(@NonNull String prefix) {
        sBackgroundThreadNamePrefix = Objects.requireNonNull(prefix);
    }

    @NonNull
    public static ThreadPoolExecutor getThreadUtilsExecutor() {
        ThreadPoolExecutor executor = sThreadUtilsExecutor;
        if (executor == null) {
            THREAD_UTILS_EXECUTOR_LOCK.lock();
            try {
                executor = sThreadUtilsExecutor;
                if (executor == null) {
                    executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
                            new SynchronousQueue<Runnable>(), new ThreadFactory() {
                        @Override
                        public Thread newThread(@NonNull Runnable runnable) {
                            THREAD_COUNTER.compareAndSet(Integer.MAX_VALUE, 1);
                            Thread thread = new Thread(runnable,
                                    sBackgroundThreadNamePrefix + THREAD_COUNTER.getAndIncrement());
                            if (thread.getPriority() != Thread.NORM_PRIORITY) {
                                thread.setPriority(Thread.NORM_PRIORITY);
                            }
                            if (thread.isDaemon()) {
                                thread.setDaemon(false);
                            }
                            return thread;
                        }
                    });
                    sThreadUtilsExecutor = executor;
                }
            } finally {
                THREAD_UTILS_EXECUTOR_LOCK.unlock();
            }
        }
        return executor;
    }

    @NonNull
    public static ThreadPoolExecutor getImageLoaderExecutor() {
        ThreadPoolExecutor executor = sImageLoaderExecutor;
        if (executor == null) {
            IMAGE_LOADER_EXECUTOR_LOCK.lock();
            try {
                executor = sImageLoaderExecutor;
                if (executor == null) {
                    executor = new ThreadPoolExecutor(0,
                            Math.round(Runtime.getRuntime().availableProcessors() * 1.5F), 300,
                            TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),
                            new ThreadFactory() {
                                @Override
                                public Thread newThread(@NonNull Runnable runnable) {
                                    THREAD_COUNTER.compareAndSet(Integer.MAX_VALUE, 1);
                                    Thread thread = new Thread(runnable,
                                            sBackgroundThreadNamePrefix +
                                                    THREAD_COUNTER.getAndIncrement());
                                    if (thread.getPriority() != Thread.MIN_PRIORITY) {
                                        thread.setPriority(Thread.MIN_PRIORITY);
                                    }
                                    if (thread.isDaemon()) {
                                        thread.setDaemon(false);
                                    }
                                    return thread;
                                }
                            });
                    sImageLoaderExecutor = executor;
                }
            } finally {
                IMAGE_LOADER_EXECUTOR_LOCK.unlock();
            }
        }
        return executor;
    }

    @NonNull
    public static MainThreadExecutor getMainThreadExecutor() {
        MainThreadExecutor executor = sMainThreadExecutor;
        if (executor == null) {
            MAIN_THREAD_EXECUTOR_LOCK.lock();
            try {
                executor = sMainThreadExecutor;
                if (executor == null) {
                    executor = new MainThreadExecutor();
                    sMainThreadExecutor = executor;
                }
            } finally {
                MAIN_THREAD_EXECUTOR_LOCK.unlock();
            }
        }
        return executor;
    }
}
