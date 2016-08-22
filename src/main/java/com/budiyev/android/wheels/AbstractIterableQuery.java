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

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Abstract class for {@link IterableQuery}
 * <br>
 * Contains internal infrastructure
 */
abstract class AbstractIterableQuery<T> implements Iterable<T> {
    private final Queue<Runnable> mTasksQueue = new LinkedList<>();
    private Iterable<T> mIterable;
    private boolean mIterableMutable;

    /**
     * {@inheritDoc}
     */
    @Override
    public final Iterator<T> iterator() {
        return executeTasks().iterator();
    }

    @NonNull
    protected final Iterable<T> getIterable() {
        return mIterable;
    }

    protected final void setImmutableIterable(@NonNull Iterable<T> iterable) {
        mIterable = iterable;
        mIterableMutable = false;
    }

    @NonNull
    protected final List<T> getMutableIterable() {
        Iterable<T> iterable = mIterable;
        if (mIterableMutable) {
            return (List<T>) iterable;
        } else {
            List<T> mutableList = CollectionUtils.copy(iterable);
            mIterable = mutableList;
            mIterableMutable = true;
            return mutableList;
        }
    }

    protected final void setMutableIterable(@NonNull List<T> mutableList) {
        mIterable = mutableList;
        mIterableMutable = true;
    }

    protected final void clearMutableIterable() {
        if (mIterableMutable) {
            ((List<T>) mIterable).clear();
        }
    }

    protected final void enqueueTask(@NonNull Runnable task) {
        mTasksQueue.offer(task);
    }

    @NonNull
    protected final Iterable<T> executeTasks() {
        for (Runnable task = mTasksQueue.poll(); task != null; task = mTasksQueue.poll()) {
            task.run();
        }
        return mIterable;
    }
}
