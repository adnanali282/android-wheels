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
    private volatile Future<?> mFuture;

    public AsyncLoader(@NonNull Context context, @Nullable Bundle arguments) {
        super(context);
        mArguments = arguments;
    }

    @Nullable
    protected abstract T load(@Nullable Bundle arguments);

    @Override
    protected void onStartLoading() {
    }

    @Override
    protected boolean onCancelLoad() {
        Future<?> future = mFuture;
        return future != null && future.cancel(false);
    }

    @Override
    protected void onForceLoad() {
        mFuture = ThreadUtils.runAsync(new LoadTask());
    }

    @Override
    protected void onStopLoading() {
    }

    @Override
    protected void onAbandon() {
    }

    @Override
    protected void onReset() {
    }

    private class LoadTask implements Runnable {
        @Override
        public void run() {
            final T data = load(mArguments);
            ThreadUtils.runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    deliverResult(data);
                }
            });
        }
    }
}
