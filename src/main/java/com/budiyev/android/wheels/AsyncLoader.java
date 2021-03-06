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

import java.util.concurrent.Future;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

/**
 * Abstract Loader based on {@link ThreadUtils}
 * <br>
 * See {@link Loader} and {@link LoaderManager} for more details.
 *
 * @param <A> Arguments type, {@link Bundle} in most cases
 * @param <D> Data type
 */
public abstract class AsyncLoader<A, D> extends Loader<D> {
    private volatile LoadTask mLoadTask;
    private volatile A mArguments;
    private volatile boolean mForceReload;

    /**
     * AsyncLoader
     * <br>
     * Stores away the application context associated with {@code context}.
     * Since Loaders can be used across multiple activities it's dangerous to
     * store the context directly; always use {@link #getContext()} to retrieve
     * the Loader's context, don't use the constructor argument directly.
     * The context returned by {@link #getContext()} is safe to use across
     * Activity instances.
     *
     * @param context context
     * @see #setArguments(Object)
     */
    public AsyncLoader(@NonNull Context context) {
        super(context);
    }

    /**
     * AsyncLoader
     * <br>
     * Stores away the application context associated with {@code context}.
     * Since Loaders can be used across multiple activities it's dangerous to
     * store the context directly; always use {@link #getContext()} to retrieve
     * the Loader's context, don't use the constructor argument directly.
     * The context returned by {@link #getContext()} is safe to use across
     * Activity instances.
     *
     * @param context   context
     * @param arguments arguments that will be transferred to the
     *                  {@link #load(Object, LoadState)} method
     */
    public AsyncLoader(@NonNull Context context, @Nullable A arguments) {
        super(context);
        mArguments = arguments;
    }

    /**
     * Get {@code arguments}, that will be transferred to the
     * {@link #load(Object, LoadState)} method
     */
    @Nullable
    public A getArguments() {
        return mArguments;
    }

    /**
     * Set {@code arguments} that will be transferred to the
     * {@link #load(Object, LoadState)} method
     */
    public void setArguments(@Nullable A arguments) {
        mArguments = arguments;
    }

    /**
     * Set whether to force data reload on each {@link #startLoading()} call,
     * {@code false} by default
     */
    public void setForceReload(boolean forceReload) {
        mForceReload = forceReload;
    }

    /**
     * Check whether to force data reload on each {@link #startLoading()} call,
     * {@code false} by default
     */
    public boolean isForceReload() {
        return mForceReload;
    }

    /**
     * Loaded data
     * <br>
     * Note that returned {@code null} doesn't guarantees that loading hasn't
     * started or is still in progress; {@code null} can also be returned by
     * {@link #load(Object, LoadState)} method. So, this method can't be used to
     * check loading state.
     *
     * @return data, if it has already been loaded or {@code null} otherwise
     */
    @Nullable
    public D getLoadedData() {
        LoadTask loadTask = mLoadTask;
        if (loadTask != null && loadTask.loaded) {
            return loadTask.data;
        } else {
            return null;
        }
    }

    /**
     * Load data asynchronously
     * <br>
     * This method will be called on a worker thread.
     * <br>
     * Implementations should not deliver the result directly, but should return it
     * from this method, which will eventually end up calling {@link #deliverResult(Object)} or,
     * if {@link #cancelLoad()} was called, {@link #deliverCancellation()} on the main thread.
     * If {@link #abandon()} or {@link #stopLoading()} was called, no callbacks will be delivered.
     * If {@link #forceLoad()} called after the task was started, no callbacks will be delivered
     * for current task and loaded data can't be forced to be delivered on next
     * {@link #startLoading()} call.
     * If implementations need to process the results on the main thread
     * they may override {@link #deliverResult(Object)} and do so there.
     * <br>
     * To support cancellation, this method should periodically check {@code loadState}
     * parameter's values: {@link LoadState#isAbandoned()}, {@link LoadState#isCancelled()},
     * {@link LoadState#isStopped()}, {@link LoadState#isForcedStop()} or
     * {@link LoadState#shouldStopLoading()}.
     *
     * @param arguments arguments, transferred through constructor
     * @param loadState current loading state
     * @return loaded data
     */
    @Nullable
    @WorkerThread
    protected abstract D load(@Nullable A arguments, @NonNull LoadState loadState);

