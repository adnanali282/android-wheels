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

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.util.concurrent.Future;

/**
 * Abstract Loader based on {@link ThreadUtils}
 * <br>
 * See {@link Loader} and {@link LoaderManager} for more details.
 *
 * @param <A> Arguments type
 * @param <D> Data type
 */
public abstract class AsyncLoader<A, D> extends Loader<D> {
    private final A mArguments;
    private volatile LoadTask mLoadTask;

    /**
     * AsyncLoader
     * <br>
     * Stores away the application context associated with {@code context}.
     * Since Loaders can be used across multiple activities it's dangerous to
     * store the context directly; always use {@link #getContext} to retrieve
     * the Loader's context, don't use the constructor argument directly.
     * The context returned by {@link #getContext} is safe to use across
     * Activity instances.
     *
     * @param context   context
     * @param arguments arguments that will be transferred to {@link #load} method
     */
    public AsyncLoader(@NonNull Context context, @Nullable A arguments) {
        super(context);
        mArguments = arguments;
    }

    /**
     * Load data asynchronously
     * <br>
     * Implementations should not deliver the result directly, but should return it
     * from this method, which will eventually end up calling {@link #deliverResult} or,
     * if loading was cancelled, {@link #deliverCancellation} on the main thread.
     * If implementations need to process the results on the main thread
     * they may override {@link #deliverResult} and do so there.
     * <br>
     * To support cancellation, this method should periodically check {@code state}
     * parameter's values: {@link LoadState#isAbandoned}, {@link LoadState#isCancelled}
     * and {@link LoadState#isStopped}.
     *
     * @param arguments arguments, transferred through constructor
     * @param state     current loading state
     * @return loaded data
     */
    @Nullable
    @WorkerThread
    protected abstract D load(@Nullable A arguments, @NonNull LoadState state);

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
            cancelFuture(loadTask);
            loadTask.state.stopped = true;
        }
    }

    @Override
    protected void onAbandon() {
        LoadTask loadTask = mLoadTask;
        if (loadTask != null) {
            cancelFuture(loadTask);
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
        cancelFuture(loadTask);
        loadTask.state.cancelled = true;
        return true;
    }

    private void cancelFuture(@NonNull LoadTask loadTask) {
        if (loadTask.future != null) {
            loadTask.future.cancel(false);
        }
    }

    /**
     * Loading state
     */
    protected static final class LoadState {
        private volatile boolean abandoned;
        private volatile boolean cancelled;
        private volatile boolean stopped;

        private LoadState() {
        }

        /**
         * Whether if loading was abandoned
         * <br>
         * Bound with {@link AsyncLoader#abandon}
         */
        public boolean isAbandoned() {
            return abandoned;
        }

        /**
         * Whether if loading was cancelled
         * <br>
         * Bound with {@link AsyncLoader#cancelLoad}
         */
        public boolean isCancelled() {
            return cancelled;
        }

        /**
         * Whether if loading was stopped
         * <br>
         * Bound with {@link AsyncLoader#stopLoading}
         */
        public boolean isStopped() {
            return stopped;
        }
    }

    private class LoadTask implements Runnable {
        private final LoadState state = new LoadState();
        private volatile Future<?> future;
        private volatile D data;
        private volatile boolean loaded;

        @Override
        public void run() {
            final D localData = load(mArguments, state);
            data = localData;
            loaded = !state.abandoned && !state.cancelled && !state.stopped;
            if (state.abandoned || state.stopped) {
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
