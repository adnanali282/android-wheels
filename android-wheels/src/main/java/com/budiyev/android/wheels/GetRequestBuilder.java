/**
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2016 Yuriy Budiyev [yuriy.budiyev@yandex.ru]
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p/>
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

/**
 * Builder for GET HTTP requests
 */
public final class GetRequestBuilder {
    private final String mUrl;
    private ArrayList<HeaderParameter> mHeaderParameters;
    private ArrayList<QueryParameter> mQueryParameters;
    private RequestResultType mResultType = RequestResultType.STRING;
    private RequestCallback mCallback;

    GetRequestBuilder(@NonNull String url) {
        mUrl = url;
    }

    public GetRequestBuilder appendHeaderParameter(@NonNull String key, @NonNull String value) {
        if (mHeaderParameters == null) {
            mHeaderParameters = new ArrayList<>();
        }
        mHeaderParameters.add(HttpRequest.newHeaderParameter(key, value));
        return this;
    }

    public GetRequestBuilder appendQueryParameter(@NonNull String key, @Nullable String value) {
        if (mQueryParameters == null) {
            mQueryParameters = new ArrayList<>();
        }
        mQueryParameters.add(HttpRequest.newQueryParameter(key, value));
        return this;
    }

    public GetRequestBuilder setResultType(@NonNull RequestResultType resultType) {
        mResultType = resultType;
        return this;
    }

    public GetRequestBuilder setCallback(@Nullable RequestCallback callback) {
        mCallback = callback;
        return this;
    }

    @NonNull
    public GetHttpRequest build() {
        return new GetHttpRequest(mUrl, mHeaderParameters, mQueryParameters, mResultType,
                mCallback);
    }
}