    @Override
    protected void onStartLoading() {
        LoadTask loadTask = mLoadTask;
        if (loadTask == null) {
            startNewLoadTask();
        } else if (!mForceReload && loadTask.loaded) {
            deliverResult(loadTask.data);
        } else {
            forceLoad();
        }
    }

    @Override
    protected boolean onCancelLoad() {
        LoadTask loadTask = mLoadTask;
        if (loadTask == null || loadTask.state.cancelled) {
            return false;
        }
        loadTask.state.cancelled = true;
        return true;
    }

    @Override
    protected void onForceLoad() {
        LoadTask loadTask = mLoadTask;
        if (loadTask != null) {
            loadTask.state.forcedStop = true;
            cancelFuture(loadTask);
        }
        startNewLoadTask();
    }

    @Override
    protected void onStopLoading() {
        LoadTask loadTask = mLoadTask;
        if (loadTask != null) {
            loadTask.state.stopped = true;
            cancelFuture(loadTask);
        }
    }

    @Override
    protected void onAbandon() {
        LoadTask loadTask = mLoadTask;
        if (loadTask != null) {
            loadTask.state.abandoned = true;
            cancelFuture(loadTask);
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

    private void cancelFuture(@NonNull LoadTask loadTask) {
        Future<?> future = loadTask.future;
        if (future != null) {
            future.cancel(false);
        }
    }

    /**
     * Loading state
     * <br>
     * Represents loading state for concrete {@link AsyncLoader#load(Object, LoadState)}
     * method call
     */
    public static final class LoadState {
        private volatile boolean abandoned;
        private volatile boolean cancelled;
        private volatile boolean stopped;
        private volatile boolean forcedStop;
        private volatile boolean forceLoaded;

        private LoadState() {
        }

        /**
         * Whether if loading was abandoned
         * <br>
         * Bound with {@link AsyncLoader#abandon()}
         */
        public boolean isAbandoned() {
            return abandoned;
        }

        /**
         * Whether if loading was cancelled
         * <br>
         * Bound with {@link AsyncLoader#cancelLoad()}
         */
        public boolean isCancelled() {
            return cancelled;
        }

        /**
         * Whether if loading was stopped
         * <br>
         * Bound with {@link AsyncLoader#stopLoading()}
         */
        public boolean isStopped() {
            return stopped;
        }

        /**
         * Whether if current loading process was cancelled by
         * calling {@link AsyncLoader#forceLoad()}
         */
        public boolean isForcedStop() {
            return forcedStop;
        }

        /**
         * Convenience method to check if loading process should be sopped
         *
         * @return {@code true} if one of {@link #isAbandoned()}, {@link #isCancelled()},
         * {@link #isStopped()} or {@link #isForcedStop()} returns {@code true},
         * {@code false} otherwise.
         */
        public boolean shouldStopLoading() {
            return abandoned || cancelled || stopped || forcedStop;
        }

        /**
         * Tell the loader that despite abandoning, cancelling or stopping,
         * the data is loaded normally and ready to be delivered immediately
         * on next {@link AsyncLoader#startLoading()} call.
         * <br>
         * This flag will be ignored if {@link #isForcedStop()} return {@code true}.
         *
         * @param force whether to force or not, {@code false} by default
         */
        public void setForceLoaded(boolean force) {
            forceLoaded = force;
        }
    }

    private class LoadTask implements Runnable {
        private final LoadState state = new LoadState();
        private volatile Future<?> future;
        private volatile D data;
        private volatile boolean loaded;

        @Override
        @WorkerThread
        public void run() {
            final D localData = load(mArguments, state);
            boolean loadInterrupted = state.abandoned || state.stopped || state.forcedStop;
            loaded = !state.cancelled && !loadInterrupted || state.forceLoaded && !state.forcedStop;
            if (loaded) {
                data = localData;
            }
            if (loadInterrupted) {
                return;
            }
            ThreadUtils.runOnMainThread(new Runnable() {
                @Override
                @MainThread
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
