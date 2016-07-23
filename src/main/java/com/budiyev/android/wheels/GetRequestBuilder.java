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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Builder for GET HTTP requests
 */
public final class GetRequestBuilder {
    private final String mUrl;
    private List<HeaderParameter> mHeaderParameters;
    private List<QueryParameter> mQueryParameters;
    private List<RequestCallback> mCallbacks;
    private int mDataType = RequestResult.STRING;

    GetRequestBuilder(@NonNull String url) {
        mUrl = url;
    }

    /**
     * Append HTTP header parameter
     */
    public GetRequestBuilder appendHeaderParameter(@NonNull String key, @NonNull String value) {
        if (mHeaderParameters == null) {
            mHeaderParameters = new ArrayList<>();
        }
        mHeaderParameters.add(HttpRequest.newHeaderParameter(key, value));
        return this;
    }

    /**
     * Append query string parameter
     */
    public GetRequestBuilder appendQueryParameter(@NonNull String key, @Nullable String value) {
        if (mQueryParameters == null) {
            mQueryParameters = new ArrayList<>();
        }
        mQueryParameters.add(HttpRequest.newQueryParameter(key, value));
        return this;
    }

    /**
     * Add request callback
     */
    public GetRequestBuilder addCallback(@NonNull RequestCallback callback) {
        if (mCallbacks == null) {
            mCallbacks = new ArrayList<>();
        }
        mCallbacks.add(Objects.requireNonNull(callback));
        return this;
    }

    /**
     * Set request result data type
     * Default - {@link RequestResult#STRING}
     */
    public GetRequestBuilder setDataType(@RequestResult.DataType int dataType) {
        mDataType = dataType;
        return this;
    }

    /**
     * Build request
     */
    @NonNull
    public HttpRequest build() {
        return new GetHttpRequest(mUrl, mHeaderParameters, mQueryParameters, mCallbacks, mDataType);
    }
}
