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

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Executor for the main (UI) thread tasks in {@link ThreadUtils}
 */
final class MainThreadExecutor extends AbstractExecutorService {
    private final Handler mHandler;
    private final Thread mThread;

    public MainThreadExecutor() {
        Looper mainLooper = Looper.getMainLooper();
        mHandler = new Handler(mainLooper);
        mThread = mainLooper.getThread();
    }

    @Override
    public void execute(@NonNull Runnable task) {
        if (Thread.currentThread() == mThread) {
            wrapTask(task).run();
        } else {
            execute(task, 0);
        }
    }

    public void execute(@NonNull Runnable task, long delay) {
        mHandler.postDelayed(wrapTask(task), delay);
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

    private void afterExecute(Runnable runnable, Throwable throwable) {
        ThreadUtils.throwExecutionExceptionIfNeeded(runnable, throwable);
    }

    private Runnable wrapTask(@NonNull final Runnable task) {
        return new Runnable() {
            @Override
            public void run() {
                Throwable error = null;
                try {
                    task.run();
                } catch (Throwable t) {
                    error = t;
                } finally {
                    afterExecute(task, error);
                }
            }
        };
    }
}
