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

import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AsyncLoader<T> extends Loader<T> {
    private final Lock mLock = new ReentrantLock();
    private final Bundle mArguments;
    private volatile LoadTask mLoadTask;

    public AsyncLoader(@NonNull Context context, @Nullable Bundle arguments) {
        super(context);
        mArguments = arguments;
    }

    @Nullable
    protected abstract T load(@Nullable Bundle arguments);

    @Override
    protected void onStartLoading() {
        mLock.lock();
        try {
            LoadTask loadTask = mLoadTask;
            if (loadTask != null && loadTask.loaded && !loadTask.cancelled) {
                deliverResult(loadTask.data);
            } else {
                cancelCurrentLoadTask();
                startNewLoadTask();
            }
        } finally {
            mLock.unlock();
        }
    }

    @Override
    protected boolean onCancelLoad() {
        mLock.lock();
        try {
            return cancelCurrentLoadTask();
        } finally {
            mLock.unlock();
        }
    }

    @Override
    protected void onForceLoad() {
        mLock.lock();
        try {
            cancelCurrentLoadTask();
            startNewLoadTask();
        } finally {
            mLock.unlock();
        }
    }

    @Override
    protected void onStopLoading() {
    }

    @Override
    protected void onAbandon() {
        mLock.lock();
        try {
            LoadTask loadTask = mLoadTask;
            if (loadTask != null && loadTask.future != null) {
                loadTask.future.cancel(false);
            }
        } finally {
            mLock.unlock();
        }
    }

    @Override
    protected void onReset() {
        mLoadTask = null;
    }

    private void startNewLoadTask() {
        LoadTask loadTask = new LoadTask();
        loadTask.future = ThreadUtils.runAsync(loadTask);
        mLoadTask = loadTask;
    }

    private boolean cancelCurrentLoadTask() {
        LoadTask loadTask = mLoadTask;
        if (loadTask == null) {
            return false;
        }
        loadTask.cancelled = true;
        return true;
    }

    private class LoadTask implements Runnable {
        public volatile Future<?> future;
        public volatile boolean cancelled;
        public volatile boolean loaded;
        public volatile T data;

        @Override
        public void run() {
            data = load(mArguments);
            loaded = true;
            if (isAbandoned()) {
                return;
            }
            ThreadUtils.runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (cancelled) {
                        deliverCancellation();
                    } else {
                        deliverResult(data);
                    }
                }
            });
        }
    }
}
