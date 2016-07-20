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

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

public abstract class HttpRequest implements Runnable {
    protected static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
        @NonNull
        @Override
        public Thread newThread(@NonNull Runnable runnable) {
            Thread thread = new Thread(runnable, "HttpRequest-background-thread");
            if (thread.isDaemon()) {
                thread.setDaemon(false);
            }
            if (thread.getPriority() != Thread.NORM_PRIORITY) {
                thread.setPriority(Thread.NORM_PRIORITY);
            }
            return thread;
        }
    };
    protected static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(THREAD_FACTORY);

    @NonNull
    protected static String buildParamsUrlString(@NonNull Collection<QueryParameter> params,
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
     * Create new GET HTTP request
     *
     * @param url              URL-address
     * @param headerParameters Request header parameters
     * @param queryParameters  Query string parameters
     * @param resultType       Type of request result in callback
     * @param callback         Response callback
     * @return New GET request instance
     */
    @NonNull
    public static GetHttpRequest newGetRequest(@NonNull String url,
            @Nullable Collection<HeaderParameter> headerParameters,
            @Nullable Collection<QueryParameter> queryParameters, @NonNull ResultType resultType,
            @Nullable RequestCallback callback) {
        return new GetHttpRequest(url, headerParameters, queryParameters, resultType, callback);
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
     * @param resultType       Type of request result in callback
     * @param callback         Response callback
     * @return New GET request instance
     */
    @NonNull
    public static PostHttpRequest newPostRequest(@NonNull String url,
            @Nullable Collection<HeaderParameter> headerParameters,
            @Nullable Collection<QueryParameter> queryParameters,
            @Nullable Collection<PostParameter> postParameters, @NonNull ResultType resultType,
            @Nullable RequestCallback callback) {
        return new PostHttpRequest(url, headerParameters, queryParameters, postParameters,
                resultType, callback);
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

    @NonNull
    public abstract Future<RequestResult> execute();

    @NonNull
    public abstract RequestResult getResult();

    @NonNull
    public abstract RequestResult executeAndGetResult();

}
