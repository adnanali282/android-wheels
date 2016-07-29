package com.budiyev.android.wheels;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

final class AndroidWheelsThreadFactory implements ThreadFactory {
    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(1);
    private static volatile String sThreadNamePrefix = "AndroidWheels-background-thread-";
    private final int mThreadPriority;
    private final boolean mDaemonThread;

    public AndroidWheelsThreadFactory() {
        this(Thread.NORM_PRIORITY, false);
    }

    public AndroidWheelsThreadFactory(
            @IntRange(from = Thread.MIN_PRIORITY, to = Thread.MAX_PRIORITY) int threadPriority) {
        this(threadPriority, false);
    }

    public AndroidWheelsThreadFactory(
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
     * Can be accessed via {@link ThreadUtils#getBackgroundThreadNamePrefix()}
     */
    @NonNull
    public static String getThreadNamePrefix() {
        return sThreadNamePrefix;
    }

    /**
     * Can be accessed via {@link ThreadUtils#setBackgroundThreadNamePrefix(String)}
     */
    public static void setThreadNamePrefix(@NonNull String prefix) {
        sThreadNamePrefix = Objects.requireNonNull(prefix);
    }
}
