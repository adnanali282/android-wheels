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
import java.io.IOException;
import java.io.InputStreamReader;
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
    private final Iterable<HeaderParameter> mHeaderParameters;
    private final Iterable<QueryParameter> mQueryParameters;
    private final Iterable<RequestCallback> mCallbacks;
    private final int mDataType;

    private final Callable<RequestResult> mRequestAction = new Callable<RequestResult>() {
        @Override
        public RequestResult call() throws Exception {
            HttpURLConnection connection = null;
            RequestResult result = new RequestResult();
            try {
                String query = mUrl;
                if (!CollectionUtils.isNullOrEmpty(mQueryParameters)) {
                    query += "?" + buildParamsUrlString(mQueryParameters, CHARSET_UTF_8);
                }
                connection = openHttpUrlConnection(query);
                connection.setRequestMethod(REQUEST_METHOD_GET);
                connection.setRequestProperty(KEY_ACCEPT_CHARSET, CHARSET_UTF_8);
                if (mHeaderParameters != null) {
                    for (HeaderParameter parameter : mHeaderParameters) {
                        if (parameter.key != null && parameter.value != null) {
                            connection.setRequestProperty(parameter.key, parameter.value);
                        }
                    }
                }
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
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

    GetHttpRequest(@NonNull String url, @Nullable Iterable<HeaderParameter> headerParameters,
            @Nullable Iterable<QueryParameter> queryParameters,
            @Nullable Iterable<RequestCallback> callbacks, @RequestResult.DataType int dataType) {
        mUrl = Objects.requireNonNull(url);
        mHeaderParameters = headerParameters;
        mQueryParameters = queryParameters;
        mCallbacks = callbacks;
        mDataType = dataType;
    }

    @NonNull
    @Override
    public Future<RequestResult> submit() {
        return InternalExecutors.getHttpRequestExecutor().submit(mRequestAction);
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
