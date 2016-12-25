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

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Executors for internal usage in AndroidWheels
 */
final class InternalExecutors {
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final Lock THREAD_UTILS_EXECUTOR_LOCK = new ReentrantLock();
    private static final Lock THREAD_UTILS_SCHEDULED_EXECUTOR_LOCK = new ReentrantLock();
    private static final Lock HTTP_REQUEST_EXECUTOR_LOCK = new ReentrantLock();
    private static final Lock IMAGE_LOADER_EXECUTOR_LOCK = new ReentrantLock();
    private static final Lock STORAGE_IMAGE_CACHE_EXECUTOR_LOCK = new ReentrantLock();
    private static final Lock MAIN_THREAD_EXECUTOR_LOCK = new ReentrantLock();
    private static final Lock POOL_SIZE_LOCK = new ReentrantLock();
    private static volatile ThreadPoolExecutor sThreadUtilsExecutor;
    private static volatile ScheduledThreadPoolExecutor sThreadUtilsScheduledExecutor;
    private static volatile ThreadPoolExecutor sHttpRequestExecutor;
    private static volatile ThreadPoolExecutor sImageLoaderExecutor;
    private static volatile ThreadPoolExecutor sStorageImageCacheExecutor;
    private static volatile MainThreadExecutor sMainThreadExecutor;

    private InternalExecutors() {
    }

    @NonNull
    public static ThreadPoolExecutor getThreadUtilsExecutor() {
        ThreadPoolExecutor executor = sThreadUtilsExecutor;
        if (executor == null) {
            THREAD_UTILS_EXECUTOR_LOCK.lock();
            try {
                executor = sThreadUtilsExecutor;
                if (executor == null) {
                    executor = new AsyncExecutor(0, Integer.MAX_VALUE, 90, TimeUnit.SECONDS,
                            new SynchronousQueue<Runnable>(), new AsyncThreadFactory());
                    sThreadUtilsExecutor = executor;
                }
            } finally {
                THREAD_UTILS_EXECUTOR_LOCK.unlock();
            }
        }
        return executor;
    }

    @NonNull
    public static ScheduledThreadPoolExecutor getThreadUtilsScheduledExecutor() {
        ScheduledThreadPoolExecutor executor = sThreadUtilsScheduledExecutor;
        if (executor == null) {
            THREAD_UTILS_SCHEDULED_EXECUTOR_LOCK.lock();
            try {
                executor = sThreadUtilsScheduledExecutor;
                if (executor == null) {
                    executor = new ScheduledThreadPoolExecutor(1, new AsyncThreadFactory());
                    sThreadUtilsScheduledExecutor = executor;
                }
            } finally {
                THREAD_UTILS_SCHEDULED_EXECUTOR_LOCK.unlock();
            }
        }
        return executor;
    }

    @NonNull
    public static ThreadPoolExecutor getHttpRequestExecutor() {
        ThreadPoolExecutor executor = sHttpRequestExecutor;
        if (executor == null) {
            HTTP_REQUEST_EXECUTOR_LOCK.lock();
            try {
                executor = sHttpRequestExecutor;
                if (executor == null) {
                    executor = new AsyncExecutor(CPU_COUNT, CPU_COUNT, 0, TimeUnit.NANOSECONDS,
                            new LinkedBlockingQueue<Runnable>(), new AsyncThreadFactory());
                    sHttpRequestExecutor = executor;
                }
            } finally {
                HTTP_REQUEST_EXECUTOR_LOCK.unlock();
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
                    int threadCount = Math.round(CPU_COUNT * 1.5F);
                    executor = new AsyncExecutor(threadCount, threadCount, 0, TimeUnit.NANOSECONDS,
                            new LinkedBlockingQueue<Runnable>(),
                            new AsyncThreadFactory(Thread.MIN_PRIORITY));
                    sImageLoaderExecutor = executor;
                }
            } finally {
                IMAGE_LOADER_EXECUTOR_LOCK.unlock();
            }
        }
        return executor;
    }

    @NonNull
    public static ThreadPoolExecutor getStorageImageCacheExecutor() {
        ThreadPoolExecutor executor = sStorageImageCacheExecutor;
        if (executor == null) {
            STORAGE_IMAGE_CACHE_EXECUTOR_LOCK.lock();
            try {
                executor = sStorageImageCacheExecutor;
                if (executor == null) {
                    executor = new AsyncExecutor(1, 1, 0, TimeUnit.NANOSECONDS,
                            new LinkedBlockingQueue<Runnable>(),
                            new AsyncThreadFactory(Thread.MIN_PRIORITY));
                    sStorageImageCacheExecutor = executor;
                }
            } finally {
                STORAGE_IMAGE_CACHE_EXECUTOR_LOCK.unlock();
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

    public static void setPoolSize(@NonNull ThreadPoolExecutor executor,
            @IntRange(from = 1, to = Integer.MAX_VALUE) int size) {
        POOL_SIZE_LOCK.lock();
        try {
            int corePoolSize = executor.getCorePoolSize();
            if (size == corePoolSize) {
                return;
            }
            if (size > corePoolSize) {
                executor.setMaximumPoolSize(size);
                executor.setCorePoolSize(size);
            } else {
                executor.setCorePoolSize(size);
                executor.setMaximumPoolSize(size);
            }
        } finally {
            POOL_SIZE_LOCK.unlock();
        }
    }
}
