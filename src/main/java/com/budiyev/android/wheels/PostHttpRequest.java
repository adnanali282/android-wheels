/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2016 Yuriy Budiyev [yuriy.budiyev@yandex.ru]
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Instance class for POST HTTP requests
 */
public final class PostHttpRequest extends HttpRequest {
    private static final String UTF_8 = "UTF-8";
    private static final String POST = "POST";
    private static final String KEY_CONTENT_TYPE = "Content-Type";
    private static final String MULTIPART_FORM_DATA = "multipart/form-data; boundary=";
    private static final String KEY_ACCEPT_CHARSET = "Accept-Charset";
    private static final String LINE_END = "\r\n";
    private static final String KEY_CONNECTION = "Connection";
    private static final String KEEP_ALIVE = "keep-alive";
    private static final String CONTENT_DISPOSITION = "Content-Disposition: form-data; name=\"";
    private static final String QUOTE = "\"";
    private static final String DOUBLE_DASH = "--";
    private static final String CONTENT_TYPE_REQUEST = "Content-Type: ";
    private static final String PLAIN_TEXT = "text/plain; charset=" + UTF_8;
    private static final String FILENAME = "; filename=\"";
    private static final String CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding: ";
    private static final String BINARY = "binary";
    private static final int CONNECTION_TIMEOUT = 10000;
    private static final int BUFFER_SIZE = 4096;
    private final Object mResultLock = new Object();
    private final String mUrl;
    private final Iterable<HeaderParameter> mHeaderParameters;
    private final Iterable<QueryParameter> mQueryParameters;
    private final Iterable<PostParameter> mPostParameters;
    private final RequestCallback mCallback;
    private final RequestResultType mResultType;
    private volatile RequestResult mResult;

    private final Callable<RequestResult> mRequestAction = new Callable<RequestResult>() {
        @Override
        public RequestResult call() throws Exception {
            HttpURLConnection connection = null;
            RequestResult result = RequestResult.NONE;
            mResult = null;
            try {
                String boundary = "===" + System.currentTimeMillis() + "===";
                String request = mUrl;
                if (!CommonUtils.isNullOrEmpty(mQueryParameters)) {
                    request += "?" + buildParamsUrlString(mQueryParameters, UTF_8);
                }
                URL url = new URL(request);
                connection = (HttpURLConnection) url.openConnection();
                connection.setUseCaches(false);
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod(POST);
                connection.setRequestProperty(KEY_ACCEPT_CHARSET, UTF_8);
                connection.setRequestProperty(KEY_CONTENT_TYPE, MULTIPART_FORM_DATA + boundary);
                connection.setRequestProperty(KEY_CONNECTION, KEEP_ALIVE);
                if (!CommonUtils.isNullOrEmpty(mHeaderParameters)) {
                    for (HeaderParameter parameter : mHeaderParameters) {
                        if (parameter.key != null && parameter.value != null) {
                            connection.setRequestProperty(parameter.key, parameter.value);
                        }
                    }
                }
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
                OutputStream outputStream = connection.getOutputStream();
                try (PrintWriter writer = new PrintWriter(
                        new OutputStreamWriter(outputStream, UTF_8), false)) {
                    if (!CommonUtils.isNullOrEmpty(mPostParameters)) {
                        for (PostParameter postParameter : mPostParameters) {
                            if (postParameter.key != null) {
                                if (postParameter.value != null) {
                                    writer.append(DOUBLE_DASH).append(boundary).append(LINE_END)
                                            .append(CONTENT_DISPOSITION).append(postParameter.key)
                                            .append(QUOTE).append(LINE_END)
                                            .append(CONTENT_TYPE_REQUEST).append(PLAIN_TEXT)
                                            .append(LINE_END).append(LINE_END)
                                            .append(postParameter.value).append(LINE_END).flush();
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
                                        outputStream.flush();
                                    }
                                    writer.append(LINE_END).flush();
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
                                        outputStream.flush();
                                    }
                                    writer.append(LINE_END).flush();
                                }
                            }
                        }
                    }
                    writer.append(DOUBLE_DASH).append(boundary).append(DOUBLE_DASH).append(LINE_END)
                            .flush();
                }
                outputStream.flush();
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

    PostHttpRequest(@NonNull String url, @Nullable Iterable<HeaderParameter> headerParameters,
            @Nullable Iterable<QueryParameter> queryParameters,
            @Nullable Iterable<PostParameter> postParameters, @NonNull RequestResultType resultType,
            @Nullable RequestCallback callback) {
        mUrl = url;
        mHeaderParameters = headerParameters;
        mQueryParameters = queryParameters;
        mPostParameters = postParameters;
        mCallback = callback;
        mResultType = resultType;
    }

    @NonNull
    @Override
    public Future<RequestResult> execute() {
        return ThreadUtils.runAsync(mRequestAction);
    }

    @NonNull
    @Override
    public RequestResult getResult() {
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
        try {
            return mRequestAction.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        try {
            mRequestAction.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
