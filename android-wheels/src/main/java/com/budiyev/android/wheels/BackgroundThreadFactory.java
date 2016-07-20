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

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

class BackgroundThreadFactory implements ThreadFactory {
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
        Thread thread = new Thread(runnable, mThreadNamePrefix + mThreadCounter.getAndIncrement());
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
