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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Instance class for GET HTTP requests
 */
final class GetHttpRequest extends HttpRequest {
    private static final String UTF_8 = "UTF-8";
    private static final String GET = "GET";
    private static final String ACCEPT_CHARSET = "Accept-Charset";
    private static final int CONNECTION_TIMEOUT = 10000;
    private static final int BUFFER_SIZE = 4096;
    private final Object mResultLock = new Object();
    private final String mUrl;
    private final Iterable<HeaderParameter> mHeaderParameters;
    private final Iterable<QueryParameter> mQueryParameters;
    private final RequestCallback mCallback;
    private final RequestResultType mResultType;
    private volatile RequestResult mResult;
    private volatile boolean mHasBeenExecuted;

    private final Callable<RequestResult> mRequestAction = new Callable<RequestResult>() {
        @Override
        public RequestResult call() throws Exception {
            HttpURLConnection connection = null;
            RequestResult result = RequestResult.NONE;
            try {
                String request = mUrl;
                if (!CommonUtils.isNullOrEmpty(mQueryParameters)) {
                    request += "?" + buildParamsUrlString(mQueryParameters, UTF_8);
                }
                URL url = new URL(request);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod(GET);
                connection.setRequestProperty(ACCEPT_CHARSET, UTF_8);
                if (!CommonUtils.isNullOrEmpty(mHeaderParameters)) {
                    for (HeaderParameter parameter : mHeaderParameters) {
                        if (parameter.key != null && parameter.value != null) {
                            connection.setRequestProperty(parameter.key, parameter.value);
                        }
                    }
                }
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    switch (mResultType) {
                        case STREAM: {
                            result = RequestResult.SUCCESS;
                            result.setConnection(connection);
                            result.setHttpCode(responseCode);
                            result.setDataStream(connection.getInputStream());
                            break;
                        }
                        case STRING: {
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
                                result = RequestResult.SUCCESS;
                                result.setConnection(connection);
                                result.setHttpCode(responseCode);
                                result.setDataString(responseBuilder.toString());
                            }
                            break;
                        }
                    }
                } else {
                    result = RequestResult.ERROR_HTTP;
                    result.setConnection(connection);
                    result.setHttpCode(responseCode);
                }
            } catch (MalformedURLException e) {
                result = RequestResult.ERROR_MALFORMED_URL;
                result.setConnection(connection);
                result.setException(e);
            } catch (UnsupportedEncodingException e) {
                result = RequestResult.ERROR_UNSUPPORTED_ENCODING;
                result.setConnection(connection);
                result.setException(e);
            } catch (ProtocolException e) {
                result = RequestResult.ERROR_PROTOCOL;
                result.setConnection(connection);
                result.setException(e);
            } catch (IOException e) {
                result = RequestResult.ERROR_IO;
                result.setConnection(connection);
                result.setException(e);
            } catch (Exception e) {
                result = RequestResult.ERROR_UNEXPECTED;
                result.setConnection(connection);
                result.setException(e);
            }
            if (mCallback != null) {
                mCallback.onResult(result);
            }
            mResult = result;
            synchronized (mResultLock) {
                mResultLock.notifyAll();
            }
            return result;
        }
    };

    GetHttpRequest(@NonNull String url, @Nullable Iterable<HeaderParameter> headerParameters,
            @Nullable Iterable<QueryParameter> queryParameters,
            @NonNull RequestResultType resultType, @Nullable RequestCallback callback) {
        mUrl = url;
        mHeaderParameters = headerParameters;
        mQueryParameters = queryParameters;
        mCallback = callback;
        mResultType = resultType;
    }

    private void prepareExecution() {
        mResult = null;
        mHasBeenExecuted = true;
    }

    @NonNull
    @Override
    public Future<RequestResult> execute() {
        prepareExecution();
        return ExecutorUtils.getHttpRequestExecutor().submit(mRequestAction);
    }

    @NonNull
    @Override
    public RequestResult getResult() {
        if (!mHasBeenExecuted) {
            throw new IllegalStateException();
        }
        RequestResult result;
        for (; ; ) {
            result = mResult;
            if (result != null) {
                break;
            }
            try {
                mResultLock.wait();
            } catch (InterruptedException ignored) {
            }
        }
        return result;
    }

    @NonNull
    @Override
    public RequestResult executeAndGetResult() {
        prepareExecution();
        try {
            return mRequestAction.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        prepareExecution();
        try {
            mRequestAction.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
