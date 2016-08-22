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

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URLConnection;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Instance class for POST HTTP requests
 */
final class PostHttpRequest extends HttpRequest {
    private static final String KEY_CONTENT_TYPE = "Content-Type";
    private static final String KEY_CONNECTION = "Connection";
    private static final String MULTIPART_FORM_DATA = "multipart/form-data; boundary=";
    private static final String LINE_END = "\r\n";
    private static final String KEEP_ALIVE = "keep-alive";
    private static final String CONTENT_DISPOSITION = "Content-Disposition: form-data; name=\"";
    private static final String QUOTE = "\"";
    private static final String DOUBLE_DASH = "--";
    private static final String CONTENT_TYPE_REQUEST = "Content-Type: ";
    private static final String PLAIN_TEXT = "text/plain; charset=" + CHARSET_UTF_8;
    private static final String FILENAME = "; filename=\"";
    private static final String CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding: ";
    private static final String BINARY = "binary";
    private static final int BUFFER_SIZE = 8192;
    private final String mUrl;
    private final Iterable<HttpHeaderParameter> mHeaderParameters;
    private final Iterable<HttpQueryParameter> mQueryParameters;
    private final Iterable<HttpBodyParameter> mBodyParameters;
    private final Iterable<HttpRequestCallback> mCallbacks;

    private final Callable<HttpRequestResult> mRequestAction = new Callable<HttpRequestResult>() {
        @Override
        public HttpRequestResult call() throws Exception {
            HttpURLConnection connection = null;
            HttpRequestResult result = new HttpRequestResult();
            try {
                String boundary = "===" + System.currentTimeMillis() + "===";
                String query = mUrl;
                if (!CollectionUtils.isNullOrEmpty(mQueryParameters)) {
                    query += "?" + buildParamsUrlString(mQueryParameters, CHARSET_UTF_8);
                }
                connection = openHttpUrlConnection(query);
                connection.setDoInput(true);
                connection.setRequestMethod(REQUEST_METHOD_POST);
                connection.setRequestProperty(KEY_ACCEPT_CHARSET, CHARSET_UTF_8);
                connection.setRequestProperty(KEY_CONTENT_TYPE, MULTIPART_FORM_DATA + boundary);
                connection.setRequestProperty(KEY_CONNECTION, KEEP_ALIVE);
                if (!CollectionUtils.isNullOrEmpty(mHeaderParameters)) {
                    addHeaderParameters(connection, mHeaderParameters);
                }
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
                if (!CollectionUtils.isNullOrEmpty(mBodyParameters)) {
                    connection.setDoOutput(true);
                    OutputStream outputStream = connection.getOutputStream();
                    try (BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(outputStream, CHARSET_UTF_8))) {
                        for (HttpBodyParameter bodyParameter : mBodyParameters) {
                            if (bodyParameter.value != null) {
                                writer.append(DOUBLE_DASH).append(boundary).append(LINE_END)
                                        .append(CONTENT_DISPOSITION).append(bodyParameter.key)
                                        .append(QUOTE).append(LINE_END).append(CONTENT_TYPE_REQUEST)
                                        .append(PLAIN_TEXT).append(LINE_END).append(LINE_END)
                                        .append(bodyParameter.value).append(LINE_END);
                            } else if (bodyParameter.file != null) {
                                String fileName = bodyParameter.file.getName();
                                String contentType =
                                        URLConnection.guessContentTypeFromName(fileName);
                                writer.append(DOUBLE_DASH).append(boundary).append(LINE_END)
                                        .append(CONTENT_DISPOSITION).append(bodyParameter.key)
                                        .append(QUOTE).append(FILENAME).append(fileName)
                                        .append(QUOTE).append(LINE_END).append(CONTENT_TYPE_REQUEST)
                                        .append(contentType).append(LINE_END)
                                        .append(CONTENT_TRANSFER_ENCODING).append(BINARY)
                                        .append(LINE_END).append(LINE_END).flush();
                                try (FileInputStream fileInput = new FileInputStream(
                                        bodyParameter.file)) {
                                    byte[] buffer = new byte[BUFFER_SIZE];
                                    for (int read; (read = fileInput.read(buffer)) != -1; ) {
                                        outputStream.write(buffer, 0, read);
                                    }
                                }
                                writer.append(LINE_END);
                            } else if (bodyParameter.stream != null &&
                                    bodyParameter.fileName != null &&
                                    bodyParameter.contentType != null) {
                                writer.append(DOUBLE_DASH).append(boundary).append(LINE_END)
                                        .append(CONTENT_DISPOSITION).append(bodyParameter.key)
                                        .append(QUOTE).append(FILENAME)
                                        .append(bodyParameter.fileName).append(QUOTE)
                                        .append(LINE_END).append(CONTENT_TYPE_REQUEST)
                                        .append(bodyParameter.contentType).append(LINE_END)
                                        .append(CONTENT_TRANSFER_ENCODING).append(BINARY)
                                        .append(LINE_END).append(LINE_END).flush();
                                try (InputStream inputStream = bodyParameter.stream) {
                                    byte[] buffer = new byte[BUFFER_SIZE];
                                    for (int read; (read = inputStream.read(buffer)) != -1; ) {
                                        outputStream.write(buffer, 0, read);
                                    }
                                }
                                writer.append(LINE_END);
                            }
                        }
                        writer.append(DOUBLE_DASH).append(boundary).append(DOUBLE_DASH)
                                .append(LINE_END).flush();
                    }
                }
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

    PostHttpRequest(@NonNull String url, @Nullable Iterable<HttpHeaderParameter> headerParameters,
            @Nullable Iterable<HttpQueryParameter> queryParameters,
            @Nullable Iterable<HttpBodyParameter> bodyParameters,
            @Nullable Iterable<HttpRequestCallback> callbacks) {
        mUrl = Objects.requireNonNull(url);
        mHeaderParameters = headerParameters;
        mQueryParameters = queryParameters;
        mBodyParameters = bodyParameters;
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
}
