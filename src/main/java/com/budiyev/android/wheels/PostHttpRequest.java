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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    private final String mUrl;
    private final Iterable<HeaderParameter> mHeaderParameters;
    private final Iterable<QueryParameter> mQueryParameters;
    private final Iterable<PostParameter> mPostParameters;
    private final Iterable<RequestCallback> mCallbacks;
    private final int mDataType;

    private final Callable<RequestResult> mRequestAction = new Callable<RequestResult>() {
        @Override
        public RequestResult call() throws Exception {
            HttpURLConnection connection = null;
            RequestResult result = new RequestResult();
            try {
                String boundary = "===" + System.currentTimeMillis() + "===";
                String query = mUrl;
                if (!CommonUtils.isNullOrEmpty(mQueryParameters)) {
                    query += "?" + buildParamsUrlString(mQueryParameters, CHARSET_UTF_8);
                }
                connection = openHttpUrlConnection(query);
                connection.setUseCaches(false);
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod(REQUEST_METHOD_POST);
                connection.setRequestProperty(KEY_ACCEPT_CHARSET, CHARSET_UTF_8);
                connection.setRequestProperty(KEY_CONTENT_TYPE, MULTIPART_FORM_DATA + boundary);
                connection.setRequestProperty(KEY_CONNECTION, KEEP_ALIVE);
                if (mHeaderParameters != null) {
                    for (HeaderParameter parameter : mHeaderParameters) {
                        if (parameter.key != null && parameter.value != null) {
                            connection.setRequestProperty(parameter.key, parameter.value);
                        }
                    }
                }
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
                OutputStream outputStream = connection.getOutputStream();
                try (BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(outputStream, CHARSET_UTF_8))) {
                    if (mPostParameters != null) {
                        for (PostParameter postParameter : mPostParameters) {
                            if (postParameter.key != null) {
                                if (postParameter.value != null) {
                                    writer.append(DOUBLE_DASH).append(boundary).append(LINE_END)
                                            .append(CONTENT_DISPOSITION).append(postParameter.key)
                                            .append(QUOTE).append(LINE_END)
                                            .append(CONTENT_TYPE_REQUEST).append(PLAIN_TEXT)
                                            .append(LINE_END).append(LINE_END)
                                            .append(postParameter.value).append(LINE_END);
                                } else if (postParameter.file != null) {
                                    String fileName = postParameter.file.getName();
                                    String contentType =
                                            URLConnection.guessContentTypeFromName(fileName);
                                    writer.append(DOUBLE_DASH).append(boundary).append(LINE_END)
                                            .append(CONTENT_DISPOSITION).append(postParameter.key)
                                            .append(QUOTE).append(FILENAME).append(fileName)
                                            .append(QUOTE).append(LINE_END)
                                            .append(CONTENT_TYPE_REQUEST).append(contentType)
                                            .append(LINE_END).append(CONTENT_TRANSFER_ENCODING)
                                            .append(BINARY).append(LINE_END).append(LINE_END)
                                            .flush();
                                    try (FileInputStream fileInput = new FileInputStream(
                                            postParameter.file)) {
                                        byte[] buffer = new byte[BUFFER_SIZE];
                                        for (int read; (read = fileInput.read(buffer)) != -1; ) {
                                            outputStream.write(buffer, 0, read);
                                        }
                                    }
                                    writer.append(LINE_END);
                                } else if (postParameter.stream != null &&
                                        postParameter.fileName != null &&
                                        postParameter.contentType != null) {
                                    writer.append(DOUBLE_DASH).append(boundary).append(LINE_END)
                                            .append(CONTENT_DISPOSITION).append(postParameter.key)
                                            .append(QUOTE).append(FILENAME)
                                            .append(postParameter.fileName).append(QUOTE)
                                            .append(LINE_END).append(CONTENT_TYPE_REQUEST)
                                            .append(postParameter.contentType).append(LINE_END)
                                            .append(CONTENT_TRANSFER_ENCODING).append(BINARY)
                                            .append(LINE_END).append(LINE_END).flush();
                                    try (InputStream inputStream = postParameter.stream) {
                                        byte[] buffer = new byte[BUFFER_SIZE];
                                        for (int read; (read = inputStream.read(buffer)) != -1; ) {
                                            outputStream.write(buffer, 0, read);
                                        }
                                    }
                                    writer.append(LINE_END);
                                }
                            }
                        }
                    }
                    writer.append(DOUBLE_DASH).append(boundary).append(DOUBLE_DASH).append(LINE_END)
                            .flush();
                }
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    switch (mDataType) {
                        case RequestResult.NONE: {
                            result.setResultType(RequestResult.SUCCESS);
                            result.setConnection(connection);
                            result.setHttpCode(responseCode);
                            break;
                        }
                        case RequestResult.STRING: {
                            try (BufferedReader bufferedReader = new BufferedReader(
                                    new InputStreamReader(connection.getInputStream()))) {
                                StringBuilder responseBuilder = new StringBuilder();
                                char[] buffer = new char[BUFFER_SIZE];
                                for (; ; ) {
                                    int read = bufferedReader.read(buffer);
                                    if (read > -1) {
                                        responseBuilder.append(buffer, 0, read);
                                    } else {
                                        break;
                                    }
                                }
                                result.setResultType(RequestResult.SUCCESS);
                                result.setDataType(RequestResult.STRING);
                                result.setConnection(connection);
                                result.setHttpCode(responseCode);
                                result.setString(responseBuilder.toString());
                            }
                            break;
                        }
                        case RequestResult.STREAM: {
                            result.setResultType(RequestResult.SUCCESS);
                            result.setDataType(RequestResult.STREAM);
                            result.setConnection(connection);
                            result.setHttpCode(responseCode);
                            result.setStream(connection.getInputStream());
                            break;
                        }
                    }
                } else {
                    result.setResultType(RequestResult.ERROR_HTTP);
                    result.setConnection(connection);
                    result.setHttpCode(responseCode);
                }
            } catch (MalformedURLException e) {
                result.setResultType(RequestResult.ERROR_MALFORMED_URL);
                result.setConnection(connection);
                result.setException(e);
            } catch (UnsupportedEncodingException e) {
                result.setResultType(RequestResult.ERROR_UNSUPPORTED_ENCODING);
                result.setConnection(connection);
                result.setException(e);
            } catch (ProtocolException e) {
                result.setResultType(RequestResult.ERROR_PROTOCOL);
                result.setConnection(connection);
                result.setException(e);
            } catch (IOException e) {
                result.setResultType(RequestResult.ERROR_IO);
                result.setConnection(connection);
                result.setException(e);
            } catch (Exception e) {
                result.setResultType(RequestResult.ERROR_UNEXPECTED);
                result.setConnection(connection);
                result.setException(e);
            }
            if (mCallbacks != null) {
                for (RequestCallback callback : mCallbacks) {
                    if (callback != null) {
                        callback.onResult(result);
                    }
                }
            }
            return result;
        }
    };

    PostHttpRequest(@NonNull String url, @Nullable Iterable<HeaderParameter> headerParameters,
            @Nullable Iterable<QueryParameter> queryParameters,
            @Nullable Iterable<PostParameter> postParameters,
            @Nullable Iterable<RequestCallback> callbacks, @RequestResult.DataType int dataType) {
        mUrl = Objects.requireNonNull(url);
        mHeaderParameters = headerParameters;
        mQueryParameters = queryParameters;
        mPostParameters = postParameters;
        mCallbacks = callbacks;
        mDataType = dataType;
    }

    @NonNull
    @Override
    public Future<RequestResult> submit() {
        return ExecutorUtils.getHttpRequestExecutor().submit(mRequestAction);
    }

    @NonNull
    @Override
    public RequestResult execute() {
        try {
            return mRequestAction.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
