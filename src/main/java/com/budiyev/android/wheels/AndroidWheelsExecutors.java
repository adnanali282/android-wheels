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

import android.support.annotation.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Executors for internal usage in AndroidWheels
 */
final class AndroidWheelsExecutors {
    private static final Lock THREAD_UTILS_EXECUTOR_LOCK = new ReentrantLock();
    private static final Lock HTTP_REQUEST_EXECUTOR_LOCK = new ReentrantLock();
    private static final Lock IMAGE_LOADER_EXECUTOR_LOCK = new ReentrantLock();
    private static final Lock STORAGE_IMAGE_CACHE_EXECUTOR_LOCK = new ReentrantLock();
    private static final Lock MAIN_THREAD_EXECUTOR_LOCK = new ReentrantLock();
    private static volatile ExecutorService sThreadUtilsExecutor;
    private static volatile ExecutorService sHttpRequestExecutor;
    private static volatile ExecutorService sImageLoaderExecutor;
    private static volatile ExecutorService sStorageImageCacheExecutor;
    private static volatile ExecutorService sMainThreadExecutor;

    private AndroidWheelsExecutors() {
    }

    public static boolean setPoolSize(@NonNull ExecutorService executor, int size) {
        if (executor instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executor;
            threadPoolExecutor.setCorePoolSize(size);
            threadPoolExecutor.setMaximumPoolSize(size);
            return true;
        } else {
            return false;
        }
    }

    public static int getPoolSize(@NonNull ExecutorService executor) {
        if (executor instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor) executor).getPoolSize();
        } else {
            return -1;
        }
    }

    @NonNull
    public static ExecutorService getThreadUtilsExecutor() {
        ExecutorService executor = sThreadUtilsExecutor;
        if (executor == null) {
            THREAD_UTILS_EXECUTOR_LOCK.lock();
            try {
                executor = sThreadUtilsExecutor;
                if (executor == null) {
                    executor = Executors.newCachedThreadPool(new AndroidWheelsThreadFactory());
                    sThreadUtilsExecutor = executor;
                }
            } finally {
                THREAD_UTILS_EXECUTOR_LOCK.unlock();
            }
        }
        return executor;
    }

    @NonNull
    public static ExecutorService getHttpRequestExecutor() {
        ExecutorService executor = sHttpRequestExecutor;
        if (executor == null) {
            HTTP_REQUEST_EXECUTOR_LOCK.lock();
            try {
                executor = sHttpRequestExecutor;
                if (executor == null) {
                    executor = Executors
                            .newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
                                    new AndroidWheelsThreadFactory());
                    sHttpRequestExecutor = executor;
                }
            } finally {
                HTTP_REQUEST_EXECUTOR_LOCK.unlock();
            }
        }
        return executor;
    }

    @NonNull
    public static ExecutorService getImageLoaderExecutor() {
        ExecutorService executor = sImageLoaderExecutor;
        if (executor == null) {
            IMAGE_LOADER_EXECUTOR_LOCK.lock();
            try {
                executor = sImageLoaderExecutor;
                if (executor == null) {
                    executor = Executors.newFixedThreadPool(
                            Math.round(Runtime.getRuntime().availableProcessors() * 1.5F),
                            new AndroidWheelsThreadFactory(Thread.MIN_PRIORITY));
                    sImageLoaderExecutor = executor;
                }
            } finally {
                IMAGE_LOADER_EXECUTOR_LOCK.unlock();
            }
        }
        return executor;
    }

    @NonNull
    public static ExecutorService getStorageImageCacheExecutor() {
        ExecutorService executor = sStorageImageCacheExecutor;
        if (executor == null) {
            STORAGE_IMAGE_CACHE_EXECUTOR_LOCK.lock();
            try {
                executor = sStorageImageCacheExecutor;
                if (executor == null) {
                    executor = Executors.newSingleThreadExecutor(
                            new AndroidWheelsThreadFactory(Thread.MIN_PRIORITY));
                    sStorageImageCacheExecutor = executor;
                }
            } finally {
                STORAGE_IMAGE_CACHE_EXECUTOR_LOCK.unlock();
            }
        }
        return executor;
    }

    @NonNull
    public static ExecutorService getMainThreadExecutor() {
        ExecutorService executor = sMainThreadExecutor;
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