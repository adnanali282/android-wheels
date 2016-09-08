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
import android.support.annotation.Nullable;

import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * Simple {@link Callable} with arguments
 */
public abstract class CallableTask<A, V> implements Callable<V> {
    private final A[] mArguments;

    /**
     * Task with arguments
     *
     * @param arguments task arguments
     */
    @SafeVarargs
    public CallableTask(@NonNull A... arguments) {
        mArguments = Objects.requireNonNull(arguments);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public final V call() throws Exception {
        return call(mArguments);
    }

    /**
     * Compute a result, or throw an exception if unable to do so.
     *
     * @param arguments task arguments, passed through constructor
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Nullable
    protected abstract V call(@NonNull A[] arguments) throws Exception;
}
