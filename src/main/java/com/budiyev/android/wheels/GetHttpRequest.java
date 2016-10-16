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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Instance class for GET HTTP requests
 */
final class GetHttpRequest extends HttpRequest {
    private final String mUrl;
    private final Iterable<HttpHeaderParameter> mHeaderParameters;
    private final Iterable<HttpQueryParameter> mQueryParameters;
    private final Iterable<HttpRequestCallback> mCallbacks;

    private final Callable<HttpRequestResult> mRequestAction = new Callable<HttpRequestResult>() {
        @Override
        public HttpRequestResult call() {
            HttpURLConnection connection = null;
            HttpRequestResult result = new HttpRequestResult();
            try {
                String query = mUrl;
                if (!CollectionUtils.isNullOrEmpty(mQueryParameters)) {
                    query += "?" + buildParamsUrlString(mQueryParameters, CHARSET_UTF_8);
                }
                connection = openHttpUrlConnection(query);
                connection.setDoInput(true);
                connection.setRequestMethod(REQUEST_METHOD_GET);
                connection.setRequestProperty(KEY_ACCEPT_CHARSET, CHARSET_UTF_8);
                if (!CollectionUtils.isNullOrEmpty(mHeaderParameters)) {
                    addHeaderParameters(connection, mHeaderParameters);
                }
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
                processResponse(connection, result);
            } catch (MalformedURLException e) {
                result.setResultType(HttpRequestResult.ERROR_MALFORMED_URL);
                result.setConnection(connection);
                result.setException(e);
            } catch (UnsupportedEncodingException e) {
                result.setResultType(HttpRequestResult.ERROR_UNSUPPORTED_ENCODING);
                result.setConnection(connection);
                result.setException(e);
            } catch (ProtocolException e) {
                result.setResultType(HttpRequestResult.ERROR_PROTOCOL);
                result.setConnection(connection);
                result.setException(e);
            } catch (IOException e) {
                result.setResultType(HttpRequestResult.ERROR_IO);
                result.setConnection(connection);
                result.setException(e);
            } catch (Exception e) {
                result.setResultType(HttpRequestResult.ERROR_UNEXPECTED);
                result.setConnection(connection);
                result.setException(e);
            }
            if (mCallbacks != null) {
                for (HttpRequestCallback callback : mCallbacks) {
                    if (callback != null) {
                        callback.onResult(result);
                    }
                }
            }
            return result;
        }
    };

    GetHttpRequest(@NonNull String url, @Nullable Iterable<HttpHeaderParameter> headerParameters,
            @Nullable Iterable<HttpQueryParameter> queryParameters,
            @Nullable Iterable<HttpRequestCallback> callbacks) {
        mUrl = Objects.requireNonNull(url);
        mHeaderParameters = headerParameters;
        mQueryParameters = queryParameters;
        mCallbacks = callbacks;
    }

    @NonNull
    @Override
    public Future<HttpRequestResult> submit() {
        return InternalExecutors.getHttpRequestExecutor().submit(mRequestAction);
    }

    @NonNull
    @Override
    public HttpRequestResult execute() {
        try {
            return mRequestAction.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int hashCode() {
        int hash = mUrl.hashCode();
        if (mHeaderParameters != null) {
            hash ^= mHeaderParameters.hashCode();
        }
        if (mQueryParameters != null) {
            hash ^= mQueryParameters.hashCode();
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof GetHttpRequest) {
            GetHttpRequest other = (GetHttpRequest) obj;
            return Objects.equals(mUrl, other.mUrl) &&
                    Objects.equals(mHeaderParameters, other.mHeaderParameters) &&
                    Objects.equals(mQueryParameters, other.mQueryParameters);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("HttpRequest GET [url: ").append(mUrl);
        if (mHeaderParameters != null) {
            stringBuilder.append("; header parameters: ").append(mHeaderParameters);
        }
        if (mQueryParameters != null) {
            stringBuilder.append("; query parameters: ").append(mQueryParameters);
        }
        return stringBuilder.append(']').toString();
    }
}
