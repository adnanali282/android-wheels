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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.Future;

/**
 * HTTP request
 */
public abstract class HttpRequest {
    HttpRequest() {
    }

    @NonNull
    protected static String buildParamsUrlString(@NonNull Iterable<QueryParameter> params,
            @NonNull String charset) throws UnsupportedEncodingException {
        StringBuilder dataBuilder = new StringBuilder();
        boolean firstEntry = true;
        for (QueryParameter queryParameter : params) {
            if (firstEntry) {
                firstEntry = false;
            } else {
                dataBuilder.append("&");
            }
            if (queryParameter.key != null) {
                dataBuilder.append(URLEncoder.encode(queryParameter.key, charset));
                if (queryParameter.value != null) {
                    dataBuilder.append("=");
                    dataBuilder.append(URLEncoder.encode(queryParameter.value, charset));
                }
            }
        }
        return dataBuilder.toString();
    }

    /**
     * Maximum number of requests that can be executed simultaneously
     * by calling {@link HttpRequest#submit()}
     */
    public static int getParallelRequestsLimit() {
        return ExecutorUtils.getHttpRequestMaximumThreadPoolSize();
    }

    /**
     * Maximum number of requests that can be executed simultaneously
     * by calling {@link HttpRequest#submit()}
     */
    public static void setParallelRequestsLimit(int limit) {
        ExecutorUtils.setHttpRequestMaximumThreadPoolSize(limit);
    }

    /**
     * Create new GET HTTP request
     *
     * @param url              URL-address
     * @param headerParameters Request header parameters
     * @param queryParameters  Query string parameters
     * @param callbacks        Response callbacks
     * @param resultType       Type of request result in callback
     */
    @NonNull
    public static HttpRequest newGetRequest(@NonNull String url,
            @Nullable Iterable<HeaderParameter> headerParameters,
            @Nullable Iterable<QueryParameter> queryParameters,
            @Nullable Iterable<RequestCallback> callbacks, @NonNull RequestResultType resultType) {
        return new GetHttpRequest(url, headerParameters, queryParameters, callbacks, resultType);
    }

    /**
     * Create new GET HTTP request builder
     *
     * @param url URL-address
     * @return New GET request builder instance
     */
    @NonNull
    public static GetRequestBuilder newGetBuilder(@NonNull String url) {
        return new GetRequestBuilder(url);
    }

    /**
     * Create new POST HTTP request
     *
     * @param url              URL-address
     * @param headerParameters Request header parameters
     * @param queryParameters  Query string parameters
     * @param postParameters   Request body multipart/form-data parameters
     * @param callbacks        Response callbacks
     * @param resultType       Type of request result in callback
     */
    @NonNull
    public static HttpRequest newPostRequest(@NonNull String url,
            @Nullable Iterable<HeaderParameter> headerParameters,
            @Nullable Iterable<QueryParameter> queryParameters,
            @Nullable Iterable<PostParameter> postParameters,
            @Nullable Iterable<RequestCallback> callbacks, @NonNull RequestResultType resultType) {
        return new PostHttpRequest(url, headerParameters, queryParameters, postParameters,
                callbacks, resultType);
    }

    /**
     * Create new POST HTTP request builder
     *
     * @param url URL-address
     * @return New POST request instance
     */
    @NonNull
    public static PostRequestBuilder newPostBuilder(@NonNull String url) {
        return new PostRequestBuilder(url);
    }

    /**
     * Create new request header parameter
     *
     * @param key   Key
     * @param value Value
     * @return New request header parameter
     */
    @NonNull
    public static HeaderParameter newHeaderParameter(@NonNull String key, @Nullable String value) {
        HeaderParameter headerParameter = new HeaderParameter();
        headerParameter.key = key;
        headerParameter.value = value;
        return headerParameter;
    }

    /**
     * Create new query string request parameter
     *
     * @param key   Key
     * @param value Value
     * @return New query string request parameter
     */
    @NonNull
    public static QueryParameter newQueryParameter(@NonNull String key, @Nullable String value) {
        QueryParameter queryParameter = new QueryParameter();
        queryParameter.key = key;
        queryParameter.value = value;
        return queryParameter;
    }

    /**
     * Create new POST  request body parameter
     *
     * @param key   Key
     * @param value Value
     * @return New POST request body parameter
     */
    @NonNull
    public static PostParameter newPostParameter(@NonNull String key, @NonNull String value) {
        PostParameter postParameter = new PostParameter();
        postParameter.key = key;
        postParameter.value = value;
        return postParameter;
    }

    /**
     * Create new POST  request body parameter
     *
     * @param key  Key
     * @param file File
     * @return New POST request body parameter
     */
    @NonNull
    public static PostParameter newPostParameter(@NonNull String key, @NonNull File file) {
        PostParameter postParameter = new PostParameter();
        postParameter.key = key;
        postParameter.file = file;
        return postParameter;
    }

    /**
     * Create new POST  request body parameter
     *
     * @param key         Key
     * @param inputStream Input stream
     * @param fileName    File name
     * @param contentType Content type
     * @return New POST request body parameter
     */
    @NonNull
    public static PostParameter newPostParameter(@NonNull String key,
            @NonNull InputStream inputStream, @NonNull String fileName,
            @NonNull String contentType) {
        PostParameter postParameter = new PostParameter();
        postParameter.key = key;
        postParameter.stream = inputStream;
        postParameter.fileName = fileName;
        postParameter.contentType = contentType;
        return postParameter;
    }

    /**
     * Submit request for asynchronous execution
     *
     * @return a {@link Future} representing pending completion of the request
     */
    @NonNull
    public abstract Future<RequestResult> submit();

    /**
     * Execute request immediately
     *
     * @return Request result
     */
    @NonNull
    public abstract RequestResult execute();
}
