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

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Builder for POST HTTP requests
 */
public final class PostRequestBuilder {
    private final String mUrl;
    private List<HeaderParameter> mHeaderParameters;
    private List<QueryParameter> mQueryParameters;
    private List<PostParameter> mPostParameters;
    private List<RequestCallback> mCallbacks;
    private int mDataType = RequestResult.STRING;

    PostRequestBuilder(@NonNull String url) {
        mUrl = url;
    }

    /**
     * Append HTTP header parameter
     */
    public PostRequestBuilder appendHeaderParameter(@NonNull String key, @NonNull String value) {
        if (mHeaderParameters == null) {
            mHeaderParameters = new ArrayList<>();
        }
        mHeaderParameters.add(HttpRequest.newHeaderParameter(key, value));
        return this;
    }

    /**
     * Append query string parameter
     */
    public PostRequestBuilder appendQueryParameter(@NonNull String key, @Nullable String value) {
        if (mQueryParameters == null) {
            mQueryParameters = new ArrayList<>();
        }
        mQueryParameters.add(HttpRequest.newQueryParameter(key, value));
        return this;
    }

    /**
     * Append request body parameter
     */
    public PostRequestBuilder appendPostParameter(@NonNull String key, @NonNull String value) {
        if (mPostParameters == null) {
            mPostParameters = new ArrayList<>();
        }
        mPostParameters.add(HttpRequest.newPostParameter(key, value));
        return this;
    }

    /**
     * Append request body parameter
     */
    public PostRequestBuilder appendPostParameter(@NonNull String key, @NonNull File file) {
        if (mPostParameters == null) {
            mPostParameters = new ArrayList<>();
        }
        mPostParameters.add(HttpRequest.newPostParameter(key, file));
        return this;
    }

    /**
     * Append request body parameter
     */
    public PostRequestBuilder appendPostParameter(@NonNull String key,
            @NonNull InputStream inputStream, @NonNull String fileName,
            @NonNull String contentType) {
        if (mPostParameters == null) {
            mPostParameters = new ArrayList<>();
        }
        mPostParameters.add(HttpRequest.newPostParameter(key, inputStream, fileName, contentType));
        return this;
    }

    /**
     * Add request callback
     */
    public PostRequestBuilder addCallback(@NonNull RequestCallback callback) {
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
    public PostRequestBuilder setDataType(@RequestResult.DataType int dataType) {
        mDataType = dataType;
        return this;
    }

    /**
     * Build request
     */
    @NonNull
    public HttpRequest build() {
        return new PostHttpRequest(mUrl, mHeaderParameters, mQueryParameters, mPostParameters,
                mCallbacks, mDataType);
    }
}
