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

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * HTTP request
 */
public abstract class HttpRequest {
    protected static final String CHARSET_UTF_8 = "UTF-8";
    protected static final String REQUEST_METHOD_GET = "GET";
    protected static final String REQUEST_METHOD_POST = "POST";
    protected static final String KEY_ACCEPT_CHARSET = "Accept-Charset";
    protected static final int CONNECTION_TIMEOUT = 10000;

    HttpRequest() {
    }

    @NonNull
    protected static String buildParamsUrlString(@NonNull Iterable<HttpQueryParameter> params,
            @NonNull String charset) throws UnsupportedEncodingException {
        StringBuilder dataBuilder = new StringBuilder();
        boolean firstEntry = true;
        for (HttpQueryParameter queryParameter : params) {
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

    @NonNull
    protected static HttpURLConnection openHttpUrlConnection(@NonNull String query) throws
            IOException {
        URLConnection connection = new URL(query).openConnection();
        if (connection instanceof HttpURLConnection) {
            return (HttpURLConnection) connection;
        } else {
            throw new ProtocolException("Not HTTP/HTTPS");
        }
    }

    protected static void addHeaderParameters(@NonNull HttpURLConnection connection,
            @NonNull Iterable<HttpHeaderParameter> parameters) {
        for (HttpHeaderParameter parameter : parameters) {
            connection.addRequestProperty(parameter.key, parameter.value);
        }
    }

    protected static void processResponse(@NonNull HttpURLConnection connection,
            @NonNull HttpRequestResult result) throws IOException {
        InputStream dataStream;
        try {
            dataStream = connection.getInputStream();
            result.setResultType(HttpRequestResult.SUCCESS);
        } catch (IOException e) {
            dataStream = connection.getErrorStream();
            result.setResultType(HttpRequestResult.ERROR_HTTP);
            result.setException(e);
        }
        result.setDataStream(dataStream);
        result.setConnection(connection);
        result.setHeaderFields(connection.getHeaderFields());
        result.setHttpCode(connection.getResponseCode());
    }

    /**
     * Maximum number of requests that can be executed simultaneously
     * by calling {@link #submit()}
     */
    public static int getParallelRequestsLimit() {
        return InternalExecutors.getHttpRequestExecutor().getPoolSize();
    }

    /**
     * Maximum number of requests that can be executed simultaneously
     * by calling {@link #submit()}
     */
    public static void setParallelRequestsLimit(
            @IntRange(from = 1, to = Integer.MAX_VALUE) int limit) {
        ThreadPoolExecutor executor = InternalExecutors.getHttpRequestExecutor();
        executor.setCorePoolSize(limit);
        executor.setMaximumPoolSize(limit);
    }

    /**
     * Create new GET HTTP request
     *
     * @param url              URL-address
     * @param headerParameters Request header parameters
     * @param queryParameters  Query string parameters
     * @param callbacks        Response callbacks
     */
    @NonNull
    public static HttpRequest newGetRequest(@NonNull String url,
            @Nullable Iterable<HttpHeaderParameter> headerParameters,
            @Nullable Iterable<HttpQueryParameter> queryParameters,
            @Nullable Iterable<HttpRequestCallback> callbacks) {
        return new GetHttpRequest(url, headerParameters, queryParameters, callbacks);
    }

    /**
     * Create new GET HTTP request builder
     *
     * @param url URL-address
     * @return New GET request builder instance
     */
    @NonNull
    public static GetHttpRequestBuilder newGetBuilder(@NonNull String url) {
        return new GetHttpRequestBuilder(url);
    }

    /**
     * Create new POST HTTP request
     *
     * @param url              URL-address
     * @param headerParameters Request header parameters
     * @param queryParameters  Query string parameters
     * @param bodyParameters   Request body multipart/form-data parameters
     * @param callbacks        Response callbacks
     */
    @NonNull
    public static HttpRequest newPostRequest(@NonNull String url,
            @Nullable Iterable<HttpHeaderParameter> headerParameters,
            @Nullable Iterable<HttpQueryParameter> queryParameters,
            @Nullable Iterable<HttpBodyParameter> bodyParameters,
            @Nullable Iterable<HttpRequestCallback> callbacks) {
        return new PostHttpRequest(url, headerParameters, queryParameters, bodyParameters,
                callbacks);
    }

    /**
     * Create new POST HTTP request builder
     *
     * @param url URL-address
     * @return New POST request instance
     */
    @NonNull
    public static PostHttpRequestBuilder newPostBuilder(@NonNull String url) {
        return new PostHttpRequestBuilder(url);
    }

    /**
     * Create new request header parameter
     *
     * @param key   Key
     * @param value Value
     * @return New request header parameter
     */
    @NonNull
    public static HttpHeaderParameter newHeaderParameter(@NonNull String key,
            @NonNull String value) {
        return new HttpHeaderParameter(key, value);
    }

    /**
     * Create new query string request parameter
     *
     * @param key   Key
     * @param value Value
     * @return New query string request parameter
     */
    @NonNull
    public static HttpQueryParameter newQueryParameter(@NonNull String key,
            @Nullable String value) {
        return new HttpQueryParameter(key, value);
    }

    /**
     * Create new POST request body parameter
     *
     * @param key   Key
     * @param value Value
     * @return New POST request body parameter
     */
    @NonNull
    public static HttpBodyParameter newBodyParameter(@NonNull String key, @NonNull String value) {
        return new HttpBodyParameter(key, value);
    }

    /**
     * Create new POST request body parameter
     *
     * @param key  Key
     * @param file File
     * @return New POST request body parameter
     */
    @NonNull
    public static HttpBodyParameter newBodyParameter(@NonNull String key, @NonNull File file) {
        return new HttpBodyParameter(key, file);
    }

    /**
     * Create new POST request body parameter
     *
     * @param key         Key
     * @param inputStream Input stream
     * @param fileName    File name
     * @param contentType Content type
     * @return New POST request body parameter
     */
    @NonNull
    public static HttpBodyParameter newBodyParameter(@NonNull String key,
            @NonNull InputStream inputStream, @NonNull String fileName,
            @NonNull String contentType) {
        return new HttpBodyParameter(key, inputStream, fileName, contentType);
    }

    /**
     * Submit request for asynchronous execution
     *
     * @return a {@link Future} representing pending completion of the request
     */
    @NonNull
    public abstract Future<HttpRequestResult> submit();

    /**
     * Execute request immediately
     *
     * @return Request result
     */
    @NonNull
    public abstract HttpRequestResult execute();
}
