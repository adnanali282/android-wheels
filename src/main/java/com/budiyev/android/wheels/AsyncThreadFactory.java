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

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread factory for internal usage in AndroidWheels
 */
final class AsyncThreadFactory implements ThreadFactory {
    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(1);
    private static volatile String sThreadNamePrefix = "AndroidWheels-background-thread-";
    private final int mThreadPriority;
    private final boolean mDaemonThread;

    public AsyncThreadFactory() {
        this(Thread.NORM_PRIORITY, false);
    }

    public AsyncThreadFactory(
            @IntRange(from = Thread.MIN_PRIORITY, to = Thread.MAX_PRIORITY) int threadPriority) {
        this(threadPriority, false);
    }

    public AsyncThreadFactory(
            @IntRange(from = Thread.MIN_PRIORITY, to = Thread.MAX_PRIORITY) int threadPriority,
            boolean daemonThread) {
        mThreadPriority = threadPriority;
        mDaemonThread = daemonThread;
    }

    @Override
    public Thread newThread(@NonNull Runnable runnable) {
        THREAD_COUNTER.compareAndSet(Integer.MAX_VALUE, 1);
        Thread thread = new Thread(runnable, sThreadNamePrefix + THREAD_COUNTER.getAndIncrement());
        if (thread.getPriority() != mThreadPriority) {
            thread.setPriority(mThreadPriority);
        }
        if (thread.isDaemon() != mDaemonThread) {
            thread.setDaemon(mDaemonThread);
        }
        return thread;
    }

    /**
     * Accessible via {@link ThreadUtils#getBackgroundThreadNamePrefix()}
     */
    @NonNull
    public static String getThreadNamePrefix() {
        return sThreadNamePrefix;
    }

    /**
     * Accessible via {@link ThreadUtils#setBackgroundThreadNamePrefix(String)}
     */
    public static void setThreadNamePrefix(@NonNull String prefix) {
        sThreadNamePrefix = CommonUtils.requireNonNull(prefix);
    }
}
