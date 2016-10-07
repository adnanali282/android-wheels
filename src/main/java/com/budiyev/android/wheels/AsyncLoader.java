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

/**
 * {@link Loader} implementation, based on {@link ThreadUtils}
 */
public abstract class AsyncLoader<T> extends Loader<T> {
    private final Bundle mArguments;
    private volatile LoadTask mLoadTask;

    /**
     * AsyncLoader
     *
     * @param context   Context
     * @param arguments Arguments
     */
    public AsyncLoader(@NonNull Context context, @Nullable Bundle arguments) {
        super(context);
        mArguments = arguments;
    }

    /**
     * Load data asynchronously
     *
     * @param arguments arguments
     * @param state     current loading state
     * @return loaded data
     */
    @Nullable
    protected abstract T load(@Nullable Bundle arguments, @NonNull LoadState state);

    @Override
    protected void onStartLoading() {
        LoadTask loadTask = mLoadTask;
        if (loadTask != null && loadTask.loaded) {
            deliverResult(loadTask.data);
        } else {
            cancelCurrentLoadTask();
            startNewLoadTask();
        }
    }

    @Override
    protected boolean onCancelLoad() {
        return cancelCurrentLoadTask();
    }

    @Override
    protected void onForceLoad() {
        cancelCurrentLoadTask();
        startNewLoadTask();
    }

    @Override
    protected void onStopLoading() {
        LoadTask loadTask = mLoadTask;
        if (loadTask != null) {
            if (loadTask.future != null) {
                loadTask.future.cancel(false);
            }
            loadTask.state.stopped = true;
        }
    }

    @Override
    protected void onAbandon() {
        LoadTask loadTask = mLoadTask;
        if (loadTask != null) {
            if (loadTask.future != null) {
                loadTask.future.cancel(false);
            }
            loadTask.state.abandoned = true;
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
        if (loadTask == null || loadTask.state.cancelled) {
            return false;
        }
        if (loadTask.future != null) {
            loadTask.future.cancel(false);
        }
        loadTask.state.cancelled = true;
        return true;
    }

    /**
     * Loading state
     */
    protected static final class LoadState {
        private volatile boolean abandoned;
        private volatile boolean cancelled;
        private volatile boolean stopped;

        /**
         * Whether if loading was abandoned
         * <br>
         * Bound with {@link Loader#abandon()}
         */
        public boolean isAbandoned() {
            return abandoned;
        }

        /**
         * Whether if loading was cancelled
         * <br>
         * Bound with {@link Loader#cancelLoad()}
         */
        public boolean isCancelled() {
            return cancelled;
        }

        /**
         * Whether if loading was stopped
         * <br>
         * Bound with {@link Loader#stopLoading()}
         */
        public boolean isStopped() {
            return stopped;
        }
    }

    private class LoadTask implements Runnable {
        private final LoadState state = new LoadState();
        private volatile Future<?> future;
        private volatile T data;
        private volatile boolean loaded;

        @Override
        public void run() {
            final T localData = load(mArguments, state);
            data = localData;
            loaded = !state.abandoned && !state.cancelled && !state.stopped;
            if (state.abandoned) {
                return;
            }
            ThreadUtils.runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (state.cancelled) {
                        deliverCancellation();
                    } else {
                        deliverResult(localData);
                    }
                }
            });
        }
    }
}
