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

public abstract class AsyncLoader<T> extends Loader<T> {
    private final Bundle mArguments;
    private volatile LoadTask mLoadTask;

    public AsyncLoader(@NonNull Context context, @Nullable Bundle arguments) {
        super(context);
        mArguments = arguments;
    }

    @Nullable
    protected abstract T load(@Nullable Bundle arguments, @NonNull TaskState state);

    @Override
    protected void onStartLoading() {
        LoadTask loadTask = mLoadTask;
        if (loadTask != null && loadTask.loaded) {
            loadTask.state.setAbandoned(false);
            loadTask.state.setCancelled(false);
            loadTask.state.setStopped(false);
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
            loadTask.state.setStopped(true);
        }
    }

    @Override
    protected void onAbandon() {
        LoadTask loadTask = mLoadTask;
        if (loadTask != null) {
            if (loadTask.future != null) {
                loadTask.future.cancel(false);
            }
            loadTask.state.setAbandoned(true);
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
        if (loadTask == null || loadTask.state.isCancelled()) {
            return false;
        }
        if (loadTask.future != null) {
            loadTask.future.cancel(false);
        }
        loadTask.state.setCancelled(true);
        return true;
    }

    protected static final class TaskState {
        private volatile boolean mAbandoned;
        private volatile boolean mCancelled;
        private volatile boolean mStopped;

        public boolean isAbandoned() {
            return mAbandoned;
        }

        public boolean isCancelled() {
            return mCancelled;
        }

        public boolean isStopped() {
            return mStopped;
        }

        private void setAbandoned(boolean abandoned) {
            mAbandoned = abandoned;
        }

        private void setCancelled(boolean cancelled) {
            mCancelled = cancelled;
        }

        private void setStopped(boolean stopped) {
            mStopped = stopped;
        }
    }

    private class LoadTask implements Runnable {
        public final TaskState state = new TaskState();
        public volatile Future<?> future;
        public volatile T data;
        public volatile boolean loaded;

        @Override
        public void run() {
            final T localData = load(mArguments, state);
            data = localData;
            loaded = true;
            if (state.isAbandoned()) {
                return;
            }
            ThreadUtils.runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (state.isCancelled()) {
                        deliverCancellation();
                    } else {
                        deliverResult(localData);
                    }
                }
            });
        }
    }
}
