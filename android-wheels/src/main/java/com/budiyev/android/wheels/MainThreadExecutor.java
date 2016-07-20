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
class MainThreadExecutor extends AbstractExecutorService {
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
